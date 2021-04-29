package net.tomobou.extensions;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProxyResponseTransformer extends ResponseTransformer {

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        try {
            var isGzip = Gzip.isGzipped(response.getBody());
            var bodyJson = Json.node((isGzip)
                    ? Gzip.unGzipToString(response.getBody())
                    : response.getBodyAsString());

            parameters.keySet().forEach(key -> {
                JsonNodeTransformUtil.nodeTransform(bodyJson, Arrays.asList(key.split("\\.").clone()), parameters.getString(key));
            });

            return Response.Builder.like(response)
                    .headers(response.getHeaders().plus(new HttpHeader("X-Response-Transformed", "true")))
                    .body((isGzip) ? Gzip.gzip(bodyJson.toString()) : bodyJson.toString().getBytes(StandardCharsets.UTF_8))
                    .build();
        } catch (Exception ex) {
            return response;
        }
    }

    @Override
    public boolean applyGlobally() {
        return true;
    }

    @Override
    public String getName() {
        return "proxy-response-transformer";
    }

}
