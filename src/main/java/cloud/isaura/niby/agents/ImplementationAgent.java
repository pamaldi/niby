package cloud.isaura.niby.agents;

import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface ImplementationAgent
{

    String chat(String userMessage);
}
