package cloud.isaura.niby.agents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessage {
    private final String message;
    private final String mode;

    @JsonCreator
    public ChatMessage(@JsonProperty("message") String message, @JsonProperty("mode") String mode) {
        this.message = message;
        this.mode = mode != null ? mode : "basic"; // default to basic if not specified
    }

    public String getMessage() {
        return message;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "message='" + message + '\'' +
                ", mode='" + mode + '\'' +
                '}';
    }
}
