package com.example.medicalnotifications.ui.main;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.medicalnotifications.database.Alarm;
import com.example.medicalnotifications.database.AlarmDAO;
import com.example.medicalnotifications.database.DatabaseClient;

import java.util.List;

public class MainViewModel extends ViewModel {
    AlarmDAO mAlarmsDao;
    public MainViewModel(@NonNull Context application){

        mAlarmsDao= DatabaseClient.getInstance(application.getApplicationContext()).getAppDatabase().alarmDao();
    }
    LiveData<List<Alarm>> getAllAlarms() {
        return mAlarmsDao.getAll();
    }

}