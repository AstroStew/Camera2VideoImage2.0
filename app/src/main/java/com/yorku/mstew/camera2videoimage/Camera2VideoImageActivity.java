package com.yorku.mstew.camera2videoimage;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_SHADE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_TWILIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.toIntExact;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2VideoImageActivity extends AppCompatActivity {


    //firstly we want to make the window sticky. We acheive this by making system flags
    //Making the window sticky

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    //This is our attribute section.Note:this list will increase as we progress through the tutorial. We will create all members in this section:
    //private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    // private HandlerThread mBackgroundHandlerThread;
    //private Handler mBackgroundHandler;
    private Size mPreviewSize;
    //private static final int REQUEST_CAMERA_PERMISSION_RESULT=0;
    //private CaptureRequest.Builder mCaptureRequestBuilder;

    // under here is my previous code. lets see if it works?
//nope so lets restart
    //create surface texture listener
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);

            connectCamera();

            // Toast.makeText(getApplicationContext(), "Texture is available", Toast.LENGTH_SHORT).show();
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

    long ShutterSpeedValue;
    long xx2;
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    //Creating the camera device
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            //Toast.makeText(getApplicationContext(), "Camera Connected", Toast.LENGTH_SHORT).show();

            if (mIsRecording) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startRecord();
                mMediaRecorder.start();

                try {
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.start();
                    //new
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                startPreview();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }

    };
    //
    //Getting Camera Id

    //doesn't work
    private String mCameraId;


    private int mTotalRotation;
    StreamConfigurationMap map;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "Autofocus locked", Toast.LENGTH_SHORT).show();




                            }
                            if ( afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
                                Toast.makeText(getApplicationContext(), "Autofocus no locked!", Toast.LENGTH_SHORT).show();
                            }
                            startStillCaptureRequest();
                            break;
                    }

                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)


                {
                    super.onCaptureCompleted(session, request, result);
                    mCaptureResult = result;
                    process(result);
                }
            };
    /*private boolean hasPermissionsGranted(String[] permissions){
        for (String permission: permissions){
            if(ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    } */
    private static final String[] VIDEO_PERMISSIONS ={
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };


    private void setupCamera(int width, int height) {
       /* if(!hasPermissionsGranted(VIDEO_PERMISSIONS)){
            //requestVideoPermissions();
            return;
        } */

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            mCameraId = cameraManager.getCameraIdList()[FlipNumber];





                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);

                    map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);


                    int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                    mTotalRotation = sensorDeviceRotation(cameraCharacteristics, deviceOrientation);
                    boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                    int rotatedWidth = width;
                    int rotatedHeight = height;
                    if (swapRotation) {
                        rotatedWidth = height;
                        rotatedHeight = width;

                    }

                    mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);

                    mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);


                    mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                    mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                    mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);


                    mRawImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.RAW_SENSOR), rotatedWidth, rotatedHeight);
                    mRawImageReader = ImageReader.newInstance(mRawImageSize.getWidth(), mRawImageSize.getHeight(), ImageFormat.RAW_SENSOR, 1);
                    mRawImageReader.setOnImageAvailableListener(mOnRawImageAvailableListener, mBackgroundHandler);

                    //mCameraId = cameraManager.getCameraIdList()[FlipNumber];
                    mCameraCharacteristics = cameraCharacteristics;


                    //continue;








        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "App requires access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                            REQUEST_CAMERA_PERMISSION_RESULT);
                }
                //return;
            } else {


                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //close the camera
    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    //Creating a background thread
    private HandlerThread mBackgroundHandlerThread;

    private Handler mBackgroundHandler;

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Cam2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());

    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Adjusting orientation for calculating preview size
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);


    }

    private static int sensorDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;

    }


    //setting preview size dimensions
    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());

        }

    }

    //pt8
    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            for(Size option: choices){
                if(option.getHeight()*option.getWidth()<= height*width){
                    return option;
                }
            }
            return choices[0];

        }
    }



    //onCreate was here since the start

    Button mSettingsbutton;
    int ISOvalue=0;
    int progressValue;
    EditText mTextSeekBar;
    EditText mMinimumShutterSpeed;
    EditText mMaximumShutterSpeed;
    Button mAutobutton;
    EditText mISOtext;
    public boolean mIsAuto2=false;
    int AutoNumber=1;
    boolean menuonline=false;
    ImageButton mCloseALLbutton;
    Button mShutterAuto;
    boolean ShutterAutoon=false;
    String ShutterSpeed2String;
    String ShutterSpeed1String;
    private static Uri mRequestingAppUri;
    SeekBar mSeekBar2;
    ImageButton mFlipCamera;
    Boolean FlipNumberBoolean=false;
    int FlipNumber;
    private TextView  mCameraInfoTextView;
    SeekBar mISOseekbar;
    int ISOprogressValue;
    int ISOseekProgress;
    private int mWBMode = CONTROL_AWB_MODE_AUTO;
    private EditText mISOEditText;
    private TextView mISOEditTextView;
    private EditText mShutterSpeedEditText;
    private TextView mShutterSpeedEditTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_camera2_video_image);


        //mShutterAuto=(Button) findViewById(R.id.shutterAuto);
        /*mShutterAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ShutterAutoon){
                    AutoNumber=3;
                }
            }
        });
        */

        createVideoFolder();
        createImageFolder();
        Intent intent=getIntent();
        String action=intent.getAction();
        if(MediaStore.ACTION_IMAGE_CAPTURE.equals(action)){
            mRequestingAppUri=intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        }
        mMediaRecorder = new MediaRecorder();
        //mIsAuto2=false;
        //this is new
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mStillImageButton = (ImageButton) findViewById(R.id.CameraButton);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        mFlipCamera= (ImageButton) findViewById(R.id.FlipButton);



        mFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FlipNumberBoolean) {

                    FlipNumberBoolean = false;
                    FlipNumber = 0;
                    mFlipCamera.setImageResource(R.drawable.flipfront);
                    closeCamera();
                    stopBackgroundThread();

                    startBackgroundThread();

                    if (mTextureView.isAvailable()) {
                        setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                        connectCamera();

                    } else {
                        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                    }
                }else{

                    FlipNumberBoolean=true;
                    FlipNumber=1;
                    mFlipCamera.setImageResource(R.drawable.flipback);
                    closeCamera();
                    stopBackgroundThread();
                    startBackgroundThread();
                    if(mTextureView.isAvailable()){
                        setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                        connectCamera();
                    }else{
                        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                    }



                }
            }
        });

        mSettingsbutton = (Button) findViewById(R.id.button);
        mRawSwitch = (Switch) findViewById(R.id.RawSwitch);
        mRawSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "RAW Capture turned ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "RAW Capture turned OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*mAutobutton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mIsAuto2=true;
                    return true;
                }
               }); */


        mAutobutton= (Button) findViewById(R.id.Auto);
        mAutobutton.setText("AUTO ON");
        mAutobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AutoNumber==0){
                    AutoNumber=1;
                    //Toast.makeText(getApplicationContext(), "AUTO ON", Toast.LENGTH_SHORT).show();
                    mAutobutton.setText("AUTO ON");
                    startPreview();

                }
                else if(AutoNumber==1){
                    AutoNumber=0;
                    Toast.makeText(getApplicationContext(), "AUTO OFF", Toast.LENGTH_SHORT).show();
                    mAutobutton.setText("AUTO OFF");
                    startPreview();


                }
            }

        });


        mSettingsbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                menuonline = true;


                //Toast.makeText(Camera2VideoImageActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu = new PopupMenu(Camera2VideoImageActivity.this, mSettingsbutton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                SubMenu sM=popupMenu.getMenu().addSubMenu(0,100,0, "Change Resolution");



                StreamConfigurationMap scmap=mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                final Size previewSizes[] =scmap.getOutputSizes(ImageFormat.JPEG);

                                for (int i=0; i<previewSizes.length; i++){
                    sM.add(0,i+200,0,""+previewSizes[i]);
                }






                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //add settings
                        int position = item.getItemId();
                        for(int i=0; i < previewSizes.length; i++){
                            if(position==200+i){
                                Toast.makeText(getApplicationContext(), ""+ previewSizes[i], Toast.LENGTH_SHORT).show();
                                adjustAspectRatio(previewSizes[i].getHeight(),previewSizes[i].getWidth());
                                setupCamera(previewSizes[i].getHeight(),previewSizes[i].getWidth());
                                startPreview();

                            }
                        }


                        final Range<Long> ShutterSpeed = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                        final long ShutterSpeed1 = (ShutterSpeed.getLower());

                        final long ShutterSpeed2 = (ShutterSpeed.getUpper());
                        //Toast.makeText(getApplicationContext(), "ShutterSpeedMax: "+ ShutterSpeed2, Toast.LENGTH_LONG).show();
                        double ShutterSpeed1Double = (double) ShutterSpeed1 / 1000000000;
                        double ShutterSpeed2Double = (double) ShutterSpeed2 / 1000000000;
                        //trying to convert to fractions
                        double x = 1 / ShutterSpeed1Double;
                        if(ShutterSpeed2Double<=1){
                            double y = 1 / ShutterSpeed2Double;
                            ShutterSpeed2String = ("1"+"/"+ (int)y);
                        }
                        else {
                            double y=ShutterSpeed2Double;
                            ShutterSpeed2String = ("" + (int)y);

                        }
                        ShutterSpeed1String = ("1" + "/" + (int)x);
                        //since ShutterSpeed1 is usually a fraction anyways

                        mISOtext=(EditText) findViewById(R.id.ISOtext);
                        if (ISOvalue==0) {
                            mISOtext.setText("ISO:AUTO");
                        }

                        final Range <Integer> ISOrange= mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                        final int LowestISO=ISOrange.getLower();
                        final int HighestISO=ISOrange.getUpper();






                        switch (position) {
                            case R.id.WhiteBalanceCloudyDaylight:
                                mWBMode=CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceDaylight:
                                mWBMode=CONTROL_AWB_MODE_DAYLIGHT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceFluorescent:
                                mWBMode=CONTROL_AWB_MODE_FLUORESCENT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceShade:
                                mWBMode=CONTROL_AWB_MODE_SHADE;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceTwilight:
                                mWBMode=CONTROL_AWB_MODE_TWILIGHT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceWarmFluorescent:
                                mWBMode=CONTROL_AWB_MODE_WARM_FLUORESCENT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceIncandenscent:
                                mWBMode=CONTROL_AWB_MODE_INCANDESCENT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceAuto:
                                if (mWBMode != CONTROL_AWB_MODE_AUTO) {
                                    mWBMode = CONTROL_AWB_MODE_AUTO;
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "AUTO is already on", Toast.LENGTH_SHORT).show();
                                }
                                startPreview();
                                break;
                            case R.id.ChangeISO:


                                mISOtext.setVisibility(View.VISIBLE);

                                mCloseALLbutton= (ImageButton) findViewById(R.id.CloseALLbutton);
                                mCloseALLbutton.setVisibility(View.VISIBLE);
                                mCloseALLbutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mISOtext.setVisibility(View.INVISIBLE);
                                        mCloseALLbutton.setVisibility(View.INVISIBLE);
                                        if(mSeekbar.getVisibility()==View.VISIBLE){
                                            mSeekbar.setVisibility(View.INVISIBLE);
                                            mMaximumShutterSpeed.setVisibility(View.INVISIBLE);
                                            mMinimumShutterSpeed.setVisibility(View.INVISIBLE);
                                            mTextSeekBar.setVisibility(View.INVISIBLE);
                                        }
                                        if(mISOseekbar.getVisibility()==View.VISIBLE) {
                                            mISOseekbar.setVisibility(View.INVISIBLE);
                                        }


                                    }
                                });


                                break;

                            case R.id.ISO100:

                                //Toast.makeText(getApplicationContext(), "100 ISO", Toast.LENGTH_SHORT).show();
                                ISOvalue = 100;
                                mISOtext.setText("ISO:"+ ISOvalue);

                                startPreview();
                                break;
                            case R.id.ISO200:
                                ISOvalue = 200;

                                mISOtext.setText("ISO:"+ ISOvalue);
                                startPreview();
                                break;
                            case R.id.ISO400:
                                ISOvalue = 400;
                                mISOtext.setText("ISO:"+ ISOvalue);
                                startPreview();

                                //Toast.makeText(getApplicationContext(), "400 ISO", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.ISO800:
                                ISOvalue = 800;
                                mISOtext.setText("ISO:"+ ISOvalue);
                                startPreview();

                                //Toast.makeText(getApplicationContext(), "800", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.ISO1600:
                                ISOvalue = 1600;
                                mISOtext.setText("ISO:"+ ISOvalue);
                                startPreview();
                                //Toast.makeText(getApplicationContext(), "1600", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.customISO:
                                Toast.makeText(getApplicationContext(), "Custom ISO", Toast.LENGTH_SHORT).show();
                                mISOseekbar = (SeekBar) findViewById(R.id.ISOseekbar);
                                mISOseekbar.setVisibility(View.VISIBLE);

                                mISOseekbar.setMax((int)HighestISO-LowestISO);

                                mISOseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        progress=ISOprogressValue;
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                        mISOtext.setText("ISO:" + (mISOseekbar.getProgress()+LowestISO));
                                       ISOseekProgress=(mISOseekbar.getProgress()+LowestISO);
                                        ISOvalue=ISOseekProgress;
                                        startPreview();

                                    }
                                });

                                startPreview();
                                break;
                            case R.id.custominputISO:



                                LayoutInflater inflater = LayoutInflater.from(Camera2VideoImageActivity.this);
                                final View subsubView=inflater.inflate(R.layout.manual_input_alertdialog, null);
                                final AlertDialog.Builder manualISODialog = new AlertDialog.Builder(Camera2VideoImageActivity.this);



                                mISOEditText= (EditText)subsubView.findViewById(R.id.isoEditText);
                                mISOEditTextView= (TextView) subsubView.findViewById(R.id.isoTitle);
                                mISOEditTextView.setText("ISO Range:"+LowestISO+"to"+HighestISO);
                                manualISODialog.setTitle("Manual ISO Input");
                                manualISODialog.setView(subsubView);
                                manualISODialog.setCancelable(true);
                                manualISODialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                         int tempISO=Integer.parseInt(mISOEditText.getText().toString());
                                        if(tempISO <= HighestISO && tempISO>= LowestISO ){
                                            ISOvalue=tempISO;
                                            mISOtext.setText("ISO:"+ ISOvalue);
                                            startPreview();
                                            return;
                                        }else{
                                            Toast.makeText(getApplicationContext(), "ISO value is out of range", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                manualISODialog.show();

                            break;
                            case R.id.ChangeShutterSpeedSeek:
                                mSeekbar = (SeekBar) findViewById(R.id.seekBar);
                                mSeekbar.setVisibility(View.VISIBLE);
                                mSeekbar.setProgress(progressValue);
                                mTextSeekBar = (EditText) findViewById(R.id.editText);
                                mTextSeekBar.setVisibility(View.VISIBLE);
                                if(ShutterSpeed2Double<1) {
                                    mSeekbar.setMax((int) (ShutterSpeed2 - ShutterSpeed1));
                                }
                                else {
                                    //Working on a precision bar for camera's with higher shutter speed capacity
                                    Toast.makeText(getApplicationContext(), "Precision Option Available", Toast.LENGTH_SHORT).show();
                                    mSeekBar2= (SeekBar) findViewById(R.id.seekBar2);
                                    mSeekBar2.setVisibility(View.VISIBLE);
                                    mSeekbar.setMax((int)Math.round(ShutterSpeed2Double));
                                    mTextSeekBar.setText("Shutter Speed(in s)");
                                }

                                //Note:The SeekBar can only take Interger Values. If ShutterSpeed2-ShutterSpeed1==0 then the ShutterSpeed difference is too great
                                //Integers can
                                //mSeekbar.setProgress(100000);
                                mMinimumShutterSpeed = (EditText) findViewById(R.id.MinimumShutterSpeed);
                                mMinimumShutterSpeed.setVisibility(View.VISIBLE);
                                mMinimumShutterSpeed.setText(ShutterSpeed1String);
                                mMaximumShutterSpeed = (EditText) findViewById(R.id.MaximumShutterSpeed);
                                mMaximumShutterSpeed.setVisibility(View.VISIBLE);
                                mMaximumShutterSpeed.setText(ShutterSpeed2String);



                                //mTextSeekBar.setText("Shutter Speed(in ns) :" +  + "/" + mSeekbar.getMax());
                                mCloseALLbutton= (ImageButton) findViewById(R.id.CloseALLbutton);
                                mCloseALLbutton.setVisibility(View.VISIBLE);
                                mCloseALLbutton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMinimumShutterSpeed.setVisibility(View.INVISIBLE);
                                        mTextSeekBar.setVisibility(View.INVISIBLE);
                                        mSeekbar.setVisibility(View.INVISIBLE);
                                        mMaximumShutterSpeed.setVisibility(View.INVISIBLE);
                                        mCloseALLbutton.setVisibility(View.INVISIBLE);
                                        if (mISOtext.getVisibility()==View.VISIBLE){
                                            mISOtext.setVisibility(View.INVISIBLE);
                                        }
                                        if(mISOseekbar.getVisibility()==View.VISIBLE) {
                                            mISOseekbar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });




                                mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                        progress = progressValue;

                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {
                                        //Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                        mTextSeekBar.setText("Shutter Speed(in ns):" + (mSeekbar.getProgress()+ShutterSpeed1) + "/" + Math.round(mSeekbar.getMax()+ShutterSpeed1));
                                        Toast.makeText(getApplicationContext(), "Setting Shutter Speed", Toast.LENGTH_SHORT).show();
                                        ShutterSpeedValue=(mSeekbar.getProgress()+ShutterSpeed1);

                                        startPreview();
                                    }
                                });



                                break;
                            case R.id.ChangeShutterSpeedInput:

                                LayoutInflater inflater3= LayoutInflater.from(Camera2VideoImageActivity.this);
                                final View ChangeShutterSpeedView=inflater3.inflate(R.layout.shutterspeed_input_alertdialog, null);
                                final AlertDialog.Builder manualShutterSpeedDialog = new AlertDialog.Builder(Camera2VideoImageActivity.this);



                                mShutterSpeedEditText= (EditText)ChangeShutterSpeedView.findViewById(R.id.ShutterSpeedEditText);
                                mShutterSpeedEditTextView= (TextView) ChangeShutterSpeedView.findViewById(R.id.ShutterSpeedTitle);
                                mShutterSpeedEditTextView.setText("ShutterSpeed Range: "+ShutterSpeed1+" to "+ShutterSpeed2);
                                manualShutterSpeedDialog.setTitle("Manual Shutter Speed Input");
                                manualShutterSpeedDialog.setView(ChangeShutterSpeedView);
                                manualShutterSpeedDialog.setCancelable(true);
                                manualShutterSpeedDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int tempShutterSpeed=Integer.parseInt(mShutterSpeedEditText.getText().toString());
                                        if(tempShutterSpeed <= ShutterSpeed2 && tempShutterSpeed>= ShutterSpeed1 ){
                                            ShutterSpeedValue=tempShutterSpeed;
                                            //.setText("ISO:"+ ISOvalue);
                                            startPreview();
                                            return;
                                        }else{
                                            Toast.makeText(getApplicationContext(), "ShutterSpeed value is out of range", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                manualShutterSpeedDialog.show();










                                break;

                            case R.id.ChangeWhiteBalance:
                                Toast.makeText(getApplicationContext(), "ChangeWhiteBalance", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.getCameraInfo:
                                LayoutInflater inflater2 = LayoutInflater.from(Camera2VideoImageActivity.this);
                                View cameraInfoSubView = inflater2.inflate(R.layout.camera_info_alertdialog, null);
                                mCameraInfoTextView = (TextView)cameraInfoSubView.findViewById(R.id.cameraInfoTextView);
                                mCameraInfoTextView.setMovementMethod(new ScrollingMovementMethod());



                                AlertDialog.Builder builder = new AlertDialog.Builder(Camera2VideoImageActivity.this);
                                builder.setTitle("Camera Information");
                                builder.setMessage("Shutter Speed Information(in s):" + ShutterSpeed1String + "-" + ShutterSpeed2String + "\n" + "ISO Range:" +mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                                        + "\n" + "White Level:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_INFO_WHITE_LEVEL) + "\n" + "Sensor Physical Size: " + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                                        + "\n" + "Sensor Max Analog Sensitivity:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY)
                                        + "\n" + "Standard reference illuminant:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1)
                                        + "\n" + "Camera Compensation Range:"+mCameraCharacteristics.get(mCameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                                        + "\n" + "Supported JPEG Resolution:");
                                for (int i = 0; i < previewSizes.length; i++) {
                                    String oldTextView = mCameraInfoTextView.getText().toString();
                                    String newText= oldTextView + "\n" + previewSizes[i] + ""; // can manipulate using substring also
                                    mCameraInfoTextView.setText(newText);
                                }
                                builder.setView(cameraInfoSubView);

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });



                                AlertDialog alertDialog2 = builder.create();
                                alertDialog2.show();
                                break;
                            default:
                                return false;
                        }
                        return true;
                    }

                });
                popupMenu.show();

            }

        });


        mStillImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockFocus();
            }
        });
        mRecordImageButton = (ImageButton) findViewById(R.id.VideoButton);

        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (mIsRecording || mIsTimelapse) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;
                    mIsTimelapse = false;
                    mRecordImageButton.setImageResource(R.mipmap.vidpiconline);
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                    sendBroadcast(mediaStoreUpdateIntent);


                    startPreview();
                } else {
                    //mIsRecording = true;

                    //new
                    mRecordImageButton.setImageResource(R.mipmap.vidpicbusy);
                    try {
                        checkWriteStoragePermission();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        });
        mRecordImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onLongClick(View v) {
                mIsTimelapse = true;

                try {
                    checkWriteStoragePermission();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            }
        });


    }





    //pt9
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "App will not run without camera services", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "App will not run without audio services", Toast.LENGTH_SHORT).show();

            }
        }
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                mRecordImageButton.setImageResource(R.mipmap.vidpiconline);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "App needs to save video to run", Toast.LENGTH_SHORT).show();

            }
        }
    }


    private CaptureRequest.Builder mCaptureRequestBuilder;

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            if (AutoNumber==1){
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            }
            else if(AutoNumber==0){
                //manual settings
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_MODE_OFF);
                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ShutterSpeedValue );
                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,ISOvalue);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,mWBMode);

            }
            else if(AutoNumber==2){
                Toast.makeText(getApplicationContext(), "5193301137", Toast.LENGTH_SHORT).show();
            }
            else if(AutoNumber==3){
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                //mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CaptureRequest )
            }







            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface(), mRawImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mPreviewCaptureSession = session;
                            try {

                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler);

                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Unable to set up camera preview", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();


    }

    //Here we are going to create a video busy button
    private ImageButton mRecordImageButton;
    private boolean mIsRecording = false;
    //Setting up storage
    private File mVideoFolder;
    private String mVideoFileName;

    //creating the video folder
    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "Camera2_Video_Image");
        //check to see if the folder is already created
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();

        }

    }

