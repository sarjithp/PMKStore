/**
 * 
 */
package com.pmk.client.widgets;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author sarjith
 *
 */
public class IntegerListBox extends ListBox implements LeafValueEditor<Integer> {

	public IntegerListBox() {
		super();
	}
	
	@Override
	public void setValue(Integer value) {
		if (value == null) {
			setSelectedIndex(-1);
			return;
		}

		for (int i = 0; i < getItemCount(); i++) {
			if (getValue(i).equals(String.valueOf(value))) {
				setSelectedIndex(i);
				return;
			}
		}

	}

	@Override
	public Integer getValue() {
		if (getSelectedIndex() == -1)
			return -1;

		String value = getValue(getSelectedIndex());

		if (value != null) {
			return Integer.valueOf(value);
		}

		return -1;
	}
	
}
