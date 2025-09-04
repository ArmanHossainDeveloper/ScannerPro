package arman.image.jpg;

import android.graphics.Bitmap;
import android.media.Image;

import java.nio.ByteBuffer;

import arman.common.io.FileUtil;
import arman.image.filter.PixelArrayCalculator;
import arman.image.png.PngEncoder;

public class JpgEncoder {


    PixelArrayCalculator pixACalc = new PixelArrayCalculator();
    //HashMap<String, byte[]> hashMap = new HashMap<>();
    int width, height, channelPerPixel = 1;
    byte[] jpgData;
    boolean isError = false;

    int maskRadius = 25, constant = -10;

    public JpgEncoder(int width, int height, byte[] jpgData){
        this.width = width;
        this.height = height;
        /*this.width = height;
        this.height = width;*/
        this.jpgData = jpgData;


    }

    public byte[] encodeGrayscale(){
        byte[] binaryPixels = getBinaryPixels();

        PngEncoder pngEncoder = new PngEncoder(width, height, binaryPixels, true);
        return pngEncoder.encodeGrayscale();
    }



    private byte[] getBinaryPixels() {
        byte[] grayscalePixels = getGrayscalePixels();
        int[][] integralImage = pixACalc.getGrayscaleIntegral(grayscalePixels, width, height);

        int pixIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int threshold = pixACalc.calculateLocalThreshold(integralImage, width, height, x, y, maskRadius);
                int pixelValue = grayscalePixels[pixIndex] & 0xFF;
                grayscalePixels[pixIndex++] = (byte) (pixelValue - constant < threshold ? 0 : 255);
            }
        }

        return grayscalePixels;
    }

    private boolean[] getBooleanPixels(){
        byte[] grayscaleScanlines = getGrayscalePixels();
        int[][] integralImage = pixACalc.getGrayscaleIntegral(grayscaleScanlines, width, height);
        boolean[] binaryPixels = new boolean[width * height];


        int pixIndex = 0;
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++) {

                int threshold = pixACalc.calculateLocalThreshold(integralImage, width, height, x, y, maskRadius);
                int pixelValue = (grayscaleScanlines[pixIndex] & 0xFF);

                binaryPixels[pixIndex++] = pixelValue - constant < threshold;
            }
        }

        return binaryPixels;
    }

    private byte[][] getGrayscaleScanlines(){
        Bitmap bitmap = new FileUtil().getBitmap(jpgData);
        jpgData = null;
        byte[][] grayscaleScanlines = new byte[height][width];

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++) {
                grayscaleScanlines[y][x] = pixACalc.getGrayPixel(bitmap.getPixel(x, y));
            }
        }
        bitmap.recycle();

        return grayscaleScanlines;
    }

    private byte[] getGrayscalePixels(){
        Bitmap bitmap = new FileUtil().getBitmap(jpgData);
        jpgData = null;
        int len = height * width;
        byte[] grayscalePixels = new byte[len];

        PixelArrayCalculator pixACalc = new PixelArrayCalculator();

        int pixIndex = 0;
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++) {
                grayscalePixels[pixIndex++] = pixACalc.getGrayPixel(bitmap.getPixel(x, y));
            }
        }
        bitmap.recycle();
        return grayscalePixels;
    }

    /*public byte[] encodeGrayscale(){
        int totalSegment = 16;
        int segmentHeight = height / totalSegment;
        int segmentedLength = width * segmentHeight;
        int pixelLength = width * height;
        Bitmap bitmap = new FileUtil().getBitmap(jpgData);

        ByteBuffer buffer = ByteBuffer.allocate(pixelLength);
        PixelArrayCalculator pixACalc = new PixelArrayCalculator();
        byte[] grayscalePixels = new byte[pixelLength];
        for (int i = 0; i < height; i++){
            int[] rgbPixels = new int[width];
            bitmap.getPixels(rgbPixels, 0, width, 0, i, width, 1);
            buffer.put(pixACalc.getGrayscalePixels(rgbPixels, width, segmentHeight));
        }

        bitmap.recycle();

        PngEncoder pngEncoder = new PngEncoder(width, height, buffer.array(), true);

        return pngEncoder.encodeGrayscale();
    }*/

    /*public byte[] encodeGrayscale(){
        Bitmap bitmap = new FileUtil().getBitmap(jpgData);
        int[] rgbPixels = new int[width * height];
        bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height);
        image.close();
        image = null;

        byte[]
        return new PixelArrayCalculator().getGrayscalePixels(rgbPixels, width, height);
    }*/




}
