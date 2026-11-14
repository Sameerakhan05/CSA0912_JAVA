class Vehicle {
    protected String regNo;
    Vehicle(String regNo) {
        this.regNo = regNo;
        System.out.println("Vehicle constructor");
    }
}
class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
        System.out.println("Car constructor");
    }
}
class ElectricCar extends Car {
    ElectricCar(String regNo) {
        super(regNo);
        System.out.println("ElectricCar constructor");
    }
}
public class Main {
    public static void main(String[] args) {
        ElectricCar ev = new ElectricCar("TN03C3");
        System.out.println("Registration: " + ev.regNo);
    }
}