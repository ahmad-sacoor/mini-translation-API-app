package tp0411.tickets;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tp0411.integration.TranslationClient;

import java.time.LocalDateTime;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TranslationClient translationClient;

    public TicketService(TicketRepository ticketRepository, TranslationClient translationClient) {
        this.ticketRepository = ticketRepository;
        this.translationClient = translationClient;
    }

    public Ticket getOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public Ticket translate(Long id) {
        Ticket ticket = getOrThrow(id);

        if (ticket.getStatus() == TicketStatus.TRANSLATED) {
            throw new TicketAlreadyTranslatedException(id);
        }

        try {
            String translated = translationClient.translate(
                    ticket.getOriginalText(),
                    ticket.getSourceLang(),
                    ticket.getTargetLang()
            );

            ticket.setTranslatedText(translated);
            ticket.setStatus(TicketStatus.TRANSLATED);
            ticket.setTranslatedAt(LocalDateTime.now());
            return ticketRepository.save(ticket);

        } catch (RuntimeException ex) {
            // Provider failed → mark FAILED in DB and return 502 to the client.
            ticket.setStatus(TicketStatus.FAILED);
            ticket.setTranslatedText(null);
            ticket.setTranslatedAt(null);
            ticketRepository.save(ticket);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Translation provider failed: " + ex.getMessage()
            );
        }
    }

    public DeliverResponse deliver(Long id) {
        Ticket ticket = getOrThrow(id);

        if (ticket.getStatus() != TicketStatus.TRANSLATED) {
            throw new TicketNotTranslatedException(id);
        }

        DeliverResponse.Payload payload = new DeliverResponse.Payload(
                ticket.getOriginalText(),
                ticket.getTranslatedText(),
                ticket.getSourceLang(),
                ticket.getTargetLang()
        );

        return new DeliverResponse(
                true,
                ticket.getId(),
                LocalDateTime.now(),
                payload
        );
    }
}
