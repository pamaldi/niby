package cloud.isaura.niby.ui.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Backend client that communicates with the niby-be-core service.
 * For now, this uses a simple approach - we'll enhance it to use WebSocket later.
 */
@ApplicationScoped
public class BackendWebSocketClient {

    private static final Logger LOG = Logger.getLogger(BackendWebSocketClient.class);

    @ConfigProperty(name = "niby.backend.host")
    String backendHost;

    @ConfigProperty(name = "niby.backend.port")
    int backendPort;

    @Inject
    ChatWebSocketProxy proxy;

    private final Client httpClient = ClientBuilder.newClient();
    private final ConcurrentMap<String, Boolean> activeConnections = new ConcurrentHashMap<>();

    /**
     * Establish a backend connection for a specific client
     */
    public void connectForClient(String clientConnectionId) {
        LOG.infof("Registering backend connection for client %s", clientConnectionId);
        activeConnections.put(clientConnectionId, true);
        
        // Send welcome message to client
        proxy.sendToClient(clientConnectionId, "Welcome to Niby! How can I help you today?");
    }

    /**
     * Send a message to the backend for a specific client
     */
    public void sendToBackend(String clientConnectionId, String message) {
        if (!activeConnections.containsKey(clientConnectionId)) {
            LOG.warnf("No active connection for client %s", clientConnectionId);
            return;
        }

        LOG.infof("Sending message to backend for client %s: %s", clientConnectionId, message);
        
        // Send message to backend asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                String backendUrl = String.format("http://%s:%d/api/chat", backendHost, backendPort);
                
                Response response = httpClient
                    .target(backendUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(new ChatRequest(message)));
                
                if (response.getStatus() == 200) {
                    String responseText = response.readEntity(String.class);
                    LOG.infof("Received response from backend for client %s: %s", clientConnectionId, responseText);
                    return responseText;
                } else {
                    LOG.errorf("Backend returned error status %d for client %s", response.getStatus(), clientConnectionId);
                    return "Sorry, I'm having trouble connecting to the backend service.";
                }
            } catch (Exception e) {
                LOG.errorf(e, "Failed to send message to backend for client %s", clientConnectionId);
                return "Sorry, I encountered an error while processing your request.";
            }
        }).thenAccept(response -> {
            // Send response back to client
            proxy.sendToClient(clientConnectionId, response);
        });
    }

    /**
     * Disconnect backend connection for a specific client
     */
    public void disconnectForClient(String clientConnectionId) {
        activeConnections.remove(clientConnectionId);
        LOG.infof("Disconnected backend connection for client %s", clientConnectionId);
    }

    /**
     * Get the number of active backend connections
     */
    public int getActiveBackendConnectionCount() {
        return activeConnections.size();
    }

    /**
     * Close all backend connections (for shutdown)
     */
    public void closeAllBackendConnections() {
        LOG.info("Closing all backend connections");
        activeConnections.clear();
        httpClient.close();
    }

    /**
     * Simple DTO for chat requests
     */
    public static class ChatRequest {
        public String message;
        
        public ChatRequest() {}
        
        public ChatRequest(String message) {
            this.message = message;
        }
    }
}
