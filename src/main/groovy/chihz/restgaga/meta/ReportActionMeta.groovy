package chihz.restgaga.meta

import chihz.restgaga.report.ReportAction

public class ReportActionMeta extends BaseMeta {

    def final ReportAction action

    def ReportActionMeta(String name, ReportAction action) {
        super(name)
        this.action = action
    }
}
