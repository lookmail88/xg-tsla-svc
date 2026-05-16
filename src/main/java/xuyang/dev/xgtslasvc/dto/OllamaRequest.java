package xuyang.dev.xgtslasvc.dto;

public class OllamaRequest {

    private String model;
    private String prompt;
    private String system;
    private boolean stream = false;
    /**
     * Forces Ollama to coerce the model's output into valid JSON.
     * See https://github.com/ollama/ollama/blob/main/docs/api.md#request-with-format
     */
    private String format = "json";

    public OllamaRequest(String model, String prompt, String system) {
        this.model = model;
        this.prompt = prompt;
        this.system = system;
    }

    public String getModel() { return model; }
    public String getPrompt() { return prompt; }
    public String getSystem() { return system; }
    public boolean isStream() { return stream; }
    public String getFormat() { return format; }

}