package chihz.restgaga.test.mock;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;


public class MockHttpService {

    private final int port;

    public MockHttpService() {
        this(8090);
    }

    public MockHttpService(int port) {
        this.port = port;
    }

    public void run() {
        port(this.port);

        get("/hello", (req, res) -> "hello, world");
        get("/json", (req, res) -> {
            res.header("Content-Type", "application/json;charset=utf-8");
            return new Result(200, "success");
        }, new JSONTransformer());

        // 基于json请求的登录接口
        post("/v1/user/login", "application/json", (req, res) -> {
            JSONObject jsonBody = new JSONObject(req.body());

            String username = jsonBody.getString("username");
            String password = jsonBody.getString("password");

            res.header("Content-Type", "application/json;charset=utf-8");

            if ("samchi".equals(username) && "123456".equals(password)) {
                res.header("Sign", "sigN");
                res.header("token", "token");
                return new Result(200, "success");
            } else if ("hehe".equals(username)) {
                halt(400, "bad username!");
                return null;
            } else {
                return new Result(400, "login_error");
            }
        }, new JSONTransformer());

        // 基于form请求的注册接口
        post("/v1/user/register", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");
            String repassword = req.queryParams("repassword");
            String birthday = req.queryParams("birthday");

            res.header("Content-Type", "application/json;charset=utf-8");

            if ("samchi".equals(username)) {
                return new Result(400, "user_existed");
            }

            if (!password.equals(repassword)) {
                return new Result(400, "repassword_error");
            }

            return new Result(200, "success", new User(1, username, birthday));
        }, new JSONTransformer());

        // get with path variable
        get("/v1/user/profile/:userId", (req, res) -> {
            int userId = Integer.parseInt(req.params("userId"));
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("user", new User(userId, "SamChi", "1989-11-07"));
            List<Feed> feeds = new ArrayList<>();
            feeds.add(new Feed("1-1-1-1", "早上好"));
            feeds.add(new Feed("2-2-2-2", "下午好"));
            profileData.put("feeds", feeds);
            res.header("Content-Type", "application/json;charset=utf-8");
            return profileData;
        }, new JSONTransformer());

        // put body
        put("/v1/pan/upload", (req, res) -> req.body());

        // echo headers
        get("/v1/test/echoHeaders", (req, res) -> {
            Map<String, String> headers = new HashMap<>();
            for (String header : req.headers()) {
                headers.put(header, req.headers(header));
            }
            return headers;
        }, new JSONTransformer());
    }

    public void stopMock() {
        stop();
    }

    public static void main(String[] args) throws UnirestException {
        MockHttpService service = new MockHttpService();
        service.run();
        Map<String, String> map = new HashMap<>();
        map.put("username", "samchi");
        map.put("password", "123456");
        JsonNode node = Unirest.post("http://localhost:8090/v1/user/login"
        ).header("Accept", "application/json").body(new JSONObject(map)).asJson().getBody();
        System.out.println(node.getObject());
    }
}
