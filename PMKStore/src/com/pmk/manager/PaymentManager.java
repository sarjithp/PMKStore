/**
 * 
 */
package com.pmk.manager;

import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MPInstance;
import org.compiere.model.MPayment;
import org.compiere.model.PO;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.wf.MWorkflow;

import com.pmk.shared.OperationException;
import com.pmk.util.PoHandler;

/**
 * @author sarjith
 * 
 */
public class PaymentManager {

	public static void saveCustomerPayment(Properties ctx,int customerId, BigDecimal payAmt,
			boolean allocateToOldInvoices, String trxName) throws OperationException {
		
		MPayment payment = new MPayment(ctx, 0, trxName);
		payment.setPayAmt(payAmt);
		payment.setTenderType(MPayment.TENDERTYPE_Cash);
		payment.setC_BPartner_ID(customerId);
		payment.setC_DocType_ID(true);
		payment.setC_CashBook_ID(getCashbookId(ctx));
		payment.setC_Currency_ID(304);//INR
		
		PoHandler.savePO(payment);
		PoHandler.processIt(payment, MPayment.DOCACTION_Complete);
		
		int[] ids = MInvoice.getAllIDs(MInvoice.Table_Name, "ISACTIVE = 'Y' and docstatus in ('CO','CL') " +
				" AND invoiceopen(C_Invoice_ID,0) > 0 AND C_BPartner_ID = " + customerId, trxName);
		
		if (ids != null && ids.length > 0) {
			MAllocationHdr hdr = new MAllocationHdr(ctx, true, null, payment.getC_Currency_ID(), null, trxName);
			PoHandler.savePO(hdr);
			
			for (int id : ids) {
				if (payAmt.signum() <= 0) {
					break;
				}
				MInvoice invoice = new MInvoice(ctx, id, trxName);
				BigDecimal allocationAmt = invoice.getOpenAmt();
				if (payAmt.compareTo(allocationAmt) < 0) {
					allocationAmt = payAmt;
				}
				
				payAmt = payAmt.subtract(allocationAmt);
				
				MAllocationLine line = new MAllocationLine(hdr);
				line.setC_Invoice_ID(id);
				line.setC_Payment_ID(payment.get_ID());
				line.setAmount(allocationAmt);
				
				PoHandler.savePO(line);
			}
			
			PoHandler.processIt(hdr, MAllocationHdr.ACTION_Complete);
		}
	}

	public static int getCashbookId(Properties ctx) {
		String sql = "select c_cashbook_id from u_posterminal where ad_client_id ="+Env.getAD_Client_ID(ctx);
		return DB.getSQLValue(null, sql);
	}

}
