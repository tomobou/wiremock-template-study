package net.tomobou;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.tomobou.extensions.ProxyResponseTransformer;
import net.tomobou.extensions.RequestBodyModifierFilter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class Main {
    public static void main(String[] args) {
        WireMockServer wireMockServer = new WireMockServer(options().port(8089)
                .extensions(new ProxyResponseTransformer(), new RequestBodyModifierFilter())
        );

        wireMockServer.start();

    }

}
