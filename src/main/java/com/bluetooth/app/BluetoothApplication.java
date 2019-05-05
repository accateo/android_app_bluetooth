package com.bluetooth.app;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Matteo on 12/10/2016.
 */
//simple class that contain the bluetooth socket. useful to mantain the connection between activities
public class BluetoothApplication {

      private static BluetoothSocket bsocket;


     public static synchronized BluetoothSocket getBSocket(){

         return bsocket;


     }

    public static synchronized void setBSocket(BluetoothSocket socket){

        bsocket = socket;

    }





}
