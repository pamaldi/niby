package cloud.isaura.niby.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@ApplicationScoped
public class ChatMemoryConfiguration implements Supplier<ChatMemory> {

    @Inject
    ChatMemoryStore chatMemoryStore;

    @Override
    public ChatMemory get() {
        return MessageWindowChatMemory.builder()
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(20)
                .build();
    }
}
