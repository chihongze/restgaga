package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.Ignore
import chihz.restgaga.meta.MetaListFactory


class TestCaseMetaBuilderTestCase extends DSLBuilderTestCase {

    def TestCaseMetaBuilder builder = null

    def Expando mockCtx = null

    def File testFile = null

    void setUp() {
        builder = new TestCaseMetaBuilder()
        mockCtx = new Expando()
        testFile = new File("testfile.txt")
        testFile.write("papapa")
    }

    void testBuild() {
        def testcase = builder.build login {

            success

            withEnv "test", "pro"

            headers(
                    Accept: "application/json"
            )

            params(
                    username: "samchi",
                    password: "123456"
            )

            mapBody(
                    username: "samchi",
                    password: "123456"
            )

            pathVariables(

            )

            responseHeaders(
                    "Content-type": "application/json"
            )

            responseBody(
                    "code": 200,
                    "msg": Ignore.instance
            )

            setUp {
                "setUp"
            }

            tearDown {
                "tearDown"
            }

            fixtures << ["sign", "token"]
        }

        assertEquals(testcase.api, "login")
        assertEquals(testcase.name, "success")
        assertEquals(testcase.withEnv, ["test", "pro"] as String[])
        assertEquals(testcase.headers.map, [Accept: "application/json"])
        assertEquals(testcase.params.map, [username: "samchi", password: "123456"])
        assertEquals(testcase.body.getBody(), [username: "samchi", password: "123456"])
        assertEquals(testcase.pathVariables.map, Collections.emptyMap())
        assertEquals(testcase.responseHeaders.map, ["Content-type": "application/json"])
        assertEquals(testcase.responseBody.map, ["code": 200, "msg": Ignore.instance])
        assertEquals(testcase.setUp(), "setUp")
        assertEquals(testcase.tearDown(), "tearDown")
        assertEquals(testcase.fixtures.operation, MetaListFactory.APPEND)
        assertEquals(testcase.fixtures.list, ["sign", "token"])
    }

    void testBytesBody() {
        def testcase = builder.build uploadImg {

            success

            bytesBody new File("testfile.txt")

        }

        assertEquals(testcase.body.getBody(), this.testFile.bytes)
    }

    void testFileBody() {
        def testcase = builder.build uploadImg {

            success

            fileBody "testfile.txt"
        }

        assertEquals(testcase.body.getBody(), this.testFile.bytes)
    }

    void tearDown() {
        this.testFile.delete()
    }
}
