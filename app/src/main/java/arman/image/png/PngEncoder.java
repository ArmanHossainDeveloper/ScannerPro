package arman.image.png;

import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import arman.common.infocodes.InfoCode;
import arman.image.CRC;
import arman.image.filter.PixelArrayCalculator;

public class PngEncoder {

    //HashMap<String, byte[]> hashMap = new HashMap<>();
    PixelArrayCalculator pixAcalc = new PixelArrayCalculator();
    int width, height, channelPerPixel = 1;
    byte[] pixels;
    boolean useFilter = false, isError = false, isJpg = false, isBinary = false;
    byte[] pngSignature = {-119, 80, 78, 71, 13, 10, 26, 10}, ihdrType = {73, 72, 68, 82}, idatType = {73, 68, 65, 84};
    byte[] iendChunk = {0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};

    public PngEncoder(int width, int height, byte[] pixels, boolean isJpg){
        this.width = width;
        this.height = height;
        /*this.width = height;
        this.height = width;*/
        this.pixels = pixels;
        this.isJpg = isJpg;
    }

    public byte[] encodeGrayscale(){
        channelPerPixel = 1;
        useFilter = false;
        return createPngImageData();
        //Thread backgroundThread =  new Thread(this::createPngImageData);
        //backgroundThread.start();
    }

    public byte[] encodeBinary(){
        isBinary = true;
        return createBinaryPngData();
    }

    byte[] createBinaryPngData(){
        byte[] idatChunkData = getCompressedPixelData();
		final int idatDataLength = idatChunkData.length;
        ByteBuffer buffer = ByteBuffer.allocate(57 + idatDataLength);   // 57 = PNGSignature(8 bytes) + IHDRChunk(25 bytes) + IDATChunk(12 bytes) + IENDChunk(12 bytes)
        buffer.put(pngSignature);
        buffer.put(getIHDRChunk());
        buffer.putInt(idatDataLength);
        buffer.put(idatType);
        buffer.put(idatChunkData);
        buffer.putInt(getChecksum(idatType, idatChunkData));
        buffer.put(iendChunk);

        //InfoCode.log(Arrays.toString(bytes));

        //if (!isError) callback.onEncodeFinished("Successful");
        return buffer.array();
    }

    byte[] createPngImageData(){
        //isError = false;

        //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //DataOutputStream dos = new DataOutputStream(outputStream);
        //dos.writeInt(12);

        byte[] idatChunkData = getCompressedPixelData();

		final int idatDataLength = idatChunkData.length;
        ByteBuffer buffer = ByteBuffer.allocate(57 + idatDataLength);   // 57 = PNGSignature(8 bytes) + IHDRChunk(25 bytes) + IDATChunk(12 bytes) + IENDChunk(12 bytes)
        buffer.put(pngSignature);
        buffer.put(getIHDRChunk());
        buffer.putInt(idatDataLength);
        buffer.put(idatType);
        buffer.put(idatChunkData);
        buffer.putInt(getChecksum(idatType, idatChunkData));
        buffer.put(iendChunk);

        //InfoCode.log(Arrays.toString(bytes));

        //if (!isError) callback.onEncodeFinished("Successful");
        return buffer.array();
    }
    byte[] createPngImageDataNewMethod(){
        //isError = false;

        //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //DataOutputStream dos = new DataOutputStream(outputStream);
        //dos.writeInt(12);

        //byte[] idatChunkData = getCompressedPixelData();

		final byte[] idatRawData = getIdatTypeAndData();
		final int idatDataLength = idatRawData.length - 4;
		final int crc = (int) CRC.crc(idatRawData);
		final byte[] idatLengthBytes = fromInt(idatDataLength);
		final byte[] idatCrcBytes = fromInt(crc);
        ByteBuffer buffer = ByteBuffer.allocate(57 + idatDataLength);   // 57 = PNGSignature(8 bytes) + IHDRChunk(25 bytes) + IDATChunk(12 bytes) + IENDChunk(12 bytes)
        buffer.put(pngSignature);
        buffer.put(getIHDRChunk());
        buffer.put(idatLengthBytes);
        buffer.put(idatRawData);
        buffer.put(idatCrcBytes);
        buffer.put(iendChunk);

        //InfoCode.log(Arrays.toString(bytes));

        //if (!isError)
        //callback.onEncodeFinished("Successful");
        return buffer.array();
    }

