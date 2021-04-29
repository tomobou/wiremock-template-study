package net.tomobou.extensions;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestWrapper;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Request;

import java.io.IOException;
import java.util.Arrays;

public class ParameterModifyingFilter extends StubRequestFilter {
    private static final String PROPERTIES_FILE_PATH = "properties/requestMapping.json";
    private JsonNode requestMapping;

    public ParameterModifyingFilter() {
        try {
            requestMapping = Json.node(Strings.stringFromBytes(getClass().getResourceAsStream("properties/requestMapping.json").readAllBytes()));
        } catch (IOException e) {
            e.printStackTrace();
//            requestMapping = Json.node("{}");
        }
    }

    @Override
    public RequestFilterAction filter(Request request) {
        Request wrappedRequest = RequestWrapper.create()
                .transformBody(body -> {
                    var bodyJson = body.asJson();
                    var url = request.getUrl();
                    var operations = requestMapping.get(url);
                    if (operations != null) {
                        for (var it = operations.fields(); it.hasNext(); ) {
                            var elm = it.next();
                            JsonNodeTransformUtil.nodeTransform(bodyJson, Arrays.asList(elm.getKey().split("\\.").clone()), elm.getValue().textValue());
                        }
                    }
                    return new Body(bodyJson);
                })
                .addHeader("X-Custom-Header", "headerval")
                .wrap(request);

        return RequestFilterAction.continueWith(wrappedRequest);
    }

    @Override
    public String getName() {
        return "url-and-header-modifier";
    }
}
