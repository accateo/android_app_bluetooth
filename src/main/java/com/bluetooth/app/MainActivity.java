package com.bluetooth.app;


/**
 * Created by Matteo on 15/03/2017.
 */

public class MainActivity extends AppCompatActivity {

	private BluetoothAdapter mAdapter;
	
	public static final String MEDIA = "Device name";
	
	//constant for handler
	public static final int INFO_DEVICE = 2;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
		
		mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.enable();
		
		connectedSection();
        
    }

    @Override
    public void onStart(){

        super.onStart();
    }

    @Override
    public void onPause() {


        super.onPause();
    }


    @Override
    public void onBackPressed(){

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	private void connectedSection(){


            //check if device already bonded to the phone
            if(findBondedDeviceByName(mAdapter,MEDIA)) {
                //try to connect with 15 seconds timeout
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(connected==0) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, 15000);
                    }
                });

            }
            else{
                //in this case the user has to re-connect to the device

			}




    }
	//check if device already bonded to the phone
	private boolean findBondedDeviceByName (BluetoothAdapter adapter, String name) {
        for (BluetoothDevice device : getBondedDevices(adapter)) {
            if (name.matches(device.getName())) {
                System.out.println("Found device with name" + device.getName() + " and address " + device.getAddress());

                bsocket = BluetoothApplication.getBSocket();

                if(bsocket!=null) {
                    //check if the socket is active
                    if (bsocket.isConnected()) {
                        connected = 1;

                        cdbt=new ConnectedThreadBT(bsocket,mHandler, "MainActivity");
                        cdbt.start();

                    }
                }
                else{
                    return false;
                }

                return true;

            }
        }
        System.out.println("Unable to find device with name" + name);

        return false;

    }
	
	//bonded devices
    private static Set<BluetoothDevice> getBondedDevices (BluetoothAdapter adapter) {
        Set<BluetoothDevice> results = adapter.getBondedDevices();
        if (results == null) {
            results = new HashSet<BluetoothDevice>();
        }
        return results;
    }
	
	//with this handler we use data downloaded from the device. These data come from ConnectedThreadBT activity
	@SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case INFO_DEVICE:
					
					//from ConnectedThreadBT activity
                    String ddata =(String) msg.obj;
					
                    break;




            }
        }
    };

}
