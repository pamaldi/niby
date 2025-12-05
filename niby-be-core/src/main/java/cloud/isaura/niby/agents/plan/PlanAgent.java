package cloud.isaura.niby.agents.plan;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.BeanChatMemoryProviderSupplier.class)
public interface PlanAgent
{
    @SystemMessage(fromResource = "/system-messages/prototype-system.txt")
    Multi<String> chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
