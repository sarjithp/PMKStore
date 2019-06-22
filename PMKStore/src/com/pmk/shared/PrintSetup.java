package com.pmk.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PrintSetup implements IsSerializable {
	public PrintSetup() {
	}
	private String printDevice;
	private String printWidth;
	private String printType;
	
	public String getPrintType() {
		return printType;
	}
	public void setPrintType(String printType) {
		this.printType = printType;
	}
	public String getPrintDevice() {
		return printDevice;
	}
	public void setPrintDevice(String printDevice) {
		this.printDevice = printDevice;
	}
	public String getPrintWidth() {
		return printWidth;
	}
	public void setPrintWidth(String printWidth) {
		this.printWidth = printWidth;
	}
	
}
