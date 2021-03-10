package com.example.medicalnotifications;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.medicalnotifications.database.Alarm;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AlarmActivity extends AppCompatActivity {
    public static final String TAG = "AlarmActivity";
    public static Intent getIntent(Context context, Alarm alarm){
        Log.d(TAG,"Lanzando actividad");
        Intent launchA = new Intent(context, AlarmActivity.class);
        launchA.putExtra("alarm", alarm);
        return launchA;
    }
    public static int getAlarmImageResource(Alarm.Type type){
        switch (type){
            case BLOOD_OXYGEN:
                return R.drawable.oxygen;
            case HEARTBEAT:
                return R.drawable.heartbeat;
            case TEMPERATURE:
                return R.drawable.temperatura;
            default:
                return R.drawable.error;
        }
    }
    private void setupViews(Alarm alarm){
        //Prepare view variables
        TextView patientTextView=findViewById(R.id.patientActivityTextView);
        TextView timestampTextView=findViewById(R.id.alarmDateActivityTextView);
        TextView sensorTextView=findViewById(R.id.sensorTypeActivityTextView);
        TextView measuredValueTextView=findViewById(R.id.measuredValueActivityTextView);
        TextView lowerLimitTextView=findViewById(R.id.lowerLimitValueTextView);
        TextView upperLimitTextView=findViewById(R.id.upperLimitValueTextView);
        ImageView sensorImageView=findViewById(R.id.alarmITypeActivityImageView);
        this.setTitle("");
        //Alarm image setup
        sensorImageView.setImageResource(getAlarmImageResource(alarm.getAlarm_typeEnum()));

        //Patient id setup
        String patientText=getString(R.string.patient_alarm_message,alarm.getPatient_id());
        patientTextView.setText(patientText);

        //Timestamp Setup
        String timestamp;
        if(alarm.getAlarm_time()!=null) {
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/M/yy hh:mm:ss");
            Date date = new Date(alarm.getAlarm_time());
            timestamp=dateFormat.format(date);
        }else{
            timestamp="N/A";
        }
        String timestampText=getString(R.string.date_alarm_message,timestamp);
        timestampTextView.setText(timestampText);

        //Sensor type setup
        String sensor="";
        switch (alarm.getAlarm_typeEnum()) {
            case BLOOD_OXYGEN:
                sensor = "Oxigeno en sangre";
                break;
            case TEMPERATURE:
                sensor = "Temperatura";
                break;
            case HEARTBEAT:
                sensor = "Pulso";
                break;
            case UNKNOWN:
                sensor = "Desconocido";
                break;
        }

        sensorTextView.setText(getString(R.string.sensor_type_message,sensor));

        DecimalFormat decimalFormat=new DecimalFormat("###.#");
        //Measured value setup
        if(alarm.getMeasured_value()!=null)
            measuredValueTextView.setText(decimalFormat.format(alarm.getMeasured_value()));
        else
            measuredValueTextView.setText("N/A");
        //Upper limit setup
        if(alarm.getUpper_limit()!=null)
            upperLimitTextView.setText(decimalFormat.format(alarm.getUpper_limit()));
        else
            upperLimitTextView.setText("N/A");
        //Lower limit setup
        if(alarm.getLower_limit()!=null)
            lowerLimitTextView.setText(decimalFormat.format(alarm.getLower_limit()));
        else
            lowerLimitTextView.setText("N/A");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);


        Bundle bundle = getIntent().getExtras();
        Alarm alarm = (Alarm) bundle.getSerializable("alarm");

        if(alarm != null){
            setupViews(alarm);
        }else{
            Log.d(TAG,"Alarm not loaded on activity");
        }
    }
}