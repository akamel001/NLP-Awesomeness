package project3;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectSerializer {
    public static<T> void writeObject(T object, String filename) {
        try {
            System.out.println("Writing object to file:" + filename);
            FileOutputStream fout = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(object);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Failed to serialize!");
        }
    }

    @SuppressWarnings("unchecked")
    public static<T> T readObject(String filename) {
        T object = null;
        try {
            System.out.println("Reading object from file:" + filename);
            FileInputStream fin = new FileInputStream(filename);
            ObjectInputStream iis = new ObjectInputStream(fin);
            object = (T)iis.readObject();
            iis.close();
        } catch(Exception e) {
            System.err.println("Failed to deserialize!");
        }
        return object;
    }
}