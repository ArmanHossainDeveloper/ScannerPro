package arman.image.opencv;

import static org.opencv.imgcodecs.Imgcodecs.imdecode;
import static org.opencv.imgcodecs.Imgcodecs.imread;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import arman.common.infocodes.InfoCode;
import arman.image.png.PngEncoder;

public class OpenCVEncoder {



    //HashMap<String, byte[]> hashMap = new HashMap<>();
    int width, height, channelPerPixel = 1, blockSize = 51, subsConst = 18;;
    byte[] imageData;
    Mat srcMat;
    boolean isError = false;

    public OpenCVEncoder(int width, int height, byte[] imageData, int blockSize, int subsConst){
        this.blockSize = blockSize;
        this.subsConst = subsConst;
        this.width = width;
        this.height = height;
        /*this.width = height;
        this.height = width;*/
        this.imageData = imageData;
    }

    public OpenCVEncoder(byte[] imageData){
        srcMat = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_GRAYSCALE);
    }

    public Mat adaptiveThreshold(int blockSize, int subsConst){
        Mat thresholdedMat = new Mat();
        Imgproc.adaptiveThreshold(srcMat, thresholdedMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, subsConst);
        return thresholdedMat;
    }

    public byte[] encodeGrayscale(/*String filePath*/){

        Mat mat = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_GRAYSCALE);
        if (mat.empty()) {
            InfoCode.log("Error decoding image data");
            return null;
        }
        Mat thresholdedMat = new Mat();
        Imgproc.adaptiveThreshold(mat, thresholdedMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, subsConst);

        /*Imgcodecs.imwrite(filePath, thresholdedMat);
        return new File(filePath);*/

        /*Mat binaryMat = new Mat();
        thresholdedMat.convertTo(binaryMat, CvType.CV_8UC1);
        Core.randu(binaryMat, 0, 2);
        MatOfInt parameteres = new MatOfInt(Imgcodecs.IMWRITE_PNG_BILEVEL, 1);
        Imgcodecs.imwrite(filePath, binaryMat, parameteres);
        return new File(filePath);*/

        byte[] pixels = new byte[width * height];
        thresholdedMat.get(0, 0, pixels);


        PngEncoder encoder = new PngEncoder(height, width, pixels, false);

        return encoder.encodeBinary();
    }





}