    byte[] getIHDRChunk() {
        byte[] ihdrChunkData = getHeaderData();
        ByteBuffer buffer = ByteBuffer.allocate(25);
        buffer.putInt(13);
        buffer.put(ihdrType);
        buffer.put(ihdrChunkData);
        buffer.putInt(getChecksum(ihdrType, ihdrChunkData));
        return buffer.array();
    }

    byte[] getHeaderData(){
        //todo: create HeaderByteArray using the params
        ByteBuffer buffer = ByteBuffer.allocate(13);
        int residue = width % 8;
        int fillUp = 8 - residue;
        boolean isResidue = residue > 0;
        buffer.putInt(isResidue ? width + fillUp : width);
        buffer.putInt(height);

        // bit depth
        if (isBinary) buffer.put((byte) 1); // bit depth 1
        else buffer.put((byte) 8); // bits per channel. max is 16 bits. although 10 bit is overkill.

        buffer.put((byte) 0); // color type: grayscale(0)
        buffer.put(new byte[]{0, 0, 0}); //methods {compression, filter, interlace}
        return buffer.array();
    }

    byte[] getIdatTypeAndData(){
		final byte[] rawLines = getCompressibleByteArray();
		final Deflater deflater = new Deflater();

		deflater.setInput(rawLines);
		deflater.finish();

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(idatType, 0, 4);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = deflater.deflate(buffer)) != 0) {
			baos.write(buffer, 0, bytesRead);
		}

        return baos.toByteArray();
	}

    byte[] getCompressedPixelData(){
        byte[] byteArray = getCompressibleByteArray();
        ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
        try(DeflaterOutputStream dos = new DeflaterOutputStream(outputStream, new Deflater())) {
            dos.write(byteArray);
        }
        catch (IOException e) {
            isError = true;
            return null;
        }

        return outputStream.toByteArray();
    }


    int getChecksum(byte[] chunkType, byte[] chunkData){
        CRC32 crc32 = new CRC32();
        crc32.update(chunkType);
        crc32.update(chunkData);
        return (int) crc32.getValue();
    }

    byte[] getCompressibleByteArray() {
        if (isBinary) return getBinaryByteArray();
        if (useFilter) return getFilteredByteArray();
        int byteLength = pixels.length + height;
        byte[] byteArray = new byte[byteLength];

        if (isJpg){
            int scanlineWidth = width + 1;
            for (int i = 0; i < height; i++) {
                System.arraycopy(pixels, width * i, byteArray, scanlineWidth * i + 1, width);
            }
        }
        else {
            int pixIndx = 0;
            for (int i = width; i > 0; i--) {
                for (int j = 0; j < height; j++) {
                    byteArray[i + j * (width + 1)] = pixels[pixIndx++];
                }
            }
        }

        return byteArray;

    }


    byte[] getBinaryByteArray() {
        int residueBits = width % 8;
        int fillUpBits = 8 - residueBits;
        boolean isResidue = residueBits > 0;
        int availableBytePerRow = (width - residueBits) / 8;
        int bytePerRow = isResidue ? availableBytePerRow + 1 : availableBytePerRow;

        int pixelLength =  bytePerRow * height + height;
        byte[] byteArray = new byte[pixelLength];


        int pixIndx = 0;
        int bytIndx = 1; //0th byte is for filter. so we skip it.

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < availableBytePerRow; x++) {
                for (int z = 7; z >= 0; z--) {
                    if (pixels[pixIndx++] != 0) {
                        byteArray[bytIndx] |= (1 << z);
                    }
                }
                bytIndx++;
            }
            if (isResidue){
                // bitwise operation for last byte
                int z = 7;
                while (z >= fillUpBits) {
                    if (pixels[pixIndx++] != 0) {
                        byteArray[bytIndx] |= (1 << z);
                    }
                    z--;
                }
                while (z >= 0) {
                    // fill up rest with white
                    byteArray[bytIndx] |= (1 << z);
                    z--;
                }
                bytIndx++;

            }
            bytIndx++; // skipping filter byte.
        }


        return byteArray;

    }


    byte[] getBinaryByteArray1() {
        int bytePerRow = width / 8;
        int pixelLength =  bytePerRow * height + height;
        byte[] byteArray = new byte[pixelLength];


        int pixIndx = 0;
        int bytIndx = 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < bytePerRow; x++) {
                for (int z = 7; z >= 0; z--) {
                    if (pixels[pixIndx++] != 0) {
                        byteArray[bytIndx] |= (1 << z);
                    }
                }
                bytIndx++;
            }
            bytIndx++;
        }


        return byteArray;

    }
    byte[] getBinaryByteArray2() {
        int bytePerRow = width / 8;
        int pixelLength =  bytePerRow * height + height;
        byte[] byteArray = new byte[pixelLength];


        int pixIndx = 0;
        int bytIndx = 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < bytePerRow; x++) {
                for (int z = 7; z >= 0; z--) {
                    if (pixels[pixIndx++] != 0) {
                        byteArray[bytIndx] |= (1 << z);
                    }
                }
                bytIndx++;
            }
            bytIndx++;
        }


        return byteArray;

    }

    byte[] getCompressibleByteArray2() { // if image builder had rotation 90 degree
        int byteLength = pixels.length + height;
        byte[] byteArray = new byte[byteLength];

        int pixIndx = 0;
        for (int i = width; i > 0; i--){
            for (int j = 0; j < height; j++){
                byteArray[i + j * (width + 1)] = pixels[pixIndx++];
            }
        }

        return byteArray;
    }

    byte[] getCompressibleByteArrayTest() {
        if (useFilter) return getFilteredByteArray();

        InfoCode.log("pixelLength: " + pixels.length);
        int byteLength = pixels.length + height;
        InfoCode.log("byteLength: " + byteLength);
        ByteBuffer buffer = ByteBuffer.allocate(byteLength);
        InfoCode.log("bufferLength: " + buffer.array().length);
        //byte[] byteArray = new byte[byteLength];
/*
        int pixIndx = 0;
        for (int i = 0; i < height; i++){
            buffer.put((byte) 0);
            for (int j = 0; j < width; j++){
                buffer.put(pixelBytes[pixIndx++]);
                //byteArray[i++] = pixelBytes[pixIndx++];
            }
        }*/

        return buffer.array();
        /*
        int bytIndx = 0;
        for (int i = 0; i < height; i++){
            bytIndx++;
            for (int j = 0; j < width; j++){
                byteArray[bytIndx++] = pixelBytes[pixIndx++];
            }
        }*/

        //return byteArray;
    }

    byte[][] getScanlines(){
        return pixAcalc.getGrayscaleScanlines(pixels, width, height);
    }
    byte[] getFilteredByteArray(){
        /* 4 Filter types are: Sub = 1, Up = 2, Average = 3, Paeth = 4
        For Grayscale and 256-Indexed color-palate, Filter type: None is Best.*/

        byte[][] scanlines = getScanlines();

        /*
        Different Filter is applied per line basis.
        Learn more about how filtering works.
        */

        //for now just return null
        return null;
    }

	public static final byte[] fromInt(final int value) {
		final byte[] out = new byte[4];

		out[0] = (byte) ((value >>> 24) & 0xFF);
		out[1] = (byte) ((value >>> 16) & 0xFF);
		out[2] = (byte) ((value >>> 8) & 0xFF);
		out[3] = (byte) (value & 0xFF);

		return out;
	}
}
