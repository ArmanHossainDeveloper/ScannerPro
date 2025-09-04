package arman.common.ui.drawer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import arman.common.infocodes.InfoCode;


public class DrawerHandler {

	private boolean isLeftOpen = false;
	private boolean isRightOpen = false;
	private DrawerWarehouse warehouse;

    DrawerHandler(DrawerWarehouse warehouse){
        this.warehouse = warehouse;
    }


	void toggleLeftDrawer() {
		if (isLeftOpen) closeLeft();
		else {
			if (isRightOpen) closeRight();
			openLeft();
		}
	}
	void toggleRightDrawer() {
		if (isRightOpen) closeRight();
		else {
			if (isLeftOpen) closeLeft();
			openRight();
		}
	}

	boolean onBackPressed(){
		if (isLeftOpen) hideLeft();
		else if (isRightOpen) hideRight();
		else if (warehouse.hasRightDrawer) showRight();
		else if (warehouse.hasLeftDrawer) openLeft();
		else return false;
		return true;
	}


	void openLeft(){
		//if (isDrawerDisabled) return;
		if(warehouse.hasLeftDrawer) {
			if (isRightOpen) closeRight();
			if (!isLeftOpen) showLeft();
		}
		else InfoCode.log("No LeftDrawer");
	}
	void openRight(){
		//if (isDrawerDisabled) return;
		if(warehouse.hasRightDrawer) {
			if (!isRightOpen) showRight();
		}
		else InfoCode.log("No RightDrawer");
	}
	void closeLeft(){
		if (isLeftOpen) hideLeft();
		//else InfoCode.log("No LeftDrawer");
	}
	void closeRight(){
		if (isRightOpen) hideRight();
		//else InfoCode.log("No RightDrawer");
	}

	private void showLeft(){ warehouse.showLeft(); isLeftOpen = true;}
	private void showRight(){ warehouse.showRight(); isRightOpen = true;}
	private void hideLeft(){ warehouse.hideLeft(); isLeftOpen = false;}
	private void hideRight(){ warehouse.hideRight(); isRightOpen = false;}

}
