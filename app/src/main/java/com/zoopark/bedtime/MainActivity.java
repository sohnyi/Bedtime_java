package com.zoopark.bedtime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zoopark.lib.BedTimeDial;

public class MainActivity extends AppCompatActivity implements BedTimeDial.TimeChangedListener {

    private static final String TAG = "MainActivity";

    private TextView mSleepTv;
    private TextView mWeakUpTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSleepTv = (TextView) findViewById(R.id.sleep_tv);
        mWeakUpTv = (TextView) findViewById(R.id.weak_up_tv);

        mSleepTv.setText(getResources().getString(R.string.sleep_time_text, 23, 0));
        mWeakUpTv.setText(getResources().getString(R.string.getup_time_text, 8, 0));
    }

    @Override
    public void onSleepTimeChanged(int hr, int min) {
        mSleepTv.setText(getResources().getString(R.string.sleep_time_text, hr, min));
    }

    @Override
    public void onWeakUpTimeChanged(int hr, int min) {
        mWeakUpTv.setText(getResources().getString(R.string.getup_time_text, hr, min));
    }

    @Override
    public void onBedtimeChanged(int sleepHr, int sleepMin, int weakUpHr, int weakUpMin) {
        mSleepTv.setText(getResources().getString(R.string.sleep_time_text, sleepHr, sleepMin));
        mWeakUpTv.setText(getResources().getString(R.string.getup_time_text, weakUpHr, weakUpMin));
    }
}
