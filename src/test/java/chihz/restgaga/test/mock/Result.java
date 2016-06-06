package chihz.restgaga.test.mock;


public class Result {

    private final int code;

    private final String msg;

    private final Object data;

    public Result(int code, String msg) {
        this(code, msg, null);
    }

    public Result(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public Object getData() {
        return this.data;
    }
}
