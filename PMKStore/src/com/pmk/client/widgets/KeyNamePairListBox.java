/**
 * 
 */
package com.pmk.client.widgets;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.pmk.client.PosService;
import com.pmk.client.PosServiceAsync;
import com.pmk.shared.KeyNamePair;

/**
 * @author sarjith
 *
 */
public class KeyNamePairListBox extends IntegerListBox {

	public static PosServiceAsync service = GWT.create(PosService.class);
	
	String tableName = null;
	
	@UiConstructor
	public KeyNamePairListBox(String tableName) {
		super();
		this.tableName = tableName;
	}
	
	public void refresh() {
		service.getKeyNamePairList(tableName,new AsyncCallback<List<KeyNamePair>>() {
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
