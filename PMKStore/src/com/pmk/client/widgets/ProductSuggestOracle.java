package com.pmk.client.widgets;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.pmk.client.PosService;
import com.pmk.client.PosServiceAsync;
import com.pmk.shared.ProductBean;

public class ProductSuggestOracle extends SuggestOracle {

	private static PosServiceAsync service = GWT.create(PosService.class);

	public ProductSuggestOracle() {
	}

	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		String text = request.getQuery();
		if (text.trim().length() > 2) {
			service.getProductSuggestions(text.trim(),
					new AsyncCallback<List<Suggestion>>() {
						@Override
						public void onFailure(Throwable caught) {

						}

						@Override
						public void onSuccess(List<Suggestion> result) {
							if (result.size() == 0) {
								ProductBean bean = new ProductBean();
								bean.setDescription("No records found for : "
												+ request.getQuery());
								result.add(bean);
							}

							Response resp = new Response(result);
							callback.onSuggestionsReady(request, resp);
						}
					});
		}
	}
}