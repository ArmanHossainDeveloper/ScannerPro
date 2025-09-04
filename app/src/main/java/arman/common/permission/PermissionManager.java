package arman.common.permission;

import android.app.Activity;
import android.content.Intent;

import arman.common.infocodes.InfoCode;

public class PermissionManager {
    PermissionWarehouse warehouse;
    public PermissionManager(Activity activity){warehouse = new PermissionWarehouse(activity);}

    public boolean isGranted(int requestCode, String[] permissions, int[] grantResults) {return warehouse.isGranted(requestCode, permissions, grantResults);}
    public int getPermissionResult(int requestCode, int resultCode, Intent data) {return warehouse.getPermissionResult(requestCode, resultCode, data);}

    public boolean hasRequiredPermissions(int[] permissions, boolean request) {
        if (permissions == null || permissions.length == 0) return true;
        for (int permission : permissions){
            if (!checkPermission(permission, request)) return false;
        }
        return true;
    }

    private boolean checkPermission(int permission, boolean request) {
        switch (permission) {
            case InfoCode.NO_PERMISSION_REQUIRED: return true;
            case InfoCode.FILE_PERMISSION: return warehouse.hasFilePermission(request);
            case InfoCode.CAMERA_PERMISSION: return warehouse.hasCameraPermission(request);
            case InfoCode.MICROPHONE_PERMISSION: return warehouse.hasMicrophonePermission(request);
            case InfoCode.NOTIFICATION_PERMISSION: return warehouse.hasNotificationPermission(request);
            case InfoCode.FOREGROUND_PERMISSION: return warehouse.hasForegroundPermission(request);
        }
        return false;
    }

    /*
    boolean hasFilePermission(boolean request) {return warehouse.hasFilePermission(request);}
    boolean hasCameraPermission(boolean request) {return warehouse.hasCameraPermission(request);}
    boolean hasMicrophonePermission(boolean request) {return warehouse.hasMicrophonePermission(request);}
    boolean hasNotificationPermission(boolean request) {return warehouse.hasNotificationPermission(request);}
    */

     /*
    boolean hasScannerPermission(boolean request) {
        boolean file = hasFilePermission(request);
        boolean camera = hasCameraPermission(request);
        return file && camera;
    }
    boolean hasVoiceRecPermission(boolean request) {
        boolean file = hasFilePermission(request);
        boolean microphone = hasMicrophonePermission(request);
        return file && microphone;
    }
    boolean hasVideoRecPermission(boolean request) {
        boolean file = hasFilePermission(request);
        boolean camera = hasCameraPermission(request);
        boolean microphone = hasMicrophonePermission(request);
        return file && camera && microphone;
    }
    */
}
