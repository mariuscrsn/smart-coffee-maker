package com.unizar.seu.coffeConnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.unizar.seu.coffeConnect.model.Coffee;
import com.unizar.seu.coffeConnect.model.CoffeeAdapter;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle abdt;
    private GridView gridView;
    private CoffeeAdapter adapter;

    public static Coffee[] rec_coffes = {
            new Coffee("Personalizado", 100, 20, "ALTA", R.drawable.personal),
            new Coffee("Espresso", 40, 10, "MEDIA", R.drawable.espresso),
            new Coffee("Largo", 200, 20, "MEDIA", R.drawable.largo),
            new Coffee("Capuchino", 80, 5, "MEDIA", R.drawable.cappuchino),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Grid
        gridView = (GridView) findViewById(R.id.recommend_grid);
        adapter = new CoffeeAdapter(this, rec_coffes);
        gridView.setAdapter(adapter);
//        gridView.setOnItemClickListener(this);

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
                        Intent i = new Intent(MainActivity.this ,CustomCoffeeActivity.class);
                        startActivity(i);
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

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}