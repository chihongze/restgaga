package chihz.restgaga.meta

/**
 * <p>
 *     用于表示测试环境的元数据, 在RESTGaga中,
 *     可以根据不同的测试环境来指定不同的测试行为
 * </p>
 */
public class EnvMeta extends BaseMeta {

    /** 基础部分URL 比如测试环境 'http://test.hehe.com' */
    def final String baseUrl

    /** 该环境下的默认HTTP Header */
    def final MetaMapFactory headers

    /** 该环境下默认应用的fixture */
    def final MetaListFactory fixtures

    def EnvMeta(String name, String baseUrl, MetaMapFactory headers, MetaListFactory fixtures) {
        super(name)
        this.baseUrl = baseUrl
        this.headers = headers
        this.fixtures = fixtures
    }
}
