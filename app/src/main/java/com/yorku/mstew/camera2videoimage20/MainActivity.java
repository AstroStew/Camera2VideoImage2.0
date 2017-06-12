package com.yorku.mstew.camera2videoimage20;
import android.Manifest;
import android.content.ClipData;
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
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.FaceDetector;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
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
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.transition.Scene;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yorku.mstew.camera2videoimage20.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_SHADE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_TWILIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_MODE_USE_SCENE_MODE;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_ACTION;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_BARCODE;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_BEACH;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_DISABLED;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_HDR;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_NIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_PARTY;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_SNOW;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_SPORTS;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_SUNSET;
import static android.hardware.camera2.CameraMetadata.CONTROL_SCENE_MODE_THEATRE;
import static android.hardware.camera2.CameraMetadata.FLASH_MODE_OFF;
import static android.hardware.camera2.CameraMetadata.FLASH_MODE_SINGLE;
import static android.hardware.camera2.CameraMetadata.FLASH_MODE_TORCH;
import static android.hardware.camera2.CameraMetadata.FLASH_STATE_UNAVAILABLE;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.toIntExact;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private Button mSettingsbutton;
    private int ISOvalue = 0;
    private int progressValue;
    private EditText mTextSeekBar;
    private EditText mMinimumShutterSpeed;
    private EditText mMaximumShutterSpeed;
    private Button mAutobutton;
    private EditText mISOtext;
    public boolean mIsAuto2 = false;
    private int AutoNumber = 0;
    private boolean menuonline = false;
    private ImageButton mCloseALLbutton;
    private Button mShutterAuto;
    boolean ShutterAutoon = false;
    private String ShutterSpeed2String;
    private String ShutterSpeed1String;
    private static Uri mRequestingAppUri;
    private SeekBar mSeekBar2;
    private ImageButton mFlipCamera;
    private Boolean FlipNumberBoolean = false;
    private int FlipNumber;
    private TextView mCameraInfoTextView;
    private TextView mCameraInfoTextView2;
    private TextView mCameraInfoTextView3;
    private TextView mCameraInfoTextView4;
    private SeekBar mISOseekbar;
    private int ISOprogressValue;
    private int ISOseekProgress;
    private int mWBMode = CONTROL_AWB_MODE_AUTO;
    private int mSceneMode = CONTROL_SCENE_MODE_FACE_PRIORITY;
    private int mAFMode = CONTROL_AF_MODE_AUTO;
    private EditText mISOEditText;
    private TextView mISOEditTextView;
    private EditText mShutterSpeedEditText;
    private TextView mShutterSpeedEditTextView;
    private EditText mShutterSpeedEditText2;
    private TextView mShutterSpeedEditTextView2;
    private SeekBar mChangeFocusSeekBar;
    private LinearLayout mManualFocusLayout;
    private double mFocusDistance = 20;
    private double getmFocusDistanceMem = 20;
    boolean mUnlockFocus = false;
    boolean mBurstOn = false;
    private int mBurstNumber = 0;
    private int ChronoCount = 0;
    private EditText mPhotoBurstText;
    private EditText mManualFocusInput;
    private EditText mPhotoBurstLimitText;
    private int mPhotoTimeLimitNumber = 1;
    int SecondStep = 5;
    int PhotoBurstTimeStop;
    EditText mVideoTimelapse;
    int VideoTimelapsSecondStep = 2;
    ImageButton mFlashButtonOnOff;
    int mFlashMode = 0;
    boolean BooleanAutoFocusLock = false;
    boolean BooleanOpticalStabilizationOn = true;
    TextView mTimeInterval;
    int AutoLocks=0;
    int mCameraEffect=0;
    long mCurrentSSvalue=500000000;
    int mCurrentAutoFocus;
    Integer afStateRealTime;
    int mNumberofFaces;

    int mCurrentISOValue=200;
    double mCurrentFocusDistance=1;
    private float mMinFocusDistance;
    private float mMaxFocusDistance=2;
    private TextView mFocusTextView;

    private boolean supports_face_detection_mode_simple;
    private boolean isSupports_face_detection_mode_full;
    FaceDetector FaceDetector;
    String OFFtext="";
    String SIMPLEtext="";
    String FULLtext="";
    TextView mInfoTextView;
    private boolean mRawImageCaptureon=false;

    private boolean afstateBoolean=false;
    CheckBox  mRawCheckBox;
    boolean UnlockFocusSpecialBooleanCaptureon=true;


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
                    Integer mode =  captureResult.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
                    Face [] faces = captureResult.get(CaptureResult.STATISTICS_FACES);
                    if (faces != null && mode != null) {
                        //Log.e("tag", "faces:"+ faces.length + ", mode" + mode);


                    }




                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            if(!mUnlockFocus) {
                                if (!BooleanAutoFocusLock) {
                                    unLockFocus();
                                }
                            }
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "Autofocus locked", Toast.LENGTH_SHORT).show();



                            }
                            if (afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "Autofocus not locked!", Toast.LENGTH_SHORT).show();
                            }
                            if(faces.length==0){

                            }else{
                                Toast.makeText(getApplicationContext(), "Face(s) Detected", Toast.LENGTH_SHORT).show();
                            }


                            startStillCaptureRequest();




                            break;
                    }

                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)


                {
                    mCurrentFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
                    mCurrentISOValue = result.get(CaptureResult.SENSOR_SENSITIVITY);
                    mCurrentSSvalue = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);

                    //Trying to implement facial recognition

                    super.onCaptureCompleted(session, request, result);
                    mCaptureResult = result;
                    process(result);

                    //

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
    private static final String[] VIDEO_PERMISSIONS = {
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


        } catch (CameraAccessException e) {
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
            for (Size option : choices) {
                if (option.getHeight() * option.getWidth() <= height * width) {
                    return option;
                }
            }
            return choices[0];

        }
    }


    //onCreate was here since the start





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_camera2_video_image);






        createVideoFolder();
        createImageFolder();
        mInfoTextView = (TextView)findViewById(R.id.infotextView2);


        mFocusTextView = (TextView)findViewById(R.id.infoTextView);
        //we have to create a new thread in order to get real time info from ISO SS adn Aperature
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override

                            public void run() {
                                String convertSS;


                                if (1000000000 / mCurrentSSvalue <= 1) {
                                    convertSS = String.valueOf(mCurrentSSvalue / 1000000000);
                                } else {
                                    convertSS = "1/" + String.valueOf(1000000000 / mCurrentSSvalue);
                                }
                                if (1 / mCurrentFocusDistance < 1 / mMaxFocusDistance - 0.1) {
                                    mInfoTextView.setText("ISO: " + mCurrentISOValue + "\t\t" + "Shutter Speed:" + convertSS + "\t\t\t\t" + "Focus Distance: " + String.format("%.2f", 100 / mCurrentFocusDistance) + " cm"  + "Faces Detected:" +
                                            mNumberofFaces

                                    );
                                } else {
                                    mInfoTextView.setText("ISO: " + mCurrentISOValue + "\t\t" + "Shutter Speed: " + convertSS + "\t\t\t\t\t" + "Focus Distance: " + "INFINITE"
                                            + "Faces Detected:" + mNumberofFaces
                                    ); // this action have to be in UI thread
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        })).start();





        Intent intent = getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)) {
            mRequestingAppUri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        }
        mMediaRecorder = new MediaRecorder();
        //mIsAuto2=false;
        //this is new
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mStillImageButton = (ImageButton) findViewById(R.id.CameraButton);
        mStillImageButton.setImageResource(R.mipmap.campic);
        mTimeInterval = (TextView) findViewById(R.id.TimeIntervalDisplay);

        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        mFlipCamera = (ImageButton) findViewById(R.id.FlipButton);

        mFlashButtonOnOff = (ImageButton) findViewById(R.id.FlashButton);
        mFlashButtonOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Toast.makeText(getApplicationContext(), "Flash", Toast.LENGTH_SHORT).show();
                PopupMenu popMenu2 = new PopupMenu(MainActivity.this, mFlashButtonOnOff);

                popMenu2.inflate(R.menu.flash_popup_menu);
                popMenu2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int position2 = item.getItemId();
                        switch (position2) {
                            case R.id.FlashOff:
                                mFlashButtonOnOff.setImageResource(R.drawable.ic_flash_off_black_24dp);
                                mFlashMode = 0;
                                startPreview();


                                break;
                            case R.id.FlashAuto:
                                mFlashButtonOnOff.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                                mFlashMode = 1;


                                break;
                            case R.id.FlashOn:
                                mFlashButtonOnOff.setImageResource(R.drawable.ic_flash_on_black_24dp);
                                mFlashMode = 2;
                                break;
                            case R.id.TorchOn:
                                mFlashButtonOnOff.setImageResource(R.drawable.ic_highlight_black_24dp);
                                mFlashMode = 3;
                                startPreview();
                                break;


                        }
                        return false;
                    }
                });

                popMenu2.show();


            }
        });


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
                } else {

                    FlipNumberBoolean = true;
                    FlipNumber = 1;
                    mFlipCamera.setImageResource(R.drawable.flipback);
                    closeCamera();
                    stopBackgroundThread();
                    startBackgroundThread();
                    if (mTextureView.isAvailable()) {
                        setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                        connectCamera();
                    } else {
                        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                    }


                }
            }
        });


        mSettingsbutton = (Button) findViewById(R.id.button);
        mAutobutton = (Button) findViewById(R.id.Auto);
        mAutobutton.setText("AUTO ON");
        mAutobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AutoNumber == 0) {
                    AutoNumber = 1;
                    Toast.makeText(getApplicationContext(), "AUTO OFF", Toast.LENGTH_SHORT).show();
                    mAutobutton.setText("AUTO OFF");

                } else if (AutoNumber == 1) {
                    AutoNumber = 0;
                    Toast.makeText(getApplicationContext(), "AUTO ON", Toast.LENGTH_SHORT).show();
                    mAutobutton.setText("AUTO ON");
                    startPreview();


                } else if (AutoNumber == 2) {
                    AutoNumber = 0;
                    Toast.makeText(getApplicationContext(), "SceneAutoOff", Toast.LENGTH_SHORT).show();
                    mAutobutton.setText("AUTO ON");
                    startPreview();

                }
            }

        });

        mAutobutton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AutoNumber = 2;

                Toast.makeText(getApplicationContext(), "AutoNumber now 2", Toast.LENGTH_SHORT).show();
                mAutobutton.setText("AUTO SCENE");
                startPreview();

                return true;
            }
        });



        mSettingsbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                menuonline = true;







                //Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, mSettingsbutton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                SubMenu sM = popupMenu.getMenu().addSubMenu(0, 100, 0, "Change Resolution:");
                SubMenu submenu2 = popupMenu.getMenu().addSubMenu(0,100, 0, "Available Effects");


                StreamConfigurationMap scmap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                final Size previewSizes[] = scmap.getOutputSizes(ImageFormat.JPEG);

                for (int i = 0; i < previewSizes.length; i++) {
                    sM.add(0, i + 200, 0, "" + previewSizes[i]);
                }


                final int[] SupportedSceneModes = new int[mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES).length];


                for (int i = 0; i < SupportedSceneModes.length; i++) {
                    SupportedSceneModes[i] = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)[i];

                }
                final int[] AvailableEffectsArray1=new int[mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS).length];
                for (int i=0; i<AvailableEffectsArray1.length; i++){
                    AvailableEffectsArray1[i]=(mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)[i]);
                }
                final String[] AvailableEffectsArray2=new String[AvailableEffectsArray1.length];
                for (int i=0; i<AvailableEffectsArray1.length; i++){
                    if(AvailableEffectsArray1[i]==0){
                        AvailableEffectsArray2[i]="OFF";
                    }
                    if(AvailableEffectsArray1[i]==1){
                        AvailableEffectsArray2[i]="Mono";
                    }
                    if(AvailableEffectsArray1[i]==2){
                        AvailableEffectsArray2[i]="Negative";
                    }
                    if(AvailableEffectsArray1[i]==3){
                        AvailableEffectsArray2[i]="Solarize";
                    }
                    if(AvailableEffectsArray1[i]==4){
                        AvailableEffectsArray2[i]="Sepia";
                    }
                    if(AvailableEffectsArray1[i]==5){
                        AvailableEffectsArray2[i]="Posterize";
                    }
                    if(AvailableEffectsArray1[i]==6){
                        AvailableEffectsArray2[i]="Whiteboard";
                    }
                    if(AvailableEffectsArray1[i]==7){
                        AvailableEffectsArray2[i]="Blackboard";
                    }
                    if(AvailableEffectsArray1[i]==8){
                        AvailableEffectsArray2[i]="Aqua";
                    }
                    submenu2.add(0, i + 100, 0, "" + AvailableEffectsArray2[i]);

                }


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //add settings
                        int position = item.getItemId();
                        for (int i = 0; i < previewSizes.length; i++) {
                            if (position == 200 + i) {
                                Toast.makeText(getApplicationContext(), "" + previewSizes[i], Toast.LENGTH_SHORT).show();
                                adjustAspectRatio(previewSizes[i].getHeight(), previewSizes[i].getWidth());
                                setupCamera(previewSizes[i].getHeight(), previewSizes[i].getWidth());
                                startPreview();

                            }
                        }
                        for (int i=0; i<AvailableEffectsArray2.length; i++){
                            if(position== 100+i) {
                                mCameraEffect = AvailableEffectsArray1[i];
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
                        if (ShutterSpeed2Double <= 1) {
                            double y = 1 / ShutterSpeed2Double;
                            ShutterSpeed2String = ("1" + "/" + (int) y);
                        } else {
                            double y = ShutterSpeed2Double;
                            ShutterSpeed2String = ("" + (int) y);

                        }
                        ShutterSpeed1String = ("1" + "/" + (int) x);
                        //since ShutterSpeed1 is usually a fraction anyways

                        mISOtext = (EditText) findViewById(R.id.ISOtext);
                        if (ISOvalue == 0) {
                            mISOtext.setText("ISO:AUTO");
                        }

                        final Range<Integer> ISOrange = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                        final int LowestISO = ISOrange.getLower();
                        final int HighestISO = ISOrange.getUpper();


                        mChangeFocusSeekBar = (SeekBar) findViewById(R.id.FocusChangeSeekBar);
                        mChangeFocusSeekBar.setMax((int) ((int) (1/mMaxFocusDistance - 1/mMinFocusDistance)/0.05));

                        mCloseALLbutton = (ImageButton) findViewById(R.id.CloseALLbutton);
                        mCloseALLbutton.setVisibility(View.VISIBLE);
                        mSeekbar = (SeekBar) findViewById(R.id.seekBar);
                        mISOseekbar = (SeekBar) findViewById(R.id.ISOseekbar);
                        mTextSeekBar = (EditText) findViewById(R.id.editText);
                        mMinimumShutterSpeed = (EditText) findViewById(R.id.MinimumShutterSpeed);
                        mMaximumShutterSpeed = (EditText) findViewById(R.id.MaximumShutterSpeed);


                        mCloseALLbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mISOtext.getVisibility() == View.VISIBLE) {
                                    mISOtext.setVisibility(View.INVISIBLE);
                                }
                                if(mFocusTextView.getVisibility()==View.VISIBLE){
                                    mFocusTextView.setVisibility(View.INVISIBLE);
                                }
                                if(mInfoTextView.getVisibility() == View.VISIBLE){
                                    mInfoTextView.setVisibility(View.INVISIBLE);
                                }

                                if (mSeekbar.getVisibility() == View.VISIBLE) {
                                    mSeekbar.setVisibility(View.INVISIBLE);
                                }
                                if (mISOseekbar.getVisibility() == View.VISIBLE) {
                                    mISOseekbar.setVisibility(View.INVISIBLE);
                                }
                                if (mTextSeekBar.getVisibility() == View.VISIBLE) {
                                    mTextSeekBar.setVisibility(View.INVISIBLE);
                                }
                                if (mMinimumShutterSpeed.getVisibility() == View.VISIBLE) {
                                    mMinimumShutterSpeed.setVisibility(View.INVISIBLE);
                                }
                                if (mMaximumShutterSpeed.getVisibility() == View.VISIBLE) {
                                    mMaximumShutterSpeed.setVisibility(View.INVISIBLE);
                                }
                                if (mChangeFocusSeekBar.getVisibility() == View.VISIBLE) {
                                    mChangeFocusSeekBar.setVisibility(View.INVISIBLE);
                                }
                                if (mTimeInterval.getVisibility() == View.VISIBLE) {
                                    mTimeInterval.setVisibility(View.INVISIBLE);
                                }
                                mCloseALLbutton.setVisibility(View.INVISIBLE);


                            }
                        });
                        //mRawCheckBox = (CheckBox) findViewById(R.id.RawInput);

                        switch (position) {
                            case R.id.LockAutoFocus:

                                if (!BooleanAutoFocusLock) {
                                    BooleanAutoFocusLock = true;
                                    Toast.makeText(getApplicationContext(), "AutoFocus lock Enabled", Toast.LENGTH_SHORT).show();
                                    //UnlockFocusSpecialBooleanCaptureon=true;



                                    startPreview();


                                } else if (BooleanAutoFocusLock) {
                                    BooleanAutoFocusLock = false;
                                    Toast.makeText(getApplicationContext(), "AutoFocus Unlock Enabled", Toast.LENGTH_SHORT).show();

                                    //mFocusTextView.setVisibility(View.INVISIBLE);


                                    UnlockFocusSpecialBooleanCaptureon=false;
                                    startPreview();
                                    /*

                                    unLockFocus();
                                    startPreview();*/

                                }
                                break;
                            case R.id.OpticalStabilizationInput:
                                if (BooleanOpticalStabilizationOn) {
                                    BooleanOpticalStabilizationOn = false;

                                    Toast.makeText(getApplicationContext(), "Optical Stabilization Disabled", Toast.LENGTH_SHORT).show();
                                } else if (!BooleanOpticalStabilizationOn) {
                                    BooleanOpticalStabilizationOn = true;
                                    Toast.makeText(getApplicationContext(), "Optical Stabilization Enabled", Toast.LENGTH_SHORT).show();

                                }

                                startPreview();


                                break;

                            case R.id.manualFocus:
                                if (!mUnlockFocus) {
                                    mUnlockFocus = true;
                                    mFocusTextView.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), "Manual Focus Activated", Toast.LENGTH_SHORT).show();

                                    startPreview();
                                } else if (mUnlockFocus) {
                                    mUnlockFocus = false;
                                    mFocusTextView.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(), "Auto Focus Enabled", Toast.LENGTH_SHORT).show();
                                    startPreview();
                                }
                                if (mChangeFocusSeekBar.getVisibility() == View.VISIBLE) {
                                    mChangeFocusSeekBar.setVisibility(View.INVISIBLE);
                                } else if (mChangeFocusSeekBar.getVisibility() == View.INVISIBLE) {

                                    mChangeFocusSeekBar.setVisibility(View.VISIBLE);
                                    mChangeFocusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                        @Override
                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                            mFocusDistance = (progress * 0.05);
                                            mFocusTextView.setText(String.format( "Focal Distance: "+"%.2f",mFocusDistance)+"m");
                                        }

                                        @Override
                                        public void onStartTrackingTouch(SeekBar seekBar) {

                                        }

                                        @Override
                                        public void onStopTrackingTouch(SeekBar seekBar) {
                                            startPreview();

                                        }
                                    });
                                }
                                //startPreview();
                                break;
                            case R.id.manualinputFocus:

                                //Toast.makeText(getApplicationContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                                LayoutInflater inflate5 = LayoutInflater.from(MainActivity.this);
                                View ThemanualinputView= inflate5.inflate(R.layout.manual_focus_input, null);
                                AlertDialog.Builder manualinputalert= new AlertDialog.Builder(MainActivity.this);
                                manualinputalert.setTitle("Manual Focus Input");
                                manualinputalert.setView(ThemanualinputView);
                                manualinputalert.setCancelable(true);
                                mManualFocusInput= (EditText) ThemanualinputView.findViewById(R.id.FocusEditText);
                                manualinputalert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                    }
                                });
                                manualinputalert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!mUnlockFocus) {
                                            mUnlockFocus = true;
                                            mFocusTextView.setVisibility(View.VISIBLE);
                                            Toast.makeText(getApplicationContext(), "Manual Focus Activated", Toast.LENGTH_SHORT).show();


                                        } else if (mUnlockFocus) {
                                            mUnlockFocus = false;
                                            mFocusTextView.setVisibility(View.INVISIBLE);
                                            Toast.makeText(getApplicationContext(), "Auto Focus Enabled", Toast.LENGTH_SHORT).show();

                                        }

                                        double TempManualFocusInput= Double.parseDouble(mManualFocusInput.getText().toString());
                                        mFocusTextView.setText(TempManualFocusInput+"");
                                        mFocusDistance=TempManualFocusInput;

                                    }
                                });
                                manualinputalert.show();
                                startPreview();





                                break;
                            case R.id.PhotoBurstInput:

                                final LayoutInflater inflate4 = LayoutInflater.from(MainActivity.this);
                                final View ThePhotoBurstView = inflate4.inflate(R.layout.photo_burst_input, null);
                                final AlertDialog.Builder PhotoBurstInputthing = new AlertDialog.Builder(MainActivity.this);
                                PhotoBurstInputthing.setTitle("Photo Burst Input:");
                                PhotoBurstInputthing.setView(ThePhotoBurstView);
                                PhotoBurstInputthing.setCancelable(true);
                                mPhotoBurstText = (EditText) ThePhotoBurstView.findViewById(R.id.PhotoBurstEditText);

                                mPhotoBurstLimitText = (EditText) ThePhotoBurstView.findViewById(R.id.PhotoBurstTimeLimitInputEditText);
                                PhotoBurstInputthing.setNegativeButton("Close", new DialogInterface.OnClickListener() {


                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                PhotoBurstInputthing.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int TempSecondInterval = Integer.parseInt(mPhotoBurstText.getText().toString());
                                        SecondStep = TempSecondInterval;
                                        if (mPhotoBurstLimitText.getText().toString().isEmpty()) {
                                            mPhotoTimeLimitNumber = 1;

                                        } else {
                                            mPhotoTimeLimitNumber = 0;
                                            int TempTimeLimit = Integer.parseInt(mPhotoBurstLimitText.getText().toString());
                                            PhotoBurstTimeStop = TempTimeLimit;
                                        }
                                    }
                                });

                                PhotoBurstInputthing.show();


                                break;
                            case R.id.VideoTimeLapseInput:
                                LayoutInflater inflate3 = LayoutInflater.from(MainActivity.this);
                                View TheVideoTimelapseview = inflate3.inflate(R.layout.video_timelapse_input, null);
                                AlertDialog.Builder VideoLapseInputthing = new AlertDialog.Builder(MainActivity.this);
                                VideoLapseInputthing.setTitle("Video timelapse Input:");
                                VideoLapseInputthing.setView(TheVideoTimelapseview);
                                VideoLapseInputthing.setCancelable(true);
                                mVideoTimelapse = (EditText) TheVideoTimelapseview.findViewById(R.id.VideoTimeLapseEditText);
                                VideoLapseInputthing.setNegativeButton("Close", new DialogInterface.OnClickListener() {


                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                VideoLapseInputthing.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int TempSecondInterval = Integer.parseInt(mVideoTimelapse.getText().toString());
                                        VideoTimelapsSecondStep = TempSecondInterval;


                                    }
                                });

                                VideoLapseInputthing.show();


                                break;

                            case R.id.WhiteBalanceCloudyDaylight:
                                mWBMode = CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceDaylight:
                                mWBMode = CONTROL_AWB_MODE_DAYLIGHT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceFluorescent:
                                mWBMode = CONTROL_AWB_MODE_FLUORESCENT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceShade:
                                mWBMode = CONTROL_AWB_MODE_SHADE;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceTwilight:
                                mWBMode = CONTROL_AWB_MODE_TWILIGHT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceWarmFluorescent:
                                mWBMode = CONTROL_AWB_MODE_WARM_FLUORESCENT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceIncandenscent:
                                mWBMode = CONTROL_AWB_MODE_INCANDESCENT;
                                startPreview();
                                break;
                            case R.id.WhiteBalanceAuto:
                                if (mWBMode != CONTROL_AWB_MODE_AUTO) {
                                    mWBMode = CONTROL_AWB_MODE_AUTO;
                                } else {
                                    Toast.makeText(getApplicationContext(), "AUTO is already on", Toast.LENGTH_SHORT).show();
                                }
                                startPreview();
                                break;
                            case R.id.ChangeISO:


                                mISOtext.setVisibility(View.VISIBLE);
                                break;

                            case R.id.ISO100:

                                //Toast.makeText(getApplicationContext(), "100 ISO", Toast.LENGTH_SHORT).show();
                                ISOvalue = 100;
                                mISOtext.setText("ISO:" + ISOvalue);

                                startPreview();
                                break;
                            case R.id.ISO200:
                                ISOvalue = 200;

                                mISOtext.setText("ISO:" + ISOvalue);
                                startPreview();
                                break;
                            case R.id.ISO400:
                                ISOvalue = 400;
                                mISOtext.setText("ISO:" + ISOvalue);
                                startPreview();

                                //Toast.makeText(getApplicationContext(), "400 ISO", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.ISO800:
                                ISOvalue = 800;
                                mISOtext.setText("ISO:" + ISOvalue);
                                startPreview();

                                //Toast.makeText(getApplicationContext(), "800", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.ISO1600:
                                ISOvalue = 1600;
                                mISOtext.setText("ISO:" + ISOvalue);
                                startPreview();
                                //Toast.makeText(getApplicationContext(), "1600", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.customISO:
                                Toast.makeText(getApplicationContext(), "Custom ISO", Toast.LENGTH_SHORT).show();

                                mISOseekbar.setVisibility(View.VISIBLE);

                                mISOseekbar.setMax((int) HighestISO - LowestISO);

                                mISOseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        progress = ISOprogressValue;
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                        mISOtext.setText("ISO:" + (mISOseekbar.getProgress() + LowestISO));
                                        ISOseekProgress = (mISOseekbar.getProgress() + LowestISO);
                                        ISOvalue = ISOseekProgress;
                                        startPreview();

                                    }
                                });

                                startPreview();
                                break;
                            case R.id.custominputISO:


                                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                final View subsubView = inflater.inflate(R.layout.manual_input_alertdialog, null);
                                final AlertDialog.Builder manualISODialog = new AlertDialog.Builder(MainActivity.this);


                                mISOEditText = (EditText) subsubView.findViewById(R.id.isoEditText);
                                mISOEditTextView = (TextView) subsubView.findViewById(R.id.isoTitle);
                                mISOEditTextView.setText("ISO Range:" + LowestISO + "to" + HighestISO);
                                manualISODialog.setTitle("Manual ISO Input");
                                manualISODialog.setView(subsubView);
                                manualISODialog.setCancelable(true);
                                manualISODialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {


                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                manualISODialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int tempISO = Integer.parseInt(mISOEditText.getText().toString());
                                        if (tempISO <= HighestISO && tempISO >= LowestISO) {
                                            ISOvalue = tempISO;
                                            mISOtext.setText("ISO:" + ISOvalue);
                                            startPreview();
                                            return;
                                        } else {
                                            Toast.makeText(getApplicationContext(), "ISO value is out of range", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                manualISODialog.show();

                                break;
                            case R.id.ChangeShutterSpeedSeek:

                                mSeekbar.setVisibility(View.VISIBLE);
                                mSeekbar.setProgress(progressValue);

                                mTextSeekBar.setVisibility(View.VISIBLE);
                                if (ShutterSpeed2Double < 1) {
                                    mSeekbar.setMax((int) (ShutterSpeed2 - ShutterSpeed1));
                                } else {
                                    //Working on a precision bar for camera's with higher shutter speed capacity
                                    Toast.makeText(getApplicationContext(), "Precision Option Available", Toast.LENGTH_SHORT).show();
                                    mSeekBar2 = (SeekBar) findViewById(R.id.seekBar2);
                                    mSeekBar2.setVisibility(View.VISIBLE);
                                    mSeekbar.setMax((int) Math.round(ShutterSpeed2Double));
                                    mTextSeekBar.setText("Shutter Speed(in s)");
                                }

                                //Note:The SeekBar can only take Interger Values. If ShutterSpeed2-ShutterSpeed1==0 then the ShutterSpeed difference is too great
                                //Integers can
                                //mSeekbar.setProgress(100000);

                                mMinimumShutterSpeed.setVisibility(View.VISIBLE);
                                mMinimumShutterSpeed.setText(ShutterSpeed1String);

                                mMaximumShutterSpeed.setVisibility(View.VISIBLE);
                                mMaximumShutterSpeed.setText(ShutterSpeed2String);
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
                                        mTextSeekBar.setText("Shutter Speed(in ns):" + (mSeekbar.getProgress() + ShutterSpeed1) + "/" + Math.round(mSeekbar.getMax() + ShutterSpeed1));
                                        Toast.makeText(getApplicationContext(), "Setting Shutter Speed", Toast.LENGTH_SHORT).show();
                                        ShutterSpeedValue = (mSeekbar.getProgress() + ShutterSpeed1);

                                        startPreview();
                                    }
                                });


                                break;
                            case R.id.ChangeShutterSpeedInput:


                                LayoutInflater inflater3 = LayoutInflater.from(MainActivity.this);
                                final View ChangeShutterSpeedView = inflater3.inflate(R.layout.shutterspeed_input_alertdialog, null);
                                final AlertDialog.Builder manualShutterSpeedDialog = new AlertDialog.Builder(MainActivity.this);
                                mShutterSpeedEditText = (EditText) ChangeShutterSpeedView.findViewById(R.id.ShutterSpeedEditText);
                                mShutterSpeedEditTextView = (TextView) ChangeShutterSpeedView.findViewById(R.id.ShutterSpeedTitle);
                                mShutterSpeedEditTextView.setText("ShutterSpeed Range: " + ShutterSpeed1 + " to " + ShutterSpeed2);
                                manualShutterSpeedDialog.setTitle("Manual Shutter Speed Input");
                                manualShutterSpeedDialog.setView(ChangeShutterSpeedView);
                                manualShutterSpeedDialog.setCancelable(true);
                                manualShutterSpeedDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {


                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });


                                manualShutterSpeedDialog.setPositiveButton("Confirm ", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int tempShutterSpeed = Integer.parseInt(mShutterSpeedEditText.getText().toString());
                                        if (tempShutterSpeed <= ShutterSpeed2 && tempShutterSpeed >= ShutterSpeed1) {
                                            ShutterSpeedValue = tempShutterSpeed;
                                            //.setText("ISO:"+ ISOvalue);
                                            startPreview();
                                            return;
                                        } else {
                                            Toast.makeText(getApplicationContext(), "ShutterSpeed value is out of range", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                manualShutterSpeedDialog.show();

                                break;
                            case R.id.ChangeShutterSpeedInput2:
                                LayoutInflater inflater4 = LayoutInflater.from(MainActivity.this);
                                final View ChangeShutterSpeedView2 = inflater4.inflate(R.layout.shutterspeed_input_alertdialog2, null);
                                final AlertDialog.Builder manualShutterSpeedDialog2 = new AlertDialog.Builder(MainActivity.this);
                                mShutterSpeedEditText2 = (EditText) ChangeShutterSpeedView2.findViewById(R.id.ShutterSpeedEditText2);
                                mShutterSpeedEditTextView2 = (TextView) ChangeShutterSpeedView2.findViewById(R.id.ShutterSpeedTitle2);
                                mShutterSpeedEditTextView2.setText("ShutterSpeed Range: " + ShutterSpeed1String + " to " + ShutterSpeed2String);
                                manualShutterSpeedDialog2.setTitle("Manual Shutter Speed Input");
                                manualShutterSpeedDialog2.setView(ChangeShutterSpeedView2);
                                manualShutterSpeedDialog2.setCancelable(true);
                                manualShutterSpeedDialog2.setNegativeButton("Close", new DialogInterface.OnClickListener() {


                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });


                                manualShutterSpeedDialog2.setPositiveButton("Confirm ", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String tempShutterSpeedString[] = new String[2];
                                        String tt;
                                        tt = mShutterSpeedEditText2.getText().toString();

                                        //make regex if statement here to satify numbers numbers greater than 1
                                        tempShutterSpeedString = tt.split("/");
                                        double tempShutterSpeed1 = Double.parseDouble(tempShutterSpeedString[0]);
                                        double tempShutterSpeed2 = Double.parseDouble(tempShutterSpeedString[1]);

                                        double tempShutterSpeed = ((tempShutterSpeed1 / tempShutterSpeed2) * 1000000000);
                                        if (tempShutterSpeed <= ShutterSpeed2 && tempShutterSpeed >= ShutterSpeed1) {
                                            ShutterSpeedValue = (long) tempShutterSpeed;
                                            //.setText("ISO:"+ ISOvalue);
                                            startPreview();
                                            return;
                                        } else {
                                            Toast.makeText(getApplicationContext(), "ShutterSpeed value is out of range", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                manualShutterSpeedDialog2.show();


                                break;

                            case R.id.getCameraInfo:
                                LayoutInflater inflater2 = LayoutInflater.from(MainActivity.this);
                                View cameraInfoSubView = inflater2.inflate(R.layout.camera_info_alertdialog, null);
                                mCameraInfoTextView = (TextView) cameraInfoSubView.findViewById(R.id.cameraInfoTextView);
                                mCameraInfoTextView.setText("Camera Resolutions;");
                                mCameraInfoTextView.setMovementMethod(new ScrollingMovementMethod());
                                mCameraInfoTextView2 = (TextView) cameraInfoSubView.findViewById(R.id.cameraInfoTextView2);
                                mCameraInfoTextView2.setText("Supported Camera Scenes:");
                                mCameraInfoTextView2.setMovementMethod(new ScrollingMovementMethod());
                                mCameraInfoTextView3 = (TextView) cameraInfoSubView.findViewById(R.id.cameraInfoTextView3);
                                mCameraInfoTextView3.setText("Supported Effects:");
                                mCameraInfoTextView3.setMovementMethod(new ScrollingMovementMethod());
                                mCameraInfoTextView4 = (TextView) cameraInfoSubView.findViewById(R.id.cameraInfoTextView4);
                                mCameraInfoTextView4.setText("Supported Face Detections");
                                mCameraInfoTextView4.setMovementMethod(new ScrollingMovementMethod());


                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Camera Information");
                                builder.setMessage("Shutter Speed Information(in s):" + ShutterSpeed1String + "-" + ShutterSpeed2String + "\n" + "ISO Range:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                                        + "\n" + "White Level:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_INFO_WHITE_LEVEL) + "\n" + "Sensor Physical Size: " + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                                        + "\n" + "Sensor Max Analog Sensitivity:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY)
                                        + "\n" + "Standard reference illuminant:" + mCameraCharacteristics.get(mCameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1)
                                        + "\n" + "Camera Compensation Range:" + mCameraCharacteristics.get(mCameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                                        + "\n" + "Flash Available: " + mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                                        + "\n" + "Supported Available Burst Capabilities:" + contains(mCameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES), CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE)

                                );
                                for (int i = 0; i < previewSizes.length; i++) {
                                    String oldTextView = mCameraInfoTextView.getText().toString();
                                    String newText = oldTextView + " , " + previewSizes[i] + ""; // can manipulate using substring also
                                    mCameraInfoTextView.setText(newText);
                                }
                                for (int i = 0; i < mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES).length; i++) {
                                    String oldTextView2 = mCameraInfoTextView2.getText().toString();
                                    String newText2 = oldTextView2 + "" + mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)[i] + " , ";
                                    mCameraInfoTextView2.setText(newText2);
                                }
                                for (int i=0; i< mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS).length; i++)
                                {
                                    String oldTextView3 = mCameraInfoTextView3.getText().toString();
                                    String newText3 = oldTextView3 + "" + mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)[i] + " , ";
                                    mCameraInfoTextView3.setText(newText3);
                                }
                                builder.setView(cameraInfoSubView);

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                String oldTextView4 = mCameraInfoTextView4.getText().toString();

                                mCameraInfoTextView4.setText(oldTextView4+ " "+ OFFtext+", "+SIMPLEtext+""+FULLtext+"" );



                                AlertDialog alertDialog2 = builder.create();
                                alertDialog2.show();
                                break;
                            case R.id.ChangeScene:
                                if (SupportedSceneModes[0] == 0) {
                                    Toast.makeText(getApplicationContext(), "No Supported Scene Modes", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case R.id.ChangeSceneDisabled:
                                mSceneMode = CONTROL_SCENE_MODE_DISABLED;
                                startPreview();
                                break;
                            case R.id.ChangeSceneFacePriority:
                                mSceneMode = CONTROL_SCENE_MODE_FACE_PRIORITY;
                                startPreview();
                                break;
                            case R.id.ChangeSceneAction:
                                mSceneMode = CONTROL_SCENE_MODE_ACTION;
                                startPreview();
                                break;
                            case R.id.ChangeSceneBarcode:
                                mSceneMode = 16;
                                startPreview();
                                break;
                            case R.id.ChangeSceneBeach:
                                mSceneMode = CONTROL_SCENE_MODE_BEACH;
                                startPreview();
                                break;
                            case R.id.ChangeSceneCandlelight:
                                mSceneMode = CONTROL_SCENE_MODE_CANDLELIGHT;
                                startPreview();
                                break;
                            case R.id.ChangeSceneFireworks:
                                mSceneMode = CONTROL_SCENE_MODE_FIREWORKS;
                                startPreview();
                                break;
                            case R.id.ChangeSceneHDR:
                                mSceneMode = CONTROL_SCENE_MODE_HDR;
                                startPreview();
                                break;
                            case R.id.ChangeSceneLandscape:
                                mSceneMode = CONTROL_SCENE_MODE_LANDSCAPE;
                                startPreview();
                                break;
                            case R.id.ChangeSceneNight:
                                mSceneMode = CONTROL_SCENE_MODE_NIGHT;
                                startPreview();
                                break;
                            case R.id.ChangeSceneNightPortrait:
                                mSceneMode = CONTROL_SCENE_MODE_NIGHT_PORTRAIT;
                                startPreview();
                                break;
                            case R.id.ChangeSceneParty:
                                mSceneMode = CONTROL_SCENE_MODE_PARTY;
                                startPreview();
                                break;
                            case R.id.ChangeScenePortrait:
                                mSceneMode = CONTROL_SCENE_MODE_PORTRAIT;
                                startPreview();
                                break;
                            case R.id.ChangeSceneSnow:
                                mSceneMode = CONTROL_SCENE_MODE_SNOW;
                                startPreview();
                                break;
                            case R.id.ChangeSceneSports:
                                mSceneMode = CONTROL_SCENE_MODE_SPORTS;
                                startPreview();
                                break;
                            case R.id.ChangeSceneSteadyphoto:
                                mSceneMode = CONTROL_SCENE_MODE_STEADYPHOTO;
                                startPreview();
                                break;
                            case R.id.ChangeSceneSunset:
                                mSceneMode = CONTROL_SCENE_MODE_SUNSET;
                                startPreview();
                                break;
                            case R.id.ChangeSceneTheatre:
                                mSceneMode = CONTROL_SCENE_MODE_THEATRE;
                                startPreview();
                                break;



                            case R.id.ShowRealTimeInfo:
                                if(mInfoTextView.getVisibility()==View.INVISIBLE){
                                    mInfoTextView.setVisibility(View.VISIBLE);
                                }else{
                                    mInfoTextView.setVisibility(View.INVISIBLE);
                                }
                                break;
                            case R.id.RawInput:
                                if(!mRawImageCaptureon){

                                    mRawImageCaptureon=true;
                                    Toast.makeText(getApplicationContext(), "Raw Capture Turned on", Toast.LENGTH_SHORT).show();


                                    //mRawCheckBox.setChecked(true);
                                }else{
                                    mRawImageCaptureon=false;
                                    Toast.makeText(getApplicationContext(), "Raw Capture Turned Off", Toast.LENGTH_SHORT).show();
                                }

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
                mStillImageButton.setImageResource(R.mipmap.campic);
                if (!mBurstOn) {
                    lockFocus();
                } else if (mBurstOn) {
                    //Toast.makeText(getApplicationContext(), "Burst Done", Toast.LENGTH_SHORT).show();
                    mBurstOn = false;
                }
                if (mChronometer.getVisibility() == View.VISIBLE) {
                    mTimeInterval.setVisibility(View.INVISIBLE);

                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                }
            }
        });
        mStillImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mStillImageButton.setImageResource(R.mipmap.btn_timelapse);
                mTimeInterval.setVisibility(View.VISIBLE);
                mTimeInterval.setText("Second Step:" + SecondStep);


                mBurstOn = true;
                Toast.makeText(getApplicationContext(), "Burst Started", Toast.LENGTH_SHORT).show();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
                mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        ChronoCount = ChronoCount + 1;
                        //chronometer.refreshDrawableState();
                        if (mPhotoTimeLimitNumber == 1) {


                            if (ChronoCount % SecondStep == 0) {
                                lockFocus();
                            }

                        } else if (mPhotoTimeLimitNumber == 0) {
                            if (ChronoCount == (PhotoBurstTimeStop)) {
                                if (ChronoCount % SecondStep == 0) {
                                    lockFocus();
                                }

                                mChronometer.stop();
                                mChronometer.setVisibility(View.INVISIBLE);
                                mStillImageButton.setImageResource(R.mipmap.campic);
                            } else {
                                if (ChronoCount % SecondStep == 0) {
                                    lockFocus();
                                }
                            }


                        }

                    }
                });


                //mStillImageButton.setImageResource(R.mipmap.campic);


                return true;
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
                    mTimeInterval.setVisibility(View.INVISIBLE);
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                    sendBroadcast(mediaStoreUpdateIntent);


                    startPreview();
                } else {
                    //mIsRecording = true;


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
                mTimeInterval.setVisibility(View.VISIBLE);
                mTimeInterval.setText("Pictures per Second" + VideoTimelapsSecondStep);
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



        for (int i =0; i < mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES).length; i++){


            if(mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)[i]==0){
                OFFtext =  "OFF";

            }
            if(mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)[i]==1) {
                SIMPLEtext = "SIMPLE";
                supports_face_detection_mode_simple=true;
            }
            if(mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)[i]==2)
            {
                FULLtext = "FULL";
                isSupports_face_detection_mode_full=true;
            }
            //String newText4 = oldTextView4 + "" + mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)[i]+ " , ";

        }

        try {

            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, mCameraEffect);
            mCaptureRequestBuilder.addTarget(previewSurface);

            if(supports_face_detection_mode_simple && isSupports_face_detection_mode_full==false){
                mCaptureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            }
            if(isSupports_face_detection_mode_full){
                mCaptureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);
            }



            if (BooleanOpticalStabilizationOn) {
                mCaptureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            } else if (!BooleanOpticalStabilizationOn) {
                mCaptureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
            }
            if (mUnlockFocus) {
                //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                mCaptureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) ((float) 1 / (float )mFocusDistance));
                //Toast.makeText(getApplicationContext(), "CONTROL AF OFF", Toast.LENGTH_SHORT).show();
            } else if (!mUnlockFocus) {
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                //Toast.makeText(getApplicationContext(), "CONTROL AF AUTO", Toast.LENGTH_SHORT).show();
            }
            if (AutoNumber == 0) {
                //AutoSettings

                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
            if (AutoNumber == 1) {
                //manual settings
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_MODE_OFF);

                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ShutterSpeedValue);
                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, ISOvalue);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mWBMode);


            }
            if (AutoNumber == 2) {

                //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_USE_SCENE_MODE);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, mSceneMode);
                Toast.makeText(getApplicationContext(), "mSceneModeNumber:" + mSceneMode, Toast.LENGTH_SHORT).show();

            } else if (AutoNumber == 3) {
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ShutterSpeedValue);
                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, ISOvalue);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mWBMode);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,mCameraEffect);

                //mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CaptureRequest )
            }
            if (mFlashMode == 0) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, FLASH_MODE_OFF);
            }
            if (mFlashMode == 3) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, FLASH_MODE_TORCH);
            }
            final CameraCaptureSession.CaptureCallback PreCaptureCall = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    mCurrentFocusDistance=result.get(CaptureResult.LENS_FOCUS_DISTANCE);
                    mCurrentISOValue=result.get(CaptureResult.SENSOR_SENSITIVITY);
                    mCurrentSSvalue=result.get(CaptureResult.SENSOR_EXPOSURE_TIME);


                    Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
                    Face [] faces = result.get(CaptureResult.STATISTICS_FACES);
                    mNumberofFaces=faces.length;

                }
            };



            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface(), mRawImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession session) {

                            mPreviewCaptureSession = session;

                            try {
                                if(supports_face_detection_mode_simple){
                                    if (isSupports_face_detection_mode_full) {
                                        mCaptureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);
                                    } else{
                                        mCaptureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                                    }
                                }

                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        PreCaptureCall, mBackgroundHandler);

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
        mMinFocusDistance = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        mMaxFocusDistance = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
        //mChangeFocusSeekBar.setMax((int) ((int) (1/mMaxFocusDistance - 1/mMinFocusDistance)/0.05));
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
            } else {
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
                        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE,mCameraEffect);

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
        if(AutoLocks==0) {
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
        AutoLocks = 1;
    }

    private void unLockFocus() {
        if (AutoLocks == 1) {

            try {
                //mCaptureState=STATE_WAIT_LOCK;

                mCaptureState = STATE_PREVIEW;


                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback,
                        mBackgroundHandler);
                //
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(),mPreviewCaptureCallback,
                        mBackgroundHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        AutoLocks=0;
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

            } else if(!mIsRecording||!mIsTimelapse) {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(
                        CameraDevice.TEMPLATE_STILL_CAPTURE);
            }


            if(mRawImageCaptureon){
                mCaptureRequestBuilder.addTarget(mRawImageReader.getSurface());
                mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            } else {

                mCaptureRequestBuilder.addTarget(mImageReader.getSurface());



            }


            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);
            if(mFlashMode==2){
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, FLASH_MODE_SINGLE);

            }


            //Testing Exposure Time
            //units nanoseconds

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);


                            try {
                                if(UnlockFocusSpecialBooleanCaptureon){
                                    createImageFileName(); //forImage
                                    if (mRawImageCaptureon) {
                                        createRawImageFileName(); //for RawImage
                                    }}
                                else{
                                    UnlockFocusSpecialBooleanCaptureon=true;
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

                            if (!BooleanAutoFocusLock){
                                unLockFocus();
                            }else {
                                AutoLocks = 0;
                            }



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
                    //mIsWritingImage=true;
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
                        {
                            if (!mUnlockFocus) {
                                if (!BooleanAutoFocusLock) {
                                    unLockFocus();
                                }
                            }
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "Autofocus locked", Toast.LENGTH_SHORT).show();

                            }


                            startStillCaptureRequest();





                        }

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
        mMediaRecorder.setCaptureRate(VideoTimelapsSecondStep);
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
    //ISO CHANGE
//ASpect Ratio stuff
    private static final String TAG = MainActivity.TAG;
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