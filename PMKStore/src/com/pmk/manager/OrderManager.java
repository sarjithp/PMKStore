package com.pmk.manager;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
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
	
	static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

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
		
		BigDecimal priceStd = price.getPriceStd();
		
		if (!MPriceList.get(ctx, priceListId, null).isTaxIncluded()) {
			//setting tax rate to tax
			MTaxCategory taxCategory = (MTaxCategory) product.getC_TaxCategory(); 
			MTax tax = taxCategory.getDefaultTax();
			BigDecimal taxRate = getTaxRate(ctx, tax);
			
			priceStd = priceStd.add(priceStd.multiply(taxRate).divide(ONE_HUNDRED,2,RoundingMode.HALF_UP));
		}
		item.setPriceEntered(priceStd);
		
		return item;
	}
	
	private static BigDecimal getTaxRate(Properties ctx, MTax tax) {
		BigDecimal taxRate = BigDecimal.ZERO;
		if (!tax.isSummary()) {
			taxRate = tax.getRate();
		} else {
			MTax[] childTaxes = tax.getChildTaxes(false);
			for (MTax childTax : childTaxes) {
				taxRate = taxRate.add(childTax.getRate());
			}
		}
		return taxRate;
	}

	public static OrderBean completeOrder(Properties ctx, OrderBean bean,
			List<CartItem> items, String trxName) throws OperationException {
		
		boolean editingExistingOrder = bean.getOrderId() != 0;
		MOrder order = new MOrder(ctx, bean.getOrderId(), trxName);
		if (editingExistingOrder) {//if its editing of existing order
			List<MPayment> payments = MPayment.getOfOrder(order);
			for (MPayment payment : payments) {
				PoHandler.processIt(payment, MPayment.ACTION_Void);
			}
			PoHandler.processIt(order, MOrder.ACTION_ReActivate);
			MInvoice[] invoices = order.getInvoices();
			for (MInvoice inv : invoices) {
				if (!MInvoice.DOCSTATUS_Completed.equalsIgnoreCase(inv.getDocStatus())) {
					inv.setDocumentNo(inv.getDocumentNo() + "_" + inv.getC_Invoice_ID());
					PoHandler.savePO(inv);
				}
			}
		}
		
		MBPartner partner = MBPartner.get(ctx, bean.getCustomerId());
		order.setBPartner(partner);
		if (bean.getDeliveryLocationId() > 0) {
			order.setC_BPartner_Location_ID(bean.getDeliveryLocationId());
		} else if (bean.getDeliveryAddress() != null && !bean.getDeliveryAddress().trim().isEmpty()) {
			MBPartnerLocation newLocation = CustomerManager.createNewShipLocation(ctx, partner, bean.getDeliveryAddress(), trxName);
			order.setC_BPartner_Location_ID(newLocation.get_ID());
		}
		boolean isGSTCustomer = partner.getTaxID() != null && !partner.getTaxID().isEmpty();
		
		order.setM_PriceList_ID(bean.getPriceListId());
		order.setPaymentRule("cash".equalsIgnoreCase(bean.getPaymentType()) ? MOrder.PAYMENTRULE_Cash : MOrder.PAYMENTRULE_OnCredit);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(MDocType.DOCSUBTYPESO_POSOrder);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.DOCACTION_Complete);
		order.setDescription(bean.getDescription());
		
		//checking the order number entered by user
		String orderNo = bean.getOrderNo();
		if (orderNo != null && !orderNo.trim().isEmpty()) {
			int[] ids = PoHandler.getAllIDs(MOrder.Table_Name, "documentno = ? and ad_client_id = ? and isactive='Y' AND C_ORDER_ID <> ", 
					new Object[]{orderNo, Env.getAD_Client_ID(ctx), order.get_ID()}, trxName);
			if (ids != null && ids.length > 0) {
				throw new OperationException("Order number already exists");
			}
			order.setDocumentNo(orderNo);
		}
		if (bean.getDateOrdered() != null) {
			order.setDateOrdered(new Timestamp(bean.getDateOrdered().getTime()));
			order.setDateAcct(order.getDateOrdered());
			order.setDatePrinted(order.getDateOrdered());
			order.setDatePromised(order.getDateOrdered());
		}
		
		PoHandler.savePO(order);
		
		List<MOrderLine> orderlines = new ArrayList<MOrderLine>();
		List<MOrderLine> oldLines = new LinkedList<MOrderLine>(Arrays.asList(order.getLines()));
		for (CartItem item : items) {
			MOrderLine orderline = null;
			if (item.getOrderLineId() != 0) {
				orderline = new MOrderLine(ctx,item.getOrderLineId(),trxName);
				oldLines.remove(orderline);
			} else {
				orderline = new MOrderLine(order);
				orderline.setPrice();
			}
			orderline.setM_Product_ID(item.getProductId());
			orderline.setQty(item.getQtyOrdered());

			/** GST+Cess will be the default tax set to the line
			 * if its a GST customer, then cess is not applicable here
			 * so non-default tax is set
			 */
			MProduct product = orderline.getProduct();
			if (isGSTCustomer) {
				String sql = "AD_Client_ID = " + Env.getAD_Client_ID(ctx) + " AND C_TaxCategory_ID = " + product.getC_TaxCategory_ID()
					+ " and IsDefault='N' and isactive='Y' and coalesce(parent_tax_id,0) = 0";
				int[] ids = MTax.getAllIDs(MTax.Table_Name, sql, trxName);
				if (ids != null && ids.length > 0) {
					orderline.setC_Tax_ID(ids[0]);
				}
			} else {
//				orderline.setTax();//so as to set the detault tax
				MTaxCategory taxCategory = (MTaxCategory) product.getC_TaxCategory();
				MTax tax = taxCategory.getDefaultTax();
				if (tax != null) {
					orderline.setC_Tax_ID(tax.get_ID());
				}
			}
			
			BigDecimal unitPrice = item.getPriceEntered();
			
			/**
			 * Entered price will be inclusive price
			 * if the PL is exclusive then we have to calculate the excl price from this.
			 */
			if (!order.getM_PriceList().isTaxIncluded()) {
				BigDecimal taxRate = getTaxRate(ctx, MTax.get(ctx, orderline.getC_Tax_ID()));
				unitPrice = unitPrice.multiply(ONE_HUNDRED).divide(ONE_HUNDRED.add(taxRate),4, RoundingMode.HALF_UP);
			}
			
			orderline.setPrice(unitPrice);
			
			PoHandler.savePO(orderline);
			orderlines.add(orderline);
		}
		
		for (MOrderLine orderLine : oldLines) {
			orderLine.delete(true, trxName);
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
		/*MInOut inout = new MInOut(order, 0, null);
		inout.setC_Invoice_ID(invoice.getC_Invoice_ID());
		PoHandler.savePO(inout);
		
		for (MInvoiceLine line : invoiceLines) {
			MInOutLine inoutline = new MInOutLine(inout);
			inoutline.setInvoiceLine(line, 0, line.getQtyInvoiced());
			
			PoHandler.savePO(inoutline);
		}
		
		PoHandler.processIt(invoice, MOrder.DOCACTION_Complete);*/
		
		//since invoice is automatically created from order.completeit()
		MInvoice invoice = null;
		//if the order number is passed from screen then we have to modify the invoice's details too
		if (orderNo != null && !orderNo.trim().isEmpty()) {
			MInvoice[] invoices = order.getInvoices();
			for (MInvoice inv : invoices) {
				if (MInvoice.DOCSTATUS_Completed.equalsIgnoreCase(inv.getDocStatus())) {
					inv.setDocumentNo(orderNo);
					PoHandler.savePO(inv);
					invoice = inv;
				}
			}
		}
		
		if (MOrder.PAYMENTRULE_Cash.equalsIgnoreCase(order.getPaymentRule())) {
			if (invoice == null) {
				int ids[] = MInvoice.getAllIDs(MInvoice.Table_Name, "C_Order_ID = " + order.getC_Order_ID(), trxName);
				if (ids != null && ids.length > 0) {
					invoice = new MInvoice(ctx, ids[0], trxName);
				}
			}
			MPayment payment = new MPayment(ctx, 0, trxName);
			payment.setPayAmt(invoice.getGrandTotal());
			payment.setTenderType(MPayment.TENDERTYPE_Cash);
			payment.setC_BPartner_ID(order.getC_BPartner_ID());
			payment.setC_DocType_ID(true);
			payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
			payment.setC_Order_ID(order.getC_Order_ID());
			payment.setC_CashBook_ID(PaymentManager.getCashbookId(ctx));
			payment.setC_Currency_ID(304);//INR //TODO remove the hardcode.
			
			PoHandler.savePO(payment);
			PoHandler.processIt(payment, MPayment.DOCACTION_Complete);
			
			invoice.setC_Payment_ID(payment.get_ID());
			PoHandler.savePO(invoice);
		}
		
		bean.setOrderId(order.get_ID());
		bean.setOrderNo(order.getDocumentNo());
		return bean;
	}

	//return today's date without time
	private static Date getTodaysDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static List<OrderBean> getSalesHistory(Properties ctx, long fromDateLong, long toDateLong, String paymentType) {
		String sql = "SELECT o.c_order_id,o.documentno, o.paymentrule, i.grandtotal, b.name, max(p.description) as description,"
				+ " i.documentno as invoiceno "
				+ " from c_order o join c_orderline ol on o.c_order_id = ol.c_order_id"
				+ " LEFT JOIN C_Invoice i on o.c_order_id = i.c_order_id and i.docstatus in ('CO','CL') "
				+ " JOIN m_product p on ol.m_product_id = p.m_product_id "
				+ " JOIN c_bpartner b on o.c_bpartner_id = b.c_bpartner_id "
				+ " WHERE o.docstatus in ('CO','CL') and o.ad_client_id = " + Env.getAD_Client_ID(ctx) 
				+ " AND o.dateordered::date >= ? ::date "
				+ " AND o.dateordered::date <= ? ::date ";
		if ("cash".equalsIgnoreCase(paymentType)) {
			sql += " AND o.paymentrule = '" + MOrder.PAYMENTRULE_Cash + "'"; 
		} else if ("credit".equalsIgnoreCase(paymentType)) {
			sql += " AND o.paymentrule = '" + MOrder.PAYMENTRULE_OnCredit + "'";
		}
		sql += " group by o.c_order_id,b.c_bpartner_id,i.c_invoice_id "
			+ " ORDER BY o.created ";
		List<OrderBean> list = new ArrayList<OrderBean>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DB.prepareStatement(sql,null);
			stmt.setDate(1, new java.sql.Date(fromDateLong));
			stmt.setDate(2, new java.sql.Date(toDateLong));
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				OrderBean details = new OrderBean();
				details.setOrderId(rs.getInt("c_order_id"));
				details.setOrderNo(rs.getString("documentno"));
				details.setGrandTotal(rs.getBigDecimal("grandtotal"));
				details.setCustomerName(rs.getString("name"));
				details.setPaymentType(MOrder.PAYMENTRULE_Cash.equalsIgnoreCase(rs.getString("paymentrule")) ? "Cash":"Credit");
				details.setProductDescription(rs.getString("description"));
				details.setInvoiceNo(rs.getString("invoiceno"));
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

	public static void printOrder(Properties ctx, int orderId, HttpServletResponse response) throws Exception {
		String printType = MSysConfig.getValue(Constants.PRINT_TYPE,null, Env.getAD_Client_ID(ctx));
		String printDevice = MSysConfig.getValue(Constants.PRINT_DEVICE,null, Env.getAD_Client_ID(ctx));
		MOrder order = new MOrder(ctx, orderId, null);
		MInvoice[] invoices = null;//order.getInvoices();
		if (printType != null && Constants.PRINT_TYPE_VALUE_SLIP.equalsIgnoreCase(printType)) {//slip printing
			if (printDevice == null || printDevice.trim().isEmpty()) {
				throw new OperationException("Printer is not defined");
			}
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
			FileOutputStream output = null;
			try {
			
				String reportName = printDevice + "taxInvoice";
				
				String pdfFileName = printDevice + "Invoice_"; 
				pdfFileName += ((invoices != null && invoices.length > 0) ? invoices[0].getDocumentNo() : order.getDocumentNo()); 
				pdfFileName	+= ".pdf"; 
				output = new FileOutputStream(pdfFileName);
				
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("c_order_id", new BigDecimal(orderId));
				connection = DB.getConnectionRO();

				// compiles jrxml
				JasperCompileManager.compileReportToFile(reportName + ".jrxml");
				// fills compiled report with parameters and a connection
				JasperPrint print = JasperFillManager.fillReport(reportName + ".jasper", parameters, connection);
				// exports report to pdf
				JRPdfExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output); // your output goes here
				
				exporter.exportReport();
				
				//prepare to export the pdf
//				File reportFile = new File(reportName + ".jasper");
//				byte[] bytes = JasperRunManager.runReportToPdf(reportFile.getPath(), parameters, connection);
//
//	            response.setContentType("application/pdf");
//	            response.setContentLength(bytes.length);
//	            ServletOutputStream outStream = response.getOutputStream();
//	            outStream.write(bytes, 0, bytes.length);
//	            outStream.flush();
//	            outStream.close();

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("It's not possible to generate the pdf report.", e);
			} finally {
				if (output != null) {
					output.close();
					output = null;
				}
			}
		}
		
	}
	
	public static void printOrder2(Properties ctx, int orderId, HttpServletResponse response) throws Exception {
		String printType = MSysConfig.getValue(Constants.PRINT_TYPE,null, Env.getAD_Client_ID(ctx));
		String printDevice = MSysConfig.getValue(Constants.PRINT_DEVICE,null, Env.getAD_Client_ID(ctx));
		MOrder order = new MOrder(ctx, orderId, null);
		MInvoice[] invoices = null;//order.getInvoices();
		if (printType != null && Constants.PRINT_TYPE_VALUE_SLIP.equalsIgnoreCase(printType)) {//slip printing
			if (printDevice == null || printDevice.trim().isEmpty()) {
				throw new OperationException("Printer is not defined");
			}
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
			FileOutputStream output = null;
			try {
				String reportName = "taxInvoice";
				
				String pdfFileName = "Invoice_"; 
				pdfFileName += ((invoices != null && invoices.length > 0) ? invoices[0].getDocumentNo() : order.getDocumentNo()); 
				pdfFileName	+= ".pdf"; 
				output = new FileOutputStream(pdfFileName);
				
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("c_order_id", new BigDecimal(orderId));
				connection = DB.getConnectionRO();

				// compiles jrxml
				JasperCompileManager.compileReportToFile(reportName + ".jrxml");
				// fills compiled report with parameters and a connection
				JasperPrint print = JasperFillManager.fillReport(reportName + ".jasper", parameters, connection);
				// exports report to pdf
				JRPdfExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output); // your output goes here
				
				exporter.exportReport();
				
				//prepare to export the pdf
				File reportFile = new File(reportName + ".jasper");
				byte[] bytes = JasperRunManager.runReportToPdf(reportFile.getPath(), parameters, connection);

	            response.setContentType("application/pdf");
	            response.setContentLength(bytes.length);
	            ServletOutputStream outStream = response.getOutputStream();
	            outStream.write(bytes, 0, bytes.length);
	            outStream.flush();
	            outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("It's not possible to generate the pdf report.", e);
			} finally {
				if (output != null) {
					output.close();
					output = null;
				}
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
	
	public static void main(String[] args) {
		Connection connection = null;
		try {
		
			String reportName = "/home/sarjith/workspace/taxInvoice";
			Map<String, Object> parameters = new HashMap<String, Object>();

			// compiles jrxml
			JasperCompileManager.compileReportToFile(reportName + ".jrxml");
			// fills compiled report with parameters and a connection
			JasperPrint print = JasperFillManager.fillReport(reportName + ".jasper", parameters, new JREmptyDataSource());
			// exports report to pdf
			JRPdfExporter exporter = new JRPdfExporter();
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, new FileOutputStream(reportName + ".pdf")); // your output goes here
			
			exporter.exportReport();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}

	public static OrderBean loadOrder(Properties ctx, int orderId, String trxName) {
		MOrder morder = new MOrder(ctx, orderId, trxName);
		OrderBean bean = new OrderBean();
		
		bean.setOrderId(morder.get_ID());
		bean.setOrderNo(morder.getDocumentNo());
		bean.setPaymentType(morder.getPaymentRule());
		bean.setDateOrdered(morder.getDateOrdered());
		bean.setPriceListId(morder.getM_PriceList_ID());
		bean.setDescription(morder.getDescription());
		
		MBPartner partner = (MBPartner) morder.getC_BPartner();
		bean.setCustomerId(partner.get_ID());
		bean.setCustomerName(partner.getName());
		MBPartnerLocation bplocation = (MBPartnerLocation) morder.getC_BPartner_Location();
		bean.setDeliveryLocationId(bplocation.get_ID());
		bean.setDeliveryAddress(bplocation.getC_Location().getAddress1());
		
		MOrderLine[] orderlines = morder.getLines();
		List<CartItem> items = new ArrayList<CartItem>();
		bean.setLines(items);
		for (MOrderLine orderLine : orderlines) {
			CartItem item = new CartItem();
			MProduct product = orderLine.getProduct();
			item.setProductId(product.get_ID());
			item.setDescription(product.getDescription());
			item.setBarcode(product.getValue());
			item.setUom(product.getUOMSymbol());
			
			item.setQtyOrdered(orderLine.getQtyOrdered());
			BigDecimal taxRate = getTaxRate(ctx, MTax.get(ctx, orderLine.getC_Tax_ID()));
			item.setTaxRate(taxRate);
			BigDecimal unitPrice = orderLine.getPriceActual();
			/**
			 *  what we get from here is exlcusive price if its a exclusive PL
			 * we should convert it to inclusive
			 */
			if (!morder.isTaxIncluded()) {
				unitPrice = unitPrice.add(unitPrice.multiply(taxRate).divide(ONE_HUNDRED,4,RoundingMode.HALF_UP));
			}
			item.setPriceEntered(unitPrice);
			item.setOrderLineId(orderLine.get_ID());
			items.add(item);
		}
		
		return bean;
	}

}
