package cloud.isaura.niby.ws;

import cloud.isaura.niby.dto.ChatMessage;
import cloud.isaura.niby.agents.act.ActAgent;
import cloud.isaura.niby.agents.base.BasicAgent;
import cloud.isaura.niby.agents.plan.PlanAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@WebSocket(path = "/niby-ws")
public class NibyWebSocket {

    private static final Logger log = LoggerFactory.getLogger(NibyWebSocket.class);

    private final BasicAgent basicAgent;
    private final PlanAgent planAgent;
    private final ActAgent actAgent;
    private final ObjectMapper objectMapper;

    @Inject
    public NibyWebSocket(BasicAgent basicAgent,
                         PlanAgent planAgent,
                         ActAgent actAgent,
                         ObjectMapper objectMapper) {
        this.basicAgent = basicAgent;
        this.planAgent = planAgent;
        this.actAgent = actAgent;
        this.objectMapper = objectMapper;
    }

    @OnOpen
    public String onOpen() {
        return "Welcome to Niby! How can I help you today?";
    }

    @OnTextMessage
    public Multi<String> onTextMessage(String message, WebSocketConnection connection) {
        log.info("Received message from WebSocket");
        log.info("Raw message content: {}", message);

        String sessionId = connection.id();

        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            String mode = chatMessage.getMode();
            String text = chatMessage.getMessage();
            log.info("Parsed message: '{}', mode: '{}'", text, mode);
            return routeToAgent(sessionId, text, mode);
        } catch (Exception e) {
            log.warn("Failed to parse JSON, treating as plain text: {}", e.getMessage());
            return routeToAgent(sessionId, message, "basic");
        }
    }

    private Multi<String> routeToAgent(String sessionId, String message, String mode) {
        String normalizedMode = (mode == null ? "basic" : mode.toLowerCase(Locale.ROOT));
        Multi<String> response;
        switch (normalizedMode) {
            case "plan":
                log.info("Routing to PlanAgent");
                response = planAgent.chat(sessionId, message);
                break;
            case "act":
                log.info("Routing to ActAgent");
                response = actAgent.chat(sessionId, message);
                break;
            case "basic":
            default:
                if (!"basic".equals(normalizedMode)) {
                    log.warn("Unknown mode '{}', falling back to BasicAgent", mode);
                } else {
                    log.info("Routing to BasicAgent");
                }
                response = basicAgent.chat(sessionId, message);
                break;
        }

        // Add logging to debug streaming
        return response
                .onItem().invoke(item -> log.info("Streaming chunk: {}", item))
                .onFailure().invoke(error -> log.error("Stream error: {}", error.getMessage(), error))
                .onCompletion().invoke(() -> log.info("Stream completed"));
    }

    @OnError
    void onError(Throwable t, WebSocketConnection connection) {
        log.error("WebSocket error in session [{}]: {}", connection.id(), t.toString(), t);
    }
}
