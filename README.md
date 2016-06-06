# RESTGaga

## 简介

RESTGaga是一个用于针对RESTFul接口进行单元/集成测试的框架。核心特色如下：

1. 基于Groovy开发，但使用者不必掌握任何编程语言，通过DSL就可以编写绝大多数测试用例，这意味着项目中的各个角色都可以方便参与测试。同时因为Groovy是可以跟Java无缝集成的语言，您可以在DSL中调用任何Java API甚至是使用Java语法，满足您各种非同一般的需求。
2. 自由的形式，RESTGaga对工程结构没有什么特殊要求，您可以随意按照您的习惯来组织您的测试用例。
3. 灵活与专业，有很多测试框架，比如Cucumber、Robot Framework，也能针对RESTFul接口进行测试，但是RESTGaga是专门用于API测试的，提供了一些专为API测试而生的功能。
4. 使用Mock提升前后端联调效率，RESTGaga提供了接口Mock功能，不必等后端的接口开发完毕，前端就可以开始工作。

## 安装

<b>安装要求：</b>

1. JDK 1.8
2. Gradle

<b>安装步骤：</b>

直接clone工程到本地，运行 `gradle installDist` 即可实现编译安装。

看下版本：

```
build/install/restgaga/bin/restgaga -v
```

可以将`build/install/restgaga/bin/`加入到PATH中，方便运行。

