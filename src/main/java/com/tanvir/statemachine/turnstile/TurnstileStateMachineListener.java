package com.tanvir.statemachine.turnstile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TurnstileStateMachineListener {

    private final StateMachine<TurnstileStates, TurnstileEvents> stateMachine;

    public TurnstileStateMachineListener(@Qualifier("turnstileStateMachine") StateMachine<TurnstileStates, TurnstileEvents> stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostConstruct
    public void checkStateMachineInitialization() {
        stateMachine.addStateListener(new StateMachineListenerAdapter<TurnstileStates, TurnstileEvents>() {
            @Override
            public void stateChanged(State<TurnstileStates, TurnstileEvents> from, State<TurnstileStates, TurnstileEvents> to) {
                log.info("State changed from {} to {}", from, to);
            }
        });
        stateMachine.startReactively()
            .subscribe();
    }
}
