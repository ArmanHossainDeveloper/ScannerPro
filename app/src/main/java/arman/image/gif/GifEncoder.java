package arman.image.gif;

import android.media.Image;

public class GifEncoder {

    //HashMap<String, byte[]> hashMap = new HashMap<>();
    int width, height, channelPerPixel = 1;
    byte[] pixelBytes;
    boolean isError = false;

    public GifEncoder(int width, int height, byte[] pixels){
        this.width = width;
        this.height = height;
        /*this.width = height;
        this.height = width;*/
        pixelBytes = pixels;
    }

    public byte[] encodeGrayscale(){

        return null;
    }

}
