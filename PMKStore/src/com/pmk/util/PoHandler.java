package com.pmk.util;

import org.compiere.model.PO;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.ValueNamePair;

import com.pmk.shared.OperationException;

public class PoHandler {

	public static void savePO(PO po) throws OperationException {
		if(! po.save() ) {			
			reportError(po);
		}
	}
	
	public static void reportError(PO po) throws OperationException {
		Exception ex = CLogger.retrieveException();
		ValueNamePair error = CLogger.retrieveError();
		String msg = null;
		if (error != null) {
			msg = error.getValue() + error.getName();
		}
		
		if(ex != null) {
			throw new OperationException("Cannot save PO object. " + po.getClass().getName() + " Cause: " + ex.getMessage());
		}
		
		if(msg != null)	{
			throw new OperationException("Cannot save PO object. " + po.getClass().getName() + " Cause: " + msg);
		}
		
		throw new OperationException("Cannot save PO object. " + po.getClass().getName());			
	}

	public static void processIt(PO po, String processAction)
			throws OperationException {
		boolean processed = false;
		try {
			if (po instanceof DocAction) {
				DocAction docPo = (DocAction) po;
				processed = docPo.processIt(processAction);
				savePO(po);
				if (!processed) {
					throw new OperationException("Cannot process "
							+ po.getClass().getName() + " to: " + processAction
							+ " " + docPo.getProcessMsg());
				}

			}
		} catch (Exception ex) {
			throw new OperationException(ex.getMessage());
		}
	}
}
