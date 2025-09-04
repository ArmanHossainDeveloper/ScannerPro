package arman.common.ui;

import android.os.Handler;

public class BaseWarehouse {
    BaseActivity activity;


    private boolean shouldExit = false;
    BaseWarehouse(BaseActivity activity){
        this.activity = activity;
    }

    void onBackPressed() {
        if (shouldExit){
            activity.exit();
        }
        else {
            shouldExit = true;
            activity.toast("Press again To Exit");
            new Handler().postDelayed(this::revert, 1000);
        }
    }
    private void revert(){ shouldExit = false; }

}
