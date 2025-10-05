package cloud.isaura.niby.agents.plan;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
@RegisterAiService
public interface PlanAgent
{
    @SystemMessage(fromResource = "/system-messages/prototype-system.txt")
    Multi<String> chat(String userMessage);
}
