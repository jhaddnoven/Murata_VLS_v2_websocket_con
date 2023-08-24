package com.example.murata_vls;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mysql.jdbc.ResultSetMetaData;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;

import io.socket.client.IO;
import io.socket.client.Socket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private String url;

    private Boolean cameraPause_flag = false;
    private Boolean isDisplayed = false;
    private Bitmap bitmap ;
    private Bitmap rotatedBitmap;
    public  RelativeLayout rlayout1;
    private String screenSaverFlag = "";
    private int oneSec = 0;
    private int ticks = 0;

    private ApiService apiService ;

    private WebSocketClient webSocketClient;
    private String deviceIPadd ;
    private String deviceName = "AVEERA1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rlayout1 = findViewById(R.id.rlayout);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        textureView.setRotation(180.0f);
        //deviceIPadd = IPAddressUtil.getIPAddress(this);

            // Call this method when a button is clicked

        //webSocketService.connectWebSocket();
        /*
        try {
            Socket socket = IO.socket("http://192.168.1.11:8000");
            socket.connect();


            socket.on("App\\Events\\cameraComandEvent", args -> {
                int postId = (int) args[0];
                // React to the event, e.g., fetch the updated data
            });

            Log.d("WebSocket", "Connected successfully");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("WebSocket", "Invalid URI: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("WebSocket", "General Exception: " + e.getMessage());
        }

         */



        //for websocket
        //reconnectToWebsocket();

        //makeLongPollingRequest();

        Thread t = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(250);
                        Asyncronous async = new Asyncronous();
                        async.execute();


                        oneSec ++;
                        if (oneSec >= 4){
                            oneSec = 0;
                            ticks++;
                        }
                        if(ticks >= 10){
                            ticks = 0;
                            BGidleTime();
                        }
                        // BG
                        runOnUiThread((new Runnable() {
                            @Override
                            public void run() {
                                //UI
                                captureTextureViewFrame();

                                updatePreview();
                            }
                        }));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();



    }
    private void reconnectToWebsocket(){
        try {
            URI uri = new URI("ws://192.168.1.177:3030");
            webSocketClient = new WebSocketClient(uri) {
                JSONObject connectMessage = new JSONObject();
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    /*
                    command: command,
                    devIpAdd:devlocalIp,
                    devicecsrf: devcsrf,
                    deviceName: devNamevar,
                    data:[guest_id,status,fullname,cameradev]

                     */
                    try {
                        connectMessage.put("command", "connectcam");
                        connectMessage.put("devIpAdd", "");
                        connectMessage.put("devicecsrf", "");
                        connectMessage.put("deviceName", deviceName);
                        connectMessage.put("data", "");
                        // Send the JSON message to the server
                        send(connectMessage.toString());

                        Log.d(TAG, "WebSocket connection opened");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject jsonObject = new JSONObject(message);

                        String receivedMessage = jsonObject.getString("command");
                        String clientcsrf = jsonObject.getString("devicecsrf");
                      switch(receivedMessage) {
                          case "capture":
                              cameraPause_flag = true;
                              sendCapturedFrameUsingWebSocket(rotatedBitmap, clientcsrf);
                              break;
                          case "recapture":
                              cameraPause_flag = false;
                              updatePreview();
                              break;
                          case "verify":
                              //Toast.makeText(MainActivity.this, " Mission Success", Toast.LENGTH_SHORT).show();

                              cameraPause_flag = false;
                              updatePreview();
                              break;
                          default:
                              break;
                      }
                        // Process the received JSON data
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket connection closed");

                    webSocketClient.close();

                    reconnectToWebsocket();
                    //webSocketClient.connect();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error: " + ex.getMessage());
                    //webSocketClient.connect();

                }
            };

            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private void sendJsonMessage() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the WebSocket connection when the activity is destroyed
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
    private void sendCapturedFrameUsingWebSocket(Bitmap frameBitmap, String csrf) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            // Convert the bitmap to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            frameBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();
            String imageDataBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);
            // Send the image data using WebSocket
            JSONObject message = new JSONObject();
            try {
                message.put("command", "send_frame");
                message.put("devIpAdd", "");
                message.put("devicecsrf", "");
                message.put("deviceName", deviceName);
                message.put("data", imageDataBase64);
                message.put("sendto",csrf);
                webSocketClient.send(message.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // WebSocket connection is not open, handle accordingly
        }
    }

    private void makeLongPollingRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.32:7050")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        Call<UpdateResponse> call = apiService.checkUpdates();
        call.enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(Call<UpdateResponse> call, retrofit2.Response<UpdateResponse> response) {
                if (response.isSuccessful()) {
                    UpdateResponse responseBody = response.body();
                    String get_ref =  responseBody.getRef_no();
                    boolean get_success = responseBody.isSuccess();
                    int get_cameraFlag = responseBody.getCameraFlag();
                    if(get_success){
                        if(get_cameraFlag == 1) {
                            cameraPause_flag = true;
                            Toast.makeText(MainActivity.this, " Mission Success:  Reference No = " + get_ref, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            cameraPause_flag = false;
                        }
                    }
                    else{
                      //  Toast.makeText(MainActivity.this, " Mission Successfully Failed", Toast.LENGTH_SHORT).show();
                    }
                    //makeLongPollingRequest();
                    // Initiate another long polling request
                } else {
                    //UpdateResponse responseBody = response.body();
                    //makeLongPollingRequest();
                }
            }
            @Override
            public void onFailure(Call<UpdateResponse> call, Throwable t) {
                //makeLongPollingRequest();
            }
        });
    }

    private void BGinsertBlob( String ref_no){
        try {
            url = "jdbc:mysql://192.168.1.82:3306/basic_hris_database_default?characterEncoding=latin1";
            //url = "jdbc:mysql://192.168.1.129:3306/vue_app?characterEncoding=latin1";
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, "root", "G3AR-gr0up");
            String result = "";
            Statement st = con.createStatement();
            //ResultSet rs = st.executeQuery("SELECT emp_id,name,is_contract_expired FROM vue_app.employees WHERE emp_id = '" + Result + "';");
            ResultSet rs = st.executeQuery("SELECT id FROM murata_vls_db.visitor_logs WHERE ref_no = '"+ref_no+"' ;");
            ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
            rs.next();
            Statement stmt = con.createStatement();
            int hasrow = rs.getRow();
            if (hasrow == 1) {
                //byte[] byteArray = stream.toByteArray();
                ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapOutputStream);
                byte[] bitmapBytes = bitmapOutputStream.toByteArray();
                InputStream inputStream = new ByteArrayInputStream(bitmapBytes);

                PreparedStatement pst;
                pst = con.prepareStatement("UPDATE  murata_vls_db.blob_tbl SET blob_data = ?  WHERE blob_ref_no = '"+ref_no+"' ");
                pst.setBinaryStream(1, inputStream);
                pst.executeUpdate();

            }

            con.close();
        } catch (SQLException e) {
            String j = e.toString();
        } catch (Exception e) {
            String j = e.toString();
        }
    }

    private void BGidleTime(){
        try {
            url = "jdbc:mysql://192.168.1.80:3306/basic_hris_database_default?characterEncoding=latin1";
            //url = "jdbc:mysql://192.168.1.129:3306/vue_app?characterEncoding=latin1";
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, "root", "G3AR-gr0up");
            String result = "";
            Statement st = con.createStatement();
            //ResultSet rs = st.executeQuery("SELECT emp_id,name,is_contract_expired FROM vue_app.employees WHERE emp_id = '" + Result + "';");
            ResultSet rs = st.executeQuery("SELECT idle_flag FROM murata_vls_db.device_remote ;");
            ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
            rs.next();
            Statement stmt = con.createStatement();
            screenSaverFlag = rs.getString(1);
            if(screenSaverFlag.equals("0")) {// add idle_flag on the database if 0. the idle clock will ticks to go to screensaver
                Intent intent = new Intent(MainActivity.this, screenSaver.class);
                startActivity(intent);
            }
            else
                ticks = 0;
            con.close();
        } catch (SQLException e) {
            String j = e.toString();
        } catch (Exception e) {
            String j = e.toString();
        }
    }

    private void BGwaitforSignal(){
        try {
            url = "jdbc:mysql://192.168.1.82:3306/basic_hris_database_default?characterEncoding=latin1";
            //url = "jdbc:mysql://192.168.1.129:3306/vue_app?characterEncoding=latin1";
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, "root", "G3AR-gr0up");
            String result = "";
            Statement st = con.createStatement();
            //ResultSet rs = st.executeQuery("SELECT emp_id,name,is_contract_expired FROM vue_app.employees WHERE emp_id = '" + Result + "';");
            ResultSet rs = st.executeQuery("SELECT camera_flag,ref_no FROM murata_vls_db.device_remote ;");
            ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
            rs.next();
            Statement stmt = con.createStatement();
            int row1 = rs.getInt(1);
            String row2 = rs.getString(2);

            if(row1 == 1){
                cameraPause_flag = true;
                BGinsertBlob(row2);
            }
            else if(row1 == 0){
                isDisplayed = false;
                cameraPause_flag = false;
            }
            con.close();
        } catch (SQLException e) {
            String j = e.toString();
        } catch (Exception e) {
            String j = e.toString();
        }
    }

    private void captureTextureViewFrame() {
        bitmap = textureView.getBitmap();
        float rotationDegrees = 180.0f; // The desired rotation angle in degrees

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {


           openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            // Handle texture size change if needed
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            updatePreview();
        }
    };

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0]; // Use the first available camera
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(cameraId, cameraStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        Surface surface = new Surface(texture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }

                    cameraCaptureSession = session;

                    // Rotate the camera preview by 180 degrees
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this, "Camera preview configuration failed.", Toast.LENGTH_SHORT).show();
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation) {
        // Calculate the orientation degrees based on the device rotation
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 180;
                break;
            case Surface.ROTATION_90:
                degrees = 270;
                break;
            case Surface.ROTATION_180:
                degrees = 0;
                break;
            case Surface.ROTATION_270:
                degrees = 90;
                break;
        }

        // Adjust the orientation based on the camera sensor orientation
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics;
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            degrees = (degrees + sensorOrientation + 270) % 360;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return degrees;
    }
    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }
        if (cameraPause_flag) {
            // Pause the camera preview
            try {
                cameraCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            // Resume or start the camera preview
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            try {
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                captureTextureViewFrame();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ticks = 0;
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    private class Asyncronous extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            //comment this when you enable websocket
            BGwaitforSignal();
            //makeLongPollingRequest();

            return null;
        }
    }
}


