package chihz.restgaga.meta

public enum RequestMethod {

    GET,
    POST,
    PUT,
    DELETE

}

/**
 * <p>用于表示一组API接口信息</p>
 */
public class APIMeta extends BaseMeta {

    /** 用于表示HTTP请求方法 */
    def final RequestMethod method

    /** API请求路径 */
    def final String path

    /** 该API所使用的默认HTTP请求头 */
    def final MetaMapFactory headers

    /** 该API所使用的默认请求参数 */
    def final MetaMapFactory params

    /** 该API所使用的默认path variable */
    def final MetaMapFactory pathVariables

    /** 该API测试默认使用的fixtures */
    def final MetaListFactory fixtures

    def APIMeta(String name, RequestMethod method, String path,
                MetaMapFactory headers, MetaMapFactory params,
                MetaMapFactory pathVariables, MetaListFactory fixtures) {
        super(name)
        this.method = method
        this.path = path
        this.headers = headers
        this.params = params
        this.pathVariables = pathVariables
        this.fixtures = fixtures
    }
}
