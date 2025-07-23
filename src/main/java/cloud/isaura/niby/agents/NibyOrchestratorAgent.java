package cloud.isaura.niby.agents;

import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface NibyOrchestratorAgent
{

    Multi<String> chat(String userMessage);
}
