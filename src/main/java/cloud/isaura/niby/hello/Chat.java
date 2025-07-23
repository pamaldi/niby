package cloud.isaura.niby.hello;

import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@SessionScoped
@RegisterAiService
public interface Chat
{

    Multi<String> chat(String userMessage);
}
