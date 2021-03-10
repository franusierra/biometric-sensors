package com.example.medicalnotifications.utils;

import com.example.medicalnotifications.database.Alarm;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmConverter {
    public static Alarm jsonObjectToAlarm(JSONObject alarmJson,String alarmTopic) throws JSONException {

        Alarm.Type type=null;

        switch (alarmTopic){
            case "blood-oxygen":
                type=Alarm.Type.BLOOD_OXYGEN;
                break;
            case "heartbeat":
                type=Alarm.Type.HEARTBEAT;
                break;
            case "temperature":
                type=Alarm.Type.TEMPERATURE;
                break;
            default:
                type=Alarm.Type.UNKNOWN;
                break;
        }
        String patientID=alarmJson.has("patient-id") ? alarmJson.getString("patient-id") : null;
        Long alarmTime=alarmJson.has("alarm-time") ? alarmJson.getLong("alarm-time") : null;
        Double measuredValue=alarmJson.has("measured-value") ? alarmJson.getDouble("measured-value") : null;
        Double upperLimit=alarmJson.has("upper-limit") ? alarmJson.getDouble("upper-limit") : null;
        Double lowerLimit=alarmJson.has("lower-limit") ? alarmJson.getDouble("lower-limit") : null;
        Alarm a=new Alarm(
                patientID,
                alarmTime,
                measuredValue,
                upperLimit,
                lowerLimit,
                type
        );
        return a;
    }
}
