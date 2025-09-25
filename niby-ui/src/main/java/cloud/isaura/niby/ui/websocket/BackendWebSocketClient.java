package cloud.isaura.niby.ui.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.websocket.*;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Backend client that communicates with the niby-be-core service via WebSocket.
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

    private final ConcurrentMap<String, Session> backendSessions = new ConcurrentHashMap<>();

    /**
     * Establish a backend WebSocket connection for a specific client
     */
    public void connectForClient(String clientConnectionId) {
        LOG.infof("Establishing backend WebSocket connection for client %s", clientConnectionId);
        
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String backendWsUrl = String.format("ws://%s:%d/niby-ws", backendHost, backendPort);
            URI serverEndpointUri = new URI(backendWsUrl);
            
            // Create endpoint using the class directly
            Session session = container.connectToServer(BackendClientEndpoint.class, serverEndpointUri);
            
            // Store the client connection ID in the session for later use
            session.getUserProperties().put("clientConnectionId", clientConnectionId);
            session.getUserProperties().put("parent", this);
            
            backendSessions.put(clientConnectionId, session);
            
            LOG.infof("Successfully connected to backend WebSocket for client %s", clientConnectionId);
            
        } catch (Exception e) {
            LOG.errorf(e, "Failed to connect to backend WebSocket for client %s", clientConnectionId);
            proxy.sendToClient(clientConnectionId, "Sorry, I'm experiencing technical difficulties connecting to the backend service.");
        }
    }

    /**
     * Send a message to the backend WebSocket for a specific client
     */
    public void sendToBackend(String clientConnectionId, String message) {
        Session session = backendSessions.get(clientConnectionId);
        if (session == null || !session.isOpen()) {
            LOG.warnf("No active backend session for client %s", clientConnectionId);
            // Try to reconnect
            connectForClient(clientConnectionId);
            session = backendSessions.get(clientConnectionId);
        }
        
        if (session != null && session.isOpen()) {
            try {
                LOG.infof("Sending message to backend for client %s: %s", clientConnectionId, message);
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to send message to backend for client %s", clientConnectionId);
                proxy.sendToClient(clientConnectionId, "Sorry, I encountered an error while processing your request.");
            }
        }
    }

    /**
     * Disconnect backend WebSocket connection for a specific client
     */
    public void disconnectForClient(String clientConnectionId) {
        Session session = backendSessions.remove(clientConnectionId);
        if (session != null) {
            try {
                session.close();
                LOG.infof("Disconnected backend WebSocket for client %s", clientConnectionId);
            } catch (Exception e) {
                LOG.warnf(e, "Error closing backend session for client %s", clientConnectionId);
            }
        }
    }

    /**
     * Get the number of active backend connections
     */
    public int getActiveBackendConnectionCount() {
        return backendSessions.size();
    }

    /**
     * Close all backend connections (for shutdown)
     */
    public void closeAllBackendConnections() {
        LOG.info("Closing all backend WebSocket connections");
        backendSessions.values().forEach(session -> {
            try {
                session.close();
            } catch (Exception e) {
                LOG.warnf(e, "Error closing backend session");
            }
        });
        backendSessions.clear();
    }

    /**
     * WebSocket client endpoint for connecting to backend
     */
    @ClientEndpoint
    public static class BackendClientEndpoint {
        
        private static final Logger LOG = Logger.getLogger(BackendClientEndpoint.class);
        
        @OnOpen
        public void onOpen(Session session) {
            String clientConnectionId = (String) session.getUserProperties().get("clientConnectionId");
            LOG.infof("Backend WebSocket opened for client %s", clientConnectionId);
        }
        
        @OnMessage
        public void onMessage(String message, Session session) {
            String clientConnectionId = (String) session.getUserProperties().get("clientConnectionId");
            BackendWebSocketClient parent = (BackendWebSocketClient) session.getUserProperties().get("parent");
            
            LOG.infof("Received streaming message from backend for client %s: %s", clientConnectionId, message);
            // Forward the streaming message to the client
            if (parent != null && clientConnectionId != null) {
                parent.proxy.sendToClient(clientConnectionId, message);
            }
        }
        
        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            String clientConnectionId = (String) session.getUserProperties().get("clientConnectionId");
            BackendWebSocketClient parent = (BackendWebSocketClient) session.getUserProperties().get("parent");
            
            LOG.infof("Backend WebSocket closed for client %s: %s", clientConnectionId, closeReason);
            if (parent != null && clientConnectionId != null) {
                parent.backendSessions.remove(clientConnectionId);
            }
        }
        
        @OnError
        public void onError(Session session, Throwable throwable) {
            String clientConnectionId = (String) session.getUserProperties().get("clientConnectionId");
            BackendWebSocketClient parent = (BackendWebSocketClient) session.getUserProperties().get("parent");
            
            LOG.errorf(throwable, "Backend WebSocket error for client %s", clientConnectionId);
            if (parent != null && clientConnectionId != null) {
                parent.backendSessions.remove(clientConnectionId);
                parent.proxy.sendToClient(clientConnectionId, "Sorry, I encountered an error while processing your request.");
            }
        }
    }
}
