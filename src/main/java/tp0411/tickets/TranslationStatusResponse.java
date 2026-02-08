package tp0411.tickets;

public class TranslationStatusResponse {
    private Long id;
    private TicketStatus status;
    private String translatedText;

    public TranslationStatusResponse() {}

    public TranslationStatusResponse(Long id, TicketStatus status, String translatedText) {
        this.id = id;
        this.status = status;
        this.translatedText = translatedText;
    }

    public Long getId() {
        return id;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getTranslatedText() {
        return translatedText;
    }
}
