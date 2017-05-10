package com.yorku.mstew.camera2videoimage20;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)


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
        }//checked
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

    //checked
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            //Toast.makeText(this, "we done", Toast.LENGTH_LONG).show();
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    //checked
//Creating the camera device
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
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
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
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
    //checked
    //Getting Camera Id

    //doesn't work
    private String mCameraId;
    private int mTotalRotation;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult){
                    switch (mCaptureState){
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState=STATE_PREVIEW;
                            Integer afState=captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if(afState==CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED||afState==CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
                                Toast.makeText(getApplicationContext(), "Autofocus locked", Toast.LENGTH_SHORT).show();
                                startStillCaptureRequest();
                            }
                            break;
                    }

                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)


                {
                    super.onCaptureCompleted(session, request, result);
                    mCaptureResult=result;
                    process(result);
                }
            };


    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(!contains(cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)){
                    continue;
                }
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                /*Size largestImageSize=Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),new CompareSizeByArea()
                );
                Size largestRawImageSize=Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.RAW_SENSOR)),new CompareSizeByArea()
                );
                mImageReader=ImageReader.newInstance(largestImageSize.getWidth(),largestImageSize.getHeight(),ImageFormat.JPEG,1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);
                mRawImageReader=ImageReader.newInstance(largestRawImageSize.getWidth(),largestRawImageSize.getHeight(),ImageFormat.RAW_SENSOR,1);
                mRawImageReader.setOnImageAvailableListener(mOnRawImageAvailableListener,mBackgroundHandler);
                */

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


                mImageSize=chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                mImageReader=ImageReader.newInstance(mImageSize.getWidth(),mImageSize.getHeight(),ImageFormat.JPEG,1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);
                mRawImageSize=chooseOptimalSize(map.getOutputSizes(ImageFormat.RAW_SENSOR),rotatedWidth, rotatedHeight);
                mRawImageReader=ImageReader.newInstance(mRawImageSize.getWidth(),mRawImageSize.getHeight(),ImageFormat.RAW_SENSOR,1);
                mRawImageReader.setOnImageAvailableListener(mOnRawImageAvailableListener,mBackgroundHandler);

                mCameraId = cameraId;
                mCameraCharacteristics=cameraCharacteristics;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "Video app needs access to camera", Toast.LENGTH_SHORT).show();
                    }

                    requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }
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
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
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
            return choices[0];

        }
    }

    //onCreate was here since the start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createVideoFolder();
        createImageFolder();
        mMediaRecorder=new MediaRecorder();
        //this is new
        mChronometer=(Chronometer) findViewById(R.id.chronometer);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mStillImageButton=(ImageButton)findViewById(R.id.CameraButton);
        mStillImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                lockFocus();
            }
        });
        mRecordImageButton = (ImageButton) findViewById(R.id.VideoButton);

        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording || mIsTimelapse) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;
                    mIsTimelapse=false;
                    mRecordImageButton.setImageResource(R.mipmap.vidpiconline);
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    Intent mediaStoreUpdateIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
                    sendBroadcast(mediaStoreUpdateIntent);


                    startPreview();
                } else {
                    mIsRecording = true;

                    //new
                    mRecordImageButton.setImageResource(R.mipmap.vidpicbusy);
                    checkWriteStoragePermission();

                }
            }

        });
        mRecordImageButton.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                mIsTimelapse=true;
                mRecordImageButton.setImageResource(R.mipmap.btn_timelapse);
                checkWriteStoragePermission();
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


            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface,mImageReader.getSurface(),
                    mRawImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mPreviewCaptureSession=session;
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
    private void checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "Recording Video", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


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
            } else {
                //Toast.makeText(this, "Permission to write is not granted", Toast.LENGTH_SHORT).show();
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();


                }
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {

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

    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(8000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }
    //checked

    //Capturing and Saving Videos
    private void startRecord() {
        try {
            if(mIsRecording){
                setupMediaRecorder();
            }else if(mIsTimelapse){
                setupTimelapse();
            }
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface,mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {


                        @Override
                        public void onConfigured(CameraCaptureSession session) {

                            mRecordCaptureSession=session;

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


    private Size mImageSize;
    private Size mRawImageSize;

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener(){
                @Override
                public void onImageAvailable(ImageReader reader){
                    mBackgroundHandler.post(new ImageSaver(/*mActivity,*/ reader.acquireLatestImage(),/*mUiHandler,*/mCaptureResult,mCameraCharacteristics));

                }
            };
    private ImageReader mRawImageReader;
    private final ImageReader.OnImageAvailableListener mOnRawImageAvailableListener =
            new ImageReader.OnImageAvailableListener(){
                @Override
                public void onImageAvailable(ImageReader reader){
                    mBackgroundHandler.post(new ImageSaver(/*mActivity,*/ reader.acquireLatestImage(),/*mUiHandler,*/mCaptureResult,mCameraCharacteristics));

                }
            };


    private static final int STATE_PREVIEW=0;
    private static final int STATE_WAIT_LOCK=1;
    private int mCaptureState = STATE_PREVIEW;
    private void lockFocus(){
        mCaptureState=STATE_WAIT_LOCK;

        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            if(mIsRecording)
            {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(),mRecordCaptureCallback,mBackgroundHandler);

            }else{
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(),mPreviewCaptureCallback,mBackgroundHandler);}
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
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile , "Camera2_Video_Image_JPEG");
        mRawGalleryFolder=new File(imageFile,"Camera2_Video_Image_RAW" );
        //check to see if the folder is already created
        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();

        }
        if (!mRawGalleryFolder.exists()){
            mRawGalleryFolder.mkdirs();
        }

    }

//now we have to call the videoFolder onCreate

    File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //there are two types of SimpleDateFormat and Date()
        String prepend = "JPEG_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;

    }
    File createRawImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //there are two types of SimpleDateFormat and Date()
        String prepend = "RAW_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".dng", mRawGalleryFolder);
        mRawImageFileName = imageFile.getAbsolutePath();
        return imageFile;

    }
    private void startStillCaptureRequest(){
        try {
            if(mIsRecording){
                mCaptureRequestBuilder=mCameraDevice.createCaptureRequest(
                        CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);

            }else{
                mCaptureRequestBuilder=mCameraDevice.createCaptureRequest(
                        CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.addTarget(mRawImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,mTotalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback=new
                    CameraCaptureSession.CaptureCallback(){
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);


                            try {
                                createImageFileName();
                                createRawImageFileName();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

            if(mIsRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            }else{
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);


            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private class ImageSaver implements Runnable {

        private final Image mImage;
        //private final Activity mActivity;
        //private final Handler mHandler;
        private final CaptureResult mCaptureResult;
        private final CameraCharacteristics mCameraCharacteristics;




        public ImageSaver(/*Activity activity,*/ Image image,/*Handler handler,*/ CaptureResult captureResult,
                          CameraCharacteristics cameraCharacteristics){
            mImage=image;

            //mActivity=activity;
            //mHandler=handler;
            mCaptureResult=captureResult;
            mCameraCharacteristics=cameraCharacteristics;

        }



        @Override
        public void run() {
            int format=mImage.getFormat();
            switch(format){
                case ImageFormat.JPEG:
                    ByteBuffer byteBuffer= mImage.getPlanes()[0].getBuffer();
                    byte[] bytes=new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    FileOutputStream fileOutputStream=null;
                    try {
                        fileOutputStream=new FileOutputStream(mImageFile);
                        fileOutputStream.write(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        mImage.close();

                        //notifying the media store
                        Intent mediaStoreUpdateIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName)));
                        sendBroadcast(mediaStoreUpdateIntent);

                        if(fileOutputStream != null){
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    break;
                case ImageFormat.RAW_SENSOR:
                    DngCreator dngCreator=new DngCreator(mCameraCharacteristics,mCaptureResult);
                    FileOutputStream rawFileOutputStream=null;
                    try {
                        rawFileOutputStream=new FileOutputStream(mRawImageFile);
                        dngCreator.writeImage(rawFileOutputStream,mImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        mImage.close();
                        if(rawFileOutputStream != null){
                            try {
                                rawFileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }



                    break;

            }



        }
    }
    //Part 18. Capturing a photo while recording
    private CameraCaptureSession mRecordCaptureSession;
    private CameraCaptureSession.CaptureCallback mRecordCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult){
                    switch (mCaptureState){
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState=STATE_PREVIEW;
                            Integer afState=captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if(afState==CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED||afState==CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
                                Toast.makeText(getApplicationContext(), "Autofocus locked", Toast.LENGTH_SHORT).show();
                                startStillCaptureRequest();
                            }
                            break;
                    }

                }
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)

                {
                    super.onCaptureCompleted(session, request, result);
                    process(result);
                    //unlockFocus();
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
        if(modes == null){
            return false;

        }
        for(int i:modes){
            if(i== mode ){
                return  true;
            }
        }
        return false;
    }
    //Create an Activity member for the raw folder
    private File mRawGalleryFolder;
    private String mRawImageFileName;

    //Create a file for the captured raw image
    private static File mRawImageFile;
    private static File mImageFile;

    private CameraCharacteristics mCameraCharacteristics;
    //RAW Image Capture part2
    private CaptureResult mCaptureResult;


}
