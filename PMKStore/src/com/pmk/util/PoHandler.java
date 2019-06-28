package com.pmk.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.compiere.model.PO;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
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
	
	/*************************************************************************
	 * 	Get All IDs of Table.
	 * 	Used for listing all Entities
	 * 	<code>
	 	int[] IDs = PO.getAllIDs ("AD_PrintFont", null);
		for (int i = 0; i < IDs.length; i++)
		{
			pf = new MPrintFont(Env.getCtx(), IDs[i]);
			System.out.println(IDs[i] + " = " + pf.getFont());
		}
	 *	</code>
	 * 	@param TableName table name (key column with _ID)
	 * 	@param WhereClause optional where clause
	 * 	@return array of IDs or null
	 * 	@param trxName transaction
	 */
	public static int[] getAllIDs (String TableName, String WhereClause,Object[] params, String trxName) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		StringBuffer sql = new StringBuffer("SELECT ");
		sql.append(TableName).append("_ID FROM ").append(TableName);
		if (WhereClause != null && WhereClause.length() > 0)
			sql.append(" WHERE ").append(WhereClause);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), trxName);
			for (int i = 0; params != null && i < params.length ; i++) {
				pstmt.setObject(i+1, params[i]);
			}
			rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new Integer(rs.getInt(1)));
		}
		catch (SQLException e)
		{
			return null;
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//	Convert to array
		int[] retValue = new int[list.size()];
		for (int i = 0; i < retValue.length; i++)
			retValue[i] = ((Integer)list.get(i)).intValue();
		return retValue;
	}	//	getAllIDs
}
 