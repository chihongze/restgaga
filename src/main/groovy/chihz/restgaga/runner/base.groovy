package chihz.restgaga.runner

public abstract class RunnerResult {

    def final String name

    def RunnerResult(String name) {
        this.name = name
    }
}


public abstract class Runner {

    abstract run()
}
