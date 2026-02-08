package tp0411.tickets;

public class TicketNotFoundException extends RuntimeException {
    private final Long id;

    public TicketNotFoundException(Long id) {
        super("Ticket with id " + id + " not found");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
