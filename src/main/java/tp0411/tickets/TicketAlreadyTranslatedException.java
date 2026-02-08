package tp0411.tickets;

public class TicketAlreadyTranslatedException extends RuntimeException {
    private final Long id;

    public TicketAlreadyTranslatedException(Long id) {
        super("Ticket with id " + id + " is already translated");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
