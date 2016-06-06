package chihz.restgaga.report

import chihz.restgaga.runner.TestSuiteResult


interface ReportAction {

    void report(TestSuiteResult result, Map args)
}
