package chihz.restgaga.runner

import chihz.restgaga.meta.TestCaseMeta
import chihz.restgaga.meta.loader.DSLMetaLoader
import chihz.restgaga.meta.mgr.TestCaseMetaManager


class BuildHeadersTestCase extends GroovyTestCase {

    def final DSLMetaLoader metaLoader = DSLMetaLoader.instance

    def final TestCaseMetaManager testCaseMetaManager = TestCaseMetaManager.instance


    void setUp() {
        def script = """
        globalHeaders {
            if (currentEnv == "test") {
                [a: 1, B: 0]
            } else {
                [a: 2]
            }
        }

        fixture sign {
            setUp {
                headers['sign'] = 'sign'
            }
        }

        fixture token {
            setUp {
                headers['token'] = 'token'
            }
        }

        env test {
            baseUrl 'http://localhost:8090'
            headers (
                B: 3
            )
            fixtures << [ "sign", "token" ]
        }

        env pro {
            baseUrl 'http://localhost:8080'
            headers (
                b: 5
            )
            fixtures << [ "sign" ]
        }

        api login {
            post '/v1/user/login/{channel}'
            headers (
                currentAPI: "login"
            )
            params (
                username: "samchi",
                password: "123456"
            )
        }

        api register {
            post '/v1/user/register'
            headers (
                currentAPI: "register"
            )
            params (
                username: "samchi",
                password: "123456",
                address: "Beijing",
                birth: "1989-11-07"
            )
        }

        testcase login {
            success
            headers (
                currentTestCase: "login.success"
            )
            params (
                username: "jackson",
                password: "123456"
            )
            responseBody (
                code: 200,
                msg: Ignore
            )
        }

        testcase login {
            passwordError
            headers (
                currentTestCase: "login.passwordError"
            )
            params (
                username: "jackson",
                password: "654321"
            )
            responseBody (
                code: 403,
                msg: Ignore
            )
        }
        """

        metaLoader.load(script)

    }


    void testConstructor() {
        TestCaseMeta testCaseMeta = testCaseMetaManager.getMetaObject("login.success")
        RuntimeProperties.instance.currentEnv = "test"
        TestCaseRunner testCaseRunner = new TestCaseRunner(testCaseMeta)
        assertEquals(testCaseRunner.fullUrl, "http://localhost:8090/v1/user/login/{channel}")
    }

    void testUpdateHeaders() {
        TestCaseMeta testCaseMeta = testCaseMetaManager.getMetaObject("login.success")
        RuntimeProperties.instance.currentEnv = "test"
        TestCaseRunner testCaseRunner = new TestCaseRunner(testCaseMeta)

        testCaseRunner._buildHeaders()

        TestCaseContext ctx = testCaseRunner.testCaseContext
        assertEquals(ctx.headers["A"], 1)
        assertEquals(ctx.headers["b"], 3)
        assertEquals(ctx.headers["B"], 3)
        assertEquals(ctx.headers["CURRENTAPI"], "login")
        assertEquals(ctx.headers["currenttestcase"], "login.success")

        RuntimeProperties.instance.currentEnv = "pro"
        testCaseMeta = testCaseMetaManager.getMetaObject("login.passwordError")
        testCaseRunner = new TestCaseRunner(testCaseMeta)

        testCaseRunner._buildHeaders()

        ctx = testCaseRunner.testCaseContext
        assertEquals(ctx.headers["a"], 2)
        assertEquals(ctx.headers["B"], 5)
        assertEquals(ctx.headers["currentTestCase"], "login.passwordError")
    }

    void tearDown() {
        this.metaLoader.cleanAll()
    }
}
