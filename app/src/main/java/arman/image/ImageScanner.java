package arman.image;

import android.media.Image;

import java.io.File;
import java.nio.ByteBuffer;

import arman.common.infocodes.InfoCode;
import arman.common.io.FileUtil;
import arman.image.gif.GifEncoder;
import arman.image.jpg.JpgEncoder;
import arman.image.opencv.OpenCVEncoder;
import arman.image.png.PngEncoder;

public class ImageScanner {

    //MainActivity activity;
    EncoderCallback callback;
    private String photoPath = InfoCode.SD_CARD + "/DCIM/Scanner";
    private File photoDir = new File(photoPath);
    FileUtil fileUtil = new FileUtil();
    Image image;
    int format, blockSize = 51, subsConst = 18;
    private static class Format {
        static int GRAYSCALE_PNG = 1;
        static int JPG2_GRAYSCALE_PNG = 11;
        static int JPG2_OpenCV_PNG = 21;
        static int GRAYSCALE_GIF = 31;
    }
    public ImageScanner(/*MainActivity activity, */EncoderCallback callback){
        //this.activity = activity;
        this.callback = callback;
    }

    public void scanPng(Image image){
        doInBackground(image, Format.GRAYSCALE_PNG);
    }
    public void scanJpgToPng(Image image){
        doInBackground(image, Format.JPG2_GRAYSCALE_PNG);
    }
    public void scanJpgToOpenCVPng(Image image, int blockSize, int subsConst){
        this.blockSize = blockSize;
        this.subsConst = subsConst;
        doInBackground(image, Format.JPG2_OpenCV_PNG);
    }
    public void scanGif(Image image){
        doInBackground(image, Format.GRAYSCALE_GIF);
    }
    private void doInBackground(Image image, int format){
        this.image = image;
        this.format = format;
        Thread thread = new Thread(this::scanImage);
        thread.start();
    }
    private void scanImage(){
        int width = image.getWidth();
        int height = image.getHeight();

        Image.Plane luminance = image.getPlanes()[0];
        ByteBuffer buffer = luminance.getBuffer();
        byte[] imageData = new byte[buffer.capacity()];
        buffer.get(imageData);
        image.close();
        image = null;
        String ext = ".png";

        if (format < 10) { // PngEncoder
            PngEncoder encoder = new PngEncoder(height, width, imageData, false);
            if (format == Format.GRAYSCALE_PNG) imageData = encoder.encodeGrayscale();
        }
        else if (format < 20) { // JpgEncoder
            JpgEncoder encoder = new JpgEncoder(height, width, imageData);
            if (format == Format.JPG2_GRAYSCALE_PNG) imageData = encoder.encodeGrayscale();
        }
        else if (format < 30) { // OpenCVEncoder
            /*String filePath = photoPath + "/p" + System.currentTimeMillis() + ext;
            File outputFile = null;
            OpenCVEncoder encoder = new OpenCVEncoder(width, height, imageData);
            if (format == Format.JPG2_OpenCV_PNG){
                outputFile = encoder.encodeGrayscale(filePath);
                if (outputFile.exists()) callback.onEncodeFinished(outputFile, "Saved");
                else callback.onError("Error decoding image data");
            }
            return;*/

            OpenCVEncoder encoder = new OpenCVEncoder(width, height, imageData, blockSize, subsConst);
            if (format == Format.JPG2_OpenCV_PNG) imageData = encoder.encodeGrayscale();

        }

        else if (format < 40){ // GifEncoder
            ext = ".gif";
            GifEncoder encoder = new GifEncoder(width, height, imageData);
            if (format == Format.GRAYSCALE_GIF) imageData = encoder.encodeGrayscale();
        }
        save(ext, imageData);
    }

    private void save(String ext, byte[] bytes) {
        if (bytes == null) {
            callback.onError("Encoding Failed");
            return;
        }
        final File file = new File(photoPath + "/p" + System.currentTimeMillis() + ext);
        boolean success = fileUtil.saveFile(file, bytes);
        //image.close();
        if (success) callback.onEncodeFinished(file, "Saved");
        else callback.onError("Failed to Save");
    }

}
