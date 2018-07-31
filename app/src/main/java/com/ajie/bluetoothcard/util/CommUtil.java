package com.ajie.bluetoothcard.util;

public class CommUtil {
	/** 字符组转String */
	public static String printHexString( byte[] b /*,int len*/) {  
		String src = "";
		   for (int i = 0; i < b.length; i++) { 
		     String hex = Integer.toHexString(b[i] & 0xFF); 
		     if (hex.length() == 1) { 
		       hex = '0' + hex; 
		     } 
		     src+=hex.toUpperCase(); 
		   } 
		   return src;
		}
	
	public static byte[][] result(byte[] value){
		String[] valueString = printHexString(value).split("B5F3");
		byte one = (byte)0xb5;
		byte two = (byte)0xf3;
		int numOne = -1;
		int numTwo = -1;
		byte[][] resultArr = new byte[valueString.length-1][10];
		for (int i = 0; i < value.length; i++) {
			if(i+1 < value.length && value[i] == one && value[i+1] == two){
				numOne ++;
				numTwo = 0 ;
				resultArr[numOne] = new byte[valueString[numOne+1].length()/2+2];
			}else{
				numTwo++;
			}
			if(numOne != -1){
				resultArr[numOne][numTwo] = value[i];
			}
		}
		return resultArr;
	}
}
