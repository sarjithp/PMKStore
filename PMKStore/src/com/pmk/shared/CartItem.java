/**
 * 
 */
package com.pmk.shared;

import java.math.BigDecimal;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author sarjith
 *
 */
public class CartItem implements IsSerializable {

	public CartItem() {
	}
	
	private int productId;
	private String productName;
	private String description;
	private BigDecimal qtyOnHand;
	private BigDecimal qtyOrdered;
	private BigDecimal priceEntered;
	private String barcode;
	private String uom;
	private BigDecimal taxRate;
	private int orderLineId;
	
	public int getOrderLineId() {
		return orderLineId;
	}
	public void setOrderLineId(int orderLineId) {
		this.orderLineId = orderLineId;
	}
	public BigDecimal getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}
	public BigDecimal getPriceEntered() {
		return priceEntered;
	}
	public void setPriceEntered(BigDecimal priceEntered) {
		this.priceEntered = priceEntered;
	}
	public String getUom() {
		return uom;
	}
	public void setUom(String uom) {
		this.uom = uom;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getQtyOnHand() {
		return qtyOnHand;
	}
	public void setQtyOnHand(BigDecimal qtyOnHand) {
		this.qtyOnHand = qtyOnHand;
	}
	public BigDecimal getQtyOrdered() {
		return qtyOrdered;
	}
	public void setQtyOrdered(BigDecimal qtyOrdered) {
		this.qtyOrdered = qtyOrdered;
	}
	
	/**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<CartItem> SALE_KEY_PROVIDER = new ProvidesKey<CartItem>() {
      @Override
      public Object getKey(CartItem item) {
        return item == null ? null : item.getProductId();
      }
    };

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + productId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CartItem other = (CartItem) obj;
		if (productId != other.productId)
			return false;
		return true;
	}
}
