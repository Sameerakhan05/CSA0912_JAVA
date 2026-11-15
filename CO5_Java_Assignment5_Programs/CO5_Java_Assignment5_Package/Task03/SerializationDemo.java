import java.io.*;
import java.util.*;
class Resident implements Serializable {
    private static final long serialVersionUID = 1L;
    String name, registerNumber;
    transient String password;       // do not persist credentials
    transient Object databaseConnection; // stand-in for live Connection
    Resident(String n, String r, String p) { name=n; registerNumber=r; password=p; }
}
class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    int roomNumber; List<Resident> residents = new ArrayList<>();
}
class Hostel implements Serializable {
    private static final long serialVersionUID = 1L;
    List<Room> rooms = new ArrayList<>();
}
public class SerializationDemo {
    public static void main(String[] args) throws Exception {
        Hostel h = new Hostel(); Room room = new Room(); room.roomNumber=101;
        room.residents.add(new Resident("Asha","1923001","secret"));
        h.rooms.add(room);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("hostel.ser"))) {
            out.writeObject(h);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("hostel.ser"))) {
            Hostel copy = (Hostel) in.readObject();
            Resident r = copy.rooms.get(0).residents.get(0);
            System.out.println(r.name + " " + r.registerNumber + " password=" + r.password);
        }
    }
}
