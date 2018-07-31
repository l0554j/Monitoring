package com.ajie.monitoring.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


import com.ajie.bluetoothcard.ui.MainActivity;
import com.ajie.bluetoothcard.util.CommUtil;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class WifiService {

	public static int RELAYOPT = 1;
	public static int RELAYCHK = 2;
	public static int RELAYSTATE = 3;
	public static int APPQUIT = 4;

	public static int CLOSETCP = 5;
	public static int MAINMENU = 6;
	public static int MESSAGE_READ = 7;

	protected Socket socket;// Socket 数据
	// 目标端口
	public boolean State;
	private byte[] sData = new byte[1024];// 接收缓存
	ReadThread readThread = null;
	Handler hOptMsg = null;
	Context mct = null;

	/**
	 * @param s
	 * @param ctrlHandle
	 */
	public WifiService(Handler hmsg, Context context) {
		socket = new Socket();
		hOptMsg = hmsg;
		mct = context;
	}

	public boolean sendData(byte[] data) throws IOException {
		if ((readThread == null) || (readThread.state == false)) {
//			RelayCtrlActivity.showMessage(mct.getString(R.string.msg5));
//			hOptMsg.stateCheck(0);
		  	hOptMsg.sendEmptyMessage(WifiService.CLOSETCP);
			return false;
		}
//		data = new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x00,(byte) 0x03,(byte) 0x11,(byte) 0xf6,(byte) 0x02,(byte) 0x0d};
		// TODO Auto-generated method stub
		OutputStream out = socket.getOutputStream();
		if (out == null)
			return false;
		out.write(data);
		return true;
	}

	public boolean stopConn() {
		State = false;
		if (readThread == null)
			return false;
		readThread.abortRead();
		return false;
	}

	public boolean startConn(String ip, int port) {
		if (socket.isClosed())
			socket = new Socket();
		SocketAddress remoteAddr = new InetSocketAddress(ip, port);
		try {
			socket.connect(remoteAddr, 2000);
		} catch (IOException e) {
			socket = new Socket();
			Log.v("tcpserver", e.getMessage());
			return false;
		}
		this.readThread = new ReadThread(hOptMsg, sData, socket);
		readThread.start();
		State = true;
		return true;
	}

//	public byte[] packageCmd(byte id, byte opt) {
//
//		if (id > 5)
//			return null;
//
//		byte[] cmd = new byte[] { 0x55, 0x01, 0x01, 0, 0, 0, 0, 0 };
//		if (id == 5) {
//			cmd[2] = 0;
//		} else if (id == 0) {
//			cmd[3] = opt;
//			cmd[4] = opt;
//			cmd[5] = opt;
//			cmd[6] = opt;
//		} else
//			cmd[2 + id] = opt;
//		cmd[7] = (byte) (cmd[0] + cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5] + cmd[6]);
//		return cmd;
//	}

//	public void sendrelayCmd(int id, int opt) {
//		byte[] cmd = packageCmd((byte) id, (byte) opt);
//		if (cmd == null)
//			return;
//		if ((readThread == null) || (readThread.state == false)) {
//			RelayCtrlActivity.showMessage(mct.getString(R.string.msg5));
////			hOptMsg.stateCheck(0);
//			return;
//		}
//		try {
//			sendData(cmd);
////			if (id != 5)
////				hOptMsg.stateCheck(2);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			// e.printStackTrace();
//			RelayCtrlActivity.showMessage(mct.getString(R.string.msg4));
//			// hOptMsg.sendEmptyMessage(DataProcess.CLOSETCP);
//		}
//	}
	public class ReadThread extends Thread {
		boolean state;
		Handler hOptMsg;
		byte[] sData;
		Socket socket;

		public ReadThread(Handler hmsg, byte[] sData, Socket socket) {
			hOptMsg = hmsg;
			this.sData = sData;
			this.socket = socket;
		}

		/*
		 * @see java.lang.Thread#run() 线程接收主循�?
		 */

		public void run() {
			int rlRead;
			state = true;
			byte[] command = null;
			try {
				while (state) {
					rlRead = socket.getInputStream().read(sData);// 对方断开返回-1
					
					System.out.println("------>>>    "+rlRead+"    "+printHexString(sData, rlRead));
					if (rlRead > 0) {
		                try {
                    		command = new byte[rlRead];
                    		for (int i = 0; i < rlRead; i++) {
                    			command[i] = sData[i];
                    		}
                    		byte[][] resultValue = CommUtil.result(command);
                    		for (int i = 0; i < resultValue.length; i++) {
                    			hOptMsg.obtainMessage(WifiService.MESSAGE_READ, rlRead, -1, resultValue[i]).sendToTarget();
							}
		                } catch (Exception e) {
		                    break;
		                }
		            
					} else {
						state = false;
						hOptMsg.sendEmptyMessage(WifiService.CLOSETCP);
						break;
					}
				}
			} catch (Exception e) {
				Log.v("tcpserver", e.getMessage());
				state = false;
				hOptMsg.sendEmptyMessage(WifiService.CLOSETCP);
			}
		}
		
		public String printHexString( byte[] b ,int len) {  
			String src = "";
			   for (int i = 0; i < len; i++) { 
			     String hex = Integer.toHexString(b[i] & 0xFF); 
			     if (hex.length() == 1) { 
			       hex = '0' + hex; 
			     } 
			     src+=hex.toUpperCase(); 
			   } 
			   return src;
			}
		
		public void abortRead() {
			if (socket == null)
				return;
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			state = false;
		}

	}
}
