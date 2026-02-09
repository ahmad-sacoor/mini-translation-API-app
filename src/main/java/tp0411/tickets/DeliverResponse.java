package tp0411.tickets;

import java.time.LocalDateTime;

public class DeliverResponse {

    private boolean delivered;
    private Long ticketId;
    private LocalDateTime deliveredAt;
    private Payload payload;

    public DeliverResponse() {}

    public DeliverResponse(boolean delivered, Long ticketId, LocalDateTime deliveredAt, Payload payload) {
        this.delivered = delivered;
        this.ticketId = ticketId;
        this.deliveredAt = deliveredAt;
        this.payload = payload;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public Payload getPayload() {
        return payload;
    }

    public static class Payload {
        private String originalText;
        private String translatedText;
        private String sourceLang;
        private String targetLang;

        public Payload() {}

        public Payload(String originalText, String translatedText, String sourceLang, String targetLang) {
            this.originalText = originalText;
            this.translatedText = translatedText;
            this.sourceLang = sourceLang;
            this.targetLang = targetLang;
        }

        public String getOriginalText() {
            return originalText;
        }

        public String getTranslatedText() {
            return translatedText;
        }

        public String getSourceLang() {
            return sourceLang;
        }

        public String getTargetLang() {
            return targetLang;
        }
    }
}
