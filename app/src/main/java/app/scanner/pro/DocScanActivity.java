package app.scanner.pro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;
import arman.common.ui.widget.PhotoView;
import arman.image.EncoderCallback;
import arman.image.opencv.OpenCVEncoder;
import arman.image.png.PngEncoder;


public class DocScanActivity extends DrawerActivity {

    private String photoPath = InfoCode.SD_CARD + "/DCIM/Scanner";
    private File photoDir = new File(photoPath);

    //ImageView previewIV;
    PhotoView previewPV;
    Button previewBtn, saveBtn;

    //TextView blockSizeTV, subsConstTV;
    NumberPicker blockSizePicker, constPicker;
    int blockSizeCount = 101;
    String[] availableBlockSize = new String[blockSizeCount];



    int width, height, blockSize = 51, subsConst = 18;

    boolean isSingle = true, isSaveButtonClick;

    PngEncoder pngEncoder;
    OpenCVEncoder encoder;
    Mat mat = null;

    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.FILE_PERMISSION};
    }

    @Override
    protected void onCreate() {
        if (!OpenCVLoader.initDebug()) toast("Failed to Initialize OpenCV");
        setContentView(R.layout.activity_docscan);
        initialize();
        handleIntent();
    }

    @Override
    protected void onSettingsChange() {

    }

    private void handleIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action == null) {
            toast("Error from Action");
            return;
        }

        if(action.contains("action.SEND_MULTIPLE")) handleSendMultipleImages(intent);
        else if(action.contains("action.SEND")){
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (handleSingleImagesUri(uri)) preview();
        }

    }

    private void initialize() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!photoDir.exists()) photoDir.mkdir();

        setupPreview();

        //blockSizeTV = find(R.id.block_sizes_tv);
        //subsConstTV = find(R.id.consts_tv);
        previewBtn = find(R.id.preview_btn);
        saveBtn = find(R.id.save_btn);

        initBlockSizePicker();
        initConstPicker();

        EncoderCallback encoderCallback = new EncoderCallback() {
            public void onEncodeFinished(File file, String message) {
                runOnUiThread(()-> {
                    enable(previewBtn);
                    updateMediaStore(file);
                });
            }
            public void onError(String message) {
                runOnUiThread(()-> {
                    enable(previewBtn);
                    toast(message);
                });
            }
        };

        previewBtn.setOnClickListener(v -> {
            blockSize = getBlockSize();
            subsConst = constPicker.getValue();
            preview();

        });
        saveBtn.setOnClickListener(v -> onClickSaveBtn());


    }



    private void setupPreview(){
        //previewIV = find(R.id.scan_preview);
        previewPV = find(R.id.scan_preview);

        /*int width = getScreenWidth(); // textureView.getWidth();
        int height = width * 4 / 3;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        previewPV.setLayoutParams(params);
        */

    }



    private void initBlockSizePicker() {
        blockSizePicker = find(R.id.block_size_picker);
        int min = 3;
        for (int i = 0; i < blockSizeCount; i++) {
            availableBlockSize[i] = String.valueOf(min + (i * 2));
        }

        blockSizePicker.setMinValue(0);
        blockSizePicker.setMaxValue(blockSizeCount - 1);
        blockSizePicker.setDisplayedValues(availableBlockSize);

        blockSizePicker.setValue(24);
        blockSizePicker.setWrapSelectorWheel(false);

    }

    private void initConstPicker() {
        constPicker = find(R.id.const_picker);
        constPicker.setMinValue(1);
        constPicker.setMaxValue(50);
        constPicker.setValue(18);
        constPicker.setWrapSelectorWheel(false);
    }

    int getBlockSize(){
        int i = blockSizePicker.getValue();
        return Integer.parseInt(availableBlockSize[i]);
    }


    private int getScreenWidth() {
        WindowManager wm = getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }



    private void preview(){
        mat = encoder.adaptiveThreshold(blockSize, subsConst);
        this.width = mat.width();
        this.height = mat.height();
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        //previewPV.setImageBitmap(bitmap);
        //if (width * height < 16000000) previewPV.setImageBitmap(bitmap);
        previewPV.setBitmap(bitmap);

        //else toast("Cannot preview, Image too large!");
    }
    public static int getOdd(int num) {
        // Sets the least significant bit to ensure the number is odd
        return num | 1;
    }

    private void onClickSaveBtn() {
        isSaveButtonClick = true;
        save();
        isSaveButtonClick = false;
    }

    private void save(){
        if (mat == null) {
            toast("Null");
            return;
        }


        byte[] pixels = new byte[width * height];
        mat.get(0, 0, pixels);


        PngEncoder encoder = new PngEncoder(width, height, pixels, false);
        save(encoder.encodeBinary());
    }


    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }

    private void save(byte[] bytes){
        final File file = new File(photoPath + "/p" + System.currentTimeMillis() + ".png");
        try (OutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
            if (isSaveButtonClick) toast("Saved");
            updateMediaStore(file);
        } catch (IOException e) {
            toast("Failed");
        }

    }

    public void updateMediaStore(File file){
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
        .setData(Uri.fromFile(file)));
    }

    private boolean handleSingleImagesUri(Uri uri) {
        try(InputStream is = getContentResolver().openInputStream(uri)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = is.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
            byte[] bytes = os.toByteArray();
            encoder = new OpenCVEncoder(bytes);
            return true;
        }
        catch (Exception ignored) {}
        return false;

    }

    private void handleSendMultipleImages(Intent intent) {
        isSingle = false;
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        if (imageUris != null) {
            for (Uri uri : imageUris) {
                if (handleSingleImagesUri(uri)) {
                    preview();
                    save();
                }
            }
            toast("Total Images: " + imageUris.size());

        } else toast("UriArray is Null");
    }

}

