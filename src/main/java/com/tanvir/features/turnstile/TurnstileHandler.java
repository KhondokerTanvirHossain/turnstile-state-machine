package com.tanvir.features.turnstile;

import com.tanvir.core.util.exception.ErrorHandler;
import com.tanvir.core.util.exception.ExceptionHandlerUtil;
import com.tanvir.statemachine.turnstile.TurnstileEvents;
import com.tanvir.statemachine.turnstile.TurnstileStates;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TurnstileHandler {

    private final StateMachine<TurnstileStates, TurnstileEvents> stateMachine;

    public TurnstileHandler(@Qualifier("turnstileStateMachine") StateMachine<TurnstileStates, TurnstileEvents> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public Mono<ServerResponse> state(ServerRequest serverRequest) {
        return Mono.defer(() -> Mono.justOrEmpty(stateMachine.getState().getId()))
            .flatMap(state -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(state))
            .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
            ;
    }

    public Mono<ServerResponse> events(ServerRequest serverRequest) {
        Flux<EventResultResponseDto> responseFlux = serverRequest.bodyToFlux(EventData.class)
            .filter(ed -> ed.getEvent() != null)
            .map(ed -> MessageBuilder.withPayload(TurnstileEvents.valueOf(ed.getEvent())).build())
            .flatMap(turnstileEventsMessage -> stateMachine.sendEvent(Mono.just(turnstileEventsMessage)))
            .map(EventResultResponseDto::new)
            .doOnNext(eventResultResponseDto -> log.info("State machine event result: {}", eventResultResponseDto))
            .doOnError(e -> log.error("Error while processing event", e));

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseFlux, EventResultResponseDto.class);
    }

}
