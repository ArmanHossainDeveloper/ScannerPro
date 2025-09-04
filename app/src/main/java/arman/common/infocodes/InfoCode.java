package arman.common.infocodes;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class InfoCode {
	public static final int SETTINGS_UPDATE = 120021;
	public static final int NO_PERMISSION_REQUIRED = 0;
	public static final int FILE_PERMISSION = 123101;
	public static final int CAMERA_PERMISSION = 123102;
	public static final int MICROPHONE_PERMISSION = 123103;
	public static final int NOTIFICATION_PERMISSION = 123104;
	public static final int FOREGROUND_PERMISSION = 123105;
	public static final int SCANNER_PERMISSION = 123201;
	public static final int VOICEREC_PERMISSION = 123202;
	public static final int VIDEOREC_PERMISSION = 123301;
	public static final int PERMISSION_GRANTED = 123111;
	public static final int PERMISSION_DENIED = 123000;


    public static final int sdk = Build.VERSION.SDK_INT;
	public static final String SD_CARD = Environment.getExternalStorageDirectory().toString();

	public static enum ORIENTATION {
		Landscape,
		Portrait
	}
    public static void log(String s) {Log.e("DebugLog", s);}
}
