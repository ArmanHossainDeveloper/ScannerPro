package arman.image.filter;

public class PixelArrayCalculator {


    public byte[] getGrayscalePixels(int[] rgbPixels, int width, int height) {
        byte[] grayscalePixels = new byte[width * height];
        int pixIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grayscalePixels[pixIndex] = getGrayPixel(rgbPixels[pixIndex]);
                pixIndex++;
            }
        }
        return grayscalePixels;
    }

    public byte getGrayPixel(int rgbPixel) {
        int r = (rgbPixel >> 16) & 0xFF;
        int g = (rgbPixel >> 8) & 0xFF;
        int b = rgbPixel & 0xFF;
        return (byte) (0.3 * r + 0.59 * g + 0.11 * b);
    }

    public byte[][] getGrayscaleScanlines(byte[] grayscalePixels, int width, int height) {
        byte[][] scanlines = new byte[height][width];
        /*int index = 0;
        for (int i = 0; i < height; i++){
            for (int j = 1; j <= width; j++){
                scanlines[i][j] = bytes[index++];
            }
        }*/
        for (int i = 0; i < height; i++) {
            System.arraycopy(grayscalePixels, width * i, scanlines[i], 0, width);
        }
        return scanlines;
    }

    public int[][] getGrayscaleIntegral(byte[] grayscalePixels, int width, int height) {
        int[][] integralImage = new int[height][width];

        // Compute the first row
        for (int x = 0; x < width; x++) {
            integralImage[0][x] = (grayscalePixels[x] & 0xFF);
        }

        // Compute the first column
        for (int y = 1; y < height; y++) {
            integralImage[y][0] = (grayscalePixels[y * width] & 0xFF);
            for (int x = 1; x < width; x++) {
                int pixelValue = (grayscalePixels[y * width + x] & 0xFF);
                integralImage[y][x] = pixelValue + integralImage[y - 1][x] + integralImage[y][x - 1] - integralImage[y - 1][x - 1];
            }
        }

        return integralImage;
    }

    public int calculateLocalThreshold(int[][] integralImage, int width, int height, int x, int y, int maskRadius) {
        int x0 = Math.max(0, x - maskRadius);
        int y0 = Math.max(0, y - maskRadius);
        int x1 = Math.min(width - 1, x + maskRadius);
        int y1 = Math.min(height - 1, y + maskRadius);

        int sum = integralImage[y1][x1];
        if (y0 > 0) sum -= integralImage[y0 - 1][x1];
        if (x0 > 0) sum -= integralImage[y1][x0 - 1];
        if (x0 > 0 && y0 > 0) sum += integralImage[y0 - 1][x0 - 1];

        int count = (x1 - x0 + 1) * (y1 - y0 + 1);
        int average = sum / count;
        return average;
    }

}
