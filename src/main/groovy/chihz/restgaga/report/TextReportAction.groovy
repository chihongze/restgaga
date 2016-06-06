package chihz.restgaga.report

import chihz.restgaga.runner.RunnerResult
import chihz.restgaga.runner.TestCaseResult
import chihz.restgaga.runner.TestStatus
import chihz.restgaga.runner.TestSuiteResult


class TextReportAction implements ReportAction {


    void report(TestSuiteResult result, Map args = Collections.emptyMap()) {
        OutputStream outputStream = args.get("output")
        if (outputStream == null) {
            outputStream = System.out
        }
        String report = printTestSuite(result, 0)
        outputStream.write(report.getBytes("UTF-8"))
        outputStream.flush()
    }

    def printTestSuite(TestSuiteResult result, int level) {
        def buffer = []
        def space = '\t' * level
        buffer.add("${space}TestSuite: ${result.name}")
        for (RunnerResult r in result.results) {
            buffer.add("")
            if (r instanceof TestSuiteResult) {
                buffer.add(printTestSuite(r, level + 1))
            } else if (r instanceof TestCaseResult) {
                buffer.add(printTestCase(r, level + 1))
            }
        }
        buffer.add("")
        buffer.add("${space}Summary:")
        for (TestStatus status in TestStatus.values()) {
            buffer.add("${space}  ${status}: ${result.statusCounts[status]}")
        }

        String.join('\n', buffer)
    }

    def printTestCase(TestCaseResult result, int level) {
        def buffer = []
        def space = '\t' * level
        buffer.add("${space}TestCase: ${result.name}")
        buffer.add("${space}  Status: ${result.status}")
        buffer.add("${space}  Used time: ${result.usedTime}")
        buffer.add("${space}  Request time: ${result.requestTime}")
        if (result.status == TestStatus.STATUS_NO_MATCH) {
            buffer.add("${space}  Expected status code: ${result.expectedStatus}")
            buffer.add("${space}  Actual status code: ${result.actualStatus}")
        } else if (result.status == TestStatus.HEADER_NO_MATCH) {
            buffer.add("${space}  Expected headers:")
            buffer.add("${space}    ${result.expectedHeaders}")
            buffer.add("${space}  Actual headers:")
            buffer.add("${space}    ${result.actualHeaders}")
            buffer.add("${space}  Different header items:")
            for (def item in result.differentHeaderItems) {
                buffer.add("${space}    key=${item.key}, expectedValue=${item.expectedValue}, actualValue=${item.actualValue}")
            }
        } else if (result.status == TestStatus.BODY_NO_MATCH) {
            buffer.add("${space}  Expected body:")
            buffer.add("${space}    ${result.expectedBody}")
            buffer.add("${space}  Actual body:")
            buffer.add("${space}     ${result.actualBody}}")
            buffer.add("${space}  Different body items:")
            for (def item in result.differentBodyItem) {
                buffer.add("${space}    key=${item.key}, expectedValue=${item.expectedValue}, actualValue=${item.actualValue}")
            }
        } else if (result.status == TestStatus.EXCEPTION) {
            buffer.add("${space}  Exception detals:")
            buffer.add(printExceptionDetails(result.exception, level + 1))
        }
        String.join('\n', buffer)
    }

    def printExceptionDetails(Throwable exception, int level) {
        def space = '\t' * level + "  "
        def buffer = []
        buffer.add("${space}${exception}")
        for (def element in exception.getStackTrace()) {
            buffer.add("${space}\tat ${element}")
        }
        String.join('\n', buffer)
    }
}
