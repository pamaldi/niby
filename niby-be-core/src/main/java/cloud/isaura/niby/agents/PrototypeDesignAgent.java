package cloud.isaura.niby.agents;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface PrototypeDesignAgent
{

    @SystemMessage(fromResource = "/system-messages/prototype-system.txt")
    String chat(String userMessage);
}
