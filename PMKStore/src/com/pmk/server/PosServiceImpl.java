package com.pmk.server;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.compiere.util.Trx;

import com.pmk.client.PosService;
import com.pmk.manager.CustomerManager;
import com.pmk.manager.LoginManager;
import com.pmk.manager.OrderManager;
import com.pmk.manager.PaymentManager;
import com.pmk.manager.ProductManager;
import com.pmk.shared.CartItem;
import com.pmk.shared.CustomerBean;
import com.pmk.shared.KeyNamePair;
import com.pmk.shared.LoginUser;
import com.pmk.shared.OperationException;
import com.pmk.shared.OrderBean;
import com.pmk.shared.ProductBean;
import com.pmk.util.DBUtil;
import com.pmk.util.POSEnv;
import com.pmk.util.TrxPrefix;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PosServiceImpl extends RemoteServiceServlet implements
		PosService {

	@Override
	public void init() throws ServletException {
		super.init();
		DBUtil.setDbTarget(getServletContext());
	}
	
	@Override
	public LoginUser getLoginUser(String userpin) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return LoginManager.loginUser(ctx, userpin);
	}

	@Override
	public CartItem addProductToCartFromBarcode(String text,int priceListId, int bpartnerId) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		int productId = ProductManager.getProductFromBarcode(ctx,text);
		if (productId >= 0) {
			return OrderManager.createCartItem(ctx,productId,priceListId,bpartnerId);
		} else {
			throw new OperationException("Product not found");
		}
	}
	
	@Override
	public CartItem addProductToCart(int productId,int priceListId, int bpartnerId) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return OrderManager.createCartItem(ctx,productId,priceListId,bpartnerId);
	}


	@Override
	public void saveProduct(ProductBean bean) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		String trxName = TrxPrefix.getPrefix();
		Trx trx = null;
		try {
			trx = Trx.get(trxName, true);
			trx.start();
			ProductManager.saveProduct(ctx, bean, trx.getTrxName());
			trx.commit();
		} catch (Exception e) {
			trx.rollback();
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		} finally {
			trx.close();
		}
	}

	@Override
	public List<KeyNamePair> getUomList() {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return ProductManager.getUomList(ctx);
	}

	@Override
	public ProductBean loadProduct(int productId, int priceListId) {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return ProductManager.loadProduct(ctx,productId,priceListId);
	}

	@Override
	public CustomerBean loadCustomer(int customerId) {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return CustomerManager.loadCustomer(ctx, customerId);
	}

	@Override
	public void saveCustomer(CustomerBean bean) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		String trxName = TrxPrefix.getPrefix();
		Trx trx = null;
		try {
			trx = Trx.get(trxName, true);
			trx.start();
			CustomerManager.saveCustomer(ctx, bean, trx.getTrxName());
			trx.commit();
		} catch (Exception e) {
			trx.rollback();
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		} finally {
			trx.close();
		}
	}

	@Override
	public void savePaymentDetails(int customerId, BigDecimal payAmt) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		String trxName = TrxPrefix.getPrefix();
		Trx trx = null;
		try {
			trx = Trx.get(trxName, true);
			trx.start();
			PaymentManager.saveCustomerPayment(ctx,customerId,payAmt,true, trx.getTrxName());
			trx.commit();
		} catch (Exception e) {
			trx.rollback();
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		} finally {
			trx.close();
		}
	}

	@Override
	public void completeOrder(List<CartItem> items, OrderBean order) throws OperationException {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		String trxName = TrxPrefix.getPrefix();
		Trx trx = null;
		try {
			trx = Trx.get(trxName, true);
			trx.start();
			OrderManager.completeOrder(ctx,order, items, trxName);
			trx.commit();
		} catch (Exception e) {
			trx.rollback();
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		} finally {
			trx.close();
		}
	}

	@Override
	public List<OrderBean> getSalesHistory(long date,String paymentType) {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return OrderManager.getSalesHistory(ctx, date, paymentType);
	}

	@Override
	public List<Suggestion> getProductSuggestions(String text) {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return ProductManager.getProductSuggestions(ctx,text);
	}

	@Override
	public List<Suggestion> getCustomerSuggestions(String text) {
		HttpServletRequest request = this.getThreadLocalRequest();
		Properties ctx = POSEnv.getCtx(request);
		return CustomerManager.getCustomerSuggestions(ctx,text);
	}
	
}
