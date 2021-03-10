package com.example.medicalnotifications.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDAO {
    @Query("SELECT * FROM alarm ORDER BY alarm_time DESC" +
            "")
    LiveData<List<Alarm>> getAll();

    @Insert
    void insert(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Query("SELECT * FROM alarm WHERE id=:id")
    Alarm getById(int id);

}
