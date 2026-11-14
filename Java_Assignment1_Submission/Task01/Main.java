public class Main {
    static class Vehicle {
        String registrationNumber;
        String type;
        Vehicle(String registrationNumber, String type) {
            this.registrationNumber = registrationNumber;
            this.type = type;
        }
    }
    static class ParkingSlot {
        int floor, slotNumber;
        Vehicle vehicle;
        ParkingSlot(int floor, int slotNumber) {
            this.floor = floor; this.slotNumber = slotNumber;
        }
        boolean isFree() { return vehicle == null; }
    }
    public static void main(String[] args) {
        ParkingSlot slot = new ParkingSlot(0, 1);
        Vehicle v = new Vehicle("TN01AB1111", "Car");
        slot.vehicle = v;
        System.out.println("Vehicle: " + slot.vehicle.registrationNumber);
        System.out.println("Slot free: " + slot.isFree());
    }
}