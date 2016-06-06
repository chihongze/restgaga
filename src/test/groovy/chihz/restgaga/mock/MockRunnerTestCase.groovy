package chihz.restgaga.mock

import chihz.restgaga.meta.loader.DSLMetaLoader
import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MockRunnerTestCase {

    def final DSLMetaLoader dslMetaLoader = DSLMetaLoader.instance

    def MockRunner mockRunner = null

    @Before
    void setUp() {
        def script = """
        api login {
            post '/user/login'
        }

        api userProfile {
            get '/user/profile/{id}'
        }

        mockInterceptor checkToken {
            def path = req.pathInfo()
            if (path == "/user/login") {
                return
            }
            def token = req.headers("token")
            if (!token) {
                halt(200, [ code: 400, msg: "invalid token" ])
            }
        }

        mock login {

            validate {
                // validate username
                usernameRequired true, [code: 400, msg: "username required"]
                usernameMinLength 6, [code: 400, msg: "username too short"]
                usernameMaxLength 20, [code: 400, msg: "username too long"]

                // validate password
                passwordRequired true, [code: 400, msg: "password required"]
                passwordMinLength 6, [code: 400, msg: "password too short"]
                passwordMaxLength 20, [code: 400, msg: "password too long"]
            }

            jsonBody = req.jsonBody()
            username = jsonBody["username"]
            password = jsonBody["password"]
            if (username == "samchi" && password == "123456") {
                [ code: 200, msg: "success" ]
            } else {
                [ code: 400, msg: "loginFailed" ]
            }
        }

        mock userProfile {
            [ id: Integer.valueOf(req.params("id")) ]
        }
        """

        dslMetaLoader.load(script)
        this.mockRunner = new MockRunner(8081)
        this.mockRunner.run()
    }

    @Test
    void testRunMockService() {
        def result = Unirest.post("http://localhost:8081/user/login"
        ).header("Content-Type", "application/json").body(
                new JSONObject(["username": "samchi", "password": "123456"])).asJson().body.object
        println result
        Assert.assertEquals(result["code"], 200)
        result = Unirest.post("http://localhost:8081/user/login"
        ).header("Content-Type", "application/json").body(
                new JSONObject(["username": "jackson", "password": "123456"])).asJson().body.object
        Assert.assertEquals(result["code"], 400)

        result = Unirest.post("http://localhost:8081/user/login"
        ).header("Content-Type", "application/json").body(
                new JSONObject([username: "", "password": "123456"])).asJson().body.object
        Assert.assertEquals(result["code"], 400)
        Assert.assertEquals(result["msg"], "username required")

        result = Unirest.post("http://localhost:8081/user/login").header("Content-Type", "application/json").body(
                new JSONObject([username: "samchi", password: "123"])).asJson().body.object
        Assert.assertEquals(result["code"], 400)
        Assert.assertEquals(result["msg"], "password too short")
    }

    @Test
    void testInterceptor() {
        def result = Unirest.get("http://localhost:8081/user/profile/1"
        ).header("Content-Type", "application/json").asJson().body.object
        Assert.assertEquals(result.code, 400)
        Assert.assertEquals(result.msg, "invalid token")

        result = Unirest.get("http://localhost:8081/user/profile/1"
        ).header("Content-Type", "application/json").header("token", "123456").asJson().body.object
        Assert.assertEquals(result.id, 1)
    }

    @After
    void tearDown() {
        mockRunner.stop()
        dslMetaLoader.cleanAll()
    }
}
