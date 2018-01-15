/**
 * 
 */
package com.pmk.client.widgets;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.pmk.client.PosService;
import com.pmk.client.PosServiceAsync;
import com.pmk.shared.KeyNamePair;

/**
 * @author sarjith
 *
 */
public class UomListBox extends IntegerListBox {

	private static PosServiceAsync service = GWT.create(PosService.class);
	
	public UomListBox() {
		super();
	}
	
	public void refresh() {
		service.getUomList(new AsyncCallback<List<KeyNamePair>>() {
			@Override
			public void onFailure(Throwable caught) {
				
			}
			@Override
			public void onSuccess(List<KeyNamePair> result) {
				for (KeyNamePair pair : result) {
					addItem(pair.getName(), String.valueOf(pair.getKey()));
				}
			}
		});
	}
	
	
}
