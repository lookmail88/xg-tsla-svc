package xuyang.dev.xgtslasvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import xuyang.dev.xgtslasvc.dto.OllamaRequest;
import xuyang.dev.xgtslasvc.dto.OllamaResponse;

import java.time.Duration;

@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final RestClient restClient;
    private final String model;
    private final String systemPrompt;

    public OllamaService(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model:llama3.2}") String model,
            @Value("${ollama.system-prompt}") String systemPrompt) {
        this.model = model;
        this.systemPrompt = systemPrompt;
        log.info("OllamaService initialized model={} baseUrl={}", model, baseUrl);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofMinutes(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public String generateAnalysis(String prompt) {
        OllamaRequest request = new OllamaRequest(model, prompt, systemPrompt);
        log.info("Ollama request model={} system=[{}] prompt=[{}]", request.getModel(), request.getSystem(), request.getPrompt());
        try {
            OllamaResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);
            if (response == null || !response.isDone()) {
                log.warn("Ollama returned empty or incomplete response");
                return null;
            }
            log.info("Ollama generation completed model={}", model);
            log.info("Ollama generation response:{}", response.getResponse());
            return response.getResponse();
        } catch (Exception e) {
            log.error("Failed to get Ollama response", e);
            return null;
        }
    }
}
