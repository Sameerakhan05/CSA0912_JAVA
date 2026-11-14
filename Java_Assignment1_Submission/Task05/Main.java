class Ticket {
    private static int counter = 500;
    private final int ticketId;
    Ticket() { ticketId = ++counter; }
    int getTicketId() { return ticketId; }
    static int getNextTicketNumber() { return counter + 1; }
}
public class Main {
    public static void main(String[] args) {
        Ticket a = new Ticket();
        Ticket b = new Ticket();
        System.out.println("Ticket A: " + a.getTicketId());
        System.out.println("Ticket B: " + b.getTicketId());
        System.out.println("Next ticket: " + Ticket.getNextTicketNumber());
    }
}