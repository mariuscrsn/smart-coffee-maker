package com.unizar.seu.coffeConnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static  final String EXTRA_AROMA = "com.example.seu.AROMA";
    public static  final String EXTRA_WATER = "com.example.seu.WATER";
    public static  final String EXTRA_TEMP = "com.example.seu.TEMP";
    String[] tempLevel = {"BAJA", "MEDIA", "ALTA"};
    final int MIN_WATER = 20, MIN_AROMA=5;
    int aroma = MIN_AROMA;
    int water = MIN_WATER;
    int temp = 0; // water index

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        // Aroma
        SeekBar seekBarAroma = findViewById(R.id.seekBarAroma);
        TextView txtAroma = findViewById(R.id.txtAroma);
        aroma = intent.getIntExtra(EXTRA_AROMA, MIN_AROMA);
        String aromaRes = aroma + " mg";
        txtAroma.setText(aromaRes);
        seekBarAroma.setProgress(aroma-MIN_AROMA);

        seekBarAroma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aroma = progress+MIN_AROMA;
                String aromaRes = aroma + " mg";
                Log.d("Custom", "Aroma: " + aromaRes);
                txtAroma.setText(aromaRes);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Water
        SeekBar seekBarWater = findViewById(R.id.seekBarWater);
        TextView txtWater = findViewById(R.id.txtWater);
        water = intent.getIntExtra(EXTRA_WATER, MIN_WATER);
        String waterVol = water + " ml";
        txtWater.setText(waterVol);
        seekBarWater.setProgress(water-MIN_WATER);

        seekBarWater.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                water = progress+MIN_WATER;
                String waterVol = water + " ml";
                Log.d("Custom", "Water: " + waterVol);
                txtWater.setText(waterVol);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Temperature
        SeekBar seekBarTemp = findViewById(R.id.seekBarTemp);
        TextView txtTemp = findViewById(R.id.txtTemp);
        temp = intent.getIntExtra(EXTRA_TEMP, 0);
        txtTemp.setText(tempLevel[temp]);
        seekBarTemp.setProgress(temp);

        seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                temp=progress;
                Log.d("Custom", String.valueOf(progress));
                txtTemp.setText(tempLevel[temp]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    /** Called when the user taps the prepareX2 button */
    public void prepareTwo(View view) {
        // TODO: send data to
        Log.i("Custom", "Preparing 2 coffees");
        saveConfig(view);
    }

    /** Called when the user taps the prepare button */
    public void prepareOne(View view) {
        // TODO: send data to
        Log.i("Custom", "Preparing 1 coffee");
        saveConfig(view);
    }

    /** Called when the user taps the Save Config button */
    public void saveConfig(View view) {
        Log.i("Custom", "Saving config");
        Intent intent = new Intent(this, MainActivity.class); // TODO: change it
        intent.putExtra(EXTRA_AROMA, aroma);
        intent.putExtra(EXTRA_WATER, water);
        intent.putExtra(EXTRA_TEMP, temp);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}