package com.pmk.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.pmk.client.widgets.BigDecimalBox;
import com.pmk.client.widgets.CustomerSuggestionOracle;
import com.pmk.client.widgets.IntegerListBox;
import com.pmk.client.widgets.ProductSuggestOracle;
import com.pmk.client.widgets.KeyNamePairListBox;
import com.pmk.client.widgets.TaxCatgoryListBox;
import com.pmk.shared.CartItem;
import com.pmk.shared.CustomerBean;
import com.pmk.shared.LoginUser;
import com.pmk.shared.OrderBean;
import com.pmk.shared.PrintSetup;
import com.pmk.shared.ProductBean;

public class StorePOS extends Composite {

	private static StorePOSUiBinder uiBinder = GWT
			.create(StorePOSUiBinder.class);

	private static PosServiceAsync service = GWT.create(PosService.class);

	@UiField(provided = true)
	CellTable<CartItem> cartTable = null;
	ListDataProvider<CartItem> dataList = new ListDataProvider<CartItem>();
	SingleSelectionModel<CartItem> selectionModel = new SingleSelectionModel<CartItem>();
	
	@UiField(provided=true)
	SuggestBox customerSuggest, description;

	interface StorePOSUiBinder extends UiBinder<Widget, StorePOS> {
	}

	class QtyTotal extends Header<String> {
		public QtyTotal() {
			super(new TextCell());
		}

		@Override
		public String getValue() {
			BigDecimal total = BigDecimal.ZERO;
			for (CartItem item : dataList.getList()) {
				total = total.add(item.getQtyOrdered());
			}
			return String.valueOf(total);
		}
	}
	
/*	class ExcludedTotal extends Header<String> {
		public ExcludedTotal() {
			super(new TextCell());
		}

		@Override
		public String getValue() {
			BigDecimal total = BigDecimal.ZERO;
			for (CartItem item : dataList.getList()) {
				total = total.add(item.getExclTotalAmt().setScale(2, RoundingMode.HALF_UP));
			}
			return String.valueOf(total);
		}
	}
	
	class TaxTotal extends Header<String> {
		public TaxTotal() {
			super(new TextCell());
		}

		@Override
		public String getValue() {
			BigDecimal total = BigDecimal.ZERO;
			for (CartItem item : dataList.getList()) {
				total = total.add(item.getTotalTaxAmt());
			}
			return String.valueOf(total);
		}
	}*/
	
	class LineTotal extends Header<String> {
		public LineTotal() {
			super(new TextCell());
		}

		@Override
		public String getValue() {
			BigDecimal total = BigDecimal.ZERO;
			for (CartItem item : dataList.getList()) {
				total = total.add(item.getPriceEntered().multiply(item.getQtyOrdered()));
			}
			grantTotalBox.setText(String.valueOf(total));
			totalCountBox.setText(String.valueOf(dataList.getList().size()));
			return String.valueOf(total);
		}
	}
	
	class HistoryTotal extends Header<String> {
		public HistoryTotal() {
			super(new TextCell());
		}

		@Override
		public String getValue() {
			BigDecimal total = BigDecimal.ZERO;
			for (OrderBean item : salesDataList.getList()) {
				total = total.add(item.getGrandTotal());
			}
			return String.valueOf(total);
		}
	}

	private void initScreenWidgets() {
		initCartTable();
		initSalesHistoryDataTable();
		initSuggestBoxes();
		initProductListingTable();
		initCustomerListingTable();
	}
	
	private void addToCartFromProductId(int productId) {
		service.addProductToCart(productId, user.getPriceListId(), getCustomerID(), new AsyncCallback<CartItem>() {
			@Override
			public void onFailure(Throwable caught) {
				displayErrorMesasge(caught.getMessage(), barcode);
			}
			@Override
			public void onSuccess(CartItem result) {
				addOrUpdateDataList(result);
			}
		});
	}
	
	private void initSuggestBoxes() {
		SuggestOracle oracle = new ProductSuggestOracle();
		description = new SuggestBox(oracle);
		description.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				ProductBean bean = (ProductBean) event.getSelectedItem();
				if (bean.getProductId() != 0) {
					addToCartFromProductId(bean.getProductId());
					addedFromBarcode = false;
				}
			}
		});
		
		oracle = new CustomerSuggestionOracle();
		customerSuggest = new SuggestBox(oracle);
		customerSuggest.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				CustomerBean bean = (CustomerBean) event.getSelectedItem();
				if (bean.getCustomerId() != 0) {
					salesCustomerId = bean.getCustomerId();
					deliveryLocationId = bean.getDeliveryLocationId();
					deliveryAddress.setValue(bean.getAddress(),false);
					deliveryPhone.setValue(bean.getPhone());
					customerBalance.setText(bean.getOpenBalance() == null ? "0" : String.valueOf(bean.getOpenBalance()));
					saleTypeList.setSelectedIndex(1);
				}
			}
		});
	}
		
	private void initCartTable() {
		cartTable = new CellTable<CartItem>(Integer.MAX_VALUE,
				CartItem.SALE_KEY_PROVIDER);
		cartTable.setWidth("100%");

		cartTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		
		Column<CartItem, String> column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return object.getBarcode();
			}
		};
		cartTable.addColumn(column, "Code");

		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return object.getDescription();
			}
		};
		cartTable.addColumn(column, "Description");
		
		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return object.getUom();
			}
		};
//		cartTable.addColumn(column, "Uom");
		
		Header<String> footer = new QtyTotal();
		footer.setHeaderStyleNames("textAlignRight");
		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return String.valueOf(object.getQtyOrdered());
			}
		};
		cartTable.addColumn(column, new TextHeader("Qty"), footer);
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return String.valueOf(object.getPriceEntered());
			}
		};
		cartTable.addColumn(column, "Price");
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);



