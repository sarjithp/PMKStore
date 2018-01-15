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
public class ProductBean implements IsSerializable, Suggestion {

	public ProductBean() {
	}
	
	private int productId;
	private String productCode;
	private String description;
	private Integer uomId;
	private BigDecimal salesPrice;
	private BigDecimal limitPrice;
	private BigDecimal purchasePrice;
	private int priceListId;
	
	public int getPriceListId() {
		return priceListId;
	}
	public void setPriceListId(int priceListId) {
		this.priceListId = priceListId;
	}
	public BigDecimal getPurchasePrice() {
		return purchasePrice;
	}
	public void setPurchasePrice(BigDecimal purchasePrice) {
		this.purchasePrice = purchasePrice;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String code) {
		this.productCode = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Integer getUomId() {
		return uomId;
	}
	public void setUomId(Integer uomId) {
		this.uomId = uomId;
	}
	public BigDecimal getSalesPrice() {
		return salesPrice;
	}
	public void setSalesPrice(BigDecimal salesPrice) {
		this.salesPrice = salesPrice;
	}
	public BigDecimal getLimitPrice() {
		return limitPrice;
	}
	public void setLimitPrice(BigDecimal limitPrice) {
		this.limitPrice = limitPrice;
	}
	@Override
	public String getDisplayString() {
		return description;
	}
	@Override
	public String getReplacementString() {
		return "";
	}
	
	
}
