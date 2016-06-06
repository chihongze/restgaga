package chihz.restgaga.meta


public class Regexp {

    // 要进行匹配的正则表达式
    def final String pattern

    // 是否进行完全匹配
    def final boolean completeMatch

    def Regexp(String pattern, boolean completeMatch = true) {
        this.pattern = pattern
        this.completeMatch = completeMatch
    }

    def match(String text) {
        if (completeMatch) {
            return text ==~ pattern
        } else {
            return text =~ pattern
        }
    }
}
