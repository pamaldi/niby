package cloud.isaura.niby.agents;

import cloud.isaura.niby.agents.act.ActAgent;
import cloud.isaura.niby.agents.base.BasicAgent;
import cloud.isaura.niby.agents.plan.PlanAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.*;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
    public void onTextMessage(String message, WebSocketConnection connection) {
        String sessionId = connection.id();
        log.info("Received message from WebSocket session [{}]", sessionId);
        log.info("Raw message content: {}", message);

        // Process asynchronously to avoid blocking the worker thread
        CompletableFuture.supplyAsync(() -> {
            try {
                ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
                String mode = chatMessage.getMode();
                String text = chatMessage.getMessage();
                log.info("Parsed message: '{}', mode: '{}'", text, mode);
                return routeToAgent(text, mode, sessionId);
            } catch (Exception e) {
                log.warn("Failed to parse JSON for session [{}], treating as plain text: {}", sessionId, e.getMessage());
                return routeToAgent(message, "basic", sessionId);
            }
        }).thenAccept(response -> {
            try {
                connection.sendTextAndAwait(response);
                log.info("Sent response to session [{}]: {}", sessionId, response.substring(0, Math.min(100, response.length())) + "...");
            } catch (Exception e) {
                log.error("Failed to send response to session [{}]: {}", sessionId, e.getMessage(), e);
            }
        }).exceptionally(throwable -> {
            log.error("Error processing message for session [{}]: {}", sessionId, throwable.getMessage(), throwable);
            try {
                connection.sendTextAndAwait("Sorry, I encountered an error processing your request. Please try again.");
            } catch (Exception e) {
                log.error("Failed to send error message to session [{}]: {}", sessionId, e.getMessage(), e);
            }
            return null;
        });
    }

    private String routeToAgent(String message, String mode, String sessionId) {
        String normalizedMode = (mode == null ? "basic" : mode.toLowerCase(Locale.ROOT));
        switch (normalizedMode) {
            case "plan":
                log.info("Routing to PlanAgent for session [{}]", sessionId);
                return planAgent.chat(message);
            case "act":
                log.info("Routing to ActAgent for session [{}]", sessionId);
                return actAgent.chat(message);
            case "basic":
            default:
                if (!"basic".equals(normalizedMode)) {
                    log.warn("Unknown mode '{}' for session [{}], falling back to BasicAgent", mode, sessionId);
                } else {
                    log.info("Routing to BasicAgent for session [{}]", sessionId);
                }
                return basicAgent.chat(message);
        }
    }

    @OnError
    void onError(Throwable t, WebSocketConnection connection) {
        log.error("WebSocket error in session [{}]: {}", connection.id(), t.toString(), t);
    }
}
