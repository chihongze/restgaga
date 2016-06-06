package chihz.restgaga.meta.mgr

import chihz.restgaga.meta.BaseMeta

/**
 * Manager类用来加载注册检查所有的MetaObject
 * 各个类型的MetaObject需要继承该类来实现自己的Manager
 */
public abstract class BaseMetaManager {

    // store in linked hash map
    def final Map<String, BaseMeta> metaMap = [:]

    /**
     * 获取Meta对象
     * @param name Meta对象名称
     * @return 返回对应名称的Meta对象
     * @throws MetaObjectNotExistedException  如果元对象不存在 抛出该异常
     */
    def BaseMeta getMetaObject(String name) throws MetaObjectNotExistedException {
        if (!metaMap.containsKey(name)) {
            return null
        }
        this.metaMap[name]
    }

    /**
     * 向Manager当中添加新的Meta对象
     * @param metaObj 目标Meta对象
     * @return
     * @throws MetaObjectExistedException  如果Meta对象已经存在抛出该异常
     */
    def add(BaseMeta metaObj) throws MetaObjectExistedException {
        if (metaMap.containsKey(metaObj.name)) {
            throw new MetaObjectExistedException("Meta object ${metaObj.name} already existed.")
        }
        this.metaMap[metaObj.name] = metaObj
    }

    /**
     * 根据名称检查目标对象是否存在
     */
    def boolean existed(String name) {
        metaMap.containsKey(name)
    }

    /**
     * 对Manager中保存的对象做关联性的检查
     * @return
     * @throws InvalidMetaObjectException 如果Meta中引用了不合法的对象, 抛出该异常
     */
    def abstract check() throws InvalidMetaObjectException


    def void cleanAll() {
        this.metaMap.clear()
    }

}

public class MetaObjectNotExistedException extends Exception {

    def MetaObjectNotExistedException(String msg) {
        super(msg)
    }
}

public class MetaObjectExistedException extends Exception {

    def MetaObjectExistedException(String msg) {
        super(msg)
    }
}

public class InvalidMetaObjectException extends Exception {

    def InvalidMetaObjectException(String msg) {
        super(msg)
    }
}

/**
 * MetaManager链, 方便依次在链上每个MetaManager上应用操作
 */
public class MetaManagerChain extends BaseMetaManager {

    def final List<BaseMetaManager> mgrList

    def MetaManagerChain(List<BaseMetaManager> mgrList) {
        this.mgrList = mgrList
    }

    def BaseMeta getMetaObject(String name) throws MetaObjectNotExistedException {
        for (BaseMetaManager mgr in this.mgrList) {
            BaseMeta obj = mgr.getMetaObject(name)
            if (obj != null) {
                return obj
            }
        }
        return null
    }

    def boolean existed(String name) {
        for (BaseMetaManager mgr in this.mgrList) {
            if (mgr.existed(name)) {
                return true
            }
        }
        return false
    }

    def add(BaseMeta metaObj) {
        throw new UnsupportedOperationException(
                "Can't support add operation in MetaManagerChain")
    }

    def check() throws InvalidMetaObjectException {
        for (BaseMetaManager mgr in mgrList) {
            mgr.check()
        }
    }

}
