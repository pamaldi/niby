package cloud.isaura.niby.ui.websocket;

import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages WebSocket connections between clients and the proxy service.
 */
@ApplicationScoped
public class ConnectionManager {

    private static final Logger LOG = Logger.getLogger(ConnectionManager.class);

    private final ConcurrentMap<String, WebSocketConnection> clientConnections = new ConcurrentHashMap<>();

    /**
     * Add a client connection to the manager
     */
    public void addClientConnection(WebSocketConnection connection) {
        clientConnections.put(connection.id(), connection);
        LOG.infof("Added client connection: %s. Total connections: %d", 
                 connection.id(), clientConnections.size());
    }

    /**
     * Remove a client connection from the manager
     */
    public void removeClientConnection(String connectionId) {
        WebSocketConnection removed = clientConnections.remove(connectionId);
        if (removed != null) {
            LOG.infof("Removed client connection: %s. Total connections: %d", 
                     connectionId, clientConnections.size());
        }
    }

    /**
     * Get a client connection by ID
     */
    public WebSocketConnection getClientConnection(String connectionId) {
        return clientConnections.get(connectionId);
    }

    /**
     * Check if a client connection exists and is open
     */
    public boolean isClientConnected(String connectionId) {
        WebSocketConnection connection = clientConnections.get(connectionId);
        return connection != null && connection.isOpen();
    }

    /**
     * Get the number of active client connections
     */
    public int getActiveConnectionCount() {
        return clientConnections.size();
    }

    /**
     * Close all client connections (for shutdown)
     */
    public void closeAllConnections() {
        LOG.info("Closing all client connections");
        clientConnections.values().forEach(connection -> {
            try {
                if (connection.isOpen()) {
                    connection.close();
                }
            } catch (Exception e) {
                LOG.warnf(e, "Error closing connection: %s", connection.id());
            }
        });
        clientConnections.clear();
    }
}
