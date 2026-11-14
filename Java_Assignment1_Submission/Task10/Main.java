// This standalone example demonstrates encapsulation and validation.
// In the submitted package design, place this class in parking.model.
class ParkingSlot {
    private int slotNumber;
    private String vehicleNumber;

    ParkingSlot(int slotNumber) { setSlotNumber(slotNumber); }
    public int getSlotNumber() { return slotNumber; }
    public void setSlotNumber(int slotNumber) {
        if (slotNumber < 1) throw new IllegalArgumentException("Slot must be positive");
        this.slotNumber = slotNumber;
    }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) {
        if (vehicleNumber != null && vehicleNumber.isBlank())
            throw new IllegalArgumentException("Vehicle number cannot be blank");
        this.vehicleNumber = vehicleNumber;
    }
}
public class Main {
    public static void main(String[] args) {
        ParkingSlot slot = new ParkingSlot(5);
        slot.setVehicleNumber("TN01AB1111");
        System.out.println("Slot: " + slot.getSlotNumber());
        System.out.println("Vehicle: " + slot.getVehicleNumber());
        try { slot.setSlotNumber(-2); }
        catch (IllegalArgumentException e) { System.out.println("Validation: " + e.getMessage()); }
        Integer count = 10; // autoboxing
        System.out.println("Count: " + count.intValue());
    }
}