package cloud.isaura.niby.agents;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/customer-support-agent")
public class NibyWebSocket
{

    private final NibyOrchestratorAgent nibyOrchestratorAgent;

    public NibyWebSocket(NibyOrchestratorAgent nibyOrchestratorAgent)
    {
        this.nibyOrchestratorAgent = nibyOrchestratorAgent;
    }

    @OnOpen
    public String onOpen()
    {
        return "Welcome to Niby! How can I help you today?";
    }

    @OnTextMessage
    public String onTextMessage(String message)
    {
        return nibyOrchestratorAgent.chat(message);
    }
}
