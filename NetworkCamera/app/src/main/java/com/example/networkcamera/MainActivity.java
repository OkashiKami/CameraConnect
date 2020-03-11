package com.example.networkcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final  int PERMISSIONS_CODE  = 2301;
    public static final String[] PERMISSIONS = { Manifest.permission.CAMERA, };
    public boolean isCameraInitialized;
    public Camera mCamera = null;
    private static SurfaceHolder myHolder;
    public static CameraPreview mPreview;
    public FrameLayout preview;
    private static int rotation;
    private boolean whichCamera = true;
    private  static Camera.Parameters p;
    private  static List<String> camEffects = new ArrayList<>();
    private static FloatingActionButton flashB;
    private static OrientationEventListener orientationListener = null;
    private static boolean fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

              FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ServiceCast")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_CODE && grantResults.length > 0)
        {
            if(arePermissionsDenied())
            {
                ((ActivityManager)(getSystemService(ACCESSIBILITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
            else
            {
                onResume();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied())
        {
            requestPermissions(PERMISSIONS, PERMISSIONS_CODE);
            return;
        }

        if(!isCameraInitialized)
        {
            mPreview = new CameraPreview(this, mCamera);
            preview = findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            RotateCamera();
            flashB = findViewById(R.id.flash_button);
            if(hasFlash())
            {
                flashB.setVisibility(View.VISIBLE);
            }
            else
            {
                flashB.setVisibility(View.GONE);
            }
            orientationListener = new OrientationEventListener(this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    RotateCamera();
                }
            };
            orientationListener.enable();
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(whichCamera)
                    {
                        
                    }
                    return false;
                }
            });
        }
    }

    @SuppressLint("NewApi")
    public boolean arePermissionsDenied () {
        int numberOfPermission = PERMISSIONS.length;
        for (int i = 0; i < numberOfPermission; i++)
        {
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
        }
        return false;
    }

    public void RotateCamera() {
        if(mCamera != null)
        {
            rotation = getWindowManager().getDefaultDisplay().getRotation();
            if(rotation == 0)
                rotation = 90;
            else if (rotation == 1)
                rotation = 0;
            else if(rotation == 2)
                rotation = 270;
            else if(rotation == 3)
                rotation = 180;
            mCamera.setDisplayOrientation(rotation);
            if(!whichCamera)
            {
                if(rotation == 90)
                    rotation = 270;
                else if (rotation == 270)
                    rotation = 90;
            }
            p = mCamera.getParameters();
            p.setRotation(rotation);
            mCamera.setParameters(p);
        }
    }

    public static boolean hasFlash(){
        camEffects = p.getSupportedColorEffects();
        final List<String> flashModes = p.getSupportedFlashModes();
        if(flashModes == null)
            return  false;
        for (String flashMode : flashModes) {
            if(Camera.Parameters.FLASH_MODE_ON.equals(flashMode))
                return true;
        }
        return false;
    }

    public static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private static SurfaceHolder mHolder;
        private static Camera mCamera;

        public CameraPreview(Context context, Camera camera)
        {
            super(context);
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            myHolder = holder;
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
        {

        }
    }

}
