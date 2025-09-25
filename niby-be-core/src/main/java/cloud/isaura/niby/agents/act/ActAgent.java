package cloud.isaura.niby.agents.act;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
@RegisterAiService
public interface ActAgent
{
    @SystemMessage(fromResource = "/system-messages/plan-agent.txt")
    Multi<String> chat(String userMessage);
}
