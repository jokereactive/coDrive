package com.cdms.codrive.Startup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.cdms.codrive.R;

/**
 * Created by Danish Goel on 01-May-15.
 */
public class Splash extends ActionBarActivity

{
    LocationManager manager;
    boolean internet=false;

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("net", "on resume");
        ConnectivityManager conMgr  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if(info != null && info.isConnected())
        {
            internet=true;
            Log.d("net", "1net on");
            startLoginfragment();
        }
        else
        {
            Log.d("net", "net off");
            buildAlertMessageNoInternet();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        if(internet==true)
        {
            startLoginfragment();
        }
    }

    public void startLoginfragment()
    {
        Log.d("login","starting login fargment");
        Fragment mFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.view_fragment_container, mFragment).commit();
    }

    private void buildAlertMessageNoInternet()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Internet connection seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        dialog.cancel();
                        new AlertDialog.Builder(Splash.this)
                                .setTitle("Warning")
                                .setMessage("This App can't be used without Internet.\nClick OK to close.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
