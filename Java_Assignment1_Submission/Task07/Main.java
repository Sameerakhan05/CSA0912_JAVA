class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; }
    String getRegNo() { return regNo; }
    double calculateFee(int hours) { return hours * 20.0; }
    @Override public String toString() { return getClass().getSimpleName() + " " + regNo; }
    @Override public boolean equals(Object obj) {
        return obj instanceof Vehicle &&
               regNo.equals(((Vehicle) obj).regNo);
    }
    @Override public int hashCode() { return regNo.hashCode(); }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo); }
    @Override double calculateFee(int hours) { return hours * 10.0; }
}
class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override double calculateFee(int hours) { return hours * 30.0; }
}
public class Main {
    public static void main(String[] args) {
        Vehicle bike = new TwoWheeler("TN01A1");
        Vehicle car = new Car("TN02B2");
        System.out.println(bike + " fee Rs " + bike.calculateFee(2));
        System.out.println(car + " fee Rs " + car.calculateFee(2));
        System.out.println(new Car("TN02B2").equals(new TwoWheeler("TN02B2")));
    }
}