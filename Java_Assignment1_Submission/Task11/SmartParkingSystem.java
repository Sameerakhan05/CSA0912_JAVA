import java.util.*;

abstract class ParkedVehicle {
    private final String regNo;
    ParkedVehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Invalid registration");
        this.regNo = regNo;
    }
    String getRegNo() { return regNo; }
    abstract double hourlyRate();
    double fee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration");
        return ((minutes + 59) / 60) * hourlyRate();
    }
}
class Bike extends ParkedVehicle {
    Bike(String r) { super(r); }
    double hourlyRate() { return 10; }
}
class Sedan extends ParkedVehicle {
    Sedan(String r) { super(r); }
    double hourlyRate() { return 30; }
}
class EV extends Sedan {
    EV(String r) { super(r); }
}
class Ticket {
    private static int nextId = 1000;
    final int id = ++nextId;
    final ParkedVehicle vehicle;
    final int floor, slot;
    final long entryMillis = System.currentTimeMillis();
    Ticket(ParkedVehicle v, int f, int s) { vehicle=v; floor=f; slot=s; }
}
class Facility {
    private final String[][] slots;
    private final Map<String, Ticket> active = new HashMap<>();
    Facility(int[] capacity) {
        slots = new String[capacity.length][];
        for (int i=0;i<capacity.length;i++) slots[i]=new String[capacity[i]];
    }
    Ticket enter(ParkedVehicle v) {
        if (active.containsKey(v.getRegNo())) throw new IllegalArgumentException("Duplicate registration");
        for (int f=0;f<slots.length;f++)
            for (int s=0;s<slots[f].length;s++)
                if (slots[f][s]==null) {
                    slots[f][s]=v.getRegNo();
                    Ticket t=new Ticket(v,f,s); active.put(v.getRegNo(),t); return t;
                }
        return null;
    }
    double exit(String regNo, int minutes) {
        Ticket t=active.remove(regNo);
        if (t==null) throw new IllegalArgumentException("No active ticket");
        slots[t.floor][t.slot]=null;
        return t.vehicle.fee(minutes);
    }
}
public class SmartParkingSystem {
    public static void main(String[] args) {
        Facility f = new Facility(new int[]{1,2});
        Ticket t1=f.enter(new Bike("TN01A1"));
        Ticket t2=f.enter(new Sedan("TN02B2"));
        Ticket t3=f.enter(new EV("TN03C3"));
        System.out.println("Tickets issued: " + t1.id + ", " + t2.id + ", " + t3.id);
        System.out.println("Bike fee for 60 minutes: Rs " + f.exit("TN01A1",60));
        System.out.println("Car fee for 61 minutes: Rs " + f.exit("TN02B2",61));
        System.out.println("EV parking fee for 120 minutes: Rs " + f.exit("TN03C3",120));
        try { f.exit("UNKNOWN",30); }
        catch (IllegalArgumentException e) { System.out.println("Invalid exit handled: " + e.getMessage()); }
    }
}