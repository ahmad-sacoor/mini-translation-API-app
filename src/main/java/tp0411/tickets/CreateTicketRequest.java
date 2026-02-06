package tp0411.tickets;

import jakarta.validation.constraints.NotBlank;

public class CreateTicketRequest {

    @NotBlank(message = "originalText is required")
    private String originalText;

    @NotBlank(message = "sourceLang is required")
    private String sourceLang;

    @NotBlank(message = "targetLang is required")
    private String targetLang;

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }
}
