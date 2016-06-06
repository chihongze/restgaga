package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.FixtureMeta

/**
 * 使用此builder可以通过以下DSL来构建FixtureMeta对象
 *
 * <pre>
 *     builder = new FixtureMetaBuilder()
 *     builder.build sign {*          setUp {*              ...
 *}*          tearDown {*              ...
 *}*}* </pre>
 */
public class FixtureMetaBuilder extends BaseDSLBuilder {

    def _setUp = null

    def _tearDown = null

    def setUp(setUp) {
        this._setUp = setUp
        this
    }

    def tearDown(tearDown) {
        this._tearDown = tearDown
        this
    }

    def newInstance() {
        new FixtureMeta(this.name, this._setUp, this._tearDown)
    }
}
