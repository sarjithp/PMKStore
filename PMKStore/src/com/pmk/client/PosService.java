package com.pmk.client;

import java.math.BigDecimal;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.pmk.shared.CartItem;
import com.pmk.shared.CustomerBean;
import com.pmk.shared.KeyNamePair;
import com.pmk.shared.LoginUser;
import com.pmk.shared.OperationException;
import com.pmk.shared.OrderBean;
import com.pmk.shared.ProductBean;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("posservice")
public interface PosService extends RemoteService {

	LoginUser getLoginUser(String userpin) throws OperationException;

	void saveProduct(ProductBean bean) throws OperationException;

	List<KeyNamePair> getUomList();

	CustomerBean loadCustomer(int salesCustomerId);

	void saveCustomer(CustomerBean bean) throws OperationException;

	void savePaymentDetails(int salesCustomerId, BigDecimal value) throws OperationException;

	ProductBean loadProduct(int productId, int priceListId);

	void completeOrder(List<CartItem> items, OrderBean order) throws OperationException;

	List<OrderBean> getSalesHistory(long timelong, String paymentType);

	List<Suggestion> getProductSuggestions(String trim);

	List<Suggestion> getCustomerSuggestions(String trim);

	CartItem addProductToCart(int productId, int priceListId, int customerID) throws OperationException;

	CartItem addProductToCartFromBarcode(String text, int priceListId,
			int bpartnerId) throws OperationException;


}
