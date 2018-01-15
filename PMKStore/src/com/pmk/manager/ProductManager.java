package com.pmk.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.MProductPrice;
import org.compiere.model.MTaxCategory;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.pmk.shared.KeyNamePair;
import com.pmk.shared.OperationException;
import com.pmk.shared.ProductBean;
import com.pmk.util.PoHandler;

public class ProductManager {

	public static int getProductFromBarcode(Properties ctx, String text) {
		String sql = "select m_product_id from m_product where value = ? and ad_client_id = " + Env.getAD_Client_ID(ctx);
		return DB.getSQLValue(null, sql, text);
	}
	
	public static void saveProduct(Properties ctx, ProductBean bean,
			String trxName) throws OperationException {
		if (bean.getProductCode() != null && !bean.getProductCode().trim().isEmpty()) {
			int prids[] = MProduct.getAllIDs(MProduct.Table_Name, "value = '" + bean.getProductCode() 
					+ "' AND ad_client_id = " + Env.getAD_Client_ID(ctx), null);
			if (prids != null && prids.length > 0) {
				throw new OperationException("Code Already exists...!!!");
			}
		}
		
		
		MProduct product = new MProduct(ctx,bean.getProductId(),trxName);
		product.setValue(bean.getProductCode());
		product.setName(bean.getProductCode());
		product.setDescription(bean.getDescription());
		product.setC_UOM_ID(bean.getUomId());
		
		//setting default category
		int ids[] = MProductCategory.getAllIDs(MProductCategory.Table_Name, "ad_client_id = " + Env.getAD_Client_ID(ctx), null);
		product.setM_Product_Category_ID(ids[0]);
		
		//setting default tax category
		ids = MTaxCategory.getAllIDs(MTaxCategory.Table_Name, "isdefault = 'Y' and ad_client_id = " + Env.getAD_Client_ID(ctx), null);
		product.setC_TaxCategory_ID(ids[0]);
		
		PoHandler.savePO(product);
		
		
		MPriceList pl = MPriceList.get(ctx, bean.getPriceListId(), trxName);
		
		MPriceListVersion version = pl.getPriceListVersion(null);
		if (version == null) {
			version = new MPriceListVersion(pl);
			PoHandler.savePO(version);
		}
		
		MProductPrice price = MProductPrice.get(ctx, version.get_ID(), product.get_ID(), trxName);
		if (price == null) {
			price = new MProductPrice(ctx, 0, trxName);
			price.setM_Product_ID(product.get_ID());
			price.setM_PriceList_Version_ID(version.get_ID());
		}
		price.setPriceLimit(bean.getLimitPrice());
		price.setPriceStd(bean.getSalesPrice());
		price.setPriceList(bean.getSalesPrice());
		
		PoHandler.savePO(price);
	}

	public static List<KeyNamePair> getUomList(Properties ctx) {
		String sql = "select c_uom_id,name from c_uom where ad_client_id = " + Env.getAD_Client_ID(ctx) + " Order by isdefault desc ";
		List<KeyNamePair> pair = new ArrayList<KeyNamePair>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DB.prepareStatement(sql, null);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				pair.add(new KeyNamePair(rs.getInt("c_uom_id"), rs.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs, stmt);
		}
		return pair;
	}

	public static ProductBean loadProduct(Properties ctx, int productId, int pricelistId) {
		MProduct product = MProduct.get(ctx, productId);
		ProductBean bean = new ProductBean();
		bean.setProductId(product.get_ID());
		bean.setProductCode(product.getValue());
		bean.setDescription(product.getDescription());
		bean.setUomId(product.getC_UOM_ID());
		bean.setPriceListId(pricelistId);
		
		MPriceList pl = MPriceList.get(ctx, bean.getPriceListId(), null);
		MPriceListVersion version = pl.getPriceListVersion(null);
		
		MProductPrice price = MProductPrice.get(ctx, version.get_ID(), product.get_ID(), null);
		if (price != null) {
			bean.setLimitPrice(price.getPriceLimit());
			bean.setSalesPrice(price.getPriceStd());
		}
		
		return bean;
	}

	public static MProductPrice getProductPrice(Properties ctx, int productId, int priceListId) {
		MPriceList pl = MPriceList.get(ctx, priceListId, null);
		MPriceListVersion version = pl.getPriceListVersion(null);
		
		MProductPrice price = MProductPrice.get(ctx, version.get_ID(), productId, null);
		return price;
	}

	public static List<Suggestion> getProductSuggestions(Properties ctx,
			String text) {
		String sql = "select m_product_id, description, case when lower(description) like lower(?) then 1 else 2 end as rank " +
				" from m_product where ad_client_id = " + Env.getAD_Client_ID(ctx) +
				" AND lower(description) like lower(?) order by rank, description ";
		List<Suggestion> list = new ArrayList<Suggestion>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DB.prepareStatement(sql, null);
			stmt.setString(1, text+"%");
			stmt.setString(2, "%"+ text + "%");
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				ProductBean bean = new ProductBean();
				bean.setProductId(rs.getInt("m_product_id"));
				bean.setDescription(rs.getString("description"));
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
