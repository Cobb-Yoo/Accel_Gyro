package com.example.accel_gyro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class MainActivity extends AppCompatActivity {
    private static final int WRITE_REQUEST_CODE = 43;
    private ParcelFileDescriptor pfd;
    private FileOutputStream fileOutputStream;
    TextView x,y,z;
    EditText nameText;
    Button toggleButton;
    String inputData;
    boolean flag;
    long start;
    File file;

    private SensorManager mSensorManager;
    //private Sensor mGyroscope;
    private Sensor accSensor;
    private SensorEventListener accLisn;
    private Object environment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init(){
        //센서 매니저 얻기
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //자이로스코프 센서(회전)
        //mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //엑셀러로미터 센서(가속)
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accLisn = new AccelerometerListner();

        x = (TextView)findViewById(R.id.textView1);
        y = (TextView)findViewById(R.id.textView2);
        z = (TextView)findViewById(R.id.textView3);
        nameText = (EditText)findViewById(R.id.nameText);

        toggleButton = (Button)findViewById(R.id.toggleButton);

        inputData = "";
        flag = true;
        start = System.currentTimeMillis();

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(flag) start();
                else end();
            }
        });
    }

    public void start(){
        toggleButton.setText("실험 끝");

        onResume();
        start = System.currentTimeMillis();

        flag = false;
    }

    public void end(){
        toggleButton.setText("실험 시작");

        onPause();

        pushingData(); //파일에 데이터 밀어 넣기
        flag = true;
        inputData = "";
    }

    private class AccelerometerListner implements SensorEventListener {
        //센서값 얻어오기
        @Override
        public void onSensorChanged(SensorEvent event) {
                    /*
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroX = Math.round(event.values[0] * 1000);
                gyroY = Math.round(event.values[1] * 1000);
                gyroZ = Math.round(event.values[2] * 1000);
                System.out.println("gyroX ="+gyroX);
                System.out.println("gyroY ="+gyroY);
                System.out.println("gyroZ ="+gyroZ);
            }

                     */
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                x.setText(String.valueOf(event.values[0]));
                y.setText(String.valueOf(event.values[1]));
                z.setText(String.valueOf(event.values[2]));

                inputData += String.valueOf(System.currentTimeMillis()-start) + " "
                        + String.valueOf(event.values[0]) + " "
                        + String.valueOf(event.values[1]) + " "
                        + String.valueOf(event.values[2]) + "\n";
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    // 주기 설명
    // SENSOR_DELAY_UI 갱신에 필요한 정도 주기
    // SENSOR_DELAY_NORMAL 화면 방향 전환 등의 일상적인  주기
    // SENSOR_DELAY_GAME 게임에 적합한 주기
    // SENSOR_DELAY_FASTEST 최대한의 빠른 주기
    public void pushingData(){
//Environment.DIRECTORY_DOWNLOADS 파일 경로
        String state=Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "SDcard is not mounted", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = nameText.getText() + ".txt";
        File[] dirs = getExternalFilesDirs(String.valueOf(nameText.getText()));
        File path = dirs[0];
        File file = new File(path, fileName);

        //if(!file.exists()) {
        //    file.mkdir();
        //}

        try{
            FileOutputStream fos= new FileOutputStream(file, true);
            PrintWriter writer= new PrintWriter(fos);

            writer.println(inputData);
            writer.flush();
            writer.close();

            Toast.makeText(this,"SAVED",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //y.setText(String.valueOf(dirs[0]));
    }

    //리스너 등록
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener((SensorEventListener) this, mGyroscope,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(accLisn, accSensor,SensorManager.SENSOR_DELAY_FASTEST);
    }

    //리스너 해제
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(accLisn);
    }
}