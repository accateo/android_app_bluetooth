package com.bluetooth.app;

import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Matteo on 14/05/2017.
 */

public class ConnectedThreadBT extends Thread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private static String mMessage_source;
    private String activitySource;
    private static Handler mHandler;


    public ConnectedThreadBT(BluetoothSocket socket, Handler handler, String activity_sourc) {
        mmSocket = socket;
        mHandler = handler;
        this.activitySource = activity_sourc;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {

        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
		
		//check the source of call
		//we send an "example" command, just to trigger the handler in MainActivity
		//i suppose a bluetooth command like this:
		//command: test_command=?\n
		//expected response: test_command=hello
		if(activitySource.equals("MainActivity")){
            String mex = "test_command=?\n";
            byte[] databyte = mex.getBytes();
            write(databyte, "TEST");
        }
		
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {

                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                String strReceived = new String(buffer, 0, bytes);
                final String msgReceived = strReceived;

				//check that my bluetooth device responds with some "test" string
                if (msgReceived.contains("test_command")) {
	
                    if(msgReceived.length()>=6){
                        
                        String dinfo = msgReceived;
                        mHandler.obtainMessage(MainActivity.INFO_DEVICE, dinfo).sendToTarget();
                    }

                }

                










                }

            } catch (IOException e) {

                break;
            }
        }


    }
	
	//i added a second parameter for helping understand message source
    public void write(byte[] bytes, String message_source) {
        mMessage_source = message_source;

        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {


        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }





}
