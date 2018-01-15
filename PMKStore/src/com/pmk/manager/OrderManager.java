package com.pmk.manager;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MProductPricing;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.pmk.shared.CartItem;
import com.pmk.shared.OperationException;
import com.pmk.shared.OrderBean;
import com.pmk.util.PoHandler;

public class OrderManager {

	public static CartItem createCartItem(Properties ctx, int productId, int priceListId, int bpartnerId) throws OperationException {
		CartItem item = new CartItem();
		MProduct product = MProduct.get(ctx, productId);
		if (!product.isActive()) {
			throw new OperationException("Product is not active");
		}
		
		item.setProductId(product.get_ID());
		item.setDescription(product.getDescription());
		item.setBarcode(product.getValue());
		item.setUom(product.getUOMSymbol());
		item.setQtyOrdered(BigDecimal.ONE);
		
		MProductPrice price = ProductManager.getProductPrice(ctx,productId, priceListId);
		
		item.setInclPrice(price.getPriceStd().setScale(2));
		
		return item;
	}

	public static void completeOrder(Properties ctx, OrderBean bean,
			List<CartItem> items, String trxName) throws OperationException {
		MOrder order = new MOrder(ctx,0, trxName);
		order.setC_BPartner_ID(bean.getCustomerId());
		order.setM_PriceList_ID(bean.getPriceListId());
		order.setPaymentRule("cash".equalsIgnoreCase(bean.getPaymentType()) ? MOrder.PAYMENTRULE_Cash : MOrder.PAYMENTRULE_OnCredit);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(MDocType.DOCSUBTYPESO_POSOrder);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.DOCACTION_Complete);
		
		PoHandler.savePO(order);
		
		List<MOrderLine> orderlines = new ArrayList<MOrderLine>();
		for (CartItem item : items) {
			MOrderLine orderline = new MOrderLine(order);
			orderline.setM_Product_ID(item.getProductId());
			orderline.setQty(item.getQtyOrdered());
			orderline.setPrice();
			orderline.setPrice(item.getInclPrice());
			
			PoHandler.savePO(orderline);
			orderlines.add(orderline);
		}
		
		PoHandler.processIt(order, MOrder.DOCACTION_Complete);
		
		//creating invoice
		/*MInvoice invoice = new MInvoice(order,0,null);
		PoHandler.savePO(invoice);
		
		List<MInvoiceLine> invoiceLines = new ArrayList<MInvoiceLine>();
		for (MOrderLine line : orderlines) {
			MInvoiceLine invoiceline = new MInvoiceLine(invoice);
			invoiceline.setOrderLine(line);
			
			PoHandler.savePO(invoiceline);
			invoiceLines.add(invoiceline);
		}
		
		PoHandler.processIt(invoice, MOrder.DOCACTION_Complete);
		
		//creating shipments
		MInOut inout = new MInOut(order, 0, null);
		inout.setC_Invoice_ID(invoice.getC_Invoice_ID());
		PoHandler.savePO(inout);
		
		for (MInvoiceLine line : invoiceLines) {
			MInOutLine inoutline = new MInOutLine(inout);
			inoutline.setInvoiceLine(line, 0, line.getQtyInvoiced());
			
			PoHandler.savePO(inoutline);
		}
		
		PoHandler.processIt(invoice, MOrder.DOCACTION_Complete);
		
		if (MOrder.PAYMENTRULE_Cash.equalsIgnoreCase(order.getPaymentRule())) {
			MPayment payment = new MPayment(ctx, 0, trxName);
			payment.setPayAmt(order.getGrandTotal());
			payment.setTenderType(MPayment.TENDERTYPE_Cash);
			payment.setC_BPartner_ID(order.getC_BPartner_ID());
			payment.setC_DocType_ID(true);
			payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
			payment.setC_Order_ID(order.getC_Order_ID());
			payment.setC_CashBook_ID(PaymentManager.getCashbookId(ctx));
			payment.setC_Currency_ID(304);//INR
			
			PoHandler.savePO(payment);
			PoHandler.processIt(payment, MPayment.DOCACTION_Complete);
			
			invoice.setC_Payment_ID(payment.get_ID());
			PoHandler.savePO(payment);
		}*/
	}

	public static List<OrderBean> getSalesHistory(Properties ctx, long date, String paymentType) {
		String sql = "SELECT documentno, o.paymentrule, grandtotal, name from c_order o join c_bpartner b on o.c_bpartner_id = b.c_bpartner_id " +
				" where o.ad_client_id = " + Env.getAD_Client_ID(ctx) + " AND o.dateordered::date = ? ::date ";
		if ("cash".equalsIgnoreCase(paymentType)) {
			sql += " AND o.paymentrule = '" + MOrder.PAYMENTRULE_Cash + "'"; 
		} else if ("credit".equalsIgnoreCase(paymentType)) {
			sql += " AND o.paymentrule = '" + MOrder.PAYMENTRULE_OnCredit + "'";
		}
		sql += " ORDER BY o.created ";
		List<OrderBean> list = new ArrayList<OrderBean>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DB.prepareStatement(sql,null);
			stmt.setDate(1, new java.sql.Date(date));
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				OrderBean details = new OrderBean();
				details.setOrderNo(rs.getString("documentno"));
				details.setGrandTotal(rs.getBigDecimal("grandtotal"));
				details.setCustomerName(rs.getString("name"));
				details.setPaymentType(MOrder.PAYMENTRULE_Cash.equalsIgnoreCase(rs.getString("paymentrule")) ? "Cash":"Credit");
				list.add(details);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs, stmt);
		}
		return list;
	}

}
