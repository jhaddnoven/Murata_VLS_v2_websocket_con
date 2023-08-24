package com.example.murata_vls;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mysql.jdbc.ResultSetMetaData;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

public class screenSaver extends AppCompatActivity {
    private ViewPager viewPager;
    private CarouselAdapter carouselAdapter;
    private Timer carouselTimer;
    private TimerTask carouselTimerTask;
    private int[] images = {
            R.drawable.untitled_1,
            R.drawable.robotics_2022,
            R.drawable.sfa_wp_img0002,
            R.drawable.sfa_wp_img0004
    };

    private int oneSec = 0;
    private int ticks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        viewPager = findViewById(R.id.viewPager);
        carouselAdapter = new CarouselAdapter(this, images);
        viewPager.setAdapter(carouselAdapter);


        Thread t = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(250);

                        runOnUiThread((new Runnable() {
                            @Override
                            public void run() {
                                //UI

                                Asyncronous async = new Asyncronous();
                                async.execute();

                                if(ticks >= 5){
                                    ticks = 0;

                                    int currentItem = viewPager.getCurrentItem();
                                    int nextItem = (currentItem + 1) % carouselAdapter.getCount();
                                    viewPager.setCurrentItem(nextItem, true);
                                }
                                oneSec ++;
                                if (oneSec > 4){
                                    oneSec = 0;
                                    ticks++;;
                                }
                            }
                        }));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t.start();
    }


    private class Asyncronous extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {


            try {
                String url = "jdbc:mysql://192.168.1.80:3306/basic_hris_database_default?characterEncoding=latin1";
                //url = "jdbc:mysql://192.168.1.129:3306/vue_app?characterEncoding=latin1";
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, "root", "G3AR-gr0up");
                String result = "";
                Statement st = con.createStatement();
                //ResultSet rs = st.executeQuery("SELECT emp_id,name,is_contract_expired FROM vue_app.employees WHERE emp_id = '" + Result + "';");
                ResultSet rs = st.executeQuery("SELECT idle_flag FROM murata_vls_db.device_remote ;");
                ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
                rs.next();
                Statement stmt = con.createStatement();
                String row1 = rs.getString(1);
                if(row1.equals("1")) {// add idle_flag on the database if 1. back to camera
                    finish();
                }

            } catch (SQLException e) {
                String j = e.toString();
            } catch (Exception e) {
                String j = e.toString();
            }



            return null;
        }

    }
}






