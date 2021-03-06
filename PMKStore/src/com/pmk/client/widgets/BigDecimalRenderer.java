package com.pmk.client.widgets;

import java.math.BigDecimal;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;

public class BigDecimalRenderer extends AbstractRenderer<BigDecimal> {

	private static BigDecimalRenderer INSTANCE;

	/**
	 * Returns the instance.
	 */
	public static Renderer<BigDecimal> instance() {
		if (INSTANCE == null) {
			INSTANCE = new BigDecimalRenderer();
		}
		return INSTANCE;
	}

	protected BigDecimalRenderer() {
	}

	public String render(BigDecimal object) {
		if (object == null) {
			return "";
		}

		return NumberFormat.getDecimalFormat().format(object);
	}
}
