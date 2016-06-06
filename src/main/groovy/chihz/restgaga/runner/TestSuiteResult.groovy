package chihz.restgaga.runner


public class TestSuiteResult extends RunnerResult {

    def final RunnerResult[] results

    def final EnumMap<TestStatus, Integer> statusCounts = new EnumMap<>(TestStatus.class);

    def final long usedTime

    {
        TestStatus.values().each {
            statusCounts.put(it, 0)
        }
    }

    def TestSuiteResult(String name, RunnerResult[] results, long usedTime) {
        super(name)
        this.results = results
        if (results) {
            _handleStatusCounts(results)
        }
        this.usedTime = usedTime
    }

    def _handleStatusCounts(RunnerResult[] results) {
        for (result in results) {
            if (result instanceof TestCaseResult) {
                def count = statusCounts.get(result.status)
                statusCounts.put(result.status, ++count)
            } else if (result instanceof TestSuiteResult) {
                _handleStatusCounts(result.results)
            }
        }
    }

    String toString() {
        def buffer = []
        buffer.add("Test suite: $name")
        buffer.add("-" * 20)
        for (RunnerResult result in this.results) {
            buffer.add(result.toString())
            buffer.add("\n")
        }
        buffer.add("-" * 20)
        buffer.add("Summary:")
        buffer.add("Use time: ${usedTime} ms")
        for (TestStatus status in TestStatus.values()) {
            buffer.add("  ${status.toString()}: ${statusCounts[status]}")
        }
        buffer.add("-" * 20)
        String.join("\n", buffer)
    }
}
