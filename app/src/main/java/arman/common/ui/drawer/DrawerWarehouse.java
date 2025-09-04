package arman.common.ui.drawer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;

public class DrawerWarehouse {

    private DrawerActivity context;
	LinearLayout leftDrawer, rightDrawer;

	View drawer, leftSwiper, rightSwiper;
	boolean hasLeftDrawer, hasRightDrawer;
    private DrawerHandler handler;

    DrawerWarehouse(DrawerActivity context){
        this.context = context;
    }



    void setDrawers(int frame, int left, int right, RelativeLayout root, DrawerHandler handler) {
        if (left == right ) return;
        this.handler = handler;
        drawer = inflateLayout(frame);

        if (left != -1){
            setLeftSwiper();
            setLeftDrawer(left);
            setLeftCloser();
        }

        if (right != -1){
            setRightSwiper();
            setRightDrawer(right);
            setRightCloser();
        }
        root.addView(drawer);

    }




    private void setLeftSwiper() {
        leftSwiper = find("left_swiper");
        leftSwiper.setOnTouchListener((v, e) -> {
            if ((e.getAction() == MotionEvent.ACTION_UP) && (e.getX() > 31)) {
                handler.openLeft();
            }
            return true;
        });
    }
    private void setRightSwiper() {
        rightSwiper = find("right_swiper");
        rightSwiper.setOnTouchListener((v, e) -> {
                if ((e.getAction() == MotionEvent.ACTION_UP) && (e.getX() < -3)) {
                handler.openRight();
            }
            return true;
        });
    }
    private void setLeftDrawer(int left) {
        leftDrawer = find("left_drawer");
        leftDrawer.addView(inflateLayout(left, leftDrawer), 0);
        hasLeftDrawer = true;
    }

    private void setRightDrawer(int right) {
        rightDrawer = find("right_drawer");
        rightDrawer.addView(inflateLayout(right, rightDrawer));
        hasRightDrawer = true;
    }

    private void setLeftCloser() {
        find("left_closer").setOnClickListener(v -> handler.closeLeft());

    }
    private void setRightCloser() {
        find("right_closer").setOnClickListener(v-> handler.closeRight());

    }


    void enableSwipers(boolean enable){
		if (enable){
            if (hasLeftDrawer) show(leftSwiper);
            if (hasRightDrawer) show(rightSwiper);
        }
        else {
            if (hasLeftDrawer) hide(leftSwiper);
            if (hasRightDrawer) hide(rightSwiper);
        }
    }

    private View inflateLayout(int resource){
        return LayoutInflater.from(context).inflate(resource, null);

    }
    private View inflateLayout(int resource, ViewGroup root){
        return LayoutInflater.from(context).inflate(resource, root, false);
    }

    private <T extends View> T find(String tag){
        return drawer.findViewWithTag(tag);
    }

	void showLeft(){ show(leftDrawer);}
	void showRight(){ show(rightDrawer);}
	void hideLeft(){ hide(leftDrawer);}
	void hideRight(){ hide(rightDrawer);}

	private void show(View v){v.setVisibility(View.VISIBLE); }
	private void hide(View v){v.setVisibility(View.GONE); }
}
