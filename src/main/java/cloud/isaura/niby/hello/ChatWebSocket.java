package cloud.isaura.niby.hello;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/customer-support-agent")
public class ChatWebSocket
{

    private final Chat chat;

    public ChatWebSocket(Chat chat) {
        this.chat = chat;
    }

    @OnOpen
    public String onOpen() {
        return "Welcome to Niby! How can I help you today?";
    }

    @OnTextMessage
    public Multi<String> onTextMessage(String message) {
        return chat.chat(message);
    }
}
