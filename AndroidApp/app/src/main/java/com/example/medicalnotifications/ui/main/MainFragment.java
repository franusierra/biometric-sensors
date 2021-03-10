package com.example.medicalnotifications.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.medicalnotifications.AlarmActivity;
import com.example.medicalnotifications.R;
import com.example.medicalnotifications.database.Alarm;

public class MainFragment extends Fragment {
    private AlarmListAdapter adapter;
    private MainViewModel mViewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.main_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.alarmsRecyclerView);
        adapter = new AlarmListAdapter(getContext(), new AlarmListAdapter.OnClickAlarmListener() {
            @Override
            public void AlarmClicked(Alarm a) {
                Log.d("MainFragment","Alarm was clicked: "+a.getId());
                Intent alarmIntent= AlarmActivity.getIntent(MainFragment.this.getContext(),a);
                startActivity(alarmIntent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new MainViewModelFactory(this.getContext())).get(MainViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.getAllAlarms().observe(getViewLifecycleOwner(),alarms -> {adapter.setAlarms(alarms);});
    }

}