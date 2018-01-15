package com.pmk.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class PMKStore implements EntryPoint {

	@Override
	public void onModuleLoad() {
		StorePOS pos = new StorePOS();
		RootPanel.get().add(pos);
	}

}
