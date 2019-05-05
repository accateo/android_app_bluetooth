package com.bluetooth.app;

/**
 * Created by Matteo on 14/06/2016.
 */
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import java.util.ArrayList;
import java.util.UUID;


public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";

    /**
     * Local reference to the device's BluetoothAdapter
     */
    private BluetoothAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private ArrayList<String> mStringList = new ArrayList<String>();

    ProgressBar progress;
    ProgressBar progressAssoc;
    TextView txtstatus;
    TextView textTrovato;
    TextView textAssociazione;
    ImageView buttonpress;
    TextView textVerifica;
    TextView textpress;



    int thread_ok=0;
    int other_pps=0;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();



            // When discovery finds a device
            if (action!=null && BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                if(intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)){
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
					//if name bluetooth device is what we're looking for
                    if(device.getName()!=null && deviceName.equals(getResources().getString(R.string.device_name)) ){


                        //device founded, change the text on layout
                        textpress.setVisibility(View.INVISIBLE);
                        buttonpress.setVisibility(View.INVISIBLE);
                        progressAssoc.setVisibility(View.INVISIBLE);
                        if(textAssociazione.getVisibility()==View.INVISIBLE) {
                            textTrovato.setVisibility(View.VISIBLE);
                        }

                        //association, I added a little delay
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    textTrovato.setVisibility(View.INVISIBLE);
                                    textAssociazione.setVisibility(View.VISIBLE);
                                }
                            }, 2000);
                            }
                        });

                        progress(true);

                        
						ConnectThread ct = new ConnectThread(device);
                        ct.start();
                        //stop thread that say "device not found"
                        thread_ok = 1;
                        



                    }
                }

            }


        }
    };


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_conf);

        setup();

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        startAssoc();

        //timeout 30 seconds, device not found
        runOnUiThread(new Runnable() {
            @Override
            public void run() {new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(thread_ok==0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_not_found), Toast.LENGTH_LONG).show();
                        BluetoothActivity.this.finish();
                        Bundle b;
                        b = getIntent().getExtras();
                        String source = b.getString("source");
                        if(source.equals("0")){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        else{
                            //device not found, exit
                            BluetoothActivity.this.finish();
                        }


                    }
                }
            }, 30000);
            }
        });



    }

    @Override
    public void onStart(){

        super.onStart();
   }

    @Override
    public void onPause() {
        if (mAdapter != null) {
            if (mAdapter.isDiscovering()) {
                mAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        thread_ok=1;

        finish();
        Intent deviceintent = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(deviceintent);
    }

    private void startAssoc(){

		//little delay before start discovery devices
        runOnUiThread(new Runnable() {
            @Override
            public void run() {new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.startDiscovery();

                }
            }, 2000);
            }
        });


    }


	//all the components of activity layout
    private void setup(){

        Bundle b = new Bundle();
        b = getIntent().getExtras();
        progress = (ProgressBar)findViewById(R.id.progressBarBluetooth);
        progressAssoc = (ProgressBar) findViewById(R.id.progressAssoc);
        txtstatus = (TextView) findViewById(R.id.txtstatus);
        textTrovato = (TextView) findViewById(R.id.textTrovato);
        textVerifica = (TextView) findViewById(R.id.textVerifica);
        buttonpress = (ImageView) findViewById(R.id.imagePress);
        textAssociazione = (TextView) findViewById(R.id.textAssoc);
        textpress = (TextView) findViewById(R.id.textPress);
        //Store a local reference to the BluetoothAdapter
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }




    }




	//manage loading progress bar on layout
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void progress(final boolean show){
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                progress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });



    }


	//main thread 
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;



            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code

                String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING_WELL_KNOWN_SPP));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mAdapter.cancelDiscovery();



            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                thread_ok=1;
            } catch (IOException connectException) {

                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

			//save socket for other activities
            BluetoothApplication.setBSocket(mmSocket);



            runOnUiThread(new Runnable() {
                @Override
                public void run() {new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Bundle b;
                        b = getIntent().getExtras();
						//add a "source" data in the intenr, so MainActivity can understand that the caller is BluetoothActivity
                        String source = b.getString("source");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
						intent.putExtra("from",0);
						startActivity(intent);
                        
                        
                    }
                }, 2000);
                }
            });







        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


}
