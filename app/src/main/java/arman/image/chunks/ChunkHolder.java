package arman.image.chunks;

import java.util.zip.CRC32;

public class ChunkHolder {

    public int dataLength;
    public byte[] chunkType;
    public byte[] chunkData;
    public int checksum;
    public ChunkHolder(byte[] type, byte[] data){
        dataLength = data.length;
        chunkType = type;
        chunkData = data;
    }

}
