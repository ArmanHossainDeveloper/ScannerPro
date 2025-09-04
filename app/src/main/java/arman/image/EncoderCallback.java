package arman.image;

import java.io.File;

public interface EncoderCallback {
    void onEncodeFinished(File file, String message);
    void onError(String message);
}
