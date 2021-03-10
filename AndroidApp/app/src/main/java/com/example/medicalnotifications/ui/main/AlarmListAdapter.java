package com.example.medicalnotifications.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalnotifications.AlarmActivity;
import com.example.medicalnotifications.R;
import com.example.medicalnotifications.database.Alarm;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder> {
    public interface OnClickAlarmListener{
        void AlarmClicked(Alarm a);
    }
    private final OnClickAlarmListener mListener;
    private final LayoutInflater mInflater;
    private List<Alarm> mAlarms; // Cached copy of Alarms

    AlarmListAdapter(Context context,OnClickAlarmListener listener) {
        mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.alarm_recycler_view_item, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        if (mAlarms != null) {
            Alarm current = mAlarms.get(position);
            holder.setAlarm(current);
            String alarmType="";
            switch (current.getAlarm_typeEnum()){
                case BLOOD_OXYGEN:
                    alarmType="Oxígeno en sangre bajo";
                    break;
                case HEARTBEAT:
                    alarmType="Pulso peligroso";
                    break;
                case TEMPERATURE:
                    alarmType="Temperatura peligrosa";
                    break;
                default:
                    alarmType="Alarma desconocida";
                    break;

            }
            if(current.getAlarm_time()!=null) {
                DateFormat timestamp = SimpleDateFormat.getDateTimeInstance();
                Date date = new Date(current.getAlarm_time());

                holder.timestampTextView.setText(timestamp.format(date));
            }else{
                holder.timestampTextView.setText("");
            }
            holder.alarmTypeItemTextView.setText(alarmType);
            holder.patientIdItemTextView.setText("Paciente "+current.getPatient_id());
            DecimalFormat decimalFormat=new DecimalFormat("###.#");
            holder.measuredValueTextView.setText(decimalFormat.format(current.getMeasured_value()));
            holder.alarmItemImageView.setImageResource(AlarmActivity.getAlarmImageResource(current.getAlarm_typeEnum()));
        }
    }

    void setAlarms(List<Alarm> Alarms){
        mAlarms = Alarms;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mAlarms has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mAlarms != null)
            return mAlarms.size();
        else return 0;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView alarmTypeItemTextView;
        private final TextView patientIdItemTextView;
        private final TextView measuredValueTextView;
        private final TextView timestampTextView;
        private final ImageView alarmItemImageView;
        private Alarm alarm;

        public Alarm getAlarm() {
            return alarm;
        }

        public void setAlarm(Alarm alarm) {
            this.alarm = alarm;
        }

        private AlarmViewHolder(View itemView) {
            super(itemView);
            alarmTypeItemTextView = itemView.findViewById(R.id.alarmTypeTextView);
            patientIdItemTextView = itemView.findViewById(R.id.patientIdTextView);
            measuredValueTextView = itemView.findViewById(R.id.sensorValueTextview);
            timestampTextView = itemView.findViewById(R.id.alarmTimeTextView);
            alarmItemImageView=itemView.findViewById(R.id.alarmItemImageView);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v){
            mListener.AlarmClicked(alarm);
        }
    }
}