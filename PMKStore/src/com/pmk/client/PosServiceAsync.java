package com.pmk.client;

import java.math.BigDecimal;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.pmk.shared.CartItem;
import com.pmk.shared.CustomerBean;
import com.pmk.shared.KeyNamePair;
import com.pmk.shared.LoginUser;
import com.pmk.shared.OrderBean;
import com.pmk.shared.PrintSetup;
import com.pmk.shared.ProductBean;
import com.pmk.shared.TaxCategoryBean;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface PosServiceAsync {

	void getLoginUser(String userpin, AsyncCallback<LoginUser> asyncCallback);
	
	void addProductToCartFromBarcode(String text, int priceListId, int bpartnerId,
			AsyncCallback<CartItem> asyncCallback);

	void addProductToCart(int productId, int priceListId, int customerID,
			AsyncCallback<CartItem> asyncCallback);

	void saveProduct(ProductBean bean, AsyncCallback<Void> asyncCallback);

	void getKeyNamePairList(String tableName, AsyncCallback<List<KeyNamePair>> asyncCallback);

	void loadProduct(int productId, int priceListId,
			AsyncCallback<ProductBean> asyncCallback);

	void loadCustomer(int salesCustomerId,
			AsyncCallback<CustomerBean> asyncCallback);

	void saveCustomer(CustomerBean bean, AsyncCallback<Void> asyncCallback);

	void savePaymentDetails(int salesCustomerId, BigDecimal value,
			AsyncCallback<Void> asyncCallback);

	void completeOrder(List<CartItem> items, OrderBean order,
			AsyncCallback<OrderBean> asyncCallback);

	void getSalesHistory(long fromDatelong,
			long toDateLong, String paymentType, AsyncCallback<List<OrderBean>> asyncCallback);

	void getProductSuggestions(String trim,
			AsyncCallback<List<Suggestion>> asyncCallback);

	void getCustomerSuggestions(String trim,
			AsyncCallback<List<Suggestion>> asyncCallback);

	void searchProducts(String text, String text2, Integer value,
			AsyncCallback<List<ProductBean>> asyncCallback);

	void getNextProductCode(AsyncCallback<Integer> asyncCallback);

	void printOrder(int lastCompletedOrderId, AsyncCallback<Void> asyncCallback);

	void loadPreviousOrders(AsyncCallback<List<OrderBean>> asyncCallback);

	void savePrintSetup(PrintSetup setup, AsyncCallback<Void> asyncCallback);

	void loadPrintSetUp(AsyncCallback<PrintSetup> asyncCallback);

	void getTaxCategories(String tableName,
			AsyncCallback<List<TaxCategoryBean>> asyncCallback);

	void searchCustomers(String code, String name,
			AsyncCallback<List<CustomerBean>> asyncCallback);

	void loadOrderForEdit(int orderId, AsyncCallback<OrderBean> asyncCallback);

}