//now we have to call the videoFolder onCreate

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //there are two types of SimpleDateFormat and Date()
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;

    }

    //create and Initialize REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;


    //Now we have to make this marshmellow compatable
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkWriteStoragePermission() throws IOException {
        if (mIsTimelapse) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    mIsTimelapse = true;
                    mRecordImageButton.setImageResource(R.mipmap.btn_timelapse);
                    createVideoFileName();
                    startRecord();
                    mMediaRecorder.start();
                    //Toast.makeText(getApplicationContext(), "Recording Timelapse", Toast.LENGTH_SHORT).show();
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.start();









                } else {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();

                    }
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
                }
            }else {
                mIsRecording = true;
                mRecordImageButton.setImageResource(R.mipmap.btn_timelapse);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    mIsRecording = true;
                    mRecordImageButton.setImageResource(R.mipmap.vidpicbusy);

                    try {
                        createVideoFileName();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startRecord();

                    mMediaRecorder.start();
                    Toast.makeText(this, "Recording Video", Toast.LENGTH_SHORT).show();
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.start();
                } else {
                    //Toast.makeText(this, "Permission to write is not granted", Toast.LENGTH_SHORT).show();
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();


                    }
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
                }
            } else {
                mIsRecording = true;
                mRecordImageButton.setImageResource(R.mipmap.vidpicbusy);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();

            }

        }
    }

    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(8000000);
        mMediaRecorder.setVideoFrameRate(30);
        // mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }
    //

    //Capturing and Saving Videos
    private void startRecord() {
        try {
            if (mIsRecording) {
                setupMediaRecorder();
            } else if (mIsTimelapse) {
                setupTimelapse();
            }
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {


                        @Override
                        public void onConfigured(CameraCaptureSession session) {

                            mRecordCaptureSession = session;

                            try {
                                mRecordCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Adding a Chronometer timer for recording
    private Chronometer mChronometer;
//now lets set up for still camera capture

    private SeekBar mSeekbar;
    private Size mImageSize;
    private Size mRawImageSize;

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    if (!mIsWritingImage) {
                        mIsWritingImage = true;
                        Image image = reader.acquireLatestImage();
                        if (image != null) {
                            mBackgroundHandler.post(new ImageSaver(image, mCaptureResult, mCameraCharacteristics));


                        }
                    }

                }
            };
    private ImageReader mRawImageReader;
    private final ImageReader.OnImageAvailableListener mOnRawImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    if (!mIsWritingRawImage) {
                        Image image = reader.acquireLatestImage();
                        if (image != null) {
                            mBackgroundHandler.post(new ImageSaver(image, mCaptureResult, mCameraCharacteristics));


                        }
                    }
                }
            };


    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;

    private void lockFocus() {
        mCaptureState = STATE_WAIT_LOCK;

        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            if (mIsRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), mRecordCaptureCallback, mBackgroundHandler);

            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private ImageButton mStillImageButton;
    //image capture
    //part 17 capturing a still image in a preview mode
    private File mImageFolder;
    private String mImageFileName;


    private void createImageFolder() {
        File imageRawFile=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "Camera2_Video_Image");
        mRawGalleryFolder = new File(imageRawFile,"Camera2_Video_Image_RAW");
        //check to see if the folder is already created
        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();

        }
        if (!mRawGalleryFolder.exists()) {
            mRawGalleryFolder.mkdirs();
        }

    }

