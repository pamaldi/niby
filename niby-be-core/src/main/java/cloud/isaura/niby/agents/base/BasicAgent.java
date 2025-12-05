package cloud.isaura.niby.agents.base;

import cloud.isaura.niby.rag.RagRetrievalAugmentor;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(retrievalAugmentor = RagRetrievalAugmentor.class, chatMemoryProviderSupplier = RegisterAiService.BeanChatMemoryProviderSupplier.class)
public interface BasicAgent
{
    @SystemMessage(fromResource = "/system-messages/basic-agent.txt")
    Multi<String> chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
