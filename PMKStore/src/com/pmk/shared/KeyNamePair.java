package com.pmk.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class KeyNamePair implements IsSerializable {

	public KeyNamePair() {
	}
	
	public KeyNamePair(int key, String name) {
		this.key = key;
		this.name = name;
	}
	
	private int key;
	private String name;
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
