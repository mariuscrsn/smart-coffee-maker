package com.unizar.seu.coffeConnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.unizar.seu.coffeConnect.MQTT.MQTTCallback;
import com.unizar.seu.coffeConnect.MQTT.MQTTHelper;
import com.unizar.seu.coffeConnect.hardware.MachineState;
import com.unizar.seu.coffeConnect.model.Coffee;
import com.unizar.seu.coffeConnect.model.CoffeeAdapter;
import com.unizar.seu.coffeConnect.hardware.MqttInfo;
import com.unizar.seu.coffeConnect.model.RecommendedCoffees;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle abdt;
    private GridView gridView;
    private CoffeeAdapter adapter;
    public static MQTTHelper mqttHelper;
    private TextView status;
    private MachineState state = MachineState.UNKNOWN;

    private class MyMqttCallback  extends MQTTCallback {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            super.messageArrived(topic, message);
            state = MachineState.valueOf(message.toString());
            executeMachineState();
        }
    }

    private void executeMachineState(){
        ImageView imgState = findViewById(R.id.imgState);
        ProgressBar load = findViewById(R.id.machine_busy);
        ImageButton btnStart = findViewById(R.id.btnTurnOn_Off);
        load.setVisibility(View.GONE);
        imgState.setVisibility(View.VISIBLE);
        imgState.setImageResource(R.drawable.ic_coffee_machine);
        switch (state) {
            case OFF:
                btnStart.setImageResource(R.drawable.turn_on);
                break;
            case READY:
                btnStart.setImageResource(R.drawable.turn_off);
                break;
            case TURNING_ON:
                btnStart.setImageResource(R.drawable.turn_off);
                load.setVisibility(View.VISIBLE);
                imgState.setVisibility(View.GONE);
                break;
            case TURNING_OFF:
                btnStart.setImageResource(R.drawable.turn_on);
                load.setVisibility(View.VISIBLE);
                imgState.setVisibility(View.GONE);
            case MAKING_ONE:
                imgState.setImageResource(R.drawable.one_coffee_cup);
                break;
            case MAKING_TWO:
                imgState.setImageResource(R.drawable.two_coffee_cup);
                break;
            case STEAM:
                imgState.setImageResource(R.drawable.ic_steam);
                break;
            case NO_WATER:
                imgState.setImageResource(R.drawable.ic_no_water);
                break;
            case CONTAINER_FULL:
                imgState.setImageResource(R.drawable.ic_container_full);
                break;
            case FAILURE:
                imgState.setImageResource(R.drawable.alert);
                break;
            case DECALCIF:
                imgState.setImageResource(R.drawable.ic_decalcif);
                break;
            default:
        }
        status.setText(state.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grid
        gridView = (GridView) findViewById(R.id.recommend_grid);
        adapter = new CoffeeAdapter(this, new RecommendedCoffees().getRecCoffees());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                if(state != MachineState.READY){
                    Toast.makeText(MainActivity.this, "Opción no disponible", Toast.LENGTH_SHORT).show();
                } else {
                    Coffee c = (Coffee) adapter.getItem(position);
                    Intent intent = new Intent(MainActivity.this, CustomCoffeeActivity.class);
                    intent.putExtra(CustomCoffeeActivity.EXTRA_COFFEE, c);
                    startActivity(intent);
                }
            }
        });

        // MQTT start service
        status = findViewById(R.id.txtStatus);
        executeMachineState();
        startMqtt();

        // Toolbar
        drawerLayout = findViewById(R.id.activity_main);
        abdt = new ActionBarDrawerToggle(this, drawerLayout,  R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);

        drawerLayout.addDrawerListener(abdt);
        abdt.syncState();

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        final NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.home_nav:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        closeOptionsMenu();
                        break;
                    case R.id.custom_nav:
                        if(state != MachineState.READY){
                            Toast.makeText(MainActivity.this, "Opción no disponible", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent i = new Intent(MainActivity.this, CustomCoffeeActivity.class);
                            Coffee c = (Coffee) adapter.getItem(0);
                            i.putExtra(CustomCoffeeActivity.EXTRA_COFFEE, c);
                            startActivity(i);
                        }
                        break;
                    case R.id.stats_nav:
                        Toast.makeText(MainActivity.this, "Stats", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.coffe_store_nav:
                        Toast.makeText(MainActivity.this, "Shop", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    private void startMqtt(){
        mqttHelper = new MQTTHelper(getApplicationContext(), new MqttInfo());
        mqttHelper.setCallback(new MyMqttCallback());
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        Coffee c = (Coffee) intent.getSerializableExtra(CustomCoffeeActivity.EXTRA_COFFEE);
        if (c != null) adapter.updatePersonalCoffee(c);
    }

    /** Called when the user taps the turnOn/Off button */
    public void onTurnOnOff(View view) {
        if(state == MachineState.OFF){
            mqttHelper.publishMessage("TURNING_ON");
        } else {
            mqttHelper.publishMessage("TURNING_OFF");
        }
    }
}