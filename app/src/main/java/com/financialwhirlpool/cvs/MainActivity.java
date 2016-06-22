package com.financialwhirlpool.cvs;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.financialwhirlpool.cvs.Database.StoreDataSource;
import com.financialwhirlpool.cvs.fragment.MainFragment;
import com.google.android.gms.ads.MobileAds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {
    ProgressDialog progress;
    public static StoreDataSource dataSource;
    public static boolean largeScreen =false;
    getDatabase task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //content main
        Fragment fragment = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, fragment)
                .commit();

        if(findViewById(R.id.nav_view) == null){
            largeScreen=true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe
    public void onMessageEvent(MainFragment.MessageEvent event) {
        if (event.message.equals("Floating Button Clicked")) {
            Log.i("trido", "in floating button clciked");
            final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                {
                    drawerLayout.openDrawer(navigationView);
                }
            }
        }

        if (event.message.equals("Distance Data For List Ready")) {
            // Recylcer list for NAV
            VivzAdapter adapter = new VivzAdapter(this, MainFragment.stores);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.left_drawer);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            VivzAdapter.DividerItemDecoration itemDecoration = new VivzAdapter.DividerItemDecoration(this);
            recyclerView.addItemDecoration(itemDecoration);
        }
    }
    @Subscribe
    public void onEvent(VivzAdapter.MessageEvent event) {
        if(largeScreen == false) {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                {
                    drawerLayout.openDrawer(navigationView);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(largeScreen ==false) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            Toast.makeText(MainActivity.this, "LandScape", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        task = new getDatabase();
        task.execute();
        if (dataSource != null)
            dataSource.open();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        if (dataSource != null)
            dataSource.close();
        super.onStop();
    }

    // GET DATABASE FOR FIRST LAUNCH
    public class getDatabase extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show progress for getting database
            progress = ProgressDialog.show(MainActivity.this, "CVStore",
                    "Preparing data for launch", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            dataSource = new StoreDataSource(MainActivity.this);
            dataSource.open();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();
            MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        }
    }


}
