package com.pmk.shared;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class OrderBean implements IsSerializable {

	public OrderBean() {
	}
	private int customerId;
	private String paymentType;
	private int priceListId;
	private String customerName;
	private String orderNo, invoiceNo;
	private String productDescription;
	private BigDecimal grandTotal;
	private int orderId;
	private Date dateOrdered;
	private int deliveryLocationId;
	private String deliveryAddress;
	private String description;
	private List<CartItem> lines;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<CartItem> getLines() {
		return lines;
	}
	public void setLines(List<CartItem> lines) {
		this.lines = lines;
	}
	public int getDeliveryLocationId() {
		return deliveryLocationId;
	}
	public void setDeliveryLocationId(int deliveryLocationId) {
		this.deliveryLocationId = deliveryLocationId;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public Date getDateOrdered() {
		return dateOrdered;
	}
	public void setDateOrdered(Date dateOrdered) {
		this.dateOrdered = dateOrdered;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public BigDecimal getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(BigDecimal grandTotal) {
		this.grandTotal = grandTotal;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public int getPriceListId() {
		return priceListId;
	}
	public void setPriceListId(int priceListId) {
		this.priceListId = priceListId;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	
	
}
