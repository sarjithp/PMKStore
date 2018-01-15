package com.pmk.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SessionExpiredException extends Exception implements
		IsSerializable {
	public SessionExpiredException() {
	}

	public SessionExpiredException(String message) {
		super(message);
	}
}
