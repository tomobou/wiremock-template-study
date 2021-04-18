package net.tomobou.extensions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ProxyResponseTransformer extends ResponseTransformer {

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        // TODO Gzip クラスがあるのでResponseの内容がGzipされているかどうかで判断したほうがよさそう。
        var reqAcceptEncoding = request.getHeader("Accept-Encoding");
        var isGzip = reqAcceptEncoding != null && reqAcceptEncoding.contains("gzip");
        var bodyJson = Json.node((isGzip) ? Gzip.unGzipToString(response.getBody()) : response.getBodyAsString());

        parameters.keySet().forEach(key -> nodeTransform(bodyJson, Arrays.asList(key.split("\\.").clone()), parameters.getString(key)));

        return Response.Builder.like(response)
                .body((isGzip) ? Gzip.gzip(bodyJson.toString()) : bodyJson.toString().getBytes(StandardCharsets.UTF_8))
                .build();
    }

    /**
     * パラメータで指定されている要素に対するオペレーション（追加・削除）を行う。
     * Arrayの場合はそのArrayの子要素すべてに適用する。
     */
    private JsonNode nodeTransform(JsonNode current, List<String> path, String operation) {
        if (current.isArray()) {
            // current.elements でList要素が取れる
            for (Iterator<JsonNode> it = current.elements(); it.hasNext(); ) {
                nodeTransform(it.next(), path, operation);
            }
        } else {
            // TODO テンプレートパラメータを構造化すればこんなことしなくてもよさそう。
            if (path.size() == 1) {
                if ("!DELETE!".equals(operation)) {
                    ((ObjectNode) current).remove(path.get(0));
                } else if ("!INTEGER!".equals(operation)) {
                    ((ObjectNode) current).put(path.get(0), Integer.parseInt(operation.replace("!INTEGER!", "")));
                } else if ("!DOUBLE!".equals(operation)) {
                    ((ObjectNode) current).put(path.get(0), Double.parseDouble(operation.replace("!DOUBLE!", "")));
                } else if ("!ARRAY!".equals(operation)) {
                    var array = ((ObjectNode) current).putArray(path.get(0));
                    var addElementCount = Integer.parseInt(operation.replace("!ARRAY!", ""));
                    for (int i = 0; i < addElementCount; i++) {
                        array.addObject();
                    }
                } else {
                    ((ObjectNode) current).put(path.get(0), operation);
                }
            } else {
                var nextPath = path.subList(1, path.size());
                nodeTransform(current.get(path.get(0)), nextPath, operation);
            }
        }
        return current;
    }
//    private String isToString(InputStreamSource source) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getStream(), "UTF-8"));
//        StringBuilder sb = new StringBuilder();
//        char[] b = new char[1024];
//        int line;
//        while (0 <= (line = reader.read(b))) {
//            sb.append(b, 0, line);
//        }
//        return sb.toString();
//    }

    @Override
    public boolean applyGlobally() {
        return true;
    }

    @Override
    public String getName() {
        return "proxy-response-transformer";
    }

}
