/**
 * 
 */
package com.pmk.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
	private Integer uomId, categoryId, taxCategoryId;
	private BigDecimal salesPrice;
	private BigDecimal limitPrice;
	private BigDecimal purchasePrice;
	private int priceListId;
	private boolean createNewCategory;
	private String newCategoryName;
	private BigDecimal stockQty;
	private String uomSymbol;
	
	public String getUomSymbol() {
		return uomSymbol;
	}
	public void setUomSymbol(String uomSymbol) {
		this.uomSymbol = uomSymbol;
	}
	public BigDecimal getStockQty() {
		return stockQty;
	}
	public void setStockQty(BigDecimal stockQty) {
		this.stockQty = stockQty;
	}
	public boolean isCreateNewCategory() {
		return createNewCategory;
	}
	public void setCreateNewCategory(boolean createNewCategory) {
		this.createNewCategory = createNewCategory;
	}
	public String getNewCategoryName() {
		return newCategoryName;
	}
	public void setNewCategoryName(String newCategoryName) {
		this.newCategoryName = newCategoryName;
	}
	public Integer getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	public Integer getTaxCategoryId() {
		return taxCategoryId;
	}
	public void setTaxCategoryId(Integer taxCategoryId) {
		this.taxCategoryId = taxCategoryId;
	}
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
		String displayString = description;
		if (salesPrice != null) {
			displayString += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + salesPrice.setScale(2, RoundingMode.HALF_UP) + "/" + uomSymbol;
		}
		return displayString;
	}
	@Override
	public String getReplacementString() {
		return "";
	}
}
