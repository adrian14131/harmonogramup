package pl.ozog.harmonogramup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity{


    ActionBar actionBar;
    TextView info;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        actionBar = getSupportActionBar();
        actionBar.hide();

        SharedPreferences sharedPreferences = getSharedPreferences("schedule",Context.MODE_PRIVATE);
        info = findViewById(R.id.splashInfoTextView);
        info.setVisibility(View.INVISIBLE);

        if(isOnline()){

            if(sharedPreferences.getAll().size()==0){
                info.setText("Brak wybranego kierunku.\nPoczekaj chwilę.");
                info.setVisibility(View.VISIBLE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Intent intent = new Intent(getApplicationContext(), FirstSettings.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);

                            finish();
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                },1000);
            }
            else{
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);

                            finish();
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                },1000);
            }
        }
        else{
            info.setText("Brak połączenia z internetem lub serwerem.");
            info.setVisibility(View.VISIBLE);
        }
    }

    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo!=null){
            return netInfo.isConnected() && netInfo.isAvailable();
        }
        return false;
    }
}
