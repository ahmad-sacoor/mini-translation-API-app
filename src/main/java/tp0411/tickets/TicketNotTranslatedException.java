package tp0411.tickets;

public class TicketNotTranslatedException extends RuntimeException {
    private final Long id;

    public TicketNotTranslatedException(Long id) {
        super("Ticket with id " + id + " is not translated yet");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
