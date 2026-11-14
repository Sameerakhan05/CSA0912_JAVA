class ParkingTicket {
    private static int nextNumber = 1000;
    private final int ticketNumber;
    private final String registrationNumber;
    private final long entryTime;

    ParkingTicket() { this("UNKNOWN", System.currentTimeMillis()); }
    ParkingTicket(String registrationNumber) {
        this(registrationNumber, System.currentTimeMillis());
    }
    ParkingTicket(String registrationNumber, long entryTime) {
        if (registrationNumber == null || registrationNumber.isBlank())
            throw new IllegalArgumentException("Registration number is required");
        this.ticketNumber = ++nextNumber;
        this.registrationNumber = registrationNumber;
        this.entryTime = entryTime;
    }
    public String toString() {
        return ticketNumber + " " + registrationNumber + " " + entryTime;
    }
}
public class Main {
    public static void main(String[] args) {
        ParkingTicket t1 = new ParkingTicket();
        ParkingTicket t2 = new ParkingTicket("TN01AB1111");
        ParkingTicket t3 = new ParkingTicket("TN02CD2222", 1700000000000L);
        System.out.println(t1);
        System.out.println(t2);
        System.out.println(t3);
    }
}