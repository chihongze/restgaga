package chihz.restgaga.test.mock;

import groovy.json.JsonBuilder;
import spark.ResponseTransformer;


class JSONTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) {

        JsonBuilder builder = new JsonBuilder(model);
        return builder.toString();
    }
}
