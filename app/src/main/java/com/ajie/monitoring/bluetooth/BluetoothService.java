/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ajie.monitoring.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.ajie.bluetoothcard.ui.MainActivity;
import com.ajie.bluetoothcard.util.CommUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "MainActivity";

    // Unique UUID for this application
    //针对不同的服务,UUID会不同
//    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
 private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new MainActivity session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
//        public byte[] data01= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x00,(byte) 0x03,(byte) 0x01,(byte) 0x01,(byte) 0x02,(byte) 0x08};
//    	 public  byte[] data01= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x00,(byte) 0x03,(byte) 0x11,(byte) 0xf6,(byte) 0x02,(byte) 0x0d};
//        public  byte[] data02= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x03,(byte) 0x03,(byte) 0x04,(byte) 0x8f,(byte) 0x02,(byte) 0x9c};
//        public  byte[] data03= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x06,(byte) 0x01,(byte) 0x53,(byte) 0x5b};
//        public  byte[] data04= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x07,(byte) 0x03,(byte) 0x11,(byte) 0x24,(byte) 0x35,(byte) 0x75};
//        public  byte[] data05= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x0A,(byte) 0x02,(byte) 0x02,(byte) 0x24,(byte) 0x33};
//        public  byte[] data06= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x0C,(byte) 0x04,(byte) 0x07,(byte) 0xDE,(byte) 0x02,(byte) 0x15,(byte) 0x0d}; 
//        public  byte[] datatest= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x00,(byte) 0x03,(byte) 0x01,(byte) 0x00,(byte) 0x02,(byte) 0x07};
         public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
//            byte[] buffer = null;
            byte[] command = null;
//            byte[] commandSend = null;
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
//                	Thread.currentThread().sleep(500);//毫秒  延时，单片机的机制导致指令不能一次接收完成(大概这样)
//                	int count = 0; 
//                    while (count == 0) {  
//                        count = mmInStream.available();  
//                    }  
//                    buffer = new byte[count];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
//                    command = new byte[bytes];
//                    for (int i = 0; i < bytes; i++) {
//                    	command[i] = buffer[i];
//					}
                    if(bytes <= 2){//单片机的机制导致 ，设备返回我分两次，一次是B5(相当于是先握手) 一次是剩下的
//                    	System.out.println("指令较短");
//                    	commandSend = command;
//                    	command = new byte[bytes];
//                   	 for (int i = 0; i < bytes; i++) {
//                   		 command[i] = buffer[i];
//    					}
//                    	System.out.println("1: "+command[0]==Command.HEAD_FIRST+"   "+CommUtil.printHexString(command));
                    }else{
                    	if(buffer[0] != Command.HEAD_FIRST){//说明第一个指令不是B5
                    		command = new byte[bytes+1];
                    		command[0] = Command.HEAD_FIRST;
                    		for (int i = 0; i < bytes; i++) {
                    			command[i+1] = buffer[i];
                    		}
                    	}else{//说明指令正确
                    		command = new byte[bytes];
                    		for (int i = 0; i < bytes; i++) {
                    			command[i] = buffer[i];
                    		}
                    	}
//                    	System.out.println("2: "+CommUtil.printHexString(command));
                    	mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, command).sendToTarget();
                    }
//                    switch (buffer[3]) {
//            		case Command.BATTERY_VOLTAGE://电池电压
//            		     mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data01).sendToTarget();
//                    	break;
//            		case Command.CHARGING_CURRENT://读取充电电流
//           		     mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data02).sendToTarget();
//            			break;
//            		case Command.CHARGING_PERCENTAGE://充电完成百分比 B5F3 +01 +06+01 +53+59
//           		     mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data03).sendToTarget();
//            			break;
//            		case Command.CHARGING_TIME://读取已经充电时间 B5F3 +01 +07+03 +11 +24 +35 +75  07为小时数，08为分钟数，09为秒数
//           		     mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data04).sendToTarget();
//            			break;
//            		case Command.CHARGING_OVER://读取预计完成时间 B5F3 +01 +0A+02 +02 +24 +33 0A为小时数，0B为分钟数
//           		     mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data05).sendToTarget();
//            			break;
//            		case Command.PRODUCTION_DATE://读取生产日期  B5F3 +01 +0C+04 +07DE+02 +15 +0d 0C-0D为年份数，0E为月份，0F为日
//           		     mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data06).sendToTarget();
//            			break;
//            		default:
//            			break;
//            		}
//                    printHexString(buffer , bytes);
                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, data).sendToTarget();
                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }
//        public  byte[] printHexString( byte[] b,int num) { 
//        	byte[] arr = new byte[num];
//        	   for (int i = 0; i < num; i++) { 
//        	     String hex = Integer.toHexString(b[i] & 0xFF); 
//        	     if (hex.length() == 1) { 
//        	       hex = '0' + hex; 
//        	     } 
//        	   }
//			return b; 
//
//        	}
         /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
//        	 B5 F3 01 00 03 01 00 02 07
            try {
                mmOutStream.write(buffer);
//            	System.out.println("输出: "+CommUtil.printHexString(buffer));
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
