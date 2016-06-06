package chihz.restgaga.launcher

import chihz.restgaga.Version
import chihz.restgaga.mock.MockRunner
import chihz.restgaga.runner.RuntimeProperties
import chihz.restgaga.runner.TestRunner
import com.mashape.unirest.http.Unirest

class CmdLineLauncher {

    /**
     * Command line launcher for RESTGaga
     * restgaga
     * restgaga -d testProjectDir -t testTarget
     * restgaga -d testProjectDir -m 8080
     */
    static void main(String[] args) {
        def cli = new CliBuilder(
                usage: "restgaga -d testProjectDir -t testTarget OR restgaga -d testProjectDir -m 8080")

        cli.help("Show help message.")
        cli.v(longOpt: "--version", required: false, "Show version")
        cli.d(longOpt: "--projectdir", args: 1, required: false, "Test project dir.")
        cli.t(longOpt: "--target", args: 1, required: false, "Target test name.")
        cli.e(longOpt: "--env", args: 1, required: false, "Current env.")
        cli.m(longOpt: "--mockport", args: 1, required: false, "Mock server port.")


        def options = cli.parse(args)

        // show version
        if (options.v) {
            println "RESTGaga v${Version.currentVersion}"
            return
        }


        String projectDir = options.d ? options.d : new File(".").getAbsolutePath()
        if (!new File(projectDir).exists()) {
            println "Project dir '${projectDir}' not exsited."
        }

        RuntimeProperties.instance.currentProjectDir = projectDir

        try {
            if (options.m) {
                // run mock
                new TestRunner(projectDir, "all")._loadMeta()
                new MockRunner(Integer.valueOf(options.m)).run()
                synchronized (CmdLineLauncher.class) {
                    CmdLineLauncher.class.wait()
                }
            } else {
                // run test
                String env = options.e ? options.e : "test"
                RuntimeProperties.instance.currentEnv = env

                String targetTest = options.t ? options.t : "all"
                new TestRunner(projectDir, targetTest).run()
            }
        } catch (IllegalArgumentException e) {
            println e.message
        } catch (Throwable e) {
            e.printStackTrace()
        } finally {
            Unirest.shutdown()
            System.exit(0)
        }
    }
}
