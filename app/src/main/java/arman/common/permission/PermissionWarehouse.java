package arman.common.permission;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import java.util.ArrayList;

import arman.common.infocodes.InfoCode;

class PermissionWarehouse {


    private Activity activity;

    PermissionWarehouse(Activity activity){
        this.activity = activity;
    }

    boolean hasFilePermission(boolean request) {
        if (InfoCode.sdk < 30){
            String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (activity.checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED) return true;
            else if (!request) return false;

            activity.requestPermissions(permission, InfoCode.FILE_PERMISSION);
            return false;
        }

        if (Environment.isExternalStorageManager()) return true;
        else if (!request) return false;

        Uri myPackage = Uri.parse("package:" + activity.getPackageName());
        try {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, myPackage);
            //log("1st try: " + myPackage.toString());
            activity.startActivityForResult(i, InfoCode.FILE_PERMISSION);
        }
        catch (ActivityNotFoundException anf) {
            try {
                Intent i = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, myPackage);
                //log("2nd try: " + myPackage.toString());
                activity.startActivityForResult(i, InfoCode.FILE_PERMISSION);
            }
            catch (Exception e) {InfoCode.log("fallback method Failed");}
        }
        catch (Exception e) { InfoCode.log("Intent Failed"); }

        return false;
    }

    boolean hasCameraPermission(boolean request) {
        String[] permission = {Manifest.permission.CAMERA};
        if (activity.checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else if (!request) return false;

        activity.requestPermissions(permission, InfoCode.CAMERA_PERMISSION);
        return false;
    }

    boolean hasMicrophonePermission(boolean request) {
        String[] permission = {Manifest.permission.RECORD_AUDIO};
        if (activity.checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else if (!request) return false;

        activity.requestPermissions(permission, InfoCode.MICROPHONE_PERMISSION);
        return false;
    }

    boolean hasNotificationPermission(boolean request) {
        if (InfoCode.sdk < 33) return true;
        String[] permission = {Manifest.permission.POST_NOTIFICATIONS};
        if (activity.checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else if (!request) return false;

        activity.requestPermissions(permission, InfoCode.NOTIFICATION_PERMISSION);
        return false;
    }

    boolean hasForegroundPermission(boolean request) {
        if (InfoCode.sdk < 28) return true;
        String[] permission = {Manifest.permission.FOREGROUND_SERVICE};
        if (activity.checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else if (!request) return false;

        activity.requestPermissions(permission, InfoCode.FOREGROUND_PERMISSION);
        return false;
    }

    boolean isGranted(int requestCode, String[] permissions, int[] grantResults) {
        for (int result : grantResults){
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /*private boolean checkPermission(int requestCode){
        switch (requestCode){
            case InfoCode.FILE_PERMISSION: return hasFilePermission(false);
            //case InfoCode.CAMERA_PERMISSION: return hasCameraPermission(false);
        }
        return false;
    }*/
    int getPermissionResult(int requestCode, int resultCode, Intent data) {
        int result = -1;
        boolean wasRequested = true;
        boolean isGranted = false;//checkPermission(requestCode);

        switch (requestCode){
            case InfoCode.FILE_PERMISSION:
                isGranted = hasFilePermission(false);
                break;

            /*case InfoCode.CAMERA_PERMISSION:
                isGranted = hasCameraPermission(false);
                break; */

            default: wasRequested = false;
            /*
            case InfoCode.MICROPHONE_PERMISSION: break;
            case InfoCode.SCANNER_PERMISSION: break;
            case InfoCode.VOICEREC_PERMISSION: break;
            case InfoCode.VIDEOREC_PERMISSION: break;*/
        }

        if (wasRequested) result = isGranted ? InfoCode.PERMISSION_GRANTED : InfoCode.PERMISSION_DENIED;
        return result;
    }


    private String[] convertStringArrayListToStringArray(ArrayList<String> stringArrayList){
        String[] stringArray = new String[stringArrayList.size()];
        for (int i = 0; i < stringArrayList.size(); i++){
            stringArray[i] = stringArrayList.get(i);
        }
        return stringArray;
    }

}
