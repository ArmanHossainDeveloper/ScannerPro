package arman.common.ui.drawer;

import android.widget.RelativeLayout;

import arman.common.ui.DrawerActivity;

public class DrawerManager {
	
	/*LinearLayout leftDrawer, rightDrawer;
	boolean hasLeftDrawer, hasRightDrawer;

	View drawer, leftSwiper, rightSwiper;

	DrawerActivity activity;*/
	private DrawerHandler handler;
	private DrawerWarehouse warehouse;
	public DrawerManager(DrawerActivity activity){
		//this.activity = activity;
		warehouse = new DrawerWarehouse(activity);
		handler = new DrawerHandler(warehouse);
	}

    public void setDrawers(int frame, int left, int right, RelativeLayout root) {
		warehouse.setDrawers(frame, left, right, root, handler);

    }

    public void enableSwipers(boolean enable){
		warehouse.enableSwipers(enable);

    }
	public void openLeftDrawer(){
		handler.openLeft();
	}
    public void closeLeftDrawer(){
        handler.closeLeft();
    }
    public void closeRightDrawer(){
        handler.closeRight();
    }
	public void openRightDrawer(){
		handler.openRight();
	}
	public boolean onBackPressed(){
		return handler.onBackPressed();
	}

}
