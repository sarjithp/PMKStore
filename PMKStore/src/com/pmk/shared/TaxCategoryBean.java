/**
 * 
 */
package com.pmk.shared;

import java.math.BigDecimal;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author sarjith
 *
 */
public class TaxCategoryBean extends KeyNamePair implements IsSerializable {

	public TaxCategoryBean() {
	}
	
	private BigDecimal taxRate;

	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}
	
	
}
