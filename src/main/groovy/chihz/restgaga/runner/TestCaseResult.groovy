package chihz.restgaga.runner

import org.json.JSONObject

/**
 * 运行一个TestCase后所得到的状态
 */
public enum TestStatus {

    SUCCESS,

    EXCEPTION,

    STATUS_NO_MATCH,

    HEADER_NO_MATCH,

    BODY_NO_MATCH;
}

public class DifferentItem {

    def final key

    def final expectedValue

    def final actualValue

    def DifferentItem(key, expectedValue, actualValue) {
        this.key = key
        this.expectedValue = expectedValue
        this.actualValue = actualValue
    }

    String toString() {
        return "DifferentItem {key='$key', expectedValue='$expectedValue', actualValue='$actualValue'}"
    }
}

/**
 * TestCase执行结果
 */
public class TestCaseResult extends RunnerResult {

    /** 测试结果状态 */
    def final TestStatus status

    /** 测试使用时间 */
    def final long usedTime

    /** 请求接口所使用的时间 */
    def final long requestTime

    /** 期望得到的HTTP响应码 */
    def final int expectedStatus

    /** 接口实际返回的响应码 */
    def final int actualStatus

    /** 期望得到的HTTP 响应头*/
    def final Map expectedHeaders

    /** 实际API响应头 */
    def final Map actualHeaders

    /** 异同的header项 */
    def final List<DifferentItem> differentHeaderItems

    /** 期望得到的HTTP 响应体*/
    def final Map expectedBody

    /** 实际API响应体 */
    def final JSONObject actualBody

    /** 异同的body项 */
    def final DifferentItem differentBodyItem

    /** 如果发生异常, 产生的异常堆栈 */
    def final Throwable exception

    def TestCaseResult(String name, TestStatus status, long usedTime,
                       long requestTime = 0, int expectedStatus = 0, int actualStatus = 0,
                       Map expectedHeaders = null, Map actualHeaders = null, List differentHeaderItems = Collections.emptyList(),
                       Map expectedBody = null, JSONObject actualBody = null, DifferentItem differentBodyItem = null,
                       Throwable exception = null) {

        super(name)
        this.status = status
        this.usedTime = usedTime
        this.requestTime = requestTime
        this.expectedStatus = expectedStatus
        this.actualStatus = actualStatus
        this.expectedHeaders = expectedHeaders
        this.actualHeaders = actualHeaders
        this.differentHeaderItems = differentHeaderItems
        this.expectedBody = expectedBody
        this.actualBody = actualBody
        this.differentBodyItem = differentBodyItem
        this.exception = exception
    }

    String toString() {
        def buffer = []
        buffer.add("<$name>")
        buffer.add("Status: ${this.status.toString()}")
        buffer.add("Use time: ${this.usedTime} ms")
        buffer.add("Request time: ${this.requestTime} ms")
        if (this.status == TestStatus.STATUS_NO_MATCH) {
            buffer.add("Expected status code: ${this.expectedStatus}")
            buffer.add("Actual status code: ${this.actualStatus}")
        } else if (this.status == TestStatus.HEADER_NO_MATCH) {
            buffer.add("Different headers:")
            this.differentHeaderItems.each({
                buffer.add("  key='${it.key}', expectedValue='${it.expectedValue}', actualValue='${it.actualValue}'")
            })
            buffer.add("Expected headers:")
            this.expectedHeaders.each { k, v ->
                buffer.add("  $k: $v")
            }
            buffer.add("Actual headers:")
            this.actualHeaders.each({ k, v ->
                buffer.add("  $k: $v")
            })
        } else if (this.status == TestStatus.BODY_NO_MATCH) {
            buffer.add("Different body item:")
            buffer.add("  key='${this.differentBodyItem.key}', expectedValue='${this.differentBodyItem.expectedValue}', actualValue='${this.differentBodyItem.actualValue}'")
            buffer.add("Expected body items:")
            this.expectedBody.each({ k, v ->
                buffer.add("  $k: $v")
            })
            buffer.add("Actual body items:")
            this.actualBody.keys().each { k ->
                buffer.add("  $k: ${this.actualBody[k]}")
            }
        } else if (this.status == TestStatus.EXCEPTION) {
            buffer.add("Exception details:")
            StringWriter sw = new StringWriter()
            exception.printStackTrace(new PrintWriter(sw))
            buffer.add(sw)
        }
        String.join("\n", buffer)
    }
}
