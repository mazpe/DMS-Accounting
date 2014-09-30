/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.panels;

import dms.util.AccountingUtil;
import dms.util.TableCellListener;
import dms.windows.AccountingWindow;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Lester
 */
public class Journals extends javax.swing.JPanel {

	private String[] sql = null;
	private dms.windows.AccountingWindow accountingWindow;
        private static Journals instance = null;
	String account = null;
	String debitAmount = null;
	String creditAmount = null;
	int rowNo = 0;
	boolean isRowRemoved = false;

        public static Journals getInstance(){
            if(instance == null){
                instance = new Journals();
            }
            
            return instance;
        }
        
	/**
	 * Creates new form Journals
	 */
	protected Journals() {
		initComponents();

		// set dates

		Calendar c = Calendar.getInstance();

		if (roEndDate.getDate() == null) {
			roEndDate.setDate(c.getTime());
		}
		if (roStartDate.getDate() == null) {
			c.add(Calendar.DATE, 1 - c.get(Calendar.DATE));
			roStartDate.setDate(c.getTime());
		}

		//accountingWindow = new dms.windows.AccountingWindow();	
		AccountingUtil.addComboBoxesToTable(journalPostingTable);

		Action action1 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				double sum = 0.00;
				int modifiedRowNo = 0;
				boolean debitColumnModified = false;
				String amountForDisplay = null;
				TableCellListener tcl = (TableCellListener) e.getSource();
				if (tcl.getColumn() == 1) {
					debitColumnModified = true;
				} else if (tcl.getColumn() == 2) {
				}
				modifiedRowNo = tcl.getRow();
				int rowCount = journalPostingTable.getRowCount();

				for (int i = 0; i < rowCount; i++) {
					//if(debitColumnModified){   // come inside the condition only if Debit Column is modified
					if (journalPostingTable.getValueAt(i, 1) != null
						&& !journalPostingTable.getValueAt(i, 1).toString().isEmpty()) {
						if (journalPostingTable.getValueAt(i, 2) == null) {
							//journalPostingTable.setValueAt("0.00", i, journalPostingTable.getColumnModel().getColumnIndex("Credits"));
						}
						String debitAmount = journalPostingTable.getValueAt(i, 1).toString();
						String debit = removeSpecialCharacters(debitAmount);
						//System.out.println("debit := " + debit);
						if (!displayNumeric(debit)) {
							//System.out.println("cannot enter aplphabets/special characters in amount field");
							dms.DMSApp.displayMessage(journalPostingTable, "Please enter numbers only for debit amount.", JOptionPane.ERROR_MESSAGE);
							journalPostingTable.setCellSelectionEnabled(true);
							journalPostingTable.changeSelection(modifiedRowNo, journalPostingTable.getColumnModel().getColumnIndex("Debits"), false, false);
							journalPostingTable.requestFocus();
						}
						if (displayNumeric(debit)) {
							if (debit.contains(",")) {
								sum += Double.parseDouble(debit.replaceAll(",", ""));
								//System.out.println("sum 1:= " + sum);
							} else {
								sum += Double.parseDouble(debit);
								//System.out.println("sum 2:= " + sum);
							}
							if (modifiedRowNo == i) {   // change amount format only for current row                                    
								amountForDisplay = AccountingUtil.formatAmountForDisplay(debit);
								//System.out.println("amountForDisplay := " + amountForDisplay);                              
								journalPostingTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
							}
						}
					} else {
						//journalPostingTable.setValueAt("0.00", i, journalPostingTable.getColumnModel().getColumnIndex("Debits"));
					}
					//}                                    
				}
				journalDebits2.setValue(sum);
				double journalCredit = 0.00;

				if (!journalCredits2.getValue().toString().isEmpty()) {
					journalCredit = Double.parseDouble(journalCredits2.getValue().toString());
					journalDifferences2.setValue(journalCredit - sum);
				}
			}
		};
		TableCellListener tcl1 = new TableCellListener(journalPostingTable, action1);

		Action action2 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				double sum = 0.00;
				int modifiedRowNo = 0;
				boolean creditColumnModified = false;
				String amountForDisplay = null;
				TableCellListener tcl = (TableCellListener) e.getSource();
				if (tcl.getColumn() == 2) {
					creditColumnModified = true;
				}
				modifiedRowNo = tcl.getRow();
				int rowCount = journalPostingTable.getRowCount();

				for (int i = 0; i < rowCount; i++) {
					//if(creditColumnModified){   // come inside the condition only if Credit Column is modified
					if (journalPostingTable.getValueAt(i, 2) != null
						&& !journalPostingTable.getValueAt(i, 2).toString().isEmpty()) {
						if (journalPostingTable.getValueAt(i, 1) == null) {
							journalPostingTable.setValueAt("0.00", i, journalPostingTable.getColumnModel().getColumnIndex("Debits"));
						}
						String creditAmount = journalPostingTable.getValueAt(i, 2).toString();
						String credit = removeSpecialCharacters(creditAmount);
						//System.out.println("credit := " + credit);
						if (!displayNumeric(credit)) {
							//System.out.println("cannot enter aplphabets/special characters in amount field");
							dms.DMSApp.displayMessage(journalPostingTable, "Please enter numbers only for credit amount.", JOptionPane.ERROR_MESSAGE);
							journalPostingTable.setCellSelectionEnabled(true);
							journalPostingTable.changeSelection(modifiedRowNo, journalPostingTable.getColumnModel().getColumnIndex("Credits"), false, false);
							journalPostingTable.requestFocus();
						}
						if (displayNumeric(credit)) {
							if (credit.contains(",")) {
								sum += Double.parseDouble(credit.replaceAll(",", ""));
								//System.out.println("sum 1:= " + sum);
							} else {
								sum += Double.parseDouble(credit);
								//System.out.println("sum 2:= " + sum);
							}
							if (modifiedRowNo == i) {   // change amount format only for current row                                    
								amountForDisplay = AccountingUtil.formatAmountForDisplay(credit);
								//System.out.println("amountForDisplay := " + amountForDisplay);                              
								journalPostingTable.setValueAt(amountForDisplay, modifiedRowNo, 2);
							}
						}
					}
					//}                                    
				}
				journalCredits2.setValue(sum);
				double journalDebit = 0.00;
				if (!journalCredits2.getValue().toString().isEmpty()) {
					journalDebit = Double.parseDouble(journalDebits2.getValue().toString());
					journalDifferences2.setValue(journalDebit - sum);
				}
			}
		};
		TableCellListener tcl2 = new TableCellListener(journalPostingTable, action2);
                reloadJournalDeals();
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
		String pattern = "[0-9.,]+";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}

	public static String removeSpecialCharacters(String s) {
		//System.out.println("s = " + s);            
		String finalString = s.replaceAll("[^\\dA-Za-z,.]", "");
		//System.out.println("finalString = " + finalString);            
		return finalString;
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        reversalPopup = new javax.swing.JDialog();
        jPanel168 = new javax.swing.JPanel();
        jPanel171 = new javax.swing.JPanel();
        jPanel98 = new javax.swing.JPanel();
        jScrollPane23 = new javax.swing.JScrollPane();
        journalReversalTable = new javax.swing.JTable();
        jPanel197 = new javax.swing.JPanel();
        jPanel120 = new javax.swing.JPanel();
        journalPostDatePanel1 = new javax.swing.JPanel();
        jPanel198 = new javax.swing.JPanel();
        jPanel199 = new javax.swing.JPanel();
        jLabel121 = new javax.swing.JLabel();
        jLabel123 = new javax.swing.JLabel();
        jPanel200 = new javax.swing.JPanel();
        journalDealDate1 = new javax.swing.JLabel();
        journalReversalDate = new com.toedter.calendar.JDateChooser();
        jPanel121 = new javax.swing.JPanel();
        journalsPostPanel1 = new javax.swing.JPanel();
        journalPostReversal = new javax.swing.JButton();
        journalReversalDatePanel1 = new javax.swing.JPanel();
        jPanel201 = new javax.swing.JPanel();
        jPanel202 = new javax.swing.JPanel();
        jLabel124 = new javax.swing.JLabel();
        jLabel125 = new javax.swing.JLabel();
        jPanel203 = new javax.swing.JPanel();
        journalDealPostedDate1 = new javax.swing.JLabel();
        journalReversalDate1 = new com.toedter.calendar.JDateChooser();
        jPanel124 = new javax.swing.JPanel();
        journalsReversalPanel1 = new javax.swing.JPanel();
        jButton41 = new javax.swing.JButton();
        jPanel204 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        jLabel126 = new javax.swing.JLabel();
        jLabel127 = new javax.swing.JLabel();
        jLabel128 = new javax.swing.JLabel();
        jLabel129 = new javax.swing.JLabel();
        jLabel130 = new javax.swing.JLabel();
        journalSalesCustomer1 = new javax.swing.JLabel();
        journalSalesDeal1 = new javax.swing.JLabel();
        journalSalesStock1 = new javax.swing.JLabel();
        journalSalesMake1 = new javax.swing.JLabel();
        journalSalesYear1 = new javax.swing.JLabel();
        journalSalesModel1 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        journalSalesCustomerName1 = new javax.swing.JLabel();
        jPanel205 = new javax.swing.JPanel();
        jLabel59 = new javax.swing.JLabel();
        jLabel131 = new javax.swing.JLabel();
        jLabel132 = new javax.swing.JLabel();
        debits11 = new javax.swing.JFormattedTextField();
        credits11 = new javax.swing.JFormattedTextField();
        difference11 = new javax.swing.JFormattedTextField();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jideTabbedPane3 = new com.jidesoft.swing.JideTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        enterBillsPanel3 = new javax.swing.JPanel();
        jPanel218 = new javax.swing.JPanel();
        jPanel219 = new javax.swing.JPanel();
        jLabel139 = new javax.swing.JLabel();
        jLabel141 = new javax.swing.JLabel();
        jLabel142 = new javax.swing.JLabel();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel220 = new javax.swing.JPanel();
        billInvoice = new javax.swing.JTextField();
        billMemo = new javax.swing.JTextField();
        journalClassCombo = new javax.swing.JComboBox();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel221 = new javax.swing.JPanel();
        journallClass = new javax.swing.JPanel();
        billPostDate = new com.toedter.calendar.JDateChooser();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        filler17 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel223 = new javax.swing.JPanel();
        jLabel136 = new javax.swing.JLabel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel224 = new javax.swing.JPanel();
        jButton46 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jPanel195 = new javax.swing.JPanel();
        jPanel163 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        journalDebits2 = new javax.swing.JFormattedTextField();
        journalCredits2 = new javax.swing.JFormattedTextField();
        journalDifferences2 = new javax.swing.JFormattedTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        journalPostingTable = new javax.swing.JTable();
        journalDeals = new javax.swing.JPanel();
        jPanel111 = new javax.swing.JPanel();
        jLabel74 = new javax.swing.JLabel();
        journalRetailRadio = new javax.swing.JRadioButton();
        journalWholesaleRadio = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();
        journalPostedRadio = new javax.swing.JRadioButton();
        jPanel112 = new javax.swing.JPanel();
        jPanel113 = new javax.swing.JPanel();
        jScrollPane25 = new javax.swing.JScrollPane();
        journalDealsTable = new javax.swing.JTable();
        jPanel141 = new javax.swing.JPanel();
        jPanel149 = new javax.swing.JPanel();
        journalSearchTextField = new javax.swing.JTextField();
        journalSearchButton = new javax.swing.JButton();
        jPanel146 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel142 = new javax.swing.JPanel();
        jPanel143 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        journalDealsDetailsTable = new javax.swing.JTable();
        jPanel144 = new javax.swing.JPanel();
        jPanel92 = new javax.swing.JPanel();
        journalPostDatePanel = new javax.swing.JPanel();
        jPanel147 = new javax.swing.JPanel();
        jPanel148 = new javax.swing.JPanel();
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        jPanel150 = new javax.swing.JPanel();
        journalDealDate = new javax.swing.JLabel();
        journalPostDate = new com.toedter.calendar.JDateChooser();
        jPanel64 = new javax.swing.JPanel();
        journalsPostPanel = new javax.swing.JPanel();
        journalPostButton = new javax.swing.JButton();
        journalReversalDatePanel = new javax.swing.JPanel();
        jPanel151 = new javax.swing.JPanel();
        jPanel152 = new javax.swing.JPanel();
        jLabel102 = new javax.swing.JLabel();
        jPanel167 = new javax.swing.JPanel();
        journalDealPostedDate = new javax.swing.JLabel();
        jPanel108 = new javax.swing.JPanel();
        journalsReversalPanel = new javax.swing.JPanel();
        jButton40 = new javax.swing.JButton();
        jPanel161 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        journalSalesCustomer = new javax.swing.JLabel();
        journalSalesDeal = new javax.swing.JLabel();
        journalSalesStock = new javax.swing.JLabel();
        journalSalesMake = new javax.swing.JLabel();
        journalSalesYear = new javax.swing.JLabel();
        journalSalesModel = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        journalSalesCustomerName = new javax.swing.JLabel();
        jPanel162 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel94 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        journalDebits = new javax.swing.JFormattedTextField();
        journalCredits = new javax.swing.JFormattedTextField();
        journalDifferences = new javax.swing.JFormattedTextField();
        journalRO = new javax.swing.JPanel();
        jPanel114 = new javax.swing.JPanel();
        jLabel75 = new javax.swing.JLabel();
        inprogressRadioButton = new javax.swing.JRadioButton();
        completedRadioButton = new javax.swing.JRadioButton();
        closedRadioButton = new javax.swing.JRadioButton();
        jPanel115 = new javax.swing.JPanel();
        jPanel116 = new javax.swing.JPanel();
        jScrollPane26 = new javax.swing.JScrollPane();
        journalROTable = new javax.swing.JTable();
        jPanel145 = new javax.swing.JPanel();
        jPanel153 = new javax.swing.JPanel();
        journalROSearchTextField = new javax.swing.JTextField();
        journalROSearchButton = new javax.swing.JButton();
        jPanel154 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel86 = new javax.swing.JLabel();
        jLabel113 = new javax.swing.JLabel();
        jLabel114 = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 100), new java.awt.Dimension(32767, 32767));
        jPanel4 = new javax.swing.JPanel();
        roStartDate = new com.toedter.calendar.JDateChooser();
        roEndDate = new com.toedter.calendar.JDateChooser();
        jButton23 = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 100), new java.awt.Dimension(32767, 32767));
        jPanel155 = new javax.swing.JPanel();
        jPanel156 = new javax.swing.JPanel();
        jPanel65 = new javax.swing.JPanel();
        jScrollPane21 = new javax.swing.JScrollPane();
        journalRODetailsTable = new javax.swing.JTable();
        jPanel157 = new javax.swing.JPanel();
        jPanel93 = new javax.swing.JPanel();
        journalReversalDatePanel2 = new javax.swing.JPanel();
        jPanel164 = new javax.swing.JPanel();
        jPanel165 = new javax.swing.JPanel();
        jLabel103 = new javax.swing.JLabel();
        jLabel109 = new javax.swing.JLabel();
        jPanel169 = new javax.swing.JPanel();
        journalDealPostedDate2 = new javax.swing.JLabel();
        journalReversalDate2 = new com.toedter.calendar.JDateChooser();
        jPanel109 = new javax.swing.JPanel();
        journalsReversalPanel2 = new javax.swing.JPanel();
        jButton42 = new javax.swing.JButton();
        jPanel166 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        jLabel106 = new javax.swing.JLabel();
        jLabel107 = new javax.swing.JLabel();
        jLabel110 = new javax.swing.JLabel();
        journalSalesCustomer2 = new javax.swing.JLabel();
        journalSalesDeal2 = new javax.swing.JLabel();
        journalSalesMake2 = new javax.swing.JLabel();
        journalSalesYear2 = new javax.swing.JLabel();
        journalSalesModel2 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        journalSalesCustomerName2 = new javax.swing.JLabel();
        jPanel170 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jLabel112 = new javax.swing.JLabel();
        journalDebits3 = new javax.swing.JFormattedTextField();
        journalCredits3 = new javax.swing.JFormattedTextField();
        journalDifferences3 = new javax.swing.JFormattedTextField();
        jPanel158 = new javax.swing.JPanel();
        jButton22 = new javax.swing.JButton();
        journalCollections = new javax.swing.JPanel();
        jPanel117 = new javax.swing.JPanel();
        jLabel76 = new javax.swing.JLabel();
        dealsRadioButton = new javax.swing.JRadioButton();
        serviceRadioButton = new javax.swing.JRadioButton();
        closedRadioButton1 = new javax.swing.JRadioButton();
        jPanel118 = new javax.swing.JPanel();
        jPanel119 = new javax.swing.JPanel();
        jScrollPane27 = new javax.swing.JScrollPane();
        journalCollectionsTable = new javax.swing.JTable();
        jPanel159 = new javax.swing.JPanel();
        jPanel160 = new javax.swing.JPanel();
        journalROSearchTextField1 = new javax.swing.JTextField();
        journalROSearchButton1 = new javax.swing.JButton();
        jPanel172 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel87 = new javax.swing.JLabel();
        jLabel115 = new javax.swing.JLabel();
        jLabel116 = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 100), new java.awt.Dimension(32767, 32767));
        jPanel6 = new javax.swing.JPanel();
        roStartDate1 = new com.toedter.calendar.JDateChooser();
        roEndDate1 = new com.toedter.calendar.JDateChooser();
        jButton24 = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 100), new java.awt.Dimension(32767, 32767));
        jPanel173 = new javax.swing.JPanel();
        jPanel174 = new javax.swing.JPanel();
        jPanel66 = new javax.swing.JPanel();
        jScrollPane22 = new javax.swing.JScrollPane();
        journalCollectionsDetailTable = new javax.swing.JTable();
        jPanel175 = new javax.swing.JPanel();
        jPanel94 = new javax.swing.JPanel();
        journalReversalDatePanel3 = new javax.swing.JPanel();
        jPanel176 = new javax.swing.JPanel();
        jPanel177 = new javax.swing.JPanel();
        jLabel105 = new javax.swing.JLabel();
        jLabel117 = new javax.swing.JLabel();
        jPanel178 = new javax.swing.JPanel();
        journalDealPostedDate3 = new javax.swing.JLabel();
        journalReversalDate3 = new com.toedter.calendar.JDateChooser();
        jPanel110 = new javax.swing.JPanel();
        journalsReversalPanel3 = new javax.swing.JPanel();
        jButton43 = new javax.swing.JButton();
        jPanel179 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel108 = new javax.swing.JLabel();
        jLabel118 = new javax.swing.JLabel();
        jLabel119 = new javax.swing.JLabel();
        jLabel120 = new javax.swing.JLabel();
        journalSalesCustomer3 = new javax.swing.JLabel();
        journalSalesDeal3 = new javax.swing.JLabel();
        journalSalesMake3 = new javax.swing.JLabel();
        journalSalesYear3 = new javax.swing.JLabel();
        journalSalesModel3 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        journalSalesCustomerName3 = new javax.swing.JLabel();
        jPanel180 = new javax.swing.JPanel();
        jLabel42 = new javax.swing.JLabel();
        jLabel122 = new javax.swing.JLabel();
        jLabel133 = new javax.swing.JLabel();
        journalDebits4 = new javax.swing.JFormattedTextField();
        journalCredits4 = new javax.swing.JFormattedTextField();
        journalDifferences4 = new javax.swing.JFormattedTextField();
        jPanel181 = new javax.swing.JPanel();
        jButton25 = new javax.swing.JButton();

        reversalPopup.setMinimumSize(new java.awt.Dimension(950, 500));
        reversalPopup.setModal(true);
        reversalPopup.setName("reversalPopup"); // NOI18N
        reversalPopup.getContentPane().setLayout(new javax.swing.BoxLayout(reversalPopup.getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dms.DMSApp.class).getContext().getResourceMap(Journals.class);
        jPanel168.setBackground(resourceMap.getColor("jPanel168.background")); // NOI18N
        jPanel168.setMinimumSize(new java.awt.Dimension(911, 500));
        jPanel168.setName("jPanel168"); // NOI18N
        jPanel168.setPreferredSize(new java.awt.Dimension(911, 450));
        jPanel168.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel171.setName("jPanel171"); // NOI18N
        jPanel171.setOpaque(false);
        jPanel171.setPreferredSize(new java.awt.Dimension(720, 420));
        jPanel171.setLayout(new javax.swing.BoxLayout(jPanel171, javax.swing.BoxLayout.LINE_AXIS));

        jPanel98.setName("jPanel98"); // NOI18N
        jPanel98.setPreferredSize(new java.awt.Dimension(452, 420));
        jPanel98.setLayout(new javax.swing.BoxLayout(jPanel98, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane23.setName("jScrollPane23"); // NOI18N
        jScrollPane23.setPreferredSize(new java.awt.Dimension(452, 450));

        journalReversalTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Description", "Debit", "Credit", "Account", "Control #", "Reference #"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        journalReversalTable.setName("journalReversalTable"); // NOI18N
        journalReversalTable.setPreferredSize(new java.awt.Dimension(720, 400));
        jScrollPane23.setViewportView(journalReversalTable);
        journalReversalTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("journalReversalTable.columnModel.title0")); // NOI18N
        journalReversalTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("journalReversalTable.columnModel.title1")); // NOI18N
        journalReversalTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalReversalTable.columnModel.title2")); // NOI18N
        journalReversalTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalReversalTable.columnModel.title3")); // NOI18N
        journalReversalTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalReversalTable.columnModel.title4")); // NOI18N
        journalReversalTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalReversalTable.columnModel.title5")); // NOI18N

        jPanel98.add(jScrollPane23);

        jPanel171.add(jPanel98);

        jPanel168.add(jPanel171);

        jPanel197.setMaximumSize(new java.awt.Dimension(191, 380));
        jPanel197.setMinimumSize(new java.awt.Dimension(191, 380));
        jPanel197.setName("jPanel197"); // NOI18N
        jPanel197.setOpaque(false);
        jPanel197.setPreferredSize(new java.awt.Dimension(200, 380));

        jPanel120.setName("jPanel120"); // NOI18N
        jPanel120.setOpaque(false);
        jPanel120.setLayout(new java.awt.CardLayout());

        journalPostDatePanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("journalPostDatePanel1.border.title"))); // NOI18N
        journalPostDatePanel1.setMaximumSize(new java.awt.Dimension(190, 104));
        journalPostDatePanel1.setMinimumSize(new java.awt.Dimension(190, 104));
        journalPostDatePanel1.setName("journalPostDatePanel1"); // NOI18N
        journalPostDatePanel1.setOpaque(false);
        journalPostDatePanel1.setPreferredSize(new java.awt.Dimension(190, 104));
        journalPostDatePanel1.setLayout(new javax.swing.BoxLayout(journalPostDatePanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel198.setMaximumSize(new java.awt.Dimension(191, 50));
        jPanel198.setMinimumSize(new java.awt.Dimension(191, 50));
        jPanel198.setName("jPanel198"); // NOI18N
        jPanel198.setOpaque(false);
        jPanel198.setPreferredSize(new java.awt.Dimension(191, 50));
        jPanel198.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        jPanel199.setName("jPanel199"); // NOI18N
        jPanel199.setOpaque(false);
        jPanel199.setPreferredSize(new java.awt.Dimension(70, 40));
        jPanel199.setLayout(new java.awt.GridLayout(2, 0));

        jLabel121.setFont(resourceMap.getFont("jLabel121.font")); // NOI18N
        jLabel121.setText(resourceMap.getString("jLabel121.text")); // NOI18N
        jLabel121.setName("jLabel121"); // NOI18N
        jLabel121.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel199.add(jLabel121);

        jLabel123.setFont(resourceMap.getFont("jLabel123.font")); // NOI18N
        jLabel123.setText(resourceMap.getString("jLabel123.text")); // NOI18N
        jLabel123.setName("jLabel123"); // NOI18N
        jLabel123.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel199.add(jLabel123);

        jPanel198.add(jPanel199);

        jPanel200.setName("jPanel200"); // NOI18N
        jPanel200.setOpaque(false);
        jPanel200.setPreferredSize(new java.awt.Dimension(100, 40));
        jPanel200.setLayout(new java.awt.GridLayout(2, 1));

        journalDealDate1.setFont(resourceMap.getFont("journalDealDate1.font")); // NOI18N
        journalDealDate1.setName("journalDealDate1"); // NOI18N
        journalDealDate1.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel200.add(journalDealDate1);

        journalReversalDate.setDateFormatString(resourceMap.getString("journalReversalDate.dateFormatString")); // NOI18N
        journalReversalDate.setName("journalReversalDate"); // NOI18N
        journalReversalDate.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel200.add(journalReversalDate);

        jPanel198.add(jPanel200);

        journalPostDatePanel1.add(jPanel198);

        jPanel121.setName("jPanel121"); // NOI18N
        jPanel121.setOpaque(false);
        jPanel121.setLayout(new java.awt.CardLayout());

        journalsPostPanel1.setName("journalsPostPanel1"); // NOI18N
        journalsPostPanel1.setOpaque(false);
        journalsPostPanel1.setLayout(new java.awt.GridBagLayout());

        journalPostReversal.setText(resourceMap.getString("journalPostReversal.text")); // NOI18N
        journalPostReversal.setMaximumSize(new java.awt.Dimension(155, 23));
        journalPostReversal.setMinimumSize(new java.awt.Dimension(155, 23));
        journalPostReversal.setName("journalPostReversal"); // NOI18N
        journalPostReversal.setPreferredSize(new java.awt.Dimension(155, 23));
        journalPostReversal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalPostReversalMouseClicked(evt);
            }
        });
        journalsPostPanel1.add(journalPostReversal, new java.awt.GridBagConstraints());

        jPanel121.add(journalsPostPanel1, "card2");

        journalPostDatePanel1.add(jPanel121);

        jPanel120.add(journalPostDatePanel1, "card2");

        journalReversalDatePanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("journalReversalDatePanel1.border.title"))); // NOI18N
        journalReversalDatePanel1.setMaximumSize(new java.awt.Dimension(200, 104));
        journalReversalDatePanel1.setMinimumSize(new java.awt.Dimension(200, 104));
        journalReversalDatePanel1.setName("journalReversalDatePanel1"); // NOI18N
        journalReversalDatePanel1.setOpaque(false);
        journalReversalDatePanel1.setPreferredSize(new java.awt.Dimension(200, 104));
        journalReversalDatePanel1.setLayout(new javax.swing.BoxLayout(journalReversalDatePanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel201.setMaximumSize(new java.awt.Dimension(191, 50));
        jPanel201.setMinimumSize(new java.awt.Dimension(191, 50));
        jPanel201.setName("jPanel201"); // NOI18N
        jPanel201.setOpaque(false);
        jPanel201.setPreferredSize(new java.awt.Dimension(191, 50));
        jPanel201.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        jPanel202.setName("jPanel202"); // NOI18N
        jPanel202.setOpaque(false);
        jPanel202.setPreferredSize(new java.awt.Dimension(70, 40));
        jPanel202.setLayout(new java.awt.GridLayout(2, 0));

        jLabel124.setFont(resourceMap.getFont("jLabel124.font")); // NOI18N
        jLabel124.setText(resourceMap.getString("jLabel124.text")); // NOI18N
        jLabel124.setName("jLabel124"); // NOI18N
        jLabel124.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel202.add(jLabel124);

        jLabel125.setFont(resourceMap.getFont("jLabel125.font")); // NOI18N
        jLabel125.setText(resourceMap.getString("jLabel125.text")); // NOI18N
        jLabel125.setName("jLabel125"); // NOI18N
        jLabel125.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel202.add(jLabel125);

        jPanel201.add(jPanel202);

        jPanel203.setName("jPanel203"); // NOI18N
        jPanel203.setOpaque(false);
        jPanel203.setPreferredSize(new java.awt.Dimension(100, 40));
        jPanel203.setLayout(new java.awt.GridLayout(2, 1));

        journalDealPostedDate1.setFont(resourceMap.getFont("journalDealPostedDate1.font")); // NOI18N
        journalDealPostedDate1.setName("journalDealPostedDate1"); // NOI18N
        journalDealPostedDate1.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel203.add(journalDealPostedDate1);

        journalReversalDate1.setDateFormatString(resourceMap.getString("journalReversalDate1.dateFormatString")); // NOI18N
        journalReversalDate1.setName("journalReversalDate1"); // NOI18N
        journalReversalDate1.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel203.add(journalReversalDate1);

        jPanel201.add(jPanel203);

        journalReversalDatePanel1.add(jPanel201);

        jPanel124.setName("jPanel124"); // NOI18N
        jPanel124.setOpaque(false);
        jPanel124.setLayout(new java.awt.CardLayout());

        journalsReversalPanel1.setName("journalsReversalPanel1"); // NOI18N
        journalsReversalPanel1.setOpaque(false);
        journalsReversalPanel1.setLayout(new java.awt.GridBagLayout());

        jButton41.setText(resourceMap.getString("jButton41.text")); // NOI18N
        jButton41.setMaximumSize(new java.awt.Dimension(155, 23));
        jButton41.setMinimumSize(new java.awt.Dimension(155, 23));
        jButton41.setName("jButton41"); // NOI18N
        jButton41.setPreferredSize(new java.awt.Dimension(155, 23));
        jButton41.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton41MouseClicked(evt);
            }
        });
        journalsReversalPanel1.add(jButton41, new java.awt.GridBagConstraints());

        jPanel124.add(journalsReversalPanel1, "card2");

        journalReversalDatePanel1.add(jPanel124);

        jPanel120.add(journalReversalDatePanel1, "card3");

        jPanel197.add(jPanel120);

        jPanel204.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel204.border.title"))); // NOI18N
        jPanel204.setMaximumSize(new java.awt.Dimension(200, 150));
        jPanel204.setMinimumSize(new java.awt.Dimension(200, 150));
        jPanel204.setName("jPanel204"); // NOI18N
        jPanel204.setOpaque(false);
        jPanel204.setPreferredSize(new java.awt.Dimension(200, 150));
        jPanel204.setLayout(new java.awt.GridBagLayout());

        jLabel57.setFont(resourceMap.getFont("jLabel57.font")); // NOI18N
        jLabel57.setText(resourceMap.getString("jLabel57.text")); // NOI18N
        jLabel57.setName("jLabel57"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel57, gridBagConstraints);

        jLabel126.setFont(resourceMap.getFont("jLabel126.font")); // NOI18N
        jLabel126.setText(resourceMap.getString("jLabel126.text")); // NOI18N
        jLabel126.setName("jLabel126"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel126, gridBagConstraints);

        jLabel127.setFont(resourceMap.getFont("jLabel127.font")); // NOI18N
        jLabel127.setText(resourceMap.getString("jLabel127.text")); // NOI18N
        jLabel127.setName("jLabel127"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel127, gridBagConstraints);

        jLabel128.setFont(resourceMap.getFont("jLabel128.font")); // NOI18N
        jLabel128.setText(resourceMap.getString("jLabel128.text")); // NOI18N
        jLabel128.setName("jLabel128"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel128, gridBagConstraints);

        jLabel129.setFont(resourceMap.getFont("jLabel129.font")); // NOI18N
        jLabel129.setText(resourceMap.getString("jLabel129.text")); // NOI18N
        jLabel129.setName("jLabel129"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel129, gridBagConstraints);

        jLabel130.setFont(resourceMap.getFont("jLabel130.font")); // NOI18N
        jLabel130.setText(resourceMap.getString("jLabel130.text")); // NOI18N
        jLabel130.setName("jLabel130"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel130, gridBagConstraints);

        journalSalesCustomer1.setName("journalSalesCustomer1"); // NOI18N
        journalSalesCustomer1.setPreferredSize(new java.awt.Dimension(83, 14));
        jPanel204.add(journalSalesCustomer1, new java.awt.GridBagConstraints());

        journalSalesDeal1.setName("journalSalesDeal1"); // NOI18N
        journalSalesDeal1.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel204.add(journalSalesDeal1, gridBagConstraints);

        journalSalesStock1.setName("journalSalesStock1"); // NOI18N
        journalSalesStock1.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        jPanel204.add(journalSalesStock1, gridBagConstraints);

        journalSalesMake1.setName("journalSalesMake1"); // NOI18N
        journalSalesMake1.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        jPanel204.add(journalSalesMake1, gridBagConstraints);

        journalSalesYear1.setName("journalSalesYear1"); // NOI18N
        journalSalesYear1.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        jPanel204.add(journalSalesYear1, gridBagConstraints);

        journalSalesModel1.setName("journalSalesModel1"); // NOI18N
        journalSalesModel1.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        jPanel204.add(journalSalesModel1, gridBagConstraints);

        jLabel58.setFont(resourceMap.getFont("jLabel58.font")); // NOI18N
        jLabel58.setText(resourceMap.getString("jLabel58.text")); // NOI18N
        jLabel58.setName("jLabel58"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel204.add(jLabel58, gridBagConstraints);

        journalSalesCustomerName1.setName("journalSalesCustomerName1"); // NOI18N
        journalSalesCustomerName1.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel204.add(journalSalesCustomerName1, gridBagConstraints);

        jPanel197.add(jPanel204);

        jPanel205.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel205.border.title"))); // NOI18N
        jPanel205.setMaximumSize(new java.awt.Dimension(200, 70));
        jPanel205.setMinimumSize(new java.awt.Dimension(200, 70));
        jPanel205.setName("jPanel205"); // NOI18N
        jPanel205.setOpaque(false);
        jPanel205.setPreferredSize(new java.awt.Dimension(200, 70));
        jPanel205.setLayout(null);

        jLabel59.setFont(resourceMap.getFont("jLabel59.font")); // NOI18N
        jLabel59.setText(resourceMap.getString("jLabel59.text")); // NOI18N
        jLabel59.setName("jLabel59"); // NOI18N
        jPanel205.add(jLabel59);
        jLabel59.setBounds(40, 10, 42, 16);

        jLabel131.setFont(resourceMap.getFont("jLabel131.font")); // NOI18N
        jLabel131.setText(resourceMap.getString("jLabel131.text")); // NOI18N
        jLabel131.setName("jLabel131"); // NOI18N
        jPanel205.add(jLabel131);
        jLabel131.setBounds(40, 30, 47, 16);

        jLabel132.setFont(resourceMap.getFont("jLabel132.font")); // NOI18N
        jLabel132.setText(resourceMap.getString("jLabel132.text")); // NOI18N
        jLabel132.setName("jLabel132"); // NOI18N
        jPanel205.add(jLabel132);
        jLabel132.setBounds(20, 50, 65, 16);

        debits11.setEditable(false);
        debits11.setBorder(null);
        debits11.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        debits11.setName("debits11"); // NOI18N
        jPanel205.add(debits11);
        debits11.setBounds(90, 10, 100, 14);

        credits11.setEditable(false);
        credits11.setBorder(null);
        credits11.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        credits11.setName("credits11"); // NOI18N
        jPanel205.add(credits11);
        credits11.setBounds(90, 30, 100, 14);

        difference11.setEditable(false);
        difference11.setBorder(null);
        difference11.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        difference11.setName("difference11"); // NOI18N
        jPanel205.add(difference11);
        difference11.setBounds(90, 50, 100, 14);

        jPanel197.add(jPanel205);

        jPanel168.add(jPanel197);

        reversalPopup.getContentPane().add(jPanel168);

        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jideTabbedPane3.setBoldActiveTab(true);
        jideTabbedPane3.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jideTabbedPane3.setName("jideTabbedPane3"); // NOI18N
        jideTabbedPane3.setPreferredSize(new java.awt.Dimension(950, 651));
        jideTabbedPane3.setSelectedTabFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jideTabbedPane3.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jideTabbedPane3ComponentShown(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel1ComponentShown(evt);
            }
        });
        jPanel1.setLayout(new java.awt.BorderLayout());

        enterBillsPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("enterBillsPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
        enterBillsPanel3.setMaximumSize(new java.awt.Dimension(32767, 115));
        enterBillsPanel3.setMinimumSize(new java.awt.Dimension(872, 115));
        enterBillsPanel3.setName("enterBillsPanel3"); // NOI18N
        enterBillsPanel3.setOpaque(false);
        enterBillsPanel3.setPreferredSize(new java.awt.Dimension(872, 115));
        enterBillsPanel3.setRequestFocusEnabled(false);
        enterBillsPanel3.setLayout(new java.awt.GridLayout(1, 0, 20, 0));

        jPanel218.setMaximumSize(new java.awt.Dimension(200, 86));
        jPanel218.setMinimumSize(new java.awt.Dimension(200, 86));
        jPanel218.setName("jPanel218"); // NOI18N
        jPanel218.setOpaque(false);
        jPanel218.setPreferredSize(new java.awt.Dimension(200, 86));
        jPanel218.setLayout(new java.awt.BorderLayout(5, 0));

        jPanel219.setName("jPanel219"); // NOI18N
        jPanel219.setOpaque(false);
        jPanel219.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        jLabel139.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel139.setText(resourceMap.getString("jLabel139.text")); // NOI18N
        jLabel139.setName("jLabel139"); // NOI18N
        jPanel219.add(jLabel139);

        jLabel141.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel141.setText(resourceMap.getString("jLabel141.text")); // NOI18N
        jLabel141.setName("jLabel141"); // NOI18N
        jPanel219.add(jLabel141);

        jLabel142.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel142.setText(resourceMap.getString("jLabel142.text")); // NOI18N
        jLabel142.setName("jLabel142"); // NOI18N
        jPanel219.add(jLabel142);

        filler12.setName("filler12"); // NOI18N
        jPanel219.add(filler12);

        jPanel218.add(jPanel219, java.awt.BorderLayout.WEST);

        jPanel220.setName("jPanel220"); // NOI18N
        jPanel220.setOpaque(false);
        jPanel220.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        billInvoice.setName("billInvoice"); // NOI18N
        billInvoice.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                billInvoiceInputMethodTextChanged(evt);
            }
        });
        jPanel220.add(billInvoice);

        billMemo.setName("billMemo"); // NOI18N
        jPanel220.add(billMemo);

        journalClassCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Admin", "Miami", "Miami:Service", "Hollywood", "Wholesale", " " }));
        journalClassCombo.setName("journalClassCombo"); // NOI18N
        jPanel220.add(journalClassCombo);

        filler14.setName("filler14"); // NOI18N
        jPanel220.add(filler14);

        jPanel218.add(jPanel220, java.awt.BorderLayout.CENTER);

        enterBillsPanel3.add(jPanel218);

        jPanel221.setMaximumSize(new java.awt.Dimension(200, 86));
        jPanel221.setMinimumSize(new java.awt.Dimension(200, 86));
        jPanel221.setName("jPanel221"); // NOI18N
        jPanel221.setOpaque(false);
        jPanel221.setPreferredSize(new java.awt.Dimension(200, 86));
        jPanel221.setLayout(new java.awt.BorderLayout(5, 0));

        journallClass.setName("journallClass"); // NOI18N
        journallClass.setOpaque(false);
        journallClass.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        billPostDate.setDateFormatString(resourceMap.getString("billPostDate.dateFormatString")); // NOI18N
        billPostDate.setName("billPostDate"); // NOI18N
        journallClass.add(billPostDate);

        filler11.setName("filler11"); // NOI18N
        journallClass.add(filler11);

        filler13.setName("filler13"); // NOI18N
        journallClass.add(filler13);

        filler17.setName("filler17"); // NOI18N
        journallClass.add(filler17);

        jPanel221.add(journallClass, java.awt.BorderLayout.CENTER);

        jPanel223.setName("jPanel223"); // NOI18N
        jPanel223.setOpaque(false);
        jPanel223.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        jLabel136.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel136.setText(resourceMap.getString("jLabel136.text")); // NOI18N
        jLabel136.setName("jLabel136"); // NOI18N
        jPanel223.add(jLabel136);

        filler10.setName("filler10"); // NOI18N
        jPanel223.add(filler10);

        filler15.setName("filler15"); // NOI18N
        jPanel223.add(filler15);

        filler16.setName("filler16"); // NOI18N
        jPanel223.add(filler16);

        jPanel221.add(jPanel223, java.awt.BorderLayout.WEST);

        enterBillsPanel3.add(jPanel221);

        jPanel224.setMaximumSize(new java.awt.Dimension(200, 86));
        jPanel224.setMinimumSize(new java.awt.Dimension(200, 86));
        jPanel224.setName("jPanel224"); // NOI18N
        jPanel224.setOpaque(false);
        jPanel224.setPreferredSize(new java.awt.Dimension(200, 86));
        jPanel224.setLayout(null);

        jButton46.setText(resourceMap.getString("jButton46.text")); // NOI18N
        jButton46.setToolTipText(resourceMap.getString("jButton46.toolTipText")); // NOI18N
        jButton46.setName("jButton46"); // NOI18N
        jButton46.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton46enterBillsButtonsClicked(evt);
            }
        });
        jPanel224.add(jButton46);
        jButton46.setBounds(4, 5, 103, 23);

        jButton48.setText(resourceMap.getString("jButton48.text")); // NOI18N
        jButton48.setMaximumSize(new java.awt.Dimension(103, 23));
        jButton48.setMinimumSize(new java.awt.Dimension(103, 23));
        jButton48.setName("jButton48"); // NOI18N
        jButton48.setPreferredSize(new java.awt.Dimension(103, 23));
        jButton48.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton48enterBillsButtonsClicked(evt);
            }
        });
        jPanel224.add(jButton48);
        jButton48.setBounds(112, 5, 103, 23);

        jButton18.setText(resourceMap.getString("jButton18.text")); // NOI18N
        jButton18.setMaximumSize(new java.awt.Dimension(103, 23));
        jButton18.setMinimumSize(new java.awt.Dimension(103, 23));
        jButton18.setName("jButton18"); // NOI18N
        jButton18.setPreferredSize(new java.awt.Dimension(103, 23));
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });
        jPanel224.add(jButton18);
        jButton18.setBounds(4, 40, 103, 23);

        jButton19.setText(resourceMap.getString("jButton19.text")); // NOI18N
        jButton19.setMaximumSize(new java.awt.Dimension(103, 23));
        jButton19.setMinimumSize(new java.awt.Dimension(103, 23));
        jButton19.setName("jButton19"); // NOI18N
        jButton19.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });
        jPanel224.add(jButton19);
        jButton19.setBounds(112, 40, 103, 23);

        enterBillsPanel3.add(jPanel224);

        jPanel195.setName("jPanel195"); // NOI18N
        jPanel195.setOpaque(false);
        jPanel195.setPreferredSize(new java.awt.Dimension(100, 106));
        jPanel195.setLayout(new javax.swing.BoxLayout(jPanel195, javax.swing.BoxLayout.LINE_AXIS));

        jPanel163.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel163.border.title"))); // NOI18N
        jPanel163.setMaximumSize(new java.awt.Dimension(190, 70));
        jPanel163.setMinimumSize(new java.awt.Dimension(190, 70));
        jPanel163.setName("jPanel163"); // NOI18N
        jPanel163.setOpaque(false);
        jPanel163.setPreferredSize(new java.awt.Dimension(190, 70));
        jPanel163.setLayout(new java.awt.GridBagLayout());

        jLabel38.setFont(resourceMap.getFont("jLabel38.font")); // NOI18N
        jLabel38.setText(resourceMap.getString("jLabel38.text")); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel163.add(jLabel38, gridBagConstraints);

        jLabel98.setFont(resourceMap.getFont("jLabel98.font")); // NOI18N
        jLabel98.setText(resourceMap.getString("jLabel98.text")); // NOI18N
        jLabel98.setName("jLabel98"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel163.add(jLabel98, gridBagConstraints);

        jLabel99.setFont(resourceMap.getFont("jLabel99.font")); // NOI18N
        jLabel99.setText(resourceMap.getString("jLabel99.text")); // NOI18N
        jLabel99.setName("jLabel99"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel163.add(jLabel99, gridBagConstraints);

        journalDebits2.setEditable(false);
        journalDebits2.setBorder(null);
        journalDebits2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
        journalDebits2.setMaximumSize(new java.awt.Dimension(83, 14));
        journalDebits2.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDebits2.setName("journalDebits2"); // NOI18N
        journalDebits2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel163.add(journalDebits2, gridBagConstraints);

        journalCredits2.setEditable(false);
        journalCredits2.setBorder(null);
        journalCredits2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
        journalCredits2.setMinimumSize(new java.awt.Dimension(83, 14));
        journalCredits2.setName("journalCredits2"); // NOI18N
        journalCredits2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel163.add(journalCredits2, gridBagConstraints);

        journalDifferences2.setEditable(false);
        journalDifferences2.setBorder(null);
        journalDifferences2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
        journalDifferences2.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDifferences2.setName("journalDifferences2"); // NOI18N
        journalDifferences2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel163.add(journalDifferences2, gridBagConstraints);

        jPanel195.add(jPanel163);

        enterBillsPanel3.add(jPanel195);

        jPanel1.add(enterBillsPanel3, java.awt.BorderLayout.NORTH);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane18.setName("jScrollPane18"); // NOI18N

        journalPostingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Account", "Debits", "Credits", "Control #", "Memo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        journalPostingTable.setColumnSelectionAllowed(true);
        journalPostingTable.setMaximumSize(new java.awt.Dimension(2147483647, 100));
        journalPostingTable.setMinimumSize(new java.awt.Dimension(375, 100));
        journalPostingTable.setName("journalPostingTable"); // NOI18N
        journalPostingTable.setOpaque(false);
        journalPostingTable.setPreferredSize(new java.awt.Dimension(375, 100));
        journalPostingTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        journalPostingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalPostingTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                journalPostingTableMouseExited(evt);
            }
        });
        journalPostingTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalPostingTableKeyReleased(evt);
            }
        });
        jScrollPane18.setViewportView(journalPostingTable);
        journalPostingTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        journalPostingTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("journalPostingTable.columnModel.title0")); // NOI18N
        journalPostingTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("journalPostingTable.columnModel.title1")); // NOI18N
        journalPostingTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalPostingTable.columnModel.title5")); // NOI18N
        journalPostingTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalPostingTable.columnModel.title2")); // NOI18N
        journalPostingTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalPostingTable.columnModel.title4")); // NOI18N

        jPanel2.add(jScrollPane18);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jideTabbedPane3.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        journalDeals.setBackground(resourceMap.getColor("journalDeals.background")); // NOI18N
        journalDeals.setName("journalDeals"); // NOI18N
        journalDeals.setOpaque(false);
        journalDeals.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                journalDealsComponentShown(evt);
            }
        });
        journalDeals.setLayout(new javax.swing.BoxLayout(journalDeals, javax.swing.BoxLayout.Y_AXIS));

        jPanel111.setMaximumSize(new java.awt.Dimension(1000000, 23));
        jPanel111.setName("jPanel111"); // NOI18N
        jPanel111.setOpaque(false);
        jPanel111.setPreferredSize(new java.awt.Dimension(911, 23));
        jPanel111.setLayout(new javax.swing.BoxLayout(jPanel111, javax.swing.BoxLayout.LINE_AXIS));

        jLabel74.setText(resourceMap.getString("jLabel74.text")); // NOI18N
        jLabel74.setName("jLabel74"); // NOI18N
        jPanel111.add(jLabel74);

        buttonGroup1.add(journalRetailRadio);
        journalRetailRadio.setSelected(true);
        journalRetailRadio.setText(resourceMap.getString("journalRetailRadio.text")); // NOI18N
        journalRetailRadio.setName("journalRetailRadio"); // NOI18N
        journalRetailRadio.setOpaque(false);
        journalRetailRadio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalRetailRadiojournalsTypeRadioButton(evt);
            }
        });
        jPanel111.add(journalRetailRadio);

        buttonGroup1.add(journalWholesaleRadio);
        journalWholesaleRadio.setText(resourceMap.getString("journalWholesaleRadio.text")); // NOI18N
        journalWholesaleRadio.setName("journalWholesaleRadio"); // NOI18N
        journalWholesaleRadio.setOpaque(false);
        journalWholesaleRadio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalWholesaleRadiojournalsTypeRadioButton(evt);
            }
        });
        jPanel111.add(journalWholesaleRadio);

        buttonGroup1.add(jRadioButton9);
        jRadioButton9.setText(resourceMap.getString("jRadioButton9.text")); // NOI18N
        jRadioButton9.setEnabled(false);
        jRadioButton9.setName("jRadioButton9"); // NOI18N
        jRadioButton9.setOpaque(false);
        jPanel111.add(jRadioButton9);

        buttonGroup1.add(jRadioButton10);
        jRadioButton10.setText(resourceMap.getString("jRadioButton10.text")); // NOI18N
        jRadioButton10.setEnabled(false);
        jRadioButton10.setName("jRadioButton10"); // NOI18N
        jRadioButton10.setOpaque(false);
        jPanel111.add(jRadioButton10);

        buttonGroup1.add(journalPostedRadio);
        journalPostedRadio.setText(resourceMap.getString("journalPostedRadio.text")); // NOI18N
        journalPostedRadio.setName("journalPostedRadio"); // NOI18N
        journalPostedRadio.setOpaque(false);
        journalPostedRadio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalPostedRadiojournalsTypeRadioButton(evt);
            }
        });
        jPanel111.add(journalPostedRadio);

        journalDeals.add(jPanel111);

        jPanel112.setMaximumSize(new java.awt.Dimension(32767, 10000));
        jPanel112.setMinimumSize(new java.awt.Dimension(911, 100));
        jPanel112.setName("jPanel112"); // NOI18N
        jPanel112.setOpaque(false);
        jPanel112.setPreferredSize(new java.awt.Dimension(911, 100));
        jPanel112.setRequestFocusEnabled(false);
        jPanel112.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel113.setName("jPanel113"); // NOI18N
        jPanel113.setOpaque(false);
        jPanel113.setPreferredSize(new java.awt.Dimension(720, 130));
        jPanel113.setLayout(new javax.swing.BoxLayout(jPanel113, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane25.setName("jScrollPane25"); // NOI18N

        journalDealsTable.setAutoCreateRowSorter(true);
        journalDealsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Deal #", "Date", "Customer", "Stock #", "Deal Type", "Stage", "LotName", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        journalDealsTable.setName("journalDealsTable"); // NOI18N
        journalDealsTable.setOpaque(false);
        journalDealsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalDealsTableMouseClicked(evt);
            }
        });
        journalDealsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                journalDealsTableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalDealsTableKeyReleased(evt);
            }
        });
        jScrollPane25.setViewportView(journalDealsTable);
        journalDealsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title0")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title1")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title2")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title3")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title4")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title5")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title6")); // NOI18N
        journalDealsTable.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("journalDealsTable.columnModel.title7")); // NOI18N

        jPanel113.add(jScrollPane25);

        jPanel112.add(jPanel113);

        jPanel141.setName("jPanel141"); // NOI18N
        jPanel141.setOpaque(false);
        jPanel141.setPreferredSize(new java.awt.Dimension(191, 130));

        jPanel149.setMaximumSize(new java.awt.Dimension(170, 25));
        jPanel149.setMinimumSize(new java.awt.Dimension(170, 25));
        jPanel149.setName("jPanel149"); // NOI18N
        jPanel149.setOpaque(false);

        journalSearchTextField.setName("journalSearchTextField"); // NOI18N
        journalSearchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalSearchTextFieldKeyReleased(evt);
            }
        });

        journalSearchButton.setText(resourceMap.getString("journalSearchButton.text")); // NOI18N
        journalSearchButton.setName("journalSearchButton"); // NOI18N
        journalSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalSearchButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel149Layout = new javax.swing.GroupLayout(jPanel149);
        jPanel149.setLayout(jPanel149Layout);
        jPanel149Layout.setHorizontalGroup(
            jPanel149Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel149Layout.createSequentialGroup()
                .addComponent(journalSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(journalSearchButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel149Layout.setVerticalGroup(
            jPanel149Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel149Layout.createSequentialGroup()
                .addComponent(journalSearchButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel149Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(journalSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel141.add(jPanel149);

        jPanel146.setName("jPanel146"); // NOI18N
        jPanel146.setOpaque(false);
        jPanel146.setPreferredSize(new java.awt.Dimension(191, 100));

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel146Layout = new javax.swing.GroupLayout(jPanel146);
        jPanel146.setLayout(jPanel146Layout);
        jPanel146Layout.setHorizontalGroup(
            jPanel146Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
            .addGroup(jPanel146Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel146Layout.createSequentialGroup()
                    .addGap(0, 64, Short.MAX_VALUE)
                    .addComponent(jButton1)
                    .addGap(0, 64, Short.MAX_VALUE)))
        );
        jPanel146Layout.setVerticalGroup(
            jPanel146Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(jPanel146Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel146Layout.createSequentialGroup()
                    .addGap(0, 38, Short.MAX_VALUE)
                    .addComponent(jButton1)
                    .addGap(0, 39, Short.MAX_VALUE)))
        );

        jPanel141.add(jPanel146);

        jPanel112.add(jPanel141);

        journalDeals.add(jPanel112);

        jPanel142.setMinimumSize(new java.awt.Dimension(911, 500));
        jPanel142.setName("jPanel142"); // NOI18N
        jPanel142.setOpaque(false);
        jPanel142.setPreferredSize(new java.awt.Dimension(911, 450));
        jPanel142.setLayout(new javax.swing.BoxLayout(jPanel142, javax.swing.BoxLayout.LINE_AXIS));

        jPanel143.setMinimumSize(new java.awt.Dimension(700, 450));
        jPanel143.setName("jPanel143"); // NOI18N
        jPanel143.setOpaque(false);
        jPanel143.setPreferredSize(new java.awt.Dimension(700, 450));
        jPanel143.setLayout(new javax.swing.BoxLayout(jPanel143, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        journalDealsDetailsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Description", "Debit", "Credit", "Account", "Control #", "Reference #"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        journalDealsDetailsTable.setName("journalDealsDetailsTable"); // NOI18N
        jScrollPane1.setViewportView(journalDealsDetailsTable);
        journalDealsDetailsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("journalDealsDetailsTable.columnModel.title0")); // NOI18N
        journalDealsDetailsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("journalDealsDetailsTable.columnModel.title1")); // NOI18N
        journalDealsDetailsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalDealsDetailsTable.columnModel.title2")); // NOI18N
        journalDealsDetailsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalDealsDetailsTable.columnModel.title3")); // NOI18N
        journalDealsDetailsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalDealsDetailsTable.columnModel.title4")); // NOI18N
        journalDealsDetailsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalDealsDetailsTable.columnModel.title5")); // NOI18N

        jPanel143.add(jScrollPane1);

        jPanel142.add(jPanel143);

        jPanel144.setMaximumSize(new java.awt.Dimension(191, 380));
        jPanel144.setMinimumSize(new java.awt.Dimension(191, 380));
        jPanel144.setName("jPanel144"); // NOI18N
        jPanel144.setOpaque(false);
        jPanel144.setPreferredSize(new java.awt.Dimension(191, 380));

        jPanel92.setName("jPanel92"); // NOI18N
        jPanel92.setOpaque(false);
        jPanel92.setLayout(new java.awt.CardLayout());

        journalPostDatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("journalPostDatePanel.border.title"))); // NOI18N
        journalPostDatePanel.setMaximumSize(new java.awt.Dimension(190, 104));
        journalPostDatePanel.setMinimumSize(new java.awt.Dimension(190, 104));
        journalPostDatePanel.setName("journalPostDatePanel"); // NOI18N
        journalPostDatePanel.setOpaque(false);
        journalPostDatePanel.setPreferredSize(new java.awt.Dimension(190, 104));
        journalPostDatePanel.setLayout(new javax.swing.BoxLayout(journalPostDatePanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel147.setMaximumSize(new java.awt.Dimension(191, 50));
        jPanel147.setMinimumSize(new java.awt.Dimension(191, 50));
        jPanel147.setName("jPanel147"); // NOI18N
        jPanel147.setOpaque(false);
        jPanel147.setPreferredSize(new java.awt.Dimension(191, 50));
        jPanel147.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        jPanel148.setName("jPanel148"); // NOI18N
        jPanel148.setOpaque(false);
        jPanel148.setPreferredSize(new java.awt.Dimension(70, 40));
        jPanel148.setLayout(new java.awt.GridLayout(2, 0));

        jLabel89.setFont(resourceMap.getFont("jLabel89.font")); // NOI18N
        jLabel89.setText(resourceMap.getString("jLabel89.text")); // NOI18N
        jLabel89.setName("jLabel89"); // NOI18N
        jLabel89.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel148.add(jLabel89);

        jLabel90.setFont(resourceMap.getFont("jLabel90.font")); // NOI18N
        jLabel90.setText(resourceMap.getString("jLabel90.text")); // NOI18N
        jLabel90.setName("jLabel90"); // NOI18N
        jLabel90.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel148.add(jLabel90);

        jPanel147.add(jPanel148);

        jPanel150.setName("jPanel150"); // NOI18N
        jPanel150.setOpaque(false);
        jPanel150.setPreferredSize(new java.awt.Dimension(100, 40));
        jPanel150.setLayout(new java.awt.GridLayout(2, 1));

        journalDealDate.setFont(resourceMap.getFont("journalDealDate.font")); // NOI18N
        journalDealDate.setMaximumSize(new java.awt.Dimension(40, 20));
        journalDealDate.setMinimumSize(new java.awt.Dimension(40, 20));
        journalDealDate.setName("journalDealDate"); // NOI18N
        journalDealDate.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel150.add(journalDealDate);

        journalPostDate.setDateFormatString(resourceMap.getString("journalPostDate.dateFormatString")); // NOI18N
        journalPostDate.setName("journalPostDate"); // NOI18N
        journalPostDate.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel150.add(journalPostDate);

        jPanel147.add(jPanel150);

        journalPostDatePanel.add(jPanel147);

        jPanel64.setName("jPanel64"); // NOI18N
        jPanel64.setOpaque(false);
        jPanel64.setLayout(new java.awt.CardLayout());

        journalsPostPanel.setName("journalsPostPanel"); // NOI18N
        journalsPostPanel.setOpaque(false);
        journalsPostPanel.setLayout(new java.awt.GridBagLayout());

        journalPostButton.setText(resourceMap.getString("journalPostButton.text")); // NOI18N
        journalPostButton.setMaximumSize(new java.awt.Dimension(155, 23));
        journalPostButton.setMinimumSize(new java.awt.Dimension(155, 23));
        journalPostButton.setName("journalPostButton"); // NOI18N
        journalPostButton.setPreferredSize(new java.awt.Dimension(155, 23));
        journalPostButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalPostButtonMouseClicked(evt);
            }
        });
        journalsPostPanel.add(journalPostButton, new java.awt.GridBagConstraints());

        jPanel64.add(journalsPostPanel, "card2");

        journalPostDatePanel.add(jPanel64);

        jPanel92.add(journalPostDatePanel, "card2");

        journalReversalDatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("journalReversalDatePanel.border.title"))); // NOI18N
        journalReversalDatePanel.setMaximumSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel.setMinimumSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel.setName("journalReversalDatePanel"); // NOI18N
        journalReversalDatePanel.setOpaque(false);
        journalReversalDatePanel.setPreferredSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel.setLayout(new javax.swing.BoxLayout(journalReversalDatePanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel151.setMaximumSize(new java.awt.Dimension(191, 50));
        jPanel151.setMinimumSize(new java.awt.Dimension(191, 50));
        jPanel151.setName("jPanel151"); // NOI18N
        jPanel151.setOpaque(false);
        jPanel151.setPreferredSize(new java.awt.Dimension(191, 50));
        jPanel151.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        jPanel152.setName("jPanel152"); // NOI18N
        jPanel152.setOpaque(false);
        jPanel152.setPreferredSize(new java.awt.Dimension(70, 40));
        jPanel152.setLayout(new java.awt.GridLayout(2, 0));

        jLabel102.setFont(resourceMap.getFont("jLabel102.font")); // NOI18N
        jLabel102.setText(resourceMap.getString("jLabel102.text")); // NOI18N
        jLabel102.setName("jLabel102"); // NOI18N
        jLabel102.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel152.add(jLabel102);

        jPanel151.add(jPanel152);

        jPanel167.setName("jPanel167"); // NOI18N
        jPanel167.setOpaque(false);
        jPanel167.setPreferredSize(new java.awt.Dimension(100, 40));
        jPanel167.setLayout(new java.awt.GridLayout(2, 1));

        journalDealPostedDate.setFont(resourceMap.getFont("journalDealPostedDate.font")); // NOI18N
        journalDealPostedDate.setName("journalDealPostedDate"); // NOI18N
        journalDealPostedDate.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel167.add(journalDealPostedDate);

        jPanel151.add(jPanel167);

        journalReversalDatePanel.add(jPanel151);

        jPanel108.setName("jPanel108"); // NOI18N
        jPanel108.setOpaque(false);
        jPanel108.setLayout(new java.awt.CardLayout());

        journalsReversalPanel.setName("journalsReversalPanel"); // NOI18N
        journalsReversalPanel.setOpaque(false);
        journalsReversalPanel.setLayout(new java.awt.GridBagLayout());

        jButton40.setText(resourceMap.getString("jButton40.text")); // NOI18N
        jButton40.setMaximumSize(new java.awt.Dimension(155, 23));
        jButton40.setMinimumSize(new java.awt.Dimension(155, 23));
        jButton40.setName("jButton40"); // NOI18N
        jButton40.setPreferredSize(new java.awt.Dimension(155, 23));
        jButton40.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton40MouseClicked(evt);
            }
        });
        journalsReversalPanel.add(jButton40, new java.awt.GridBagConstraints());

        jPanel108.add(journalsReversalPanel, "card2");

        journalReversalDatePanel.add(jPanel108);

        jPanel92.add(journalReversalDatePanel, "card3");

        jPanel144.add(jPanel92);

        jPanel161.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel161.border.title"))); // NOI18N
        jPanel161.setMaximumSize(new java.awt.Dimension(190, 160));
        jPanel161.setMinimumSize(new java.awt.Dimension(190, 160));
        jPanel161.setName("jPanel161"); // NOI18N
        jPanel161.setOpaque(false);
        jPanel161.setPreferredSize(new java.awt.Dimension(190, 160));
        jPanel161.setLayout(new java.awt.GridBagLayout());

        jLabel36.setFont(resourceMap.getFont("jLabel36.font")); // NOI18N
        jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel36, gridBagConstraints);

        jLabel91.setFont(resourceMap.getFont("jLabel91.font")); // NOI18N
        jLabel91.setText(resourceMap.getString("jLabel91.text")); // NOI18N
        jLabel91.setName("jLabel91"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel91, gridBagConstraints);

        jLabel92.setFont(resourceMap.getFont("jLabel92.font")); // NOI18N
        jLabel92.setText(resourceMap.getString("jLabel92.text")); // NOI18N
        jLabel92.setName("jLabel92"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel92, gridBagConstraints);

        jLabel93.setFont(resourceMap.getFont("jLabel93.font")); // NOI18N
        jLabel93.setText(resourceMap.getString("jLabel93.text")); // NOI18N
        jLabel93.setName("jLabel93"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel93, gridBagConstraints);

        jLabel95.setFont(resourceMap.getFont("jLabel95.font")); // NOI18N
        jLabel95.setText(resourceMap.getString("jLabel95.text")); // NOI18N
        jLabel95.setName("jLabel95"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel95, gridBagConstraints);

        jLabel96.setFont(resourceMap.getFont("jLabel96.font")); // NOI18N
        jLabel96.setText(resourceMap.getString("jLabel96.text")); // NOI18N
        jLabel96.setName("jLabel96"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel96, gridBagConstraints);

        journalSalesCustomer.setName("journalSalesCustomer"); // NOI18N
        journalSalesCustomer.setPreferredSize(new java.awt.Dimension(83, 14));
        jPanel161.add(journalSalesCustomer, new java.awt.GridBagConstraints());

        journalSalesDeal.setName("journalSalesDeal"); // NOI18N
        journalSalesDeal.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel161.add(journalSalesDeal, gridBagConstraints);

        journalSalesStock.setName("journalSalesStock"); // NOI18N
        journalSalesStock.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        jPanel161.add(journalSalesStock, gridBagConstraints);

        journalSalesMake.setName("journalSalesMake"); // NOI18N
        journalSalesMake.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        jPanel161.add(journalSalesMake, gridBagConstraints);

        journalSalesYear.setName("journalSalesYear"); // NOI18N
        journalSalesYear.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        jPanel161.add(journalSalesYear, gridBagConstraints);

        journalSalesModel.setName("journalSalesModel"); // NOI18N
        journalSalesModel.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        jPanel161.add(journalSalesModel, gridBagConstraints);

        jLabel50.setFont(resourceMap.getFont("jLabel50.font")); // NOI18N
        jLabel50.setText(resourceMap.getString("jLabel50.text")); // NOI18N
        jLabel50.setName("jLabel50"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel161.add(jLabel50, gridBagConstraints);

        journalSalesCustomerName.setName("journalSalesCustomerName"); // NOI18N
        journalSalesCustomerName.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel161.add(journalSalesCustomerName, gridBagConstraints);

        jPanel144.add(jPanel161);

        jPanel162.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel162.border.title"))); // NOI18N
        jPanel162.setMaximumSize(new java.awt.Dimension(190, 70));
        jPanel162.setMinimumSize(new java.awt.Dimension(190, 70));
        jPanel162.setName("jPanel162"); // NOI18N
        jPanel162.setOpaque(false);
        jPanel162.setPreferredSize(new java.awt.Dimension(190, 70));
        jPanel162.setLayout(new java.awt.GridBagLayout());

        jLabel37.setFont(resourceMap.getFont("jLabel37.font")); // NOI18N
        jLabel37.setText(resourceMap.getString("jLabel37.text")); // NOI18N
        jLabel37.setName("jLabel37"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel162.add(jLabel37, gridBagConstraints);

        jLabel94.setFont(resourceMap.getFont("jLabel94.font")); // NOI18N
        jLabel94.setText(resourceMap.getString("jLabel94.text")); // NOI18N
        jLabel94.setName("jLabel94"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel162.add(jLabel94, gridBagConstraints);

        jLabel97.setFont(resourceMap.getFont("jLabel97.font")); // NOI18N
        jLabel97.setText(resourceMap.getString("jLabel97.text")); // NOI18N
        jLabel97.setName("jLabel97"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel162.add(jLabel97, gridBagConstraints);

        journalDebits.setEditable(false);
        journalDebits.setBorder(null);
        journalDebits.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
        journalDebits.setMaximumSize(new java.awt.Dimension(83, 14));
        journalDebits.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDebits.setName("journalDebits"); // NOI18N
        journalDebits.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel162.add(journalDebits, gridBagConstraints);

        journalCredits.setEditable(false);
        journalCredits.setBorder(null);
        journalCredits.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
        journalCredits.setMinimumSize(new java.awt.Dimension(83, 14));
        journalCredits.setName("journalCredits"); // NOI18N
        journalCredits.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel162.add(journalCredits, gridBagConstraints);

        journalDifferences.setEditable(false);
        journalDifferences.setBorder(null);
        journalDifferences.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
        journalDifferences.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDifferences.setName("journalDifferences"); // NOI18N
        journalDifferences.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel162.add(journalDifferences, gridBagConstraints);

        jPanel144.add(jPanel162);

        jPanel142.add(jPanel144);

        journalDeals.add(jPanel142);

        jideTabbedPane3.addTab(resourceMap.getString("journalDeals.TabConstraints.tabTitle"), journalDeals); // NOI18N

        journalRO.setBackground(resourceMap.getColor("journalRO.background")); // NOI18N
        journalRO.setName("journalRO"); // NOI18N
        journalRO.setOpaque(false);
        journalRO.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                journalROComponentShown(evt);
            }
        });
        journalRO.setLayout(new javax.swing.BoxLayout(journalRO, javax.swing.BoxLayout.Y_AXIS));

        jPanel114.setMaximumSize(new java.awt.Dimension(1000000, 23));
        jPanel114.setName("jPanel114"); // NOI18N
        jPanel114.setOpaque(false);
        jPanel114.setPreferredSize(new java.awt.Dimension(911, 23));
        jPanel114.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel114ComponentShown(evt);
            }
        });
        jPanel114.setLayout(new javax.swing.BoxLayout(jPanel114, javax.swing.BoxLayout.LINE_AXIS));

        jLabel75.setText(resourceMap.getString("jLabel75.text")); // NOI18N
        jLabel75.setName("jLabel75"); // NOI18N
        jPanel114.add(jLabel75);

        buttonGroup2.add(inprogressRadioButton);
        inprogressRadioButton.setSelected(true);
        inprogressRadioButton.setText(resourceMap.getString("inprogressRadioButton.text")); // NOI18N
        inprogressRadioButton.setName("inprogressRadioButton"); // NOI18N
        inprogressRadioButton.setOpaque(false);
        inprogressRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inprogressRadioButtonjournalsTypeRadioButton(evt);
            }
        });
        jPanel114.add(inprogressRadioButton);

        buttonGroup2.add(completedRadioButton);
        completedRadioButton.setText(resourceMap.getString("completedRadioButton.text")); // NOI18N
        completedRadioButton.setName("completedRadioButton"); // NOI18N
        completedRadioButton.setOpaque(false);
        completedRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                completedRadioButtonjournalsTypeRadioButton(evt);
            }
        });
        jPanel114.add(completedRadioButton);

        buttonGroup2.add(closedRadioButton);
        closedRadioButton.setText(resourceMap.getString("closedRadioButton.text")); // NOI18N
        closedRadioButton.setName("closedRadioButton"); // NOI18N
        closedRadioButton.setOpaque(false);
        closedRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closedRadioButtonMouseClicked(evt);
            }
        });
        jPanel114.add(closedRadioButton);

        journalRO.add(jPanel114);

        jPanel115.setMaximumSize(new java.awt.Dimension(32767, 100));
        jPanel115.setMinimumSize(new java.awt.Dimension(911, 100));
        jPanel115.setName("jPanel115"); // NOI18N
        jPanel115.setOpaque(false);
        jPanel115.setPreferredSize(new java.awt.Dimension(911, 150));
        jPanel115.setRequestFocusEnabled(false);
        jPanel115.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel116.setName("jPanel116"); // NOI18N
        jPanel116.setOpaque(false);
        jPanel116.setPreferredSize(new java.awt.Dimension(720, 130));
        jPanel116.setLayout(new javax.swing.BoxLayout(jPanel116, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane26.setName("jScrollPane26"); // NOI18N

        journalROTable.setAutoCreateRowSorter(true);
        journalROTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "RO #", "Open Date", "Closed Date", "Customer", "Total", "LotName"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        journalROTable.setName("journalROTable"); // NOI18N
        journalROTable.setOpaque(false);
        journalROTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalROTableMouseClicked(evt);
            }
        });
        journalROTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                journalROTableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalROTableKeyReleased(evt);
            }
        });
        jScrollPane26.setViewportView(journalROTable);
        journalROTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title2")); // NOI18N
        journalROTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title3")); // NOI18N
        journalROTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title4")); // NOI18N
        journalROTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title5")); // NOI18N

        jPanel116.add(jScrollPane26);

        jPanel115.add(jPanel116);

        jPanel145.setName("jPanel145"); // NOI18N
        jPanel145.setOpaque(false);
        jPanel145.setPreferredSize(new java.awt.Dimension(191, 130));

        jPanel153.setMaximumSize(new java.awt.Dimension(170, 25));
        jPanel153.setMinimumSize(new java.awt.Dimension(170, 25));
        jPanel153.setName("jPanel153"); // NOI18N
        jPanel153.setOpaque(false);

        journalROSearchTextField.setName("journalROSearchTextField"); // NOI18N
        journalROSearchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalROSearchTextFieldKeyReleased(evt);
            }
        });

        journalROSearchButton.setText(resourceMap.getString("journalROSearchButton.text")); // NOI18N
        journalROSearchButton.setName("journalROSearchButton"); // NOI18N
        journalROSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalROSearchButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel153Layout = new javax.swing.GroupLayout(jPanel153);
        jPanel153.setLayout(jPanel153Layout);
        jPanel153Layout.setHorizontalGroup(
            jPanel153Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel153Layout.createSequentialGroup()
                .addComponent(journalROSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(journalROSearchButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel153Layout.setVerticalGroup(
            jPanel153Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel153Layout.createSequentialGroup()
                .addComponent(journalROSearchButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel153Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(journalROSearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel145.add(jPanel153);

        jPanel154.setName("jPanel154"); // NOI18N
        jPanel154.setOpaque(false);
        jPanel154.setPreferredSize(new java.awt.Dimension(191, 100));
        jPanel154.setLayout(new javax.swing.BoxLayout(jPanel154, javax.swing.BoxLayout.LINE_AXIS));

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jLabel86.setText(resourceMap.getString("jLabel86.text")); // NOI18N
        jLabel86.setMaximumSize(new java.awt.Dimension(80, 20));
        jLabel86.setMinimumSize(new java.awt.Dimension(80, 20));
        jLabel86.setName("jLabel86"); // NOI18N
        jLabel86.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel3.add(jLabel86);

        jLabel113.setText(resourceMap.getString("jLabel113.text")); // NOI18N
        jLabel113.setMaximumSize(new java.awt.Dimension(51, 20));
        jLabel113.setMinimumSize(new java.awt.Dimension(51, 20));
        jLabel113.setName("jLabel113"); // NOI18N
        jLabel113.setPreferredSize(new java.awt.Dimension(51, 20));
        jPanel3.add(jLabel113);

        jLabel114.setText(resourceMap.getString("jLabel114.text")); // NOI18N
        jLabel114.setMaximumSize(new java.awt.Dimension(51, 20));
        jLabel114.setMinimumSize(new java.awt.Dimension(51, 20));
        jLabel114.setName("jLabel114"); // NOI18N
        jLabel114.setPreferredSize(new java.awt.Dimension(51, 20));
        jPanel3.add(jLabel114);

        filler3.setName("filler3"); // NOI18N
        jPanel3.add(filler3);

        jPanel154.add(jPanel3);

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        roStartDate.setDateFormatString(resourceMap.getString("roStartDate.dateFormatString")); // NOI18N
        roStartDate.setName("roStartDate"); // NOI18N
        jPanel4.add(roStartDate);

        roEndDate.setDateFormatString(resourceMap.getString("roEndDate.dateFormatString")); // NOI18N
        roEndDate.setName("roEndDate"); // NOI18N
        jPanel4.add(roEndDate);

        jButton23.setText(resourceMap.getString("jButton23.text")); // NOI18N
        jButton23.setName("jButton23"); // NOI18N
        jButton23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton23chartOfAccountsClicked(evt);
            }
        });
        jPanel4.add(jButton23);

        filler2.setName("filler2"); // NOI18N
        jPanel4.add(filler2);

        jPanel154.add(jPanel4);

        jPanel145.add(jPanel154);

        jPanel115.add(jPanel145);

        journalRO.add(jPanel115);

        jPanel155.setMinimumSize(new java.awt.Dimension(911, 500));
        jPanel155.setName("jPanel155"); // NOI18N
        jPanel155.setOpaque(false);
        jPanel155.setPreferredSize(new java.awt.Dimension(911, 450));
        jPanel155.setLayout(new javax.swing.BoxLayout(jPanel155, javax.swing.BoxLayout.LINE_AXIS));

        jPanel156.setName("jPanel156"); // NOI18N
        jPanel156.setPreferredSize(new java.awt.Dimension(720, 420));
        jPanel156.setLayout(new javax.swing.BoxLayout(jPanel156, javax.swing.BoxLayout.LINE_AXIS));

        jPanel65.setName("jPanel65"); // NOI18N
        jPanel65.setPreferredSize(new java.awt.Dimension(452, 420));
        jPanel65.setLayout(new javax.swing.BoxLayout(jPanel65, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane21.setName("jScrollPane21"); // NOI18N

        journalRODetailsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Description", "Debit", "Credit", "Account", "Control #", "Reference #"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        journalRODetailsTable.setName("journalRODetailsTable"); // NOI18N
        journalRODetailsTable.setPreferredSize(new java.awt.Dimension(720, 400));
        jScrollPane21.setViewportView(journalRODetailsTable);
        journalRODetailsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title0")); // NOI18N
        journalRODetailsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title1")); // NOI18N
        journalRODetailsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title2")); // NOI18N
        journalRODetailsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title3")); // NOI18N
        journalRODetailsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title4")); // NOI18N
        journalRODetailsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title5")); // NOI18N

        jPanel65.add(jScrollPane21);

        jPanel156.add(jPanel65);

        jPanel155.add(jPanel156);

        jPanel157.setMaximumSize(new java.awt.Dimension(191, 380));
        jPanel157.setMinimumSize(new java.awt.Dimension(191, 380));
        jPanel157.setName("jPanel157"); // NOI18N
        jPanel157.setOpaque(false);
        jPanel157.setPreferredSize(new java.awt.Dimension(191, 380));

        jPanel93.setName("jPanel93"); // NOI18N
        jPanel93.setOpaque(false);
        jPanel93.setLayout(new java.awt.CardLayout());

        journalReversalDatePanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("journalReversalDatePanel2.border.title"))); // NOI18N
        journalReversalDatePanel2.setMaximumSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel2.setMinimumSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel2.setName("journalReversalDatePanel2"); // NOI18N
        journalReversalDatePanel2.setOpaque(false);
        journalReversalDatePanel2.setPreferredSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel2.setLayout(new javax.swing.BoxLayout(journalReversalDatePanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel164.setMaximumSize(new java.awt.Dimension(191, 50));
        jPanel164.setMinimumSize(new java.awt.Dimension(191, 50));
        jPanel164.setName("jPanel164"); // NOI18N
        jPanel164.setOpaque(false);
        jPanel164.setPreferredSize(new java.awt.Dimension(191, 50));
        jPanel164.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        jPanel165.setName("jPanel165"); // NOI18N
        jPanel165.setOpaque(false);
        jPanel165.setPreferredSize(new java.awt.Dimension(70, 40));
        jPanel165.setLayout(new java.awt.GridLayout(2, 0));

        jLabel103.setFont(resourceMap.getFont("jLabel103.font")); // NOI18N
        jLabel103.setText(resourceMap.getString("jLabel103.text")); // NOI18N
        jLabel103.setName("jLabel103"); // NOI18N
        jLabel103.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel165.add(jLabel103);

        jLabel109.setFont(resourceMap.getFont("jLabel109.font")); // NOI18N
        jLabel109.setText(resourceMap.getString("jLabel109.text")); // NOI18N
        jLabel109.setName("jLabel109"); // NOI18N
        jLabel109.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel165.add(jLabel109);

        jPanel164.add(jPanel165);

        jPanel169.setName("jPanel169"); // NOI18N
        jPanel169.setOpaque(false);
        jPanel169.setPreferredSize(new java.awt.Dimension(100, 40));
        jPanel169.setLayout(new java.awt.GridLayout(2, 1));

        journalDealPostedDate2.setFont(resourceMap.getFont("journalDealPostedDate2.font")); // NOI18N
        journalDealPostedDate2.setName("journalDealPostedDate2"); // NOI18N
        journalDealPostedDate2.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel169.add(journalDealPostedDate2);

        journalReversalDate2.setDateFormatString(resourceMap.getString("journalReversalDate2.dateFormatString")); // NOI18N
        journalReversalDate2.setName("journalReversalDate2"); // NOI18N
        journalReversalDate2.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel169.add(journalReversalDate2);

        jPanel164.add(jPanel169);

        journalReversalDatePanel2.add(jPanel164);

        jPanel109.setName("jPanel109"); // NOI18N
        jPanel109.setOpaque(false);
        jPanel109.setLayout(new java.awt.CardLayout());

        journalsReversalPanel2.setName("journalsReversalPanel2"); // NOI18N
        journalsReversalPanel2.setOpaque(false);
        journalsReversalPanel2.setLayout(new java.awt.GridBagLayout());

        jButton42.setText(resourceMap.getString("jButton42.text")); // NOI18N
        jButton42.setMaximumSize(new java.awt.Dimension(155, 23));
        jButton42.setMinimumSize(new java.awt.Dimension(155, 23));
        jButton42.setName("jButton42"); // NOI18N
        jButton42.setPreferredSize(new java.awt.Dimension(155, 23));
        jButton42.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton42MouseClicked(evt);
            }
        });
        journalsReversalPanel2.add(jButton42, new java.awt.GridBagConstraints());

        jPanel109.add(journalsReversalPanel2, "card2");

        journalReversalDatePanel2.add(jPanel109);

        jPanel93.add(journalReversalDatePanel2, "card3");

        jPanel157.add(jPanel93);

        jPanel166.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel166.border.title"))); // NOI18N
        jPanel166.setMaximumSize(new java.awt.Dimension(190, 120));
        jPanel166.setMinimumSize(new java.awt.Dimension(190, 120));
        jPanel166.setName("jPanel166"); // NOI18N
        jPanel166.setOpaque(false);
        jPanel166.setPreferredSize(new java.awt.Dimension(190, 120));
        jPanel166.setLayout(new java.awt.GridBagLayout());

        jLabel39.setFont(resourceMap.getFont("jLabel39.font")); // NOI18N
        jLabel39.setText(resourceMap.getString("jLabel39.text")); // NOI18N
        jLabel39.setName("jLabel39"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel166.add(jLabel39, gridBagConstraints);

        jLabel104.setFont(resourceMap.getFont("jLabel104.font")); // NOI18N
        jLabel104.setText(resourceMap.getString("jLabel104.text")); // NOI18N
        jLabel104.setName("jLabel104"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel166.add(jLabel104, gridBagConstraints);

        jLabel106.setFont(resourceMap.getFont("jLabel106.font")); // NOI18N
        jLabel106.setText(resourceMap.getString("jLabel106.text")); // NOI18N
        jLabel106.setName("jLabel106"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel166.add(jLabel106, gridBagConstraints);

        jLabel107.setFont(resourceMap.getFont("jLabel107.font")); // NOI18N
        jLabel107.setText(resourceMap.getString("jLabel107.text")); // NOI18N
        jLabel107.setName("jLabel107"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel166.add(jLabel107, gridBagConstraints);

        jLabel110.setFont(resourceMap.getFont("jLabel110.font")); // NOI18N
        jLabel110.setText(resourceMap.getString("jLabel110.text")); // NOI18N
        jLabel110.setName("jLabel110"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel166.add(jLabel110, gridBagConstraints);

        journalSalesCustomer2.setName("journalSalesCustomer2"); // NOI18N
        journalSalesCustomer2.setPreferredSize(new java.awt.Dimension(83, 14));
        jPanel166.add(journalSalesCustomer2, new java.awt.GridBagConstraints());

        journalSalesDeal2.setName("journalSalesDeal2"); // NOI18N
        journalSalesDeal2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel166.add(journalSalesDeal2, gridBagConstraints);

        journalSalesMake2.setName("journalSalesMake2"); // NOI18N
        journalSalesMake2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        jPanel166.add(journalSalesMake2, gridBagConstraints);

        journalSalesYear2.setName("journalSalesYear2"); // NOI18N
        journalSalesYear2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        jPanel166.add(journalSalesYear2, gridBagConstraints);

        journalSalesModel2.setName("journalSalesModel2"); // NOI18N
        journalSalesModel2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        jPanel166.add(journalSalesModel2, gridBagConstraints);

        jLabel51.setFont(resourceMap.getFont("jLabel51.font")); // NOI18N
        jLabel51.setText(resourceMap.getString("jLabel51.text")); // NOI18N
        jLabel51.setName("jLabel51"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel166.add(jLabel51, gridBagConstraints);

        journalSalesCustomerName2.setName("journalSalesCustomerName2"); // NOI18N
        journalSalesCustomerName2.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel166.add(journalSalesCustomerName2, gridBagConstraints);

        jPanel157.add(jPanel166);

        jPanel170.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel170.border.title"))); // NOI18N
        jPanel170.setMaximumSize(new java.awt.Dimension(190, 70));
        jPanel170.setMinimumSize(new java.awt.Dimension(190, 70));
        jPanel170.setName("jPanel170"); // NOI18N
        jPanel170.setOpaque(false);
        jPanel170.setPreferredSize(new java.awt.Dimension(190, 70));
        jPanel170.setLayout(new java.awt.GridBagLayout());

        jLabel40.setFont(resourceMap.getFont("jLabel40.font")); // NOI18N
        jLabel40.setText(resourceMap.getString("jLabel40.text")); // NOI18N
        jLabel40.setName("jLabel40"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel170.add(jLabel40, gridBagConstraints);

        jLabel111.setFont(resourceMap.getFont("jLabel111.font")); // NOI18N
        jLabel111.setText(resourceMap.getString("jLabel111.text")); // NOI18N
        jLabel111.setName("jLabel111"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel170.add(jLabel111, gridBagConstraints);

        jLabel112.setFont(resourceMap.getFont("jLabel112.font")); // NOI18N
        jLabel112.setText(resourceMap.getString("jLabel112.text")); // NOI18N
        jLabel112.setName("jLabel112"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel170.add(jLabel112, gridBagConstraints);

        journalDebits3.setEditable(false);
        journalDebits3.setBorder(null);
        journalDebits3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        journalDebits3.setMaximumSize(new java.awt.Dimension(83, 14));
        journalDebits3.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDebits3.setName("journalDebits3"); // NOI18N
        journalDebits3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel170.add(journalDebits3, gridBagConstraints);

        journalCredits3.setEditable(false);
        journalCredits3.setBorder(null);
        journalCredits3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        journalCredits3.setMinimumSize(new java.awt.Dimension(83, 14));
        journalCredits3.setName("journalCredits3"); // NOI18N
        journalCredits3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel170.add(journalCredits3, gridBagConstraints);

        journalDifferences3.setEditable(false);
        journalDifferences3.setBorder(null);
        journalDifferences3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        journalDifferences3.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDifferences3.setName("journalDifferences3"); // NOI18N
        journalDifferences3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel170.add(journalDifferences3, gridBagConstraints);

        jPanel157.add(jPanel170);

        jPanel158.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel158.setName("jPanel158"); // NOI18N
        jPanel158.setOpaque(false);

        jButton22.setText(resourceMap.getString("jButton22.text")); // NOI18N
        jButton22.setName("jButton22"); // NOI18N
        jButton22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton22chartOfAccountsClicked(evt);
            }
        });
        jPanel158.add(jButton22);

        jPanel157.add(jPanel158);

        jPanel155.add(jPanel157);

        journalRO.add(jPanel155);

        jideTabbedPane3.addTab(resourceMap.getString("journalRO.TabConstraints.tabTitle"), journalRO); // NOI18N

        journalCollections.setBackground(resourceMap.getColor("journalCollections.background")); // NOI18N
        journalCollections.setName("journalCollections"); // NOI18N
        journalCollections.setOpaque(false);
        journalCollections.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                journalCollectionsComponentShown(evt);
            }
        });
        journalCollections.setLayout(new javax.swing.BoxLayout(journalCollections, javax.swing.BoxLayout.Y_AXIS));

        jPanel117.setMaximumSize(new java.awt.Dimension(1000000, 23));
        jPanel117.setName("jPanel117"); // NOI18N
        jPanel117.setOpaque(false);
        jPanel117.setPreferredSize(new java.awt.Dimension(911, 23));
        jPanel117.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel117ComponentShown(evt);
            }
        });
        jPanel117.setLayout(new javax.swing.BoxLayout(jPanel117, javax.swing.BoxLayout.LINE_AXIS));

        jLabel76.setText(resourceMap.getString("jLabel76.text")); // NOI18N
        jLabel76.setName("jLabel76"); // NOI18N
        jPanel117.add(jLabel76);

        buttonGroup3.add(dealsRadioButton);
        dealsRadioButton.setSelected(true);
        dealsRadioButton.setText(resourceMap.getString("dealsRadioButton.text")); // NOI18N
        dealsRadioButton.setName("dealsRadioButton"); // NOI18N
        dealsRadioButton.setOpaque(false);
        dealsRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dealsRadioButtonjournalsTypeRadioButton(evt);
            }
        });
        jPanel117.add(dealsRadioButton);

        buttonGroup3.add(serviceRadioButton);
        serviceRadioButton.setText(resourceMap.getString("serviceRadioButton.text")); // NOI18N
        serviceRadioButton.setName("serviceRadioButton"); // NOI18N
        serviceRadioButton.setOpaque(false);
        serviceRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serviceRadioButtonjournalsTypeRadioButton(evt);
            }
        });
        jPanel117.add(serviceRadioButton);

        buttonGroup3.add(closedRadioButton1);
        closedRadioButton1.setText(resourceMap.getString("closedRadioButton1.text")); // NOI18N
        closedRadioButton1.setName("closedRadioButton1"); // NOI18N
        closedRadioButton1.setOpaque(false);
        closedRadioButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closedRadioButton1MouseClicked(evt);
            }
        });
        jPanel117.add(closedRadioButton1);

        journalCollections.add(jPanel117);

        jPanel118.setMaximumSize(new java.awt.Dimension(32767, 100));
        jPanel118.setMinimumSize(new java.awt.Dimension(911, 100));
        jPanel118.setName("jPanel118"); // NOI18N
        jPanel118.setOpaque(false);
        jPanel118.setPreferredSize(new java.awt.Dimension(911, 150));
        jPanel118.setRequestFocusEnabled(false);
        jPanel118.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel119.setName("jPanel119"); // NOI18N
        jPanel119.setOpaque(false);
        jPanel119.setPreferredSize(new java.awt.Dimension(720, 130));
        jPanel119.setLayout(new javax.swing.BoxLayout(jPanel119, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane27.setName("jScrollPane27"); // NOI18N

        journalCollectionsTable.setAutoCreateRowSorter(true);
        journalCollectionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "RO #", "Open Date", "Closed Date", "Customer", "Total", "LotName"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        journalCollectionsTable.setName("journalCollectionsTable"); // NOI18N
        journalCollectionsTable.setOpaque(false);
        journalCollectionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalCollectionsTableMouseClicked(evt);
            }
        });
        journalCollectionsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                journalCollectionsTableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalCollectionsTableKeyReleased(evt);
            }
        });
        jScrollPane27.setViewportView(journalCollectionsTable);
        journalCollectionsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title2")); // NOI18N
        journalCollectionsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title3")); // NOI18N
        journalCollectionsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title4")); // NOI18N
        journalCollectionsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalROTable.columnModel.title5")); // NOI18N

        jPanel119.add(jScrollPane27);

        jPanel118.add(jPanel119);

        jPanel159.setName("jPanel159"); // NOI18N
        jPanel159.setOpaque(false);
        jPanel159.setPreferredSize(new java.awt.Dimension(191, 130));

        jPanel160.setMaximumSize(new java.awt.Dimension(170, 25));
        jPanel160.setMinimumSize(new java.awt.Dimension(170, 25));
        jPanel160.setName("jPanel160"); // NOI18N
        jPanel160.setOpaque(false);

        journalROSearchTextField1.setName("journalROSearchTextField1"); // NOI18N
        journalROSearchTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                journalROSearchTextField1KeyReleased(evt);
            }
        });

        journalROSearchButton1.setText(resourceMap.getString("journalROSearchButton1.text")); // NOI18N
        journalROSearchButton1.setName("journalROSearchButton1"); // NOI18N
        journalROSearchButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                journalROSearchButton1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel160Layout = new javax.swing.GroupLayout(jPanel160);
        jPanel160.setLayout(jPanel160Layout);
        jPanel160Layout.setHorizontalGroup(
            jPanel160Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel160Layout.createSequentialGroup()
                .addComponent(journalROSearchTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(journalROSearchButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel160Layout.setVerticalGroup(
            jPanel160Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel160Layout.createSequentialGroup()
                .addComponent(journalROSearchButton1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel160Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(journalROSearchTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel159.add(jPanel160);

        jPanel172.setName("jPanel172"); // NOI18N
        jPanel172.setOpaque(false);
        jPanel172.setPreferredSize(new java.awt.Dimension(191, 100));
        jPanel172.setLayout(new javax.swing.BoxLayout(jPanel172, javax.swing.BoxLayout.LINE_AXIS));

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

        jLabel87.setText(resourceMap.getString("jLabel87.text")); // NOI18N
        jLabel87.setMaximumSize(new java.awt.Dimension(80, 20));
        jLabel87.setMinimumSize(new java.awt.Dimension(80, 20));
        jLabel87.setName("jLabel87"); // NOI18N
        jLabel87.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel5.add(jLabel87);

        jLabel115.setText(resourceMap.getString("jLabel115.text")); // NOI18N
        jLabel115.setMaximumSize(new java.awt.Dimension(51, 20));
        jLabel115.setMinimumSize(new java.awt.Dimension(51, 20));
        jLabel115.setName("jLabel115"); // NOI18N
        jLabel115.setPreferredSize(new java.awt.Dimension(51, 20));
        jPanel5.add(jLabel115);

        jLabel116.setText(resourceMap.getString("jLabel116.text")); // NOI18N
        jLabel116.setMaximumSize(new java.awt.Dimension(51, 20));
        jLabel116.setMinimumSize(new java.awt.Dimension(51, 20));
        jLabel116.setName("jLabel116"); // NOI18N
        jLabel116.setPreferredSize(new java.awt.Dimension(51, 20));
        jPanel5.add(jLabel116);

        filler4.setName("filler4"); // NOI18N
        jPanel5.add(filler4);

        jPanel172.add(jPanel5);

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.Y_AXIS));

        roStartDate1.setDateFormatString(resourceMap.getString("roStartDate1.dateFormatString")); // NOI18N
        roStartDate1.setName("roStartDate1"); // NOI18N
        jPanel6.add(roStartDate1);

        roEndDate1.setDateFormatString(resourceMap.getString("roEndDate1.dateFormatString")); // NOI18N
        roEndDate1.setName("roEndDate1"); // NOI18N
        jPanel6.add(roEndDate1);

        jButton24.setText(resourceMap.getString("jButton24.text")); // NOI18N
        jButton24.setName("jButton24"); // NOI18N
        jButton24.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton24chartOfAccountsClicked(evt);
            }
        });
        jPanel6.add(jButton24);

        filler5.setName("filler5"); // NOI18N
        jPanel6.add(filler5);

        jPanel172.add(jPanel6);

        jPanel159.add(jPanel172);

        jPanel118.add(jPanel159);

        journalCollections.add(jPanel118);

        jPanel173.setMinimumSize(new java.awt.Dimension(911, 500));
        jPanel173.setName("jPanel173"); // NOI18N
        jPanel173.setOpaque(false);
        jPanel173.setPreferredSize(new java.awt.Dimension(911, 450));
        jPanel173.setLayout(new javax.swing.BoxLayout(jPanel173, javax.swing.BoxLayout.LINE_AXIS));

        jPanel174.setName("jPanel174"); // NOI18N
        jPanel174.setPreferredSize(new java.awt.Dimension(720, 420));
        jPanel174.setLayout(new javax.swing.BoxLayout(jPanel174, javax.swing.BoxLayout.LINE_AXIS));

        jPanel66.setName("jPanel66"); // NOI18N
        jPanel66.setPreferredSize(new java.awt.Dimension(452, 420));
        jPanel66.setLayout(new javax.swing.BoxLayout(jPanel66, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane22.setName("jScrollPane22"); // NOI18N

        journalCollectionsDetailTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Description", "Debit", "Credit", "Account", "Control #", "Reference #"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        journalCollectionsDetailTable.setName("journalCollectionsDetailTable"); // NOI18N
        journalCollectionsDetailTable.setPreferredSize(new java.awt.Dimension(720, 400));
        jScrollPane22.setViewportView(journalCollectionsDetailTable);
        journalCollectionsDetailTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title0")); // NOI18N
        journalCollectionsDetailTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title1")); // NOI18N
        journalCollectionsDetailTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title2")); // NOI18N
        journalCollectionsDetailTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title3")); // NOI18N
        journalCollectionsDetailTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title4")); // NOI18N
        journalCollectionsDetailTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("journalRODetailsTable.columnModel.title5")); // NOI18N

        jPanel66.add(jScrollPane22);

        jPanel174.add(jPanel66);

        jPanel173.add(jPanel174);

        jPanel175.setMaximumSize(new java.awt.Dimension(191, 380));
        jPanel175.setMinimumSize(new java.awt.Dimension(191, 380));
        jPanel175.setName("jPanel175"); // NOI18N
        jPanel175.setOpaque(false);
        jPanel175.setPreferredSize(new java.awt.Dimension(191, 380));

        jPanel94.setName("jPanel94"); // NOI18N
        jPanel94.setOpaque(false);
        jPanel94.setLayout(new java.awt.CardLayout());

        journalReversalDatePanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("journalReversalDatePanel3.border.title"))); // NOI18N
        journalReversalDatePanel3.setMaximumSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel3.setMinimumSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel3.setName("journalReversalDatePanel3"); // NOI18N
        journalReversalDatePanel3.setOpaque(false);
        journalReversalDatePanel3.setPreferredSize(new java.awt.Dimension(190, 104));
        journalReversalDatePanel3.setLayout(new javax.swing.BoxLayout(journalReversalDatePanel3, javax.swing.BoxLayout.Y_AXIS));

        jPanel176.setMaximumSize(new java.awt.Dimension(191, 50));
        jPanel176.setMinimumSize(new java.awt.Dimension(191, 50));
        jPanel176.setName("jPanel176"); // NOI18N
        jPanel176.setOpaque(false);
        jPanel176.setPreferredSize(new java.awt.Dimension(191, 50));
        jPanel176.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 5));

        jPanel177.setName("jPanel177"); // NOI18N
        jPanel177.setOpaque(false);
        jPanel177.setPreferredSize(new java.awt.Dimension(70, 40));
        jPanel177.setLayout(new java.awt.GridLayout(2, 0));

        jLabel105.setFont(resourceMap.getFont("jLabel105.font")); // NOI18N
        jLabel105.setText(resourceMap.getString("jLabel105.text")); // NOI18N
        jLabel105.setName("jLabel105"); // NOI18N
        jLabel105.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel177.add(jLabel105);

        jLabel117.setFont(resourceMap.getFont("jLabel117.font")); // NOI18N
        jLabel117.setText(resourceMap.getString("jLabel117.text")); // NOI18N
        jLabel117.setName("jLabel117"); // NOI18N
        jLabel117.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel177.add(jLabel117);

        jPanel176.add(jPanel177);

        jPanel178.setName("jPanel178"); // NOI18N
        jPanel178.setOpaque(false);
        jPanel178.setPreferredSize(new java.awt.Dimension(100, 40));
        jPanel178.setLayout(new java.awt.GridLayout(2, 1));

        journalDealPostedDate3.setFont(resourceMap.getFont("journalDealPostedDate3.font")); // NOI18N
        journalDealPostedDate3.setName("journalDealPostedDate3"); // NOI18N
        journalDealPostedDate3.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel178.add(journalDealPostedDate3);

        journalReversalDate3.setDateFormatString(resourceMap.getString("journalReversalDate3.dateFormatString")); // NOI18N
        journalReversalDate3.setName("journalReversalDate3"); // NOI18N
        journalReversalDate3.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel178.add(journalReversalDate3);

        jPanel176.add(jPanel178);

        journalReversalDatePanel3.add(jPanel176);

        jPanel110.setName("jPanel110"); // NOI18N
        jPanel110.setOpaque(false);
        jPanel110.setLayout(new java.awt.CardLayout());

        journalsReversalPanel3.setName("journalsReversalPanel3"); // NOI18N
        journalsReversalPanel3.setOpaque(false);
        journalsReversalPanel3.setLayout(new java.awt.GridBagLayout());

        jButton43.setText(resourceMap.getString("jButton43.text")); // NOI18N
        jButton43.setMaximumSize(new java.awt.Dimension(155, 23));
        jButton43.setMinimumSize(new java.awt.Dimension(155, 23));
        jButton43.setName("jButton43"); // NOI18N
        jButton43.setPreferredSize(new java.awt.Dimension(155, 23));
        jButton43.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton43MouseClicked(evt);
            }
        });
        journalsReversalPanel3.add(jButton43, new java.awt.GridBagConstraints());

        jPanel110.add(journalsReversalPanel3, "card2");

        journalReversalDatePanel3.add(jPanel110);

        jPanel94.add(journalReversalDatePanel3, "card3");

        jPanel175.add(jPanel94);

        jPanel179.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel179.border.title"))); // NOI18N
        jPanel179.setMaximumSize(new java.awt.Dimension(190, 120));
        jPanel179.setMinimumSize(new java.awt.Dimension(190, 120));
        jPanel179.setName("jPanel179"); // NOI18N
        jPanel179.setOpaque(false);
        jPanel179.setPreferredSize(new java.awt.Dimension(190, 120));
        jPanel179.setLayout(new java.awt.GridBagLayout());

        jLabel41.setFont(resourceMap.getFont("jLabel41.font")); // NOI18N
        jLabel41.setText(resourceMap.getString("jLabel41.text")); // NOI18N
        jLabel41.setName("jLabel41"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel179.add(jLabel41, gridBagConstraints);

        jLabel108.setFont(resourceMap.getFont("jLabel108.font")); // NOI18N
        jLabel108.setText(resourceMap.getString("jLabel108.text")); // NOI18N
        jLabel108.setName("jLabel108"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel179.add(jLabel108, gridBagConstraints);

        jLabel118.setFont(resourceMap.getFont("jLabel118.font")); // NOI18N
        jLabel118.setText(resourceMap.getString("jLabel118.text")); // NOI18N
        jLabel118.setName("jLabel118"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel179.add(jLabel118, gridBagConstraints);

        jLabel119.setFont(resourceMap.getFont("jLabel119.font")); // NOI18N
        jLabel119.setText(resourceMap.getString("jLabel119.text")); // NOI18N
        jLabel119.setName("jLabel119"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel179.add(jLabel119, gridBagConstraints);

        jLabel120.setFont(resourceMap.getFont("jLabel120.font")); // NOI18N
        jLabel120.setText(resourceMap.getString("jLabel120.text")); // NOI18N
        jLabel120.setName("jLabel120"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel179.add(jLabel120, gridBagConstraints);

        journalSalesCustomer3.setName("journalSalesCustomer3"); // NOI18N
        journalSalesCustomer3.setPreferredSize(new java.awt.Dimension(83, 14));
        jPanel179.add(journalSalesCustomer3, new java.awt.GridBagConstraints());

        journalSalesDeal3.setName("journalSalesDeal3"); // NOI18N
        journalSalesDeal3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel179.add(journalSalesDeal3, gridBagConstraints);

        journalSalesMake3.setName("journalSalesMake3"); // NOI18N
        journalSalesMake3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        jPanel179.add(journalSalesMake3, gridBagConstraints);

        journalSalesYear3.setName("journalSalesYear3"); // NOI18N
        journalSalesYear3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        jPanel179.add(journalSalesYear3, gridBagConstraints);

        journalSalesModel3.setName("journalSalesModel3"); // NOI18N
        journalSalesModel3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        jPanel179.add(journalSalesModel3, gridBagConstraints);

        jLabel52.setFont(resourceMap.getFont("jLabel52.font")); // NOI18N
        jLabel52.setText(resourceMap.getString("jLabel52.text")); // NOI18N
        jLabel52.setName("jLabel52"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel179.add(jLabel52, gridBagConstraints);

        journalSalesCustomerName3.setName("journalSalesCustomerName3"); // NOI18N
        journalSalesCustomerName3.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel179.add(journalSalesCustomerName3, gridBagConstraints);

        jPanel175.add(jPanel179);

        jPanel180.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel180.border.title"))); // NOI18N
        jPanel180.setMaximumSize(new java.awt.Dimension(190, 70));
        jPanel180.setMinimumSize(new java.awt.Dimension(190, 70));
        jPanel180.setName("jPanel180"); // NOI18N
        jPanel180.setOpaque(false);
        jPanel180.setPreferredSize(new java.awt.Dimension(190, 70));
        jPanel180.setLayout(new java.awt.GridBagLayout());

        jLabel42.setFont(resourceMap.getFont("jLabel42.font")); // NOI18N
        jLabel42.setText(resourceMap.getString("jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel180.add(jLabel42, gridBagConstraints);

        jLabel122.setFont(resourceMap.getFont("jLabel122.font")); // NOI18N
        jLabel122.setText(resourceMap.getString("jLabel122.text")); // NOI18N
        jLabel122.setName("jLabel122"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel180.add(jLabel122, gridBagConstraints);

        jLabel133.setFont(resourceMap.getFont("jLabel133.font")); // NOI18N
        jLabel133.setText(resourceMap.getString("jLabel133.text")); // NOI18N
        jLabel133.setName("jLabel133"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel180.add(jLabel133, gridBagConstraints);

        journalDebits4.setEditable(false);
        journalDebits4.setBorder(null);
        journalDebits4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        journalDebits4.setMaximumSize(new java.awt.Dimension(83, 14));
        journalDebits4.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDebits4.setName("journalDebits4"); // NOI18N
        journalDebits4.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel180.add(journalDebits4, gridBagConstraints);

        journalCredits4.setEditable(false);
        journalCredits4.setBorder(null);
        journalCredits4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        journalCredits4.setMinimumSize(new java.awt.Dimension(83, 14));
        journalCredits4.setName("journalCredits4"); // NOI18N
        journalCredits4.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel180.add(journalCredits4, gridBagConstraints);

        journalDifferences4.setEditable(false);
        journalDifferences4.setBorder(null);
        journalDifferences4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        journalDifferences4.setMinimumSize(new java.awt.Dimension(83, 14));
        journalDifferences4.setName("journalDifferences4"); // NOI18N
        journalDifferences4.setPreferredSize(new java.awt.Dimension(83, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        jPanel180.add(journalDifferences4, gridBagConstraints);

        jPanel175.add(jPanel180);

        jPanel181.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel181.setName("jPanel181"); // NOI18N
        jPanel181.setOpaque(false);

        jButton25.setText(resourceMap.getString("jButton25.text")); // NOI18N
        jButton25.setName("jButton25"); // NOI18N
        jButton25.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton25chartOfAccountsClicked(evt);
            }
        });
        jPanel181.add(jButton25);

        jPanel175.add(jPanel181);

        jPanel173.add(jPanel175);

        journalCollections.add(jPanel173);

        jideTabbedPane3.addTab(resourceMap.getString("journalCollections.TabConstraints.tabTitle"), journalCollections); // NOI18N

        add(jideTabbedPane3, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

        private void journalWholesaleRadiojournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalWholesaleRadiojournalsTypeRadioButton
		reloadJournalDeals();
        }//GEN-LAST:event_journalWholesaleRadiojournalsTypeRadioButton

        private void journalPostedRadiojournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalPostedRadiojournalsTypeRadioButton
		reloadJournalDeals();
        }//GEN-LAST:event_journalPostedRadiojournalsTypeRadioButton

        private void journalDealsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalDealsTableMouseClicked
		try {
			String dealNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Deal #")).toString();
			String stockNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Stock #")).toString();
			String customerNumber;
			String salesperson1Number;
			String salesperson2Number;
			String financeManager;
			String tradeInCode1;
			String tradeInCode2;
			int row = 0;
			int i = 0;
			Double debits = 0.00;
			Double credits = 0.00;
			Double acv1 = 0.00;
			Double acv2 = 0.00;
			Double journalBalance = 0.00;
			String val;
			DefaultTableModel aModel;

			String sql = "SELECT "
				+ "CAST(ROUND(SalesPrice,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(GapAmount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Product1Amount,2) AS NUMERIC(10,2)) + CAST(ROUND(Product2Amount,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product3Amount,2) AS NUMERIC(10,2)) + CAST(ROUND(Product4Amount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Product1Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product2Cost,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product3Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product4Cost,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(SalesTax,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(A.Tags,2) AS NUMERIC(10,2)) + CAST(ROUND(A.Title,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(DealerFee,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(DownPayment,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(BalanceDue,2) AS NUMERIC(10,2)) - CAST(ROUND(Discount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(ReserveAmount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Discount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(TradeAllowance1,2) AS NUMERIC(10,2)) + CAST(ROUND(TradeAllowance2,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Commissions1,2) AS NUMERIC(10,2)) + CAST(ROUND(Commissions2,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(FinanceCommissions,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(FinanceCommissions,2) AS NUMERIC(10,2)), "
				+ "BuyerCode, "
				+ "AccountNumber, "
				+ "CAST(ROUND(B.PurchasedPrice,2) AS NUMERIC(10,2)) AS PurchasePrice, "
				+ "CAST(ROUND(GapCost,2) AS NUMERIC(10,2)), "
				+ "A.StockNumber, Year, Make, Model, "
				+ "CAST(ROUND(Repairs,2) AS NUMERIC(10,2)) AS Repairs, "
				+ "CAST(ROUND(B.Cost,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(ReserveAmount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(GapCost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product1Cost,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product2Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product3Cost,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product4Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(WarrantyCost,2) AS NUMERIC(10,2)) AS InsurancePayable, "
				+ "CAST(ROUND(WarrantyPrice,2) AS NUMERIC(10,2)), CAST(ROUND(WarrantyCost,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(TradeInPayoff,2) AS NUMERIC(10,2)), "
				+ "A.Salesman1, TradeInCode1, TradeInCode2, CAST(ROUND(ACV1,2) AS NUMERIC(10,2)), CAST(ROUND(ACV2,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(TradeAllowance1,2) AS NUMERIC(10,2)), CAST(ROUND(TradeAllowance2,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Commissions1,2) AS NUMERIC(10,2)) AS Commissions1, CAST(ROUND(Commissions2,2) AS NUMERIC(10,2)) AS Commissions2, "
				+ "CAST(ROUND(FinanceCommissions,2) AS NUMERIC(10,2)) AS FinanceCommissions, FinanceManager, Salesman1, Salesman2, PostedDate "
				+ "FROM DealsTable A "
				+ "LEFT JOIN InventoryTable B "
				+ "ON A.StockNumber = B.StockNumber "
				+ "WHERE AccountNumber = '" + dealNumber + "'";
			//System.out.println(sql);

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			aModel = (DefaultTableModel) journalDealsDetailsTable.getModel();
			AccountingUtil.clearTableModel(aModel);

			while (rs.next()) {

				customerNumber = rs.getString(i + 15).toString();
				salesperson1Number = rs.getString(i + 30).toString();
				salesperson2Number = rs.getString("Salesman2").toString();
				financeManager = rs.getString("FinanceManager").toString();

				if (journalWholesaleRadio.isSelected()) {

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 1).toString());
					aModel.addRow(new String[]{"402 - Vehicle Wholesale", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString("PurchasePrice").toString());
					aModel.addRow(new String[]{"600 - COS-Vehicle", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString("Repairs").toString());
					aModel.addRow(new String[]{"603 - COS-Repairs&Transport", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 24).toString());
					aModel.addRow(new String[]{"251 - Vehicle Inventory", "", val, "", stockNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 6).toString());
					aModel.addRow(new String[]{"321 - Tags & Titles", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 8).toString());
					aModel.addRow(new String[]{"322 - Customer Deposit", val, "", "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 9).toString());
					aModel.addRow(new String[]{"211 - Contracts in Transit", val, "", "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					if (!"0.00".equals(rs.getString("Commissions1").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions1").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", val, "", "", salesperson1Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", salesperson1Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("Commissions2").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions2").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", val, "", "", salesperson2Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", salesperson2Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("FinanceCommissions").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("FinanceCommissions").toString());
						aModel.addRow(new String[]{"606 - Sales Management", val, "", "", financeManager, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", financeManager, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					tradeInCode1 = rs.getString(i + 31).toString();
					if (!tradeInCode1.equals("0")) {
						acv1 = Double.parseDouble(rs.getString(i + 33).toString());
						val = AccountingUtil.formatAmountForDisplay(acv1.toString());
						aModel.addRow(new String[]{"251 - Vehicle Inventory", val, "", "", tradeInCode1, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					}

					tradeInCode2 = rs.getString(i + 32).toString();
					if (!tradeInCode2.equals("0")) {
						acv2 = Double.parseDouble(rs.getString(i + 33).toString());
						val = AccountingUtil.formatAmountForDisplay(acv2.toString());
						aModel.addRow(new String[]{"251 - Vehicle Inventory", val, "", "", tradeInCode2, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					}

					Double trade1OverAllowance = Double.parseDouble(rs.getString(i + 35).toString());
					Double val1;
					if (trade1OverAllowance > acv1) {
						val1 = trade1OverAllowance - acv1;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", val, "", "", "", dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					} else if (trade1OverAllowance < acv1) {
						val1 = acv1 - trade1OverAllowance;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", "", val, "", "", dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					Double trade2OverAllowance = Double.parseDouble(rs.getString(i + 36).toString());
					if (trade2OverAllowance > acv2) {
						val1 = trade2OverAllowance - acv2;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", val, "", "", "", dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					} else if (trade2OverAllowance < acv2) {
						val1 = acv2 - trade2OverAllowance;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", "", val, "", "", dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 11).toString());
					aModel.addRow(new String[]{"602 - Discount - Vehicle Sales", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

				} else {
					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 1).toString());
					aModel.addRow(new String[]{"400 - Sales Vehicles", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 17).toString());
					aModel.addRow(new String[]{"600 - COS-Vehicle", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 23).toString());
					aModel.addRow(new String[]{"603 - COS-Repairs&Transport", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 24).toString());
					aModel.addRow(new String[]{"251 - Vehicle Inventory", "", val, "", stockNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 2).toString());
					aModel.addRow(new String[]{"445 - GAP Income", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 18).toString());
					aModel.addRow(new String[]{"648 - COS  - GAP", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 3).toString());
					aModel.addRow(new String[]{"448 - Product Sales", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 4).toString());
					aModel.addRow(new String[]{"649 - COS - Product", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 27).toString());
					aModel.addRow(new String[]{"447 - Warranty Sales", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 28).toString());
					aModel.addRow(new String[]{"647 - COS - Warranty", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 5).toString());
					aModel.addRow(new String[]{"333 - Sales Tax Payable", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 6).toString());
					aModel.addRow(new String[]{"321 - Tags & Titles", "", val, "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 7).toString());
					aModel.addRow(new String[]{"401 - Dealer Fees", "", val, "", customerNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 8).toString());
					aModel.addRow(new String[]{"322 - Customer Deposit", val, "", "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 9).toString());
					aModel.addRow(new String[]{"211 - Contracts in Transit", val, "", "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 25).toString());
					aModel.addRow(new String[]{"443 - Finance Income", "", val, "", dealNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 10).toString());
					aModel.addRow(new String[]{"271 - Finance Income Receivable", val, "", "", dealNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 11).toString());
					aModel.addRow(new String[]{"602 - Discount - Vehicle Sales", val, "", "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					tradeInCode1 = rs.getString(i + 31).toString();
					if (!tradeInCode1.equals("0")) {
						acv1 = Double.parseDouble(rs.getString(i + 33).toString());
						val = AccountingUtil.formatAmountForDisplay(acv1.toString());
						aModel.addRow(new String[]{"251 - Vehicle Inventory", val, "", "", tradeInCode1, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					}

					tradeInCode2 = rs.getString(i + 32).toString();
					if (!tradeInCode2.equals("0")) {
						acv2 = Double.parseDouble(rs.getString(i + 33).toString());
						val = AccountingUtil.formatAmountForDisplay(acv2.toString());
						aModel.addRow(new String[]{"251 - Vehicle Inventory", val, "", "", tradeInCode2, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					}

					Double trade1OverAllowance = Double.parseDouble(rs.getString(i + 35).toString());
					Double val1;
					if (trade1OverAllowance > acv1) {
						val1 = trade1OverAllowance - acv1;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", val, "", "", "", dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					} else if (trade1OverAllowance < acv1) {
						val1 = acv1 - trade1OverAllowance;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", "", val, "", "", dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					Double trade2OverAllowance = Double.parseDouble(rs.getString(i + 36).toString());
					if (trade2OverAllowance > acv2) {
						val1 = trade2OverAllowance - acv2;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", val, "", "", "", dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					} else if (trade2OverAllowance < acv2) {
						val1 = acv2 - trade2OverAllowance;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", "", val, "", "", dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 26).toString());
					aModel.addRow(new String[]{"332 - Insurance Payable", "", val, "", dealNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					if (!"0.00".equals(rs.getString("Commissions1").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions1").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", val, "", "", salesperson1Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", salesperson1Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("Commissions2").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions2").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", val, "", "", salesperson2Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", salesperson2Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("FinanceCommissions").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("FinanceCommissions").toString());
						aModel.addRow(new String[]{"606 - Sales Management", val, "", "", financeManager, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", financeManager, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 29).toString());
					aModel.addRow(new String[]{"320 - Vehicle Lien Payoffs", "", val, "", customerNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

				}

				journalBalance = debits - credits;

				journalDebits.setValue(debits);
				journalCredits.setValue(credits);
				journalDifferences.setValue(journalBalance);

				if (journalBalance < 0.00) {
					journalDifferences.setForeground(Color.red);
				} else {
					journalDifferences.setForeground(Color.black);
				}

				journalSalesCustomer.setText(rs.getString(i + 15).toString());
				journalSalesDeal.setText(rs.getString(i + 16).toString());
				journalSalesStock.setText(rs.getString(i + 19).toString());
				journalSalesYear.setText(rs.getString(i + 20).toString());
				journalSalesMake.setText(rs.getString(i + 21).toString());
				journalSalesModel.setText(rs.getString(i + 22).toString());

				journalSalesCustomerName.setText(journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Customer")).toString());

				if (journalPostedRadio.isSelected()) {
					journalDealPostedDate.setText(rs.getString("PostedDate"));
				} else {
					journalDealDate.setText(journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Date")).toString());
				}

				//System.out.println("PostedDate: "+ rs.getString("PostedDate"));

			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingWindow.class
				.getName()).log(Level.SEVERE, null, ex);
		}

        }//GEN-LAST:event_journalDealsTableMouseClicked

        private void journalDealsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalDealsTableKeyPressed
		//journalDealsTableMouseClicked(null);
        }//GEN-LAST:event_journalDealsTableKeyPressed

        private void journalDealsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalDealsTableKeyReleased
		journalDealsTableMouseClicked(null);
        }//GEN-LAST:event_journalDealsTableKeyReleased

        private void journalSearchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalSearchTextFieldKeyReleased
		if (evt.getKeyCode() == 10) {
			journalSearchButtonMouseClicked(null);
		}
		// TODO add your handling code here:
        }//GEN-LAST:event_journalSearchTextFieldKeyReleased

        private void journalSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalSearchButtonMouseClicked
		try {
			String sql = "";
			String filterDealType = "";

			String searchFilter = "";
			String journalSearch = journalSearchTextField.getText();
			int journalSearchInt = 0;
			String posted = "AND PostedDate IS NULL ";

			posted = "AND PostedDate IS NULL ";

			if (journalRetailRadio.isSelected()) {
				filterDealType = "AND (DealType = 'FINANCE' OR DealType = 'CASH' OR DealType = 'SAME AS CASH' OR DealType = 'BHPH') ";
			} else if (journalWholesaleRadio.isSelected()) {
				filterDealType = "AND DealType = 'WHOLESALE' ";
			} else if (journalPostedRadio.isSelected()) {
				filterDealType = "AND DealType IN ('FINANCE','CASH','SAME AS CASH','BHPH','WHOLESALE') ";
				posted = "AND PostedDate IS NOT NULL ";
			}

			if (dms.util.StringUtil.isInteger(journalSearch)) {
				journalSearchInt = Integer.parseInt(journalSearch);
				searchFilter = "AND AccountNumber = '" + journalSearchInt + "' "
					+ "OR BuyerCode = '" + journalSearchInt + "' "
					+ "OR StockNumber = '" + journalSearchInt + "' ";
			} else if (dms.util.StringUtil.isDate(journalSearch)) {
                            
//                            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(journalSearch);
//                            System.out.println("...........Date is:"+date.toString());
                            
                          DateFormat originalFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                          DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                          Date date = originalFormat.parse(journalSearch);
                          
                          String journalSearchDate = targetFormat.format(date);  // 20120821
                          
                          searchFilter = "AND SoldDate = '" + journalSearchDate + "' ";
                        } else if (!dms.util.StringUtil.isInteger(journalSearch)) {
				searchFilter = "AND (B.FirstName = '" + journalSearch + "' "
					+ "OR B.LastName = '" + journalSearch 
                                        + "' OR A.DealType = '"+journalSearch
                                        + "' OR A.DealFunded = '" + journalSearch
                                        + "' OR A.LotName = '" + journalSearch
                                        + "' OR A.Status = '" + journalSearch
                                        + "') ";
			} 

			sql = "SELECT AccountNumber, SoldDate, B.FirstName + ' ' + B.LastName, StockNumber, DealType, DealFunded, LotName, Status "
				+ "FROM DealsTable A "
				+ "LEFT JOIN CustomerTable B "
				+ "ON A.BuyerCode = B.CustomerCode "
				+ "WHERE Status NOT IN ('Quote') "
				+ posted
				+ filterDealType
				+ searchFilter
				+ "ORDER BY SoldDate DESC";

			//System.out.println("sql: " + sql);

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);
                        
			DefaultTableModel aModel = (DefaultTableModel) journalDealsTable.getModel();
			AccountingUtil.clearTableModel(aModel);

			ResultSetMetaData rsmd = rs.getMetaData();
			int colNo = rsmd.getColumnCount();
                        
			while (rs.next()) {
				Object[] values = new Object[colNo];
				for (int i = 0; i < colNo; i++) {
					if (i == 1) {
						values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
					} else {
						values[i] = rs.getObject(i + 1);
					}
				}
				aModel.addRow(values);
			}
			rs.getStatement().close();

		} catch (Exception e) {
                    e.printStackTrace();
		}
        }//GEN-LAST:event_journalSearchButtonMouseClicked

        private void journalPostButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalPostButtonMouseClicked

		boolean good = true;
		String accountNumber = null;
		String credit = null;
		String debit = null;
		String controlNumber = null; // journalSalesCustomer.getText();
		String referenceNumber = null; //journalSalesDeal.getText();

		/*
		 if (!"0.00".equals(journalDifferences.getText())) {
		 good = false;
		 dms.DMSApp.displayMessage(this, "Posting is out of balance", dms.DMSApp.WARNING_MESSAGE);
		 }
		
		 if (!"-0.00".equals(journalDifferences.getText())) {
		 good = false;
		 dms.DMSApp.displayMessage(this, "Posting2 is out of balance", dms.DMSApp.WARNING_MESSAGE);
		 }
		 */

		if (good) {
			for (int i = 0; i < journalDealsDetailsTable.getRowCount(); i++) {

				accountNumber = null;
				credit = null;
				debit = null;
				//String dealNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Deal #")).toString();
				//String stockNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Stock #")).toString();
				if (journalDealsDetailsTable.getValueAt(i, journalDealsDetailsTable.getColumnModel().getColumnIndex("Description")) != null) {
					accountNumber = journalDealsDetailsTable.getValueAt(i,
						journalDealsDetailsTable.getColumnModel().getColumnIndex("Description")).toString();
					accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
				}
				if (journalDealsDetailsTable.getValueAt(i, 1) != null) {
					credit = journalDealsDetailsTable.getValueAt(i, 1).toString();
				} else {
					credit = "0.00";
				}
				if (journalDealsDetailsTable.getValueAt(i, 2) != null) {
					debit = journalDealsDetailsTable.getValueAt(i, 2).toString();
				} else {
					debit = "0.00";
				}

				if (journalDealsDetailsTable.getValueAt(i, 4) != null) {
					controlNumber = journalDealsDetailsTable.getValueAt(i, 4).toString();
				} else {
					controlNumber = "";
				}

				if (journalDealsDetailsTable.getValueAt(i, 5) != null) {
					referenceNumber = journalDealsDetailsTable.getValueAt(i, 5).toString();
				} else {
					referenceNumber = "";
				}
				//System.out.println(debit + "" + credit);
				// Dont insert if its 0
				//   if (credit != "0.00" && debit != "0.00")
				{
					sql = new String[1];

					if ("605 ".equals(accountNumber) || "334 ".equals(accountNumber)) {

						//String[] fullName = controlNumber.trim.split(" +", 2);
						String[] fullName = controlNumber.split("\\s+");

						if (fullName.length == 2) {
							//System.out.println("Size " + fullName.length);
							//System.out.println("First: " + fullName[0] + "Last: " + fullName[1]);
							controlNumber = AccountingUtil.getEmployeeID(fullName[0], fullName[1]).toString();
							//System.out.println(controlNumber);
						} else if (fullName.length == 3) {
							//System.out.println("Size " + fullName.length);
							//System.out.println("First: " + fullName[0] + " " + fullName[1] + "Last: " + fullName[2]);
							// NOTE: FIX URGENTLY!!!
							if (fullName[0].equals("CARTANO")) {
								controlNumber = "602";
							} else {
								controlNumber = AccountingUtil.getEmployeeID(fullName[0] + ' ' + fullName[1], fullName[2]).toString();
							}
							//System.out.println(controlNumber);
						}
					}

					sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, LotName) VALUES("
						+ "'" + accountNumber + "', '" + credit + "', '" + debit + "', '" + controlNumber + "', '" + referenceNumber + "', "
						+ "'" + AccountingUtil.dateFormat.format(journalPostDate.getDate()) + "', 'Retail Sales Journal', "
						+ "'" + dms.DMSApp.getApplication().getCurrentlotName() + "'"
						+ ")";

					dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
					//System.out.println(sql[0]);
				}
			}

			sql[0] = "UPDATE DealsTable SET PostedDate = '"
				+ AccountingUtil.dateFormat.format(journalPostDate.getDate())
				+ "' WHERE AccountNumber = '" + referenceNumber + "'";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

			DefaultTableModel aModel = (DefaultTableModel) journalDealsDetailsTable.getModel();
			AccountingUtil.clearTableModel(aModel);
			reloadJournalDeals();
			COAPanel.reloadCOA2();

		}
        }//GEN-LAST:event_journalPostButtonMouseClicked

        private void jButton40MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton40MouseClicked

		/*
		 String accountNumber = null;
		 String credit = null;
		 String debit = null;
		 String controlNumber = journalSalesCustomer.getText();
		 String referenceNumber = journalSalesDeal.getText();

		 for (int i = 0; i <= journalDealsDetailsTable.getRowCount(); i++) {

		 // needs some work
		 if (i == 19) {
		 break;
		 }

		 if (journalDealsDetailsTable.getValueAt(i, 3) == null) {
		 continue;
		 }

		 accountNumber = null;
		 credit = null;
		 debit = null;

		 if (journalDealsDetailsTable.getValueAt(i, 3) != null) {
		 accountNumber = journalDealsDetailsTable.getValueAt(i, 3).toString();
		 accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
		 }
		 if (journalDealsDetailsTable.getValueAt(i, 1) != null) {
		 credit = journalDealsDetailsTable.getValueAt(i, 1).toString();
		 } else {
		 credit = "0.00";
		 }
		 if (journalDealsDetailsTable.getValueAt(i, 2) != null) {
		 debit = journalDealsDetailsTable.getValueAt(i, 2).toString();
		 } else {
		 debit = "0.00";
		 }
		 //System.out.println(debit + "" + credit);
		 // Dont insert if its 0
		 //   if (credit != "0.00" && debit != "0.00")
		 {
		 sql = new String[1];

		 sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Credit, Debit, ControlNumber, ReferenceNumber, PostDate, GLType, LotName) VALUES("
		 + "'" + accountNumber + "', '" + credit + "', '" + debit + "', '" + controlNumber + "', '" + referenceNumber + "', "
		 + "'" + dateFormat.format(journalPostDate.getDate()) + "', 'Retail Reversal Sales Journal', "
		 + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "'"
		 + ")";

		 dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
		 }

		 }

		 sql[0] = "UPDATE DealsTable SET PostedDate = null, Status = 'Delete' WHERE AccountNumber = '" + referenceNumber + "'";

		 dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
		 reloadJournalDeals();

		 */
		try {
			String dealNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Deal #")).toString();
			String stockNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Stock #")).toString();

			String customerNumber;
			String salesperson1Number;
			String salesperson2Number;
			String financeManager;
			String tradeInCode1;
			String tradeInCode2;
			int row = 0;
			int i = 0;
			Double debits = 0.00;
			Double credits = 0.00;
			Double acv1 = 0.00;
			Double acv2 = 0.00;
			Double journalBalance = 0.00;
			String val;
			DefaultTableModel aModel;

			String sql = "SELECT "
				+ "CAST(ROUND(SalesPrice,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(GapAmount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Product1Amount,2) AS NUMERIC(10,2)) + CAST(ROUND(Product2Amount,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product3Amount,2) AS NUMERIC(10,2)) + CAST(ROUND(Product4Amount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Product1Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product2Cost,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product3Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product4Cost,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(SalesTax,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(A.Tags,2) AS NUMERIC(10,2)) + CAST(ROUND(A.Title,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(DealerFee,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(DownPayment,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(BalanceDue,2) AS NUMERIC(10,2)) - CAST(ROUND(Discount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(ReserveAmount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Discount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(TradeAllowance1,2) AS NUMERIC(10,2)) + CAST(ROUND(TradeAllowance2,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Commissions1,2) AS NUMERIC(10,2)) + CAST(ROUND(Commissions2,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(FinanceCommissions,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(FinanceCommissions,2) AS NUMERIC(10,2)), "
				+ "BuyerCode, "
				+ "AccountNumber, "
				+ "CAST(ROUND(B.PurchasedPrice,2) AS NUMERIC(10,2)) AS PurchasePrice, "
				+ "CAST(ROUND(GapCost,2) AS NUMERIC(10,2)), "
				+ "A.StockNumber, Year, Make, Model, "
				+ "CAST(ROUND(Repairs,2) AS NUMERIC(10,2)) AS Repairs, "
				+ "CAST(ROUND(B.Cost,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(ReserveAmount,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(GapCost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product1Cost,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product2Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(Product3Cost,2) AS NUMERIC(10,2)) + "
				+ "CAST(ROUND(Product4Cost,2) AS NUMERIC(10,2)) + CAST(ROUND(WarrantyCost,2) AS NUMERIC(10,2)) AS InsurancePayable, "
				+ "CAST(ROUND(WarrantyPrice,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(WarrantyCost,2) AS NUMERIC(10,2)), CAST(ROUND(TradeInPayoff,2) AS NUMERIC(10,2)), "
				+ "A.Salesman1, TradeInCode1, TradeInCode2, CAST(ROUND(ACV1,2) AS NUMERIC(10,2)), CAST(ROUND(ACV2,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(TradeAllowance1,2) AS NUMERIC(10,2)), CAST(ROUND(TradeAllowance2,2) AS NUMERIC(10,2)), "
				+ "CAST(ROUND(Commissions1,2) AS NUMERIC(10,2)) AS Commissions1, CAST(ROUND(Commissions2,2) AS NUMERIC(10,2)) AS Commissions2, "
				+ "CAST(ROUND(FinanceCommissions,2) AS NUMERIC(10,2)) AS FinanceCommissions, FinanceManager, Salesman1, Salesman2, PostedDate "
				+ "FROM DealsTable A "
				+ "LEFT JOIN InventoryTable B "
				+ "ON A.StockNumber = B.StockNumber "
				+ "WHERE AccountNumber = '" + dealNumber + "'";

			dealNumber = dealNumber + "R";

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);
			ResultSetMetaData rsmd = rs.getMetaData();

			aModel = (DefaultTableModel) journalReversalTable.getModel();
			AccountingUtil.clearTableModel(aModel);


			while (rs.next()) {

				customerNumber = rs.getString(i + 15).toString();
				salesperson1Number = rs.getString(i + 30).toString();
				salesperson2Number = rs.getString("Salesman2").toString();
				financeManager = rs.getString("FinanceManager").toString();

				if (journalWholesaleRadio.isSelected()) {

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 1).toString());
					aModel.addRow(new String[]{"402 - Vehicle Wholesale", "", "", val, "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString("PurchasePrice").toString());
					aModel.addRow(new String[]{"600 - COS-Vehicle", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString("Repairs").toString());
					aModel.addRow(new String[]{"603 - COS-Repairs&Transport", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 24).toString());
					aModel.addRow(new String[]{"251 - Vehicle Inventory", "", "", val, stockNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 6).toString());
					aModel.addRow(new String[]{"321 - Tags & Titles", "", "", val, "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 8).toString());
					aModel.addRow(new String[]{"322 - Customer Deposit", "", val, "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 9).toString());
					aModel.addRow(new String[]{"211 - Contracts in Transit", "", val, "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					if (!"0.00".equals(rs.getString("Commissions1").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions1").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", "", val, "", salesperson1Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", val, "", "", salesperson1Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("Commissions2").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions2").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", "", val, "", salesperson2Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", val, "", "", salesperson2Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("FinanceCommissions").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("FinanceCommissions").toString());
						aModel.addRow(new String[]{"606 - Sales Management", "", val, "", financeManager, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", val, "", "", financeManager, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 11).toString());
					aModel.addRow(new String[]{"602 - Discount - Vehicle Sales", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

				} else {

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 1).toString());
					aModel.addRow(new String[]{"400 - Sales Vehicles", val, "", "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 17).toString());
					aModel.addRow(new String[]{"600 - COS-Vehicle", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 23).toString());
					aModel.addRow(new String[]{"603 - COS-Repairs&Transport", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 24).toString());
					aModel.addRow(new String[]{"251 - Vehicle Inventory", val, "", "", stockNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 2).toString());
					aModel.addRow(new String[]{"445 - GAP Income", val, "", "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 18).toString());
					aModel.addRow(new String[]{"648 - COS  - GAP", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 3).toString());
					aModel.addRow(new String[]{"448 - Product Sales", val, "", "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 4).toString());
					aModel.addRow(new String[]{"649 - COS - Product", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 27).toString());
					aModel.addRow(new String[]{"447 - Warranty Sales", val, "", "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 28).toString());
					aModel.addRow(new String[]{"647 - COS - Warranty", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 5).toString());
					aModel.addRow(new String[]{"333 - Sales Tax Payable", val, "", "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 6).toString());
					aModel.addRow(new String[]{"321 - Tags & Titles", val, "", "", "", dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 7).toString());
					aModel.addRow(new String[]{"401 - Dealer Fees", val, "", "", customerNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 8).toString());
					aModel.addRow(new String[]{"322 - Customer Deposit", "", val, "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 9).toString());
					aModel.addRow(new String[]{"211 - Contracts in Transit", "", val, "", customerNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 25).toString());
					aModel.addRow(new String[]{"443 - Finance Income", val, "", "", dealNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 10).toString());
					aModel.addRow(new String[]{"271 - Finance Income Receivable", "", val, "", dealNumber, dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 11).toString());
					aModel.addRow(new String[]{"602 - Discount - Vehicle Sales", "", val, "", "", dealNumber});
					debits += Double.parseDouble(val.replace(",", ""));

					tradeInCode1 = rs.getString(i + 31).toString();
					if (!tradeInCode1.equals("0")) {
						acv1 = Double.parseDouble(rs.getString(i + 33).toString());
						val = AccountingUtil.formatAmountForDisplay(acv1.toString());
						aModel.addRow(new String[]{"251 - Vehicle Inventory", "", val, "", tradeInCode1, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					}

					tradeInCode2 = rs.getString(i + 32).toString();
					if (!tradeInCode2.equals("0")) {
						acv2 = Double.parseDouble(rs.getString(i + 33).toString());
						val = AccountingUtil.formatAmountForDisplay(acv2.toString());
						aModel.addRow(new String[]{"251 - Vehicle Inventory", "", val, "", tradeInCode2, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					}

					Double trade1OverAllowance = Double.parseDouble(rs.getString(i + 35).toString());
					Double val1;
					if (trade1OverAllowance > acv1) {
						val1 = trade1OverAllowance - acv1;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", "", val, "", "", dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					} else if (trade1OverAllowance < acv1) {
						val1 = acv1 - trade1OverAllowance;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", val, "", "", "", dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					Double trade2OverAllowance = Double.parseDouble(rs.getString(i + 36).toString());
					if (trade2OverAllowance > acv2) {
						val1 = trade2OverAllowance - acv2;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", "", val, "", "", dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));
					} else if (trade2OverAllowance < acv2) {
						val1 = acv2 - trade2OverAllowance;
						val = AccountingUtil.formatAmountForDisplay(val1.toString());
						aModel.addRow(new String[]{"608 - Trade-In Over Allowance", val, "", "", "", dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 26).toString());
					aModel.addRow(new String[]{"332 - Insurance Payable", val, "", "", dealNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));

					if (!"0.00".equals(rs.getString("Commissions1").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions1").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", "", val, "", salesperson1Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", val, "", "", salesperson1Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("Commissions2").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("Commissions2").toString());
						aModel.addRow(new String[]{"605 - Sales Commissions", val, "", "", salesperson2Number, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", "", val, "", salesperson2Number, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					if (!"0.00".equals(rs.getString("FinanceCommissions").toString())) {
						val = AccountingUtil.formatAmountForDisplay(rs.getString("FinanceCommissions").toString());
						aModel.addRow(new String[]{"606 - Sales Management", "", val, "", financeManager, dealNumber});
						debits += Double.parseDouble(val.replace(",", ""));

						aModel.addRow(new String[]{"334 - Commissions", val, "", "", financeManager, dealNumber});
						credits += Double.parseDouble(val.replace(",", ""));
					}

					val = AccountingUtil.formatAmountForDisplay(rs.getString(i + 29).toString());
					aModel.addRow(new String[]{"320 - Vehicle Lien Payoffs", val, "", "", customerNumber, dealNumber});
					credits += Double.parseDouble(val.replace(",", ""));
				}

				journalBalance = debits - credits;

                              debits11.setValue(debits);
                              credits11.setValue(credits);
                              difference11.setValue(journalBalance);
                              
				if (journalBalance < 0.00) {
					journalDifferences.setForeground(Color.red);
				} else {
					journalDifferences.setForeground(Color.black);
				}

				journalSalesCustomer1.setText(rs.getString(i + 15).toString());
				journalSalesDeal1.setText(rs.getString(i + 16).toString());
				journalSalesStock1.setText(rs.getString(i + 19).toString());
				journalSalesYear1.setText(rs.getString(i + 20).toString());
				journalSalesMake1.setText(rs.getString(i + 21).toString());
				journalSalesModel1.setText(rs.getString(i + 22).toString());

				journalSalesCustomerName1.setText(journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Customer")).toString());

				if (journalPostedRadio.isSelected()) {
					journalDealPostedDate.setText(rs.getString("PostedDate"));
				} else {
					journalDealDate.setText(journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Date")).toString());
				}

			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingWindow.class.getName()).log(Level.SEVERE, null, ex);
		}

		dms.DMSApp.getApplication().show(reversalPopup);
        }//GEN-LAST:event_jButton40MouseClicked

        private void journalDealsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_journalDealsComponentShown
		Calendar c = Calendar.getInstance();

		if (journalPostDate.getDate() == null) {
			journalPostDate.setDate(c.getTime());
		}
        }//GEN-LAST:event_journalDealsComponentShown

        private void journalPostReversalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalPostReversalMouseClicked

		boolean good = true;
		String accountNumber = null;
		String credit = null;
		String debit = null;
		String controlNumber = null; // journalSalesCustomer.getText();
		String referenceNumber = null; //journalSalesDeal.getText();

		/*
		 if (!"0.00".equals(journalDifferences.getText())) {
		 good = false;
		 dms.DMSApp.displayMessage(this, "Posting is out of balance", dms.DMSApp.WARNING_MESSAGE);
		 }
		
		 if (!"-0.00".equals(journalDifferences.getText())) {
		 good = false;
		 dms.DMSApp.displayMessage(this, "Posting2 is out of balance", dms.DMSApp.WARNING_MESSAGE);
		 }
		 */

		if (good) {
			for (int i = 0; i < journalReversalTable.getRowCount(); i++) {

				accountNumber = null;
				credit = null;
				debit = null;
				//String dealNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Deal #")).toString();
				//String stockNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Stock #")).toString();
				if (journalReversalTable.getValueAt(i, journalReversalTable.getColumnModel().getColumnIndex("Description")) != null) {
					accountNumber = journalReversalTable.getValueAt(i,
						journalReversalTable.getColumnModel().getColumnIndex("Description")).toString();
					accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
				}
				if (journalReversalTable.getValueAt(i, 1) != null) {
					credit = journalReversalTable.getValueAt(i, 1).toString();
				} else {
					credit = "0.00";
				}
				if (journalReversalTable.getValueAt(i, 2) != null) {
					debit = journalReversalTable.getValueAt(i, 2).toString();
				} else {
					debit = "0.00";
				}

				if (journalReversalTable.getValueAt(i, 4) != null) {
					controlNumber = journalReversalTable.getValueAt(i, 4).toString();
				} else {
					controlNumber = "";
				}

				if (journalReversalTable.getValueAt(i, 5) != null) {
					referenceNumber = journalReversalTable.getValueAt(i, 5).toString();
					referenceNumber = referenceNumber + "-R";
				} else {
					referenceNumber = "";
				}
				//System.out.println(debit + "" + credit);
				// Dont insert if its 0
				//   if (credit != "0.00" && debit != "0.00")
				{
					sql = new String[1];

					if ("605 ".equals(accountNumber) || "334 ".equals(accountNumber)) {

						//String[] fullName = controlNumber.trim.split(" +", 2);
						String[] fullName = controlNumber.split("\\s+");

						if (fullName.length == 2) {
							//System.out.println("Size " + fullName.length);
							//System.out.println("First: " + fullName[0] + "Last: " + fullName[1]);
							controlNumber = AccountingUtil.getEmployeeID(fullName[0], fullName[1]).toString();
							//System.out.println(controlNumber);
						} else if (fullName.length == 3) {
							//System.out.println("Size " + fullName.length);
							//System.out.println("First: " + fullName[0] + " " + fullName[1] + "Last: " + fullName[2]);
							// NOTE: FIX URGENTLY!!!
							if (fullName[0].equals("CARTANO")) {
								controlNumber = "602";
							} else {
								controlNumber = AccountingUtil.getEmployeeID(fullName[0] + ' ' + fullName[1], fullName[2]).toString();
							}
							//System.out.println(controlNumber);
						}
					}

					sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, LotName, Memo, Class) VALUES("
						+ "'" + accountNumber + "', '" + credit + "', '" + debit + "', '" + controlNumber + "', '" + referenceNumber + "', "
						+ "'" + AccountingUtil.dateFormat.format(journalReversalDate.getDate()) + "', 'Deals Reversal Journal', "
						+ "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
						+ "'Reversal',''"
						+ ")";

					dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
					//System.out.println(sql[0]);
				}
			}

			sql[0] = "UPDATE DealsTable SET PostedDate = '"
				+ AccountingUtil.dateFormat.format(journalReversalDate.getDate())
				+ "', Status = 'Reversed' WHERE AccountNumber = '" + referenceNumber.replace("-R", "") + "'";

			dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
			//System.out.println(sql[0]);

			DefaultTableModel aModel = (DefaultTableModel) journalReversalTable.getModel();
			AccountingUtil.clearTableModel(aModel);
			reloadJournalDeals();
			COAPanel.reloadCOA2();
			//reversalPopup.dispose();
		}

        }//GEN-LAST:event_journalPostReversalMouseClicked

        private void jButton41MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton41MouseClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_jButton41MouseClicked

        private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked

		if (journalPostedRadio.isSelected() && journalDealsTable.getSelectedRowCount() == 0) {
			dms.DMSApp.displayMessage(journalDealsTable, "Please select a record to delete", JOptionPane.ERROR_MESSAGE);
			return;
		}
                boolean sureToDelete = false;
		if (journalPostedRadio.isSelected() && journalDealsTable.getSelectedRowCount() == 1) {
                    int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    sureToDelete = false;
                } else if (response == JOptionPane.YES_OPTION) {
                    sureToDelete = true;
                } else if (response == JOptionPane.CLOSED_OPTION) {
                    //System.out.println("JOptionPane closed");
                }
                if(sureToDelete){
			String referenceNumber = journalDealsTable.getValueAt(journalDealsTable.getSelectedRow(), journalDealsTable.getColumnModel().getColumnIndex("Deal #")).toString();
			sql = new String[2];
			sql[0] = "DELETE FROM AccountingGLTable WHERE GLType = 'Retail Sales Journal' "
				+ "AND ReferenceNumber = '" + referenceNumber + "'";
			sql[1] = "UPDATE DealsTable SET PostedDate = NULL WHERE AccountNumber = '" + referenceNumber + "'";

			try {
				dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
				reloadJournalDeals();
			} catch (Exception e) {
				dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
			}
                }else
                    return;
		}

        }//GEN-LAST:event_jButton1MouseClicked

        private void journalPostingTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalPostingTableMouseExited
        }//GEN-LAST:event_journalPostingTableMouseExited

        private void journalPostingTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalPostingTableKeyReleased
		/*if (evt.getKeyCode() == 10) {
		 DefaultTableModel aModel = (DefaultTableModel) journalPostingTable.getModel();
		 int rowCount = journalPostingTable.getRowCount();
		 if(isRowRemoved){                                
		 rowNo = rowCount - 1;
		 isRowRemoved = false;
		 }
		 if(journalPostingTable.getValueAt(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Account")) != null){
		 account = journalPostingTable.getValueAt(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Account")).toString();
		 }
		 if(journalPostingTable.getValueAt(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Debits")) != null){
		 debitAmount = journalPostingTable.getValueAt(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Debits")).toString();
		 }
		 if(journalPostingTable.getValueAt(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Credits")) != null){
		 creditAmount = journalPostingTable.getValueAt(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Credits")).toString();
		 }
		 if (account == null) {
		 dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.ERROR_MESSAGE);
		 journalPostingTable.setCellSelectionEnabled(true);
		 journalPostingTable.changeSelection(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Account"), false, false);
		 journalPostingTable.requestFocus();
		 }
		 if (debitAmount == null || debitAmount.isEmpty()) {
		 dms.DMSApp.displayMessage(this, "Please enter debit amount.", JOptionPane.ERROR_MESSAGE);
		 journalPostingTable.setCellSelectionEnabled(true);
		 journalPostingTable.changeSelection(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Debits"), false, false);
		 journalPostingTable.requestFocus();
		 } else if (debitAmount != null) {
		 if(!displayNumeric(debitAmount)){
		 //System.out.println("Amount is not Numeric");
		 dms.DMSApp.displayMessage(this, "Please enter numbers only for debit amount.", JOptionPane.ERROR_MESSAGE);
		 journalPostingTable.setCellSelectionEnabled(true);
		 journalPostingTable.changeSelection(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Debits"), false, false);
		 journalPostingTable.requestFocus();
		 }
		 }
		 if (creditAmount == null || creditAmount.isEmpty()) {
		 dms.DMSApp.displayMessage(this, "Please enter credit amount.", JOptionPane.ERROR_MESSAGE);
		 journalPostingTable.setCellSelectionEnabled(true);
		 journalPostingTable.changeSelection(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Credits"), false, false);
		 journalPostingTable.requestFocus();
		 } else if (creditAmount != null) {
		 if(!displayNumeric(creditAmount)){
		 //System.out.println("Amount is not Numeric");
		 dms.DMSApp.displayMessage(this, "Please enter numbers only for credit amount.", JOptionPane.ERROR_MESSAGE);
		 journalPostingTable.setCellSelectionEnabled(true);
		 journalPostingTable.changeSelection(rowNo, journalPostingTable.getColumnModel().getColumnIndex("Credits"), false, false);
		 journalPostingTable.requestFocus();
		 }
		 }
		 // check to insert a row
		 if(account != null && debitAmount != null && displayNumeric(debitAmount) && creditAmount != null && displayNumeric(creditAmount)){
		 addNewRowJournal(aModel,account);
		 rowNo ++;
		 }
		 }
		 if (evt.isControlDown() && evt.getKeyCode() == 83) {
		 jButton46enterBillsButtonsClicked(null);
		 }*/
        }//GEN-LAST:event_journalPostingTableKeyReleased

	private void addNewRowJournal(DefaultTableModel tableModel, String account) {
		String accTypeDesc = account;
		tableModel.addRow(new Object[]{
			//null, "0.00", "0.00", "", ""
			accTypeDesc, "", "", "", ""
		});
	}

        private void billInvoiceInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_billInvoiceInputMethodTextChanged
        }//GEN-LAST:event_billInvoiceInputMethodTextChanged

        private void jButton46enterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton46enterBillsButtonsClicked
		boolean good = true;
		int journalPostingRowCount = journalPostingTable.getRowCount();

		if (billInvoice.getText() == null || "".equals(billInvoice.getText())) {
			good = false;
			dms.DMSApp.displayMessage(this, "Reference number is required", dms.DMSApp.WARNING_MESSAGE);
		}
		//System.out.println("test = " + journalDifferences2.getValue());
		if (!journalDifferences2.getValue().toString().equals("0.0")) {
			//System.out.println("if is true..");
			good = false;
			dms.DMSApp.displayMessage(this, "Posting is out of balance", dms.DMSApp.WARNING_MESSAGE);
			return;
		}
		if (journalPostingRowCount > 0) {

			int accountColumn = AccountingUtil.getColumnByName(journalPostingTable, "Account");
			int controlColumn = AccountingUtil.getColumnByName(journalPostingTable, "Control #");

			for (int i = 0; i <= journalPostingTable.getRowCount() - 1; i++) {
				String accountNumber = null;
				accountNumber = null;

				if (journalPostingTable.getValueAt(i, accountColumn) != null) {
					accountNumber = journalPostingTable.getValueAt(i, accountColumn).toString();
					accountNumber = accountNumber.split("-")[0];
				}

				Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
				//System.out.println("isControlled : " + isControlled);
				if (isControlled && journalPostingTable.getValueAt(i, controlColumn) == null) {
					good = false;
					dms.DMSApp.displayMessage(this,
						"Please provide Control # value for Row." + ++i, JOptionPane.ERROR_MESSAGE);
				} else if (isControlled
					&& journalPostingTable.getValueAt(i, controlColumn) != null
					&& journalPostingTable.getValueAt(i, controlColumn).toString().isEmpty()) {
					good = false;
					dms.DMSApp.displayMessage(this,
						"Please provide Control # value for Row " + ++i, JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

		} else {
			good = false;
		}

		if (good) {
			String accountNumber = null;
			Double debits = null;
			Double credits = null;
			String controlNumber = null; // journalSalesCustomer.getText();
			String referenceNumber = null; //journalSalesDeal.getText();
			String memo = "";

			accountNumber = "";
			referenceNumber = removeSpecialCharacters(billInvoice.getText().toString());
			memo = removeSpecialCharacters(billMemo.getText().toString());

			sql = new String[1];

			/*

			 sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, "
			 + "PostDate, DateDue, Memo, GLType, LotName) VALUES('" + accountNumber + "', '0.00', '" + amount
			 + "', '" + AccountingUtil.getVendorID(controlNumber) + "', '" + referenceNumber + "', '"
			 + AccountingUtil.dateFormat.format(billPostDate.getDate()) + "', '"
			 + memo + "', 'Vendor Invoice', "
			 + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "'"
			 + ")";

			 dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
			 */

			int accountColumn = AccountingUtil.getColumnByName(journalPostingTable, "Account");
			int debitsColumn = AccountingUtil.getColumnByName(journalPostingTable, "Debits");
			int creditsColumn = AccountingUtil.getColumnByName(journalPostingTable, "Credits");
			int controlColumn = AccountingUtil.getColumnByName(journalPostingTable, "Control #");
			int memoColumn = AccountingUtil.getColumnByName(journalPostingTable, "Memo");

			for (int i = 0; i <= journalPostingTable.getRowCount() - 1; i++) {
				accountNumber = null;
				debits = null;
				credits = null;
				controlNumber = null;
				memo = null;

				if (journalPostingTable.getValueAt(i, accountColumn) != null) {
					accountNumber = journalPostingTable.getValueAt(i, accountColumn).toString();
					accountNumber = accountNumber.split("-")[0];
					//System.out.println("accountNumber :: " + accountNumber);
					Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
					//System.out.println("isControlled : " + isControlled);
					if (isControlled && journalPostingTable.getValueAt(i, controlColumn) == null) {
						dms.DMSApp.displayMessage(this, "Please provide Control # value for Row." + ++i, JOptionPane.ERROR_MESSAGE);
					} else if (isControlled && journalPostingTable.getValueAt(i, controlColumn) != null && journalPostingTable.getValueAt(i, controlColumn).toString().isEmpty()) {
						dms.DMSApp.displayMessage(this, "Please provide Control # value for Row " + ++i, JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				Object debitVal = journalPostingTable.getValueAt(i, debitsColumn);
				if (debitVal != null && !debitVal.toString().isEmpty()) {
					String debitValString = debitVal.toString();
					if (debitValString.contains(",")) {
						debitValString = debitValString.replaceAll(",", "");
					}
					debits = Double.parseDouble(debitValString);
				} else {
					debits = 0.00;
				}
				Object creditVal = journalPostingTable.getValueAt(i, creditsColumn);
				if (creditVal != null && !creditVal.toString().isEmpty()) {
					String creditValString = creditVal.toString();
					if (creditValString.contains(",")) {
						creditValString = creditValString.replaceAll(",", "");
					}
					credits = Double.parseDouble(creditValString);
				} else {
					credits = 0.00;
				}
				if (debits > 0.00 && credits > 0.00) {
					dms.DMSApp.displayMessage(this, "Please enter value for either Debits or Credits, not both (Check row " + ++i + ")", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (debits == 0.00 && credits == 0.00) {
					dms.DMSApp.displayMessage(this, "Please enter value for at least one field, debit or credit.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (journalPostingTable.getValueAt(i, controlColumn) != null) {
					controlNumber = journalPostingTable.getValueAt(i, controlColumn).toString();
				} else {
					controlNumber = "";
				}
				if (journalPostingTable.getValueAt(i, memoColumn) != null) {
					memo = journalPostingTable.getValueAt(i, memoColumn).toString();
				} else {
					memo = "";
				}

				sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
					+ "ReferenceNumber, PostDate, Memo, GLType, Class, LotName) VALUES("
					+ "'" + accountNumber + "', '" + debits + "', '" + credits + "', '"
					+ controlNumber + "', '" + referenceNumber + "', '"
					+ AccountingUtil.dateFormat.format(billPostDate.getDate()) + "', '"
					+ memo + "', 'General Journal', '" + journalClassCombo.getSelectedItem().toString() + "', "
					+ "'" + dms.DMSApp.getApplication().getCurrentlotName() + "'"
					+ ")";

				//System.out.println(sql[0]);

				dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

			}

			billPostDate.setDate(Calendar.getInstance().getTime());
			billMemo.setText("");
			billInvoice.setText("");

			DefaultTableModel aModel = (DefaultTableModel) journalPostingTable.getModel();
			AccountingUtil.clearTableModel(aModel);

		}
        }//GEN-LAST:event_jButton46enterBillsButtonsClicked

        private void jButton48enterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton48enterBillsButtonsClicked
		DefaultTableModel aModel = (DefaultTableModel) journalPostingTable.getModel();
		AccountingUtil.clearTableModel(aModel);

		billMemo.setText("");
		billInvoice.setText("");
		journalCredits2.setText("");
		journalDebits2.setText("");
		journalDifferences2.setText("");
        }//GEN-LAST:event_jButton48enterBillsButtonsClicked

        private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
		DefaultTableModel aModel = (DefaultTableModel) journalPostingTable.getModel();
		int rowCount = journalPostingTable.getRowCount();
		rowNo = rowCount;
		if (isRowRemoved) {
			rowNo = rowCount;
			isRowRemoved = false;
		}
		if (journalPostingTable.getRowCount() > 0) {
			if (journalPostingTable.getValueAt(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Account")) != null) {
				account = journalPostingTable.getValueAt(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Account")).toString();
			}
			if (journalPostingTable.getValueAt(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Debits")) != null) {
				debitAmount = journalPostingTable.getValueAt(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Debits")).toString();
			}
			if (journalPostingTable.getValueAt(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Credits")) != null) {
				creditAmount = journalPostingTable.getValueAt(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Credits")).toString();
			}
			if (account == null) {
				dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.ERROR_MESSAGE);
				journalPostingTable.setCellSelectionEnabled(true);
				journalPostingTable.changeSelection(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Account"), false, false);
				journalPostingTable.requestFocus();
			}
			if (creditAmount != null && !creditAmount.isEmpty() && creditAmount.contains(",")) {
				creditAmount = creditAmount.replaceAll(",", "");
			}

			if (debitAmount != null && !debitAmount.isEmpty() && debitAmount.contains(",")) {
				debitAmount = debitAmount.replaceAll(",", "");
			}
			if ((creditAmount == null || creditAmount.isEmpty() || Double.parseDouble(creditAmount) == 0.00) && (debitAmount == null || debitAmount.isEmpty() || Double.parseDouble(debitAmount) == 0.00)) {
				dms.DMSApp.displayMessage(this, "Please enter value for at least one field, debit or credit.", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (creditAmount != null && !creditAmount.isEmpty() && debitAmount != null && !debitAmount.isEmpty()) {
				dms.DMSApp.displayMessage(this, "Please enter only one value, debit or credit.", JOptionPane.ERROR_MESSAGE);
				journalPostingTable.setValueAt("", rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Debits"));
				journalPostingTable.setValueAt("", rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Credits"));
				return;
			}
			if (debitAmount != null && !debitAmount.isEmpty()) {
				if (!displayNumeric(debitAmount)) {
					//System.out.println("Amount is not Numeric");
					dms.DMSApp.displayMessage(this, "Invalid value entered for debit amount.", JOptionPane.ERROR_MESSAGE);
					journalPostingTable.setCellSelectionEnabled(true);
					journalPostingTable.changeSelection(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Debits"), false, false);
					journalPostingTable.requestFocus();
				}
			}
			if (creditAmount != null && !creditAmount.isEmpty()) {
				if (!displayNumeric(creditAmount)) {
					//System.out.println("Amount is not Numeric");
					dms.DMSApp.displayMessage(this, "Invalid value entered for credit amount.", JOptionPane.ERROR_MESSAGE);
					journalPostingTable.setCellSelectionEnabled(true);
					journalPostingTable.changeSelection(rowNo - 1, journalPostingTable.getColumnModel().getColumnIndex("Credits"), false, false);
					journalPostingTable.requestFocus();
				}
			}
		}
		// check to insert a row
		if (journalPostingTable.getRowCount() == 0) {
			addNewRowJournal(aModel, account);
			rowNo++;
		} else if ((account != null && debitAmount != null) || (account != null && creditAmount != null)) {
			addNewRowJournal(aModel, account);
			rowNo++;
		}
		//addNewRowJournal(aModel, "");
        }//GEN-LAST:event_jButton18ActionPerformed

        private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
		DefaultTableModel tableModel = (DefaultTableModel) journalPostingTable.getModel();
		// Remove last row
		tableModel.removeRow(tableModel.getRowCount() - 1);
		isRowRemoved = true;
		int rowCount = journalPostingTable.getRowCount();		
		double debitSum = 0.00;
		double creditSum = 0.00;
		for (int i = 0; i < rowCount; i++) {
			// check and sum all debit amounts
			if (journalPostingTable.getValueAt(i, 1) != null && !journalPostingTable.getValueAt(i, 1).toString().isEmpty()) {
				if (displayNumeric(journalPostingTable.getValueAt(i, 1).toString())) {
					if (journalPostingTable.getValueAt(i, 1).toString().contains(",")) {
						String beforeComma = journalPostingTable.getValueAt(i, 1).toString().substring(0, journalPostingTable.getValueAt(i, 1).toString().indexOf(","));
						String afterComma = journalPostingTable.getValueAt(i, 1).toString().substring(journalPostingTable.getValueAt(i, 1).toString().indexOf(",") + 1, journalPostingTable.getValueAt(i, 1).toString().length());
						String finalAmount = beforeComma + afterComma;
						//System.out.println("finalAmt = " + finalAmount);
						debitSum += Double.parseDouble(journalPostingTable.getValueAt(i, 1).toString());
						//System.out.println("sum 1:= " + sum);
					} else {
						debitSum += Double.parseDouble(journalPostingTable.getValueAt(i, 1).toString());
						//System.out.println("sum 2:= " + sum);
					}
				}
			}
			// check and sum all credit amounts
			if (journalPostingTable.getValueAt(i, 2) != null && !journalPostingTable.getValueAt(i, 2).toString().isEmpty()) {
				if (displayNumeric(journalPostingTable.getValueAt(i, 2).toString())) {
					if (journalPostingTable.getValueAt(i, 2).toString().contains(",")) {
						String beforeComma = journalPostingTable.getValueAt(i, 2).toString().substring(0, journalPostingTable.getValueAt(i, 2).toString().indexOf(","));
						String afterComma = journalPostingTable.getValueAt(i, 2).toString().substring(journalPostingTable.getValueAt(i, 2).toString().indexOf(",") + 2, journalPostingTable.getValueAt(i, 2).toString().length());
						String finalAmount = beforeComma + afterComma;
						//System.out.println("finalAmt = " + finalAmount);
						creditSum += Double.parseDouble(journalPostingTable.getValueAt(i, 2).toString());
						//System.out.println("sum 1:= " + sum);
					} else {
						creditSum += Double.parseDouble(journalPostingTable.getValueAt(i, 2).toString());
						//System.out.println("sum 2:= " + sum);
					}
				}
			}
		}
		journalDebits2.setValue(debitSum);
		journalCredits2.setValue(creditSum);
		double journalCredit = 0.00;
		if (!journalCredits2.getValue().toString().isEmpty()) {
			journalCredit = Double.parseDouble(journalCredits2.getValue().toString());
			journalDifferences2.setValue(journalCredit - debitSum);
		}
		double journalDebit = 0.00;
		if (!journalCredits2.getValue().toString().isEmpty()) {
			journalDebit = Double.parseDouble(journalDebits2.getValue().toString());
			journalDifferences2.setValue(journalDebit - creditSum);
		}
		//journalDifferences2.setValue(debitSum - creditSum);

		// Code to remove selected row, please choose the preferred approach
                /*int selectedRow = jTable4.getSelectedRow();
		 if (selectedRow == -1) {
		 JOptionPane.showMessageDialog(this, "Please select the row to remove");
		 } else {
		 tableModel.removeRow(selectedRow);
		 }
		 */
        }//GEN-LAST:event_jButton19ActionPerformed

        private void jPanel1ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel1ComponentShown
		journalDebits2.setValue(0.00);
		journalCredits2.setValue(0.00);
		journalDifferences2.setValue(0.00);


		Calendar c = Calendar.getInstance();

		if (billPostDate.getDate() == null) {
			billPostDate.setDate(c.getTime());
		}

		/*
		 int debitsColumn = AccountingUtil.getColumnByName(journalPostingTable, "Debits");
		 int creditsColumn = AccountingUtil.getColumnByName(journalPostingTable, "Credits");

		 journalPostingTable.setValueAt("0.00", 0, debitsColumn);
		 journalPostingTable.setValueAt("0.00", 0, creditsColumn);
		 */



        }//GEN-LAST:event_jPanel1ComponentShown

        private void inprogressRadioButtonjournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inprogressRadioButtonjournalsTypeRadioButton
		reloadROJournal();
        }//GEN-LAST:event_inprogressRadioButtonjournalsTypeRadioButton

        private void completedRadioButtonjournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_completedRadioButtonjournalsTypeRadioButton
		reloadROJournal();
        }//GEN-LAST:event_completedRadioButtonjournalsTypeRadioButton

        private void journalROTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalROTableMouseClicked
		try {
			String invoiceNumber = journalROTable.getValueAt(journalROTable.getSelectedRow(), journalROTable.getColumnModel().getColumnIndex("RO #")).toString();
			String customerNumber;
			String salesperson1Number;
			String salesperson2Number;
			String financeManager;
			String tradeInCode1;
			String tradeInCode2;

			String sql = "SELECT "
				+ "CAST(ROUND(TotalLabor,2) AS NUMERIC(10,2)) AS TotalLabor, "
				+ "CAST(ROUND(TotalLabor - LaborProfit,2) AS NUMERIC(10,2)) AS LaborCost, "
				+ "CAST(ROUND(TotalParts,2) AS NUMERIC(10,2)) AS TotalParts, "
				+ "CAST(ROUND(ShopSupplies,2) AS NUMERIC(10,2)) AS ShopSupplies, "
				+ "CAST(ROUND(Tax,2) AS NUMERIC(10,2)) AS Tax, "
				+ "CAST(ROUND(Total,2) AS NUMERIC(10,2)) AS Total, "
				+ "CAST(ROUND(Discount,2) AS NUMERIC(10,2)) AS Discount, "
				+ "CAST(ROUND(Fees,2) AS NUMERIC(10,2)) AS Fees, "
				+ "(SELECT CASE WHEN SUM(B.PartCost * B.Quantity) IS NULL THEN 0.00 ELSE "
				+ "CAST(ROUND(SUM(B.PartCost * B.Quantity),2) AS NUMERIC(10,2)) END "
				+ "FROM ServiceJobPartsTable B WHERE A.InvoiceNumber = B.InvoiceNumber) "
				+ "AS [PartsCost], "
				+ "(SELECT CASE WHEN SUM(B.SubletCost) IS NULL THEN 0.00 ELSE "
				+ "CAST(ROUND(SUM(B.SubletCost),2) AS NUMERIC(10,2)) END "
				+ "FROM ServiceJobsTable B WHERE A.InvoiceNumber = B.InvoiceNumber "
				+ ") AS[SubletCost], "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice) IS NULL THEN 0.00   "
				+ "ELSE CAST(ROUND(SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice),2) AS NUMERIC(10,2))   "
				+ "END FROM ServiceJobsTable B   "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND PayType = 'C') AS[CustomerPay],  "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'I') AS[Internal], "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal+B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'ASR') AS[AfterSalesRepair],   "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal+B.PartsTotal+B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WO') AS[WeOwe],  "
				/* LABOR */
				// Labor Customer
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'C') AS[LaborIncomeC], "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WPO') AS[LaborIncomeWPO], "
				// Labor Warranty
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WPL') AS[LaborIncomeWPL], "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WLO') AS[LaborIncomeWLO], "
				// Labor Internal				
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'I') AS[LaborIncomeI], "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WO') AS[LaborIncomeWO], "
				+ "(SELECT CASE WHEN SUM(B.LaborTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.LaborTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'ASR') AS[LaborIncomeASR], "
				/* END OF LABOR */
				/* SUBLET */
				// Sublet Customer
				+ "(SELECT CASE WHEN SUM(B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'C') AS[SubletIncomeC], "
				// Sublet Warranty
				+ "(SELECT CASE WHEN SUM(B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WPS') AS[SubletIncomeWPS], "
				// Sublet Internal				
				+ "(SELECT CASE WHEN SUM(B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'I') AS[SubletIncomeI], "
				+ "(SELECT CASE WHEN SUM(B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WO') AS[SubletIncomeWO], "
				+ "(SELECT CASE WHEN SUM(B.SubletPrice) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.SubletPrice),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'ASR') AS[SubletIncomeASR], "
				/* END OF SUBLET */
				/* PARTS */
				// Parts Customer
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'C') AS[PartsIncomeC], "
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WLO') AS[PartsIncomeWLO], "
				// Parts Warranty
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WPL') AS[PartsIncomeWPL], "
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WPO') AS[PartsIncomeWPO], "
				// Parts Internal				
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'I') AS[PartsIncomeI], "
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'WO') AS[PartsIncomeWO], "
				+ "(SELECT CASE WHEN SUM(B.PartsTotal) IS NULL  "
				+ "THEN 0.00 ELSE CAST(ROUND(SUM(B.PartsTotal),2) AS NUMERIC(10,2))  "
				+ "END FROM ServiceJobsTable B  "
				+ "WHERE A.InvoiceNumber = B.InvoiceNumber AND  PayType = 'ASR') AS[PartsIncomeASR], "
				/* END OF PARTS */
				+ "CustomerCode, InvoiceNumber, VehicleStock, VehicleYear, VehicleMake, VehicleModel, WarrantyDeductable "
				+ "FROM ServiceInvoiceTable A "
				+ "WHERE InvoiceNumber = '" + invoiceNumber + "' ";

			//System.out.println("sql" + sql);

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);
			ResultSetMetaData rsmd = rs.getMetaData();

			int row = 0;
			int i = 0;
			Double debits = 0.00;
			Double credits = 0.00;
			Double acv1 = 0.00;
			Double acv2 = 0.00;

			Double val;
			Double tax;

			DefaultTableModel aModel = (DefaultTableModel) journalRODetailsTable.getModel();

			aModel = (DefaultTableModel) journalRODetailsTable.getModel();
			AccountingUtil.clearTableModel(aModel);

			while (rs.next()) {

				tax = Double.parseDouble(AccountingUtil.formatAmountForDisplay(rs.getString("Tax").toString()));

				//// LABOR SALES
				// Customer
				if (Double.parseDouble(rs.getString("LaborIncomeC")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeC").toString());
					aModel.addRow(new String[]{"450 - Labor - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("LaborIncomeWPO")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeWPO").toString());
					aModel.addRow(new String[]{"450 - Labor - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("Fees")) != 0.00) {
					val = Double.parseDouble(rs.getString("Fees").toString());
					aModel.addRow(new String[]{"450 - Labor - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				// Warranty
				if (Double.parseDouble(rs.getString("LaborIncomeWPL")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeWPL").toString());
					aModel.addRow(new String[]{"452 - Labor - Warranty", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("LaborIncomeWLO")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeWLO").toString());
					aModel.addRow(new String[]{"452 - Labor - Warranty", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				// Internals
				if (Double.parseDouble(rs.getString("LaborIncomeI")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeI").toString());
					aModel.addRow(new String[]{"454 - Labor - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("LaborIncomeWO")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeWO").toString());
					aModel.addRow(new String[]{"454 - Labor - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("LaborIncomeASR")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeASR").toString());
					aModel.addRow(new String[]{"454 - Labor - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				//// END LABOR SALES

				val = Double.parseDouble(rs.getString("LaborCost").toString());
				aModel.addRow(new String[]{"651 - Labor - COS", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
				debits += val;

				val = Double.parseDouble(rs.getString("LaborCost").toString());
				aModel.addRow(new String[]{"264 - Labor - In Process", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
				credits += val;

				//// SUBLET SALES
				// Customer
				if (Double.parseDouble(rs.getString("SubletIncomeC")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletIncomeC").toString());
					aModel.addRow(new String[]{"456 - Sublet - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				// Warranty
				if (Double.parseDouble(rs.getString("SubletIncomeWPS")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletIncomeWPS").toString());
					aModel.addRow(new String[]{"455 - Sublet - Warranty", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				// Internals
				if (Double.parseDouble(rs.getString("SubletIncomeI")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletIncomeI").toString());
					aModel.addRow(new String[]{"457 - Sublet - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("SubletIncomeWO")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletIncomeWO").toString());
					aModel.addRow(new String[]{"457 - Sublet - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("SubletIncomeASR")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletIncomeASR").toString());
					aModel.addRow(new String[]{"457 - Sublet - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				//// END SUBLET SALES

				if (Double.parseDouble(rs.getString("SubletCost")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletCost").toString());
					aModel.addRow(new String[]{"656 - Sublet - COS", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;

					aModel.addRow(new String[]{"263 - Sublet - Inventory", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}

				//// PARTS SALES
				// Customer
				if (Double.parseDouble(rs.getString("PartsIncomeC")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeC").toString());
					aModel.addRow(new String[]{"470 - Parts - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("PartsIncomeWLO")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeWLO").toString());
					aModel.addRow(new String[]{"470 - Parts - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				// Warranty
				if (Double.parseDouble(rs.getString("PartsIncomeWPL")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeWPL").toString());
					aModel.addRow(new String[]{"472 - Parts - Warranty", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("PartsIncomeWPO")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeWPO").toString());
					aModel.addRow(new String[]{"472 - Parts - Warranty", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				// Internals
				if (Double.parseDouble(rs.getString("PartsIncomeI")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeI").toString());
					aModel.addRow(new String[]{"473 - Parts - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("PartsIncomeWO")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeWO").toString());
					aModel.addRow(new String[]{"473 - Parts - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("PartsIncomeASR")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeASR").toString());
					aModel.addRow(new String[]{"473 - Parts - Internal", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				//// END PARTS SALES

				val = Double.parseDouble(rs.getString("PartsCost").toString());
				aModel.addRow(new String[]{"670 - Parts - COS", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
				debits += val;

				val = Double.parseDouble(rs.getString("PartsCost").toString());
				aModel.addRow(new String[]{"262 - Parts - Inventory", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
				credits += val;

				val = Double.parseDouble(rs.getString("ShopSupplies").toString());
				aModel.addRow(new String[]{"458 - Other Shop Fees", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
				credits += val;

				//// RECEIVABLES
				if (Double.parseDouble(rs.getString("CustomerPay")) != 0.00) {
					val = Double.parseDouble(rs.getString("Total").toString());
					//val = Double.parseDouble(rs.getString("CustomerPay").toString());
					aModel.addRow(new String[]{"215 - Service Customer Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}
				if (Double.parseDouble(rs.getString("WarrantyDeductable")) != 0.00) {
					val = Double.parseDouble(rs.getString("WarrantyDeductable").toString());
					if (Double.parseDouble(rs.getString("CustomerPay")) == 0.00) {
						aModel.addRow(new String[]{"215 - Service Customer Receivable",
							AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
						debits += val;
					}

					aModel.addRow(new String[]{"450 - Labor - Customer", "",
						AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
				}
				if (Double.parseDouble(rs.getString("LaborIncomeWPL")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeWPL").toString());
					aModel.addRow(new String[]{"216 - Service Warranty Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}
				if (Double.parseDouble(rs.getString("LaborIncomeWLO")) != 0.00) {
					val = Double.parseDouble(rs.getString("LaborIncomeWLO").toString());
					aModel.addRow(new String[]{"216 - Service Warranty Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}
				if (Double.parseDouble(rs.getString("SubletIncomeWPS")) != 0.00) {
					val = Double.parseDouble(rs.getString("SubletIncomeWPS").toString());
					aModel.addRow(new String[]{"216 - Service Warranty Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}
				if (Double.parseDouble(rs.getString("PartsIncomeWPL")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeWPL").toString());
					aModel.addRow(new String[]{"216 - Service Warranty Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}
				if (Double.parseDouble(rs.getString("PartsIncomeWPO")) != 0.00) {
					val = Double.parseDouble(rs.getString("PartsIncomeWPO").toString());
					aModel.addRow(new String[]{"216 - Service Warranty Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}
				//// END RECEIVABLES

				if (Double.parseDouble(rs.getString("AfterSalesRepair")) != 0.00) {
					/*
					 val = Double.parseDouble(rs.getString("ASRLabor").toString());
					 aModel.addRow(new String[]{"454 - Internal Labor", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					 credits += val;
					 */

					val = Double.parseDouble(rs.getString("AfterSalesRepair"));
					aModel.addRow(new String[]{"607 - After Sales Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}

				if (Double.parseDouble(rs.getString("WeOwe")) != 0.00) {
					/*
					 val = Double.parseDouble(rs.getString("WeOweLabor").toString());
					 aModel.addRow(new String[]{"454 - Internal Labor", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					 credits += val;
					 */

					val = Double.parseDouble(rs.getString("WeOwe"));
					aModel.addRow(new String[]{"607 - After Sales Receivable", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}

				if (Double.parseDouble(rs.getString("Internal")) != 0.00) {
					/*val = Double.parseDouble(rs.getString("InternalLabor").toString());
					 aModel.addRow(new String[]{"454 - Internal Labor", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					 credits += val;
					 */

					val = Double.parseDouble(rs.getString("Internal"));
					aModel.addRow(new String[]{"251 - Vehicle Inventory", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}

				if (Double.parseDouble(rs.getString("Tax")) != 0.00) {
					val = Double.parseDouble(rs.getString("Tax").toString());
					aModel.addRow(new String[]{"333 - Sales Tax Payable", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;

					//aModel.addRow(new String[]{"450 - Labor - Customer", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					//credits += val;

				}

				if (Double.parseDouble(rs.getString("Discount")) != 0.00) {
					val = Double.parseDouble(rs.getString("Discount"));
					aModel.addRow(new String[]{"657 - Repair Order Discount", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", "", invoiceNumber});
					debits += val;
				}

				Double journalBalance = 0.00;
				journalBalance = debits - credits;

				journalBalance = (double) (Math.round(journalBalance * 100)) / 100;
				//System.out.println("journalBalance: " + journalBalance);

				if (journalBalance == 0.01) {
					//System.out.println("Here .01");
					val = 0.01;
					aModel.addRow(new String[]{"333 - Sales Tax Payable", "", AccountingUtil.formatAmountForDisplay(val.toString()), "", "", invoiceNumber});
					credits += val;
					journalBalance -= val;

				}

				journalDebits3.setValue(debits);
				journalCredits3.setValue(credits);
				journalDifferences3.setValue(journalBalance);

				if (journalBalance < 0.00) {
					journalDifferences3.setForeground(Color.red);
				} else {
					journalDifferences3.setForeground(Color.black);
				}

				journalSalesCustomer2.setText(rs.getString("CustomerCode").toString());
				journalSalesDeal2.setText(rs.getString("InvoiceNumber").toString());
				journalSalesStock.setText(rs.getString("VehicleStock").toString());
				journalSalesYear.setText(rs.getString("VehicleYear").toString());
				journalSalesMake.setText(rs.getString("VehicleMake").toString());
				journalSalesModel.setText(rs.getString("VehicleModel").toString());
			}

		} catch (SQLException ex) {
			Logger.getLogger(AccountingWindow.class
				.getName()).log(Level.SEVERE, null, ex);
		}

        }//GEN-LAST:event_journalROTableMouseClicked

        private void journalROTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalROTableKeyPressed
		// TODO add your handling code here:
        }//GEN-LAST:event_journalROTableKeyPressed

        private void journalROTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalROTableKeyReleased
		journalROTableMouseClicked(null);
        }//GEN-LAST:event_journalROTableKeyReleased

        private void journalROSearchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalROSearchTextFieldKeyReleased
		if (evt.getKeyCode() == 10) {
			journalROSearchButtonMouseClicked(null);
		}
		// TODO add your handling code here:
        }//GEN-LAST:event_journalROSearchTextFieldKeyReleased

        private void journalROSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalROSearchButtonMouseClicked
		try {
			String sql;
			String filterROType = "";
			String posted = "";
			String searchFilter = "";
			String journalSearch = journalROSearchTextField.getText();
			int journalSearchInt = 0;

			/*			if (jRadioButton11.isSelected()) {
			 filterROType = "AND Status = 'In-Progress' ";
			 } else if (jRadioButton12.isSelected()) {
			 filterROType = "AND Status = 'Completed' ";
			 } else if (jRadioButton13.isSelected()) {
			 filterROType = "AND Status = 'Closed' ";
			 }
			 */
			if (dms.util.StringUtil.isInteger(journalSearch)) {
				journalSearchInt = Integer.parseInt(journalSearch);
				searchFilter = "AND (InvoiceNumber = '" + journalSearchInt + "' "
					+ "OR Total = '" + journalSearchInt + "') ";
			} else if (!dms.util.StringUtil.isInteger(journalSearch)) {
				searchFilter = "AND (B.FirstName = '" + journalSearch + "' "
					+ "OR B.LastName = '" + journalSearch + "') ";
			}

			sql = "SELECT InvoiceNumber, ROOpen, ROClosed, B.FirstName + ' ' + B.LastName as FullName, "
				+ "CAST(ROUND(Total,2) AS NUMERIC(10,2)), "
				+ "LotName "
				+ "FROM ServiceInvoiceTable A "
				+ "LEFT JOIN CustomerTable B "
				+ "ON A.CustomerCode = B.CustomerCode "
				+ "WHERE LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
				+ filterROType
				+ searchFilter;

			//System.out.println("sql" + searchFilter);

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			DefaultTableModel aModel = (DefaultTableModel) journalROTable.getModel();
			AccountingUtil.clearTableModel(aModel);

			ResultSetMetaData rsmd = rs.getMetaData();
			int colNo = rsmd.getColumnCount();
			int opendateColumn = AccountingUtil.getColumnByName(journalROTable, "Open Date");
			int closedateColumn = AccountingUtil.getColumnByName(journalROTable, "Closed Date");

			//System.out.println("dates " + opendateColumn + " close " + closedateColumn);

			while (rs.next()) {
				Object[] values = new Object[colNo];
				for (int i = 0; i < colNo; i++) {
					if (i == opendateColumn) {
						if (rs.getObject(i + 1) != null) {
							values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
						}
					} else if (i == closedateColumn) {
						if (rs.getObject(i + 1) != null) {
							values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
						}
					} else {
						values[i] = rs.getObject(i + 1);
					}
				}
				aModel.addRow(values);
			}
			rs.getStatement().close();
		} catch (SQLException ex) {
			Logger.getLogger(Journals.class.getName()).log(Level.SEVERE, null, ex);
		}
        }//GEN-LAST:event_journalROSearchButtonMouseClicked

        private void jButton42MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton42MouseClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_jButton42MouseClicked

        private void journalROComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_journalROComponentShown
		reloadROJournal();
        }//GEN-LAST:event_journalROComponentShown

        private void jPanel114ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel114ComponentShown
		// TODO add your handling code here:
        }//GEN-LAST:event_jPanel114ComponentShown

        private void jideTabbedPane3ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jideTabbedPane3ComponentShown
		// TODO add your handling code here:
        }//GEN-LAST:event_jideTabbedPane3ComponentShown

        private void jButton22chartOfAccountsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton22chartOfAccountsClicked
		/*if (uICTable6.getSelectedRowCount() == 1) {

		 String postDateFilter = "";
		 if (!jCheckBox8.isSelected()) {
		 postDateFilter = "AND PostDate Between '" + dateFormat.format(jDateChooser7.getDate()) + "' AND '" + dateFormat.format(jDateChooser9.getDate()) + "' ";
		 }

		 try {

		 String sql = "SELECT "
		 + "AccountNumber,CAST(ROUND(Debit,2) AS NUMERIC(10,2)),CAST(ROUND(Credit,2) AS NUMERIC(10,2)),ControlNumber,ReferenceNumber,PostDate FROM AccountingGLTable "
		 + "WHERE AccountNumber = '" + uICTable6.getValueAt(uICTable6.getSelectedRow(), uICTable6.getColumnModel().getColumnIndex("Account")) + "' "
		 + postDateFilter
		 + "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
		 + "ORDER BY PostDate DESC";

		 ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

		 DefaultTableModel aModel = (DefaultTableModel) jTable5.getModel();
		 AccountingUtil.clearTableModel(aModel);
		 /*
		 if (aModel.getRowCount() > 0) {
		 for (int i = aModel.getRowCount(); i > 0; i--) {
		 aModel.removeRow(i - 1);
		 }
		 }
		 */
		/*
	  
		 ResultSetMetaData rsmd = rs.getMetaData();
		 int colNo = rsmd.getColumnCount();
		 while (rs.next()) {
		 Object[] values = new Object[colNo];
		 for (int i = 0; i < colNo; i++) {
		 if (i == 5) {
		 values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
		 } else {
		 values[i] = rs.getObject(i + 1);
		 }
		 }
		 aModel.addRow(values);
		 }
		 rs.getStatement().close();
		 } catch (Exception e) {
		 dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
		 }
		 }
		 */
        }//GEN-LAST:event_jButton22chartOfAccountsClicked

        private void jButton23chartOfAccountsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton23chartOfAccountsClicked
		reloadROJournal();
        }//GEN-LAST:event_jButton23chartOfAccountsClicked

    private void journalPostingTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalPostingTableMouseClicked
	    // TODO add your handling code here:
	    //System.out.println("Inside journalPostingTableMouseClicked()..");
	    int row = journalPostingTable.rowAtPoint(evt.getPoint());
	    int col = journalPostingTable.columnAtPoint(evt.getPoint());
	    //System.out.println("Row := " + row);
	    //System.out.println("Col := " + col);
	    if (col == 1 || col == 2) {               // if Debit or Credit Column is modified/clicked
		    //System.out.println("Amount Column clicked !");           
		    ((javax.swing.DefaultCellEditor) journalPostingTable.getDefaultEditor(new Object().getClass())).setClickCountToStart(1);
	    }
    }//GEN-LAST:event_journalPostingTableMouseClicked

        private void closedRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closedRadioButtonMouseClicked
		reloadROJournal();
        }//GEN-LAST:event_closedRadioButtonMouseClicked

        private void journalRetailRadiojournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalRetailRadiojournalsTypeRadioButton
		reloadJournalDeals();
        }//GEN-LAST:event_journalRetailRadiojournalsTypeRadioButton

        private void dealsRadioButtonjournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dealsRadioButtonjournalsTypeRadioButton
		// TODO add your handling code here:
        }//GEN-LAST:event_dealsRadioButtonjournalsTypeRadioButton

        private void serviceRadioButtonjournalsTypeRadioButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serviceRadioButtonjournalsTypeRadioButton
		// TODO add your handling code here:
        }//GEN-LAST:event_serviceRadioButtonjournalsTypeRadioButton

        private void closedRadioButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closedRadioButton1MouseClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_closedRadioButton1MouseClicked

        private void jPanel117ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel117ComponentShown
		// TODO add your handling code here:
        }//GEN-LAST:event_jPanel117ComponentShown

        private void journalCollectionsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalCollectionsTableMouseClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_journalCollectionsTableMouseClicked

        private void journalCollectionsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalCollectionsTableKeyPressed
		// TODO add your handling code here:
        }//GEN-LAST:event_journalCollectionsTableKeyPressed

        private void journalCollectionsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalCollectionsTableKeyReleased
		// TODO add your handling code here:
        }//GEN-LAST:event_journalCollectionsTableKeyReleased

        private void journalROSearchTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_journalROSearchTextField1KeyReleased
		// TODO add your handling code here:
        }//GEN-LAST:event_journalROSearchTextField1KeyReleased

        private void journalROSearchButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_journalROSearchButton1MouseClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_journalROSearchButton1MouseClicked

        private void jButton24chartOfAccountsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton24chartOfAccountsClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_jButton24chartOfAccountsClicked

        private void jButton43MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton43MouseClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_jButton43MouseClicked

        private void jButton25chartOfAccountsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton25chartOfAccountsClicked
		// TODO add your handling code here:
        }//GEN-LAST:event_jButton25chartOfAccountsClicked

        private void journalCollectionsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_journalCollectionsComponentShown
		// TODO add your handling code here:
        }//GEN-LAST:event_journalCollectionsComponentShown
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField billInvoice;
    private javax.swing.JTextField billMemo;
    private com.toedter.calendar.JDateChooser billPostDate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    public javax.swing.JRadioButton closedRadioButton;
    public javax.swing.JRadioButton closedRadioButton1;
    public javax.swing.JRadioButton completedRadioButton;
    private javax.swing.JFormattedTextField credits11;
    public javax.swing.JRadioButton dealsRadioButton;
    private javax.swing.JFormattedTextField debits11;
    private javax.swing.JFormattedTextField difference11;
    private javax.swing.JPanel enterBillsPanel3;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler17;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    public javax.swing.JRadioButton inprogressRadioButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton48;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel111;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel116;
    private javax.swing.JLabel jLabel117;
    private javax.swing.JLabel jLabel118;
    private javax.swing.JLabel jLabel119;
    private javax.swing.JLabel jLabel120;
    private javax.swing.JLabel jLabel121;
    private javax.swing.JLabel jLabel122;
    private javax.swing.JLabel jLabel123;
    private javax.swing.JLabel jLabel124;
    private javax.swing.JLabel jLabel125;
    private javax.swing.JLabel jLabel126;
    private javax.swing.JLabel jLabel127;
    private javax.swing.JLabel jLabel128;
    private javax.swing.JLabel jLabel129;
    private javax.swing.JLabel jLabel130;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel132;
    private javax.swing.JLabel jLabel133;
    private javax.swing.JLabel jLabel136;
    private javax.swing.JLabel jLabel139;
    private javax.swing.JLabel jLabel141;
    private javax.swing.JLabel jLabel142;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel108;
    private javax.swing.JPanel jPanel109;
    private javax.swing.JPanel jPanel110;
    private javax.swing.JPanel jPanel111;
    private javax.swing.JPanel jPanel112;
    private javax.swing.JPanel jPanel113;
    private javax.swing.JPanel jPanel114;
    private javax.swing.JPanel jPanel115;
    private javax.swing.JPanel jPanel116;
    private javax.swing.JPanel jPanel117;
    private javax.swing.JPanel jPanel118;
    private javax.swing.JPanel jPanel119;
    private javax.swing.JPanel jPanel120;
    private javax.swing.JPanel jPanel121;
    private javax.swing.JPanel jPanel124;
    private javax.swing.JPanel jPanel141;
    private javax.swing.JPanel jPanel142;
    private javax.swing.JPanel jPanel143;
    private javax.swing.JPanel jPanel144;
    private javax.swing.JPanel jPanel145;
    private javax.swing.JPanel jPanel146;
    private javax.swing.JPanel jPanel147;
    private javax.swing.JPanel jPanel148;
    private javax.swing.JPanel jPanel149;
    private javax.swing.JPanel jPanel150;
    private javax.swing.JPanel jPanel151;
    private javax.swing.JPanel jPanel152;
    private javax.swing.JPanel jPanel153;
    private javax.swing.JPanel jPanel154;
    private javax.swing.JPanel jPanel155;
    private javax.swing.JPanel jPanel156;
    private javax.swing.JPanel jPanel157;
    private javax.swing.JPanel jPanel158;
    private javax.swing.JPanel jPanel159;
    private javax.swing.JPanel jPanel160;
    private javax.swing.JPanel jPanel161;
    private javax.swing.JPanel jPanel162;
    private javax.swing.JPanel jPanel163;
    private javax.swing.JPanel jPanel164;
    private javax.swing.JPanel jPanel165;
    private javax.swing.JPanel jPanel166;
    private javax.swing.JPanel jPanel167;
    private javax.swing.JPanel jPanel168;
    private javax.swing.JPanel jPanel169;
    private javax.swing.JPanel jPanel170;
    private javax.swing.JPanel jPanel171;
    private javax.swing.JPanel jPanel172;
    private javax.swing.JPanel jPanel173;
    private javax.swing.JPanel jPanel174;
    private javax.swing.JPanel jPanel175;
    private javax.swing.JPanel jPanel176;
    private javax.swing.JPanel jPanel177;
    private javax.swing.JPanel jPanel178;
    private javax.swing.JPanel jPanel179;
    private javax.swing.JPanel jPanel180;
    private javax.swing.JPanel jPanel181;
    private javax.swing.JPanel jPanel195;
    private javax.swing.JPanel jPanel197;
    private javax.swing.JPanel jPanel198;
    private javax.swing.JPanel jPanel199;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel200;
    private javax.swing.JPanel jPanel201;
    private javax.swing.JPanel jPanel202;
    private javax.swing.JPanel jPanel203;
    private javax.swing.JPanel jPanel204;
    private javax.swing.JPanel jPanel205;
    private javax.swing.JPanel jPanel218;
    private javax.swing.JPanel jPanel219;
    private javax.swing.JPanel jPanel220;
    private javax.swing.JPanel jPanel221;
    private javax.swing.JPanel jPanel223;
    private javax.swing.JPanel jPanel224;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel64;
    private javax.swing.JPanel jPanel65;
    private javax.swing.JPanel jPanel66;
    private javax.swing.JPanel jPanel92;
    private javax.swing.JPanel jPanel93;
    private javax.swing.JPanel jPanel94;
    private javax.swing.JPanel jPanel98;
    public javax.swing.JRadioButton jRadioButton10;
    public javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane25;
    private javax.swing.JScrollPane jScrollPane26;
    private javax.swing.JScrollPane jScrollPane27;
    private com.jidesoft.swing.JideTabbedPane jideTabbedPane3;
    private javax.swing.JComboBox journalClassCombo;
    private javax.swing.JPanel journalCollections;
    private javax.swing.JTable journalCollectionsDetailTable;
    public javax.swing.JTable journalCollectionsTable;
    private javax.swing.JFormattedTextField journalCredits;
    private javax.swing.JFormattedTextField journalCredits2;
    private javax.swing.JFormattedTextField journalCredits3;
    private javax.swing.JFormattedTextField journalCredits4;
    private javax.swing.JLabel journalDealDate;
    private javax.swing.JLabel journalDealDate1;
    private javax.swing.JLabel journalDealPostedDate;
    private javax.swing.JLabel journalDealPostedDate1;
    private javax.swing.JLabel journalDealPostedDate2;
    private javax.swing.JLabel journalDealPostedDate3;
    private javax.swing.JPanel journalDeals;
    private javax.swing.JTable journalDealsDetailsTable;
    public static javax.swing.JTable journalDealsTable;
    private javax.swing.JFormattedTextField journalDebits;
    private javax.swing.JFormattedTextField journalDebits2;
    private javax.swing.JFormattedTextField journalDebits3;
    private javax.swing.JFormattedTextField journalDebits4;
    private javax.swing.JFormattedTextField journalDifferences;
    private javax.swing.JFormattedTextField journalDifferences2;
    private javax.swing.JFormattedTextField journalDifferences3;
    private javax.swing.JFormattedTextField journalDifferences4;
    private javax.swing.JButton journalPostButton;
    private com.toedter.calendar.JDateChooser journalPostDate;
    public static javax.swing.JPanel journalPostDatePanel;
    private javax.swing.JPanel journalPostDatePanel1;
    private javax.swing.JButton journalPostReversal;
    public static javax.swing.JRadioButton journalPostedRadio;
    private javax.swing.JTable journalPostingTable;
    private javax.swing.JPanel journalRO;
    private javax.swing.JTable journalRODetailsTable;
    private javax.swing.JButton journalROSearchButton;
    private javax.swing.JButton journalROSearchButton1;
    private javax.swing.JTextField journalROSearchTextField;
    private javax.swing.JTextField journalROSearchTextField1;
    public javax.swing.JTable journalROTable;
    public static javax.swing.JRadioButton journalRetailRadio;
    private com.toedter.calendar.JDateChooser journalReversalDate;
    private com.toedter.calendar.JDateChooser journalReversalDate1;
    private com.toedter.calendar.JDateChooser journalReversalDate2;
    private com.toedter.calendar.JDateChooser journalReversalDate3;
    public static javax.swing.JPanel journalReversalDatePanel;
    private javax.swing.JPanel journalReversalDatePanel1;
    private javax.swing.JPanel journalReversalDatePanel2;
    private javax.swing.JPanel journalReversalDatePanel3;
    private javax.swing.JTable journalReversalTable;
    private javax.swing.JLabel journalSalesCustomer;
    private javax.swing.JLabel journalSalesCustomer1;
    private javax.swing.JLabel journalSalesCustomer2;
    private javax.swing.JLabel journalSalesCustomer3;
    private javax.swing.JLabel journalSalesCustomerName;
    private javax.swing.JLabel journalSalesCustomerName1;
    private javax.swing.JLabel journalSalesCustomerName2;
    private javax.swing.JLabel journalSalesCustomerName3;
    private javax.swing.JLabel journalSalesDeal;
    private javax.swing.JLabel journalSalesDeal1;
    private javax.swing.JLabel journalSalesDeal2;
    private javax.swing.JLabel journalSalesDeal3;
    private javax.swing.JLabel journalSalesMake;
    private javax.swing.JLabel journalSalesMake1;
    private javax.swing.JLabel journalSalesMake2;
    private javax.swing.JLabel journalSalesMake3;
    private javax.swing.JLabel journalSalesModel;
    private javax.swing.JLabel journalSalesModel1;
    private javax.swing.JLabel journalSalesModel2;
    private javax.swing.JLabel journalSalesModel3;
    private javax.swing.JLabel journalSalesStock;
    private javax.swing.JLabel journalSalesStock1;
    private javax.swing.JLabel journalSalesYear;
    private javax.swing.JLabel journalSalesYear1;
    private javax.swing.JLabel journalSalesYear2;
    private javax.swing.JLabel journalSalesYear3;
    private javax.swing.JButton journalSearchButton;
    private javax.swing.JTextField journalSearchTextField;
    public static javax.swing.JRadioButton journalWholesaleRadio;
    private javax.swing.JPanel journallClass;
    private javax.swing.JPanel journalsPostPanel;
    private javax.swing.JPanel journalsPostPanel1;
    private javax.swing.JPanel journalsReversalPanel;
    private javax.swing.JPanel journalsReversalPanel1;
    private javax.swing.JPanel journalsReversalPanel2;
    private javax.swing.JPanel journalsReversalPanel3;
    private javax.swing.JDialog reversalPopup;
    private com.toedter.calendar.JDateChooser roEndDate;
    private com.toedter.calendar.JDateChooser roEndDate1;
    private com.toedter.calendar.JDateChooser roStartDate;
    private com.toedter.calendar.JDateChooser roStartDate1;
    public javax.swing.JRadioButton serviceRadioButton;
    // End of variables declaration//GEN-END:variables

	private void reloadROJournal() {
		try {
			String sql;

			String filterROType = "";
			String posted = "";
			String dateRange = "";
			String orderBy = "";
			String sql2 = "";

			posted = "AND PostedDate IS NULL ";

			if (inprogressRadioButton.isSelected()) {
				filterROType = "AND Status = 'In-Progress' ";
				orderBy = "ORDER BY ROOpen DESC ";
			} else if (completedRadioButton.isSelected()) {
				filterROType = "AND Status = 'Completed' ";
				dateRange = "AND ROCompleted Between '" + AccountingUtil.dateFormat.format(roStartDate.getDate()) + "' AND '"
					+ AccountingUtil.dateFormat.format(roEndDate.getDate()) + "' ";
				orderBy = "ORDER BY ROCompleted DESC ";
			} else if (closedRadioButton.isSelected()) {
				filterROType = "AND Status = 'Closed' ";
				dateRange = "AND ROClosed Between '" + AccountingUtil.dateFormat.format(roStartDate.getDate()) + "' AND '"
					+ AccountingUtil.dateFormat.format(roEndDate.getDate()) + "' ";
				orderBy = "ORDER BY ROClosed DESC ";
			}

			sql = "SELECT InvoiceNumber, ROOpen, ROClosed, B.FirstName + ' ' + B.LastName as FullName, "
				+ "CAST(ROUND(Total,2) AS NUMERIC(10,2)), "
				+ "LotName "
				+ "FROM ServiceInvoiceTable A "
				+ "LEFT JOIN CustomerTable B "
				+ "ON A.CustomerCode = B.CustomerCode "
				//+ "WHERE LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
				+ "WHERE 1+1 = 2 "
				+ filterROType
				+ dateRange
				+ orderBy;

			sql2 = "SELECT COUNT(*) as Count "
				+ "FROM ServiceInvoiceTable A "
				//+ "WHERE LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
				+ "WHERE 1+1 = 2 "
				+ filterROType
				+ dateRange;

			//System.out.println("sql" + sql);

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			DefaultTableModel aModel;
			aModel = (DefaultTableModel) journalROTable.getModel();
			AccountingUtil.clearTableModel(aModel);

			ResultSetMetaData rsmd = rs.getMetaData();
			int colNo = rsmd.getColumnCount();
			int rowCount = 1;

			int opendateColumn = AccountingUtil.getColumnByName(journalROTable, "Open Date");
			int closedateColumn = AccountingUtil.getColumnByName(journalROTable, "Close Date");

			while (rs.next()) {
				Object[] values = new Object[colNo];
				for (int i = 0; i < colNo; i++) {
					if (i == opendateColumn) {
						if (rs.getObject(i + 1) != null) {
							values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
						}
					} else if (i == closedateColumn) {
						if (rs.getObject(i + 1) != null) {
							values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
						}
					} else {
						values[i] = rs.getObject(i + 1);
					}
				}
				aModel.addRow(values);
			}

			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql2);

			while (rs.next()) {
				//System.out.println("Count " + rs.getString("Count"));
				jLabel114.setText(rs.getString("Count"));
			}

			rs.getStatement().close();

		} catch (SQLException ex) {
			Logger.getLogger(AccountingWindow.class
				.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public static void reloadJournalDeals() {
		try {

			String sql = "";
			String filterDealType = "";
			String posted = "";
			posted = "AND PostedDate IS NULL ";

			if (journalPostedRadio.isSelected()) {
				//System.out.println("is Selected");
				journalPostDatePanel.setVisible(false);
				journalReversalDatePanel.setVisible(true);
			} else {
				//System.out.println("is Not Selected");
				journalPostDatePanel.setVisible(true);
				journalReversalDatePanel.setVisible(false);
			}
			if (journalRetailRadio.isSelected()) {
				filterDealType = "AND (DealType = 'FINANCE' OR DealType = 'CASH' OR DealType = 'SAME AS CASH' OR DealType = 'BHPH') ";
			} else if (journalWholesaleRadio.isSelected()) {
				filterDealType = "AND DealType = 'WHOLESALE' ";
			} else if (journalPostedRadio.isSelected()) {
				filterDealType = "AND DealType IN ('FINANCE','CASH','SAME AS CASH','BHPH','WHOLESALE') ";
				posted = "AND PostedDate IS NOT NULL ";
			}

			sql = "SELECT AccountNumber, SoldDate, B.FirstName + ' ' + B.LastName, StockNumber, DealType, DealFunded, LotName, Status "
				+ "FROM DealsTable A "
				+ "LEFT JOIN CustomerTable B "
				+ "ON A.BuyerCode = B.CustomerCode "
				+ "WHERE Status NOT IN ('Quote') "
				+ posted
				+ filterDealType
				+ "ORDER BY SoldDate DESC";

			//System.out.println("reloadJournalDeals: " + sql);

			ResultSet rs;
			rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

			DefaultTableModel aModel = (DefaultTableModel) journalDealsTable.getModel();
			AccountingUtil.clearTableModel(aModel);

			ResultSetMetaData rsmd = rs.getMetaData();
			int colNo = rsmd.getColumnCount();
			while (rs.next()) {
				Object[] values = new Object[colNo];
				for (int i = 0; i < colNo; i++) {
					if (i == 1) {
						values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
					} else {
						values[i] = rs.getObject(i + 1);
					}
				}
				aModel.addRow(values);
			}

			rs.getStatement().close();

		} catch (SQLException ex) {
			ex.printStackTrace();
			Logger
				.getLogger(AccountingWindow.class
				.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
