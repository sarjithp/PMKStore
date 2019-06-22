package com.pmk.manager;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MSysConfig;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.pmk.client.Constants;
import com.pmk.shared.CartItem;
import com.pmk.shared.OperationException;
import com.pmk.shared.OrderBean;
import com.pmk.shared.PrintSetup;
import com.pmk.util.PMKPrintformat;
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

	public static OrderBean completeOrder(Properties ctx, OrderBean bean,
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
		
		bean.setOrderId(order.get_ID());
		return bean;
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

	public static List<OrderBean> loadPreviousOrders(Properties ctx) {
		String sql = "select * from (select c_order_id, documentno, grandtotal, rank() OVER (order by created desc) "
				+ " from c_order where ad_client_id = " + Env.getAD_Client_ID(ctx)
				+ " and docstatus in ('CO','CL') and created > current_date order by created desc) as innertable where rank <= 10 ";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<OrderBean> list = new ArrayList<OrderBean>();
		try {
			stmt = DB.prepareStatement(sql, null);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				OrderBean bean = new OrderBean();
				bean.setOrderId(rs.getInt("c_order_id"));
				bean.setOrderNo(rs.getString("documentno"));
				bean.setGrandTotal(rs.getBigDecimal("grandtotal"));
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs, stmt);
		}
		return list;
	}

	public static void printOrder(Properties ctx, int orderId) throws Exception {
		String printType = MSysConfig.getValue(Constants.PRINT_TYPE,null, Env.getAD_Client_ID(ctx));
		if (printType != null && Constants.PRINT_TYPE_VALUE_SLIP.equalsIgnoreCase(printType)) {//slip printing
			String printDevice = MSysConfig.getValue(Constants.PRINT_DEVICE,null, Env.getAD_Client_ID(ctx));
			if (printDevice == null || printDevice.trim().isEmpty()) {
				throw new OperationException("Printer is not defined");
			}
			MOrder order = new MOrder(ctx, orderId, null);
			PMKPrintformat format = new PMKPrintformat();
			FileWriter writer = null;
			try {
				String print = format.formatOrder(ctx, order);
				writer = new FileWriter(printDevice);
				writer.write(print);
				writer.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (writer != null)
					writer.close();
			}
		} else {//a4 printing
			Connection connection = null;
			try {
			
				String reportName = "myreport";
				Map<String, Object> parameters = new HashMap<String, Object>();
				connection = DB.getConnectionRO();

				// compiles jrxml
				JasperCompileManager.compileReportToFile(reportName + ".jrxml");
				// fills compiled report with parameters and a connection
				JasperPrint print = JasperFillManager.fillReport(reportName + ".jasper", parameters, connection);
				// exports report to pdf
				JRPdfExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, new FileOutputStream(reportName + ".pdf")); // your output goes here
				
				exporter.exportReport();

			} catch (Exception e) {
				throw new RuntimeException("It's not possible to generate the pdf report.", e);
			} finally {
				
			}
		}
		
	}

	public static PrintSetup loadPrintSetup(Properties ctx) {
		PrintSetup setup = new PrintSetup();
		setup.setPrintDevice(MSysConfig.getValue(Constants.PRINT_DEVICE,null, Env.getAD_Client_ID(ctx)));
		setup.setPrintWidth(MSysConfig.getValue(Constants.PRINT_LINE_WIDTH, "40", Env.getAD_Client_ID(ctx)));
		setup.setPrintType(MSysConfig.getValue(Constants.PRINT_TYPE, Constants.PRINT_TYPE_VALUE_SLIP, Env.getAD_Client_ID(ctx)));
		return setup;
	}

	public static void savePrintSetup(Properties ctx, PrintSetup setup,
			String trxName) throws OperationException {
		int[] ids = MSysConfig.getAllIDs(MSysConfig.Table_Name, "name = '" + Constants.PRINT_DEVICE + "' and ad_client_id = " + Env.getAD_Client_ID(ctx), 
				trxName);
		MSysConfig config = null;
		if (ids == null || ids.length == 0) {
			config = new MSysConfig(ctx, 0, trxName);
			config.setName(Constants.PRINT_DEVICE);
			config.setConfigurationLevel(MSysConfig.CONFIGURATIONLEVEL_Client);
			config.setValue(setup.getPrintDevice());
			config.setAD_Org_ID(0);
		} else {
			config = new MSysConfig(ctx, ids[0], trxName);
			config.setValue(setup.getPrintDevice());
		}
		PoHandler.savePO(config);
		
		ids = MSysConfig.getAllIDs(MSysConfig.Table_Name, "name = '" + Constants.PRINT_LINE_WIDTH + "' and ad_client_id = " + Env.getAD_Client_ID(ctx), 
				trxName);
		if (ids == null || ids.length == 0) {
			config = new MSysConfig(ctx, 0, trxName);
			config.setName(Constants.PRINT_LINE_WIDTH);
			config.setConfigurationLevel(MSysConfig.CONFIGURATIONLEVEL_Client);
			config.setValue(setup.getPrintWidth());
			config.setAD_Org_ID(0);
		} else {
			config = new MSysConfig(ctx, ids[0], trxName);
			config.setValue(setup.getPrintWidth());
		}
		PoHandler.savePO(config);
		
		ids = MSysConfig.getAllIDs(MSysConfig.Table_Name, "name = '" + Constants.PRINT_TYPE + "' and ad_client_id = " + Env.getAD_Client_ID(ctx), 
				trxName);
		if (ids == null || ids.length == 0) {
			config = new MSysConfig(ctx, 0, trxName);
			config.setName(Constants.PRINT_TYPE);
			config.setConfigurationLevel(MSysConfig.CONFIGURATIONLEVEL_Client);
			config.setValue(setup.getPrintType());
			config.setAD_Org_ID(0);
		} else {
			config = new MSysConfig(ctx, ids[0], trxName);
			config.setValue(setup.getPrintWidth());
		}
		PoHandler.savePO(config);
		MSysConfig.resetCache();
	}

}
