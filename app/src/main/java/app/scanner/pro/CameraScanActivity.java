package app.scanner.pro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;
import arman.image.EncoderCallback;
import arman.image.ImageScanner;


public class CameraScanActivity extends DrawerActivity {

    boolean trigger = false;
    private DelayedRunnable timedCapture;

    private String photoPath = InfoCode.SD_CARD + "/DCIM/Scanner";
    private File photoDir = new File(photoPath);


    private static final String TAG = "AndroidCameraApi";
    private CameraManager manager;
    TextureView textureView;
    LinearLayout popupMenu;
    TextView settingOverviewTV;
    Button captureBtn;


    //private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    /*static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }*/


    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    //protected CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;

    private ImageReader imageReader, reader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private List<Surface> outputSurfaces;
    private CaptureRequest.Builder builder;
    //Button pageBtn;

    int[] previewResolution = {2448, 3264};
    int[][] supportedResolutions = {previewResolution, {3456, 4608}, {6912, 9216}};
    float[] supportedZoomLevel = {1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f};
    int[] supportedExposureMillis = {40, 20, 10};
    int[] supportedImageFormat = {ImageFormat.JPEG, ImageFormat.YUV_420_888, ImageFormat.HEIC};

    int selectedResolution = 1, selectedZoom = 3, selectedExposure = 1, selectedImageFormat = 0, iso = 300, blockSize = 61, subsConst = 18;

    //PngEncoder pngEncoder;
    ImageScanner imageScanner;

