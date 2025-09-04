package arman.common.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

    ///File file;

    /*public FileUtil(String filePath){
        file = new File(filePath);
    }
    */

    public void read(File file){

    }

    public void write(){

    }

    public void delete(){

    }

    public void rename(){

    }

    public void copy(){

    }

    public void move(){

    }

    public boolean saveFile(File file, byte[] content){
        if (file == null) return false;
        try (OutputStream output = new FileOutputStream(file)) {
            output.write(content);
            return true;
        } catch (IOException ignored) {}
        return false;
    }

    public Bitmap getBitmap(byte[] imageData) {
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

    }
    public Bitmap getBitmap(File imageFile) {

        return null; //BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

    }
}
