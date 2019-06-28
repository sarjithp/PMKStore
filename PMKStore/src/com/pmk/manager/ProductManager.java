package com.pmk.manager;

import java.math.BigDecimal;
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
import com.pmk.shared.TaxCategoryBean;
import com.pmk.util.POSEnv;
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
					+ "' AND m_product_id <> " + bean.getProductId() + " AND ad_client_id = " + Env.getAD_Client_ID(ctx), null);
			if (prids != null && prids.length > 0) {
				throw new OperationException("Code Already exists...!!!");
			}
		}
		
		
		MProduct product = new MProduct(ctx,bean.getProductId(),trxName);
		product.setValue(bean.getProductCode());
		product.setName(bean.getProductCode());
		product.setDescription(bean.getDescription());
		product.setC_UOM_ID(bean.getUomId());
		product.setUUID(bean.getHscode());
		
		//setting default category
		if (bean.isCreateNewCategory()) {
			 MProductCategory category = new MProductCategory(ctx, 0, trxName);
			 category.setName(bean.getNewCategoryName());
			 category.setValue(category.getName());
			 PoHandler.savePO(category);
			 product.setM_Product_Category_ID(category.get_ID());
		} else if (bean.getCategoryId() != null && bean.getCategoryId() != 0 && !bean.isCreateNewCategory()) {
			product.setM_Product_Category_ID(bean.getCategoryId());
		} else {
			int ids[] = MProductCategory.getAllIDs(MProductCategory.Table_Name, "ad_client_id = " + Env.getAD_Client_ID(ctx), null);
			product.setM_Product_Category_ID(ids[0]);
		}
		
		//setting default tax category
		if (bean.getTaxCategoryId() != null && bean.getTaxCategoryId() != 0) {
			product.setC_TaxCategory_ID(bean.getTaxCategoryId());
		} else {
			int[] ids = MTaxCategory.getAllIDs(MTaxCategory.Table_Name, "isdefault = 'Y' and ad_client_id = " + Env.getAD_Client_ID(ctx), null);
			product.setC_TaxCategory_ID(ids[0]);
		}
		
		PoHandler.savePO(product);
		
		//saving sales price
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
		price.setPriceLimit(BigDecimal.ONE);
		price.setPriceStd(bean.getSalesPrice());
		price.setPriceList(bean.getSalesPrice());
		
		PoHandler.savePO(price);
		
		//saving purchase price
		if (bean.getPurchasePrice() != null && bean.getPurchasePrice().signum() > 0) {
			pl = MPriceList.getDefault(ctx, false);
			version = pl.getPriceListVersion(null);
			if (version == null) {
				version = new MPriceListVersion(pl);
				PoHandler.savePO(version);
			}
			
			price = MProductPrice.get(ctx, version.get_ID(), product.get_ID(), trxName);
			if (price == null) {
				price = new MProductPrice(ctx, 0, trxName);
				price.setM_Product_ID(product.get_ID());
				price.setM_PriceList_Version_ID(version.get_ID());
			}
			price.setPriceLimit(BigDecimal.ONE);
			price.setPriceStd(bean.getPurchasePrice());
			price.setPriceList(bean.getPurchasePrice());
			
			PoHandler.savePO(price);
		}
	}

	public static List<KeyNamePair> getKeyNamePairList(Properties ctx, String tableName) {
		String sql = "select " + tableName + "_id,name from " + tableName + " where ad_client_id = " + 
				Env.getAD_Client_ID(ctx) + " Order by isdefault desc ";
		List<KeyNamePair> pair = new ArrayList<KeyNamePair>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DB.prepareStatement(sql, null);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				pair.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
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
		bean.setTaxCategoryId(product.getC_TaxCategory_ID());
		bean.setCategoryId(product.getM_Product_Category_ID());
		bean.setHscode(product.getUUID());
		
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
		MPriceList pricelist = MPriceList.getDefault(ctx, true);
		MPriceListVersion version = pricelist.getPriceListVersion(null);
		String sql = "select p.m_product_id, p.description, pricestd,u.uomsymbol, "
				+ "case when lower(p.description) like lower(?) then 1 else 2 end as rank "
				+ " from m_product p join m_productprice pp on p.m_product_id = pp.m_product_id and pp.m_pricelist_version_id = " 
				+ version.get_ID()
				+ " JOIN c_uom u on p.c_uom_id = u.c_uom_id "
				+ " where p.ad_client_id = " + Env.getAD_Client_ID(ctx) +
				" AND lower(p.description) like lower(?) order by rank, p.description ";
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
				bean.setSalesPrice(rs.getBigDecimal("pricestd"));
				bean.setUomSymbol(rs.getString("uomsymbol"));
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs, stmt);
		}
		
		return list;
	}

	public static List<ProductBean> searchProducts(Properties ctx, String code,
			String description, Integer categoryId) {
		MPriceList pricelist = MPriceList.getDefault(ctx, true);
		MPriceListVersion version = pricelist.getPriceListVersion(null);
		String sql = "select p.m_product_id,p.value, p.description, c.name, sum(qtyonhand-qtyreserved+qtyordered) as stockqty,pricestd "
				+ " from m_product p join m_product_category c on p.m_product_category_id = c.m_product_category_id"
				+ " join m_storage s on p.m_product_id = s.m_product_id "
				+ " join m_productprice pp on p.m_product_id = pp.m_product_id and pp.m_pricelist_version_id = " 
				+ version.get_ID()
				+ " where p.ad_client_id = " + Env.getAD_Client_ID(ctx);
		if (code != null && !code.trim().isEmpty()) {
			sql+= " AND p.value = " + code;
		}
		if (description != null && !description.trim().isEmpty()) {
			sql+= " AND lower(p.description) like '%" + description.toLowerCase() + "%'";
		}
		if (categoryId != null && categoryId != 0) {
			sql+= " AND p.m_product_category_id = " + categoryId;
		}
		sql += " group by p.m_product_id, c.m_product_category_id, pricestd";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ProductBean> list = new ArrayList<ProductBean>();;
		try {
			stmt = DB.prepareStatement(sql,null);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				ProductBean bean = new ProductBean();
				bean.setProductId(rs.getInt("m_product_id"));
				bean.setDescription(rs.getString("description"));
				bean.setNewCategoryName(rs.getString("name"));
				bean.setStockQty(rs.getBigDecimal("stockqty"));
				bean.setProductCode(rs.getString("value"));
				bean.setSalesPrice(rs.getBigDecimal("pricestd"));
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.close(rs, stmt);
		}
		return list;
	}

	public static  int getNextProductCode(Properties ctx) {
		return DB.getSQLValue(null, "select max(coalesce(value::numeric,0)) + 1 from m_product where value ~ '^[0-9\\.]+$' "
				+ " and ad_client_id=" + Env.getAD_Client_ID(ctx)); 
	}

	public static List<TaxCategoryBean> getTaxCategories(Properties ctx) {
		String sql = "select tc.c_taxcategory_id, tc.name, coalesce(sum(childtax.rate),t.rate) as rate "
				+ " from c_taxcategory tc left join c_tax t on tc.c_taxcategory_id = t.c_taxcategory_id and t.isdefault='Y' "
				+ " left join c_tax childtax on childtax.parent_tax_id = t.c_tax_id "
				+ " where tc.ad_client_id = " + Env.getAD_Client_ID(ctx)
				+ " group by tc.c_taxcategory_id,t.c_tax_id "
				+ " order by tc.isdefault desc";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<TaxCategoryBean> list = new ArrayList<TaxCategoryBean>();;
		try {
			stmt = DB.prepareStatement(sql,null);
			rs = stmt.executeQuery();
			while (rs != null && rs.next()) {
				TaxCategoryBean bean = new TaxCategoryBean();
				bean.setKey(rs.getInt("c_taxcategory_id"));
				bean.setName(rs.getString("name"));
				bean.setTaxRate(rs.getBigDecimal("rate"));
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
