import java.util.*;

interface PassHolder {
    boolean hasValidPass();
}
interface PricingRule {
    double parkingFee(int minutes, int startHour);
}
abstract class NewVehicle {
    private final String regNo;
    NewVehicle(String regNo) { this.regNo=regNo; }
    String getRegNo() { return regNo; }
    abstract double fee(int minutes, int startHour);
}
class StandardVehicle extends NewVehicle {
    private final double rate;
    StandardVehicle(String regNo,double rate) { super(regNo); this.rate=rate; }
    double fee(int minutes,int startHour) { return ((minutes+59)/60)*rate; }
}
class E_Bike extends NewVehicle {
    E_Bike(String regNo) { super(regNo); }
    double fee(int minutes,int startHour) { return 25.0; } // flat fee
}
class DeliveryVan extends NewVehicle {
    DeliveryVan(String regNo) { super(regNo); }
    double fee(int minutes,int startHour) {
        double rate = (startHour >= 9 && startHour < 17) ? 60.0 : 30.0;
        return ((minutes+59)/60)*rate;
    }
}
class VisitorPass implements PassHolder {
    private final boolean valid;
    VisitorPass(boolean valid) { this.valid=valid; }
    public boolean hasValidPass() { return valid; }
}
class FlexibleFacility {
    private final String[][] slots;
    FlexibleFacility(int[] slotsPerFloor) {
        slots=new String[slotsPerFloor.length][];
        for(int i=0;i<slotsPerFloor.length;i++) slots[i]=new String[slotsPerFloor[i]];
    }
    int[] allocate(String regNo) {
        for(int f=0;f<slots.length;f++)
            for(int s=0;s<slots[f].length;s++)
                if(slots[f][s]==null) { slots[f][s]=regNo; return new int[]{f,s}; }
        return null;
    }
}
public class AdaptedParkingSystem {
    public static void main(String[] args) {
        FlexibleFacility facility = new FlexibleFacility(new int[]{2,3,4,2,5});
        NewVehicle ebike = new E_Bike("TN10E1111");
        NewVehicle van = new DeliveryVan("TN20V2222");
        System.out.println("E-bike slot: " + Arrays.toString(facility.allocate(ebike.getRegNo())));
        System.out.println("Delivery van slot: " + Arrays.toString(facility.allocate(van.getRegNo())));
        System.out.println("E-bike flat fee: Rs " + ebike.fee(90,10));
        System.out.println("Delivery van daytime fee: Rs " + van.fee(61,10));
        System.out.println("Delivery van evening fee: Rs " + van.fee(61,18));
        VisitorPass pass = new VisitorPass(true);
        System.out.println("Visitor one-day pass valid: " + pass.hasValidPass());
    }
}