package com.example.medicalnotifications;


import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicalnotifications.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_REQUEST_CODE = 10234;
    static boolean active = false;
    @Override
    public void onStart() {
        super.onStart();
        active = true;
        Log.d("MainActivity","onStart()");
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
        Log.d("MainActivity","onStop()");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_item) {
            Intent launch=SettingsActivity.getIntent(this);
            startActivity(launch);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        checkDrawOverlayPermission();
        //String manufacturer = "xiaomi";
        /*if(manufacturer.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
            //this will open auto start screen where user can enable permission for your app
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);
        }*/
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }

    private void launchMainService(){
        this.startService(new Intent(getBaseContext(), MQTTService.class));
    }

    private void checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {

                // If not, form up an Intent to launch the permission request
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));

                // Launch Intent, with the supplied request code
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
            }else{
                launchMainService();
            }
        }else{
            launchMainService();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if a request code is received that matches that which we provided for the overlay draw request
        if (requestCode == OVERLAY_REQUEST_CODE) {
            // Double-check that the user granted it, and didn't just dismiss the request
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    launchMainService();
                } else {
                    Toast.makeText(this, "Se necesita el permiso para poder lanzar las alarmas...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



}