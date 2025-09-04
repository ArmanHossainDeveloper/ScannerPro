package app.scanner.pro;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import app.scanner.pro.R;
import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;
import arman.common.ui.widget.PhotoView;


public class ImageViewer extends DrawerActivity {

    PhotoView imgView;
    private String photoPath = InfoCode.SD_CARD + "/DCIM/Scanner";
    File photoDir = new File(photoPath);
	ArrayList<File> files = new ArrayList<>();
    int index = 0;
    int length = 0;
    Button previousBtn, nextBtn;
    boolean isPreviousEnabled = false, isNextEnabled = true;
    @Override
    protected int[] getRequiredPermission() {
        return new int[0];
    }


    @Override
    protected void onCreate() {
        setContentView(R.layout.image_viewer);
        initialize();
    }

    @Override
    protected void onSettingsChange() {

    }


    private void initialize() {
        imgView = find(R.id.myimage);
        previousBtn = find(R.id.previous_btn);
        nextBtn = find(R.id.next_btn);
        getNewFiles(photoDir);
        if (length < 2) disableNext();
        if (hasPhotos()) showImage(0);
        else toast("No Photos Yet!");
    }

	public void getNewFiles(File file){
		File[] fileArray = file.listFiles();
		new app.scannercv.FileSorter().sort(fileArray);
		for(File f : fileArray){
			if (f.isFile()) files.add(f);
		}
        length = files.size();
	}

    private boolean hasPhotos() { return !files.isEmpty();}

    private void showImage(int i) {
        Bitmap bitmap = BitmapFactory.decodeFile(files.get(i).getPath());
        imgView.setBitmap(bitmap);

        if (i == 0) {
            disablePrev();
            if (length > 1 && !isNextEnabled) enableNext();
        }
        else if (i == length - 1) {
            disableNext();
            if (!isPreviousEnabled) enablePrev();
        }
        else if (!isPreviousEnabled) enablePrev();
        else if (/*length > 1 && */!isNextEnabled) enableNext();
    }
    public void deleteImg(View v) {
        if (hasPhotos()) {
            if (files.get(index).delete()) {
                files.remove(index);
                if (hasPhotos()) {
                    length = files.size();
                    if (length == 1) disableNext();
                    if (index < length) showImage(index);
                    else showImage(--index);
                }
                else {
                    finish();
                    toast("All Photos Delete");
                }
            }
        }
        else {
            finish();
            toast("No Photos to Delete");
        }
    }

    public void nextImg(View v) {showImage(++index);}
    public void previousImg(View v) {showImage(--index);}
    void enablePrev() {setPrev(true);}
    void disablePrev() {setPrev(false);}
    void setPrev(boolean enable) {previousBtn.setEnabled(isPreviousEnabled = enable);}
    void enableNext() {setNext(true);}
    void disableNext() {setNext(false);}
    void setNext(boolean enable) {nextBtn.setEnabled(isNextEnabled = enable);}


    @Override
    public void onBackPressed() {
        finish();
    }

}