这里提供了一个完整的测试工程例子：[restgaga-example](https://github.com/chihongze/restgaga-example)，Clone到本地后，在当前目录运行:

```
restgaga -m 8080
```

这是启动mock服务

然后再直接运行

```
restgaga
```

就会自动运行所有测试用例


## DSL元素介绍

一个完整的测试程序文件示例：

```groovy
connectionTimeout = 500
socketTimeout = 500

httpProxy = "127.0.0.1:8081"

globalHeaders (
	Accept: JSON_TYPE,
	"Content-Type": JSON_TYPE
)

global.x = "hello"

env test {
	baseUrl 'http://localhost:8080'
}

def getSign(Map body) {
	"sign"
}

fixture token {
	setUp {
		println global.x
	}
	tearDown {
	
	}
}

fixture sign {
	setUp {
		headers["sign"] = getSign(body)
	}
	tearDown {
	
	}
}

globalFixtures << [ "token", "sign" ]

mockInterceptor checkToken {
	...
}

api login {
	post '/v1/user/login'
	fixtures ^ [ "sign" ]
}

mock login {
	validate {
		usernameRequired true, [ code: 400, msg: "username required" ]
		usernameMinLength 6, [ code: 400, msg: "username too short" ]
		usernameMaxLength 20, [ code: 400, msg: "username too long" ]
		
		passwordRequired true, [ code: 400, msg: "password required" ]
	}
	
	def jsonBody = req.jsonBody()
	def (username, password) = [ jsonBody.username, jsonBody.password ]
	
	if (username == "samchi" && password == "123456") {
		[ code: 200, msg: "success", token: "123456"]
	} else {
		[ code: 400, msg: "password error" ]
	}
}

testcase login {
	success
	setUp {
	
	}
	headers (
		a: "aaaa"
	)
	mapBody (
		username: "samchi",
		password: "123456"
	)
	responseBody (
		code: 200,
		msg: Ignore
	)
	tearDown {
	
	}
}

testcase login {
	passwordErr
	mapBody (
		username: "samchi",
		password: "654321"
	)
	responseBody (
		code: 400,
		msg: predict { it == "password error" }
	)
}

api profile {
	get '/v1/user/profile/{id}'
}

mock profile {
	def userId = Integer.valueOf(req.params("id"))
	[ code: 200, profile: [ id: userId, username: "SamChi" ] ]
}

testcase profile {
	success
	withEnv "test", "pro"
	pathVariables (
		id: 100
	)
	responseBody (
		code: 200,
		profile: [
			id: 100,
			username: "SamChi"
		]
	)
}

testsuite loginThenViewProfile {
	test "login.success"
	test "profile.success"
}

reportAction mail { result, args ->
	def mailContent = result2html(result)
	sendMail(args.from, args.receivers, args.subject, mailContent)
}

report testMail {
	action "mail"
	env "test", "localTest"
	args (
		from: "test@hehe.com",
		receivers: [ "tech@hehe.com", "pm@hehe.com" ],
		subject: "api test result",
	)
}

report proMail {
	action "mail"
	env "pro"
	args (
		from "test@hehe.com",
		receivers: [ "boss@hehe.com", "tech@hehe.com", "pm@hehe.com" ],
		subject: "api test result"
	)
}
```

### 运行时常量

* connectionTimeout: 建立连接的超时时间，单位为毫秒，如果为0，则表示忽略，默认忽略
* socketTimeout: 执行socket读取操作的超时时间，单位为毫秒，如果为0，则表示忽略，默认忽略
* httpProxy: 如果需要使用代理服务器进行测试，那么请配置该项，格式为"host:port"

### globalHeaders

globalHeaders用来配置每次测试都使用的默认请求头。

### 全局变量

global对象是一个类型为ConcurrentHashMap的全局命名空间，可以通过它来设置全局变量。你可以在fixtures、setUp、tearDown等位置使用该全局变量。

### env

通常我们会在不同的环境下进行测试，比如本地开发环境、内网测试环境、线上正式环境等，通过配置env，可以对这些环境进行区分。

### fixture

同很多测试套件中的fixture一样，提供setUp和tearDown两个动作，分别完成测试之前的准备和清理工作。

通过globalFixtures可以指定所有的测试用例默认需要执行的fixture。

fixture的setUp会在testcase的setUp之前执行，fixture的tearDown会在testcase的tearDown之后执行，setUp和tearDown都会被指向一个叫做TestCaseContext的上下文，该上下文中的属性如下：

	global: 全局命名空间，通过它引用全局变量
	currentEnv: 当前运行环境
	headers: http请求头，可读写的map，您可以在这里添加新的header项。
	params: http请求参数，可读写的map
	pathVariables: path变量，用于'/v1/user/profile/{id}'这种情形，可读写map
	body: POST和PUT请求的请求体，如果Content-Type为application/json类型或者url encode编码的键值对，那么body是一个map，其它的情况为byte数组。

在setUp和tearDown函数中可以直接使用这些变量。

### api

api元素用于声明一个RESTful接口，包含请求方式(GET\POST\PUT\DELETE)、测试时默认使用的header、请求参数、pathVariables以及fixtures

```
api login {
	post '/v1/path/{a}'
	headers (
		a: "a",
		b: "a",
	)
	params (
		a: "a",
		b: "b"
	)
	pathVariables (
		a: "a"
	)
	fixtures << [ "a", "b" ]
}
```

fixtures支持<<和^两种操作，<<的意思表示在默认的基础上进行追加，比如globalFixtures为["a", "b"]，那么fixtures << ["c", "d"]之后将成为["a", "b", "c", "d"]；^的意思为覆盖，fixtures ^ ["c", "d"]最终的列表就是["c", "d"]

fixture将按照最终列表的顺序来执行。

### mock

mock功能基于[spark web](http://sparkjava.com/documentation.html)框架构建，可以直接在mock函数上引用spark.Request和spark.Response这两个对象，关于这两个对象的属性和方法，可以直接参见spark文档。

另外，对于有参数验证的需求，mock提供了validate功能：

```
usernameRequired true, [ code: 400, msg: "username required" ]
```

这条表示username字段在body中是必须的，如果username为空，那么将返回code为400的json。

其它的验证条件：

```groovy
// 最小长度
usernameMinLength 6, [ code: 400, msg: "username too short" ]
// 最大长度
usernameMaxLength 20, [ code: 400, msg: "username too long" ]
// 正则验证
usernameRegex '^[a-zA-Z0-9_]+$', [ code: 400, msg: "invalid format"]
// 逻辑验证
usernameLogic({ isAdmin(it) }, [ code: 400, msg: "only admin" ])
```


validate最好声明在正式业务逻辑之前

### mockInterceptor

mockInterceptor会拦截所有的mock请求，同样可以直接在其闭包中使用spark.Request和spark.Response对象。如果希望继续处理，那么什么都不用做；如果希望中断处理，那么可以调用halt函数，例如：

```
mockInterceptor onlyLogin {
	def path = req.pathInfo()
	if (path == '/v1/user/login') {
		return
	}
	halt(200, [code: 400, msg: "only allow login operation"])
}
```

halt函数支持的形态：
	
	halt()
	halt(int status)
	halt(String body)
	halt(Map body) // body会被编码为json
	halt(int status, String body)
	halt(int status, Map body)
	
### testcase

testcase用于声明隶属某个api的测试用例

```
testcase login {
	// 测试用例名称
	success
	// 支持的测试环境
	withEnv "test", "pro"
	// 准备工作
	setUp {
		
	}
	// 请求header
	headers (
		
	)
	// 路径变量
	pathVariables (
		
	)
	// 附着在路径上的请求参数
	params (
		
	)
	// 请求体
	mapBody (
		username: "samchi",
		password: "123456"
	)
	// 期望返回的Header
	responseHeaders (
		"Content-Type": JSON_TYPE
	)
	// 期望返回的Body
	responseBody (
		code: 200,
		msg: Ignore
	)
}
```

withEnv: 支持运行的环境，如果当前环境不匹配，那么将不会加载该测试用例。

setUp: 运行完所有fixture的setUp后执行，同样指定了TestCaseContext的上下文。

headers: 需要注意headers的更新顺序，依次是globalHeaders -> envHeaders -> apiHeaders -> testCaseHeaders -> fixtures setUp-> testCase setUp。按照这个顺序进行优先级覆盖。

pathVariables: 更新覆盖顺序同上

params: 通常用于指定url上附加的参数，比如/v1/userList?begin=1&limit=10，更新覆盖顺序同上。
	
body: 当使用POST和PUT请求时，需要指定请求体，请求体分为mapBody、fileBody、stringBody、bytesBody这几种。

	mapBody: 适应于body为kv对的时候，当Content-Type为json，则会编码为json，其余的情况会编码为url encode。
	
	fileBody: 直接指定一个文件路径，则该文件的内容就是请求体的内容。如果是相对路径，那么默认的相对点是工程目录。
	
	stringBody: 直接指定字符串作为body内容
	
	bytesBody: 直接指定byte数组为body内容，bytesBody可以接受一个返回byte[]的闭包，这就意味着您可以在闭包中吟诗作画。
	
responseHeaders: 期望得到的header，您只需要声明您关心的header项即可，其余的会忽略，header项的比较都是完全忽略大小写的。

responseBody: 期望得到的body，RESTGaga目前只支持json格式的body比较，支持无限嵌套比较（当然是在不会引发栈溢出的情况下）：

```
responseBody (
	code: 200,
	profile: [
		id: 1,
		name: "SamChi",
		friendList: [
			{id: 2, name: "Betty"},
		]
	]
)
```

所有的非集合类型的值都会作为字符串去进行比较，另外对于单个值，支持各种特殊形式的匹配，比如上例中的Ignore，意思就是忽略该项的值。

```
responseBody (
	a: Ignore,
	b: NotEmpty,
	c: regexp('^\\d+$'),
	d: predict {it.contains("xxx")},
	e: [
		{id: regexp('^\\d+$'), name: NotEmpty},
		Size(10)
	]
)
```

* Ignore: 忽略比较
* NotEmpty: 无论目标属性是什么值，该目标属性必须存在且不为空。
* regexp: 正则表达式
* predict: 逻辑判断，参数为服务端返回的该项参数。
* Size： 用于集合，期望的集合大小。

### testsuite

testsuite元素可以将多个测试用例编排在一起进行测试，这样就可以用来模拟一些用户的连贯动作，比如登录之后去访问商品页面，然后将商品加入购物车，再去结账。

```
testsuite buybuybuy {
	concurrent // 如果带有concurrent标记，则说明其中的测试可以并发执行，否则将会顺序执行。
	test "login.success"
	test "viewGoodsDetails.success"
	test "addToCart.success"
	test "order.success"
}
```

测试单元使用`api名称.测试用例`名称的格式来表示，也可以直接使用api名称，比如login，这样会执行该API中的所有测试用例，testsuite中也可以引用其它的testsuite名称。

### reportAction

无论做何种测试，最重要的是知道结果，restgaga通过将报告的动作类型和配置数据两者分离的方式来提供更加灵活的结果报告。

reportAction元素代表着一种报告动作，比如通过邮件、微信、短信发送。它由一个名称和一个Groovy闭包描述的动作构成，名称在整个工程中必须是唯一的，闭包接受两个参数，第一个参数是测试结果(即一个TestSuiteResult对象)，第二个是一个Map结构，用于为动作提供必要的参数。

在RESTGaga中，任何测试结果最后都会包装为一个TestSuiteResult对象，哪怕只是运行了一个TestCase，那也会被包装成只有一个元素的TestSuiteResult。reportAction的本质就是将TestSuiteResult渲染为报告文本的过程。

TestSuiteResult的属性：
	
	results: 包含TestSuiteResult或者TestCaseResult对象的结果列表
	
	statusCounts: 这是一个Map，包含各种状态的统计数目。每个TestCase运行都会产生以下几种状态 
		SUCCESS - 成功
		EXCEPTION - 异常
		STATUS_NO_MATCH - 状态号不匹配
		HEADER_NO_MATCH - 响应头不匹配
		BODY_NO_MATCH - BODY不匹配
		
		这些状态包含在TestStatus这个枚举当中
		
	usedTime: 整个TestSuite运行使用的时间
	
TestCaseResult的属性:

	status: 测试结果状态，即一个TestStatus对象
	
	usedTime: 整个测试过程使用的时间
	
	requestTime: HTTP请求所使用的时间
	
	expectedStatus: 期望得到的HTTP响应码
	
	actualStatus: 实际的HTTP响应码
	
	expectedHeaders: 期望得到的HTTP响应头
	
	actualHeaders: 实际得到的HTTP响应头
	
	differentHeaderItems: 所有异同的header项
	
	expectedBody: 期望得到的响应体
	
	actualBody: 实际的响应体 (JSONObject类型)
	
	differentBodyItem: 异同的body项
	
	exception: 如果status为EXCEPTION，那么这里保存了产生的异常对象。
	
RESTGaga默认实现了一个文本类型的reportAction: chihz.restgaga.report.TextReportAction，名字为default，您可以参照该类来编写自己的reportAction。

### report

可以将report元素看做是reportAction元素的一个实例，为reportAction提供了不同的参数配置，比如同样是邮件类型的报告，在测试环境下，收件人只需是开发人员即可，但是在线上环境进行daily test，结果可能要让领导知道，以便领导经常请测试不通过的开发人员喝茶。这种情况就可以通过一个action两个report实例的方式来实现，如上述测试代码所示。

## 测试工程结构

restgaga是以测试工程目录为目标进行加载的，会加载测试工程目录下的所有groovy文件，并解析其中包含的DSL元素到容器中，然后再依据要运行的测试目标，从容器中取出需要的元素数据执行。

restgaga对测试工程的目录结构没有任何要求，你可以把所有的测试代码都写在目录中的一个文件，也可以将其分散到多个文件中，为了方便维护和查找方便，建议分散到多个目录和文件中，每个api都拥有一个自己的目录，api和mock的声明放在一个文件中，不同环境的测试用例放在不同的环境中，就如[restgaga-example](https://github.com/chihongze/restgaga-example)这样的结构。

文件命名规则

测试代码文件必须以`groovy`进行结尾，比如`login_api.groovy`，也可以是这样：`login_api.test.groovy`，中间的test表示该文件只有在test环境运行时才会被加载。

## 命令行工具

restgaga暂时提供了命令行的测试工具 —— restgaga命令

该命令的参数如下：

	-v: 显示当前版本
	-m <port>: 按照指定的端口号启动mock server
	-d <projectDir>: 要加载的工程目录，默认为当前目录
	-t <targetTest>: 测试目标，如果为all，那么执行所有的测试用例，如果为"login.success"，那么会执行login接口的success用例，如果为login，那么会执行login接口下的测试用例，还可以指定一个testsuite的名称。
	-e <env>: 指定使用的测试环境，默认为test

## 建议使用流程

接到需求之后，后端开发人员首先编写api元素和mock元素，mock中可以写死数据，但是要尽量考虑好所有的情况，包括字段的验证条件、业务逻辑验证条件、返回的数据格式等等，然后针对mock出的接口编写测试用例，将mock接口测试通过并提交，与前端沟通确认后，前端就可以开始工作了，后端也可以开始编写真正的业务逻辑，后端在开发的过程中，持续运行测试用例进行校验。看起来有点儿类似于敏捷教材里老提到的TDD？另外前端从此不必再等候后端的接口了，PM也可以更快的看到自己的产品效果。

您也可以在您的CI工具中使用restgaga进行集成测试或者用于线上产品的测试等等。