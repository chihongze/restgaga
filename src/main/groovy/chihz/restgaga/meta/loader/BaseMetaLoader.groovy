package chihz.restgaga.meta.loader


public abstract class BaseMetaLoader {

    def abstract void load(String metaConfig)

    def abstract void cleanAll()

    def abstract void checkAll()

}
