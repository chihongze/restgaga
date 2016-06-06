package chihz.restgaga.meta

/**
 * <p>
 *     Fixture用于封装一组在分别在测试前(setUp)和测试后(tearDown)需要完成的动作
 * </p>
 */
public class FixtureMeta extends BaseMeta {

    /** 测试之前的动作 */
    def final setUp

    /** 测试之后的动作 */
    def final tearDown

    def FixtureMeta(String name, setUp, tearDown) {
        super(name)
        this.setUp = setUp
        this.tearDown = tearDown
    }
}
