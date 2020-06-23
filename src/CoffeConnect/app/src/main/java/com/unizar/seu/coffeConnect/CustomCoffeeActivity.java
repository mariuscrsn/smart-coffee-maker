package com.unizar.seu.coffeConnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.unizar.seu.coffeConnect.model.Coffee;

public class CustomCoffeeActivity extends AppCompatActivity {

    public static  final String EXTRA_COFFEE = "com.example.seu.COFFEE";

    final int MIN_WATER = 20, MIN_AROMA=5;
    private Coffee c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_coffee);

        Intent intent = getIntent();
        // Aroma
        SeekBar seekBarAroma = findViewById(R.id.seekBarAroma);
        TextView txtAroma = findViewById(R.id.txtAroma);
        c = (Coffee) intent.getSerializableExtra(EXTRA_COFFEE);
        assert c != null;
        int aroma = c.getAroma();
        String aromaRes = aroma + " mg";
        txtAroma.setText(aromaRes);
        seekBarAroma.setProgress(aroma-MIN_AROMA);

        seekBarAroma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                c.setAroma(progress+MIN_AROMA);
                String aromaRes = c.getAroma() + " mg";
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
        int water = c.getWater();
        String waterVol = water + " ml";
        txtWater.setText(waterVol);
        seekBarWater.setProgress(water-MIN_WATER);

        seekBarWater.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                c.setWater(progress+MIN_WATER);
                String waterVol = c.getWater() + " ml";
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
        txtTemp.setText(c.getTempName());
        seekBarTemp.setProgress(c.getTemp());

        seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                c.setTemp(progress);
                Log.d("Custom", String.valueOf(progress));
                txtTemp.setText(c.getTempName());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void sendAction(View view, int n_caff){
        String msg = "N_coffes:" + n_caff + ",Aroma:" + c.getAroma() + ",Water:" + c.getWater() + ",Temp:" + c.getTemp();
        MainActivity.mqttHelper.publishMessage(msg);
        Log.i("Custom", "Preparing 1 coffee");
        saveConfig(view);
    }
    /** Called when the user taps the prepareX2 button */
    public void prepareTwo(View view) {
        sendAction(view, 2);
    }

    /** Called when the user taps the prepare button */
    public void prepareOne(View view) {
        sendAction(view, 1);
    }

    /** Called when the user taps the Save Config button */
    public void saveConfig(View view) {
        Log.i("Custom", "Saving config");
        Intent intent = new Intent(this, MainActivity.class); // TODO: change it
        intent.putExtra(EXTRA_COFFEE, c);
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