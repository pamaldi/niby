package cloud.isaura.niby.resources;

import cloud.isaura.niby.agents.NibyOrchestratorAgent;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chat")
public class ChatResource {

    @Inject
    NibyOrchestratorAgent nibyOrchestratorAgent;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(ChatRequest request) {
        return nibyOrchestratorAgent.chat(request.message);
    }

    public static class ChatRequest {
        public String message;
        
        public ChatRequest() {}
        
        public ChatRequest(String message) {
            this.message = message;
        }
    }
}
