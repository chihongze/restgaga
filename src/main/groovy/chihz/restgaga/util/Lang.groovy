package chihz.restgaga.util


public class Lang {

    def static cloneAndRun(closure, delegate, delegateFirst = true, Object... args) {
        def _closure = closure.clone()
        _closure.delegate = delegate
        if (delegateFirst) {
            _closure.resolveStrategy = Closure.DELEGATE_FIRST
        }
        return _closure(*args)
    }
}
