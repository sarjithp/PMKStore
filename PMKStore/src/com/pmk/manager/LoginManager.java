package com.pmk.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;

import com.pmk.shared.LoginUser;
import com.pmk.shared.OperationException;


public class LoginManager {

	public static LoginUser loginUser(Properties ctx, String userpin) throws OperationException {
		LoginUser user = new LoginUser(); 
		String sql = "select u.ad_user_id,u.ad_client_id,u.name,ur.ad_role_id,t.U_Posterminal_ID,t.ad_org_id,o.name as orgname,m_warehouse_id, " +
				" so_pricelist_id,c_cashbpartner_id,c_cashbook_id from ad_user u left join ad_user_roles ur on u.ad_user_id = ur.ad_user_id AND ur.isactive='Y' " +
				" LEFT JOIN U_Posterminal t on u.ad_client_id = t.ad_client_id and t.isactive='Y' " +
				" LEFT JOIN AD_Org o on t.ad_org_id = o.ad_org_id " +
				" where u.userpin = ? and u.ad_client_id > 0 and u.isactive='Y' ";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = DB.prepareStatement(sql, null);
			stmt.setString(1, userpin);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				if (user.getUserId() != 0) {
					throw new OperationException("More than 1 records found with this user pin!!");
				}
				user.setUserId(rs.getInt("ad_user_id"));
				user.setUserName(rs.getString("name"));
				user.setOrgName(rs.getString("orgname"));
				int roleId = rs.getInt("ad_role_id");
				if (roleId == 0) {
					throw new OperationException("No role set for the user");
				}
				int terminalId = rs.getInt("U_Posterminal_ID");
				if (terminalId == 0) {
					throw new OperationException("No terminal found");
				}
				Env.setContext(ctx, AD_CLIENT_ID, rs.getInt("ad_client_id"));
				Env.setContext(ctx, AD_ROLE_ID, roleId);
				Env.setContext(ctx, AD_ORG_ID, rs.getInt("ad_org_id"));
				Env.setContext(ctx, AD_ORG_NAME, rs.getString("orgname"));
				Env.setContext(ctx, M_WAREHOUSE_ID, rs.getString("m_warehouse_id"));
				Env.setContext(ctx, AD_USER_ID, user.getUserId());
				user.setPriceListId(rs.getInt("so_pricelist_id"));
				user.setCashCustomerId(rs.getInt("c_cashbpartner_id"));
			}
		} catch (Exception e) {
			throw new OperationException(e.getMessage());
		} finally {
			DB.close(rs, stmt);
		}
		if (user.getUserId() == 0) {
			throw new OperationException("Login Failed...!!!");
		}
		
		return user;
	}
	
	public static final String AD_ROLE_ID = "#AD_Role_ID";

	public static final String AD_USER_ID = "#AD_User_ID";

	public static final String AD_ORG_ID = "#AD_Org_ID";

	public static final String AD_CLIENT_ID = "#AD_Client_ID";
	
	public static final String AD_ORG_NAME = "#AD_Org_Name";
	
	public static final String M_WAREHOUSE_ID = "#M_Warehouse_ID";
}
