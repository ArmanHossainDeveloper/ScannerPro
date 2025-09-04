package arman.common.io;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ContentUriConverter {
    String sdCard = "/sdcard";
    public String getAbsoluteFilePath(Uri uri){
        if (uri == null || !"content".equals(uri.getScheme())) return "";

        String contentPath = uri.getPath();
        if (contentPath == null) return "";
        int checkPoint = 0;
        //String path = contentPath;
        while (checkPoint != -1){
            String path = contentPath.substring(checkPoint);
            Log.e("getAbsoluteFilePath", "Path: " + path);
            java.io.File file = new java.io.File(sdCard + path);
            if (file.exists()){
                Log.e("getAbsoluteFilePath", "Found path: " + path);
                return path;
            }

            checkPoint = contentPath.indexOf("/", ++checkPoint);
        }
        return "";
    }
}
