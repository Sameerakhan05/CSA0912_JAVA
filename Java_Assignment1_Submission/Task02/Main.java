import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int[] slotsPerFloor = {2, 3, 1};
        String[][] slots = new String[slotsPerFloor.length][];
        for (int i = 0; i < slotsPerFloor.length; i++)
            slots[i] = new String[slotsPerFloor[i]];

        System.out.print("Enter floor index (0-2): ");
        int floor = sc.nextInt();
        System.out.print("Enter slot index: ");
        int slot = sc.nextInt();

        if (floor >= 0 && floor < slots.length &&
            slot >= 0 && slot < slots[floor].length) {
            slots[floor][slot] = "TN01AB1111";
            System.out.println("Vehicle allocated successfully.");
            System.out.println("Stored registration: " + slots[floor][slot]);
        } else {
            System.out.println("Invalid floor or slot.");
        }
        double minutes = 130;
        double hours = Math.ceil(minutes / 60.0);
        System.out.println("Billable hours: " + (int) hours);
        sc.close();
    }
}