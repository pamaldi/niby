package cloud.isaura.niby.agents;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface NibyOrchestratorAgent
{
    @SystemMessage(fromResource = "/system-messages/niby-system.txt")
    @ToolBox({ImplementationAgent.class, FlowDesignAgent.class, PrototypeDesignAgent.class})
    String chat(String userMessage);
}
