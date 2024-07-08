# turnstile-state-machine

### State machine diagram

```bash
+----------------------------------------------------------------+
|                              SM                                |
+----------------------------------------------------------------+
|                                                                |
|         +----------------+          +----------------+         |
|     *-->|     LOCKED     |          |    UNLOCKED    |         |
|         +----------------+          +----------------+         |
|     +---| entry/         |          | entry/         |---+     |
|     |   | exit/          |          | exit/          |   |     |
|     |   |                |          |                |   |     |
| PUSH|   |                |---COIN-->|                |   |COIN |
|     |   |                |          |                |   |     |
|     |   |                |          |                |   |     |
|     |   |                |<--PUSH---|                |   |     |
|     +-->|                |          |                |<--+     |
|         |                |          |                |         |
|         +----------------+          +----------------+         |
|                                                                |
+----------------------------------------------------------------+
```

### State machine Configuration
1. Step one is to define the states of the state machine.
   ```java
   public enum TurnstileStates {
        LOCKED, UNLOCKED
   }
   ```
2. Step two is to define the events that can trigger the state change.
    ```java
    public enum TurnstileEvents {
        PUSH, COIN
    }
    ```
3. Step three is to define the initial state of the state machine and also feed machine all the states.
   ```java
   @Override
    public void configure(StateMachineStateConfigurer<TurnstileStates, TurnstileEvents> states)
        throws Exception {
        states
            .withStates()
            .initial(TurnstileStates.LOCKED)
            .states(EnumSet.allOf(TurnstileStates.class));
    }
   ```
4. Step four is to define the transitions between the states based on the events.
    ```json
    {
      "states": [
        {
          "name": "LOCKED",
          "entry": "entry/",
          "exit": "exit/"
        },
        {
          "name": "UNLOCKED",
          "entry": "entry/",
          "exit": "exit/"
        }
      ],
      "events": [
        {
          "name": "PUSH"
        },
        {
          "name": "COIN"
        }
      ],
      "transitions": [
        {
          "from": "LOCKED",
          "to": "UNLOCKED",
          "event": "COIN"
        },
        {
          "from": "UNLOCKED",
          "to": "LOCKED",
          "event": "PUSH"
        }
      ]
    }
    ```
   ```java
    @Override
    public void configure(StateMachineTransitionConfigurer<TurnstileStates, TurnstileEvents> transitions)
        throws Exception {
        transitions
            .withExternal()
            .source(TurnstileStates.LOCKED)
            .target(TurnstileStates.UNLOCKED)
            .event(TurnstileEvents.COIN)
            .and()
            .withExternal()
            .source(TurnstileStates.UNLOCKED)
            .target(TurnstileStates.LOCKED)
            .event(TurnstileEvents.PUSH);
    }
   ```
5. Step five is start the state machine with a listener which will listen to any change of the statemachine.
    ```java
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
    ```

### Endpoints

#### GET /state
- **Description**: CHeck the current state of the state machine.
- **URL**: `/state`
- **Method**: `GET`
- **Curl**:
    ```bash
    curl --location 'http://localhost:8083/base/url/api/v1/turnstile/state'
    ```
- **Response**:
    ```json
      "LOCKED"
    ```

#### POST /events
- **Description**: Trigger events to change states.
- **URL**: `/events`
- **Method**: `POST`
- **Body**:
  ```json
    [
        {
            "event": "PUSH"
        },
        {
            "event": "COIN"
        }
    ]
    ```
- **Curl**:
  ```bash
  curl --location 'http://localhost:8083/base/url/api/v1/turnstile/events' \
    --header 'Content-Type: application/json' \
    --data '[
        {
          "event": "PUSH"
        },
        {
          "event": "COIN"
        }
    ]'
    ```
-  **Response**:
      ```json
    [
        {
            "regionUuid": "f6fed8e1-75e1-4e2e-a638-51b020a750d6",
            "messagePayload": "PUSH",
            "event": "PUSH",
            "messageHeaders": {
                "id": "c9e44a81-c925-0c53-62e8-8f1ee6d44cbe",
                "timestamp": 1720419300113
            },
            "regionState": "LOCKED",
            "resultType": "ACCEPTED"
        },
        {
            "regionUuid": "f6fed8e1-75e1-4e2e-a638-51b020a750d6",
            "messagePayload": "COIN",
            "event": "COIN",
            "messageHeaders": {
                "id": "761b009e-da35-4a17-5683-dee6095e43b5",
                "timestamp": 1720419300120
            },
            "regionState": "UNLOCKED",
            "resultType": "ACCEPTED"
        }
    ]       
    ```
  
