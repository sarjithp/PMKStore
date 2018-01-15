/**
 * 
 */
package com.pmk.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author sarjith
 *
 */
public class LoginUser implements IsSerializable {

	public LoginUser() {
	}
	
	private int userId;
	private String userName;
	private int priceListId;
	private int cashCustomerId;
	
	public int getPriceListId() {
		return priceListId;
	}
	public void setPriceListId(int priceListId) {
		this.priceListId = priceListId;
	}
	public int getCashCustomerId() {
		return cashCustomerId;
	}
	public void setCashCustomerId(int cashCustomerId) {
		this.cashCustomerId = cashCustomerId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
}
