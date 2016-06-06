package chihz.restgaga.meta

import chihz.restgaga.util.Lang

/**
 * 所有MetaClass均需要继承此类
 */
public abstract class BaseMeta {

    def final String name

    def BaseMeta(String name) {
        this.name = name
    }
}

/**
 * <p>用于动态生成MetaClass对象中的Map结构
 * 可以对以下三种形态的Map进行包装:</p>
 *
 * <ol>
 * <li> 完全静态的Map结构 </li>
 * <li> 某个key对应的是一个闭包, 这个闭包会根据运行时的上下文进行动态渲染 </li>
 * <li> 完全需要运行时进行动态渲染的闭包 </li>
 * </ol>
 */
public class MetaMapFactory {

    // 静态Map类型
    static final int STATIC_MAP = 0

    // 动态Map类型
    static final int DYNAMIC_MAP = 1

    // 闭包类型
    static final int CLOSURE = 2

    // 缓存动作
    def cachedAction = Collections.emptyMap()

    // 缓存的具体Map类型
    def int type = STATIC_MAP

    /**
     * <p> 接受目标Map结构,并预先判断出目标结构的类型 </p>
     * <pre>
     *     builder = new MetaMap()
     *
     *     // 接受闭包
     *     builder {*         ...
     *}*
     *     // 接受字典
     *     builder (
     *         id: 1
     *         name: "samchi",
     *         birth: "1989-11-07"
     *     )
     *
     * </pre>
     * @param action 接受的动作, 只允许Closure和Map类型
     * @return
     */
    def call(action) {
        this.cachedAction = action
        if (cachedAction instanceof Closure) { // 闭包类型
            this.type = CLOSURE
        } else if (cachedAction instanceof Map) {
            if (cachedAction.any({ _, v -> v instanceof Closure })) {
                this.type = DYNAMIC_MAP
            } else {
                this.type = STATIC_MAP
            }
        } else {
            throw new IllegalArgumentException(
                    "MetaMap object only can be called with Map or Closure argument.")
        }
    }

    /**
     * <p> 动态生成Map结构 </p>
     * @param ctx 如果在渲染的过程中需要执行闭包, 那么会将该闭包的delegate指向该上下文参数
     */
    def getMap(ctx = null) {
        if (type == STATIC_MAP) {
            return Collections.unmodifiableMap(cachedAction)
        } else if (type == DYNAMIC_MAP) {
            Map result = new HashMap(cachedAction.size())
            cachedAction.each({ k, v ->
                if (v instanceof Closure) {
                    v = Lang.cloneAndRun(v, ctx)
                }
                result.put(k, v)
            })
            return result
        } else if (type == CLOSURE) {
            return Lang.cloneAndRun(cachedAction, ctx)
        } else {
            throw new IllegalStateException("Unkown type $type")
        }
    }

}

/**
 * <p>
 * 对List对象的包装<br/>
 * Meta中的List对象往往会包含两种语义,
 * 一种是对上层的List进行替换, 第二种是在上层List的基础上添加元素
 * </p>
 *
 * <pre>
 *     list = new MetaList()
 *     list ^ [1,2,3,4]  // '^' 表示使用[1,2,3,4]对上层的列表进行替换
 *     list ^ { -> [1,2,3,4] }*     list << [1,2,3,4] // '<<' 表示将[1,2,3,4]追加到上层列表
 *     list << { -> [1,2,3,4] }* </pre>
 *
 */
public class MetaListFactory {

    def static List getFinallyList(Object ctx, MetaListFactory... listFactories) {
        List result = []
        for (MetaListFactory factory in listFactories) {
            if (factory.operation == APPEND) {
                result.addAll(factory.getList(ctx))
            } else if (factory.operation == REPLACE) {
                result.clear()
                result.addAll(factory.getList(ctx))
            }
        }
        return result
    }

    static final int APPEND = 0 // 追加状态

    static final int REPLACE = 1 // 替换状态

    def _data = Collections.emptyList()

    def int operation = APPEND

    /**
     * 将操作标记为REPLACE
     * @param list
     * @return
     */
    def xor(list) {
        this.operation = REPLACE
        _setList(list)
    }

    /**
     * 将操作标记为APPEND
     * @param list
     * @return
     */
    def leftShift(list) {
        this.operation = APPEND
        _setList(list)
    }

    def _setList(list) {
        if (list instanceof List) {
            this._data = Collections.unmodifiableList(list)
        } else if (list instanceof Closure) {
            this._data = list
        } else {
            throw new IllegalArgumentException("Only support List or Closure argument")
        }
    }

    /**
     * <p> 动态获取列表,如果是List类型,则直接返回
     *     如果是闭包类型, 返回其计算结果
     * </p>
     * @param ctx 如果是闭包类型, 会将闭包的delegate指向该上下文对象
     * @return
     */
    def getList(ctx = null) {
        if (this._data instanceof List) {
            return this._data
        } else if (this._data instanceof Closure) {
            return Lang.cloneAndRun(this._data, ctx)
        } else {
            throw new IllegalStateException("Un support list type ${_data.class.canonicalName}")
        }
    }
}

/**
 * <p>该标记用于表示忽略此项数据的对比</p>
 */
@Singleton
public class Ignore {

    String toString() {
        return "Ignore"
    }
}

/**
 * <p>该对象用于标识目标属性不能为空</p>
 */
@Singleton
public class NotEmpty {

    String toString() {
        return "NotEmpty"
    }
}

public class Size {

    def final int expectedSize

    def Size(int expectedSize) {
        this.expectedSize = expectedSize
    }

    String toString() {
        return "Size($expectedSize)"
    }
}

public class Dump {

    def final int dumpTimes

    def Dump(int dumpTimes) {
        this.dumpTimes = dumpTimes
    }
}

public class Predict {

    def final Closure predictLogic

    def Predict(Closure predictLogic) {
        this.predictLogic = predictLogic
    }
}
