package arman.common.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import arman.common.infocodes.InfoCode;
import arman.common.permission.PermissionManager;

public abstract class BaseActivity extends Activity implements View.OnClickListener{

	private PermissionManager permissionManager;
    public SharedPreferences preference;
    private int[] permissions;
    private BaseWarehouse warehouse;
    private View focusArresterView;
    InputMethodManager imm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager = new PermissionManager(this);
        warehouse = new BaseWarehouse(this);
        preference = getSharedPreferences("settings", 0);
        permissions = getRequiredPermission();
        if (permissionManager.hasRequiredPermissions(permissions, true)) onCreate();

        focusArresterView = find(android.R.id.content);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    public void arrestFocus(){
        focusArresterView.requestFocus();
    }
    public void hideIme(){
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusArresterView.getWindowToken(), 0);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(permissionManager.isGranted(requestCode, permissions, grantResults)){
            if (permissionManager.hasRequiredPermissions(this.permissions, false)) onCreate();
        }
        else {
            toast("Permissions Required");
            exit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int permissionResult = permissionManager.getPermissionResult(requestCode, resultCode, data);
        if(permissionResult == InfoCode.PERMISSION_GRANTED){
            if (permissionManager.hasRequiredPermissions(permissions, false)) onCreate();
        }
		else if(permissionResult == InfoCode.PERMISSION_DENIED){
            toast("Permissions Required");
            exit();
        }
		if (requestCode == InfoCode.SETTINGS_UPDATE && resultCode == InfoCode.SETTINGS_UPDATE){
			onSettingsChange();
		}
    }

    @Override
    public void onClick(View v) {onClick(v.getId());}

    @Override
    public void onBackPressed() {
        warehouse.onBackPressed();
    }

    protected abstract int[] getRequiredPermission();

    protected abstract void onCreate();
    protected abstract void onSettingsChange();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) onRotate(true);
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) onRotate(false);
    }

    public void onRotate(boolean landscape){}
    public void onKeyboardConnected(boolean connected){}

    public void exit(){finishAndRemoveTask();}
    public void onClick(int id){}

    /*public void setClickListenerFor(int id){find(id).setOnClickListener(this);}
    public void setClickListenerFor(View v){v.setOnClickListener(this);}*/

    public <T extends View> T find(int id){return findViewById(id);}
    public void toast(String text){Toast.makeText(this, text, Toast.LENGTH_SHORT).show();}
    public void log(String text){Log.e("DebugLog", text);}

	public void show(View v){v.setVisibility(View.VISIBLE); }
	public void hide(View v){v.setVisibility(View.GONE); }

	public void enable(View v){v.setEnabled(true);}
	public void disable(View v){v.setEnabled(false);}
}
