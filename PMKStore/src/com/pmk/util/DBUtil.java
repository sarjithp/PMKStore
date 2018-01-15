package com.pmk.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.compiere.db.CConnection;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.util.DB;

import com.pmk.shared.OperationException;

public class DBUtil {

	public static void main(String[] args) {
		setDbTarget(null);
		Properties ctx = new Properties();
		ctx.setProperty("#AD_User_ID","1000060");
		ctx.setProperty("#AD_Role_ID","1000047");
		ctx.setProperty("#AD_Org_ID","1000038");
		ctx.setProperty("#AD_Org_Name","PMK Grand Mart");
		ctx.setProperty("#AD_Client_ID","1000044");
		ctx.setProperty("#M_Warehouse_ID","1000023");
		ctx.setProperty("#AD_Language","en_US");
		MOrder order = new MOrder(ctx,0, null);
		order.setC_BPartner_ID(1000114);
		order.setM_PriceList_ID(1000041);
		order.setPaymentRule(MOrder.PAYMENTRULE_Cash);
		order.setIsSOTrx(true);
		order.setC_DocTypeTarget_ID(MDocType.DOCSUBTYPESO_POSOrder);
		order.setDocStatus(MOrder.STATUS_Drafted);
		order.setDocAction(MOrder.DOCACTION_Complete);

		try {
			PoHandler.savePO(order);
		} catch (OperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MOrderLine orderline = new MOrderLine(order);
		orderline.setM_Product_ID(1000033);
		orderline.setQty(BigDecimal.ONE);
		orderline.setPrice();
		orderline.setPrice(new BigDecimal("10"));
		
		try {
			PoHandler.savePO(orderline);
			PoHandler.processIt(order, MOrder.DOCACTION_Complete);
		} catch (OperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setDbTarget(ServletContext servletContext) {
		System.out.println("initiating DB target");
		Properties dbProperties = new Properties();
		InputStream fis = null;
		try {
			fis = servletContext.getResourceAsStream("/WEB-INF/Adempieredb.properties");
//			fis = new FileInputStream("/home/sarjith/workspace/PMKStore/war/WEB-INF/Adempieredb.properties");
			dbProperties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		CConnection cc = new CConnection(null);

		cc.setType("PostgreSQL");
		cc.setDbHost(dbProperties.getProperty("ADEMPIERE_DB_SERVER"));
		cc.setDbPort(dbProperties.getProperty("ADEMPIERE_DB_PORT"));
		cc.setDbName(dbProperties.getProperty("ADEMPIERE_DB_NAME"));
		cc.setDbUid(dbProperties.getProperty("ADEMPIERE_DB_USER"));
		cc.setDbPwd(dbProperties.getProperty("ADEMPIERE_DB_PASSWORD"));
		// cc.setBequeath(cbBequeath.isSelected());
		// cc.setViaFirewall(cbFirewall.isSelected());
		// cc.setFwHost(fwHostField.getText());
		// cc.setFwPort(fwPortField.getText());

		DB.setDBTarget(cc);
	}
}
