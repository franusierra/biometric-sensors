package com.example.medicalnotifications.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
@Entity
public class Alarm implements Serializable {
    public enum Type{
        BLOOD_OXYGEN,
        HEARTBEAT,
        TEMPERATURE,
        UNKNOWN
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "patient_id")
    private String patient_id;
    @ColumnInfo(name = "alarm_time")
    private Long alarm_time;
    @ColumnInfo(name = "measured_value")
    private Double measured_value;
    @ColumnInfo(name = "upper_limit")
    private Double upper_limit;
    @ColumnInfo(name = "lower_limit")
    private Double lower_limit;
    @ColumnInfo(name = "alarm_type")
    private String alarm_type;

    public Alarm() {
    }

    public Alarm(String patient_id, Long alarm_time, Double measured_value, Double upper_limit, Double lower_limit, Type alarm_type) {
        this.patient_id = patient_id;
        this.alarm_time = alarm_time;
        this.measured_value = measured_value;
        this.upper_limit = upper_limit;
        this.lower_limit = lower_limit;
        this.alarm_type = alarm_type.name();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public Long getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(Long alarm_time) {
        this.alarm_time = alarm_time;
    }

    public Double getMeasured_value() {
        return measured_value;
    }

    public void setMeasured_value(Double measured_value) {
        this.measured_value = measured_value;
    }

    public Double getUpper_limit() {
        return upper_limit;
    }

    public void setUpper_limit(Double upper_limit) {
        this.upper_limit = upper_limit;
    }

    public Double getLower_limit() {
        return lower_limit;
    }

    public void setLower_limit(Double lower_limit) {
        this.lower_limit = lower_limit;
    }

    public void setAlarm_type(String alarm_type) {
        this.alarm_type = alarm_type;
    }
    public void setAlarm_type(Type alarm_type) {
        this.alarm_type = alarm_type.name();
    }

    public Type getAlarm_typeEnum() {
        return Type.valueOf(this.alarm_type);
    }
    public String getAlarm_type() {
        return this.alarm_type;
    }


}
