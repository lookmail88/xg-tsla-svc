package xuyang.dev.xgtslasvc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OllamaResponse {

    private String model;

    @JsonProperty("created_at")
    private String createdAt;

    private String response;

    private boolean done;

    public String getModel() { return model; }
    public String getCreatedAt() { return createdAt; }
    public String getResponse() { return response; }
    public boolean isDone() { return done; }
}