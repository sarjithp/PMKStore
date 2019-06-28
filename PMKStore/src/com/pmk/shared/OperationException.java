/**
 * 
 */
package com.pmk.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author sarjith
 *
 */
public class OperationException extends Exception implements IsSerializable {

	public OperationException() {
	}
	
	public OperationException(String message) {
		super(message);
	}
}
