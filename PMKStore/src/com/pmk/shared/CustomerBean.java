/**
 * 
 */
package com.pmk.shared;

import java.math.BigDecimal;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author sarjith
 *
 */
public class CustomerBean implements IsSerializable, Suggestion {

	public CustomerBean() {
	}
	
	private int customerId;
	private String name;
	private String address;
	private String phone;
	private String customerCode;
	private BigDecimal openBalance;
	
	public BigDecimal getOpenBalance() {
		return openBalance;
	}
	public void setOpenBalance(BigDecimal openBalance) {
		this.openBalance = openBalance;
	}
	public String getCustomerCode() {
		return customerCode;
	}
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	@Override
	public String getDisplayString() {
		return name;
	}
	@Override
	public String getReplacementString() {
		return name;
	}
}
