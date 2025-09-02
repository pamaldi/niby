package cloud.isaura.niby.ui.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * WebSocket proxy endpoint that handles client connections and forwards messages
 * to the backend niby-be-core service.
 */
@WebSocket(path = "/customer-support-agent")
@ApplicationScoped
public class ChatWebSocketProxy {

    private static final Logger LOG = Logger.getLogger(ChatWebSocketProxy.class);

    @Inject
    BackendWebSocketClient backendClient;

    @Inject
    ConnectionManager connectionManager;

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        LOG.infof("Client connected: %s", connection.id());
        connectionManager.addClientConnection(connection);
        
        // Establish backend connection for this client
        backendClient.connectForClient(connection.id());
    }

    @OnTextMessage
    public void onMessage(String message, WebSocketConnection connection) {
        LOG.infof("Received message from client %s: %s", connection.id(), message);
        
        // Forward message to backend
        backendClient.sendToBackend(connection.id(), message);
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        LOG.infof("Client disconnected: %s", connection.id());
        connectionManager.removeClientConnection(connection.id());
        
        // Close backend connection for this client
        backendClient.disconnectForClient(connection.id());
    }

    @OnError
    public void onError(WebSocketConnection connection, Throwable throwable) {
        LOG.errorf(throwable, "WebSocket error for client %s", connection.id());
        connectionManager.removeClientConnection(connection.id());
        backendClient.disconnectForClient(connection.id());
    }

    /**
     * Send message to a specific client connection
     */
    public void sendToClient(String connectionId, String message) {
        WebSocketConnection clientConnection = connectionManager.getClientConnection(connectionId);
        if (clientConnection != null && clientConnection.isOpen()) {
            clientConnection.sendTextAndAwait(message);
        } else {
            LOG.warnf("Cannot send message to client %s - connection not found or closed", connectionId);
        }
    }
}
