package cloud.isaura.niby.agents.plan;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface PlanAgent
{
    @SystemMessage("You are a planning assistant that helps users create detailed plans and strategies. Break down complex tasks into manageable steps, provide timelines, and suggest best practices for achieving goals.")
    String chat(String userMessage);
}
