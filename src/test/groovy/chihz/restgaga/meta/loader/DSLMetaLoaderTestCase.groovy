package chihz.restgaga.meta.loader

import chihz.restgaga.meta.GlobalNamespace
import chihz.restgaga.meta.MockMeta
import chihz.restgaga.meta.RequestMethod
import chihz.restgaga.meta.mgr.*
import chihz.restgaga.runner.RuntimeProperties

class DSLMetaLoaderTestCase extends GroovyTestCase {

    void testLoad() {
        DSLMetaLoader loader = DSLMetaLoader.instance
        def metaConfig = """
        global.x = 1
        global.y = 2

        global {
            "init"
        }

        globalHeaders (
            a: 1,
            b: 2
        )

        env test {
            baseUrl 'http://test.hehe.com'
            headers (
                Accept: "application/json"
            )
            fixtures << [ "testSign", "testToken" ]
        }

        env pro {
            baseUrl 'http://www.hehe.com'
            headers (
                Accept: "application/json"
            )
            fixtures << [ "sign", "token" ]
        }

        fixture sign {
            setUp {
                "set up sign"
            }

            tearDown {
                "tear down sign"
            }
        }

        fixture token {
            setUp {
                "set up token"
            }

            tearDown {
                "tear down token"
            }
        }

        api login {
            post '/v1/user/login'
            headers (
                Accept: "application/json"
            )
            params (
                username: "samchi",
                password: "123456"
            )
            fixtures ^ [ "sign", "token" ]
        }

        api register {
            post '/v1/user/register'
        }

        testcase login {
            success
            withEnv "test", "pro"
            fixtures ^ [ "sign" ]
            headers (
                "sign": "my sign"
            )
            params (
                username: "jackson",
                password: "123456"
            )
            responseHeaders (

            )
            responseBody (

            )
            setUp {
                "login set up"
            }
            tearDown {
                "login tear down"
            }
        }

        testcase login {
            passwordError
            params (
                username: "sam",
                password: "654321"
            )
        }

        testsuite userFunctions {
            test "login.success"
            test "login.passwordError"
        }

        testsuite parallelUserFunctions {
            concurrent
            test "login.success"
            test "login.passwordError"
            test "user.settings"
        }

        mock login {
            "mock login"
        }

        mock register {
            "mock register"
        }
        """

        loader.load(metaConfig)

        def global = GlobalNamespace.instance
        assertEquals(global.x, 1)
        assertEquals(global.y, 2)
        assertEquals(global.init("test"), "init")

        RuntimeProperties properties = RuntimeProperties.instance
        assertEquals(properties.globalHeaders.map, [a: 1, b: 2])

        EnvMetaManager envMetaMgr = EnvMetaManager.instance
        assertEquals(envMetaMgr.getMetaObject("test").baseUrl, "http://test.hehe.com")
        assertEquals(envMetaMgr.getMetaObject("test").headers.map, [Accept: "application/json"])
        assertEquals(envMetaMgr.getMetaObject("pro").baseUrl, "http://www.hehe.com")
        assertEquals(envMetaMgr.getMetaObject("pro").fixtures.list, ["sign", "token"])

        FixtureMetaManager fixtureMetaManager = FixtureMetaManager.instance
        assertEquals(fixtureMetaManager.getMetaObject("sign").setUp(), "set up sign")
        assertEquals(fixtureMetaManager.getMetaObject("token").setUp(), "set up token")

        APIMetaManager apiMetaManager = APIMetaManager.instance
        assertEquals(apiMetaManager.getMetaObject("login").method, RequestMethod.POST)
        assertEquals(apiMetaManager.getMetaObject("login").path, "/v1/user/login")
        assertEquals(apiMetaManager.getMetaObject("login").headers.map, [Accept: "application/json"])
        assertEquals(apiMetaManager.getMetaObject("register").path, "/v1/user/register")

        TestCaseMetaManager testcaseMetaManager = TestCaseMetaManager.instance
        assertEquals(testcaseMetaManager.getMetaObject("login.success").withEnv, ["test", "pro"])
        assertEquals(testcaseMetaManager.getMetaObject("login.success").headers.map, ["sign": "my sign"])
        assertEquals(testcaseMetaManager.getMetaObject("login.success").setUp(), "login set up")
        assertEquals(testcaseMetaManager.getMetaObject("login.success").tearDown(), "login tear down")
        assertEquals(testcaseMetaManager.getMetaObject("login.passwordError").params.map,
                [username: "sam", password: "654321"])

        TestSuiteMetaManager testsuiteMetaManager = TestSuiteMetaManager.instance
        assertFalse((boolean) testsuiteMetaManager.getMetaObject("userFunctions").allConcurrent)
        assertEquals(
                testsuiteMetaManager.getMetaObject("userFunctions").tests,
                ["login.success", "login.passwordError"]
        )
        assertTrue((boolean) testsuiteMetaManager.getMetaObject("parallelUserFunctions").allConcurrent)
        assertEquals(testsuiteMetaManager.getMetaObject("parallelUserFunctions").tests,
                ["login.success", "login.passwordError", "user.settings"])

        MockMeta mockLogin = MockMetaManager.instance.getMetaObject("login")
        assertEquals(mockLogin.name, "login")
        assertEquals(mockLogin.mockAction(), "mock login")

        MockMeta mockRegister = MockMetaManager.instance.getMetaObject("register")
        assertEquals(mockRegister.name, "register")
        assertEquals(mockRegister.mockAction(), "mock register")

    }

    void tearDown() {
        DSLMetaLoader.instance.cleanAll()
    }
}
