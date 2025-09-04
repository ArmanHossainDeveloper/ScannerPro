package arman.common.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import arman.common.ui.drawer.DrawerManager;

public abstract class DrawerActivity extends BaseActivity {
    private boolean isInflated = false, isBlocked;
    private DrawerManager manager;

    public void setLeftDrawer(int frame, int left){
        setDrawers(frame, left, -1);
    }
    public void setRightDrawer(int frame, int right){
        setDrawers(frame, -1, right);
    }

    public void setDrawers(int frame, int left, int right){
        if (isInflated) {
            toast("Can't Inflate Drawer Twice");
            return;
        }
        manager = new DrawerManager(this);
        View view = ((ViewGroup) find(android.R.id.content)).getChildAt(0);

        if (view instanceof RelativeLayout) {
            manager.setDrawers(frame, left, right, (RelativeLayout) view);
            isInflated = true;
        }
        else toast("Activity RootView Must be RelativeLayout");
    }
    public void enableSwipers(boolean enable){
        if (isInflated) manager.enableSwipers(enable);
    }
    public void openLeftDrawer(){
        if (isInflated && !isBlocked) manager.openLeftDrawer();
    }
    public void openRightDrawer(){
        if (isInflated && !isBlocked) manager.openRightDrawer();
    }

    @Override
    public void onBackPressed() {
        /*if (isInflated && isBlocked)
        if (!isInflated)*/
        if (!isInflated || !manager.onBackPressed()) super.onBackPressed();
        //toggleDrawer();
        //super.onBackPressed();
    }

    /*public void toggleDrawer() {
        manager.onBackPressed();
    }*/
    public void blockDrawer(){
        isBlocked = true;
    }
    public void unblockDrawer(){
        isBlocked = false;
    }
    public void closeLeftDrawer(){
        manager.closeLeftDrawer();
    }
    public void closeRightDrawer(){
        manager.closeRightDrawer();
    }
}
