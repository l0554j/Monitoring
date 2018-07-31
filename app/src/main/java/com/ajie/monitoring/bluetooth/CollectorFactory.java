package com.ajie.monitoring.bluetooth;


public class CollectorFactory {

	public static String makeCollector(byte[] data/*,int len*/) {
		data = cutOut(data);
		if(data.length<4){
			return null;
		}
		if(data[0] != Command.HEAD_FIRST || data[1] != Command.HEAD_SECOND){
			return null;
		}
		byte sendType = data[2];// 导联类型 B5F3 +01 +00+03 +11+F6+02+0D
		byte type = data[3];// 导联类型 B5F3 +01 +00+03 +11+F6+02+0D
		if(!isRight(/*cutOut(*/data/*)*//*,len*/)){
			return null;
		}
		if(sendType == Command.WRITE){
			switch (type) {//暂停  B5F30200010003  //充电  B5F30200010104   //预约时间  B5F3020102030F17 
			case Command.OPERATION://暂停  B5F30200010003  //充电  B5F30200010104
				return "";
//			break;
			case Command.APPOINTMENT://预约时间  B5F3020102030F17 
				return "";
//			break;
			}
		}else{
			switch (type) {
//		public static byte[] data01= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x00,(byte) 0x03,(byte) 0x11,(byte) 0xf6,(byte) 0x02,(byte) 0x0d};
			case Command.BATTERY_VOLTAGE://电池电压 B5F3 +01 +00+03 +11+F6+02+0D  值为 ： 45.98
				String vol = numerical(data);    //B5F301000314C802e2
				if(vol!=null&&vol.contains(".")&&vol.substring(vol.indexOf(".")+1, vol.length()).length()<2){
					vol = vol+"0";
				}
				return vol;
//			break;
//		public static byte[] data02= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x03,(byte) 0x03,(byte) 0x04,,(byte) 0x8f,(byte) 0x02,(byte) 0x9c};
			case Command.CHARGING_CURRENT://读取充电电流 B5F3 +01 +03 +03 +04+8f+02+9c   值为：11.67
				String current = numerical(data);
				if(current!=null&&current.contains(".")&&current.substring(current.indexOf(".")+1, current.length()).length()<2){
					current = current+"0";
				}
				return current;
//			break;
//		public static byte[] data03= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x06,(byte) 0x01,(byte) 0x53,(byte) 0x5b};
			case Command.CHARGING_PERCENTAGE://充电完成百分比 B5F3 +01 +06+01 +53+5b  值为：83
				return (data[data.length-2] & 0xff)+"";
//			break;
//		public static byte[] data05= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x07,(byte) 0x03,(byte) 0x11,(byte) 0x24,(byte) 0x35,(byte) 0x75};
			case Command.CHARGING_TIME://读取已经充电时间 B5F3 +01 +07+03 +11 +24 +35 +75  07为小时数，08为分钟数，09为秒数 值为： 17：36：53
				return (data[data.length-4] & 0xff)+","+(data[data.length-3] & 0xff)+","+(data[data.length-2] & 0xff);
//			break;
//		public static byte[] data04= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x0A,(byte) 0x02,(byte) 0x02,(byte) 0x24,(byte) 0x33};
			case Command.CHARGING_OVER://读取预计完成时间 B5F3 +01 +0A+02 +02 +24 +33 0A为小时数，0B为分钟数 值为：02：36
				return (data[data.length-3] & 0xff)+","+(data[data.length-2] & 0xff);
//			break;
//		public static byte[] data06= new byte[]{(byte) 0xb5,(byte) 0xf3,(byte) 0x01,(byte) 0x0C,(byte) 0x04,(byte) 0x07,(byte) 0xDE,(byte) 0x02,(byte) 0x15,(byte) 0x0d};
			case Command.PRODUCTION_DATE://读取生产日期  B5F3 +01 +0C+04 +07DE+02 +15 +0d    0C-0D为年份数，0E为月份，0F为日 值为：2014年02月21日
													//B5F3 01 0C 03 07DE 06 FB

//            	System.out.println("2: "+CommUtil.printHexString(data));
				return combination(data[data.length-5], data[data.length-4])+","+(data[data.length-3] & 0xff)+","+(data[data.length-2] & 0xff);
				
//			break;
			default:
				break;
			}
		}
		return null;
	}
	
	//检验
	public static String numerical(byte[] data){
		int value = combination(data[5],data[6]);
		int num = Integer.parseInt(data[7]+"", 16);
		if(num == 1){
			return value/10f+"";
		}else if(num == 2){
			return value/100f+"";
		}else if(num == 0){
			return value+"";
		}
		return null;
	}
	
	public static byte[] cutOut(byte[] data){//传来的数据有可能太长(点了两次获取，结果返回了两次)如：B5F30200010003B5F30200010103
		byte[] reltue = null ;
		boolean isFirst = true;
		boolean isTwo = true;
		int index = 0;
		for (int i = 0; i < data.length; i++) {
			if(Command.HEAD_FIRST == data[i]){
				if(i+1 < data.length && Command.HEAD_SECOND == data[i+1]){
					if(isFirst){
						isFirst = false;
					}else{
						if(isTwo){
							isTwo = false;
							index = i ;
						}else{
						}
					}
				}
			}
		}
		if(index != 0){
			reltue = new byte[index];
			for (int i = 0; i < index; i++) {
				reltue[i] = data[i];
			}
			return reltue;
		}else{
			return data;
		}
	}
	
	//检验
	public static boolean isRight(byte[] data/*,int len*/){
		byte sum = 0;//校验和  B5F3 +01 +00+03 +11+F6+02+0D
		for (int i = 2; i < data.length - 1; i++) {
			sum += data[i] & 0xff;
		}
		if(sum == data[data.length-1]){
			return true;
		}
		return false;
	}
	
	//俩字节的转换
	public static int combination(byte high,byte low){
		int data_low = (low & 0xff);
		int data_high = ((high & 0xff)<<8);
		return (data_high)|(data_low);
	}

	/** 字符转数组 */
	public static byte[] HexString2Bytes(String hexstr) {
		byte[] b = new byte[hexstr.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = hexstr.charAt(j++);
			char c1 = hexstr.charAt(j++);
			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
		}
		return b;
	}
	private static int parse(char c) {
		if (c >= 'a')
			return (c - 'a' + 10) & 0x0f;
		if (c >= 'A')
			return (c - 'A' + 10) & 0x0f;
		return (c - '0') & 0x0f;
	}
}
