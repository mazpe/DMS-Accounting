/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import dms.DMSApp;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.EdgedBalloonStyle;

/**
 *
 * @author Administrator
 */
public class AccountingUtil {

	static ArrayList accountNumbers = new ArrayList();
	static ArrayList accountDescs = new ArrayList();
	static ArrayList accountTypes = new ArrayList();
	public static Map<Integer, Boolean> controlNumMap = new HashMap<Integer, Boolean>();
	static int currentAccount = 0;
	static public DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy 00:00:00");
	static public DateFormat dateFormat1 = new SimpleDateFormat("MM/dd/yyyy");
	static public List<Object> accountsToBeShownList = new ArrayList<Object>();
	public static final String DB_CONNECTION_PARAMS_FILE = "dbConnectionParams.txt";

	public static void dateFilter(JPanel panel, JCheckBox checkbox) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			panel.getComponents()[i].setEnabled(!checkbox.isSelected());
		}
	}

	public AccountingUtil() {
		loadChartOfAccounts();
	}

	public static void clearTableModel(DefaultTableModel aModel) {
		if (aModel.getRowCount() > 0) {
			for (int i = aModel.getRowCount(); i > 0; i--) {
				aModel.removeRow(i - 1);
			}
		}
	}

	public static void clearTable(JTable jTable) {
		DefaultTableModel aModel;
		aModel = (DefaultTableModel) jTable.getModel();

		if (aModel.getRowCount() > 0) {
			for (int i = aModel.getRowCount(); i > 0; i--) {
				aModel.removeRow(i - 1);
			}
		}

	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static int getColumnByName(JTable table, String name) {
		for (int i = 0; i < table.getColumnCount(); ++i) {
			if (table.getColumnName(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public static Integer getEmployeeID(String firstName, String lastName) {

		Integer employeeId = null;

		try {

			String sql = "SELECT TOP 1 UserID "
				+ "FROM [DMSData]..[UsersTable] "
				+ "WHERE FirstName = '" + firstName + "' AND LastName = '" + lastName + "'";

			//+ "LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
			//+ "AND ;

			ResultSet rs = null;

			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			while (rs.next()) {
				employeeId = rs.getInt("UserID");
			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return employeeId;

	}

	public String getVendorName(Integer vendorID) {

		String vendorName = null;

		try {

			String sql = "SELECT VendorName "
				+ "FROM VendorListTable "
				+ "WHERE VendorID = '" + vendorID + "'";
			//+ "LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
			//+ "AND ;

			ResultSet rs = null;

			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			while (rs.next()) {
				vendorName = rs.getString("VendorName");
			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return vendorName;

	}

//  public void getVendorID(String vendorName) throws SQLException {
	public static Integer getVendorID(String vendorName) {
		Integer vendorId = null;

		try {

			String sql = "SELECT VendorID "
				+ "FROM VendorListTable "
				+ "WHERE VendorName = '" + vendorName + "'";

			//+ "LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
			//+ "AND ;

			ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			while (rs.next()) {
				vendorId = rs.getInt("VendorID");
			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return vendorId;
	}

	public static Integer getCustomerID(String customerName) {

		int customerId = 0;
		String fName = "";
		String mName = "";
		String lName = "";
		String remainingNamePart;
		if (customerName.contains(" ")) {
			fName = customerName.split(" ")[0];
			remainingNamePart = customerName.substring(fName.length() + 1, customerName.length());
			if (remainingNamePart.contains(" ")) {
				mName = remainingNamePart.split(" ")[0];
				lName = remainingNamePart.split(" ")[1];
			} else {
				lName = remainingNamePart;
			}
		}

		try {

			String sql = "SELECT CustomerCode "
				+ "FROM CustomerTable "
				+ "WHERE FirstName = '" + fName + "' AND MiddleName =  '" + mName + "' AND LastName = '" + lName + "' ";

			ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			while (rs.next()) {
				customerId = rs.getInt("CustomerCode");
			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}

		return customerId;
	}

	public static Integer getEmployeeID(String employeeName) {

		int employeeId = 0;
		String fName = "";
		String lName = "";
		if (employeeName.contains(" ")) {
			fName = employeeName.split(" ")[0];
			lName = employeeName.split(" ")[1];
		}
		try {

			String sql = "SELECT UserID "
				+ "FROM [DMSData]..[UsersTable] "
				+ "WHERE FirstName = '" + fName + "' AND LastName = '" + lName + "' ";

			ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			while (rs.next()) {
				employeeId = rs.getInt("UserID");
			}
		} catch (SQLException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return employeeId;

	}

	// Method to check if VendorID and InvoiceNumber Combination alreay exists in DB
	public static boolean IsInvoiceExistsForVendor(String vendorID, String invoiceNumber) {
		boolean invoiceExists = false;
		String vendorId = vendorID;
		String invoiceNo = invoiceNumber;
		try {

			String sql = "SELECT * From AccountingGLTable "
				+ "WHERE ControlNumber = '" + vendorId + "' AND ReferenceNumber = '" + invoiceNo + "' ";

			ResultSet rs = null;

			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			if (rs.next()) {
				vendorId = rs.getString("ControlNumber");
				invoiceExists = true;
			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return invoiceExists;
	}

	// Method to format amount fields
	public static String formatAmountForDisplay(String amount) {
		double amountForDisplay = 0.00;
		DecimalFormat formatter = new DecimalFormat("#,###.00");

		if (amount.contains(",")) {
			amountForDisplay = Double.parseDouble(amount.replaceAll(",", ""));
			//System.out.println("amtFDisp = " + amountForDisplay);
		} else {
			amountForDisplay = Double.parseDouble(amount);
		}
		//formatter.format(amountForDisplay);
		//System.out.println("FormatedAmt = " + formatter.format(amountForDisplay));
		return String.valueOf(formatter.format(amountForDisplay));
	}

	// Method to check Number Field
	public static boolean isNumeric(String s) {
		//System.out.println("isNumeric..");
		String pattern = "[0-9.]+";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}
	// Method to Display Numeric

	public static boolean displayNumeric(String s) {
		//System.out.println("displayNumeric..");
		String pattern = "[0-9.,-]+";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}

	public static Map<Integer, Boolean> getControlNumMap() {
		return controlNumMap;
	}

	public static void setControlNumMap(Map<Integer, Boolean> controlNumMap) {
		AccountingUtil.controlNumMap = controlNumMap;
	}

	public static void loadChartOfAccounts() {
		accountNumbers.clear();
		accountDescs.clear();
		accountTypes.clear();
		controlNumMap.clear();
		boolean isControlled;
		try {
			String sql = "SELECT * FROM AccountingCOATable WHERE LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' ORDER BY Series";
			ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);
			//System.out.println(">>>>>>>> Query : " + sql);
			while (rs.next()) {
				int accountNumber = rs.getInt("AccountNumber");
				accountNumbers.add(accountNumber);
				String desc = rs.getString("Description");
				int isControlledIntVal = rs.getInt("isControlled");
				if (isControlledIntVal == 0) {
					isControlled = false;
				} else {
					isControlled = true;
				}
				controlNumMap.put(accountNumber, isControlled);
				accountDescs.add(desc);
				accountTypes.add(rs.getString("Type"));
			}
			rs.getStatement().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setControlNumMap(controlNumMap);
	}

	public static String getAccountFull(Integer GLAccount) {

		String GLAccountFull = null;

		try {
			String sql = "SELECT AccountNumber, Description, Type "
				+ "FROM AccountingCOATable "
				+ "WHERE LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
				+ "AND AccountNumber = '" + GLAccount + "' "
				+ "ORDER BY Series";

			ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			while (rs.next()) {
				//accountNumbers.add(rs.getInt("AccountNumber"));
				//accountDescs.add(rs.getString("Description"));
				//accountTypes.add(rs.getString("Type"));

				//GLAccountFull = rs.getInt("AccountNumber").toString + ' - ' + rs.getString("Description");

				GLAccountFull = Integer.toString(rs.getInt("AccountNumber")) + " - "
					+ rs.getString("Description") + " - "
					+ rs.getString("Type");

			}
			rs.getStatement().close();
		} catch (Exception e) {
		}


		return GLAccountFull;

	}

	public static String[] getAllAccountsFull() {
		if (accountNumbers.size() > 0) {
			String[] accounts = new String[accountNumbers.size()];
			for (int i = 0; i < accounts.length; i++) {
				accounts[i] = accountNumbers.get(i) + " - " + accountDescs.get(i) + " - " + accountTypes.get(i);
			}
			return accounts;
		} else {
			return null;
		}
	}

	public static String[] getFullAccountsForInvoice(int accountNo) {
		String accountNumber = String.valueOf(accountNo);
		if (accountNumbers.size() > 0) {
			String[] accounts = new String[accountNumbers.size()];
			for (int i = 0; i < accounts.length; i++) {
				//System.out.println("accountNumber = " + accountNumber);
				//System.out.println("accountNumbers.get(i)) = " + accountNumbers.get(i));
				if (accountNumber.equals(accountNumbers.get(i).toString())) {
					//System.out.println("Match Found!");
					accounts[i] = accountNumbers.get(i) + " - " + accountDescs.get(i) + " - " + accountTypes.get(i);
					//System.out.println("matched accounts = " + accounts[i]);
				}
			}
			return accounts;
		} else {
			return null;
		}
	}

	public String[] getAllAccountsNumberAndDesc() {
		if (accountNumbers.size() > 0) {
			String[] accounts = new String[accountNumbers.size()];
			for (int i = 0; i < accounts.length; i++) {
				accounts[i] = accountNumbers.get(i) + " - " + accountDescs.get(i);
			}
			return accounts;
		} else {
			return null;
		}
	}

	public static String[] getBankAccountsNumberAndDesc() {
		if (accountNumbers.size() > 0) {
			ArrayList acctString = new ArrayList();
			for (int i = 0; i < accountNumbers.size(); i++) {

				if (accountTypes.get(i).equals("Bank")) {
					acctString.add(accountNumbers.get(i) + " - " + accountDescs.get(i));
				}
			}

			if (acctString.size() > 0) {
				String[] completed = new String[acctString.size()];
				for (int i = 0; i < completed.length; i++) {
					completed[i] = (String) acctString.get(i);
				}

				return completed;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String[] getAccountsWithTypeNumberAndDesc(String type) {
		if (accountNumbers.size() > 0) {
			ArrayList acctString = new ArrayList();
			for (int i = 0; i < accountNumbers.size(); i++) {
				if (accountTypes.get(i).equals(type)) {
					acctString.add(accountNumbers.get(i) + " - " + accountDescs.get(i));
				}
			}

			if (acctString.size() > 0) {
				String[] completed = new String[acctString.size()];
				for (int i = 0; i < completed.length; i++) {
					completed[i] = (String) acctString.get(i);
				}

				return completed;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public int getAccountsCount() {
		return accountNumbers.size();
	}

	public static void addAccount(int actNumber, String actDesc, String actType, Component c) {
		boolean good = true;
		for (int i = 0; i < accountNumbers.size(); i++) {
			if (((Integer) accountNumbers.get(i)).intValue() == actNumber) {
				good = false;
				break;
			}
		}

		if (good) {
			String[] sql = new String[1];
			sql[0] = "Insert Into AccountingCOATable "
				+ "(AccountNumber, Description, Type, LotName) "
				+ "Select '" + actNumber + "','" + actDesc + "','" + actType + "','" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, c);
			loadChartOfAccounts();
		}
	}

	public static void editAccount(int oldActNumber, int newActNumber, String actDesc, String actType, boolean isControlled, String series, Component c) {
		boolean good = true;
		for (int i = 0; i < accountNumbers.size(); i++) {
			if (((Integer) accountNumbers.get(i)).intValue() == newActNumber && oldActNumber != newActNumber) {
				good = false;
				break;
			}
		}

		if (good) {
			String[] sql = new String[2];
			sql[0] = "Update AccountingGLTable "
				+ "Set AccountNumber = " + newActNumber + " "
				+ "Where AccountNumber = " + oldActNumber + " "
				+ "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

			sql[1] = "Update AccountingCOATable "
				+ "Set AccountNumber = " + newActNumber + ","
				+ "Description = '" + actDesc + "',"
				+ "isControlled = '" + isControlled + "',"
				+ "series = '" + series + "',"
				+ "Type = '" + actType.replace("'", "''") + "' "
				+ "Where AccountNumber = " + oldActNumber + " "
				+ "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, c);
			loadChartOfAccounts();
		}
	}

	public static void deleteAccount(int actNumber, Component c) {
		String query = "";
		boolean hasRecordsInGLTable = false;
		query = "Select * From AccountingGLTable "
			+ "Where AccountNumber = " + actNumber + " "
			+ "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

		try {
			ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
			if (rs.next()) {
				hasRecordsInGLTable = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!hasRecordsInGLTable) {               // If AccountNo has no records in GLTable:Delete from COATable
			String[] sql = new String[1];
			boolean sureToDelete = false;
			int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION) {
				sureToDelete = false;
				//System.out.println("No button clicked");
			} else if (response == JOptionPane.YES_OPTION) {
				sureToDelete = true;
			} else if (response == JOptionPane.CLOSED_OPTION) {
				//System.out.println("JOptionPane closed");
			}

			if (sureToDelete) {
				sql[0] = "Delete From AccountingCOATable "
					+ "Where AccountNumber = " + actNumber + " "
					+ "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

				dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, c);
				loadChartOfAccounts();
			}
		} else {
			dms.DMSApp.displayMessage(null, "Record cannot be deleted", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public void addDeal(int accountnumber, Component mainComponent) {
		try {
			String[] sql = new String[27];

			sql[0] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 400, 'Deals', 'Sales Vehicles', 0.00, Case When (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "' AND DealType <> 'Wholesale') IS NULL Then 0.00 ELSE (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "' AND DealType <> 'Wholesale') END,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[1] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 602, 'Deals', 'Discount-Vehicle Sales', (Select Discount From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[2] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 339, 'Deals', 'Doc Fees Payable', 0.00, Case When (Select FinanceCompany From DealsTable Where AccountNumber = '" + accountnumber + "') = (Select CompanyName From DMSData..CompanyTable Where CompanyCode = '" + dms.DMSApp.getApplication().getCompanyCode() + "') Then (Select DocStamp From DealsTable Where AccountNumber = '" + accountnumber + "') else 0.00 end ,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[3] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 600, 'Deals', 'COS-Vehicle', (Select case when PurchasedPrice is NULL then 0.00 else PurchasedPrice end From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[4] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 603, 'Deals', 'COS-Repairs&Transport', (Select case when Repairs is NULL then 0.00 else Repairs end From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[5] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 443, 'Deals', 'Finance Income', 0.00, (Select ReserveAmount From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[6] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 445, 'Deals', 'GAP Income', 0.00, (Select GapAmount From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[7] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 648, 'Deals', 'COS-Gap', (Select GapCost From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[8] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 448, 'Deals', 'Product Sales', 0.00, (Select Product1Amount+Product2Amount+Product3Amount+Product4Amount From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[9] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 649, 'Deals', 'COS-Product', (Select Product1Cost+Product2Cost+Product3Cost+Product4Cost From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[10] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 447, 'Deals', 'Warranty', 0.00, (Select WarrantyPrice From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[11] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 647, 'Deals', 'COS Warranty', (Select WarrantyCost From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[12] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 251, 'Deals', 'Vehicle Inventory', (Select ACV1+ACV2 From DealsTable Where AccountNumber = '" + accountnumber + "'),0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[13] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 321, 'Deals', 'Tags & Titles', 0.00, (Select Tags+Title From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[14] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 322, 'Deals', 'Customer Deposits', (Select DownPayment From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[15] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 333, 'Deals', 'Sales Tax Payable', 0.00, (Select SalesTax From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), 'FLORIDA DEPARTMENT OF REVENUE' ";

			sql[16] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 334, 'Deals', 'Commissions Payable', 0.00, (Select Commissions1+Commissions2+FinanceCommissions From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[17] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 605, 'Deals', 'Sales Commissions', (Select Commissions1+Commissions2+FinanceCommissions From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[18] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 320, 'Deals', 'Vehicle Lien Payoffs', 0.00, (Select TradeInPayoff From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[19] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 401, 'Deals', 'Dealer Fees', 0.00, (Select DealerFee From DealsTable Where AccountNumber = '" + accountnumber + "'),'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[20] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 251, 'Deals', 'Vehicle Inventory', 0.00, Case When (Select Cost From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')) is NULL then 0.00 else (Select Cost From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')) end,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[21] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 211, 'Deals', 'Contracts in Transit', Case When (Select BalanceDue-Discount From DealsTable Where AccountNumber = '" + accountnumber + "') < 0.00 then 0.00 Else (Select abs(BalanceDue-Discount) From DealsTable Where AccountNumber = '" + accountnumber + "') end, case when (Select BalanceDue-Discount From DealsTable Where AccountNumber = '" + accountnumber + "') < 0.00 then (Select abs(BalanceDue-Discount) From DealsTable Where AccountNumber = '" + accountnumber + "') else 0.00 end,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[22] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 332, 'Deals', 'Insurance Payable', 0.00, (Select WarrantyCost+GapCost+Product1Cost+Product2Cost+Product3Cost+Product4Cost From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[23] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 608, 'Deals', 'Trade-In Over Allowance', (Select abs((ACV1+ACV2)-(TradeAllowance1+TradeAllowance2)) From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,  '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[24] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 402, 'Deals', 'Vehicle Wholesale', 0.00, Case When (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "' AND DealType = 'Wholesale') IS NULL Then 0.00 ELSE (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "' AND DealType = 'Wholesale') END,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[25] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 271, 'Deals', 'Finance Income Receivable', (Select ReserveAmount From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[26] = "Delete From QuickBooksExportTable "
				+ "Where Debit = 0.00 AND Credit = 0.00 ";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, mainComponent);
		} catch (Exception e) {
			dms.DMSApp.displayMessage(mainComponent, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
		}
	}

	public void editDeal(int accountnumber, Component mainComponent) {
		try {
			String[] sql = new String[1];

			sql[0] = "Delete From QuickBooksExportTable "
				+ "Where Number = '" + accountnumber + "' "
				+ "AND TransactionType = 'Deals' ";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, mainComponent);

			addDeal(accountnumber, mainComponent);
		} catch (Exception e) {
			dms.DMSApp.displayMessage(mainComponent, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
		}
	}

	public void deleteDeal(int accountnumber, Component mainComponent) {
		try {
			String[] sql = new String[27];

			sql[0] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 400, 'Deals', 'Sales Vehicles', (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[1] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 602, 'Deals', 'Discount-Vehicle Sales', 0.00, (Select Discount From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[2] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 339, 'Deals', 'Doc Fees Payable', Case When (Select FinanceCompany From DealsTable Where AccountNumber = '" + accountnumber + "') = (Select CompanyName From DMSData..CompanyTable Where CompanyCode = '" + dms.DMSApp.getApplication().getCompanyCode() + "') Then (Select DocStamp From DealsTable Where AccountNumber = '" + accountnumber + "') else 0.00 end , 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[3] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 600, 'Deals', 'COS-Vehicle', 0.00, (Select case when PurchasedPrice is NULL then 0.00 else PurchasedPrice end From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[4] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 603, 'Deals', 'COS-Repairs&Transport', 0.00, (Select case when Repairs is NULL then 0.00 else Repairs end From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[5] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 443, 'Deals', 'Finance Income', (Select ReserveAmount*(ReservePercent/100.00) From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[6] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 445, 'Deals', 'GAP Income', (Select GapAmount From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00, '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[7] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 648, 'Deals', 'COS-Gap', 0.00,(Select GapCost From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[8] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 448, 'Deals', 'Product Sales', (Select Product1Amount+Product2Amount+Product3Amount+Product4Amount From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[9] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 649, 'Deals', 'COS-Product', 0.00,(Select Product1Cost+Product2Cost+Product3Cost+Product4Cost From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[10] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 447, 'Deals', 'Warranty', (Select WarrantyPrice From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[11] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 647, 'Deals', 'COS Warranty', 0.00,(Select WarrantyCost From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[12] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 251, 'Deals', 'Vehicle Inventory', 0.00,(Select ACV1+ACV2 From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[13] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 321, 'Deals', 'Tags & Titles', (Select Tags+Title From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[14] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 322, 'Deals', 'Customer Deposits', 0.00,(Select DownPayment From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[15] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 333, 'Deals', 'Sales Tax Payable', (Select SalesTax From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), 'FLORIDA DEPARTMENT OF REVENUE' ";

			sql[16] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 334, 'Deals', 'Commissions Payable', (Select Commissions1+Commissions2+FinanceCommissions From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[17] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 605, 'Deals', 'Sales Commissions', 0.00, (Select Commissions1+Commissions2+FinanceCommissions From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[18] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 320, 'Deals', 'Vehicle Lien Payoffs', (Select TradeInPayoff From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[19] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 401, 'Deals', 'Dealer Fees', (Select DealerFee From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[20] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 251, 'Deals', 'Vehicle Inventory', Case When (Select Cost From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')) is NULL then 0.00 else (Select Cost From InventoryTable Where StockNumber = (Select StockNumber From DealsTable Where AccountNumber = '" + accountnumber + "')) end, 0.00, '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[21] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 211, 'Deals', 'Contracts in Transit', case when (Select BalanceDue-Discount From DealsTable Where AccountNumber = '" + accountnumber + "') < 0.00 then (Select abs(BalanceDue-Discount) From DealsTable Where AccountNumber = '" + accountnumber + "') else 0.00 end, Case When (Select BalanceDue-Discount From DealsTable Where AccountNumber = '" + accountnumber + "') < 0.00 then 0.00 Else (Select abs(BalanceDue-Discount) From DealsTable Where AccountNumber = '" + accountnumber + "') end, '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[22] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 332, 'Deals', 'Insurance Payable', (Select WarrantyCost+GapCost+Product1Cost+Product2Cost+Product3Cost+Product4Cost From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00, '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[23] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 608, 'Deals', 'Trade-In Over Allowance', (Select (ACV1+ACV2)-(TradeAllowance1+TradeAllowance2) From DealsTable Where AccountNumber = '" + accountnumber + "'), 0.00, '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[24] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 402, 'Deals', 'Vehicle Wholesale', Case When (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "' AND DealType = 'Wholesale') IS NULL Then 0.00 ELSE (Select SalesPrice From DealsTable Where AccountNumber = '" + accountnumber + "' AND DealType = 'Wholesale') END,0.00,'" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[25] = "Insert Into QuickBooksExportTable "
				+ "(Number, QuickBooksAccount, TransactionType, Description, Debit, Credit, LotName, TransactionDate, VendorName) "
				+ "Select '" + accountnumber + "', 271, 'Deals', 'Finance Income Receivable', 0.00, (Select ReserveAmount From DealsTable Where AccountNumber = '" + accountnumber + "'), '" + dms.DMSApp.getApplication().getCurrentlotName() + "', (Select SoldDate From DealsTable Where AccountNumber = '" + accountnumber + "'), '' ";

			sql[26] = "Delete From QuickBooksExportTable "
				+ "Where Debit = 0.00 AND Credit = 0.00 ";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, mainComponent);
		} catch (Exception e) {
			dms.DMSApp.displayMessage(mainComponent, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
		}
	}

	public static java.util.Date getUtilDateFormatFromSqlDate(java.sql.Date dateToFormat) {
		java.util.Date billDate = new java.util.Date();
		if (dateToFormat != null) {
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
			String bDate = String.valueOf(dateToFormat);
			String datePart = bDate.substring(0, 10);
			String mm = datePart.substring(5, 7);
			String dd = datePart.substring(8, 10);
			String yyyy = datePart.substring(0, 4);
			String finalBillDate = mm + "/" + dd + "/" + yyyy;
			try {
				billDate = format.parse(finalBillDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return billDate;
	}

	public static String getStringDateFromUtilDate(java.util.Date utilDate) {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		String outputDate = df.format(utilDate);
		return outputDate;
	}

	public static void addComboBoxesToTable(JTable table) {
		if (AccountingUtil.getAllAccountsFull() != null) {
			Object[] accounts = AccountingUtil.getAllAccountsFull();
			TableColumn column = table.getColumnModel().getColumn(0);
			column.setCellRenderer(new MyComboBoxRenderer(accounts));
			column.setCellEditor(new MyComboBoxEditor(accounts));
		}

	}

	public static void addComboBoxesToTableForEditBill(JTable table, Object[] accNum) {
		if (AccountingUtil.getAllAccountsFull() != null) {
			int j = 0;
			for (int i = 0; i < accNum.length; i++) {
				if (accNum[i] != null) {
					accountsToBeShownList.add(accNum[i]);
					j++;
				}
			}
			Object[] accounts = AccountingUtil.getAllAccountsFull();
			TableColumn column = table.getColumnModel().getColumn(0);
			column.setCellRenderer(new MyComboBoxRenderer(accounts));
			column.setCellEditor(new MyComboBoxEditor(accounts));

		}

	}

	public static class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {

		public MyComboBoxRenderer(Object[] items) {
			super(items);
		}

		public MyComboBoxRenderer(Object[] items, Object[] accNum) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
			if (column == 0) {
				this.setBackground(new java.awt.Color(255, 255, 255));
			}

			setSelectedItem(value);
			return this;
		}
	}

	public static class MyComboBoxEditor extends DefaultCellEditor {

		public MyComboBoxEditor(Object[] items) {
			super(new JComboBox(items));
		}
	}

	public static void showMessageBalloon(String message, JComponent component) {
		EdgedBalloonStyle style = new EdgedBalloonStyle(Color.ORANGE, Color.BLUE);
		final BalloonTip balloonTip = new BalloonTip(
			component,
			new JLabel(message),
			style,
			BalloonTip.Orientation.LEFT_ABOVE,
			BalloonTip.AttachLocation.ALIGNED,
			30, 10,
			true);

		balloonTip.setCloseButton(BalloonTip.getDefaultCloseButton(), false);
		balloonTip.setVisible(true);
	}

	private static void showAlert(String message, JComponent component) {
		JLabel errorLabel = new JLabel(message);
		Window topLevelWin = SwingUtilities.getWindowAncestor(component);
		JWindow alertWindow = new JWindow(topLevelWin);
		JPanel contentPane = (JPanel) alertWindow.getContentPane();
		contentPane.add(errorLabel);
		contentPane.setBackground(Color.ORANGE);
		contentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		alertWindow.pack();

		Point loc = component.getLocationOnScreen();
		alertWindow.setLocation(loc.x, loc.y - 5);
		alertWindow.setVisible(true);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		alertWindow.dispose();
	}

	public static boolean isControlNumberRequired(JTable table) {
		int accountColumn = AccountingUtil.getColumnByName(table, "Account");
		int controlColumn = AccountingUtil.getColumnByName(table, "Control #");
		for (int i = 0; i <= table.getRowCount() - 1; i++) {
			String accountNumber = null;
			Boolean controlled;
			if (table.getValueAt(i, accountColumn) != null) {
				accountNumber = table.getValueAt(i, accountColumn).toString();
				accountNumber = accountNumber.split("-")[0];
			}
			controlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
			Object val = table.getValueAt(i, controlColumn);
			if (val == null) {
				val = "";
			}
			if (controlled && val.toString().isEmpty()) {
				dms.DMSApp.displayMessage(table, "Please provide Control # value for Row# " + ++i, JOptionPane.WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}

	public static void saveDBConnectionProperties(String connectionParams) {
		try {
			String content = connectionParams;
			File file = new File(DB_CONNECTION_PARAMS_FILE);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			System.out.println("New connection params saved");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getDBConnectionParamsFromFile() {
		BufferedReader reader = null;
		String dbConnectionParams = "";
		try {
			reader = new BufferedReader(new FileReader(DB_CONNECTION_PARAMS_FILE));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					dbConnectionParams += line;
				}
			} catch (IOException ex) {
				Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return dbConnectionParams;
	}

	public static void restartAcocuntingApp() {
		try {

			URL url = DMSApp.class.getProtectionDomain().getCodeSource().getLocation();
			File appRootLocation = new File(url.toURI()).getParentFile().getParentFile();
			System.out.println(appRootLocation.getParentFile().getParentFile());

			File distFolderPath = appRootLocation.listFiles(new DistFolderFilter())[0];

			File jarFilePath = distFolderPath.listFiles(new JarFileFilter())[0];
			//System.out.println("Jar file ::: " + jarFilePath);

			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = jarFilePath;
			//System.out.println("Current jar : " + currentJar.getName());
			if (!currentJar.getName().endsWith(".jar")) {
				return;
			}

			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);
			try {
				System.out.println("Restarting app ..." + command.toString());
				builder.start();
			} catch (IOException ex) {
				Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
			}
			System.exit(0);
		} catch (Exception ex) {
			Logger.getLogger(AccountingUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static class JarFileFilter implements FileFilter {

		private final String[] okFileExtensions =
			new String[]{"jar"};

		public boolean accept(File file) {
			for (String extension : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}

	public static class DistFolderFilter implements FileFilter {

		private final String[] okFolderNames =
			new String[]{"dist"};

		public boolean accept(File file) {
			for (String extension : okFolderNames) {
				if (file.getName().toLowerCase().contains(extension)) {
					return true;
				}
			}
			return false;
		}
	}
}
