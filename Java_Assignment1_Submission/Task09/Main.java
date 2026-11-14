interface Chargeable {
    double chargingFee(int units);
    default void showChargingMessage() {
        System.out.println("Vehicle can use an EV charging point.");
    }
    static boolean validUnits(int units) { return units >= 0; }
}
abstract class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; }
    String getRegNo() { return regNo; }
    abstract double parkingFee(int hours);
}
class ElectricCar extends Vehicle implements Chargeable {
    ElectricCar(String regNo) { super(regNo); }
    @Override double parkingFee(int hours) { return hours * 30.0; }
    @Override public double chargingFee(int units) {
        if (!Chargeable.validUnits(units)) throw new IllegalArgumentException();
        return units * 12.0;
    }
}
public class Main {
    public static void main(String[] args) {
        ElectricCar ev = new ElectricCar("TN03C3");
        ev.showChargingMessage();
        System.out.println("Parking fee: Rs " + ev.parkingFee(2));
        System.out.println("Charging fee: Rs " + ev.chargingFee(5));
    }
}