/*		footer = new ExcludedTotal();
		footer.setHeaderStyleNames("textAlignRight");
		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return String.valueOf(object.getExclTotalAmt().setScale(2, RoundingMode.HALF_UP));
			}
		};
		cartTable.addColumn(column, new TextHeader("Gross Amt"), footer);
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		footer = new TaxTotal();
		footer.setHeaderStyleNames("textAlignRight");
		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return String.valueOf(object.getTotalTaxAmt().setScale(2, RoundingMode.HALF_UP));
			}
		};
		cartTable.addColumn(column, new TextHeader("Tax Amt"), footer);
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);*/
		
		footer = new LineTotal();
		footer.setHeaderStyleNames("textAlignRight");
		column = new TextColumn<CartItem>() {
			@Override
			public String getValue(CartItem object) {
				return String.valueOf(object.getPriceEntered().multiply(object.getQtyOrdered()));
			}
		};
		cartTable.addColumn(column, new TextHeader("Total"), footer);
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		dataList.addDataDisplay(cartTable);
		
		cartTable.setSelectionModel(selectionModel);
		
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				CartItem item = selectionModel.getSelectedObject();
				if (item != null) {
					qtyBox.setValue(item.getQtyOrdered());
					priceBox.setValue(item.getPriceEntered().setScale(2, RoundingMode.HALF_UP));
					qtyBox.setFocus(true);
					qtyBox.selectAll();
				}
			}
		});
	}

	public StorePOS() {
		initScreenWidgets();
		initWidget(uiBinder.createAndBindUi(this));
		loginPopup.center();
		userpin.setFocus(true);
		initShortcuts();
		dateOrdered.setFormat(new DateBox.DefaultFormat 
				(DateTimeFormat.getFormat("dd MMM, yyyy")));
		salesHistoryFromDate.setFormat(new DateBox.DefaultFormat 
				(DateTimeFormat.getFormat("dd MMM, yyyy")));
		salesHistoryToDate.setFormat(new DateBox.DefaultFormat 
				(DateTimeFormat.getFormat("dd MMM, yyyy")));
	}
	
	private void initShortcuts() {
		Event.addNativePreviewHandler(new Event.NativePreviewHandler() {

			@Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				NativeEvent ne = event.getNativeEvent();
				
				if (!ne.getType().equals(KeyDownEvent.getType().getName())) {
					return;
				}
				
				if (loadingPopup.isShowing()) {
					return;
				}
				
				if (ne.getCtrlKey() &&  ne.getKeyCode() == ' ') { //Ctrl+Space - checkout
					ne.preventDefault();
					completeBtn(null);
           			return;
				} else if (ne.getKeyCode()==112) { //F1 - barcode
					ne.preventDefault();
					barcode.setFocus(true);
				} else if (ne.getKeyCode()==113) { //F2 -Product Name
					ne.preventDefault();
					description.setFocus(true);
				} else if (ne.getKeyCode()==114) { //F3 -Description
					ne.preventDefault();
					qtyBox.setFocus(true);
				} else if(ne.getKeyCode()==115){//F4 -Quantity
					ne.preventDefault();
					priceBox.setFocus(true);
				} else if(ne.getKeyCode()==116){//F4 -Quantity
					ne.preventDefault();
					customerSuggest.setFocus(true);
				} else if (ne.getAltKey() && ne.getKeyCode()=='C') {//Cash
					ne.preventDefault();
					saleTypeList.setSelectedIndex(0);
				} else if (ne.getAltKey() && ne.getKeyCode()=='D') {//Cash
					ne.preventDefault();
					saleTypeList.setSelectedIndex(1);
				} else if (ne.getAltKey() && ne.getKeyCode()=='D') {//Cash
					ne.preventDefault();
					saleTypeList.setSelectedIndex(1);
				} else if(ne.getCtrlKey()&&ne.getKeyCode()==KeyCodes.KEY_DELETE){//Ctrl+DEL - Clear
		           	ne.preventDefault();
		           	clearAll(null);
		        } else if(ne.getCtrlKey()&&ne.getKeyCode()=='P'){//Ctrl+P - print
		           	ne.preventDefault();
		           	if (successPopup.isShowing()) {
		           		successPopupPrintBtn(null);
		           	} else if (printOrderPopup.isShowing()) {
		           		printOrderOKBtn(null);
		           	} else {
		           		printOrder(null);
		           	}
		        } 
				
			}
		});
	}
	
	@UiHandler("userpin")
	void userpinEnter(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			login(null);
		}
	}
	
	@UiHandler("loginBtn")
	void login(ClickEvent e) {
		if (!userpin.getText().trim().isEmpty()) {
			service.getLoginUser(userpin.getText(),new AsyncCallback<LoginUser>() {
				@Override
				public void onFailure(Throwable caught) {
					userpinErrorMsg.setInnerText(caught.getMessage());
					userpin.setFocus(true);
				}
				@Override
				public void onSuccess(LoginUser result) {
					Window.setTitle(result.getOrgName());
					loginPopup.hide();
					settingsBtn.setVisible(result.getUserName().equalsIgnoreCase("admin"));
					uomListBox.refresh();
					productListCategory.refresh();
					taxCategoryId.refresh();
					categoryId.refresh();
					userName.setText(result.getUserName());
					orgName.setText(result.getOrgName());
					user = result;
					clearAll(null);
				}
			});
		}
	}
	
	LoginUser user = null;
	
	@UiField
	TextBox barcode, grantTotalBox, userpin, userName, orgName, updateProductCode, updateProductDescription, customerCode, customerName,
	customerAddress, customerPhone, customerBalance, totalCountBox, newCategoryName, productListCode, productListDescr, printDevice,
	printWidth, customerTaxNo, hscode, orderNo, customerListCode,customerListName, deliveryAddress, deliveryPhone, orderDescription;
	
	@UiField
	BigDecimalBox updateProductSalesPrice,updateProductLimitPrice, qtyBox, priceBox, updateProductPurchasePrice, customerPayAmt,
	updateProductSalesPriceIncl, updateProductPurchasePriceIncl;
	
	@UiField
	KeyNamePairListBox uomListBox, categoryId, productListCategory;
	
	@UiField
	TaxCatgoryListBox taxCategoryId;
	
	@UiField
	IntegerListBox orderNoList;
	
	@UiField
	Hidden updateProductId, customerId;
	
	@UiField
	ListBox saleTypeList, historySaleTypeList, printTypeList;
	
	@UiField
	SpanElement userpinErrorMsg, successOrderNo;
	
	@UiField
	SimpleCheckBox newCategory, autoProductCode;
	
	private int getCustomerID() {
		if (salesCustomerId > 0) {
			return salesCustomerId;
		}
		return user.getCashCustomerId();
	}

	boolean addedFromBarcode = true;
	
	@UiHandler("barcode")
	void onbarcode(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER && !barcode.getText().trim().isEmpty()) {
			loadingPopup.center();
			service.addProductToCartFromBarcode(barcode.getText().trim(), user.getPriceListId(), getCustomerID(), new AsyncCallback<CartItem>() {
				public void onFailure(Throwable caught) {
					loadingPopup.hide();
					displayErrorMesasge(caught.getMessage(),barcode);
				}
				public void onSuccess(CartItem result) {
					loadingPopup.hide();
					addOrUpdateDataList(result);
					addedFromBarcode = true;
				};
			});
			barcode.setText("");
		}
	}

	protected void addOrUpdateDataList(CartItem result) {
		int index = dataList.getList().indexOf(result);
		if (index == -1) {
			dataList.getList().add(result);
		} else {
			CartItem oldItem = dataList.getList().get(index);
			oldItem.setQtyOrdered(oldItem.getQtyOrdered().add(result.getQtyOrdered()));
			result = oldItem;
		}
		dataList.flush();
		dataList.refresh();
		selectionModel.setSelected(result, true);
	}

	@UiHandler("qtyBox")
	void qtyUpdate(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER && !qtyBox.getText().trim().isEmpty()) {
			CartItem item = selectionModel.getSelectedObject();
			BigDecimal qty = new BigDecimal(qtyBox.getText());
			if (item != null && qty.compareTo(item.getQtyOrdered()) != 0) {
				item.setQtyOrdered(qty);
				dataList.flush();
				dataList.refresh();
			}
			priceBox.setFocus(true);
			priceBox.selectAll();
		}
	}
	
	@UiHandler("priceBox")
	void priceUpdate(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER && !priceBox.getText().trim().isEmpty()) {
			doPriceUpdate();
		}
	}
	
	void doPriceUpdate() {
		CartItem item = selectionModel.getSelectedObject();
		BigDecimal price = priceBox.getValue();
		if (item != null) {
//			price = price.multiply(ONE_HUNDRED).divide(ONE_HUNDRED.add(item.getTaxRate()),4, RoundingMode.HALF_UP);
			item.setPriceEntered(price);
			dataList.flush();
			dataList.refresh();
		}
		(addedFromBarcode ? barcode : description).setFocus(true);
	}
	
	@UiHandler("deliveryAddress")
	void deliveryAddress(ValueChangeEvent<String> v) {
		deliveryLocationId = 0;
	}
	
	Focusable defaultFocus = null;
	int salesCustomerId = 0, deliveryLocationId = 0;
	int editOrderId = 0;
	int lastCompletedOrderId = 0;
	
	@UiField 
	Button errorCloseBtn, generalSuccessPopupCloseBtn, successPopupCloseBtn, salesHistory, printOrderOKBtn,settingsBtn;
	
	@UiField 
	DivElement errorMsg, generralSuccessMsg;
	
	@UiField
	DialogBox errorPopup, successPopup, loginPopup, updateProductPopup, generalSuccessPopup, loadingPopup, customerPopup, customerPaymentPopup,
	salesHistoryPopup, productListPopup, printOrderPopup, settingsPopup, customerListPopup;
	
	protected void displayErrorMesasge(String message, Focusable focus) {
		errorPopup.center();
		errorMsg.setInnerText(message);
		defaultFocus = focus;
		errorCloseBtn.setFocus(true);
	}
	
	@UiHandler("errorCloseBtn")
	void errorCloseBtn(ClickEvent e) {
		errorPopup.hide();
		(defaultFocus != null ? defaultFocus : barcode).setFocus(true); 
	}

	@UiHandler("clearAllBtn")
	void clearAll(ClickEvent e) {
		dataList.getList().clear();
		dataList.flush();
		selectionModel.clear();
		qtyBox.setText("");
		priceBox.setText("");
		selectionModel.clear();
		salesCustomerId = 0;
		editOrderId = 0;
		deliveryLocationId = 0;
		deliveryAddress.setValue("");
		deliveryPhone.setValue("");
		saleTypeList.setSelectedIndex(0);
		customerBalance.setValue("0");
		customerSuggest.setText("");
		lastCompletedOrderId = 0;
		orderNo.setValue("");
		dateOrdered.setValue(new Date());
		orderDescription.setValue("");
		barcode.setFocus(true);
	}
	
	private OrderBean setOrderDetails() {
		OrderBean order = new OrderBean();
		order.setCustomerId(getCustomerID());
		order.setPaymentType(saleTypeList.getValue(saleTypeList.getSelectedIndex()));
		order.setDeliveryAddress(deliveryAddress.getValue());
		order.setDeliveryLocationId(deliveryLocationId);
		order.setPriceListId(user.getPriceListId());
		order.setOrderNo(orderNo.getValue());
		order.setDateOrdered(dateOrdered.getValue());
		order.setOrderId(editOrderId);
		order.setDescription(orderDescription.getValue());
		return order;
	}
	
	private OrderBean setOrderDetailsToScreen(OrderBean order) {
		editOrderId = order.getOrderId();
		customerSuggest.setValue(order.getCustomerName(), false);
		salesCustomerId = order.getCustomerId();
		deliveryAddress.setValue(order.getDeliveryAddress());
		deliveryLocationId = order.getDeliveryLocationId();
		orderNo.setValue(order.getOrderNo());
		dateOrdered.setValue(order.getDateOrdered());
		orderDescription.setValue(order.getDescription());
		return order;
	}
	
	@UiHandler("completeBtn")
	void completeBtn(ClickEvent e) {
		if (saleTypeList.getSelectedIndex() == 1 && salesCustomerId == 0) {
			displayErrorMesasge("Choose a customer for credit sale", customerSuggest);
			return;
		}
		
		List<CartItem> items = new ArrayList<CartItem>();
		items.addAll(dataList.getList());
		if (items.size() == 0) {
			displayErrorMesasge("Please add products!!!", barcode);
			return;
		}
		loadingPopup.center();
		service.completeOrder(items, setOrderDetails(), new AsyncCallback<OrderBean>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), barcode);
			}
			@Override
			public void onSuccess(OrderBean order) {
				loadingPopup.hide();
				successPopup.center();
				successOrderNo.setInnerText(order.getOrderNo());
				successPopupCloseBtn.setFocus(true);
				lastCompletedOrderId = order.getOrderId();
			}
		});
	}
	
	@UiHandler("successPopupPrintBtn")
	void successPopupPrintBtn(ClickEvent e) {
		if (lastCompletedOrderId != 0) {
			service.printOrder(lastCompletedOrderId, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					displayErrorMesasge(caught.getMessage(), barcode);
				}
				@Override
				public void onSuccess(Void result) {
					successPopup.hide();
					clearAll(null);
				}
			});
		}
	}
	
	@UiHandler("printOrderBtn")
	void printOrder(ClickEvent e) {
		loadingPopup.center();
		service.loadPreviousOrders(new AsyncCallback<List<OrderBean>>() {
			@Override
			public void onFailure(Throwable caught) {
				displayErrorMesasge(caught.getMessage(), defaultFocus);
			}
			@Override
			public void onSuccess(List<OrderBean> result) {
				for(OrderBean bean : result) {
					orderNoList.addItem(bean.getOrderNo() + "  (Total : " + bean.getGrandTotal() + ")", String.valueOf(bean.getOrderId()));
				}
				loadingPopup.hide();
				printOrderPopup.center();
				printOrderOKBtn.setFocus(true);
			}
		});
	}
	
	@UiHandler("printOrderOKBtn")
	void printOrderOKBtn(ClickEvent e) {
		if (orderNoList.getValue() != null && orderNoList.getValue() > 0) {
			printOrder(orderNoList.getValue());
			printOrderPopup.hide();
		}
	}
	
	void printOrder(int orderId) {
		if (orderId > 0) {
			service.printOrder(orderId, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					displayErrorMesasge(caught.getMessage(), barcode);
				}
				@Override
				public void onSuccess(Void result) {
					displaySuccessMsg("Pdf saved successfully", barcode);
				}
			});
		}
	}

	@UiHandler("successPopupCloseBtn")
	void newOrder(ClickEvent e) {
		successPopup.hide();
		clearAll(null);
	}
	
	@UiHandler("addProductBtn")
	void addProductBtn(ClickEvent e) {
		ProductBean bean = new ProductBean();
		initEditProduct(bean);
	}
	
	@UiHandler("editProductBtn")
	void editProduct(ClickEvent e) {
		CartItem item = selectionModel.getSelectedObject();
		if (item != null && item.getProductId() != 0) {
			editProduct(item.getProductId());
		}
	}
	
	void editProduct(int productId) {
		loadingPopup.center();
		service.loadProduct(productId,user.getPriceListId(), new AsyncCallback<ProductBean>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), barcode);
			}
			@Override
			public void onSuccess(ProductBean result) {
				loadingPopup.hide();
				initEditProduct(result);
			}
		});
		
	}
	
	@UiHandler("updateProductPopupCancel") 
	void updateProductPopupCancel(ClickEvent e) {
		updateProductPopup.hide();
		barcode.setFocus(true);
	}
	
	@UiHandler("updateProductPopupSave")
	void updateProductPopupSave(ClickEvent e) {
		ProductBean bean = new ProductBean();
		bean.setProductId(Integer.valueOf(updateProductId.getValue()));
		final boolean newProduct = bean.getProductId() <= 0;
		bean.setProductCode(updateProductCode.getText());
		bean.setDescription(updateProductDescription.getValue());
		bean.setSalesPrice(updateProductSalesPrice.getValue());
		bean.setLimitPrice(updateProductLimitPrice.getValue());
		bean.setUomId(uomListBox.getValue());
		bean.setPriceListId(user.getPriceListId());
		bean.setCategoryId(categoryId.getValue());
		bean.setCreateNewCategory(newCategory.getValue());
		bean.setNewCategoryName(newCategoryName.getText());
		bean.setTaxCategoryId(taxCategoryId.getValue());
		bean.setHscode(hscode.getText());
		loadingPopup.center();
		service.saveProduct(bean, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), updateProductCode);
			}
			@Override
			public void onSuccess(Void result) {
				loadingPopup.hide();
				displaySuccessMsg("Product Saved Successfully", barcode);
				updateProductPopup.hide();
				if (!newProduct) {
					priceBox.setValue(updateProductSalesPriceIncl.getValue());
					doPriceUpdate();
				}
			}
		});
	}

	protected void initEditProduct(ProductBean result) {
		updateProductPopup.center();
		updateProductCode.setValue(result.getProductCode() == null ? ""  : result.getProductCode());
		updateProductDescription.setValue(result.getDescription() == null ? "" : result.getDescription());
		updateProductSalesPrice.setValue(result.getSalesPrice() == null ? BigDecimal.ZERO : result.getSalesPrice());
		updateProductLimitPrice.setValue(result.getLimitPrice() == null ? BigDecimal.ZERO : result.getLimitPrice());
		updateProductId.setValue(String.valueOf(result.getProductId()));
		updateProductPurchasePrice.setValue(result.getPurchasePrice() == null ? BigDecimal.ZERO : result.getPurchasePrice());
		hscode.setValue(result.getHscode() == null ? "" : result.getHscode());
		updateProductSalesPrice(null);
		updateProductPurchasePrice(null);
		if (result.getUomId() != null && result.getUomId() != 0) {
			uomListBox.setValue(result.getUomId());
		}
		if (result.getCategoryId() != null && result.getCategoryId() != 0) {
			categoryId.setValue(result.getCategoryId());
		}
		if (result.getTaxCategoryId() != null && result.getTaxCategoryId() != 0) {
			taxCategoryId.setValue(result.getTaxCategoryId());
		}
		newCategory.setValue(false,true);
		categoryId.setVisible(true);
		newCategoryName.setVisible(false);
		updateProductCode.setFocus(true);
		if (autoProductCode.getValue()) {
			getAutoProductCode();
			updateProductDescription.setFocus(true);
		}
	}

	@UiHandler("newCategory")
	void newCategory(ValueChangeEvent<Boolean> e) {
		categoryId.setVisible(!e.getValue());
		newCategoryName.setVisible(e.getValue());
	}
	
	@UiHandler("autoProductCode")
	void autoProductCode(ValueChangeEvent<Boolean> e) {
		if (e.getValue()) {
			getAutoProductCode();
		}
	}
	void getAutoProductCode() {
		service.getNextProductCode(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(Integer result) {
				updateProductCode.setText(String.valueOf(result));
			};
		});
	}
	static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	@UiHandler("updateProductSalesPrice")
	void updateProductSalesPrice(KeyUpEvent e) {
		BigDecimal price = updateProductSalesPrice.getValue() != null ? updateProductSalesPrice.getValue() : BigDecimal.ZERO;
		BigDecimal taxRate = taxCategoryId.getSelectedRate() != null ? taxCategoryId.getSelectedRate() : BigDecimal.ZERO;
		price = price.add(price.multiply(taxRate).divide(ONE_HUNDRED));
		updateProductSalesPriceIncl.setValue(price.setScale(2,RoundingMode.HALF_UP));
	}
	
	@UiHandler("updateProductSalesPriceIncl")
	void updateProductSalesPriceIncl(KeyUpEvent e) {
		BigDecimal price = updateProductSalesPriceIncl.getValue() != null ? updateProductSalesPriceIncl.getValue() : BigDecimal.ZERO;
		BigDecimal taxRate = taxCategoryId.getSelectedRate() != null ? taxCategoryId.getSelectedRate() : BigDecimal.ZERO;
		price = price.multiply(ONE_HUNDRED).divide(ONE_HUNDRED.add(taxRate),2,RoundingMode.HALF_UP);
		updateProductSalesPrice.setValue(price.setScale(2,RoundingMode.HALF_UP));
	}
	
	@UiHandler("updateProductPurchasePrice")
	void updateProductPurchasePrice(KeyUpEvent e) {
		BigDecimal price = updateProductPurchasePrice.getValue() != null ? updateProductPurchasePrice.getValue() : BigDecimal.ZERO;
		BigDecimal taxRate = taxCategoryId.getSelectedRate() != null ? taxCategoryId.getSelectedRate() : BigDecimal.ZERO;
		price = price.add(price.multiply(taxRate).divide(ONE_HUNDRED));
		updateProductPurchasePriceIncl.setValue(price.setScale(2,RoundingMode.HALF_UP));
	}
	
	@UiHandler("updateProductPurchasePriceIncl")
	void updateProductPurchasePriceIncl(KeyUpEvent e) {
		BigDecimal price = updateProductPurchasePriceIncl.getValue() != null ? updateProductPurchasePriceIncl.getValue() : BigDecimal.ZERO;
		BigDecimal taxRate = taxCategoryId.getSelectedRate() != null ? taxCategoryId.getSelectedRate() : BigDecimal.ZERO;
		price = price.multiply(ONE_HUNDRED).divide(ONE_HUNDRED.add(taxRate),2,RoundingMode.HALF_UP);
		updateProductPurchasePrice.setValue(price.setScale(2,RoundingMode.HALF_UP));
	}
	
	protected void initEditCustomer(CustomerBean result) {
		customerPopup.center();
		customerId.setValue(String.valueOf(result.getCustomerId()));
		customerCode.setValue(result.getCustomerCode() == null ? "" : result.getCustomerCode());
		customerName.setValue(result.getName() == null ? "" : result.getName());
		customerAddress.setValue(result.getAddress() == null ? "" : result.getAddress());
		customerPhone.setValue(result.getPhone() == null ? "" : result.getPhone());
		customerTaxNo.setValue(result.getCustomerTaxNo() == null ? "" : result.getCustomerTaxNo());
		customerCode.setFocus(true);
	}
	
	@UiHandler("addCustomterBtn")
	void addCustomterBtn(ClickEvent e) {
		CustomerBean bean = new CustomerBean();
		initEditCustomer(bean);
	}
	
	@UiHandler("editCustomterBtn")
	void editCustomterBtn(ClickEvent e) {
		if (salesCustomerId != 0) {
			initEditCustomer(salesCustomerId);
		}
	}
	
	void initEditCustomer(int customerId) {
		loadingPopup.center();
		service.loadCustomer(salesCustomerId, new AsyncCallback<CustomerBean>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), null);
			}
			@Override
			public void onSuccess(CustomerBean result) {
				loadingPopup.hide();
				initEditCustomer(result);
			}
		});
	}
	
	@UiHandler("customerPopupCancel")
	void customerPopupCancel(ClickEvent e) {
		customerPopup.hide();
	}
	
	@UiHandler("customerPopupSave")
	void customerPopupSave(ClickEvent e) {
		CustomerBean bean = new CustomerBean();
		bean.setCustomerId(Integer.valueOf(customerId.getValue()));
		bean.setCustomerCode(customerCode.getValue());
		bean.setName(customerName.getValue());
		bean.setAddress(customerAddress.getValue());
		bean.setPhone(customerPhone.getValue());
		bean.setCustomerTaxNo(customerTaxNo.getValue());
		loadingPopup.center();
		service.saveCustomer(bean, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), customerCode);
			}
			@Override
			public void onSuccess(Void result) {
				loadingPopup.hide();
				displaySuccessMsg("Customer Saved Successfully", barcode);
				customerPopup.hide();
			}
		});
	}
	
	@UiHandler("generalSuccessPopupCloseBtn")
	void generalSuccessPopupCloseBtn(ClickEvent e) {
		generalSuccessPopup.hide();
		(defaultFocus != null? defaultFocus : barcode).setFocus(true);
	}
	
	void displaySuccessMsg(String message, Focusable nextfocus) {
		defaultFocus = nextfocus;
		generralSuccessMsg.setInnerText(message);
		generalSuccessPopupCloseBtn.setFocus(true);
	}
	
	@UiHandler("customerPay")
	void customerPay(ClickEvent e) {
		if (salesCustomerId == 0) {
			displayErrorMesasge("Choose a customer", customerSuggest);
			return;
		}
		customerPaymentPopup.center();
		customerPayAmt.setValue(BigDecimal.ZERO);
		customerPayAmt.setFocus(true);
		customerPayAmt.selectAll();
	}
	
	@UiHandler("customerPayCancel") 
	void customerPayCancel(ClickEvent e) {
		customerPaymentPopup.hide();
	}
	
	@UiHandler("customerPayAmt") 
	void customerPayCancel(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			customerPaySave(null);
		}
	}
	
	@UiHandler("customerPaySave")
	void customerPaySave(ClickEvent e) {
		if (customerPayAmt.getValue().compareTo(BigDecimal.ZERO) > 0) {
			loadingPopup.center();
			service.savePaymentDetails(salesCustomerId, customerPayAmt.getValue(), new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					loadingPopup.hide();
					displayErrorMesasge(caught.getMessage(), customerPayAmt);
				}
				@Override
				public void onSuccess(Void result) {
					clearAll(null);
					loadingPopup.hide();
					customerPaymentPopup.hide();
					displaySuccessMsg("Payment saved successfully", barcode);
				}
			});
		}
	}
	
	@UiField
	DateBox salesHistoryFromDate, salesHistoryToDate, dateOrdered;
	
	@UiField(provided=true)
	CellTable<OrderBean> salesHistoryTable = null;
	@UiField(provided=true)
	CellTable<ProductBean> productListTable = null;
	@UiField(provided=true)
	CellTable<CustomerBean> customerListTable = null;
	@UiField(provided = true)
	SimplePager pager, productPager, customerPager;
	
	ListDataProvider<OrderBean> salesDataList = new ListDataProvider<OrderBean>();
	ListDataProvider<ProductBean> productList = new ListDataProvider<ProductBean>();
	ListDataProvider<CustomerBean> customerList = new ListDataProvider<CustomerBean>();

	private void initSalesHistoryDataTable() {
		salesHistoryTable = new CellTable<OrderBean>(25);
		Column<OrderBean, String> column = new TextColumn<OrderBean>() {
			@Override
			public String getValue(OrderBean object) {
				return object.getOrderNo();
			}
		};
		salesHistoryTable.addColumn(column,"Order No");
		
		column = new TextColumn<OrderBean>() {
			@Override
			public String getValue(OrderBean object) {
				return object.getInvoiceNo();
			}
		};
		salesHistoryTable.addColumn(column,"Invoice No");
		
		column = new TextColumn<OrderBean>() {
			@Override
			public String getValue(OrderBean object) {
				return object.getPaymentType();
			}
		};
		salesHistoryTable.addColumn(column,"Payment Type");
		
		column = new TextColumn<OrderBean>() {
			@Override
			public String getValue(OrderBean object) {
				return object.getCustomerName();
			}
		};
		salesHistoryTable.addColumn(column,"Customer");
		
		column = new TextColumn<OrderBean>() {
			@Override
			public String getValue(OrderBean object) {
				return object.getProductDescription();
			}
		};
		salesHistoryTable.addColumn(column,"Products");
		
		Header<String> footer = new HistoryTotal();
		footer.setHeaderStyleNames("textAlignRight");
		column = new TextColumn<OrderBean>() {
			@Override
			public String getValue(OrderBean object) {
				return String.valueOf(object.getGrandTotal());
			}
		};
		salesHistoryTable.addColumn(column, new TextHeader("Grand Total"), footer);
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		Column<OrderBean,String> buttonColumn = new Column<OrderBean,String>(new ButtonCell()) {
			@Override
			public String getValue(OrderBean object) {
				return "PRINT";
			}
		};
		salesHistoryTable.addColumn(buttonColumn, "PRINT");
		buttonColumn.setFieldUpdater(new FieldUpdater<OrderBean, String>() {
			@Override
			public void update(int index, OrderBean object, String value) {
				printOrder(object.getOrderId());
			}
		});
		
		buttonColumn = new Column<OrderBean,String>(new ButtonCell()) {
			@Override
			public String getValue(OrderBean object) {
				return "EDIT";
			}
		};
		salesHistoryTable.addColumn(buttonColumn, "EDIT");
		buttonColumn.setFieldUpdater(new FieldUpdater<OrderBean, String>() {
			@Override
			public void update(int index, OrderBean object, String value) {
				initEditOrder(object.getOrderId());
				salesHistoryClose(null);
			}
		});
		
		salesDataList.addDataDisplay(salesHistoryTable);
		
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, true, 0,true);
		pager.setDisplay(salesHistoryTable);
		pager.setPageSize(25);
	}
		
	private void initProductListingTable(){
		//setting product list table
		productListTable = new CellTable<ProductBean>(25);
		Column<ProductBean,String> productColumn = new TextColumn<ProductBean>() {
			@Override
			public String getValue(ProductBean object) {
				return object.getNewCategoryName();
			}
		};
		productListTable.addColumn(productColumn, "Category");
		
		productColumn = new TextColumn<ProductBean>() {
			@Override
			public String getValue(ProductBean object) {
				return object.getProductCode();
			}
		};
		productListTable.addColumn(productColumn, "Code");
		
		productColumn = new TextColumn<ProductBean>() {
			@Override
			public String getValue(ProductBean object) {
				return object.getDescription();
			}
		};
		productListTable.addColumn(productColumn, "Description");
		
		productColumn = new TextColumn<ProductBean>() {
			@Override
			public String getValue(ProductBean object) {
				return String.valueOf(object.getSalesPrice());
			}
		};
		productListTable.addColumn(productColumn, "Price");
		
		productColumn = new TextColumn<ProductBean>() {
			@Override
			public String getValue(ProductBean object) {
				return String.valueOf(object.getStockQty());
			}
		};
		productListTable.addColumn(productColumn, "Stock Qty");
		
		productColumn = new Column<ProductBean,String>(new ButtonCell()) {
			@Override
			public String getValue(ProductBean object) {
				return "EDIT";
			}
		};
		productListTable.addColumn(productColumn, "Edit");
		productColumn.setFieldUpdater(new FieldUpdater<ProductBean, String>() {
			@Override
			public void update(int index, ProductBean object, String value) {
				editProduct(object.getProductId());
			}
		});
		
		productColumn = new Column<ProductBean,String>(new ButtonCell()) {
			@Override
			public String getValue(ProductBean object) {
				return "ADD TO CART";
			}
		};
		productListTable.addColumn(productColumn, "Add to Cart");
		productColumn.setFieldUpdater(new FieldUpdater<ProductBean, String>() {
			@Override
			public void update(int index, ProductBean object, String value) {
				addToCartFromProductId(object.getProductId());
				productListPopup.hide();
			}
		});
		
		productList.addDataDisplay(productListTable);
		
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		productPager = new SimplePager(TextLocation.CENTER, pagerResources, true, 0,true);
		productPager.setDisplay(productListTable);
		productPager.setPageSize(25);
	}
	
	private void initCustomerListingTable(){
		//setting product list table
		customerListTable = new CellTable<CustomerBean>(25);
		Column<CustomerBean,String> column = new TextColumn<CustomerBean>() {
			@Override
			public String getValue(CustomerBean object) {
				return object.getCustomerCode();
			}
		};
		customerListTable.addColumn(column, "Code");
		
		column = new TextColumn<CustomerBean>() {
			@Override
			public String getValue(CustomerBean object) {
				return object.getName();
			}
		};
		customerListTable.addColumn(column, "Name");
		
		column = new TextColumn<CustomerBean>() {
			@Override
			public String getValue(CustomerBean object) {
				return object.getAddress();
			}
		};
		customerListTable.addColumn(column, "Address");
		
		column = new TextColumn<CustomerBean>() {
			@Override
			public String getValue(CustomerBean object) {
				return object.getPhone();
			}
		};
		customerListTable.addColumn(column, "Phone");
		
		column = new TextColumn<CustomerBean>() {
			@Override
			public String getValue(CustomerBean object) {
				return String.valueOf(object.getOpenBalance());
			}
		};
		customerListTable.addColumn(column, "Open Balance");
		
		column = new Column<CustomerBean,String>(new ButtonCell()) {
			@Override
			public String getValue(CustomerBean object) {
				return "EDIT";
			}
		};
		customerListTable.addColumn(column, "Edit");
		column.setFieldUpdater(new FieldUpdater<CustomerBean, String>() {
			@Override
			public void update(int index, CustomerBean object, String value) {
				initEditCustomer(object.getCustomerId());
				customerListPopup.hide();
			}
		});
		
		customerList.addDataDisplay(customerListTable);
		
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		customerPager = new SimplePager(TextLocation.CENTER, pagerResources, true, 0,true);
		customerPager.setDisplay(customerListTable);
		customerPager.setPageSize(25);
	}
	
	@UiHandler("salesHistory")
	void salesHistory(ClickEvent e) {
		salesHistoryFromDate.setValue(new Date());
		salesHistoryToDate.setValue(new Date());
		historySaleTypeList.setSelectedIndex(0);
		salesHistoryPopup.center();
		salesHistorySubmit(null);
	}
	
	@UiHandler("salesHistorySubmit")
	void salesHistorySubmit(ClickEvent e) {
		salesDataList.getList().clear();
		service.getSalesHistory(salesHistoryFromDate.getValue().getTime(), salesHistoryToDate.getValue().getTime(), 
				historySaleTypeList.getValue(historySaleTypeList.getSelectedIndex()),
				new AsyncCallback<List<OrderBean>>() {
			@Override
			public void onFailure(Throwable caught) {
				displayErrorMesasge(caught.getMessage(), null);
			}
			@Override
			public void onSuccess(List<OrderBean> result) {
				salesDataList.getList().addAll(result);
				salesHistoryPopup.center();
			}
		});
	}
	
	@UiHandler("salesHistoryCloseBtn")
	void salesHistoryClose(ClickEvent e) {
		salesHistoryPopup.hide();
	}
	
	protected void initEditOrder(int orderId) {
		loadingPopup.center();
		service.loadOrderForEdit(orderId,new AsyncCallback<OrderBean>() {
			@Override
			public void onFailure(Throwable caught) {
				displayErrorMesasge("Unable to load order", defaultFocus);
				loadingPopup.hide();
			}
			@Override
			public void onSuccess(OrderBean order) {
				setOrderDetailsToScreen(order);
				for (CartItem item : order.getLines()) {
					addOrUpdateDataList(item);
				}
				loadingPopup.hide();
			}
		});
	}
	
	@UiHandler("productListCode")
	void productListCode(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			productListSubmit(null);
		}
	}
	
	@UiHandler("productListDescr")
	void productListName(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			productListSubmit(null);
		}
	}
	
	@UiHandler("listProductBtn")
	void listProductBtn(ClickEvent e) {
		productListPopup.center();
	}
	
	@UiHandler("productListSubmit")
	void productListSubmit(ClickEvent e) {
		loadingPopup.center();
		productList.getList().clear();
		productList.flush();
		service.searchProducts(productListCode.getText(), productListDescr.getText(), productListCategory.getValue(), 
				new AsyncCallback<List<ProductBean>>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), productListCode);
			}
			@Override
			public void onSuccess(List<ProductBean> result) {
				productList.getList().addAll(result);
				productList.flush();
				loadingPopup.hide();
				productListPopup.center();
			}
		});
	}
	
	@UiHandler("productListCloseBtn")
	void productListCloseBtn(ClickEvent e) {
		productListPopup.hide();
		barcode.setFocus(true);
	}
	
	@UiHandler("customerListCode")
	void customerListCode(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			customerListSubmit(null);
		}
	}
	
	@UiHandler("customerListName")
	void customerListName(KeyDownEvent e) {
		if (e != null && e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			customerListSubmit(null);
		}
	}
	
	@UiHandler("listCustomterBtn")
	void listCustomerBtn(ClickEvent e) {
		customerListPopup.center();
	}
	
	@UiHandler("customerListSubmit")
	void customerListSubmit(ClickEvent e) {
		loadingPopup.center();
		customerList.getList().clear();
		customerList.flush();
		service.searchCustomers(customerListCode.getText(), customerListName.getText(),  
				new AsyncCallback<List<CustomerBean>>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), customerListCode);
			}
			@Override
			public void onSuccess(List<CustomerBean> result) {
				customerList.getList().addAll(result);
				customerList.flush();
				loadingPopup.hide();
				customerListPopup.center();
			}
		});
	}
	
	@UiHandler("customerListCloseBtn")
	void customerListCloseBtn(ClickEvent e) {
		customerListPopup.hide();
		barcode.setFocus(true);
	}
	
	@UiHandler("settingsBtn")
	void settingsBtn(ClickEvent e){
		service.loadPrintSetUp(new AsyncCallback<PrintSetup>() {
			@Override
			public void onFailure(Throwable caught) {
				displayErrorMesasge(caught.getMessage(), null);
			}
			public void onSuccess(PrintSetup result) {
				settingsPopup.center();
				printWidth.setText(result.getPrintWidth());
				printDevice.setText(result.getPrintDevice());
				if (result.getPrintType().equalsIgnoreCase(Constants.PRINT_TYPE_VALUE_SLIP)) {
					printTypeList.setSelectedIndex(0);
				} else {
					printTypeList.setSelectedIndex(1);
				}
			};
		});
	}
	
	@UiHandler("settingsCancel")
	void settingsCancel(ClickEvent e) {
		settingsPopup.hide();
	}
	@UiHandler("settingsOKBtn")
	void settingsOKBtn(ClickEvent e) {
		PrintSetup setup = new PrintSetup();
		setup.setPrintDevice(printDevice.getText());
		setup.setPrintWidth(printWidth.getText());
		setup.setPrintType(printTypeList.getValue(printTypeList.getSelectedIndex()));
		loadingPopup.center();
		service.savePrintSetup(setup, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingPopup.hide();
				displayErrorMesasge(caught.getMessage(), printDevice);
			}
			@Override
			public void onSuccess(Void result) {
				loadingPopup.hide();
				displaySuccessMsg("settings saved successully", barcode);
				settingsPopup.hide();
			}
		});
	}
	
	@UiHandler("printOrderCancel")
	void printOrderCancel(ClickEvent e) {
		printOrderPopup.hide();
	}
}
