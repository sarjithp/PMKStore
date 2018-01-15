package com.pmk.util;

public class TrxPrefix {

	private static int count = 0;

	public static synchronized String getPrefix() {
		count++;
		return "PMKStore" + count + "_" + System.currentTimeMillis();
	}
}
