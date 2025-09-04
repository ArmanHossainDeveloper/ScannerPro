package app.scanner.pro;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;

public class MainActivity extends DrawerActivity {

    TextView logTV;
    Button cameraScanBtn;


    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.FILE_PERMISSION, InfoCode.CAMERA_PERMISSION};
    }
    @Override
    protected void onCreate() {
        setContentView(R.layout.activity_main);
        //setRightDrawer(R.layout.drawer_frame, R.layout.menu_drawer);
        if(!OpenCVLoader.initDebug()) toast("Failed to Load OpenCV");
        //else toast("Successfully Loaded OpenCV");
        initialize();
        onSettingsChange();
    }

    private void initialize() {
        //logTV = find(R.id.log_tv);
        cameraScanBtn = find(R.id.camera_scan_btn);

    }



    public void openCameraScanner() {
        Intent i = new Intent(MainActivity.this, CameraScanActivity.class);
        startActivity(i);
    }
    @Override
    protected void onSettingsChange() {
        //enableSwipers(true);
        //enableSwipers(preference.getBoolean("enableSwipers", true));
    }

    @Override
    public void onRotate(boolean landscape) {
        if (landscape) toast("Landscape");
        else toast("Portrait");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    void start(Class cls){
        startActivity(new Intent(this, cls));
    }

    public void cameraScan(View v){
        start(CameraScanActivity.class);
    }

    public void viewImages(View v){
        start(ImageViewer.class);
    }


    public void scanDocs(View v){
        start(DocScanActivity.class);
    }
    public void exit(View v){
        exit();
    }


}













