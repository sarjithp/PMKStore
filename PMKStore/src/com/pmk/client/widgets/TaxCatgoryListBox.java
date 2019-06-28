/**
 * 
 */
package com.pmk.client.widgets;

import java.math.BigDecimal;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.pmk.shared.TaxCategoryBean;

/**
 * @author sarjith
 *
 */
public class TaxCatgoryListBox extends KeyNamePairListBox {

	public TaxCatgoryListBox() {
		super("C_TaxCategory");
	}
	
	List<TaxCategoryBean> list = null;
	
	@Override
	public void refresh() {
		service.getTaxCategories(tableName,new AsyncCallback<List<TaxCategoryBean>>() {
			@Override
			public void onFailure(Throwable caught) {
				
			}
			@Override
			public void onSuccess(List<TaxCategoryBean> result) {
				list = result;
				for (TaxCategoryBean pair : result) {
					addItem(pair.getName(), String.valueOf(pair.getKey()));
				}
			}
		});
	}
	
	public BigDecimal getSelectedRate() {
		int index = getSelectedIndex();
		if (index >= 0 && list != null) {
			return list.get(index).getTaxRate();
		}
		return null;
	}
}
