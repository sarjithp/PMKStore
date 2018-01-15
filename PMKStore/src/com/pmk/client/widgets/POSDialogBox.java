/**
 * 
 */
package com.pmk.client.widgets;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author sarjith
 *
 */
public class POSDialogBox extends DialogBox {

	public POSDialogBox() {
		super();
	}
	
	
	@Override
	public void setVisible(boolean visible) {
		this.getElement().getStyle().setDisplay(visible ? Display.BLOCK : Display.NONE);
		super.setVisible(visible);
	}
	
	@Override
	public void center() {
		show();
		setVisible(true);
		super.center();
	}
	
	@Override
	public void hide() {
		super.hide();
		setVisible(false);
	}
}
