package com.pfs.util;

import java.nio.ByteBuffer;

public class Converter {
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static byte[] xor(byte[] toXor, byte[] xor) {
		byte[] result = new byte[toXor.length];
		for (int i = 0; i < toXor.length; i++) {
			result[i] = (byte)(toXor[i] ^ xor[i % xor.length]);
		}
		return result;
	}
	public static String bytesToHex(byte[] bytes) {
		return bytesToHex(bytes, bytes.length);
	}

	public static String bytesToHex(byte[] bytes, int len) {
		char[] hexChars = new char[len * 2];
		for (int j = 0; j < len; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexToBytes(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len / 2 * 2; i += 2) {
			//[C#] data[i / 2] = unchecked((byte)(System.Int16.Parse(s.Substring(i, 2), System.Globalization.NumberStyles.HexNumber)));
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}


	/**
	 * 
	 * @param bytes
	 *            Must be 4 bytes.
	 * 
	 * @return
	 */
	public static int toInt(byte[] bytes) {
		ByteBuffer b = ByteBuffer.wrap(bytes);
		return b.getInt();
	}

	public static byte[] toByte(int n) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(n);
		return b.array();
	}
	

	/**
	 * Used for converting ip address to int.
	 * 
	 * @param bytes
	 * @return
	 */
	public static int toInt1(byte[] bytes) {
		int val = 0;
		for (int i = 0; i < bytes.length; i++) {
			val <<= 8;
			val |= bytes[i] & 0xff;
		}
		return val;
	}

}
