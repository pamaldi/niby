# WebSocket Logging Implementation

## What was added:

1. **SLF4J Logger**: Added comprehensive logging to NibyWebSocket class
2. **Session Tracking**: Each WebSocket connection is tracked with a unique session ID
3. **Message Logging**: All incoming messages are logged with different levels

## Log Output Examples:

### When a JSON message is received:
```
INFO  [NibyWebSocket] Received message from WebSocket session [abc123]
DEBUG [NibyWebSocket] Raw message content: {"message":"Hello world","mode":"plan"}
DEBUG [NibyWebSocket] Parsed message: 'Hello world', mode: 'plan'
INFO  [NibyWebSocket] Routing to PlanAgent for session [abc123] with mode: plan
DEBUG [NibyWebSocket] Agent PlanAgent processed message and returned response of length: 156
```

### When a plain text message is received:
```
INFO  [NibyWebSocket] Received message from WebSocket session [def456]
DEBUG [NibyWebSocket] Raw message content: Hello there
WARN  [NibyWebSocket] Failed to parse JSON message from session [def456], treating as plain text. Error: Unexpected character ('H' (code 72))
DEBUG [NibyWebSocket] Using fallback: message='Hello there', mode='basic'
INFO  [NibyWebSocket] Routing to BasicAgent for session [def456] with mode: basic
DEBUG [NibyWebSocket] Agent BasicAgent processed message and returned response of length: 89
```

### When an unknown mode is used:
```
INFO  [NibyWebSocket] Received message from WebSocket session [ghi789]
DEBUG [NibyWebSocket] Parsed message: 'Test message', mode: 'unknown'
WARN  [NibyWebSocket] Unknown mode 'unknown' for session [ghi789], falling back to BasicAgent
DEBUG [NibyWebSocket] Agent BasicAgent (fallback) processed message and returned response of length: 67
```

## Log Levels Used:

- **INFO**: Normal operations (message received, agent routing)
- **DEBUG**: Detailed information (message content, response lengths)
- **WARN**: Fallback scenarios (JSON parsing failures, unknown modes)

## Benefits:

1. **Full Traceability**: Track every message from receipt to agent selection
2. **Session Identification**: Know which user/session sent each message
3. **Agent Selection Visibility**: See exactly which agent handled each request
4. **Error Handling**: Clear logging when fallbacks occur
5. **Performance Monitoring**: Response length tracking for monitoring
