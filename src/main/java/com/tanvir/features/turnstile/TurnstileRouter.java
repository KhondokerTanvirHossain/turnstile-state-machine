package com.tanvir.features.turnstile;

import com.tanvir.core.routes.RouteNames;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class TurnstileRouter {
    private final TurnstileHandler handler;

    @Bean
    public RouterFunction<ServerResponse> turnstileRouterConfig() {
        return RouterFunctions.route()
            .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                .GET(RouteNames.BASE_URL.concat(RouteNames.TURNSTILE).concat(RouteNames.STATE), handler::state)
                .POST(RouteNames.BASE_URL.concat(RouteNames.TURNSTILE).concat(RouteNames.EVENTS), handler::events))
            .build();
    }
}
