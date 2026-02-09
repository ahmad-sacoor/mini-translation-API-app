package tp0411.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal DeepL client using Java 17 HttpClient.
 *
 * DeepL Free base URL: https://api-free.deepl.com/v2
 * Endpoint: POST /translate
 * Auth header: Authorization: DeepL-Auth-Key <key>
 */
@Component
public class TranslationClient {

    private final DeeplProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TranslationClient(DeeplProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .build();
    }

    /**
     * Translates text using DeepL.
     *
     * sourceLang is optional; if blank, we omit it and DeepL auto-detects.
     * targetLang is required.
     */
    public String translate(String text, String sourceLang, String targetLang) {
        String baseUrl = safeTrim(props.getBaseUrl());
        if (baseUrl.isEmpty()) throw new RuntimeException("DeepL baseUrl is blank");

        String apiKey = safeTrim(props.getApiKey());
        if (apiKey.isEmpty()) throw new RuntimeException("DeepL API key is missing (DEEPL_API_KEY)");

        String target = toDeeplLang(targetLang);
        if (target.isEmpty()) throw new RuntimeException("targetLang is required");

        // Build JSON body like:
        // { "text": ["Hello"], "target_lang": "PT", "source_lang": "EN" }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", new String[]{ text });
        body.put("target_lang", target);

        String source = toDeeplLang(sourceLang);
        if (!source.isEmpty()) {
            body.put("source_lang", source);
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new RuntimeException("Failed to build DeepL request JSON", e);
        }

        URI uri = URI.create(baseUrl.replaceAll("/+$", "") + "/translate");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .header("Content-Type", "application/json")
                .header("Authorization", "DeepL-Auth-Key " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException("DeepL request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("DeepL request interrupted", e);
        }

        // DeepL uses standard HTTP status codes (400 bad request, 403 auth failed, 429 too many requests, 456 quota exceeded, etc.). :contentReference[oaicite:4]{index=4}
        if (response.statusCode() != 200) {
            String snippet = response.body() == null ? "" : response.body();
            if (snippet.length() > 400) snippet = snippet.substring(0, 400) + "...";
            throw new RuntimeException("DeepL HTTP " + response.statusCode() + ": " + snippet);
        }

        // Response shape:
        // { "translations": [ { "text": "..." , ... } ] } :contentReference[oaicite:5]{index=5}
        try {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode translations = root.get("translations");
            if (translations == null || !translations.isArray() || translations.isEmpty()) {
                throw new RuntimeException("DeepL response missing translations");
            }
            JsonNode first = translations.get(0);
            JsonNode translatedText = first.get("text");
            if (translatedText == null || translatedText.isNull()) {
                throw new RuntimeException("DeepL response missing translations[0].text");
            }
            return translatedText.asText();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse DeepL response JSON", e);
        }
    }

    /**
     * Our UI sends languages like "en", "pt", "fr", "es".
     * DeepL expects uppercase codes like "EN", "PT", etc.
     */
    private static String toDeeplLang(String lang) {
        String v = safeTrim(lang);
        if (v.isEmpty()) return "";
        return v.toUpperCase();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
