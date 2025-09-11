package cloud.isaura.niby.agents.act;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface ActAgent
{
    @SystemMessage(fromResource = "/system-messages/plan-agent.txt")
    String chat(String userMessage);
}
