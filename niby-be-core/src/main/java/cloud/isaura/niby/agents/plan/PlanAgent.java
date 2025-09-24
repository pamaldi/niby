package cloud.isaura.niby.agents.plan;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface PlanAgent
{
    @SystemMessage("/system-messages/prototype-system.txt")
    String chat(String userMessage);
}
