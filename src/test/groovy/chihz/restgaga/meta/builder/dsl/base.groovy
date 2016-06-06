package chihz.restgaga.meta.builder.dsl

abstract class DSLBuilderTestCase extends GroovyTestCase {

    def methodMissing(String name, args) {
        new NamedAction(name, args[0])
    }
}