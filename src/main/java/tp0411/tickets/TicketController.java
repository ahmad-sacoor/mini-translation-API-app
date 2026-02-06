package tp0411.tickets;

import tp0411.api.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = new Ticket();

        // Set these here so clients don't control workflow state.
        ticket.setOriginalText(request.getOriginalText());
        ticket.setSourceLang(request.getSourceLang());
        ticket.setTargetLang(request.getTargetLang());
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setTranslatedText(null);
        ticket.setTranslatedAt(null);

        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("NOT_FOUND", "Ticket with id " + id + " not found")));
    }

    @GetMapping
    public ResponseEntity<?> listTickets(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            List<Ticket> all = ticketRepository.findAll();
            return ResponseEntity.ok(all);
        }

        TicketStatus parsed;
        try {
            parsed = TicketStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("BAD_REQUEST", "Invalid status: " + status));
        }

        return ResponseEntity.ok(ticketRepository.findByStatus(parsed));
    }
}
