package cloud.isaura.niby.agents.base;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface BasicAgent
{
    @SystemMessage(fromResource = "/system-messages/basic-agent.txt")
    String chat(String userMessage);
}
