package com.yorku.mstew.camera2videoimage;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import android.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Manifest;
import java.util.jar.Pack200;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2VideoImageActivity extends AppCompatActivity {

    public Bundle savedInstanceState;
//attribute section
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private String mCameraId;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private Size mPreviewSize;
    private static final int REQUEST_CAMERA_PERMISSION_RESULT=0;
    private CaptureRequest.Builder mCaptureRequestBuilder;



    private void setupCamera(int width, int height){
        CameraManager cameraManager=(CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            int rotatedWidth = 0;
            int rotatedHeight = 0;

            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                int totalRotation;
                totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                rotatedWidth = width;
                rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }


                mCameraId = cameraId;


                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                return;
            }

        }catch(CameraAccessException e)
        {
            e.printStackTrace();
        }
    }



    public Camera2VideoImageActivity() {
    }


    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(CameraDevice camera)
        {
            //startPreview();
            mCameraDevice=camera;
            Toast myToast1= Toast.makeText(getApplicationContext(),"Camera connection made!",Toast.LENGTH_SHORT);
            myToast1.show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice=null;

        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice=null;
        }
    };
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,0);
        ORIENTATIONS.append(Surface.ROTATION_90,90);
        ORIENTATIONS.append(Surface.ROTATION_180,180);
        ORIENTATIONS.append(Surface.ROTATION_270,270);
    }



    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()/(long) rhs.getWidth() * rhs.getHeight());
        }
    }
    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
            if (bigEnough.size() > 0) {
                return Collections.min(bigEnough, new CompareSizeByArea());
            } else {
                return choices[0];
            }
        }
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
        int sensorOrientation=cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation=ORIENTATIONS.get(deviceOrientation);
        return(sensorOrientation + deviceOrientation + 360)%360;
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener= new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            connectCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

    };

    /* private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);


                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(),"unable to setup camera preview", Toast.LENGTH_SHORT).show();

                }

            },null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    } */
     //check
     private void connectCamera(){
        CameraManager cameraManager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);
                }
                else{
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
                        Toast.makeText(this, "Video App requires access to the Camera u dummy", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION_RESULT);
                }

        }
            else {
                cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);

            }

    } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void startBackgroundThread(){
        mBackgroundHandlerThread=new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler=new Handler(mBackgroundHandlerThread.getLooper());
    }
    private void stopBackgroundThread(){
        mBackgroundHandlerThread.quitSafely();
        mBackgroundHandler=null;
        mBackgroundHandlerThread=null;
        try{
            mBackgroundHandlerThread.join();

        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_video_image);
        mTextureView=(TextureView)findViewById(R.id.textureView);

    }

    @Override
    protected void onResume(){

        super.onResume();
        startBackgroundThread();
        if(mTextureView.isAvailable()){
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());

        } else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        connectCamera();
    }
    @Override
    protected void onPause(){
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults){
     super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQUEST_CAMERA_PERMISSION_RESULT){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "App will not run without camera access", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onWindowFocusChanged ( boolean hasFocas){
        super.onWindowFocusChanged(hasFocas);
        View decorView = getWindow().getDecorView();
        if (hasFocas) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

    }

    private void closeCamera(){
        if(mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice=null;

        }
    }
}