//now we have to call the videoFolder onCreate

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //there are two types of SimpleDateFormat and Date()
        String prepend = "JPEG_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;

    }

    private File createRawImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //there are two types of SimpleDateFormat and Date()
        String prepend = "RAW_" + timestamp + "_";
        File imageRawFile = File.createTempFile(prepend, ".dng", mRawGalleryFolder);
        mRawFileName = imageRawFile.getAbsolutePath();
        return imageRawFile;

    }

    private void startStillCaptureRequest() {
        mIsWritingImage = false;
        mIsWritingRawImage = false;
        try {
            if (mIsRecording || mIsTimelapse) {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(
                        CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);

            } else {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(
                        CameraDevice.TEMPLATE_STILL_CAPTURE);
            }


            if (mRawSwitch.isChecked()) {
                mCaptureRequestBuilder.addTarget(mRawImageReader.getSurface());
                mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            } else {
                mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            }

            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            //Testing Exposure Time
            //units nanoseconds

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);


                            try {
                                createImageFileName(); //forImage
                                if (mRawSwitch.isChecked()) {
                                    createRawImageFileName(); //for RawImage
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

            if (mIsRecording || mIsTimelapse) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);

            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);


            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private class ImageSaver implements Runnable {

        private final Image mImage;
        private final CaptureResult mCaptureResult;
        private final CameraCharacteristics mCameraCharacteristics;

        private ImageSaver(Image mImage, CaptureResult mCaptureResult, CameraCharacteristics mCameraCharacteristics) {
            this.mImage = mImage;
            this.mCaptureResult = mCaptureResult;
            this.mCameraCharacteristics = mCameraCharacteristics;

        }


        @Override
        public void run() {
            int format = mImage.getFormat();
            switch(format) {
                case ImageFormat.JPEG:


                    ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(mImageFileName);
                        try {
                            fileOutputStream.write(bytes);
                            Toast.makeText(getApplicationContext(), "JPEG saved", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        mImage.close();
                        // media store update - images
                        /*Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName)));
                        sendBroadcast(mediaStoreUpdateIntent);*/
                        if(fileOutputStream != null){
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mIsWritingImage = false;
                    }
                    mIsWritingImage=true;
                    break;
                case ImageFormat.RAW_SENSOR:
                    //case ImageFormat.RAW10:
                    //case ImageFormat.RAW12:
                    //case ImageFormat.RAW_PRIVATE:
                    // 1
                    /*try {
                        createRawImageFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    DngCreator dngCreator = new DngCreator(mCameraCharacteristics, mCaptureResult);
                    FileOutputStream rawFileOutputStream = null;
                    try {
                        rawFileOutputStream = new FileOutputStream(mRawFileName);
                        dngCreator.writeImage(rawFileOutputStream, mImage);
                        Toast.makeText(getApplicationContext(), "RAW saved", Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error in saving RAW", Toast.LENGTH_SHORT).show();
                    } finally {
                        mImage.close();
                        // media store update - images
                        /*Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mRawFileName)));
                        sendBroadcast(mediaStoreUpdateIntent);*/
                        if(rawFileOutputStream != null){
                            try {
                                rawFileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mIsWritingRawImage = false;
                    }
                    mIsWritingRawImage = true;
                    break;
            }

        }
    }


    //Part 18. Capturing a photo while recording
    private CameraCaptureSession mRecordCaptureSession;
    private CameraCaptureSession.CaptureCallback mRecordCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "Autofocus locked", Toast.LENGTH_SHORT).show();

                            }
                            startStillCaptureRequest();

                            break;
                    }

                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    process(result);
                    mCaptureResult = result;


                }
            };

    //Recording Audio pt 19


    //Part 20 time-lapse video
    //long press on record button
    private boolean mIsTimelapse = false;

    private void setupTimelapse() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        mMediaRecorder.setOutputFile(mVideoFileName);
        //Frequency of how fast
        mMediaRecorder.setCaptureRate(2);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    //Updating MediaStore Database done
//Raw image Capture Part 1
    private static Boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;

        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }


    //Create an Activity member for the raw folder
    private File mRawGalleryFolder;
    private String mRawFileName;

    //Create a file for the captured raw image
    private static File mRawImageFile;
    private static File mImageFile;

    private CameraCharacteristics mCameraCharacteristics;
    CameraCharacteristics cameraCharacteristics;
    //RAW Image Capture part2
    private CaptureResult mCaptureResult;

    private boolean mIsWritingImage = false;
    private boolean mIsWritingRawImage = false;
//Now Were going to create a pop-up menu

    //
    //Raw Image Switch
    private Switch mRawSwitch;
    //ISO CHANGE
//ASpect Ratio stuff
    private static final String TAG = Camera2VideoImageActivity.TAG;
    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }




    }






