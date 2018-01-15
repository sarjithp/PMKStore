package com.pmk.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.pmk.shared.CustomerBean;
import com.pmk.shared.OperationException;
import com.pmk.util.PoHandler;

public class CustomerManager {

	public static CustomerBean loadCustomer(Properties ctx, int customerId) {
		CustomerBean bean = new CustomerBean();
		
		MBPartner partner = new MBPartner(ctx,customerId,null);
		bean.setName(partner.getName());
		bean.setCustomerCode(partner.getValue());
		
		MBPartnerLocation bplocation = null;
		if (partner.getPrimaryC_BPartner_Location_ID() > 0) {
			bplocation = partner.getPrimaryC_BPartner_Location();
			bean.setPhone(bplocation.getPhone());
			MLocation location = null;
			if (bplocation.getC_Location_ID() > 0) {
				location = bplocation.getLocation(true);
				bean.setAddress(location.getAddress1());
			}
		}
		
		
		return bean;
	}

	public static void saveCustomer(Properties ctx, CustomerBean bean,
			String trxName) throws OperationException {
		MBPartner partner = new MBPartner(ctx,bean.getCustomerId(),trxName);
		partner.setName(bean.getName());
		if (bean.getCustomerCode() != null && !bean.getCustomerCode().trim().isEmpty()) {
			partner.setValue(bean.getCustomerCode());
		} else {
			partner.setValue(bean.getName());
		}
		
		PoHandler.savePO(partner);
		
		MBPartnerLocation bplocation = null;
		MLocation location = null;
		if (partner.getPrimaryC_BPartner_Location_ID() > 0) {
			bplocation = partner.getPrimaryC_BPartner_Location();
			location = bplocation.getLocation(true);
			location.setAddress1(bean.getAddress());
			PoHandler.savePO(location);
		} else {
			bplocation = new MBPartnerLocation(partner);
			location = new MLocation(ctx, 0, trxName);
			location.setAddress1(bean.getAddress());
			PoHandler.savePO(location);
			bplocation.setC_Location_ID(location.get_ID());
		}
		bplocation.setPhone(bean.getPhone());
		
		PoHandler.savePO(bplocation);
	}

	public static List<Suggestion> getCustomerSuggestions(Properties ctx,
			String text) {
		String sql = "select c_bpartner_id, name, totalopenbalance " +
				" from c_bpartner where ad_client_id = " + Env.getAD_Client_ID(ctx) +
				" AND (lower(name) like lower(?) OR value = ?) order by name";
		List<Suggestion> list = new ArrayList<Suggestion>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DB.prepareStatement(sql, null);
			stmt.setString(1, text + "%");
			stmt.setString(2, text);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				CustomerBean bean = new CustomerBean();
				bean.setCustomerId(rs.getInt("c_bpartner_id"));
				bean.setName(rs.getString("name"));
				bean.setOpenBalance(rs.getBigDecimal("totalopenbalance"));
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs, stmt);
		}
		
		return list;
	}
}
