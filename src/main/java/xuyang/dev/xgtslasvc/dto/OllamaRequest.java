package xuyang.dev.xgtslasvc.dto;

public class OllamaRequest {

    private String model;
    private String prompt;
    private String system;
    private boolean stream = false;

    public OllamaRequest(String model, String prompt, String system) {
        this.model = model;
        this.prompt = prompt;
        this.system = system;
    }

    public String getModel() { return model; }
    public String getPrompt() { return prompt; }
    public String getSystem() { return system; }
    public boolean isStream() { return stream; }

}