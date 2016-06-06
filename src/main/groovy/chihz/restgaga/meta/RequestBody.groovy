package chihz.restgaga.meta

import chihz.restgaga.util.Lang


enum RequestBodyType {
    Map,
    Bytes
}

public abstract class RequestBody {

    def final RequestBodyType type

    def RequestBody(RequestBodyType type) {
        this.type = type
    }

    def abstract getBody(ctx)
}

public class MapRequestBody extends RequestBody {

    def final MetaMapFactory body

    def MapRequestBody(MetaMapFactory body) {
        super(RequestBodyType.Map)
        this.body = body
    }

    def getBody(ctx = null) {
        return this.body.getMap(ctx)
    }
}

public class BytesRequestBody extends RequestBody {

    def final body

    def BytesRequestBody(body) {
        super(RequestBodyType.Bytes)
        if ((!body instanceof byte[]) && (!body instanceof Closure) &&
                (!body instanceof File) && (!body instanceof String)) {
            throw new IllegalArgumentException(
                    "Unsupport request body type: ${body.getClass()}")
        }
        this.body = body
    }

    def getBody(ctx = null) {
        if (body instanceof byte[]) {
            return body
        } else if (body instanceof File || body instanceof String) {
            return body.bytes as byte[]
        } else if (body instanceof Closure) {
            return Lang.cloneAndRun(body, ctx)
        }
    }
}
