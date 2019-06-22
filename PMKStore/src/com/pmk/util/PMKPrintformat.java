package com.pmk.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MClient;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MOrg;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MProduct;
import org.compiere.model.MSysConfig;
import org.compiere.util.Env;

import com.pmk.client.Constants;

public class PMKPrintformat {

	private static int _lineWidth = 40;
	protected String _lineTop= "";

	public String formatOrder(Properties ctx, MOrder order) throws Exception {

		PrinterConstants printerConstants = new TmkPrinterConstants();
		String printerType = "other";
		_lineWidth = MSysConfig.getIntValue(Constants.PRINT_LINE_WIDTH, _lineWidth,
				Env.getAD_Client_ID(ctx));
		setLineTop();

		// setting receipt header
		MClient client = MClient.get(ctx);
		MOrg myorg = new MOrg(ctx, order.getAD_Org_ID(), null);// OrganisationManager.getMyOrg(ctx);

		// get footer message
		MOrgInfo orgInfo = myorg.getInfo();
		String footerMsg = orgInfo.getReceiptFooterMsg();

		String companyName = getFormattedText(printerType, client.getName(),
				printerConstants.FONT_DOUBLE, PrinterConstants.CENTER_ALIGN);

		String title = "Sales Order";

		StringBuffer reportData = new StringBuffer();
//		reportData.append(PrinterConstants.CENTER_ALIGN);
//		reportData.append(PrinterConstants.LOGO1);
		// Print header logo if set
		// (Using standalone
		// program) as NV image 1
//		reportData.append(printerConstants.LEFT_ALIGN);
//		reportData.append(PrinterConstants.LINE_FEED);
		// add company name
		reportData.append(companyName);
		
		String temp = null;
		MLocation location = MLocation.get(ctx, orgInfo.getC_Location_ID(), null);
		temp = getFormattedText(printerType,location.getAddress1(), PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN);
		reportData.append(temp);
		temp = getFormattedText(printerType,location.getCity(), PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN);
		reportData.append(temp);
		temp = getFormattedText(printerType,"Phone : " + orgInfo.getPhone(), PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN);
		reportData.append(temp);
		reportData.append(PrinterConstants.LINE_FEED);
		
		reportData
				.append(getFormattedText(printerType, title,
						printerConstants.FONT_NORMAL,
						PrinterConstants.CENTER_ALIGN));
		
		reportData.append(PrinterConstants.LINE_FEED);

		// add date & time
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(order.getCreated().getTime()));

		String date = "DATE: " + String.format("%1$te/%1$tm/%1$tY", c);
		String time = "TIME: " + String.format("%1$tH:%1$tM", c);

		String orderNoStr = "Order No: " + order.getDocumentNo();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		orderNoStr = String.format(
					"%1$s%2$" + (_lineWidth - orderNoStr.length() - 2) + "s",
					orderNoStr, "");
		reportData.append(getFormattedText(printerType, orderNoStr,
				PrinterConstants.FONT_SMALL, printerConstants.LEFT_ALIGN,
				true));
		
		temp = String.format("%1$s%2$"+ (_lineWidth  - date.length() - 2) + "s",date,time);
		reportData.append(getFormattedText(printerType,temp, PrinterConstants.FONT_SMALL, printerConstants.LEFT_ALIGN, true));
		
		// add vat#
		/*if (vatRegNumber != null && showTaxDetails) {
			if (countryCode.equals("IN"))
				vatRegNumber = "GST NO : " + vatRegNumber;
			else
				vatRegNumber = "VAT NO: " + vatRegNumber;

			if (sTaxNumber != null)
				sTaxNumber = "Service Tax No: " + sTaxNumber;
			else
				sTaxNumber = "";
			vatRegNumber = String.format(
					"%1$s%2$" + (_lineWidth - vatRegNumber.length() - 2) + "s",
					vatRegNumber, sTaxNumber);
			reportData.append(getFormattedText(printerType, vatRegNumber,
					PrinterConstants.FONT_SMALL, printerConstants.LEFT_ALIGN));
		}*/

