package com.pmk.client.widgets;

import java.math.BigDecimal;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.ValueBox;

public class BigDecimalBox extends ValueBox<BigDecimal> {
	
	public BigDecimalBox() {
		super(Document.get().createTextInputElement(), BigDecimalRenderer.instance(),
				BigDecimalParser.instance());
		setStyleName("gwt-TextBox");
	}
}
