package tp0411.tickets;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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

        String translated = "[" + ticket.getTargetLang() + "] " + ticket.getOriginalText();

        ticket.setTranslatedText(translated);
        ticket.setStatus(TicketStatus.TRANSLATED);
        ticket.setTranslatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }
}
