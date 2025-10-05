package cloud.isaura.niby.agents.base;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
@RegisterAiService
public interface BasicAgent
{
    @SystemMessage(fromResource = "/system-messages/basic-agent.txt")
    Multi<String> chat(String userMessage);
}
