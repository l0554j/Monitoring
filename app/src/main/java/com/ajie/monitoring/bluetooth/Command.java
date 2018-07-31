package com.ajie.monitoring.bluetooth;

/**
 * 控制设备的指令，启动、停止
 * */
public class Command {

	// ----------------------命令包---------------------------
	/** 读取指令 */
	public final static byte COMMAND_READ = 0x01;
	/** 电池电压0x00开始3个字节  00-01为数据大小，02为小数点位置 */
	public final static byte BATTERY_VOLTAGE = 0x00;
	/** 读取充电电流0x03开始3个字节 03-05，03-04保存的是数值，05保存的是小数点，单位为A */
	public final static byte CHARGING_CURRENT = 0x03;
	/** 充电完成百分比0x06开始 1个字节 */
	public final static byte CHARGING_PERCENTAGE = 0x06;
	/** 读取已经充电时间0x07开始 3个字节 07-09，07为小时数，08为分钟数，09为秒数*/
	public final static byte CHARGING_TIME = 0x07;
	/** 读取预计完成时间0x0A开始 2个字节 0A-0B 0A为小时数，0B为分钟数*/
	public final static byte CHARGING_OVER = 0x0A;
	/** 读取生产日期0x0C开始 4个字节 0C-0F0C-0D为年份为小时数，0E为月份，0F为日*/
	public final static byte PRODUCTION_DATE = 0x0C;
	/** 写入指令 */
	public final static byte COMMAND_WRITE = 0x02;
	/** 写入一个指令 */
	public final static byte COMMAND_WRITE_ONE = 0x01;
	/** 写入两个指令 */
	public final static byte COMMAND_WRITE_TWO = 0x02;
	/** 充电按键 0x00 1个字节 00，写入1表示充电 ,写入0表示暂停*/
	public final static byte OPERATION = 0x00;
	/** 充电开始按键 0x01  写入1表示充电 */
	public final static byte OPERATION_START = 0x01;
	/** 充电暂停按键 0x00 写入0表示暂停*/
	public final static byte OPERATION_PAUSE = 0x00;
	/** 预约确定0x01 2个字节 01-02 表示写入预约小时与分钟 */
	public final static byte APPOINTMENT = 0x01;
	/** 包头第一个字节 */
	public final static byte HEAD_FIRST = (byte) 0xb5;
	/** 包头第二个字节 */
	public final static byte HEAD_SECOND = (byte) 0xf3;
	
	/** 读指令 */
	public final static int READ = 1;
	/** 写指令 */
	public final static int WRITE = 2;
	
	
	// ask------------------------------------
	/** 读取电池电压 */
	public static byte[] makeReadBatteryVoltage() {
		return makeACommandByte(READ,BATTERY_VOLTAGE, (byte)0x03);
	}
	/** 读取充电电流 */
	public static byte[] makeReadChargingCurrent() {
		return makeACommandByte(READ,CHARGING_CURRENT, (byte)0x03);
	}
	/** 读取充电完成百分比 */
	public static byte[] makeReadChargingPerentage() {
		return makeACommandByte(READ,CHARGING_PERCENTAGE, (byte)0x01);
	}
	/** 读取已经充电时间 */
	public static byte[] makeReadChargingTime() {
		return makeACommandByte(READ,CHARGING_TIME, (byte)0x03);
	}
	/** 预计完成时间 */
	public static byte[] makeReadChargingOver() {
		return makeACommandByte(READ,CHARGING_OVER, (byte)0x02);
	}
	/** 读取生产日期 */
	public static byte[] makeReadProductionDate() {
		return makeACommandByte(READ,PRODUCTION_DATE, (byte)0x04);
	}
	/** 充电开始按键 */
	public static byte[] makeWriteOperationStart() {
		return makeACommandByte(WRITE,OPERATION, OPERATION_START);
	}
	/** 充电暂停按键 */
	public static byte[] makeWriteOperationPause() {
		return makeACommandByte(WRITE,OPERATION, OPERATION_PAUSE);
	}
	/** 预约充电按键 */
	public static byte[] makeWriteOrderSet(byte hour, byte minute) {
		return makeWriteTimeCommandByte(hour,minute);
	}
	
	/**
	 * 先是读的指令，都是 01
	 * @param type 类型 1：读  2：写
	 * @param start 起始位置
	 * @param num 读取字节数
	 * @return
	 */
	static byte[] makeACommandByte(int type,byte start, byte num) {
		byte sum = 0;//校验和
		int struct_len = 6;// 固定结构长度为6 B5F3 +01 +06 + 01+08  包头俩个字节，读取或是写入一个字节，起始位一个字节，读取字节数一个字节，校验码一个字节
		if(WRITE == type){//B5F3 +02+00 + 01 + 00 +04
			struct_len = 7;
		}
		byte[] order = new byte[struct_len];
		order[0] = HEAD_FIRST;
		order[1] = HEAD_SECOND;
		if(READ == type){
			order[2] = COMMAND_READ;
			order[3] = start;
			order[4] = num;
		}else{
			order[2] = COMMAND_WRITE;
			order[3] = start;
			order[4] = COMMAND_WRITE_ONE;
			order[5] = num;
		}
		// 求和，包含长度值
		for (int i = 2; i < order.length - 1; i++) {
			sum += order[i] & 0xff;
		}
		if(READ == type){
			order[5] = sum;
		}else{
			order[6] = sum;
		}
		return order;
	}
	static byte[] makeWriteTimeCommandByte(byte hour, byte minute) {
		byte sum = 0;//校验和
		final int struct_len = 8;// 固定结构长度为6 B5F3 +02写命令 +01 + 02+ 01 + 01 +05  包头俩个字节，读取或是写入一个字节，起始位一个字节，读取字节数一个字节，校验码一个字节
		byte[] order = new byte[struct_len];
		order[0] = HEAD_FIRST;
		order[1] = HEAD_SECOND;
		order[2] = COMMAND_WRITE;
		order[3] = APPOINTMENT;
		order[4] = COMMAND_WRITE_TWO;
		order[5] = hour;
		order[6] = minute;
		// 求和，包含长度值
		for (int i = 2; i < order.length - 1; i++) {
			sum += order[i] & 0xff;
		}
		order[7] = sum;
		return order;
	}
	
	
	public static void test() {
		// FF 08 01 12 04 10 6E 11 07 B5 B5F3 +01 +06 + 01+08
		byte[] get = makeACommandByte(1,(byte) 0x01, (byte) 0x03);
		for (int i = 0; i < get.length; i++) {
		}
	}

	
	
}
