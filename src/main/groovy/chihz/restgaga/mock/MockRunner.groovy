package chihz.restgaga.mock

import chihz.restgaga.meta.APIMeta
import chihz.restgaga.meta.MockInterceptorMeta
import chihz.restgaga.meta.MockMeta
import chihz.restgaga.meta.mgr.APIMetaManager
import chihz.restgaga.meta.mgr.MockInterceptorMetaManager
import chihz.restgaga.meta.mgr.MockMetaManager
import groovy.json.JsonBuilder
import org.json.JSONObject
import spark.ExceptionHandler
import spark.Request
import spark.ResponseTransformer
import spark.Service


class MockRunner {

    def final MockMetaManager mockMetaManager = MockMetaManager.instance

    def final MockInterceptorMetaManager mockInterceptorMetaManager = MockInterceptorMetaManager.instance

    def final APIMetaManager apiMetaManager = APIMetaManager.instance

    def final Service sparkService = new Service()

    def final int port

    def MockRunner(int port) {
        this.port = port
    }

    def run() {

        Request.metaClass.jsonBody = {
            new JSONObject(body())
        }

        sparkService.port(this.port)

        for (MockInterceptorMeta meta in mockInterceptorMetaManager.metaMap.values()) {
            sparkService.before(new MockInterceptor(meta))
        }

        for (MockMeta mockMeta in mockMetaManager.metaMap.values()) {
            APIMeta apiMeta = apiMetaManager.getMetaObject(mockMeta.name)

            if (!apiMeta) {
                continue
            }

            String apiPath = apiMeta.path.replace('{', ':').replace('}', '')

            sparkService."${-> apiMeta.method.toString().toLowerCase()}"(apiPath,
                    new MockController(mockMeta), new JSONTransformer())

            sparkService.exception(Exception.class, { exc, req, res ->
                exc.printStackTrace()
                res.header("status", "500")
            } as ExceptionHandler)
        }
    }

    def stop() {
        sparkService.stop()
    }
}

class JSONTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) {
        JsonBuilder builder = new JsonBuilder(model);
        return builder.toString();
    }
}