		String itemHdr = "";
		reportData.append(getFormattedText(printerType, _lineTop,
				PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN));

		String hdr = "";
		String hdr1 = "%1$-";
		String hdr2 = "s%2$-5";
		String itemLine = "";
		String itemLine1 = "%1$-";
		String itemLine2 = "s%2$7";
		int width = 5;
		List<String> hdrarray = new ArrayList<String>();
		hdrarray.add("Item");
		hdrarray.add("Qty");
		int hdrindex = 3;
		hdr2 += "s %" + hdrindex + "$-" + (6);
		itemLine2 += "s%" + hdrindex + "$" + (7);
		hdrarray.add("Price");
		width = width + 7;
		hdrindex++;
		hdr2 += "s   %" + hdrindex + "$-7s";
		itemLine2 += "s%" + hdrindex + "$10s";
		width += 10;
		hdrarray.add("Total");
		hdr = hdr1 + (_lineWidth - width) + hdr2;
		itemLine = itemLine1 + (_lineWidth - width - 2) + itemLine2;
		itemHdr = String.format(hdr, hdrarray.toArray());
		reportData.append(getFormattedText(printerType, itemHdr,
				PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN));
		reportData.append(getFormattedText(printerType, _lineTop,
				PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN));

		BigDecimal orderLineTotalDiscount = Env.ZERO;
		BigDecimal totalQty = BigDecimal.ZERO;
		MOrderLine[] lines = order.getLines();
		for (MOrderLine line : lines) {
			String productName = line.getDescription();
			MProduct product = line.getProduct();
			int	uomPrecision = product.getUOMPrecision();
			if (productName == null) {
				productName = product.getDescription();
			}

			BigDecimal lineQty = line.getQtyOrdered();
			BigDecimal price = line.getPriceActual();
			orderLineTotalDiscount = line.getPriceList().subtract(line.getPriceActual());
			int nameLength = productName.length();
			int space = 2;
			if ((nameLength + space + width + 4) > _lineWidth) {
				int trunc = 0;
				trunc = nameLength + space + width + 4 - _lineWidth;
				productName = productName.substring(0, nameLength - trunc - 1);
				nameLength = productName.length();
			}
			totalQty = totalQty.add(lineQty);
			String orderline = String.format("%1$." + uomPrecision + "f",
					lineQty.doubleValue());
			String priceStr = String.format("%1$.2f",
					line.getLineNetAmt().doubleValue());

			productName = String.format("%1$s", productName);
			String unitPrice = String.format("%1$.2f", price.doubleValue());

			List<String> params = new ArrayList<String>();
			params.add(productName);
			params.add(orderline);
			params.add(unitPrice);
			params.add(priceStr);

			orderline = String.format(itemLine, params.toArray());
			reportData.append(getFormattedText(printerType, orderline,
					PrinterConstants.FONT_SMALL, printerConstants.LEFT_ALIGN));
		}
		reportData.append(getFormattedText(printerType, _lineTop,
				PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN));

		// 2.footer
		String orderFooter = String.format("GRAND TOTAL :%1$10.2f  ", order.getGrandTotal());
		reportData.append(getFormattedText(printerType, orderFooter,
				printerConstants.FONT_NORMAL_BOLD,
				printerConstants.RIGHT_ALIGN));
		
		if (orderLineTotalDiscount.signum() > 0) {
			temp = String.format("Total Discount Applied:%1$10.2f  ", orderLineTotalDiscount);
			reportData.append(getFormattedText(printerType, orderFooter,
					printerConstants.FONT_NORMAL,
					printerConstants.RIGHT_ALIGN));
		}
		/*-----------------------------------------------------------------------------------*/
		reportData.append(getFormattedText(
				printerType,
				"Total No of Items : "
						+ String.format("%1$-7d",
								lines.length), PrinterConstants.FONT_SMALL,
								printerConstants.RIGHT_ALIGN));