    NumberPicker blockSizePicker, constPicker;
    int blockSizeCount = 101;
    String[] availableBlockSize = new String[blockSizeCount];



    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.FILE_PERMISSION, InfoCode.CAMERA_PERMISSION};
    }

    @Override
    protected void onCreate() {
        setContentView(R.layout.activity_camera);
        initialize();
    }

    @Override
    protected void onSettingsChange() {

    }

    private void applySettings(){
        blockSize = preference.getInt("blockSize", 61);
        subsConst = preference.getInt("subsConst", 18);
    }


    private void initialize() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (!photoDir.exists()) photoDir.mkdir();

        setupTextureView();

        setupResolutionSelector();
        setupZoomSelector();
        setupShutterSpeedSelector();
        setupImageFormatSelector();
        setupISOSelector();

        blockSize = preference.getInt("blockSize", 61);
        subsConst = preference.getInt("subsConst", 18);


        initBlockSizePicker();
        initConstPicker();

        /*setupBlockSizeSelector();
        setupSubsConstSelector();
*/


        EncoderCallback encoderCallback = new EncoderCallback() {
            public void onEncodeFinished(File file, String message) {
                runOnUiThread(()-> {
                    enable(captureBtn);
                    updateMediaStore(file);
                });
            }
            public void onError(String message) {
                runOnUiThread(()-> {
                    enable(captureBtn);
                    toast(message);
                });
            }
        };
        imageScanner = new ImageScanner(encoderCallback);

        popupMenu = find(R.id.popup_menu);
        settingOverviewTV = find(R.id.setting_overview_tv);

        String settingOverview = resolutionOptions[selectedResolution] + ", iso:" + iso + ", bs:" + blockSize + ", c:" + subsConst;
        settingOverviewTV.setText(settingOverview);


        captureBtn = find(R.id.capture_btn);
        timedCapture = new DelayedRunnable(this);


    }


    private void setupTextureView(){
        textureView = find(R.id.texture);
        int width = getScreenWidth(); // textureView.getWidth();
        int height = width * 4 / 3;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        textureView.setLayoutParams(params);
        textureView.setSurfaceTextureListener(textureListener);
    }



    Spinner resolutionSpinner;
    String[] resolutionOptions = {"8MP", "16MP", "64MP"};
    private void setupResolutionSelector(){
        resolutionSpinner = find(R.id.resolution_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(CameraScanActivity.this, android.R.layout.simple_spinner_item, resolutionOptions);
        resolutionSpinner.setAdapter(adapter);
        resolutionSpinner.setSelection(selectedResolution);
    }
    /*

    RadioGroup resolutionGroup;
    private void setupResolutionSelector2(){
        resolutionGroup = find(R.id.resolution_rg);
        resolutionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedResolution = group.indexOfChild(find(checkedId));
            try {
                setResolution(selectedResolution);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        });
    }*/


    Spinner imageFormatSpinner;
    String[] imageFormatOptions = {"JPEG", "YUV420", "HEIC"};
    private void setupImageFormatSelector(){
        imageFormatSpinner = find(R.id.image_format_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(CameraScanActivity.this, android.R.layout.simple_spinner_item, imageFormatOptions);
        imageFormatSpinner.setAdapter(adapter);
        imageFormatSpinner.setSelection(selectedImageFormat);
    }


    Spinner zoomSpinner;
    String[] zoomOptions = {"1.0x", "1.1x", "1.2x", "1.3x", "1.4x", "1.5x"};
    private void setupZoomSelector(){
        zoomSpinner = find(R.id.zoom_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(CameraScanActivity.this, android.R.layout.simple_spinner_item, zoomOptions);
        zoomSpinner.setAdapter(adapter);
        zoomSpinner.setSelection(selectedZoom);
    }


    Spinner shutterSpeedSpinner;
    String[] shutterSpeedOptions = {"1/25s", "1/50s", "1/100s"};
    private void setupShutterSpeedSelector(){
        shutterSpeedSpinner = find(R.id.shutter_speed_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(CameraScanActivity.this, android.R.layout.simple_spinner_item, shutterSpeedOptions);
        shutterSpeedSpinner.setAdapter(adapter);
        shutterSpeedSpinner.setSelection(selectedExposure);
    }


    TextView isoTV;
    SeekBar isoSeekbar;
    private void setupISOSelector(){
        isoTV = find(R.id.iso_tv);
        isoSeekbar = find(R.id.iso_seekbar);
        isoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress * 50 + 100;
                isoTV.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        isoSeekbar.setProgress((iso-100) / 50);
    }



    private void initBlockSizePicker() {
        blockSizePicker = find(R.id.cam_block_size_picker);
        int min = 3;
        for (int i = 0; i < blockSizeCount; i++) {
            availableBlockSize[i] = String.valueOf(min + (i * 2));
        }

        blockSizePicker.setMinValue(0);
        blockSizePicker.setMaxValue(blockSizeCount - 1);
        blockSizePicker.setDisplayedValues(availableBlockSize);

        blockSizePicker.setValue((blockSize - 3) / 2);
        blockSizePicker.setWrapSelectorWheel(false);

    }

    private void initConstPicker() {
        constPicker = find(R.id.cam_const_picker);
        constPicker.setMinValue(1);
        constPicker.setMaxValue(50);
        constPicker.setValue(subsConst);
        constPicker.setWrapSelectorWheel(false);
    }

    int getBlockSize(){
        int i = blockSizePicker.getValue();
        return Integer.parseInt(availableBlockSize[i]);
    }


    /*TextView blockSizeTV;
    SeekBar blockSizeSeekbar;
    private void setupBlockSizeSelector(){
        blockSizeTV = find(R.id.block_size_tv);
        blockSizeSeekbar = find(R.id.block_size_seekbar);
        blockSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress * 2 + 3;
                blockSizeTV.setText("" + progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        blockSizeSeekbar.setProgress((blockSize - 3) / 2);
    }
    */


    /*TextView subsConstTV;
    SeekBar subsConstSeekbar;
    private void setupSubsConstSelector(){
        subsConstTV = find(R.id.const_tv);
        subsConstSeekbar = find(R.id.const_seekbar);
        subsConstSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                subsConstTV.setText("" + progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        subsConstSeekbar.setProgress(subsConst);
    }
    */

    private int getScreenWidth1() {
        //WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager wm = getWindowManager();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        return 1;
    }
    private int getScreenWidth() {
        WindowManager wm = getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

/*
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){ finish(); }
        return trigger = trigger(keyCode);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(trigger){ takePicture(); }
        return trigger;
    }

    boolean trigger(int key){
        switch(key){ case 24: case 25: case 79: case 87: case 88: case 126: case 127: return true; }
        return false;
    }*/

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        public void onOpened(CameraDevice camera) {
            //Log.e(TAG, "onOpened");
            cameraDevice = camera;
            initCamera();
            createCameraPreview();
        }

        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        if (mBackgroundThread == null) return;
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void initCamera() {
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            
			StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //Size[] jpegSizes = configs.getOutputSizes(ImageFormat.JPEG);
            int[] formats = configs.getOutputFormats();
            InfoCode.log(Arrays.toString(formats));

            setReaderProperties(selectedResolution, selectedImageFormat);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void setReaderProperties(int resolution, int format){
        int width = supportedResolutions[resolution][0];
        int height = supportedResolutions[resolution][1];
        reader = ImageReader.newInstance(height, width, supportedImageFormat[format], 1);
        outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(reader.getSurface());
        outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));


        try {
            builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            toast(e.getMessage());
            return;
        }
        builder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);



        //Range<Long> exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        //float[] aperture = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);

        builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY);

        //builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);

        builder.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_HIGH_QUALITY);

        builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_EDOF);
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);


        //Range<Integer> exposureCompensationRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        //toast("" + exposureCompensationRange.getLower());
        //toast("" + exposureCompensationRange.getUpper());
        //builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensationRange.getLower());


        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
        builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);


        setZoom(selectedZoom);
        setExposureMillis(selectedExposure); // shutter Speed = 1/50s
        setISO(iso); // ISO = 200

        //builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);



        builder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
        builder.set(CaptureRequest.JPEG_ORIENTATION, 90);
        builder.addTarget(reader.getSurface());
    }

    public void setZoom(int index){
        if (InfoCode.sdk < 33) return;
        //captureRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, supportedZoomLevel[index]);
        builder.set(CaptureRequest.CONTROL_ZOOM_RATIO, supportedZoomLevel[index]);

    }


    public void setExposureMillis(int index){
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, supportedExposureMillis[index] * 1000000L);
    }

    public void setISO(int iso){
        builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);

    }


    public void processJPEG(Image image){
        /*ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        final byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        saveJpg(bytes);
        image.close();*/
        //imageScanner.scanJpgToPng(image);
        imageScanner.scanJpgToOpenCVPng(image, blockSize, subsConst);
    }
    private void saveJpg(byte[] bytes){
        save(bytes, ".jpg");
        // Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void processYUV(Image image){
        //int width = supportedResolutions[selectedResolution][0];
        //int height = supportedResolutions[selectedResolution][1];

        //image.setCropRect(); //crop image if necessary;


        imageScanner.scanPng(image);

        //toast("buffer.capacity " + buffer.capacity());
        //image.close();

    }
    private void savePng(byte[] bytes){
        //InfoCode.log(Arrays.toString(bytes));
        save(bytes, ".png");

        //toast("" + bytes.length);
        /*Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);*/

        //byte[] newB = new byte[20];
        //System.arraycopy(bytes, 0, newB, 0, 20);
        //InfoCode.log(Arrays.toString(newB));
        //toast("first " + bytes[0] +", last " + bytes[width]);
    }

    private void save(byte[] bytes, String ext){
        final File file = new File(photoPath + "/p" + System.currentTimeMillis() + ext);
        if (file == null) return;
        try (OutputStream output = new FileOutputStream(file)) {
            if (output == null) return;
            output.write(bytes);
            updateMediaStore(file);
            //runOnUiThread(()->toast("Saved"));
        } catch (IOException e) {
            toast("Failed");
        }

    }
    protected void takePicture() {
        runOnUiThread(()-> disable(captureBtn));
        try {
            ImageReader.OnImageAvailableListener readerListener = reader -> {
                Image image = reader.acquireLatestImage();
                switch (selectedImageFormat){
                    case 0: case 2: // JPEG , HEIC
                        processJPEG(image);
                        break;

                    case 1 : processYUV(image); break;
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
               //CameraCaptureSession captureSession;

                public void onConfigured(CameraCaptureSession session) {
                    //captureSession = session;
                    try {
                        session.capture(builder.build(), captureListener, mBackgroundHandler);
                    }
                    catch (CameraAccessException ignored) {}
                }
                public void onConfigureFailed(CameraCaptureSession session) {}
            }, mBackgroundHandler);
        }
        catch (CameraAccessException ignored) {}
    }

    public void updateMediaStore(File file){
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
        .setData(Uri.fromFile(file)));
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;


            int width = previewResolution[0]; //imageDimension.getWidth();
            int height = previewResolution[1]; //imageDimension.getHeight();


            //InfoCode.log("width " + width + " height " + height);
            //toast("width " + width + " height " + height);

            texture.setDefaultBufferSize(width, height);
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (InfoCode.sdk > 32) captureRequestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, supportedZoomLevel[3]);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraScanActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        /*CameraManager */manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //Log.e(TAG, "is camera open");
        try {
            String cameraId = manager.getCameraIdList()[0];

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    protected void onResume() {
        super.onResume();
        if (textureView == null) return;
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    protected void onPause() {
        timedCapture.safelyPause();
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //imageScanner.disconnectOpenCVService();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isMunuOpen){
            isMunuOpen = false;
            hide(popupMenu);
            resolutionSpinner.setSelection(selectedResolution);
            shutterSpeedSpinner.setSelection(selectedExposure);
            isoSeekbar.setProgress((iso-100) / 50);

            /*blockSizeSeekbar.setProgress((blockSize - 3) / 2);
            subsConstSeekbar.setProgress(subsConst);
            */
            blockSizePicker.setValue((blockSize - 3) / 2);
            constPicker.setValue(subsConst);
        }
        else super.onBackPressed();
    }

    /**OnClick
    public void togglePopupMenu(View v){

    }
    */
    boolean isMunuOpen = false;
    public void togglePopupMenu(View v){
        isMunuOpen = !isMunuOpen;
        if (isMunuOpen){
            show(popupMenu);
        }
        else {
            hide(popupMenu);

            //update ReaderProperties
            selectedResolution = resolutionSpinner.getSelectedItemPosition();
            selectedImageFormat = imageFormatSpinner.getSelectedItemPosition();

            setReaderProperties(selectedResolution, selectedImageFormat);

            //update zoom
            selectedZoom = zoomSpinner.getSelectedItemPosition();
            setZoom(selectedZoom);

            //update exposure
            selectedExposure = shutterSpeedSpinner.getSelectedItemPosition();
            setExposureMillis(selectedExposure);

            //update iso
            iso =  isoSeekbar.getProgress() * 50 + 100;
            setISO(iso);

            /*blockSize = blockSizeSeekbar.getProgress() * 2 + 3;
            subsConst = subsConstSeekbar.getProgress();
*/
            blockSize = getBlockSize();
            subsConst = constPicker.getValue();

            String settingOverview = resolutionOptions[selectedResolution] + ", iso:" + iso + ", bs:" + blockSize + ", c:" + subsConst;
            settingOverviewTV.setText(settingOverview);
            saveSettings();

        }
    }


    public void saveSettings() {
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("blockSize", blockSize);
        editor.putInt("subsConst", subsConst);
        editor.apply();

    }


















}