		reportData.append(getFormattedText(printerType, _lineTop,
				PrinterConstants.FONT_SMALL, PrinterConstants.CENTER_ALIGN));


		reportData.append(PrinterConstants.LINE_FEED);

		int diff = 0;
		if (false) {
			reportData.append(PrinterConstants.CENTER_ALIGN);
			reportData.append(printerConstants.BAR_CODE_HRI_POS);
			reportData.append(printerConstants.BAR_CODE_HEIGHT_100);
			reportData.append(printerConstants.BAR_CODE_WIDTH_2);
			reportData.append(printerConstants.BAR_CODE);
			String invId = String.valueOf(order.getDocumentNo());
			// diff = 11 - invId.length();
			// while(diff >0){
			// diff--;
			// invId="0"+invId;
			// }
			reportData.append(new String(new byte[] { (byte) invId.length() }));
			reportData.append(new String(invId.getBytes()));
		}
		if (footerMsg != null && footerMsg.length() != 0) {
			if (footerMsg.contains("<newline>")) {//multi line footer msg
				String[] footers = footerMsg.split("<newline>");
				for (String footer : footers) {
					reportData.append(getFormattedText(printerType, footer,
							PrinterConstants.FONT_SMALL,
							PrinterConstants.CENTER_ALIGN));
				}
			} else {
				reportData.append(getFormattedText(printerType, footerMsg,
						PrinterConstants.FONT_SMALL,
						PrinterConstants.CENTER_ALIGN));
			}
		}
		
		reportData.append(printerConstants.PAPER_CUT).append(
				PrinterConstants.LINE_FEED);

		return reportData.toString();
	}

	private void setLineTop() {
		_lineTop = "";
		for (int i = 0; i < _lineWidth ; i++) {
			_lineTop += "-";
		}
	}

	public String getFormattedText(String printerType, String text,
			String font, String alignment) {
		return getFormattedText(printerType, text, font, alignment, true);
	}

	public String getFormattedText(String printerType, String text,
			String font, String alignment, boolean addLineFeed) {
		int charPerLine = getLineWidth(font);
		String formattedText = "";

		if (printerType.equals(PrinterConstants.EPSON_COMPATIBLE)) {

			formattedText = font + alignment + text;

			if (addLineFeed) {
//				formattedText = formattedText
//						+ EscPosPrinterConstants.LINE_FEED;
			}
			return formattedText;
		}

		if (TmkPrinterConstants.CENTER_ALIGN.equals(alignment)) {
			formattedText = font + TmkPrinterConstants.CENTER_ALIGN + text;

			if (addLineFeed) {
				formattedText = formattedText + TmkPrinterConstants.LINE_FEED;
			}
			return formattedText;
		}

		if (TmkPrinterConstants.LEFT_ALIGN.equals(alignment)) {
			formattedText = font
					+ String.format("%1$-" + charPerLine + "s", text);

			if (addLineFeed) {
				formattedText = formattedText + TmkPrinterConstants.LINE_FEED;
			}
			return formattedText;
		}

		if (TmkPrinterConstants.RIGHT_ALIGN.equals(alignment)) {
			formattedText = font
					+ String.format("%1$" + charPerLine + "s", text);

			if (addLineFeed) {
				formattedText = formattedText + TmkPrinterConstants.LINE_FEED;
			}
			return formattedText;
		}

		return text;
	}

	public int getLineWidth(String font) {

		if (TmkPrinterConstants.FONT_DOUBLE.equals(font)) {
			return 16;
		}

		if (TmkPrinterConstants.FONT_NORMAL.equals(font)
				|| TmkPrinterConstants.FONT_NORMAL_BOLD.equals(font)) {
			return (_lineWidth / 5 * 4) - 1;
		}

		return _lineWidth;
		// return printWidth;
	}
}
