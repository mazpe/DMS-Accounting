/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.panels;

import dms.util.AccountingHelper;
import dms.util.AccountingHelperToLoadCustomerLastRecords;
import dms.util.AccountingHelperToLoadCustomers;
import dms.util.AccountingUtil;
import dms.util.EnglishNumberToWords;
import dms.util.PrintingUtil;
import dms.util.TableCellListener;
import dms.windows.AccountingWindow;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;

/**
 *
 * @author Lester
 */
public class BankingPanel extends javax.swing.JPanel {

    private static dms.panels.AccountsPayablePanel accountsPayablePanel = null;
    private static dms.panels.BankingPanel instance = null;
    private String[] sql = null;
    String account = null;
    String amount = null;
    int checkbookRowNo = 0;
    int depositRowNo = 0;
    boolean isRowRemoved = false;
    private List<Object> accountsToBeShownList = new ArrayList<Object>();
    private List<Object> accountsToBeShownEditDepositList = new ArrayList<Object>();
    int depositNo = 0;
    int checkNo = 0;
    int bankAccountNo = 0;
    String gLTypeForEditCheck = "";
    String paidToForEditCheck = "";
    String accountClass = "";
    String classForeditDeposit = "";
    Map<Integer, String> depositsStatusMapForDepositsPanel = new HashMap<Integer, String>();
    Map<Integer, String> checksStatusMapForDepositsPanel = new HashMap<Integer, String>();
    ArrayList customerNames = new ArrayList();
    ArrayList customerNames1 = new ArrayList();
    ArrayList vendorNames = new ArrayList();
    ArrayList employeeNames = new ArrayList();
    ArrayList<JasperPrint> jasperReportsToPrintList = new ArrayList<JasperPrint>();
    int counter = 0;
    int nextCheckNo = 0;
    boolean checkNoExists = false;
    String checkNumber = null;
    private String originalCheckNumber;

    public String getOriginalCheckNumber() {
        return originalCheckNumber;
    }

    public void setOriginalCheckNumber(String originalCheckNumber) {
        this.originalCheckNumber = originalCheckNumber;
    }
    private Double TotalAmountForMultipleInvoices;

    public Double getTotalAmountForMultipleInvoices() {
        return TotalAmountForMultipleInvoices;
    }

    public void setTotalAmountForMultipleInvoices(Double TotalAmountForMultipleInvoices) {
        this.TotalAmountForMultipleInvoices = TotalAmountForMultipleInvoices;
    }

    public static dms.panels.BankingPanel getInstance() {
        if (instance == null) {
            instance = new BankingPanel();
        }
        return instance;
    }

    /**
     * Creates new form BankingPanel
     */
    public BankingPanel() {
        // Default methods
        initComponents();
        checkbookAccountsTable.setValueAt("0.00", 0, 1);
        accountsPayablePanel = AccountsPayablePanel.getInstance();
        addComboBoxesToTables();
        reloadDeposits();
        reloadChecks();
        AccountingUtil.dateFilter(jPanel159, checkbookAllDates); // checkbookDateFilter
        AccountingUtil.dateFilter(jPanel161, depositsAllDates); // depositsDateFilter
        addTableCellListenerForCheckAccountsTable();
        // hide the Deposit# from main and edit panel
        jLabel23.setVisible(false);
        //depositsNumber.setVisible(false);
        jLabel34.setVisible(false);
        //depositsNumber1.setVisible(false);
        // Setup default dates
        Calendar c = Calendar.getInstance();

        if (depositsDate.getDate() == null) {
            depositsDate.setDate(c.getTime());
        }
        if (jDateChooser15.getDate() == null) {
            jDateChooser15.setDate(c.getTime());
        }
        if (jDateChooser13.getDate() == null) {
            jDateChooser13.setDate(c.getTime());
        }
        if (checkbookDate.getDate() == null) {
            checkbookDate.setDate(c.getTime());
        }
        if (jDateChooser12.getDate() == null) {
            c.add(Calendar.DATE, 1 - c.get(Calendar.DATE));
            jDateChooser12.setDate(c.getTime());
        }
        if (jDateChooser14.getDate() == null) {
            c.add(Calendar.DATE, 1 - c.get(Calendar.DATE));
            jDateChooser14.setDate(c.getTime());
        }

        // Default variables
        checkbookAmount.setValue(0.00);
        depositsAmount.setValue(0.00);

        // Misc
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setTotalAccountsSumOnTableCellAction(e);
            }
        };

        TableCellListener tcl = new TableCellListener(depositsAccountsTable, action);

        Action action2 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                int modifiedRowNo = 0;
                String amountForDisplay = null;
                TableCellListener tcl = (TableCellListener) e.getSource();
                modifiedRowNo = tcl.getRow();
                int rowCount = depositsAccountsTable1.getRowCount();

                for (int i = 0; i < rowCount; i++) {
                    //if (amountColumnModified) {   // come inside the condition only if amount Column is modified
                    if (depositsAccountsTable1.getValueAt(i, 1) != null && !depositsAccountsTable1.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(depositsAccountsTable1.getValueAt(i, 1).toString())) {
                            //System.out.println("cannot enter aplphabets/special characters in amount field");
                            dms.DMSApp.displayMessage(depositsAccountsTable1, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                            depositsAccountsTable1.setCellSelectionEnabled(true);
                            depositsAccountsTable1.changeSelection(modifiedRowNo, depositsAccountsTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                            depositsAccountsTable1.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(depositsAccountsTable1.getValueAt(i, 1).toString())) {
                            if (depositsAccountsTable1.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(depositsAccountsTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(depositsAccountsTable1.getValueAt(i, 1).toString());
                                //System.out.println("sum 2:= " + sum);
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(depositsAccountsTable1.getValueAt(modifiedRowNo, 1).toString());
                                //System.out.println("amountForDisplay := " + amountForDisplay);
                                depositsAccountsTable1.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                    //}
                }

                depositsTotalAccounts1.setValue(sum);

            }
        };

        TableCellListener tcl2 = new TableCellListener(depositsAccountsTable1, action2);

        Action action3 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                TableCellListener tcl = (TableCellListener) e.getSource();
                int rowCount = checkAccountsTable1.getRowCount();
                int modifiedRowNo;
                String amountForDisplay;
                modifiedRowNo = tcl.getRow();
                for (int i = 0; i < rowCount; i++) {
                    if (checkAccountsTable1.getValueAt(i, 1) != null && !checkAccountsTable1.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(checkAccountsTable1.getValueAt(i, 1).toString())) {
                            dms.DMSApp.displayMessage(checkAccountsTable1, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                            checkAccountsTable1.setCellSelectionEnabled(true);
                            checkAccountsTable1.changeSelection(modifiedRowNo, checkAccountsTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                            checkAccountsTable1.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(checkAccountsTable1.getValueAt(i, 1).toString())) {
                            if (checkAccountsTable1.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(checkAccountsTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(checkAccountsTable1.getValueAt(i, 1).toString());
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(checkAccountsTable1.getValueAt(modifiedRowNo, 1).toString());
                                checkAccountsTable1.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                }
                checkAccount1.setValue(sum);
            }
        };

        TableCellListener tcl3 = new TableCellListener(checkAccountsTable1, action3);

        reloadBankAccounts();
    }

    private void setTotalAccountsSumOnTableCellAction(ActionEvent e) {
        double sum = 0.00;
        int modifiedRowNo = 0;
        String amountForDisplay = null;
        TableCellListener tcl = (TableCellListener) e.getSource();
        modifiedRowNo = tcl.getRow();
        int rowCount = depositsAccountsTable.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            if (depositsAccountsTable.getValueAt(i, 1) != null && !depositsAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                if (!AccountingUtil.displayNumeric(depositsAccountsTable.getValueAt(i, 1).toString())) {
                    dms.DMSApp.displayMessage(depositsAccountsTable, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                    depositsAccountsTable.setCellSelectionEnabled(true);
                    depositsAccountsTable.changeSelection(modifiedRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
                    depositsAccountsTable.requestFocus();
                }
                if (AccountingUtil.displayNumeric(depositsAccountsTable.getValueAt(i, 1).toString())) {
                    if (depositsAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                        sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                    } else {
                        sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString());
                        //System.out.println("sum 2:= " + sum);
                    }
                    if (modifiedRowNo == i) {   // change amount format only for current row
                        amountForDisplay = AccountingUtil.formatAmountForDisplay(depositsAccountsTable.getValueAt(modifiedRowNo, 1).toString());
                        //System.out.println("amountForDisplay := " + amountForDisplay);
                        depositsAccountsTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
                    }
                }
            }
        }
        depositsTotalAccounts.setValue(sum);
    }

    private void setTotalAccountsSumForDeposit() {
        double sum = 0.00;
        int rowCount = depositsAccountsTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (depositsAccountsTable.getValueAt(i, 1) != null && !depositsAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                if (!AccountingUtil.displayNumeric(depositsAccountsTable.getValueAt(i, 1).toString())) {
                    dms.DMSApp.displayMessage(depositsAccountsTable, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                    depositsAccountsTable.setCellSelectionEnabled(true);
                    depositsAccountsTable.requestFocus();
                }
                if (AccountingUtil.displayNumeric(depositsAccountsTable.getValueAt(i, 1).toString())) {
                    if (depositsAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                        sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                    } else {
                        sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString());
                    }
                }
            }
        }
        depositsTotalAccounts.setValue(sum);
    }

    private void setTotalAccountsSumForCheck() {
        double sum = 0.00;
        int rowCount = checkbookAccountsTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (checkbookAccountsTable.getValueAt(i, 1) != null && !checkbookAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                if (!AccountingUtil.displayNumeric(checkbookAccountsTable.getValueAt(i, 1).toString())) {
                    dms.DMSApp.displayMessage(checkbookAccountsTable, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                    checkbookAccountsTable.setCellSelectionEnabled(true);
                    checkbookAccountsTable.requestFocus();
                }
                if (AccountingUtil.displayNumeric(checkbookAccountsTable.getValueAt(i, 1).toString())) {
                    if (checkbookAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                        sum += Double.parseDouble(checkbookAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                    } else {
                        sum += Double.parseDouble(checkbookAccountsTable.getValueAt(i, 1).toString());
                    }
                }
            }
        }
        checksTotalAccounts.setValue(sum);
    }

    private void addTableCellListenerForCheckAccountsTable() {
        Action action1 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                int modifiedRowNo = 0;
                String amountForDisplay = null;
                TableCellListener tcl = (TableCellListener) e.getSource();
                modifiedRowNo = tcl.getRow();
                int rowCount = checkbookAccountsTable.getRowCount();

                for (int i = 0; i < rowCount; i++) {
                    //if (amountColumnModified) {   // come inside the condition only if amount Column is modified
                    if (checkbookAccountsTable.getValueAt(i, 1) != null && !checkbookAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(checkbookAccountsTable.getValueAt(i, 1).toString())) {
                            //System.out.println("cannot enter aplphabets/special characters in amount field");
                            dms.DMSApp.displayMessage(checkbookAccountsTable, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                            checkbookAccountsTable.setCellSelectionEnabled(true);
                            checkbookAccountsTable.changeSelection(modifiedRowNo, checkbookAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
                            checkbookAccountsTable.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(checkbookAccountsTable.getValueAt(i, 1).toString())) {
                            if (checkbookAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(checkbookAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(checkbookAccountsTable.getValueAt(i, 1).toString());
                                //System.out.println("sum 2:= " + sum);
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(checkbookAccountsTable.getValueAt(modifiedRowNo, 1).toString());
                                //System.out.println("amountForDisplay := " + amountForDisplay);
                                checkbookAccountsTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                    //}
                }
                checksTotalAccounts.setValue(sum);
            }
        };
        TableCellListener tcl1 = new TableCellListener(checkbookAccountsTable, action1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        editDepositDialog1 = new javax.swing.JDialog();
        jPanel8 = new javax.swing.JPanel();
        jPanel132 = new javax.swing.JPanel();
        jPanel133 = new javax.swing.JPanel();
        jPanel134 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel148 = new javax.swing.JPanel();
        jLabel162 = new javax.swing.JLabel();
        depositsBankCombo2 = new javax.swing.JComboBox();
        jPanel186 = new javax.swing.JPanel();
        jLabel158 = new javax.swing.JLabel();
        depositsMemo2 = new javax.swing.JTextField();
        billClass_deposit1 = new com.jidesoft.swing.AutoCompletionComboBox();
        jLabel7 = new javax.swing.JLabel();
        jPanel131 = new javax.swing.JPanel();
        jPanel185 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        depositsEndingBalance1 = new javax.swing.JFormattedTextField();
        jPanel187 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        depositsNumber1 = new javax.swing.JFormattedTextField();
        jPanel188 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        depositsDate1 = new com.toedter.calendar.JDateChooser();
        jPanel189 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        depositsAmount1 = new javax.swing.JFormattedTextField();
        jPanel99 = new javax.swing.JPanel();
        depositsDeposit1 = new javax.swing.JRadioButton();
        depositsACH1 = new javax.swing.JRadioButton();
        jPanel199 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        depositsTotalAccounts1 = new javax.swing.JFormattedTextField();
        jLabel54 = new javax.swing.JLabel();
        jPanel97 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        depositsAccountsTable1 = new javax.swing.JTable();
        jPanel112 = new javax.swing.JPanel();
        jPanel130 = new javax.swing.JPanel();
        saveDepositButton2 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        clearCheck3 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        checkbookEntryID3 = new javax.swing.JLabel();
        jPanel98 = new javax.swing.JPanel();
        previewPopup = new javax.swing.JDialog();
        previewPanel = new javax.swing.JPanel();
        editCheckDialog = new javax.swing.JDialog();
        jPanel9 = new javax.swing.JPanel();
        jPanel135 = new javax.swing.JPanel();
        jPanel136 = new javax.swing.JPanel();
        jPanel137 = new javax.swing.JPanel();
        jPanel37 = new javax.swing.JPanel();
        jPanel149 = new javax.swing.JPanel();
        jLabel163 = new javax.swing.JLabel();
        checkCombo1 = new javax.swing.JComboBox();
        jPanel190 = new javax.swing.JPanel();
        jLabel161 = new javax.swing.JLabel();
        checkMemo1 = new javax.swing.JTextField();
        checkTypeCombo1 = new javax.swing.JComboBox();
        checkbookPayto1 = new javax.swing.JComboBox();
        checkClass1 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel138 = new javax.swing.JPanel();
        jPanel191 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        depositsEndingBalance2 = new javax.swing.JFormattedTextField();
        jPanel192 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        checkNumber1 = new javax.swing.JFormattedTextField();
        jPanel193 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        checkDate1 = new com.toedter.calendar.JDateChooser();
        jPanel194 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        checkAmount1 = new javax.swing.JFormattedTextField();
        jPanel100 = new javax.swing.JPanel();
        depositsDeposit2 = new javax.swing.JRadioButton();
        depositsACH2 = new javax.swing.JRadioButton();
        jLabel41 = new javax.swing.JLabel();
        checkAccount1 = new javax.swing.JFormattedTextField();
        jLabel55 = new javax.swing.JLabel();
        jPanel101 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        checkAccountsTable1 = new javax.swing.JTable();
        jPanel113 = new javax.swing.JPanel();
        jPanel139 = new javax.swing.JPanel();
        saveDepositButton3 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        clearCheck4 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        checkbookEntryID4 = new javax.swing.JLabel();
        jPanel102 = new javax.swing.JPanel();
        jideTabbedPane4 = new com.jidesoft.swing.JideTabbedPane();
        apCheckbookPanel = new javax.swing.JPanel();
        jPanel154 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel115 = new javax.swing.JPanel();
        jPanel128 = new javax.swing.JPanel();
        jPanel123 = new javax.swing.JPanel();
        jPanel116 = new javax.swing.JPanel();
        jPanel145 = new javax.swing.JPanel();
        jLabel159 = new javax.swing.JLabel();
        checkbookBankCombo = new javax.swing.JComboBox();
        jPanel169 = new javax.swing.JPanel();
        jPanel170 = new javax.swing.JPanel();
        jLabel155 = new javax.swing.JLabel();
        checkbookMemo = new javax.swing.JTextField();
        jLabel153 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        checkTypeCombo = new javax.swing.JComboBox();
        billClass_checkbook = new com.jidesoft.swing.AutoCompletionComboBox();
        jLabel2 = new javax.swing.JLabel();
        checkbookPayto = new com.jidesoft.swing.AutoCompletionComboBox();
        jPanel122 = new javax.swing.JPanel();
        jPanel173 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        checkbookEndingBalance = new javax.swing.JFormattedTextField();
        jPanel172 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        checkBookNumber = new javax.swing.JFormattedTextField();
        jPanel174 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        checkbookDate = new com.toedter.calendar.JDateChooser();
        jPanel175 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        checkbookAmount = new javax.swing.JFormattedTextField();
        jPanel93 = new javax.swing.JPanel();
        checkbookCheck = new javax.swing.JRadioButton();
        checkbookAch = new javax.swing.JRadioButton();
        jPanel196 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        checksTotalAccounts = new javax.swing.JFormattedTextField();
        jLabel51 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel110 = new javax.swing.JPanel();
        jPanel119 = new javax.swing.JPanel();
        saveCheck = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        clearCheck = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        checkbookEntryID = new javax.swing.JLabel();
        jPanel90 = new javax.swing.JPanel();
        jPanel94 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        checkbookAccountsTable = new javax.swing.JTable();
        jPanel158 = new javax.swing.JPanel();
        jPanel117 = new javax.swing.JPanel();
        checkbookAllDates = new javax.swing.JCheckBox();
        jPanel159 = new javax.swing.JPanel();
        jLabel103 = new javax.swing.JLabel();
        jDateChooser12 = new com.toedter.calendar.JDateChooser();
        jLabel104 = new javax.swing.JLabel();
        jDateChooser13 = new com.toedter.calendar.JDateChooser();
        jButton31 = new javax.swing.JButton();
        checksSearchTextField = new javax.swing.JTextField();
        checksSearchButton = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        printCheckButton = new javax.swing.JButton();
        editCheckButton = new javax.swing.JButton();
        voidCheckButton = new javax.swing.JButton();
        deleteCheckButton = new javax.swing.JButton();
        jPanel39 = new javax.swing.JPanel();
        jScrollPane24 = new javax.swing.JScrollPane();
        checksBottomTable = new javax.swing.JTable();
        checksBottomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apDepositsPanel = new javax.swing.JPanel();
        jPanel155 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel118 = new javax.swing.JPanel();
        jPanel129 = new javax.swing.JPanel();
        jPanel124 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        jPanel146 = new javax.swing.JPanel();
        jLabel160 = new javax.swing.JLabel();
        depositsBankCombo = new javax.swing.JComboBox();
        jPanel176 = new javax.swing.JPanel();
        jLabel156 = new javax.swing.JLabel();
        depositsMemo = new javax.swing.JTextField();
        billClass_deposit = new com.jidesoft.swing.AutoCompletionComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel125 = new javax.swing.JPanel();
        jPanel177 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        depositsEndingBalance = new javax.swing.JFormattedTextField();
        jPanel178 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        depositsNumber = new javax.swing.JFormattedTextField();
        jPanel179 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        depositsDate = new com.toedter.calendar.JDateChooser();
        jPanel180 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        depositsAmount = new javax.swing.JFormattedTextField();
        jPanel95 = new javax.swing.JPanel();
        depositsDeposit = new javax.swing.JRadioButton();
        depositsACH = new javax.swing.JRadioButton();
        jPanel197 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        depositsTotalAccounts = new javax.swing.JFormattedTextField();
        jLabel52 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel111 = new javax.swing.JPanel();
        jPanel121 = new javax.swing.JPanel();
        saveDepositButton = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        clearDeposit = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        checkbookEntryID1 = new javax.swing.JLabel();
        jPanel91 = new javax.swing.JPanel();
        jPanel96 = new javax.swing.JPanel();
        jScrollPane20 = new javax.swing.JScrollPane();
        depositsAccountsTable = new javax.swing.JTable();
        jPanel160 = new javax.swing.JPanel();
        jPanel126 = new javax.swing.JPanel();
        depositsAllDates = new javax.swing.JCheckBox();
        jPanel161 = new javax.swing.JPanel();
        jLabel105 = new javax.swing.JLabel();
        jDateChooser14 = new com.toedter.calendar.JDateChooser();
        jLabel106 = new javax.swing.JLabel();
        jDateChooser15 = new com.toedter.calendar.JDateChooser();
        jButton32 = new javax.swing.JButton();
        depositsSearchTextField = new javax.swing.JTextField();
        depositsSearchButton = new javax.swing.JButton();
        jPanel24 = new javax.swing.JPanel();
        jPanel40 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jButton38 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        voidDepositBtn = new javax.swing.JButton();
        deleteDeposit = new javax.swing.JButton();
        jScrollPane25 = new javax.swing.JScrollPane();
        depositsBottomTable = new javax.swing.JTable();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dms.DMSApp.class).getContext().getResourceMap(BankingPanel.class);
        editDepositDialog1.setTitle(resourceMap.getString("editDepositDialog1.title")); // NOI18N
        editDepositDialog1.setBackground(resourceMap.getColor("editDepositDialog1.background")); // NOI18N
        editDepositDialog1.setForeground(resourceMap.getColor("editDepositDialog1.foreground")); // NOI18N
        editDepositDialog1.setModal(true);
        editDepositDialog1.setName("editDepositDialog1"); // NOI18N

        jPanel8.setBackground(resourceMap.getColor("jPanel8.background")); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setLayout(null);

        jPanel132.setBackground(resourceMap.getColor("jPanel132.background")); // NOI18N
        jPanel132.setMaximumSize(new java.awt.Dimension(800, 330));
        jPanel132.setName("jPanel132"); // NOI18N
        jPanel132.setOpaque(false);
        jPanel132.setLayout(new java.awt.BorderLayout());

        jPanel133.setMaximumSize(new java.awt.Dimension(800, 30));
        jPanel133.setMinimumSize(new java.awt.Dimension(800, 30));
        jPanel133.setName("jPanel133"); // NOI18N
        jPanel133.setOpaque(false);

        javax.swing.GroupLayout jPanel133Layout = new javax.swing.GroupLayout(jPanel133);
        jPanel133.setLayout(jPanel133Layout);
        jPanel133Layout.setHorizontalGroup(
            jPanel133Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel133Layout.setVerticalGroup(
            jPanel133Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel132.add(jPanel133, java.awt.BorderLayout.NORTH);

        jPanel134.setMaximumSize(new java.awt.Dimension(800, 300));
        jPanel134.setMinimumSize(new java.awt.Dimension(800, 300));
        jPanel134.setName("jPanel134"); // NOI18N
        jPanel134.setOpaque(false);
        jPanel134.setPreferredSize(new java.awt.Dimension(800, 300));
        jPanel134.setLayout(new java.awt.BorderLayout());

        jPanel36.setMaximumSize(new java.awt.Dimension(550, 330));
        jPanel36.setMinimumSize(new java.awt.Dimension(550, 330));
        jPanel36.setName("jPanel36"); // NOI18N
        jPanel36.setOpaque(false);

        jPanel148.setName("jPanel148"); // NOI18N
        jPanel148.setOpaque(false);
        jPanel148.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel162.setFont(getFont());
        jLabel162.setText("BANK ACCOUNT :"); // NOI18N
        jLabel162.setName("jLabel162"); // NOI18N
        jPanel148.add(jLabel162);

        depositsBankCombo2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0 -", "202 - Cash in Bank-BOA", "203 - Cash on hand" }));
        depositsBankCombo2.setSelectedIndex(1);
        depositsBankCombo2.setMaximumSize(new java.awt.Dimension(190, 20));
        depositsBankCombo2.setMinimumSize(new java.awt.Dimension(190, 20));
        depositsBankCombo2.setName("depositsBankCombo2"); // NOI18N
        depositsBankCombo2.setPreferredSize(new java.awt.Dimension(190, 20));
        depositsBankCombo2.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                depositsBankCombo2bankSelected(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        depositsBankCombo2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                depositsBankCombo2ItemStateChanged(evt);
            }
        });
        jPanel148.add(depositsBankCombo2);

        jPanel186.setName("jPanel186"); // NOI18N
        jPanel186.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel158.setFont(getFont());
        jLabel158.setText("MEMO :"); // NOI18N
        jLabel158.setName("jLabel158"); // NOI18N
        jPanel186.add(jLabel158);

        depositsMemo2.setMaximumSize(new java.awt.Dimension(350, 20));
        depositsMemo2.setMinimumSize(new java.awt.Dimension(350, 20));
        depositsMemo2.setName("depositsMemo2"); // NOI18N
        depositsMemo2.setPreferredSize(new java.awt.Dimension(350, 20));
        jPanel186.add(depositsMemo2);

        billClass_deposit1.setEditable(false);
        billClass_deposit1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", " " }));
        billClass_deposit1.setName("billClass_deposit1"); // NOI18N
        billClass_deposit1.setStrict(false);
        billClass_deposit1.setStrictCompletion(false);
        billClass_deposit1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                billClass_deposit1ItemStateChanged(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jLabel7.setFont(getFont());
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel186, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel148, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(billClass_deposit1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel36Layout.setVerticalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addComponent(jPanel148, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(billClass_deposit1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(39, 39, 39)
                .addComponent(jPanel186, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        jPanel134.add(jPanel36, java.awt.BorderLayout.WEST);

        jPanel131.setEnabled(false);
        jPanel131.setMaximumSize(new java.awt.Dimension(210, 100));
        jPanel131.setMinimumSize(new java.awt.Dimension(210, 100));
        jPanel131.setName("jPanel131"); // NOI18N
        jPanel131.setOpaque(false);

        jPanel185.setName("jPanel185"); // NOI18N
        jPanel185.setOpaque(false);

        jLabel32.setFont(getFont());
        jLabel32.setText(resourceMap.getString("jLabel32.text")); // NOI18N
        jLabel32.setName("jLabel32"); // NOI18N
        jPanel185.add(jLabel32);

        depositsEndingBalance1.setEditable(false);
        depositsEndingBalance1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("¤#,##0.00"))));
        depositsEndingBalance1.setMaximumSize(new java.awt.Dimension(80, 20));
        depositsEndingBalance1.setMinimumSize(new java.awt.Dimension(80, 20));
        depositsEndingBalance1.setName("depositsEndingBalance1"); // NOI18N
        depositsEndingBalance1.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel185.add(depositsEndingBalance1);

        jPanel187.setName("jPanel187"); // NOI18N
        jPanel187.setOpaque(false);

        jLabel34.setFont(getFont());
        jLabel34.setText(resourceMap.getString("jLabel34.text")); // NOI18N
        jLabel34.setName("jLabel34"); // NOI18N
        jPanel187.add(jLabel34);

        depositsNumber1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        depositsNumber1.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        depositsNumber1.setMinimumSize(new java.awt.Dimension(81, 20));
        depositsNumber1.setName("depositsNumber1"); // NOI18N
        depositsNumber1.setPreferredSize(new java.awt.Dimension(81, 20));
        depositsNumber1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                depositsNumber1FocusLost(evt);
            }
        });
        depositsNumber1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                depositsNumber1InputMethodTextChanged(evt);
            }
        });
        depositsNumber1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                depositsNumber1KeyReleased(evt);
            }
        });
        jPanel187.add(depositsNumber1);

        jPanel188.setName("jPanel188"); // NOI18N
        jPanel188.setOpaque(false);

        jLabel35.setFont(getFont());
        jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
        jLabel35.setName("jLabel35"); // NOI18N
        jPanel188.add(jLabel35);

        depositsDate1.setDateFormatString(resourceMap.getString("depositsDate1.dateFormatString")); // NOI18N
        depositsDate1.setMaximumSize(new java.awt.Dimension(99, 2147483647));
        depositsDate1.setMinimumSize(new java.awt.Dimension(99, 20));
        depositsDate1.setName("depositsDate1"); // NOI18N
        depositsDate1.setPreferredSize(new java.awt.Dimension(99, 20));
        jPanel188.add(depositsDate1);

        jPanel189.setName("jPanel189"); // NOI18N
        jPanel189.setOpaque(false);

        jLabel36.setFont(getFont());
        jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N
        jPanel189.add(jLabel36);

        depositsAmount1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        depositsAmount1.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        depositsAmount1.setMinimumSize(new java.awt.Dimension(81, 20));
        depositsAmount1.setName("depositsAmount1"); // NOI18N
        depositsAmount1.setPreferredSize(new java.awt.Dimension(81, 20));
        depositsAmount1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositsAmount1ActionPerformed(evt);
            }
        });
        jPanel189.add(depositsAmount1);

        jPanel99.setName("jPanel99"); // NOI18N
        jPanel99.setOpaque(false);
        jPanel99.setLayout(new java.awt.GridLayout(1, 0));

        buttonGroup1.add(depositsDeposit1);
        depositsDeposit1.setFont(getFont());
        depositsDeposit1.setText(resourceMap.getString("depositsDeposit1.text")); // NOI18N
        depositsDeposit1.setName("depositsDeposit1"); // NOI18N
        depositsDeposit1.setOpaque(false);
        depositsDeposit1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsDeposit1MouseClicked(evt);
            }
        });
        jPanel99.add(depositsDeposit1);

        buttonGroup1.add(depositsACH1);
        depositsACH1.setFont(getFont());
        depositsACH1.setText(resourceMap.getString("depositsACH1.text")); // NOI18N
        depositsACH1.setName("depositsACH1"); // NOI18N
        depositsACH1.setOpaque(false);
        depositsACH1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsACH1MouseClicked(evt);
            }
        });
        jPanel99.add(depositsACH1);

        jPanel199.setName("jPanel199"); // NOI18N
        jPanel199.setOpaque(false);

        jLabel37.setFont(getFont());
        jLabel37.setText(resourceMap.getString("jLabel37.text")); // NOI18N
        jLabel37.setName("jLabel37"); // NOI18N
        jPanel199.add(jLabel37);

        depositsTotalAccounts1.setEditable(false);
        depositsTotalAccounts1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        depositsTotalAccounts1.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        depositsTotalAccounts1.setMinimumSize(new java.awt.Dimension(81, 20));
        depositsTotalAccounts1.setName("depositsTotalAccounts1"); // NOI18N
        depositsTotalAccounts1.setPreferredSize(new java.awt.Dimension(81, 20));
        depositsTotalAccounts1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositsTotalAccounts1ActionPerformed(evt);
            }
        });
        jPanel199.add(depositsTotalAccounts1);

        javax.swing.GroupLayout jPanel131Layout = new javax.swing.GroupLayout(jPanel131);
        jPanel131.setLayout(jPanel131Layout);
        jPanel131Layout.setHorizontalGroup(
            jPanel131Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel131Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel131Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel131Layout.createSequentialGroup()
                        .addComponent(jPanel187, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel131Layout.createSequentialGroup()
                        .addComponent(jPanel188, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel131Layout.createSequentialGroup()
                        .addComponent(jPanel189, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))))
            .addGroup(jPanel131Layout.createSequentialGroup()
                .addGroup(jPanel131Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel185, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                    .addComponent(jPanel99, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel131Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel131Layout.createSequentialGroup()
                    .addContainerGap(27, Short.MAX_VALUE)
                    .addComponent(jPanel199, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(11, 11, 11)))
        );
        jPanel131Layout.setVerticalGroup(
            jPanel131Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel131Layout.createSequentialGroup()
                .addComponent(jPanel185, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel99, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel187, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel188, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel189, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 126, Short.MAX_VALUE))
            .addGroup(jPanel131Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel131Layout.createSequentialGroup()
                    .addContainerGap(186, Short.MAX_VALUE)
                    .addComponent(jPanel199, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(84, 84, 84)))
        );

        jPanel134.add(jPanel131, java.awt.BorderLayout.CENTER);

        jPanel132.add(jPanel134, java.awt.BorderLayout.CENTER);

        jPanel8.add(jPanel132);
        jPanel132.setBounds(0, 0, 790, 330);

        jLabel54.setIcon(resourceMap.getIcon("jLabel51.icon")); // NOI18N
        jLabel54.setMaximumSize(new java.awt.Dimension(800, 330));
        jLabel54.setMinimumSize(new java.awt.Dimension(800, 330));
        jLabel54.setName("jLabel54"); // NOI18N
        jLabel54.setPreferredSize(new java.awt.Dimension(800, 330));
        jPanel8.add(jLabel54);
        jLabel54.setBounds(0, 0, 800, 330);

        jPanel97.setBackground(resourceMap.getColor("jPanel97.background")); // NOI18N
        jPanel97.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel97.setMinimumSize(new java.awt.Dimension(1127, 205));
        jPanel97.setName("jPanel97"); // NOI18N
        jPanel97.setOpaque(false);
        jPanel97.setPreferredSize(new java.awt.Dimension(1127, 205));
        jPanel97.setLayout(new javax.swing.BoxLayout(jPanel97, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        depositsAccountsTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Account", "Amount", "Control #", "Reference #", "Memo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        depositsAccountsTable1.setName("depositsAccountsTable1"); // NOI18N
        depositsAccountsTable1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        depositsAccountsTable1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                depositsAccountsTable1FocusGained(evt);
            }
        });
        jScrollPane2.setViewportView(depositsAccountsTable1);

        jPanel97.add(jScrollPane2);

        jPanel112.setBackground(resourceMap.getColor("jPanel112.background")); // NOI18N
        jPanel112.setName("jPanel112"); // NOI18N
        jPanel112.setLayout(null);

        jPanel130.setName("jPanel130"); // NOI18N
        jPanel130.setOpaque(false);

        saveDepositButton2.setText(resourceMap.getString("saveDepositButton2.text")); // NOI18N
        saveDepositButton2.setMaximumSize(new java.awt.Dimension(135, 23));
        saveDepositButton2.setMinimumSize(new java.awt.Dimension(135, 23));
        saveDepositButton2.setName("saveDepositButton2"); // NOI18N
        saveDepositButton2.setPreferredSize(new java.awt.Dimension(135, 23));
        saveDepositButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveDepositButton2(evt);
            }
        });
        jPanel130.add(saveDepositButton2);

        jButton50.setText(resourceMap.getString("jButton50.text")); // NOI18N
        jButton50.setMaximumSize(new java.awt.Dimension(135, 23));
        jButton50.setMinimumSize(new java.awt.Dimension(135, 23));
        jButton50.setName("jButton50"); // NOI18N
        jButton50.setPreferredSize(new java.awt.Dimension(135, 23));
        jButton50.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton50saveCheckButtonsClicked(evt);
            }
        });
        jPanel130.add(jButton50);

        clearCheck3.setText(resourceMap.getString("clearCheck3.text")); // NOI18N
        clearCheck3.setMaximumSize(new java.awt.Dimension(135, 23));
        clearCheck3.setMinimumSize(new java.awt.Dimension(135, 23));
        clearCheck3.setName("clearCheck3"); // NOI18N
        clearCheck3.setPreferredSize(new java.awt.Dimension(135, 23));
        clearCheck3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearCheck3(evt);
            }
        });
        jPanel130.add(clearCheck3);

        jButton26.setText(resourceMap.getString("jButton26.text")); // NOI18N
        jButton26.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton26.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton26.setName("jButton26"); // NOI18N
        jButton26.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });
        jPanel130.add(jButton26);

        jButton27.setText(resourceMap.getString("jButton27.text")); // NOI18N
        jButton27.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton27.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton27.setName("jButton27"); // NOI18N
        jButton27.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });
        jPanel130.add(jButton27);

        jPanel112.add(jPanel130);
        jPanel130.setBounds(30, 180, 250, 115);

        checkbookEntryID3.setName("checkbookEntryID3"); // NOI18N
        jPanel112.add(checkbookEntryID3);
        checkbookEntryID3.setBounds(820, 110, 250, 215);

        jPanel98.setName("jPanel98"); // NOI18N
        jPanel98.setLayout(new javax.swing.BoxLayout(jPanel98, javax.swing.BoxLayout.LINE_AXIS));
        jPanel112.add(jPanel98);
        jPanel98.setBounds(0, 330, 250, 0);

        javax.swing.GroupLayout editDepositDialog1Layout = new javax.swing.GroupLayout(editDepositDialog1.getContentPane());
        editDepositDialog1.getContentPane().setLayout(editDepositDialog1Layout);
        editDepositDialog1Layout.setHorizontalGroup(
            editDepositDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editDepositDialog1Layout.createSequentialGroup()
                .addGap(0, 829, Short.MAX_VALUE)
                .addComponent(jPanel112, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(editDepositDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(editDepositDialog1Layout.createSequentialGroup()
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 840, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 295, Short.MAX_VALUE)))
            .addGroup(editDepositDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel97, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1135, Short.MAX_VALUE))
        );
        editDepositDialog1Layout.setVerticalGroup(
            editDepositDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editDepositDialog1Layout.createSequentialGroup()
                .addComponent(jPanel112, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 242, Short.MAX_VALUE))
            .addGroup(editDepositDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(editDepositDialog1Layout.createSequentialGroup()
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 242, Short.MAX_VALUE)))
            .addGroup(editDepositDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editDepositDialog1Layout.createSequentialGroup()
                    .addContainerGap(331, Short.MAX_VALUE)
                    .addComponent(jPanel97, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        previewPopup.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        previewPopup.setTitle(resourceMap.getString("previewPopup.title")); // NOI18N
        previewPopup.setLocationByPlatform(true);
        previewPopup.setModal(true);
        previewPopup.setName("previewPopup"); // NOI18N
        previewPopup.setResizable(false);
        previewPopup.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        previewPanel.setName("previewPanel"); // NOI18N
        previewPanel.setLayout(new java.awt.GridLayout(1, 0));
        previewPopup.getContentPane().add(previewPanel);

        editCheckDialog.setTitle(resourceMap.getString("editCheckDialog.title")); // NOI18N
        editCheckDialog.setBackground(resourceMap.getColor("editCheckDialog.background")); // NOI18N
        editCheckDialog.setLocationByPlatform(true);
        editCheckDialog.setMaximumSize(new java.awt.Dimension(1120, 600));
        editCheckDialog.setMinimumSize(new java.awt.Dimension(1120, 600));
        editCheckDialog.setModal(true);
        editCheckDialog.setName("editCheckDialog"); // NOI18N
        editCheckDialog.setPreferredSize(new java.awt.Dimension(1120, 600));
        editCheckDialog.setResizable(false);

        jPanel9.setBackground(resourceMap.getColor("jPanel9.background")); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(null);

        jPanel135.setMaximumSize(new java.awt.Dimension(800, 330));
        jPanel135.setName("jPanel135"); // NOI18N
        jPanel135.setOpaque(false);
        jPanel135.setLayout(new java.awt.BorderLayout());

        jPanel136.setMaximumSize(new java.awt.Dimension(800, 30));
        jPanel136.setMinimumSize(new java.awt.Dimension(800, 30));
        jPanel136.setName("jPanel136"); // NOI18N
        jPanel136.setOpaque(false);

        javax.swing.GroupLayout jPanel136Layout = new javax.swing.GroupLayout(jPanel136);
        jPanel136.setLayout(jPanel136Layout);
        jPanel136Layout.setHorizontalGroup(
            jPanel136Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel136Layout.setVerticalGroup(
            jPanel136Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel135.add(jPanel136, java.awt.BorderLayout.NORTH);

        jPanel137.setMaximumSize(new java.awt.Dimension(800, 300));
        jPanel137.setMinimumSize(new java.awt.Dimension(800, 300));
        jPanel137.setName("jPanel137"); // NOI18N
        jPanel137.setOpaque(false);
        jPanel137.setPreferredSize(new java.awt.Dimension(800, 300));
        jPanel137.setLayout(new java.awt.BorderLayout());

        jPanel37.setMaximumSize(new java.awt.Dimension(550, 330));
        jPanel37.setMinimumSize(new java.awt.Dimension(550, 330));
        jPanel37.setName("jPanel37"); // NOI18N
        jPanel37.setOpaque(false);

        jPanel149.setName("jPanel149"); // NOI18N
        jPanel149.setOpaque(false);
        jPanel149.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel163.setFont(getFont());
        jLabel163.setText("BANK ACCOUNT :"); // NOI18N
        jLabel163.setName("jLabel163"); // NOI18N
        jPanel149.add(jLabel163);

        checkCombo1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0 -", "202 - Cash in Bank-BOA", "203 - Cash on hand" }));
        checkCombo1.setSelectedIndex(1);
        checkCombo1.setMaximumSize(new java.awt.Dimension(190, 20));
        checkCombo1.setMinimumSize(new java.awt.Dimension(190, 20));
        checkCombo1.setName("checkCombo1"); // NOI18N
        checkCombo1.setPreferredSize(new java.awt.Dimension(190, 20));
        checkCombo1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkCombo1bankSelected(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        checkCombo1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkCombo1ItemStateChanged(evt);
            }
        });
        jPanel149.add(checkCombo1);

        jPanel190.setName("jPanel190"); // NOI18N
        jPanel190.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel161.setFont(getFont());
        jLabel161.setText("MEMO :"); // NOI18N
        jLabel161.setName("jLabel161"); // NOI18N
        jPanel190.add(jLabel161);

        checkMemo1.setMaximumSize(new java.awt.Dimension(350, 20));
        checkMemo1.setMinimumSize(new java.awt.Dimension(350, 20));
        checkMemo1.setName("checkMemo1"); // NOI18N
        checkMemo1.setPreferredSize(new java.awt.Dimension(350, 20));
        jPanel190.add(checkMemo1);

        checkTypeCombo1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Vendor", "Customer", "Employee" }));
        checkTypeCombo1.setName("checkTypeCombo1"); // NOI18N
        checkTypeCombo1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkTypeCombo1ItemStateChanged(evt);
            }
        });

        checkbookPayto1.setName("checkbookPayto1"); // NOI18N

        checkClass1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin" }));
        checkClass1.setName("checkClass1"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37.setLayout(jPanel37Layout);
        jPanel37Layout.setHorizontalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel37Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel190, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel149, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel37Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(39, 39, 39)
                        .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(checkTypeCombo1, 0, 188, Short.MAX_VALUE)
                            .addComponent(checkbookPayto1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(checkClass1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel37Layout.setVerticalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel37Layout.createSequentialGroup()
                        .addComponent(jPanel149, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkTypeCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addGap(26, 26, 26)
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkbookPayto1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(26, 26, 26)
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkClass1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addComponent(jPanel190, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        jPanel137.add(jPanel37, java.awt.BorderLayout.WEST);

        jPanel138.setEnabled(false);
        jPanel138.setMaximumSize(new java.awt.Dimension(210, 100));
        jPanel138.setMinimumSize(new java.awt.Dimension(210, 100));
        jPanel138.setName("jPanel138"); // NOI18N
        jPanel138.setOpaque(false);

        jPanel191.setName("jPanel191"); // NOI18N
        jPanel191.setOpaque(false);

        jLabel33.setFont(getFont());
        jLabel33.setText(resourceMap.getString("jLabel33.text")); // NOI18N
        jLabel33.setName("jLabel33"); // NOI18N
        jPanel191.add(jLabel33);

        depositsEndingBalance2.setEditable(false);
        depositsEndingBalance2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("¤#,##0.00"))));
        depositsEndingBalance2.setMaximumSize(new java.awt.Dimension(80, 20));
        depositsEndingBalance2.setMinimumSize(new java.awt.Dimension(80, 20));
        depositsEndingBalance2.setName("depositsEndingBalance2"); // NOI18N
        depositsEndingBalance2.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel191.add(depositsEndingBalance2);

        jPanel192.setName("jPanel192"); // NOI18N
        jPanel192.setOpaque(false);

        jLabel38.setFont(getFont());
        jLabel38.setText(resourceMap.getString("jLabel38.text")); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N
        jPanel192.add(jLabel38);

        checkNumber1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        checkNumber1.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        checkNumber1.setMinimumSize(new java.awt.Dimension(81, 20));
        checkNumber1.setName("checkNumber1"); // NOI18N
        checkNumber1.setPreferredSize(new java.awt.Dimension(81, 20));
        checkNumber1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkNumber1FocusLost(evt);
            }
        });
        checkNumber1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                checkNumber1InputMethodTextChanged(evt);
            }
        });
        checkNumber1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkNumber1KeyReleased(evt);
            }
        });
        jPanel192.add(checkNumber1);

        jPanel193.setName("jPanel193"); // NOI18N
        jPanel193.setOpaque(false);

        jLabel39.setFont(getFont());
        jLabel39.setText(resourceMap.getString("jLabel39.text")); // NOI18N
        jLabel39.setToolTipText(resourceMap.getString("jLabel39.toolTipText")); // NOI18N
        jLabel39.setName("jLabel39"); // NOI18N
        jPanel193.add(jLabel39);

        checkDate1.setDateFormatString(resourceMap.getString("depositsDate1.dateFormatString")); // NOI18N
        checkDate1.setMaximumSize(new java.awt.Dimension(99, 2147483647));
        checkDate1.setMinimumSize(new java.awt.Dimension(99, 20));
        checkDate1.setName("checkDate1"); // NOI18N
        checkDate1.setPreferredSize(new java.awt.Dimension(99, 20));
        jPanel193.add(checkDate1);

        jPanel194.setName("jPanel194"); // NOI18N
        jPanel194.setOpaque(false);

        jLabel40.setFont(getFont());
        jLabel40.setText(resourceMap.getString("jLabel40.text")); // NOI18N
        jLabel40.setName("jLabel40"); // NOI18N
        jPanel194.add(jLabel40);

        checkAmount1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        checkAmount1.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        checkAmount1.setMinimumSize(new java.awt.Dimension(81, 20));
        checkAmount1.setName("checkAmount1"); // NOI18N
        checkAmount1.setPreferredSize(new java.awt.Dimension(81, 20));
        checkAmount1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAmount1ActionPerformed(evt);
            }
        });
        jPanel194.add(checkAmount1);

        jPanel100.setName("jPanel100"); // NOI18N
        jPanel100.setOpaque(false);
        jPanel100.setLayout(new java.awt.GridLayout(1, 0));

        buttonGroup1.add(depositsDeposit2);
        depositsDeposit2.setFont(getFont());
        depositsDeposit2.setText(resourceMap.getString("depositsDeposit2.text")); // NOI18N
        depositsDeposit2.setName("depositsDeposit2"); // NOI18N
        depositsDeposit2.setOpaque(false);
        depositsDeposit2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsDeposit2MouseClicked(evt);
            }
        });
        jPanel100.add(depositsDeposit2);

        buttonGroup1.add(depositsACH2);
        depositsACH2.setFont(getFont());
        depositsACH2.setText(resourceMap.getString("depositsACH2.text")); // NOI18N
        depositsACH2.setName("depositsACH2"); // NOI18N
        depositsACH2.setOpaque(false);
        depositsACH2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsACH2MouseClicked(evt);
            }
        });
        jPanel100.add(depositsACH2);

        jLabel41.setFont(getFont());
        jLabel41.setText(resourceMap.getString("jLabel41.text")); // NOI18N
        jLabel41.setName("jLabel41"); // NOI18N

        checkAccount1.setEditable(false);
        checkAccount1.setText(resourceMap.getString("checkAccount1.text")); // NOI18N
        checkAccount1.setName("checkAccount1"); // NOI18N

        javax.swing.GroupLayout jPanel138Layout = new javax.swing.GroupLayout(jPanel138);
        jPanel138.setLayout(jPanel138Layout);
        jPanel138Layout.setHorizontalGroup(
            jPanel138Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel138Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel138Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel138Layout.createSequentialGroup()
                        .addComponent(jPanel192, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel138Layout.createSequentialGroup()
                        .addComponent(jPanel193, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel138Layout.createSequentialGroup()
                        .addComponent(jPanel194, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel138Layout.createSequentialGroup()
                        .addComponent(jLabel41)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkAccount1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34))))
            .addGroup(jPanel138Layout.createSequentialGroup()
                .addGroup(jPanel138Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel191, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                    .addComponent(jPanel100, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel138Layout.setVerticalGroup(
            jPanel138Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel138Layout.createSequentialGroup()
                .addComponent(jPanel191, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel192, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel193, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel194, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel138Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(checkAccount1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 88, Short.MAX_VALUE))
        );

        jPanel137.add(jPanel138, java.awt.BorderLayout.CENTER);

        jPanel135.add(jPanel137, java.awt.BorderLayout.CENTER);

        jPanel9.add(jPanel135);
        jPanel135.setBounds(0, 0, 790, 330);

        jLabel55.setIcon(resourceMap.getIcon("jLabel51.icon")); // NOI18N
        jLabel55.setMaximumSize(new java.awt.Dimension(800, 330));
        jLabel55.setMinimumSize(new java.awt.Dimension(800, 330));
        jLabel55.setName("jLabel55"); // NOI18N
        jLabel55.setPreferredSize(new java.awt.Dimension(800, 330));
        jPanel9.add(jLabel55);
        jLabel55.setBounds(0, 0, 800, 330);

        jPanel101.setBackground(resourceMap.getColor("jPanel101.background")); // NOI18N
        jPanel101.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel101.setMinimumSize(new java.awt.Dimension(1127, 205));
        jPanel101.setName("jPanel101"); // NOI18N
        jPanel101.setPreferredSize(new java.awt.Dimension(1127, 205));
        jPanel101.setLayout(new javax.swing.BoxLayout(jPanel101, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        checkAccountsTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Account", "Amount", "Control #", "Reference #", "Memo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        checkAccountsTable1.setCellSelectionEnabled(true);
        checkAccountsTable1.setName("checkAccountsTable1"); // NOI18N
        checkAccountsTable1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        checkAccountsTable1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                checkAccountsTable1FocusGained(evt);
            }
        });
        checkAccountsTable1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                checkAccountsTable1InputMethodTextChanged(evt);
            }
        });
        jScrollPane3.setViewportView(checkAccountsTable1);
        checkAccountsTable1.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("checkAccountsTable1.columnModel.title0")); // NOI18N
        checkAccountsTable1.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("checkAccountsTable1.columnModel.title1")); // NOI18N
        checkAccountsTable1.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("checkAccountsTable1.columnModel.title2")); // NOI18N
        checkAccountsTable1.getColumnModel().getColumn(3).setMinWidth(100);
        checkAccountsTable1.getColumnModel().getColumn(3).setPreferredWidth(100);
        checkAccountsTable1.getColumnModel().getColumn(3).setMaxWidth(100);
        checkAccountsTable1.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("checkAccountsTable1.columnModel.title3")); // NOI18N
        checkAccountsTable1.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("checkAccountsTable1.columnModel.title4")); // NOI18N

        jPanel101.add(jScrollPane3);

        jPanel113.setBackground(resourceMap.getColor("jPanel113.background")); // NOI18N
        jPanel113.setName("jPanel113"); // NOI18N
        jPanel113.setLayout(null);

        jPanel139.setName("jPanel139"); // NOI18N
        jPanel139.setOpaque(false);

        saveDepositButton3.setLabel(resourceMap.getString("saveDepositButton3.label")); // NOI18N
        saveDepositButton3.setMaximumSize(new java.awt.Dimension(135, 23));
        saveDepositButton3.setMinimumSize(new java.awt.Dimension(135, 23));
        saveDepositButton3.setName("saveDepositButton3"); // NOI18N
        saveDepositButton3.setPreferredSize(new java.awt.Dimension(135, 23));
        saveDepositButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveDepositButton3(evt);
            }
        });
        jPanel139.add(saveDepositButton3);

        jButton51.setLabel(resourceMap.getString("jButton51.label")); // NOI18N
        jButton51.setMaximumSize(new java.awt.Dimension(135, 23));
        jButton51.setMinimumSize(new java.awt.Dimension(135, 23));
        jButton51.setName("jButton51"); // NOI18N
        jButton51.setPreferredSize(new java.awt.Dimension(135, 23));
        jButton51.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton51saveCheckButtonsClicked(evt);
            }
        });
        jPanel139.add(jButton51);

        clearCheck4.setLabel(resourceMap.getString("clearCheck4.label")); // NOI18N
        clearCheck4.setMaximumSize(new java.awt.Dimension(135, 23));
        clearCheck4.setMinimumSize(new java.awt.Dimension(135, 23));
        clearCheck4.setName("clearCheck4"); // NOI18N
        clearCheck4.setPreferredSize(new java.awt.Dimension(135, 23));
        clearCheck4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearCheck4(evt);
            }
        });
        jPanel139.add(clearCheck4);

        jButton28.setLabel(resourceMap.getString("jButton28.label")); // NOI18N
        jButton28.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton28.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton28.setName("jButton28"); // NOI18N
        jButton28.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });
        jPanel139.add(jButton28);

        jButton29.setLabel(resourceMap.getString("jButton29.label")); // NOI18N
        jButton29.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton29.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton29.setName("jButton29"); // NOI18N
        jButton29.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });
        jPanel139.add(jButton29);

        jPanel113.add(jPanel139);
        jPanel139.setBounds(30, 180, 250, 115);

        checkbookEntryID4.setName("checkbookEntryID4"); // NOI18N
        jPanel113.add(checkbookEntryID4);
        checkbookEntryID4.setBounds(820, 110, 250, 215);

        jPanel102.setName("jPanel102"); // NOI18N
        jPanel102.setLayout(new javax.swing.BoxLayout(jPanel102, javax.swing.BoxLayout.LINE_AXIS));
        jPanel113.add(jPanel102);
        jPanel102.setBounds(0, 330, 250, 0);

        javax.swing.GroupLayout editCheckDialogLayout = new javax.swing.GroupLayout(editCheckDialog.getContentPane());
        editCheckDialog.getContentPane().setLayout(editCheckDialogLayout);
        editCheckDialogLayout.setHorizontalGroup(
            editCheckDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editCheckDialogLayout.createSequentialGroup()
                .addGap(0, 794, Short.MAX_VALUE)
                .addComponent(jPanel113, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(editCheckDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(editCheckDialogLayout.createSequentialGroup()
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 307, Short.MAX_VALUE)))
            .addGroup(editCheckDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel101, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        editCheckDialogLayout.setVerticalGroup(
            editCheckDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editCheckDialogLayout.createSequentialGroup()
                .addComponent(jPanel113, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 249, Short.MAX_VALUE))
            .addGroup(editCheckDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(editCheckDialogLayout.createSequentialGroup()
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 248, Short.MAX_VALUE)))
            .addGroup(editCheckDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editCheckDialogLayout.createSequentialGroup()
                    .addContainerGap(376, Short.MAX_VALUE)
                    .addComponent(jPanel101, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setFont(resourceMap.getFont("Form.font")); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jideTabbedPane4.setBoldActiveTab(true);
        jideTabbedPane4.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jideTabbedPane4.setName("jideTabbedPane4"); // NOI18N
        jideTabbedPane4.setSelectedTabFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jideTabbedPane4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jideTabbedPane4MouseClicked(evt);
            }
        });
        jideTabbedPane4.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jideTabbedPane4ComponentShown(evt);
            }
        });

        apCheckbookPanel.setName("apCheckbookPanel"); // NOI18N
        apCheckbookPanel.setOpaque(false);
        apCheckbookPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                apCheckbookPanelComponentShown(evt);
            }
        });
        apCheckbookPanel.setLayout(new java.awt.BorderLayout());

        jPanel154.setAutoscrolls(true);
        jPanel154.setMaximumSize(new java.awt.Dimension(1050, 330));
        jPanel154.setMinimumSize(new java.awt.Dimension(1050, 330));
        jPanel154.setName("jPanel154"); // NOI18N
        jPanel154.setOpaque(false);
        jPanel154.setPreferredSize(new java.awt.Dimension(1050, 330));
        jPanel154.setLayout(new java.awt.GridBagLayout());

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setOpaque(false);
        jPanel2.setLayout(null);

        jPanel115.setMaximumSize(new java.awt.Dimension(800, 330));
        jPanel115.setName("jPanel115"); // NOI18N
        jPanel115.setOpaque(false);
        jPanel115.setLayout(new java.awt.BorderLayout());

        jPanel128.setMaximumSize(new java.awt.Dimension(800, 30));
        jPanel128.setMinimumSize(new java.awt.Dimension(800, 30));
        jPanel128.setName("jPanel128"); // NOI18N
        jPanel128.setOpaque(false);

        javax.swing.GroupLayout jPanel128Layout = new javax.swing.GroupLayout(jPanel128);
        jPanel128.setLayout(jPanel128Layout);
        jPanel128Layout.setHorizontalGroup(
            jPanel128Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel128Layout.setVerticalGroup(
            jPanel128Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel115.add(jPanel128, java.awt.BorderLayout.NORTH);

        jPanel123.setMaximumSize(new java.awt.Dimension(800, 300));
        jPanel123.setMinimumSize(new java.awt.Dimension(800, 300));
        jPanel123.setName("jPanel123"); // NOI18N
        jPanel123.setOpaque(false);
        jPanel123.setPreferredSize(new java.awt.Dimension(800, 300));
        jPanel123.setLayout(new java.awt.BorderLayout());

        jPanel116.setMaximumSize(new java.awt.Dimension(550, 330));
        jPanel116.setMinimumSize(new java.awt.Dimension(550, 330));
        jPanel116.setName("jPanel116"); // NOI18N
        jPanel116.setOpaque(false);

        jPanel145.setName("jPanel145"); // NOI18N
        jPanel145.setOpaque(false);
        jPanel145.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel159.setFont(getFont());
        jLabel159.setText("BANK ACCOUNT :"); // NOI18N
        jLabel159.setName("jLabel159"); // NOI18N
        jPanel145.add(jLabel159);

        checkbookBankCombo.setMaximumSize(new java.awt.Dimension(190, 20));
        checkbookBankCombo.setMinimumSize(new java.awt.Dimension(190, 20));
        checkbookBankCombo.setName("checkbookBankCombo"); // NOI18N
        checkbookBankCombo.setPreferredSize(new java.awt.Dimension(190, 20));
        checkbookBankCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkbookBankCombobankSelected(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        checkbookBankCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkbookBankComboItemStateChanged(evt);
            }
        });
        jPanel145.add(checkbookBankCombo);

        jPanel169.setMaximumSize(new java.awt.Dimension(400, 100));
        jPanel169.setMinimumSize(new java.awt.Dimension(400, 100));
        jPanel169.setName("jPanel169"); // NOI18N
        jPanel169.setOpaque(false);
        jPanel169.setPreferredSize(new java.awt.Dimension(400, 100));
        jPanel169.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel170.setName("jPanel170"); // NOI18N
        jPanel170.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel155.setFont(getFont());
        jLabel155.setText("MEMO :"); // NOI18N
        jLabel155.setName("jLabel155"); // NOI18N
        jPanel170.add(jLabel155);

        checkbookMemo.setMaximumSize(new java.awt.Dimension(350, 20));
        checkbookMemo.setMinimumSize(new java.awt.Dimension(350, 20));
        checkbookMemo.setName("checkbookMemo"); // NOI18N
        checkbookMemo.setPreferredSize(new java.awt.Dimension(350, 20));
        jPanel170.add(checkbookMemo);

        jLabel153.setFont(getFont());
        jLabel153.setText("PAY TO :"); // NOI18N
        jLabel153.setName("jLabel153"); // NOI18N
        jLabel153.setRequestFocusEnabled(false);

        jLabel1.setFont(getFont());
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(resourceMap.getString("jLabel1.toolTipText")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        checkTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Vendor", "Customer", "Employee" }));
        checkTypeCombo.setName("checkTypeCombo"); // NOI18N
        checkTypeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkTypeComboItemStateChanged(evt);
            }
        });

        billClass_checkbook.setEditable(false);
        billClass_checkbook.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", "" }));
        billClass_checkbook.setName("billClass_checkbook"); // NOI18N
        billClass_checkbook.setStrict(false);
        billClass_checkbook.setStrictCompletion(false);
        billClass_checkbook.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                billClass_checkbookItemStateChanged(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jLabel2.setFont(getFont());
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        checkbookPayto.setEditable(false);
        checkbookPayto.setMaximumSize(new java.awt.Dimension(350, 20));
        checkbookPayto.setMinimumSize(new java.awt.Dimension(350, 20));
        checkbookPayto.setName("checkbookPayto"); // NOI18N
        checkbookPayto.setPreferredSize(new java.awt.Dimension(350, 20));
        checkbookPayto.setStrict(false);
        checkbookPayto.setStrictCompletion(false);
        checkbookPayto.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkbookPaytoPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        checkbookPayto.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkbookPaytoItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel116Layout = new javax.swing.GroupLayout(jPanel116);
        jPanel116.setLayout(jPanel116Layout);
        jPanel116Layout.setHorizontalGroup(
            jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel116Layout.createSequentialGroup()
                .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel116Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel153)
                            .addGroup(jPanel116Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(checkTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(checkbookPayto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel116Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel170, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jPanel169, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel116Layout.createSequentialGroup()
                                .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel116Layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(billClass_checkbook, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jPanel145, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel116Layout.setVerticalGroup(
            jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel116Layout.createSequentialGroup()
                .addComponent(jPanel145, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(checkTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel153)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel116Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel169, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(checkbookPayto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(billClass_checkbook, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel170, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(70, 70, 70))
        );

        jPanel123.add(jPanel116, java.awt.BorderLayout.WEST);

        jPanel122.setEnabled(false);
        jPanel122.setMaximumSize(new java.awt.Dimension(200, 100));
        jPanel122.setName("jPanel122"); // NOI18N
        jPanel122.setOpaque(false);
        jPanel122.setPreferredSize(new java.awt.Dimension(200, 100));

        jPanel173.setName("jPanel173"); // NOI18N
        jPanel173.setOpaque(false);

        jLabel18.setFont(getFont());
        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N
        jPanel173.add(jLabel18);

        checkbookEndingBalance.setEditable(false);
        checkbookEndingBalance.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("¤#,##0.00"))));
        checkbookEndingBalance.setMaximumSize(new java.awt.Dimension(80, 20));
        checkbookEndingBalance.setMinimumSize(new java.awt.Dimension(80, 20));
        checkbookEndingBalance.setName("checkbookEndingBalance"); // NOI18N
        checkbookEndingBalance.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel173.add(checkbookEndingBalance);

        jPanel172.setName("jPanel172"); // NOI18N
        jPanel172.setOpaque(false);

        jLabel19.setFont(getFont());
        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N
        jPanel172.add(jLabel19);

        checkBookNumber.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        checkBookNumber.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        checkBookNumber.setMinimumSize(new java.awt.Dimension(81, 20));
        checkBookNumber.setName("checkBookNumber"); // NOI18N
        checkBookNumber.setPreferredSize(new java.awt.Dimension(81, 20));
        checkBookNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkBookNumberFocusLost(evt);
            }
        });
        checkBookNumber.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                checkBookNumberInputMethodTextChanged(evt);
            }
        });
        checkBookNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checkBookNumberKeyReleased(evt);
            }
        });
        jPanel172.add(checkBookNumber);

        jPanel174.setName("jPanel174"); // NOI18N
        jPanel174.setOpaque(false);

        jLabel20.setFont(getFont());
        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N
        jPanel174.add(jLabel20);

        checkbookDate.setDateFormatString(resourceMap.getString("checkbookDate.dateFormatString")); // NOI18N
        checkbookDate.setMaximumSize(new java.awt.Dimension(99, 2147483647));
        checkbookDate.setMinimumSize(new java.awt.Dimension(99, 20));
        checkbookDate.setName("checkbookDate"); // NOI18N
        checkbookDate.setPreferredSize(new java.awt.Dimension(99, 20));
        jPanel174.add(checkbookDate);

        jPanel175.setName("jPanel175"); // NOI18N
        jPanel175.setOpaque(false);

        jLabel21.setFont(getFont());
        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N
        jPanel175.add(jLabel21);

        checkbookAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        checkbookAmount.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        checkbookAmount.setMinimumSize(new java.awt.Dimension(81, 20));
        checkbookAmount.setName("checkbookAmount"); // NOI18N
        checkbookAmount.setPreferredSize(new java.awt.Dimension(81, 20));
        checkbookAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkbookAmountActionPerformed(evt);
            }
        });
        checkbookAmount.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkbookAmountFocusLost(evt);
            }
        });
        jPanel175.add(checkbookAmount);

        jPanel93.setName("jPanel93"); // NOI18N
        jPanel93.setOpaque(false);
        jPanel93.setLayout(new java.awt.GridLayout(1, 0));

        buttonGroup3.add(checkbookCheck);
        checkbookCheck.setFont(getFont());
        checkbookCheck.setSelected(true);
        checkbookCheck.setText(resourceMap.getString("checkbookCheck.text")); // NOI18N
        checkbookCheck.setName("checkbookCheck"); // NOI18N
        checkbookCheck.setOpaque(false);
        checkbookCheck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkbookCheckMouseClicked(evt);
            }
        });
        checkbookCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkbookCheckActionPerformed(evt);
            }
        });
        jPanel93.add(checkbookCheck);

        buttonGroup3.add(checkbookAch);
        checkbookAch.setFont(getFont());
        checkbookAch.setText(resourceMap.getString("checkbookAch.text")); // NOI18N
        checkbookAch.setName("checkbookAch"); // NOI18N
        checkbookAch.setOpaque(false);
        checkbookAch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkbookAchMouseClicked(evt);
            }
        });
        checkbookAch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkbookAchActionPerformed(evt);
            }
        });
        jPanel93.add(checkbookAch);

        jPanel196.setName("jPanel196"); // NOI18N
        jPanel196.setOpaque(false);

        jLabel26.setFont(getFont());
        jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N
        jPanel196.add(jLabel26);

        checksTotalAccounts.setEditable(false);
        checksTotalAccounts.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        checksTotalAccounts.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        checksTotalAccounts.setMinimumSize(new java.awt.Dimension(81, 20));
        checksTotalAccounts.setName("checksTotalAccounts"); // NOI18N
        checksTotalAccounts.setPreferredSize(new java.awt.Dimension(81, 20));
        checksTotalAccounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checksTotalAccountsActionPerformed(evt);
            }
        });
        jPanel196.add(checksTotalAccounts);

        javax.swing.GroupLayout jPanel122Layout = new javax.swing.GroupLayout(jPanel122);
        jPanel122.setLayout(jPanel122Layout);
        jPanel122Layout.setHorizontalGroup(
            jPanel122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel122Layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(jPanel122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel122Layout.createSequentialGroup()
                        .addComponent(jPanel172, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel122Layout.createSequentialGroup()
                        .addComponent(jPanel174, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel122Layout.createSequentialGroup()
                        .addComponent(jPanel175, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))))
            .addGroup(jPanel122Layout.createSequentialGroup()
                .addGroup(jPanel122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel173, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                    .addComponent(jPanel93, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel122Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel196, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(11, 11, 11)))
        );
        jPanel122Layout.setVerticalGroup(
            jPanel122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel122Layout.createSequentialGroup()
                .addComponent(jPanel173, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel93, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel172, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel174, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel175, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
            .addGroup(jPanel122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel122Layout.createSequentialGroup()
                    .addContainerGap(58, Short.MAX_VALUE)
                    .addComponent(jPanel196, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(84, 84, 84)))
        );

        jPanel123.add(jPanel122, java.awt.BorderLayout.CENTER);

        jPanel115.add(jPanel123, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel115);
        jPanel115.setBounds(0, 0, 790, 330);

        jLabel51.setIcon(resourceMap.getIcon("jLabel51.icon")); // NOI18N
        jLabel51.setText(resourceMap.getString("jLabel51.text")); // NOI18N
        jLabel51.setMaximumSize(new java.awt.Dimension(800, 330));
        jLabel51.setMinimumSize(new java.awt.Dimension(800, 330));
        jLabel51.setName("jLabel51"); // NOI18N
        jLabel51.setPreferredSize(new java.awt.Dimension(800, 330));
        jPanel2.add(jLabel51);
        jLabel51.setBounds(0, 0, 800, 330);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 789;
        gridBagConstraints.ipady = 319;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel154.add(jPanel2, gridBagConstraints);

        jPanel3.setMaximumSize(new java.awt.Dimension(250, 330));
        jPanel3.setMinimumSize(new java.awt.Dimension(250, 330));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(250, 330));
        jPanel3.setLayout(null);

        jPanel110.setName("jPanel110"); // NOI18N
        jPanel110.setOpaque(false);
        jPanel110.setLayout(null);

        jPanel119.setName("jPanel119"); // NOI18N
        jPanel119.setOpaque(false);

        saveCheck.setText(resourceMap.getString("saveCheck.text")); // NOI18N
        saveCheck.setMaximumSize(new java.awt.Dimension(135, 23));
        saveCheck.setMinimumSize(new java.awt.Dimension(135, 23));
        saveCheck.setName("saveCheck"); // NOI18N
        saveCheck.setPreferredSize(new java.awt.Dimension(135, 23));
        saveCheck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveCheckMouseClicked(evt);
            }
        });
        jPanel119.add(saveCheck);

        jButton47.setText(resourceMap.getString("jButton47.text")); // NOI18N
        jButton47.setMaximumSize(new java.awt.Dimension(135, 23));
        jButton47.setMinimumSize(new java.awt.Dimension(135, 23));
        jButton47.setName("jButton47"); // NOI18N
        jButton47.setPreferredSize(new java.awt.Dimension(135, 23));
        jButton47.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton47saveCheckButtonsClicked(evt);
            }
        });
        jPanel119.add(jButton47);

        clearCheck.setText(resourceMap.getString("clearCheck.text")); // NOI18N
        clearCheck.setMaximumSize(new java.awt.Dimension(135, 23));
        clearCheck.setMinimumSize(new java.awt.Dimension(135, 23));
        clearCheck.setName("clearCheck"); // NOI18N
        clearCheck.setPreferredSize(new java.awt.Dimension(135, 23));
        clearCheck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearCheck(evt);
            }
        });
        jPanel119.add(clearCheck);

        jButton20.setText(resourceMap.getString("jButton20.text")); // NOI18N
        jButton20.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton20.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton20.setName("jButton20"); // NOI18N
        jButton20.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });
        jPanel119.add(jButton20);

        jButton21.setText(resourceMap.getString("jButton21.text")); // NOI18N
        jButton21.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton21.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton21.setName("jButton21"); // NOI18N
        jButton21.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });
        jPanel119.add(jButton21);

        jPanel110.add(jPanel119);
        jPanel119.setBounds(0, 180, 250, 130);

        checkbookEntryID.setName("checkbookEntryID"); // NOI18N
        jPanel110.add(checkbookEntryID);
        checkbookEntryID.setBounds(820, 110, 250, 215);

        jPanel90.setName("jPanel90"); // NOI18N
        jPanel90.setLayout(new javax.swing.BoxLayout(jPanel90, javax.swing.BoxLayout.LINE_AXIS));
        jPanel110.add(jPanel90);
        jPanel90.setBounds(0, 330, 250, 0);

        jPanel3.add(jPanel110);
        jPanel110.setBounds(0, 10, 320, 310);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 70;
        gridBagConstraints.ipady = -10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 10, 13);
        jPanel154.add(jPanel3, gridBagConstraints);

        apCheckbookPanel.add(jPanel154, java.awt.BorderLayout.NORTH);

        jPanel94.setMinimumSize(new java.awt.Dimension(1127, 200));
        jPanel94.setName("jPanel94"); // NOI18N
        jPanel94.setOpaque(false);
        jPanel94.setPreferredSize(new java.awt.Dimension(1127, 200));
        jPanel94.setLayout(new javax.swing.BoxLayout(jPanel94, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        checkbookAccountsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Account", "Amount", "Control #", "Reference #", "Memo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        checkbookAccountsTable.setCellSelectionEnabled(true);
        checkbookAccountsTable.setMinimumSize(new java.awt.Dimension(375, 16));
        checkbookAccountsTable.setName("checkbookAccountsTable"); // NOI18N
        checkbookAccountsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        jScrollPane1.setViewportView(checkbookAccountsTable);
        checkbookAccountsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        checkbookAccountsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("checkbookAccountsTable.columnModel.title0")); // NOI18N
        checkbookAccountsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("checkbookAccountsTable.columnModel.title1")); // NOI18N
        checkbookAccountsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("checkbookAccountsTable.columnModel.title2")); // NOI18N
        checkbookAccountsTable.getColumnModel().getColumn(3).setMinWidth(100);
        checkbookAccountsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        checkbookAccountsTable.getColumnModel().getColumn(3).setMaxWidth(100);
        checkbookAccountsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("checkbookAccountsTable.columnModel.title3")); // NOI18N
        checkbookAccountsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("checkbookAccountsTable.columnModel.title4")); // NOI18N

        jPanel94.add(jScrollPane1);

        jPanel158.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel158.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
        jPanel158.setMinimumSize(new java.awt.Dimension(400, 200));
        jPanel158.setName("jPanel158"); // NOI18N
        jPanel158.setOpaque(false);
        jPanel158.setPreferredSize(new java.awt.Dimension(400, 200));
        jPanel158.setLayout(new javax.swing.BoxLayout(jPanel158, javax.swing.BoxLayout.Y_AXIS));

        jPanel117.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel117.setName("jPanel117"); // NOI18N
        jPanel117.setOpaque(false);
        jPanel117.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        checkbookAllDates.setSelected(true);
        checkbookAllDates.setText(resourceMap.getString("checkbookAllDates.text")); // NOI18N
        checkbookAllDates.setName("checkbookAllDates"); // NOI18N
        checkbookAllDates.setOpaque(false);
        checkbookAllDates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkbookAllDatesMouseClicked(evt);
            }
        });
        jPanel117.add(checkbookAllDates);

        jPanel159.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel159.setName("jPanel159"); // NOI18N
        jPanel159.setOpaque(false);

        jLabel103.setText(resourceMap.getString("jLabel103.text")); // NOI18N
        jLabel103.setName("jLabel103"); // NOI18N
        jPanel159.add(jLabel103);

        jDateChooser12.setDateFormatString(resourceMap.getString("jDateChooser12.dateFormatString")); // NOI18N
        jDateChooser12.setName("jDateChooser12"); // NOI18N
        jPanel159.add(jDateChooser12);

        jLabel104.setText(resourceMap.getString("jLabel104.text")); // NOI18N
        jLabel104.setName("jLabel104"); // NOI18N
        jPanel159.add(jLabel104);

        jDateChooser13.setDateFormatString(resourceMap.getString("jDateChooser13.dateFormatString")); // NOI18N
        jDateChooser13.setName("jDateChooser13"); // NOI18N
        jPanel159.add(jDateChooser13);

        jButton31.setText(resourceMap.getString("jButton31.text")); // NOI18N
        jButton31.setName("jButton31"); // NOI18N
        jButton31.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton31refreshButtonsClicked(evt);
            }
        });
        jPanel159.add(jButton31);

        jPanel117.add(jPanel159);

        checksSearchTextField.setMaximumSize(new java.awt.Dimension(100, 20));
        checksSearchTextField.setMinimumSize(new java.awt.Dimension(100, 20));
        checksSearchTextField.setName("checksSearchTextField"); // NOI18N
        checksSearchTextField.setPreferredSize(new java.awt.Dimension(100, 20));
        checksSearchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                checksSearchTextFieldKeyReleased(evt);
            }
        });
        jPanel117.add(checksSearchTextField);

        checksSearchButton.setText(resourceMap.getString("checksSearchButton.text")); // NOI18N
        checksSearchButton.setName("checksSearchButton"); // NOI18N
        checksSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checksSearchButtonMouseClicked(evt);
            }
        });
        jPanel117.add(checksSearchButton);

        jPanel158.add(jPanel117);

        jPanel23.setName("jPanel23"); // NOI18N
        jPanel23.setOpaque(false);
        jPanel23.setLayout(new javax.swing.BoxLayout(jPanel23, javax.swing.BoxLayout.LINE_AXIS));

        jPanel16.setName("jPanel16"); // NOI18N
        jPanel16.setLayout(new javax.swing.BoxLayout(jPanel16, javax.swing.BoxLayout.Y_AXIS));

        printCheckButton.setText(resourceMap.getString("printCheckButton.text")); // NOI18N
        printCheckButton.setMaximumSize(new java.awt.Dimension(95, 23));
        printCheckButton.setMinimumSize(new java.awt.Dimension(95, 23));
        printCheckButton.setName("printCheckButton"); // NOI18N
        printCheckButton.setPreferredSize(new java.awt.Dimension(95, 23));
        printCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                printCheckButtoncheckButtonsClicked(evt);
            }
        });
        jPanel16.add(printCheckButton);

        editCheckButton.setText(resourceMap.getString("bpCheckbookEdit.text")); // NOI18N
        editCheckButton.setMaximumSize(new java.awt.Dimension(95, 23));
        editCheckButton.setMinimumSize(new java.awt.Dimension(95, 23));
        editCheckButton.setName("bpCheckbookEdit"); // NOI18N
        editCheckButton.setPreferredSize(new java.awt.Dimension(95, 23));
        editCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editCheckButtonMouseClicked(evt);
            }
        });
        jPanel16.add(editCheckButton);

        voidCheckButton.setText(resourceMap.getString("voidCheckButton.text")); // NOI18N
        voidCheckButton.setMaximumSize(new java.awt.Dimension(95, 23));
        voidCheckButton.setMinimumSize(new java.awt.Dimension(95, 23));
        voidCheckButton.setName("voidCheckButton"); // NOI18N
        voidCheckButton.setPreferredSize(new java.awt.Dimension(95, 23));
        voidCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                voidCheckButtoncheckButtonsClicked(evt);
            }
        });
        jPanel16.add(voidCheckButton);

        deleteCheckButton.setText(resourceMap.getString("deleteCheckButton.text")); // NOI18N
        deleteCheckButton.setName("deleteCheckButton"); // NOI18N
        deleteCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteCheckButtonMouseClicked(evt);
            }
        });
        jPanel16.add(deleteCheckButton);

        jPanel23.add(jPanel16);

        jPanel39.setName("jPanel39"); // NOI18N
        jPanel39.setOpaque(false);
        jPanel39.setLayout(new javax.swing.BoxLayout(jPanel39, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane24.setName("jScrollPane24"); // NOI18N
        jScrollPane24.setOpaque(false);

        checksBottomTable.setAutoCreateRowSorter(true);
        checksBottomTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bank", "Check #", "ACH #", "Paid To", "Amount", "Check Date", "Memo", "Type", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        checksBottomTable.setName("checksBottomTable"); // NOI18N
        checksBottomTable.setOpaque(false);
        checksBottomTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        checksBottomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checksBottomTableMouseClicked(evt);
            }
        });
        jScrollPane24.setViewportView(checksBottomTable);
        checksBottomTable.getColumnModel().getColumn(0).setMinWidth(150);
        checksBottomTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        checksBottomTable.getColumnModel().getColumn(0).setMaxWidth(150);
        checksBottomTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("checksBottomTable.columnModel.title0")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(1).setMinWidth(60);
        checksBottomTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        checksBottomTable.getColumnModel().getColumn(1).setMaxWidth(60);
        checksBottomTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("checksBottomTable.columnModel.title1")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(2).setMinWidth(60);
        checksBottomTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        checksBottomTable.getColumnModel().getColumn(2).setMaxWidth(60);
        checksBottomTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("checksBottomTable.columnModel.title2")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("checksBottomTable.columnModel.title8")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(4).setMinWidth(80);
        checksBottomTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        checksBottomTable.getColumnModel().getColumn(4).setMaxWidth(80);
        checksBottomTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("checkbookChecksTable.columnModel.title4")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(5).setMinWidth(80);
        checksBottomTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        checksBottomTable.getColumnModel().getColumn(5).setMaxWidth(80);
        checksBottomTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("checkbookChecksTable.columnModel.title5")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("checkbookChecksTable.columnModel.title6")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(7).setMinWidth(160);
        checksBottomTable.getColumnModel().getColumn(7).setPreferredWidth(160);
        checksBottomTable.getColumnModel().getColumn(7).setMaxWidth(160);
        checksBottomTable.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("checksBottomTable.columnModel.title6")); // NOI18N
        checksBottomTable.getColumnModel().getColumn(8).setMinWidth(50);
        checksBottomTable.getColumnModel().getColumn(8).setPreferredWidth(50);
        checksBottomTable.getColumnModel().getColumn(8).setMaxWidth(50);
        checksBottomTable.getColumnModel().getColumn(8).setHeaderValue(resourceMap.getString("checksBottomTable.columnModel.title7")); // NOI18N

        jPanel39.add(jScrollPane24);

        jPanel23.add(jPanel39);

        jPanel158.add(jPanel23);

        jPanel94.add(jPanel158);

        apCheckbookPanel.add(jPanel94, java.awt.BorderLayout.CENTER);

        jideTabbedPane4.addTab(resourceMap.getString("apCheckbookPanel.TabConstraints.tabTitle"), apCheckbookPanel); // NOI18N

        apDepositsPanel.setName("apDepositsPanel"); // NOI18N
        apDepositsPanel.setOpaque(false);
        apDepositsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                apDepositsPanelComponentShown(evt);
            }
        });
        apDepositsPanel.setLayout(new java.awt.BorderLayout());

        jPanel155.setMaximumSize(new java.awt.Dimension(926, 335));
        jPanel155.setMinimumSize(new java.awt.Dimension(926, 335));
        jPanel155.setName("jPanel155"); // NOI18N
        jPanel155.setOpaque(false);
        jPanel155.setPreferredSize(new java.awt.Dimension(926, 335));
        jPanel155.setLayout(new java.awt.GridBagLayout());

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setOpaque(false);
        jPanel4.setLayout(null);

        jPanel118.setMaximumSize(new java.awt.Dimension(800, 330));
        jPanel118.setName("jPanel118"); // NOI18N
        jPanel118.setOpaque(false);
        jPanel118.setLayout(new java.awt.BorderLayout());

        jPanel129.setMaximumSize(new java.awt.Dimension(800, 30));
        jPanel129.setMinimumSize(new java.awt.Dimension(800, 30));
        jPanel129.setName("jPanel129"); // NOI18N
        jPanel129.setOpaque(false);

        javax.swing.GroupLayout jPanel129Layout = new javax.swing.GroupLayout(jPanel129);
        jPanel129.setLayout(jPanel129Layout);
        jPanel129Layout.setHorizontalGroup(
            jPanel129Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel129Layout.setVerticalGroup(
            jPanel129Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jPanel118.add(jPanel129, java.awt.BorderLayout.NORTH);

        jPanel124.setMaximumSize(new java.awt.Dimension(800, 300));
        jPanel124.setMinimumSize(new java.awt.Dimension(800, 300));
        jPanel124.setName("jPanel124"); // NOI18N
        jPanel124.setOpaque(false);
        jPanel124.setPreferredSize(new java.awt.Dimension(800, 300));
        jPanel124.setLayout(new java.awt.BorderLayout());

        jPanel34.setMaximumSize(new java.awt.Dimension(550, 330));
        jPanel34.setMinimumSize(new java.awt.Dimension(550, 330));
        jPanel34.setName("jPanel34"); // NOI18N
        jPanel34.setOpaque(false);
        jPanel34.setPreferredSize(new java.awt.Dimension(550, 330));

        jPanel146.setName("jPanel146"); // NOI18N
        jPanel146.setOpaque(false);
        jPanel146.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel160.setFont(getFont());
        jLabel160.setText("BANK ACCOUNT :"); // NOI18N
        jLabel160.setName("jLabel160"); // NOI18N
        jPanel146.add(jLabel160);

        depositsBankCombo.setMaximumSize(new java.awt.Dimension(190, 20));
        depositsBankCombo.setMinimumSize(new java.awt.Dimension(190, 20));
        depositsBankCombo.setName("depositsBankCombo"); // NOI18N
        depositsBankCombo.setPreferredSize(new java.awt.Dimension(190, 20));
        depositsBankCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                depositsBankCombobankSelected(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        depositsBankCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                depositsBankComboItemStateChanged(evt);
            }
        });
        jPanel146.add(depositsBankCombo);

        jPanel176.setName("jPanel176"); // NOI18N
        jPanel176.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel156.setFont(getFont());
        jLabel156.setText("MEMO :"); // NOI18N
        jLabel156.setName("jLabel156"); // NOI18N
        jPanel176.add(jLabel156);

        depositsMemo.setMaximumSize(new java.awt.Dimension(350, 20));
        depositsMemo.setMinimumSize(new java.awt.Dimension(350, 20));
        depositsMemo.setName("depositsMemo"); // NOI18N
        depositsMemo.setPreferredSize(new java.awt.Dimension(350, 20));
        jPanel176.add(depositsMemo);

        billClass_deposit.setEditable(false);
        billClass_deposit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", "" }));
        billClass_deposit.setName("billClass_deposit"); // NOI18N
        billClass_deposit.setStrict(false);
        billClass_deposit.setStrictCompletion(false);
        billClass_deposit.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                billClass_depositItemStateChanged(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jLabel3.setFont(getFont());
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel176, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel146, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(billClass_deposit, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addComponent(jPanel146, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(billClass_deposit, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(60, 60, 60)
                .addComponent(jPanel176, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        jPanel124.add(jPanel34, java.awt.BorderLayout.WEST);

        jPanel125.setEnabled(false);
        jPanel125.setMaximumSize(new java.awt.Dimension(210, 100));
        jPanel125.setMinimumSize(new java.awt.Dimension(210, 100));
        jPanel125.setName("jPanel125"); // NOI18N
        jPanel125.setOpaque(false);
        jPanel125.setPreferredSize(new java.awt.Dimension(210, 100));

        jPanel177.setName("jPanel177"); // NOI18N
        jPanel177.setOpaque(false);

        jLabel22.setFont(getFont());
        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N
        jPanel177.add(jLabel22);

        depositsEndingBalance.setEditable(false);
        depositsEndingBalance.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("¤#,##0.00"))));
        depositsEndingBalance.setMaximumSize(new java.awt.Dimension(80, 20));
        depositsEndingBalance.setMinimumSize(new java.awt.Dimension(80, 20));
        depositsEndingBalance.setName("depositsEndingBalance"); // NOI18N
        depositsEndingBalance.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel177.add(depositsEndingBalance);

        jPanel178.setName("jPanel178"); // NOI18N
        jPanel178.setOpaque(false);

        jLabel23.setFont(getFont());
        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N
        jPanel178.add(jLabel23);

        depositsNumber.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        depositsNumber.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        depositsNumber.setMinimumSize(new java.awt.Dimension(81, 20));
        depositsNumber.setName("depositsNumber"); // NOI18N
        depositsNumber.setPreferredSize(new java.awt.Dimension(81, 20));
        depositsNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                depositsNumberFocusLost(evt);
            }
        });
        depositsNumber.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                depositsNumberInputMethodTextChanged(evt);
            }
        });
        depositsNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                depositsNumberKeyReleased(evt);
            }
        });
        jPanel178.add(depositsNumber);

        jPanel179.setName("jPanel179"); // NOI18N
        jPanel179.setOpaque(false);

        jLabel24.setFont(getFont());
        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N
        jPanel179.add(jLabel24);

        depositsDate.setDateFormatString(resourceMap.getString("depositsDate.dateFormatString")); // NOI18N
        depositsDate.setMaximumSize(new java.awt.Dimension(99, 2147483647));
        depositsDate.setMinimumSize(new java.awt.Dimension(99, 20));
        depositsDate.setName("depositsDate"); // NOI18N
        depositsDate.setPreferredSize(new java.awt.Dimension(99, 20));
        jPanel179.add(depositsDate);

        jPanel180.setName("jPanel180"); // NOI18N
        jPanel180.setOpaque(false);

        jLabel25.setFont(getFont());
        jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N
        jPanel180.add(jLabel25);

        depositsAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        depositsAmount.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        depositsAmount.setMinimumSize(new java.awt.Dimension(81, 20));
        depositsAmount.setName("depositsAmount"); // NOI18N
        depositsAmount.setPreferredSize(new java.awt.Dimension(81, 20));
        depositsAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositsAmountActionPerformed(evt);
            }
        });
        depositsAmount.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                depositsAmountFocusLost(evt);
            }
        });
        jPanel180.add(depositsAmount);

        jPanel95.setName("jPanel95"); // NOI18N
        jPanel95.setOpaque(false);
        jPanel95.setLayout(new java.awt.GridLayout(1, 0));

        buttonGroup1.add(depositsDeposit);
        depositsDeposit.setFont(getFont());
        depositsDeposit.setSelected(true);
        depositsDeposit.setText(resourceMap.getString("depositsDeposit.text")); // NOI18N
        depositsDeposit.setName("depositsDeposit"); // NOI18N
        depositsDeposit.setOpaque(false);
        depositsDeposit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsDepositMouseClicked(evt);
            }
        });
        jPanel95.add(depositsDeposit);

        buttonGroup1.add(depositsACH);
        depositsACH.setFont(getFont());
        depositsACH.setText(resourceMap.getString("depositsACH.text")); // NOI18N
        depositsACH.setName("depositsACH"); // NOI18N
        depositsACH.setOpaque(false);
        depositsACH.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsACHMouseClicked(evt);
            }
        });
        jPanel95.add(depositsACH);

        jPanel197.setName("jPanel197"); // NOI18N
        jPanel197.setOpaque(false);

        jLabel27.setFont(getFont());
        jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N
        jPanel197.add(jLabel27);

        depositsTotalAccounts.setEditable(false);
        depositsTotalAccounts.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        depositsTotalAccounts.setMaximumSize(new java.awt.Dimension(81, 2147483647));
        depositsTotalAccounts.setMinimumSize(new java.awt.Dimension(81, 20));
        depositsTotalAccounts.setName("depositsTotalAccounts"); // NOI18N
        depositsTotalAccounts.setPreferredSize(new java.awt.Dimension(81, 20));
        depositsTotalAccounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositsTotalAccountsActionPerformed(evt);
            }
        });
        jPanel197.add(depositsTotalAccounts);

        javax.swing.GroupLayout jPanel125Layout = new javax.swing.GroupLayout(jPanel125);
        jPanel125.setLayout(jPanel125Layout);
        jPanel125Layout.setHorizontalGroup(
            jPanel125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel125Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel125Layout.createSequentialGroup()
                        .addComponent(jPanel178, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel125Layout.createSequentialGroup()
                        .addComponent(jPanel179, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel125Layout.createSequentialGroup()
                        .addComponent(jPanel180, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))))
            .addGroup(jPanel125Layout.createSequentialGroup()
                .addGroup(jPanel125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel177, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                    .addComponent(jPanel95, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel125Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel197, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(11, 11, 11)))
        );
        jPanel125Layout.setVerticalGroup(
            jPanel125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel125Layout.createSequentialGroup()
                .addComponent(jPanel177, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel95, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel178, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel179, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel180, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel125Layout.createSequentialGroup()
                    .addContainerGap(58, Short.MAX_VALUE)
                    .addComponent(jPanel197, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(84, 84, 84)))
        );

        jPanel124.add(jPanel125, java.awt.BorderLayout.CENTER);

        jPanel118.add(jPanel124, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel118);
        jPanel118.setBounds(0, 0, 790, 330);

        jLabel52.setIcon(resourceMap.getIcon("jLabel52.icon")); // NOI18N
        jLabel52.setText(resourceMap.getString("jLabel52.text")); // NOI18N
        jLabel52.setMaximumSize(new java.awt.Dimension(800, 330));
        jLabel52.setMinimumSize(new java.awt.Dimension(800, 330));
        jLabel52.setName("jLabel52"); // NOI18N
        jLabel52.setPreferredSize(new java.awt.Dimension(800, 330));
        jPanel4.add(jLabel52);
        jLabel52.setBounds(0, 0, 800, 330);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 789;
        gridBagConstraints.ipady = 319;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        jPanel155.add(jPanel4, gridBagConstraints);

        jPanel5.setMaximumSize(new java.awt.Dimension(250, 330));
        jPanel5.setMinimumSize(new java.awt.Dimension(250, 330));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(250, 330));
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

        jPanel111.setName("jPanel111"); // NOI18N
        jPanel111.setOpaque(false);
        jPanel111.setLayout(null);

        jPanel121.setName("jPanel121"); // NOI18N
        jPanel121.setOpaque(false);

        saveDepositButton.setText(resourceMap.getString("saveDepositButton.text")); // NOI18N
        saveDepositButton.setMaximumSize(new java.awt.Dimension(135, 23));
        saveDepositButton.setMinimumSize(new java.awt.Dimension(135, 23));
        saveDepositButton.setName("saveDepositButton"); // NOI18N
        saveDepositButton.setPreferredSize(new java.awt.Dimension(135, 23));
        saveDepositButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveDepositButton(evt);
            }
        });
        jPanel121.add(saveDepositButton);

        jButton48.setText(resourceMap.getString("jButton48.text")); // NOI18N
        jButton48.setMaximumSize(new java.awt.Dimension(135, 23));
        jButton48.setMinimumSize(new java.awt.Dimension(135, 23));
        jButton48.setName("jButton48"); // NOI18N
        jButton48.setPreferredSize(new java.awt.Dimension(135, 23));
        jButton48.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton48saveCheckButtonsClicked(evt);
            }
        });
        jPanel121.add(jButton48);

        clearDeposit.setText(resourceMap.getString("clearDeposit.text")); // NOI18N
        clearDeposit.setMaximumSize(new java.awt.Dimension(135, 23));
        clearDeposit.setMinimumSize(new java.awt.Dimension(135, 23));
        clearDeposit.setName("clearDeposit"); // NOI18N
        clearDeposit.setPreferredSize(new java.awt.Dimension(135, 23));
        clearDeposit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearDeposit(evt);
            }
        });
        jPanel121.add(clearDeposit);

        jButton22.setText(resourceMap.getString("jButton22.text")); // NOI18N
        jButton22.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton22.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton22.setName("jButton22"); // NOI18N
        jButton22.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });
        jPanel121.add(jButton22);

        jButton23.setText(resourceMap.getString("jButton23.text")); // NOI18N
        jButton23.setMaximumSize(new java.awt.Dimension(110, 23));
        jButton23.setMinimumSize(new java.awt.Dimension(110, 23));
        jButton23.setName("jButton23"); // NOI18N
        jButton23.setPreferredSize(new java.awt.Dimension(110, 23));
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });
        jPanel121.add(jButton23);

        jPanel111.add(jPanel121);
        jPanel121.setBounds(30, 180, 250, 115);

        checkbookEntryID1.setName("checkbookEntryID1"); // NOI18N
        jPanel111.add(checkbookEntryID1);
        checkbookEntryID1.setBounds(820, 110, 250, 215);

        jPanel91.setName("jPanel91"); // NOI18N
        jPanel91.setLayout(new javax.swing.BoxLayout(jPanel91, javax.swing.BoxLayout.LINE_AXIS));
        jPanel111.add(jPanel91);
        jPanel91.setBounds(0, 330, 250, 0);

        jPanel5.add(jPanel111);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 70;
        gridBagConstraints.ipady = -10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 15, 13);
        jPanel155.add(jPanel5, gridBagConstraints);

        apDepositsPanel.add(jPanel155, java.awt.BorderLayout.NORTH);

        jPanel96.setMinimumSize(new java.awt.Dimension(1127, 205));
        jPanel96.setName("jPanel96"); // NOI18N
        jPanel96.setOpaque(false);
        jPanel96.setPreferredSize(new java.awt.Dimension(1127, 205));
        jPanel96.setLayout(new javax.swing.BoxLayout(jPanel96, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane20.setName("jScrollPane20"); // NOI18N

        depositsAccountsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Account", "Amount", "Control #", "Reference #", "Memo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        depositsAccountsTable.setColumnSelectionAllowed(true);
        depositsAccountsTable.setMinimumSize(new java.awt.Dimension(375, 16));
        depositsAccountsTable.setName("depositsAccountsTable"); // NOI18N
        depositsAccountsTable.setOpaque(false);
        depositsAccountsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        depositsAccountsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsAccountsTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                depositsAccountsTableMouseExited(evt);
            }
        });
        depositsAccountsTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                depositsAccountsTableFocusLost(evt);
            }
        });
        depositsAccountsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                depositsAccountsTableKeyReleased(evt);
            }
        });
        jScrollPane20.setViewportView(depositsAccountsTable);
        depositsAccountsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        depositsAccountsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("depositsAccountsTable.columnModel.title0")); // NOI18N
        depositsAccountsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("depositsAccountsTable.columnModel.title1")); // NOI18N
        depositsAccountsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("depositsAccountsTable.columnModel.title2")); // NOI18N
        depositsAccountsTable.getColumnModel().getColumn(3).setMinWidth(100);
        depositsAccountsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        depositsAccountsTable.getColumnModel().getColumn(3).setMaxWidth(100);
        depositsAccountsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("depositsAccountsTable.columnModel.title3")); // NOI18N
        depositsAccountsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("depositsAccountsTable.columnModel.title4")); // NOI18N

        jPanel96.add(jScrollPane20);

        jPanel160.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel160.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
        jPanel160.setMinimumSize(new java.awt.Dimension(365, 200));
        jPanel160.setName("jPanel160"); // NOI18N
        jPanel160.setOpaque(false);
        jPanel160.setPreferredSize(new java.awt.Dimension(400, 200));
        jPanel160.setLayout(new javax.swing.BoxLayout(jPanel160, javax.swing.BoxLayout.Y_AXIS));

        jPanel126.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel126.setName("jPanel126"); // NOI18N
        jPanel126.setOpaque(false);
        jPanel126.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        depositsAllDates.setSelected(true);
        depositsAllDates.setText(resourceMap.getString("depositsAllDates.text")); // NOI18N
        depositsAllDates.setName("depositsAllDates"); // NOI18N
        depositsAllDates.setOpaque(false);
        depositsAllDates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsAllDatesMouseClicked(evt);
            }
        });
        jPanel126.add(depositsAllDates);

        jPanel161.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel161.setName("jPanel161"); // NOI18N
        jPanel161.setOpaque(false);

        jLabel105.setText(resourceMap.getString("jLabel105.text")); // NOI18N
        jLabel105.setName("jLabel105"); // NOI18N
        jPanel161.add(jLabel105);

        jDateChooser14.setDateFormatString(resourceMap.getString("jDateChooser14.dateFormatString")); // NOI18N
        jDateChooser14.setName("jDateChooser14"); // NOI18N
        jPanel161.add(jDateChooser14);

        jLabel106.setText(resourceMap.getString("jLabel106.text")); // NOI18N
        jLabel106.setName("jLabel106"); // NOI18N
        jPanel161.add(jLabel106);

        jDateChooser15.setDateFormatString(resourceMap.getString("jDateChooser15.dateFormatString")); // NOI18N
        jDateChooser15.setName("jDateChooser15"); // NOI18N
        jPanel161.add(jDateChooser15);

        jButton32.setText(resourceMap.getString("jButton32.text")); // NOI18N
        jButton32.setName("jButton32"); // NOI18N
        jButton32.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton32refreshButtonsClicked(evt);
            }
        });
        jPanel161.add(jButton32);

        jPanel126.add(jPanel161);

        depositsSearchTextField.setMaximumSize(new java.awt.Dimension(100, 20));
        depositsSearchTextField.setMinimumSize(new java.awt.Dimension(100, 20));
        depositsSearchTextField.setName("depositsSearchTextField"); // NOI18N
        depositsSearchTextField.setPreferredSize(new java.awt.Dimension(100, 20));
        depositsSearchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                depositsSearchTextFieldKeyReleased(evt);
            }
        });
        jPanel126.add(depositsSearchTextField);

        depositsSearchButton.setText(resourceMap.getString("depositsSearchButton.text")); // NOI18N
        depositsSearchButton.setName("depositsSearchButton"); // NOI18N
        depositsSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsSearchButtonMouseClicked(evt);
            }
        });
        jPanel126.add(depositsSearchButton);

        jPanel160.add(jPanel126);

        jPanel24.setName("jPanel24"); // NOI18N
        jPanel24.setOpaque(false);
        jPanel24.setLayout(new javax.swing.BoxLayout(jPanel24, javax.swing.BoxLayout.LINE_AXIS));

        jPanel40.setName("jPanel40"); // NOI18N
        jPanel40.setOpaque(false);
        jPanel40.setPreferredSize(new java.awt.Dimension(450, 400));
        jPanel40.setLayout(new javax.swing.BoxLayout(jPanel40, javax.swing.BoxLayout.LINE_AXIS));

        jPanel17.setName("jPanel17"); // NOI18N
        jPanel17.setLayout(new javax.swing.BoxLayout(jPanel17, javax.swing.BoxLayout.Y_AXIS));

        jButton38.setText(resourceMap.getString("jButton38.text")); // NOI18N
        jButton38.setMaximumSize(new java.awt.Dimension(102, 23));
        jButton38.setMinimumSize(new java.awt.Dimension(102, 23));
        jButton38.setName("jButton38"); // NOI18N
        jButton38.setPreferredSize(new java.awt.Dimension(102, 23));
        jButton38.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton38checkButtonsClicked(evt);
            }
        });
        jPanel17.add(jButton38);

        jButton40.setText(resourceMap.getString("jButton40.text")); // NOI18N
        jButton40.setMaximumSize(new java.awt.Dimension(102, 23));
        jButton40.setMinimumSize(new java.awt.Dimension(102, 23));
        jButton40.setName("jButton40"); // NOI18N
        jButton40.setPreferredSize(new java.awt.Dimension(102, 23));
        jButton40.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton40checkButtonsClicked(evt);
            }
        });
        jPanel17.add(jButton40);

        voidDepositBtn.setText(resourceMap.getString("voidDepositBtn.text")); // NOI18N
        voidDepositBtn.setMaximumSize(new java.awt.Dimension(102, 23));
        voidDepositBtn.setMinimumSize(new java.awt.Dimension(102, 23));
        voidDepositBtn.setName("voidDepositBtn"); // NOI18N
        voidDepositBtn.setPreferredSize(new java.awt.Dimension(102, 23));
        voidDepositBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                voidDepositBtncheckButtonsClicked(evt);
            }
        });
        jPanel17.add(voidDepositBtn);

        deleteDeposit.setText(resourceMap.getString("deleteDeposit.text")); // NOI18N
        deleteDeposit.setMaximumSize(new java.awt.Dimension(102, 23));
        deleteDeposit.setMinimumSize(new java.awt.Dimension(102, 23));
        deleteDeposit.setName("deleteDeposit"); // NOI18N
        deleteDeposit.setPreferredSize(new java.awt.Dimension(102, 23));
        deleteDeposit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteDepositMouseClicked(evt);
            }
        });
        jPanel17.add(deleteDeposit);

        jPanel40.add(jPanel17);

        jScrollPane25.setName("jScrollPane25"); // NOI18N
        jScrollPane25.setOpaque(false);
        jScrollPane25.setPreferredSize(new java.awt.Dimension(450, 400));

        depositsBottomTable.setAutoCreateRowSorter(true);
        depositsBottomTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bank", "Deposit #", "Amount", "Deposit Date", "Memo", "Type", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        depositsBottomTable.setName("depositsBottomTable"); // NOI18N
        depositsBottomTable.setOpaque(false);
        depositsBottomTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        depositsBottomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                depositsBottomTableMouseClicked(evt);
            }
        });
        jScrollPane25.setViewportView(depositsBottomTable);
        depositsBottomTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("depositsBottomTable.columnModel.title0")); // NOI18N
        depositsBottomTable.getColumnModel().getColumn(1).setMinWidth(80);
        depositsBottomTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        depositsBottomTable.getColumnModel().getColumn(1).setMaxWidth(80);
        depositsBottomTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("depositsBottomTable.columnModel.title1")); // NOI18N
        depositsBottomTable.getColumnModel().getColumn(2).setMinWidth(100);
        depositsBottomTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        depositsBottomTable.getColumnModel().getColumn(2).setMaxWidth(100);
        depositsBottomTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("depositsChecksTable.columnModel.title4")); // NOI18N
        depositsBottomTable.getColumnModel().getColumn(3).setMinWidth(80);
        depositsBottomTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        depositsBottomTable.getColumnModel().getColumn(3).setMaxWidth(80);
        depositsBottomTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("depositsChecksTable.columnModel.title5")); // NOI18N
        depositsBottomTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("depositsChecksTable.columnModel.title6")); // NOI18N
        depositsBottomTable.getColumnModel().getColumn(5).setMinWidth(160);
        depositsBottomTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        depositsBottomTable.getColumnModel().getColumn(5).setMaxWidth(160);
        depositsBottomTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("depositsBottomTable.columnModel.title6")); // NOI18N
        depositsBottomTable.getColumnModel().getColumn(6).setMinWidth(50);
        depositsBottomTable.getColumnModel().getColumn(6).setPreferredWidth(50);
        depositsBottomTable.getColumnModel().getColumn(6).setMaxWidth(50);
        depositsBottomTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("depositsBottomTable.columnModel.title5")); // NOI18N

        jPanel40.add(jScrollPane25);

        jPanel24.add(jPanel40);

        jPanel160.add(jPanel24);

        jPanel96.add(jPanel160);

        apDepositsPanel.add(jPanel96, java.awt.BorderLayout.CENTER);

        jideTabbedPane4.addTab(resourceMap.getString("apDepositsPanel.TabConstraints.tabTitle"), apDepositsPanel); // NOI18N

        add(jideTabbedPane4);
    }// </editor-fold>//GEN-END:initComponents

        private void jButton31refreshButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton31refreshButtonsClicked
            reloadChecks();
        }//GEN-LAST:event_jButton31refreshButtonsClicked

        private void checksSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checksSearchButtonMouseClicked
            try {
                String sqlQuery;
                String searchFilter = "";
                String checkSearch = checksSearchTextField.getText();
                int checkSearchInt = 0;

                if (checkSearch == null || checkSearch.isEmpty()) {
                    searchFilter = "(BankAccount = '202' OR BankAccount = '203') ";
                } else {
                    if (AccountingUtil.isNumeric(checkSearch)) {
                        checkSearchInt = Integer.parseInt(checkSearch);
                        searchFilter = "CheckNumber LIKE '%" + checkSearchInt + "%' "
                                + "OR BankAccount LIKE '%" + checkSearchInt + "%' "
                                + "OR A.Memo LIKE '%" + checkSearchInt + "%' ";
                    } else {
                        searchFilter = "PaidTo LIKE '%" + checkSearch + "%' "
                                + "OR A.Memo LIKE '%" + checkSearch + "%' ";
                    }
                }

                sqlQuery = "SELECT CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description,"
                        + "A.CheckNumber,A.PaidTo,CAST(ROUND(A.Amount,2) AS NUMERIC(10,2)),"
                        + "A.Date,A.Memo, B.GLType, A.Status  "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingGLTable B ON (B.AccountNumber = A.BankAccount AND B.ReferenceNumber = CAST(A.CheckNumber AS VarChar)) "
                        + "LEFT JOIN AccountingCOATable C "
                        + "ON A.BankAccount = C.AccountNumber "
                        + "WHERE CheckNumber <> 0 AND "
                        + searchFilter
                        + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + "AND (GLType like 'Check%' OR GLType like 'Bill Payment%') "
                        + "ORDER BY CheckNumber Desc ";

                sqlQuery = "SELECT "
                        + " CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description, A.CheckNumber, A.AchCheckNumber, "
                        + "A.PaidTo,CAST(ROUND(A.Amount,2) AS NUMERIC(10,2)) AS Amount,A.Date,A.Memo, A.Type, A.Status "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingCOATable C ON (A.BankAccount = C.AccountNumber) "
                        + "WHERE "
                        + searchFilter
                        + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + "AND A.Type LIKE '%Check%' "
                        + "ORDER BY CheckNumber DESC";

                System.out.println("Check Search Query : " + sqlQuery);
                ResultSet rs;
                rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);

                DefaultTableModel aModel = (DefaultTableModel) checksBottomTable.getModel();
                AccountingUtil.clearTableModel(aModel);

                ResultSetMetaData rsmd = rs.getMetaData();
                int colNo = rsmd.getColumnCount();
//                while (rs.next()) {
//                    Object[] values = new Object[colNo];
//                    for (int i = 0; i < colNo; i++) {
//                        if (i == 3) {
//                            values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject("Amount").toString());
//                        } else if (i == 4) {
//                            values[i] = AccountingUtil.dateFormat1.format(rs.getObject("Date"));
//                        } else {
//                            values[i] = rs.getObject(i + 1);
//                        }
//                    }
//                    aModel.addRow(values);
//                }

                while (rs.next()) {
                    Object[] values = new Object[colNo];
                    for (int i = 0; i < colNo; i++) {
                        if (i == AccountingUtil.getColumnByName(checksBottomTable, "Check Date")) {
                            values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                        } else if (i == AccountingUtil.getColumnByName(checksBottomTable, "Amount")) {
                            values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject("Amount").toString());
                        } else {
                            values[i] = rs.getObject(i + 1);
                        }
                    }
                    aModel.addRow(values);
                }
                rs.getStatement().close();
            } catch (SQLException ex) {

                Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }//GEN-LAST:event_checksSearchButtonMouseClicked

        private void printCheckButtoncheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_printCheckButtoncheckButtonsClicked
            int selectedChecksRowCount = checksBottomTable.getSelectedRowCount();

            String checkType = checksBottomTable.getValueAt(
                    checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Type")).toString();
            int lastCheckNumberInSequence = Integer.parseInt(checksBottomTable.getValueAt(
                    checksBottomTable.getSelectedRows()[selectedChecksRowCount - 1], checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString());
            int bankAccount = Integer.parseInt(checksBottomTable.getValueAt(
                    checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Bank")).toString().split("-")[0].trim());

            if (selectedChecksRowCount > 0) {
                if ("Check - Vendor".equals(checkType) || "Check - Customer".equals(checkType) || "Check - Employee".equals(checkType)) {
                    printSelectedCheck(lastCheckNumberInSequence);
                } else if ("Bill Payment Check - Vendor".equals(checkType)) {
                    accountsPayablePanel.printBillPayCheck(lastCheckNumberInSequence, bankAccount);
                }
            } else {
                dms.DMSApp.displayMessage(this, "Please select at least one check to print", dms.DMSApp.WARNING_MESSAGE);
            }

        }//GEN-LAST:event_printCheckButtoncheckButtonsClicked

    public boolean isCheckNumberAlreadyExists(String checkNo) {
        boolean checkAlreadyExists = false;
        System.out.println("Checking if " + checkNo + " already exists");
        // check if check# already exists
        try {
            String uniqueCheckNumberQuery = "select CheckNumber from AccountingCBTable where Status = 'Printed' AND CheckNumber = " + checkNo;
            ResultSet rsUniqueCheckNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniqueCheckNumberQuery);
            if (rsUniqueCheckNumber.next()) {
                dms.DMSApp.displayMessage(this, "Check # " + checkNo + " has aleardy been printed.", dms.DMSApp.WARNING_MESSAGE);
                checkAlreadyExists = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkAlreadyExists;
    }

    private void printSelectedCheck(int selectedCheckNumber) {
        ArrayList<Integer> checkNumbersListFromDialog = new ArrayList<Integer>();
        ArrayList<Integer> checkNumbersListFromTable = new ArrayList<Integer>();
        try {
            // Show input - check# dialog
            ResultSet resultSetForAccountDetails;
            int selectedChecksRowCount = checksBottomTable.getSelectedRowCount();

            System.out.println("selectedChecksRowCount := " + selectedChecksRowCount);
            String checkNumberFromDialog = JOptionPane.showInputDialog(this, "Please enter first Check Number", selectedCheckNumber);

            if (checkNumberFromDialog == null || checkNumberFromDialog.isEmpty()) {
                return;
            }
            if (!AccountingUtil.isNumeric(checkNumberFromDialog)) {
                dms.DMSApp.displayMessage(this, "Invalid check# value, please input again ", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            int selectedRows[] = checksBottomTable.getSelectedRows();
            Integer checkNumberFromDialogIntVal = Integer.valueOf(checkNumberFromDialog);
            if (!checkNoExists) {
                for (int i = 0; i < selectedRows.length; i++) {
                    // get the vendors address based on paidTo
                    int bankAccountNumberFromTable = Integer.parseInt(checksBottomTable.getValueAt(
                            selectedRows[i], checksBottomTable.getColumnModel().getColumnIndex("Bank")).toString().split("-")[0].trim());

                    int checkNumberFromTable = Integer.parseInt(checksBottomTable.getValueAt(
                            selectedRows[i], checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString());
                    String checkMemo = "";
                    String checkAmount = "";
                    String checkDate = "";
                    String paidToType = "";
                    String sqlQuery;
                    int paidToID = 0;
                    String address = "";
                    String city = "";
                    String state = "";
                    String zip = "";
                    String name = "";


                    sqlQuery = "SELECT "
                            + "AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, "
                            + "VendorName "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B "
                            + "ON (A.ControlNumber = CAST(B.VendorID AS VARCHAR)) "
                            + "WHERE ReferenceNumber = '" + checkNumberFromTable + "' AND AccountNumber = '" + bankAccountNumberFromTable + "' ";
                    System.out.println("sqlQuery: " + sqlQuery);
                    resultSetForAccountDetails = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);
                    while (resultSetForAccountDetails.next()) {
                        checkMemo = resultSetForAccountDetails.getString("Memo");
                        checkAmount = AccountingUtil.formatAmountForDisplay(resultSetForAccountDetails.getString("Credit"));
                        checkDate = AccountingUtil.dateFormat.format(resultSetForAccountDetails.getObject("PostDate")).split(" ")[0];
                        paidToID = Integer.parseInt(resultSetForAccountDetails.getString("ControlNumber"));
                        paidToType = resultSetForAccountDetails.getString("GLType");
                    }
                    if ("Check - Vendor".equalsIgnoreCase(paidToType)) {
                        sqlQuery = "SELECT VendorName, VendorAddress, VendorCity, VendorState, VendorZip "
                                + "FROM VendorListTable A "
                                + "WHERE VendorId = '" + paidToID + "'";
                        ResultSet resultSetForVendorDetails = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);
                        while (resultSetForVendorDetails.next()) {
                            name = resultSetForVendorDetails.getString("VendorName");
                            address = resultSetForVendorDetails.getString("VendorAddress");
                            city = resultSetForVendorDetails.getString("VendorCity");
                            state = resultSetForVendorDetails.getString("VendorState");
                            zip = resultSetForVendorDetails.getString("VendorZip");
                        }
                    } else if ("Check - Customer".equalsIgnoreCase(paidToType)) {
                        sqlQuery = "SELECT FirstName, MiddleName, LastName, Address, City, State, Zip "
                                + "FROM CustomerTable A "
                                + "WHERE CustomerCode = '" + paidToID + "'";
                        //System.out.println("customer: " + sqlQuery);
                        ResultSet resultSetForCustomersDetails = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);
                        while (resultSetForCustomersDetails.next()) {
                            name = resultSetForCustomersDetails.getString("FirstName") + " " + resultSetForCustomersDetails.getString("MiddleName") + " " + resultSetForCustomersDetails.getString("LastName");
                            address = resultSetForCustomersDetails.getString("Address");
                            city = resultSetForCustomersDetails.getString("City");
                            state = resultSetForCustomersDetails.getString("State");
                            zip = resultSetForCustomersDetails.getString("Zip");
                        }
                    } else if ("Check - Employee".equalsIgnoreCase(paidToType)) {
                        sqlQuery = "SELECT FirstName, LastName, Address, City, State, Zip "
                                + "FROM DMSData..UsersTable "
                                + "WHERE UserID = '" + paidToID + "'";

                        ResultSet resultSetForCustomersDetails = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);
                        while (resultSetForCustomersDetails.next()) {
                            name = resultSetForCustomersDetails.getString("FirstName") + " " + resultSetForCustomersDetails.getString("LastName");
                            address = resultSetForCustomersDetails.getString("Address");
                            city = resultSetForCustomersDetails.getString("City");
                            state = resultSetForCustomersDetails.getString("State");
                            zip = resultSetForCustomersDetails.getString("Zip");
                        }
                    }

                    if (selectedChecksRowCount == 1) {
                        // If check# already exists
                        if (isCheckNumberAlreadyExists(checkNumberFromDialog)) {
                            checkNoExists = false;
                            return;
                        }
                        populateReportForSingleCheck(bankAccountNumberFromTable, checkNumberFromTable, checkMemo, checkDate,
                                checkAmount, name, address, city, state, zip,
                                fetchDataForSelectedCheque(checkNumberFromTable), checkNumberFromDialogIntVal);
                    } else {
                        populateReportsForMultipleChecks(bankAccountNumberFromTable, checkNumberFromTable, checkMemo, checkDate, checkAmount, name, address, city, state, zip,
                                fetchDataForSelectedCheque(checkNumberFromTable));
                        checkNumbersListFromDialog.add(checkNumberFromDialogIntVal);
                        checkNumbersListFromTable.add(checkNumberFromTable);
                        checkNumberFromDialogIntVal++;
                    }
                }
            } // end if
        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!checkNumbersListFromDialog.isEmpty()) {
            launchJasperDialog(0, checkNumbersListFromDialog, checkNumbersListFromTable);
        }
    }

    public void updateSingleCheckNumber(int newCheckNumber, int oldCheckNumber) {
        String[] updateQuery = new String[2];
        // AccountingCBTable
        updateQuery[0] = "UPDATE AccountingCBTable "
                + "SET CheckNumber = '" + newCheckNumber + "', Status = 'Printed' "
                + "WHERE CheckNumber = '" + oldCheckNumber + "'";

        // AccountingGLTable
        updateQuery[1] = "UPDATE AccountingGLTable "
                + "SET ReferenceNumber = '" + newCheckNumber + "' "
                + "WHERE ReferenceNumber = '" + oldCheckNumber + "' ";

        dms.DMSApp.getApplication().getDBConnection().executeStatements(updateQuery, this);
    }

    public void updateMultipleCheckNumbers(int checkNumber, int oldCheckNumber, int index) {
        // Update check# with next check# values
        String[] updateSql2 = new String[2];
        //System.out.println("index is = " + index);
        if (index == 0) {
            nextCheckNo = checkNumber;
        } else {
            nextCheckNo = ++checkNumber;
        }
        //System.out.println("nextChkno = " + nextCheckNo);

        if (isCheckNumberAlreadyExists(String.valueOf(nextCheckNo))) {
            checkNoExists = true;
            return;
        } else {
            // AccountingCBTable
            updateSql2[0] = "UPDATE AccountingCBTable "
                    + "SET CheckNumber = '" + nextCheckNo + "' "
                    + "WHERE CheckNumber = '" + oldCheckNumber + "' AND Status = 'Printed' ";

            //System.out.println("upd 2 = " + updateSql2[0]);

            // AccountingGLTable
            updateSql2[1] = "UPDATE AccountingGLTable "
                    + "SET ReferenceNumber = '" + nextCheckNo + "' "
                    + "WHERE ReferenceNumber = '" + oldCheckNumber + "' ";

            //System.out.println("upd 2-222 = " + updateSql2[1]);
        }
        dms.DMSApp.getApplication().getDBConnection().executeStatements(updateSql2, this);
        System.out.println("***** Printing Check# : ***** " + nextCheckNo);
    }

    public void populateReportsForMultipleChecks(int bankAccountNor, int chkNumber, String checkMemo, String checkDate, String checkAmount, String paidTo, String vendorAddress, String vendorCity, String vendorState, String vendorZip, List<String> checkDetailsList) {

        String reportName = "dms/panels/check.jasper";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(reportName);

        //System.out.println("checkAmount " + checkAmount);
        String checkAmountWithoutComma = checkAmount.replaceAll(",", "");
        checkAmountWithoutComma = checkAmountWithoutComma.replaceAll("-", "");
        String amountInWords = EnglishNumberToWords.convert(Long.parseLong(checkAmountWithoutComma.substring(0, checkAmountWithoutComma.indexOf("."))));
        //System.out.println("checkAmount " + checkAmount.split("\\.")[1]);

        amountInWords = amountInWords + " Dollars and . " + checkAmount.split("\\.")[1] + " Cents";

        HashMap parameters = new HashMap();
        parameters.put("bankAccount", bankAccountNo);
        parameters.put("checkDate", checkDate);
        parameters.put("checkMemo", checkMemo);
        parameters.put("paidTo", paidTo);
        parameters.put("vendorAddress", vendorAddress);
        parameters.put("vendorCity", vendorCity);
        parameters.put("vendorState", vendorState);
        parameters.put("vendorZip", vendorZip);
        parameters.put("checkAmount", checkAmount);
        parameters.put("totalAmount", checkAmount);
        //parameters.put("controlNumber", fetchControlNumForCheckNumber(checkNumber));
        parameters.put("amountInWords", amountInWords);

        for (int i = 0; i < 15; i++) {
            if (i < checkDetailsList.size()) {
                String[] currentValArray = checkDetailsList.get(i).split("\\*\\*\\*");
                String controlNumber = currentValArray[0];
                String accountNumber = currentValArray[1];
                String lineMemo = currentValArray[2];
                String lineAmount = currentValArray[3];

                parameters.put("controlNumber" + i, controlNumber);
                parameters.put("account" + i, accountNumber);
                parameters.put("linememo" + i, lineMemo);
                parameters.put("amount" + i, lineAmount);

            } else {
                parameters.put("controlNumber" + i, " ");
                parameters.put("account" + i, " ");
                parameters.put("linememo" + i, " ");
                parameters.put("amount" + i, " ");
            }
        }

        JasperPrint jasperPrint = null;
        try {
            jasperPrint = JasperFillManager.fillReport(inputStream, parameters, new JREmptyDataSource());
        } catch (JRException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        jasperReportsToPrintList.add(jasperPrint);
    }

    private void launchJasperDialog(int index, ArrayList<Integer> checkNumbersListFromDialog,
            ArrayList<Integer> checkNumbersListFromTable) {
        int currentCheckNumber = checkNumbersListFromDialog.get(index);
        if (isCheckNumberAlreadyExists(String.valueOf(currentCheckNumber))) {
        } else {
            if (!jasperReportsToPrintList.isEmpty()) {
                showNextJasperPrintDialogForMultiplePrints(index, checkNumbersListFromDialog, checkNumbersListFromTable);
            }
        }
        if (!jasperReportsToPrintList.isEmpty()) {
            jasperReportsToPrintList.remove(index);
        }
        if (!checkNumbersListFromDialog.isEmpty()) {
            checkNumbersListFromDialog.remove(index);
        }
        if (!checkNumbersListFromDialog.isEmpty()) {
            launchJasperDialog(index, checkNumbersListFromDialog, checkNumbersListFromTable);
        }
    }

    public void populateReportForSingleCheck(int bankAccountNumber, int oldCheckNumber, String checkMemo, String checkDate,
            String checkAmount, String paidTo, String vendorAddress, String vendorCity,
            String vendorState, String vendorZip, List<String> checkDetailsList, int newCheckNumber) {

        String reportName = "dms/panels/check.jasper";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(reportName);
        String checkAmountWithoutComma = checkAmount.replaceAll(",", "");
        checkAmountWithoutComma = checkAmountWithoutComma.replaceAll("-", "");
        String amountInWords = EnglishNumberToWords.convert(Long.parseLong(checkAmountWithoutComma.substring(0, checkAmountWithoutComma.indexOf("."))));
        amountInWords = amountInWords + " Dollars and . " + checkAmount.split("\\.")[1] + " Cents";
        HashMap parameters = new HashMap();
        parameters.put("bankAccount", bankAccountNumber);
        parameters.put("checkDate", checkDate);
        parameters.put("checkMemo", checkMemo);
        parameters.put("paidTo", paidTo);
        parameters.put("vendorAddress", vendorAddress);
        parameters.put("vendorCity", vendorCity);
        parameters.put("vendorState", vendorState);
        parameters.put("vendorZip", vendorZip);
        parameters.put("checkAmount", checkAmount);
        parameters.put("totalAmount", checkAmount);
        //parameters.put("controlNumber", fetchControlNumForCheckNumber(checkNumber));
        parameters.put("amountInWords", amountInWords);

        for (int i = 0; i < 15; i++) {
            if (i < checkDetailsList.size()) {
                String currentElement = checkDetailsList.get(i);
                String[] currentValArray = currentElement.split("\\*\\*\\*");
                String controlNumber = currentValArray[0];
                String accountNumber = currentValArray[1];
                String lineMemo = currentValArray[2];
                String lineAmount = currentValArray[3];

                parameters.put("controlNumber" + i, controlNumber);
                parameters.put("account" + i, accountNumber);
                parameters.put("linememo" + i, lineMemo);
                parameters.put("amount" + i, lineAmount);

            } else {
                parameters.put("controlNumber" + i, " ");
                parameters.put("account" + i, " ");
                parameters.put("linememo" + i, " ");
                parameters.put("amount" + i, " ");
            }
        }

        JasperPrint jasperPrint = null;
        try {
            jasperPrint = JasperFillManager.fillReport(inputStream, parameters, new JREmptyDataSource());
        } catch (JRException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        showJasperPrintDialog(jasperPrint, newCheckNumber, oldCheckNumber);
        reloadChecks();

    }

    private void showJasperPrintDialog(JasperPrint jasperPrint, int newCheckNumber, int oldCheckNumber) {
        try {
            boolean status = JasperPrintManager.printReport(jasperPrint, true);
            if (status == true) {
                System.out.println("Print sent for check number : " + newCheckNumber);
                updateSingleCheckNumber(newCheckNumber, oldCheckNumber);
            } else {
                System.out.println("Printing skipped for check number : " + newCheckNumber);
            }
        } catch (JRException ex) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showNextJasperPrintDialogForMultiplePrints(int jasperReportIndex,
            ArrayList<Integer> checkNumbersListFromDialog, ArrayList<Integer> checkNumbersListFromTable) {
        int newCheckNumber = checkNumbersListFromDialog.get(jasperReportIndex);
        int oldCheckNumber = checkNumbersListFromTable.get(jasperReportIndex);
        JasperPrint currentJasperPrint = jasperReportsToPrintList.get(jasperReportIndex);
        try {
            boolean status = JasperPrintManager.printReport(currentJasperPrint, true);
            System.out.println("Print sent : " + status);
            // Check if jasper report was sent for printing
            if (status == true) {
                System.out.println("Printing for check # " + newCheckNumber + " done, updating check ...");
                updateSingleCheckNumber(newCheckNumber, oldCheckNumber);
            } else {
                System.out.println("Printing for check # " + newCheckNumber + " skipped");
            }
            jasperReportsToPrintList.remove(jasperReportIndex);
            checkNumbersListFromDialog.remove(jasperReportIndex);
            checkNumbersListFromTable.remove(jasperReportIndex);
            if (jasperReportsToPrintList.isEmpty()) {
                System.out.println("Finished handling multiple jasper reports");
                reloadChecks();
                dms.DMSApp.displayMessage(this, "Handling done for selected checks.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("Launching next report");
                launchJasperDialog(jasperReportIndex, checkNumbersListFromDialog, checkNumbersListFromTable);
            }
        } catch (JRException ex) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        private void voidCheckButtoncheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_voidCheckButtoncheckButtonsClicked

            if (!voidCheckButton.isEnabled()) {
                return;
            }
            boolean sureToVoid = false;
            String checkStatus = "";
            String checkMemo = "";
            String voidCheckMemo;
            String updateSql[] = new String[1];
            String checkAccountNumber;
            String checkAccountNumberCredit = "";
            Double checkDebit;
            Double checkCredit;
            String checkControlNo = "";
            String checkGLType = "";
            boolean reverseEntries = false;
            if (checksBottomTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(checksBottomTable, "Please select a record to void", JOptionPane.WARNING_MESSAGE);
            } else if (checksBottomTable.getSelectedRowCount() > 1) {
                dms.DMSApp.displayMessage(checksBottomTable, "Please select only one record to void", JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    int checkNum = Integer.parseInt(checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString());
                    int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to void the selected check?", "Confirm",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.NO_OPTION) {
                        sureToVoid = false;
                        return;
                    } else if (response == JOptionPane.YES_OPTION) {
                        sureToVoid = true;
                    }

                    if (sureToVoid) {
                        // First Get the Status of CheckNumber from AccountingCBTable (it should be clear - to be set as VOID)
                        String sqlQuery = "Select * from AccountingCBTable "
                                + "Where CheckNumber = '" + checkNum + "' ";

                        //System.out.println("CheckStatus Sql := " + sql);
                        ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);
                        if (rs.next()) {
                            checkStatus = rs.getString("Status");
                            checkMemo = rs.getString("Memo");
                        }
                        if (checkMemo != null && !checkMemo.isEmpty()) {
                            voidCheckMemo = "VOID: " + checkMemo;
                        } else {
                            voidCheckMemo = "VOID: ";
                        }
                        // check the status of check - clear
                        if (checkStatus == null) {
                            checkStatus = "null"; //Posted
                        }
                        if (!checkStatus.equalsIgnoreCase("clear")) {
                            // Update CBTable (Memo = void : Status = VOID)
                            updateSql[0] = "UPDATE AccountingCBTable "
                                    + "SET Status = 'Void',Memo = '" + voidCheckMemo + "' "
                                    + "Where CheckNumber = '" + checkNum + "' ";

                            dms.DMSApp.getApplication().getDBConnection().executeStatements(updateSql, this);
                            reverseEntries = true;
                        }

                        // Reverse the same Entry in AccountingGLTable
                        // CASE -1  : Insert 1st Reversal
                        if (reverseEntries) {
                            String query1 = "Select AccountNumber,Debit,Credit,ControlNumber,GLType from AccountingGLTable "
                                    + "Where ReferenceNumber = '" + checkNum + "' AND Debit = '0.00' ";

                            ResultSet res = dms.DMSApp.getApplication().getDBConnection().getResultSet(query1);

                            while (res.next()) {
                                checkAccountNumberCredit = res.getString("AccountNumber");
                                checkCredit = res.getDouble("Credit");
                                checkControlNo = res.getString("ControlNumber");
                                checkGLType = res.getString("GLType");

                                // Insert Qry
                                String insertQry1[] = new String[1];
                                insertQry1[0] = "INSERT INTO AccountingGLTable "
                                        + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName) "
                                        + "VALUES ("
                                        + "'" + checkAccountNumberCredit + "','" + checkCredit + "', '0.00', "
                                        + "'" + checkControlNo + "', '" + checkNum + "', "
                                        + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + checkGLType + "', "
                                        + "'" + voidCheckMemo + "', "
                                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                                        + ")";

                                System.out.println("Insert1 Qry 1 := " + insertQry1[0]);
                                dms.DMSApp.getApplication().getDBConnection().executeStatements(insertQry1, this);
                            }

                            // CASE -2 : Insert 2nd Reversal
                            String query2 = "Select AccountNumber,Debit,Credit,ControlNumber,GLType from AccountingGLTable "
                                    + "Where ReferenceNumber = '" + checkNum + "' AND Credit = '0.00' AND AccountNumber NOT IN ('" + checkAccountNumberCredit + "') ";

                            System.out.println("q2 := " + query2);
                            ResultSet resultSet = dms.DMSApp.getApplication().getDBConnection().getResultSet(query2);

                            while (resultSet.next()) {
                                checkAccountNumber = resultSet.getString("AccountNumber");
                                checkDebit = resultSet.getDouble("Debit");
                                checkControlNo = resultSet.getString("ControlNumber");
                                checkGLType = resultSet.getString("GLType");

                                String insertQry2[] = new String[1];
                                insertQry2[0] = "INSERT INTO AccountingGLTable "
                                        + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName) "
                                        + "VALUES ("
                                        + "'" + checkAccountNumber + "','0.00', '" + checkDebit + "', "
                                        + "'" + checkControlNo + "', '" + checkNum + "', "
                                        + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + checkGLType + "', "
                                        + "'" + voidCheckMemo + "', "
                                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                                        + ")";

                                System.out.println("Insert1 Qry 2:= " + insertQry2[0]);
                                dms.DMSApp.getApplication().getDBConnection().executeStatements(insertQry2, this);
                            }
                        }
                        reloadChecks();
                        dms.DMSApp.displayMessage(this, "Selected check voided successfully", dms.DMSApp.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }//GEN-LAST:event_voidCheckButtoncheckButtonsClicked

        private void apCheckbookPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_apCheckbookPanelComponentShown
            setRefNumValueForTable(checkbookAccountsTable, checkBookNumber.getText().toString());
        }//GEN-LAST:event_apCheckbookPanelComponentShown

        private void jideTabbedPane4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jideTabbedPane4MouseClicked
        }//GEN-LAST:event_jideTabbedPane4MouseClicked

        private void jButton47saveCheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton47saveCheckButtonsClicked
            if (checkSave()) {
                checksBottomTable.setRowSelectionInterval(0, 0);
                System.out.println("Printing check after saving ... ");
                int selectedCheckNumber = Integer.parseInt(checksBottomTable.getValueAt(
                        checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString());
                printSelectedCheck(selectedCheckNumber);
            }
        }//GEN-LAST:event_jButton47saveCheckButtonsClicked

        private void clearCheck(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearCheck
            clearCheck();
            AccountingUtil.showMessageBalloon("Check data cleared, keeping the minimum required one account line intact", checkbookAccountsTable);
        }//GEN-LAST:event_clearCheck

        private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
            DefaultTableModel aModel = (DefaultTableModel) checkbookAccountsTable.getModel();
            int rowCount = checkbookAccountsTable.getRowCount();
            checkbookRowNo = rowCount;
            if (rowCount == 0) {
                addNewRowCheckbook(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
            }

            if (isRowRemoved) {
                checkbookRowNo = rowCount;
                isRowRemoved = false;
            }
            if (checkbookAccountsTable.getValueAt(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Account")) != null) {
                account = checkbookAccountsTable.getValueAt(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Account")).toString();
            }
            if (checkbookAccountsTable.getValueAt(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Amount")) != null) {
                amount = checkbookAccountsTable.getValueAt(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Amount")).toString();
            }
            if (account == null) {
                dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.WARNING_MESSAGE);
                checkbookAccountsTable.setCellSelectionEnabled(true);
                checkbookAccountsTable.changeSelection(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Account"), false, false);
                checkbookAccountsTable.requestFocus();
            }
            if (amount == null || amount.isEmpty()) {
                dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.WARNING_MESSAGE);
                checkbookAccountsTable.setCellSelectionEnabled(true);
                checkbookAccountsTable.changeSelection(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
                checkbookAccountsTable.requestFocus();
            } else if (amount != null) {
                if (!AccountingUtil.displayNumeric(amount)) {
                    //System.out.println("Amount is not Numeric");
                    dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                    checkbookAccountsTable.setCellSelectionEnabled(true);
                    checkbookAccountsTable.changeSelection(checkbookRowNo - 1, checkbookAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
                    checkbookAccountsTable.requestFocus();
                }
            }
            if (isControlNumRequiredForTable(checkbookAccountsTable)) {
                dms.DMSApp.displayMessage(this,
                        "Please provide Control # value for current row", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // check to insert a row
            if (account != null && amount != null && AccountingUtil.displayNumeric(amount)) {
                addNewRowCheckbook(aModel, account);
                checkbookRowNo++;
            }
            //addNewRowCheckbook(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
        }//GEN-LAST:event_jButton20ActionPerformed

        private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
            int rowCount = checkbookAccountsTable.getRowCount();
            if (rowCount == 1) {
                dms.DMSApp.displayMessage(this, "At least one account is required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DefaultTableModel tableModel = (DefaultTableModel) checkbookAccountsTable.getModel();
            tableModel.removeRow(tableModel.getRowCount() - 1);
            rowCount--;
            isRowRemoved = true;
            double sum = 0.00;
            for (int i = 0; i < rowCount; i++) {
                if (checkbookAccountsTable.getValueAt(i, 1) != null && !checkbookAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                    if (AccountingUtil.displayNumeric(checkbookAccountsTable.getValueAt(i, 1).toString())) {
                        if (checkbookAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                            sum += Double.parseDouble(checkbookAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                        } else {
                            sum += Double.parseDouble(checkbookAccountsTable.getValueAt(i, 1).toString());
                            //System.out.println("sum 2:= " + sum);
                        }
                    }
                }
            } // end for loop
            checksTotalAccounts.setValue(sum);
        }//GEN-LAST:event_jButton21ActionPerformed

        private void checkbookBankCombobankSelected(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_checkbookBankCombobankSelected
        }//GEN-LAST:event_checkbookBankCombobankSelected

    private void setRefNumValueForTable(JTable table, String fieldNumber) {
        int referenceColumn = AccountingUtil.getColumnByName(table, "Reference #");
        if (table.getRowCount() > 1) {
            for (int i = 0; i < table.getRowCount(); i++) {
                table.setValueAt(fieldNumber, i, referenceColumn);
            }
        } else {
            table.setValueAt(fieldNumber, 0, referenceColumn);
        }
    }
        private void checkbookBankComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkbookBankComboItemStateChanged
            bankSelected();
        }//GEN-LAST:event_checkbookBankComboItemStateChanged

        private void checkbookPaytoPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_checkbookPaytoPopupMenuWillBecomeInvisible
            //System.out.println("Selected: " + checkbookPayto.getSelectedItem().toString());
/*
             if (!checkbookPayto.getSelectedItem().toString().isEmpty()
             || !"".equals(checkbookPayto.getSelectedItem().toString())) {
             for (int i = 0; i < checkbookAccountsTable.getRowCount(); i++) {
             int controlColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Control #");
             int vendorId = AccountingUtil.getVendorID(checkbookPayto.getSelectedItem().toString());
             checkbookAccountsTable.setValueAt(vendorId, i, controlColumn);
             }
             }
             */
        }//GEN-LAST:event_checkbookPaytoPopupMenuWillBecomeInvisible

        private void checkBookNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_checkBookNumberFocusLost
            String checkNumber = "";
            int referenceColumn;

            for (int i = 0; i < checkbookAccountsTable.getRowCount(); i++) {
                referenceColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Reference #");

                if (checkbookCheck.isSelected()) {
                    checkNumber = checkBookNumber.getText().toString();
                }
                if (checkbookAch.isSelected()) {
                    checkNumber = checkBookNumber.getText().toString();
                }

                //System.out.println("ref: " + referenceColumn + " check: " + checknumber);
                checkbookAccountsTable.setValueAt(checkNumber, i, referenceColumn);
            }
        }//GEN-LAST:event_checkBookNumberFocusLost

        private void checkBookNumberInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_checkBookNumberInputMethodTextChanged
        }//GEN-LAST:event_checkBookNumberInputMethodTextChanged

        private void checkBookNumberKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkBookNumberKeyReleased
        }//GEN-LAST:event_checkBookNumberKeyReleased

        private void checkbookAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkbookAmountActionPerformed
        }//GEN-LAST:event_checkbookAmountActionPerformed

        private void checkbookCheckMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkbookCheckMouseClicked
            checkBookNumber.setEditable(true);
            checkBookNumber.setText("");
            bankSelected();

            int referenceColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Reference #");

            int checknumber = 0;
            if (!checkBookNumber.getText().isEmpty()) {
                checknumber = Integer.parseInt(checkBookNumber.getText().toString());
            }
            if (checkbookAccountsTable.getRowCount() > 1) {
                for (int i = 0; i < checkbookAccountsTable.getRowCount(); i++) {
                    checkbookAccountsTable.setValueAt(checknumber, i, referenceColumn);
                }
            } else {
                checkbookAccountsTable.setValueAt(checknumber, 0, referenceColumn);
            }
        }//GEN-LAST:event_checkbookCheckMouseClicked

        private void checkbookAchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkbookAchMouseClicked
            checkBookNumber.setEditable(false);

            bankSelected();

            int referenceColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Reference #");

            int checknumber = 0;
            if (!checkBookNumber.getText().isEmpty()) {
                checknumber = Integer.parseInt(checkBookNumber.getText().toString());
            }
            if (checkbookAccountsTable.getRowCount() > 1) {
                for (int i = 0; i < checkbookAccountsTable.getRowCount(); i++) {
                    checkbookAccountsTable.setValueAt(checknumber, i, referenceColumn);
                }
            } else {
                checkbookAccountsTable.setValueAt(checknumber, 0, referenceColumn);
            }
        }//GEN-LAST:event_checkbookAchMouseClicked

        private void checksTotalAccountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checksTotalAccountsActionPerformed
        }//GEN-LAST:event_checksTotalAccountsActionPerformed

        private void depositsNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_depositsNumberFocusLost

            for (int i = 0; i < depositsAccountsTable.getRowCount(); i++) {
                int referenceColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Reference #");
                int depositnumber = 0;
                if (depositsNumber.getText().toString() != null && depositsNumber.getText().toString().trim().length() > 0) {
                    depositnumber = Integer.parseInt(depositsNumber.getText().toString());
                }
                //System.out.println("ref: " + referenceColumn + " check: " + checknumber);
                depositsAccountsTable.setValueAt(depositnumber, i, referenceColumn);
            }

        }//GEN-LAST:event_depositsNumberFocusLost

        private void depositsNumberInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_depositsNumberInputMethodTextChanged
        }//GEN-LAST:event_depositsNumberInputMethodTextChanged

        private void depositsNumberKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_depositsNumberKeyReleased
        }//GEN-LAST:event_depositsNumberKeyReleased

        private void depositsAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositsAmountActionPerformed
        }//GEN-LAST:event_depositsAmountActionPerformed

        private void depositsDepositMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsDepositMouseClicked
            bankDepositsSelected();

            int referenceColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Reference #");

            int depositNumber = 0;
            if (!depositsNumber.getText().isEmpty()) {
                depositNumber = Integer.parseInt(depositsNumber.getText().toString());
            }
            if (depositsAccountsTable.getRowCount() > 1) {
                for (int i = 0; i < depositsAccountsTable.getRowCount(); i++) {
                    depositsAccountsTable.setValueAt(depositNumber, i, referenceColumn);
                }
            } else {
                depositsAccountsTable.setValueAt(depositNumber, 0, referenceColumn);
            }

        }//GEN-LAST:event_depositsDepositMouseClicked

        private void depositsACHMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsACHMouseClicked
            bankDepositsSelected();

            int referenceColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Reference #");

            int depositNumber = 0;
            if (!depositsNumber.getText().isEmpty()) {
                depositNumber = Integer.parseInt(depositsNumber.getText().toString());
            }
            if (depositsAccountsTable.getRowCount() > 1) {
                for (int i = 0; i < depositsAccountsTable.getRowCount(); i++) {
                    depositsAccountsTable.setValueAt(depositNumber, i, referenceColumn);
                }
            } else {
                depositsAccountsTable.setValueAt(depositNumber, 0, referenceColumn);
            }

        }//GEN-LAST:event_depositsACHMouseClicked

        private void depositsTotalAccountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositsTotalAccountsActionPerformed
        }//GEN-LAST:event_depositsTotalAccountsActionPerformed

        private void saveDepositButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveDepositButton
            boolean good = true;
            String bankNumber;
            int depositsRowCount = depositsAccountsTable.getRowCount();
            if (depositsRowCount == 0) {
                dms.DMSApp.displayMessage(this, "Please select an account first", dms.DMSApp.WARNING_MESSAGE);
                addNewDepositsRow((DefaultTableModel) depositsAccountsTable.getModel(), account);
                depositRowNo++;
                return;
            }

            if (depositsNumber.getText().isEmpty()) {
                dms.DMSApp.displayMessage(this, "The deposit# is blank", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (!AccountingUtil.isNumeric(depositsNumber.getText().toString())) {
                dms.DMSApp.displayMessage(this, "The deposit# can only be Numeric ", dms.DMSApp.WARNING_MESSAGE);
                return;
            }

            int depNo = Integer.parseInt(depositsNumber.getText().toString());
            //System.out.println("depNo at 1 = " + depNo);
            while (isDepositNumberAlreadyExists(depNo)) {
                dms.DMSApp.displayMessage(this, "The deposit# " + depNo + " already exists \n Incrementing deposit# and checking...", dms.DMSApp.WARNING_MESSAGE);
                ++depNo;
                System.out.println("depNo at 2nd = " + depNo);
            }
            //System.out.println("after loop..");
            if (!depositsBankCombo.getSelectedItem().equals("")) {
                bankNumber = "" + depositsBankCombo.getSelectedItem();
                bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
            } else {
                dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (depositsAmount.getText().isEmpty()) {
                dms.DMSApp.displayMessage(this, "The desposit amount is empty", dms.DMSApp.WARNING_MESSAGE);
                return;
            } else {
                if (depositsTotalAccounts.getText() != null && !depositsTotalAccounts.getText().isEmpty()) {
                    String depositsTotalAccountsStringVal = depositsTotalAccounts.getText().toString();
                    if (depositsTotalAccountsStringVal.contains(",")) {
                        depositsTotalAccountsStringVal = depositsTotalAccountsStringVal.replaceAll(",", "");
                    }

                    String depositsAmountStringVal = depositsAmount.getText().toString();
                    if (depositsAmountStringVal.contains(",")) {
                        depositsAmountStringVal = depositsAmountStringVal.replaceAll(",", "");
                    }

                    Double billAmountDoubleVal = Double.parseDouble(depositsAmountStringVal);
                    Double billTotalExpensesDoubleVal = Double.parseDouble(depositsTotalAccountsStringVal);

                    System.out.println("billAmountDoubleVal: " + billAmountDoubleVal);
                    System.out.println("billTotalExpensesDoubleVal: " + billTotalExpensesDoubleVal);

                    int retval = Double.compare(billAmountDoubleVal, billTotalExpensesDoubleVal);
                    if (retval != 0) {
                        good = false;
                        dms.DMSApp.displayMessage(this, "The deposit amount and accounts total are not the same", dms.DMSApp.WARNING_MESSAGE);
                        return;
                    }
                } else {
                    String depositsAmountStringVal = depositsAmount.getText().toString();
                    if (depositsAmountStringVal.contains(",")) {
                        depositsAmountStringVal = depositsAmountStringVal.replaceAll(",", "");
                    }
                    double sum = 0.00;
                    String amountTotalLines;
                    String finalLinesAmount = "";
                    int rowCount = depositsAccountsTable.getRowCount();
                    if (rowCount > 0) {
                        for (int i = 0; i < rowCount; i++) {
                            if (depositsAccountsTable.getValueAt(i, 1) != null && !depositsAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                                if (AccountingUtil.displayNumeric(depositsAccountsTable.getValueAt(i, 1).toString())) {
                                    if (depositsAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                                        sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                                    } else {
                                        sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString());
                                    }
                                }
                            }

                        } // end for loop
                        amountTotalLines = Double.valueOf(sum).toString();
                        finalLinesAmount = AccountingUtil.formatAmountForDisplay(amountTotalLines);
                    }
                    if (depositsAmountStringVal.equals(finalLinesAmount)) {
                        depositsTotalAccounts.setText(finalLinesAmount);
                    } else {
                        good = false;
                        dms.DMSApp.displayMessage(this, "The deposit accounts total amount is empty", dms.DMSApp.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            if (depositsRowCount > 0) {
                int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Account");
                int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Control #");

                for (int i = 0; i <= depositsAccountsTable.getRowCount() - 1; i++) {
                    int accountNumber = -1;
                    Object accNumString = depositsAccountsTable.getValueAt(i, accountColumn);
                    if (accNumString == null) {
                        dms.DMSApp.displayMessage(this,
                                "Please select account first", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!accNumString.toString().isEmpty()) {
                        accountNumber = Integer.valueOf(accNumString.toString().split("-")[0].trim());
                    }
                    //System.out.println("Account Number : " + accountNumber);
                    Boolean isControlled = AccountingUtil.getControlNumMap().get(accountNumber);
                    if (isControlled && depositsAccountsTable.getValueAt(i, controlColumn) == null) {
                        good = false;
                        dms.DMSApp.displayMessage(this,
                                "Please provide Control # value for Row." + ++i, JOptionPane.WARNING_MESSAGE);
                    } else if (isControlled
                            && depositsAccountsTable.getValueAt(i, controlColumn) != null
                            && depositsAccountsTable.getValueAt(i, controlColumn).toString().isEmpty()) {
                        dms.DMSApp.displayMessage(this,
                                "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

            } else {
                good = false;
            }

            try {

                if (good) {
                    sql = new String[4];
                    //System.out.println("inside good.....");
                    String depositType = "";
                    String depositLineType = "";
                    if (depositsDeposit.isSelected()) {
                        depositType = "Deposit";
                        depositLineType = "Deposit Line";
                    } else {
                        depositType = "ACH Deposit";
                        depositLineType = "ACH Deposit Line";
                    }
                    // Checkbook Table: enter check information in the checkbook table
                    sql[0] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, DepositNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Type, Status) "
                            + "VALUES ("
                            + "'1', '" + depNo + "', '" + bankNumber + "', '0', "
                            + "'', '" + AccountingUtil.dateFormat.format(depositsDate.getDate()) + "', '"
                            + depositsAmount.getText() + "', '" + depositsMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'" + depositType + "', 'Posted'"
                            + ") ";

                    System.out.println("AccountingCBTable Qry = " + sql[0]);
                    // Journal Entry for check. Control = Check Number and Reference is Paid To Id
                    String class_deposit = billClass_deposit.getSelectedItem().toString();

                    sql[1] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                            + "VALUES ("
                            + "'" + bankNumber + "', '" + depositsAmount.getText() + "','0.00', '', "
                            + "'" + depNo + "', " + "'" + AccountingUtil.dateFormat.format(depositsDate.getDate())
                            + "', '" + depositType + "', '" + depositsMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' , '" + class_deposit + "' "
                            + ")";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                    String[] query = new String[1];

                    String accountNumber;
                    Double amountValue;
                    String controlNumber;
                    String referenceNumber;
                    String memo;

                    int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Account");
                    int amountColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Amount");
                    int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Control #");
                    int referenceColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Reference #");
                    int memoColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Memo");

                    for (int i = 0; i <= depositsAccountsTable.getRowCount() - 1; i++) {
                        accountNumber = null;
                        amountValue = 0.00;
                        controlNumber = null;
                        referenceNumber = null;
                        memo = null;

                        if (depositsAccountsTable.getValueAt(i, accountColumn) != null) {
                            accountNumber = depositsAccountsTable.getValueAt(i, accountColumn).toString();
                            accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
                        }
                        Object amountVal = depositsAccountsTable.getValueAt(i, amountColumn);
                        if (amountVal != null && !amountVal.toString().isEmpty()) {
                            if (amountVal.toString().contains(",")) {
                                amountVal = amountVal.toString().replaceAll(",", "");
                            }
                            amountValue = Double.parseDouble(amountVal.toString());
                        } else {
                            amountValue = 0.00;
                        }
                        if (depositsAccountsTable.getValueAt(i, controlColumn) != null) {
                            controlNumber = depositsAccountsTable.getValueAt(i, controlColumn).toString();
                        } else {
                            controlNumber = "";
                        }
                        if (i == 0) {
                            referenceNumber = depositsNumber.getText().toString();
                        }
                        if (i > 0) {
                            if (depositsAccountsTable.getValueAt(i, referenceColumn) != null) {
                                referenceNumber = depositsAccountsTable.getValueAt(i, referenceColumn).toString();
                            } else {
                                referenceNumber = "";
                            }
                        }
                        if (depositsAccountsTable.getValueAt(i, memoColumn) != null) {
                            memo = depositsAccountsTable.getValueAt(i, memoColumn).toString();
                        } else {
                            memo = "";
                        }

                        query[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                                + "ReferenceNumber, PostDate, Memo, GLType, LotName, Class) VALUES("
                                + "'" + accountNumber + "', '0.00', '" + amountValue + "', '" + controlNumber + "', '"
                                + referenceNumber + "', '" + AccountingUtil.dateFormat.format(depositsDate.getDate()) + "', '"
                                + memo + "','" + depositLineType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' , '" + class_deposit + "' "
                                + ")";

                        //System.out.println("Insert query : " + query[0]);
                        dms.DMSApp.getApplication().getDBConnection().executeStatements(query, this);
                    }
                    clearDeposits();
                    reloadDeposits();
                    dms.DMSApp.displayMessage(this, "Deposit saved successfully", dms.DMSApp.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
                dms.DMSApp.displayMessage(this, "Error saving desposit", dms.DMSApp.ERROR_MESSAGE);
            }
        }//GEN-LAST:event_saveDepositButton

        private void jButton48saveCheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton48saveCheckButtonsClicked
            // Save Deposit
            boolean good = true;
            String bankNumber = "";
            int depositsRowCount = depositsAccountsTable.getRowCount();


            if (depositsNumber.getText().isEmpty()) {
                dms.DMSApp.displayMessage(this, "The deposit# is blank", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (!AccountingUtil.isNumeric(depositsNumber.getText().toString())) {
                good = false;
                dms.DMSApp.displayMessage(this, "The deposit# can only be Numeric ", dms.DMSApp.WARNING_MESSAGE);
                return;
            }

            int depNo = Integer.parseInt(depositsNumber.getText().toString());
            //System.out.println("depNo at 1 = " + depNo);
            while (isDepositNumberAlreadyExists(depNo)) {
                dms.DMSApp.displayMessage(this, "The deposit# " + depNo + " already exists \n Incrementing deposit# and checking...", dms.DMSApp.WARNING_MESSAGE);
                ++depNo;
                //System.out.println("depNo at 2nd = " + depNo);
            }
            //System.out.println("after loop..");

            if (!depositsBankCombo.getSelectedItem().equals("")) {
                bankNumber = "" + depositsBankCombo.getSelectedItem();
                bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
            } else {
                good = false;
                dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (depositsAmount.getText().isEmpty()) {
                dms.DMSApp.displayMessage(this, "The desposit amount is empty", dms.DMSApp.WARNING_MESSAGE);
                return;
            } else {
                if (depositsTotalAccounts.getText() != null && !depositsTotalAccounts.getText().isEmpty()) {
                    String depositsTotalAccountsStringVal = depositsTotalAccounts.getValue().toString();
                    if (depositsTotalAccountsStringVal.contains(",")) {
                        depositsTotalAccountsStringVal = depositsTotalAccountsStringVal.replaceAll(",", "");
                    }

                    String depositsAmountStringVal = depositsAmount.getText().toString();
                    if (depositsAmountStringVal.contains(",")) {
                        depositsAmountStringVal = depositsAmountStringVal.replaceAll(",", "");
                    }

                    if (Double.parseDouble(depositsTotalAccountsStringVal)
                            != Double.parseDouble(depositsAmountStringVal)) {
                        good = false;
                        dms.DMSApp.displayMessage(this, "The deposit amount and accounts total are not the same", dms.DMSApp.WARNING_MESSAGE);
                        return;
                    }
                } else {
                    good = false;
                    dms.DMSApp.displayMessage(this, "The deposit accounts total amount is empty", dms.DMSApp.WARNING_MESSAGE);
                    return;
                }
            }

            if (depositsRowCount > 0) {

                int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Account");
                int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Control #");

                for (int i = 0; i <= depositsAccountsTable.getRowCount() - 1; i++) {
                    String accountNumber = null;
                    accountNumber = null;

                    if (depositsAccountsTable.getValueAt(i, accountColumn) != null) {
                        accountNumber = depositsAccountsTable.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.split("-")[0];
                    }

                    Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                    //System.out.println("isControlled : " + isControlled);
                    if (isControlled && depositsAccountsTable.getValueAt(i, controlColumn) == null) {
                        good = false;
                        dms.DMSApp.displayMessage(this,
                                "Please provide Control # value for Row." + ++i, JOptionPane.WARNING_MESSAGE);
                    } else if (isControlled
                            && depositsAccountsTable.getValueAt(i, controlColumn) != null
                            && depositsAccountsTable.getValueAt(i, controlColumn).toString().isEmpty()) {
                        good = false;
                        dms.DMSApp.displayMessage(this,
                                "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

            } else {
                good = false;
            }

            try {

                if (good) {
                    sql = new String[4];
                    String depositType = "";
                    String depositLineType = "";
                    if (depositsDeposit.isSelected()) {
                        depositType = "Deposit";
                        depositLineType = "Deposit Line";
                    } else {
                        depositType = "ACH Deposit";
                        depositLineType = "ACH Deposit Line";
                    }

                    // Checkbook Table: enter check information in the checkbook table
                    sql[0] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, DepositNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Type) "
                            + "VALUES ("
                            + "'1', '" + depositsNumber.getText() + "', '" + bankNumber + "', '0', "
                            + "'', '" + AccountingUtil.dateFormat.format(depositsDate.getDate()) + "', '"
                            + depositsAmount.getText() + "', '" + depositsMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'" + depositType + "' "
                            + ") ";

                    // Journal Entry for check. Control = Check Number and Reference is Paid To Id
                    sql[1] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                            + "VALUES ("
                            + "'" + bankNumber + "', '" + depositsAmount.getText() + "','0.00', '', "
                            + "'" + depositsNumber.getText() + "', " + "'" + AccountingUtil.dateFormat.format(depositsDate.getDate()) + "', '" + depositType + "', "
                            + "'" + depositsMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'" + billClass_deposit.getSelectedItem().toString() + "' "
                            + ")";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                    String[] sql1 = new String[1];

                    String accountNumber;
                    Double amount;
                    String controlNumber;
                    String referenceNumber;
                    String memo;

                    int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Account");
                    int amountColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Amount");
                    int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Control #");
                    int referenceColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Reference #");
                    int memoColumn = AccountingUtil.getColumnByName(depositsAccountsTable, "Memo");

                    for (int i = 0; i <= depositsAccountsTable.getRowCount() - 1; i++) {
                        accountNumber = null;
                        amount = 0.00;
                        controlNumber = null;
                        referenceNumber = null;
                        memo = null;

                        if (depositsAccountsTable.getValueAt(i, accountColumn) != null) {
                            accountNumber = depositsAccountsTable.getValueAt(i, accountColumn).toString();
                            accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
                        }
                        Object amountVal = depositsAccountsTable.getValueAt(i, amountColumn);
                        if (amountVal != null && !amountVal.toString().isEmpty()) {
                            if (amountVal.toString().contains(",")) {
                                amountVal = amountVal.toString().replaceAll(",", "");

                            }
                            amount = Double.parseDouble(amountVal.toString());
                        } else {
                            amount = 0.00;
                        }
                        if (depositsAccountsTable.getValueAt(i, controlColumn) != null) {
                            controlNumber = depositsAccountsTable.getValueAt(i, controlColumn).toString();
                        } else {
                            controlNumber = "";
                        }
                        if (depositsAccountsTable.getValueAt(i, referenceColumn) != null) {
                            referenceNumber = depositsAccountsTable.getValueAt(i, referenceColumn).toString();
                        } else {
                            referenceNumber = "";
                        }
                        if (depositsAccountsTable.getValueAt(i, memoColumn) != null) {
                            memo = depositsAccountsTable.getValueAt(i, memoColumn).toString();
                        } else {
                            memo = "";
                        }

                        sql1[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                                + "ReferenceNumber, PostDate, Memo, GLType, LotName, Class) VALUES("
                                + "'" + accountNumber + "', '0.00', '" + amount + "', '" + controlNumber + "', '"
                                + referenceNumber + "', '" + AccountingUtil.dateFormat.format(depositsDate.getDate()) + "', '"
                                + memo + "', '" + depositLineType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                                + "'" + billClass_deposit.getSelectedItem().toString() + "' "
                                + ")";

                        dms.DMSApp.getApplication().getDBConnection().executeStatements(sql1, this);

                    }

                    reloadDeposits();
                    clearDeposits();
                }

            } catch (Exception e) {
                dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
            }
            // Print Deposit
        }//GEN-LAST:event_jButton48saveCheckButtonsClicked

        private void clearDeposit(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearDeposit
            // TODO add your handling code here:
            depositsAmount.setText("0.00");
            depositsNumber.setText("");
            depositsMemo.setText("");
            depositsTotalAccounts.setText("0.00");

            DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable.getModel();
            clearTableModelForBankingPanel(aModel);
            AccountingUtil.showMessageBalloon("Deposit data cleared, keeping the minimum required one account line intact", depositsAccountsTable);
        }//GEN-LAST:event_clearDeposit

        private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
            DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable.getModel();
            int rowCount = depositsAccountsTable.getRowCount();
            depositRowNo = rowCount;
            if (rowCount == 0) {
                addNewDepositsRow(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
            }

            if (isRowRemoved) {
                //System.out.println("inside if for row del.");
                depositRowNo = rowCount;
                isRowRemoved = false;
            }
            //System.out.println("depositRowNo : " + depositRowNo);
            if (depositRowNo == 0) {
                return;
            }
            if (depositsAccountsTable.getValueAt(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Account")) != null) {
                account = depositsAccountsTable.getValueAt(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Account")).toString();
            }
            if (depositsAccountsTable.getValueAt(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Amount")) != null) {
                amount = depositsAccountsTable.getValueAt(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Amount")).toString();
            }
            if (account == null) {
                dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.WARNING_MESSAGE);
                depositsAccountsTable.setCellSelectionEnabled(true);
                depositsAccountsTable.changeSelection(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Account"), false, false);
                depositsAccountsTable.requestFocus();
            }
            if (amount == null || amount.isEmpty()) {
                dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.WARNING_MESSAGE);
                depositsAccountsTable.setCellSelectionEnabled(true);
                depositsAccountsTable.changeSelection(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
                depositsAccountsTable.requestFocus();
            } else if (amount != null) {
                if (!AccountingUtil.displayNumeric(amount)) {
                    //System.out.println("Amount is not Numeric");
                    dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                    depositsAccountsTable.setCellSelectionEnabled(true);
                    depositsAccountsTable.changeSelection(depositRowNo - 1, depositsAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
                    depositsAccountsTable.requestFocus();
                }
            }
            if (isControlNumRequiredForTable(depositsAccountsTable)) {
                dms.DMSApp.displayMessage(this,
                        "Please provide Control # value for current row", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // check to insert a row
            if (account != null && amount != null && AccountingUtil.displayNumeric(amount)) {
                addNewDepositsRow(aModel, account);
                depositRowNo++;
            }

        }//GEN-LAST:event_jButton22ActionPerformed

        private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
            int rowCount = depositsAccountsTable.getRowCount();
            if (rowCount == 1) {
                dms.DMSApp.displayMessage(this, "At least one account is required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DefaultTableModel tableModel = (DefaultTableModel) depositsAccountsTable.getModel();
            tableModel.removeRow(tableModel.getRowCount() - 1);
            rowCount--;
            isRowRemoved = true;
            double sum = 0.00;
            for (int i = 0; i < rowCount; i++) {
                if (depositsAccountsTable.getValueAt(i, 1) != null && !depositsAccountsTable.getValueAt(i, 1).toString().isEmpty()) {
                    if (AccountingUtil.displayNumeric(depositsAccountsTable.getValueAt(i, 1).toString())) {
                        if (depositsAccountsTable.getValueAt(i, 1).toString().contains(",")) {
                            sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                        } else {
                            sum += Double.parseDouble(depositsAccountsTable.getValueAt(i, 1).toString());
                            //System.out.println("sum 2:= " + sum);
                        }
                    }
                }
            }
            depositsTotalAccounts.setValue(sum);
        }//GEN-LAST:event_jButton23ActionPerformed

        private void depositsAccountsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsAccountsTableMouseExited
            // TODO add your handling code here:
        }//GEN-LAST:event_depositsAccountsTableMouseExited

        private void depositsAccountsTableFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_depositsAccountsTableFocusLost
            // TODO add your handling code here:
        }//GEN-LAST:event_depositsAccountsTableFocusLost

        private void depositsAccountsTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_depositsAccountsTableKeyReleased
            /*if (evt.getKeyCode() == 10) {
             DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable.getModel();
             int rowCount = depositsAccountsTable.getRowCount();
             //System.out.println("RowCount = " + rowCount);
             if (isRowRemoved) {
             //System.out.println("inside if for row del.");
             depositRowNo = rowCount - 1;
             isRowRemoved = false;
             }
             //System.out.println("rno = " + depositRowNo);
             if (depositsAccountsTable.getValueAt(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Account")) != null) {
             account = depositsAccountsTable.getValueAt(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Account")).toString();
             }
             if (depositsAccountsTable.getValueAt(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Amount")) != null) {
             amount = depositsAccountsTable.getValueAt(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Amount")).toString();
             }
             //System.out.println("account := " + account);
             //System.out.println("amount := " + amount);
             if (account == null) {
             dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.ERROR_MESSAGE);
             depositsAccountsTable.setCellSelectionEnabled(true);
             depositsAccountsTable.changeSelection(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Account"), false, false);
             depositsAccountsTable.requestFocus();
             }
             if (amount == null || amount.isEmpty()) {
             dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.ERROR_MESSAGE);
             depositsAccountsTable.setCellSelectionEnabled(true);
             depositsAccountsTable.changeSelection(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
             depositsAccountsTable.requestFocus();
             } else if (amount != null) {
             if (!displayNumeric(amount)) {
             //System.out.println("Amount is not Numeric");
             dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
             depositsAccountsTable.setCellSelectionEnabled(true);
             depositsAccountsTable.changeSelection(depositRowNo, depositsAccountsTable.getColumnModel().getColumnIndex("Amount"), false, false);
             depositsAccountsTable.requestFocus();
             }
             }
             // check to insert a row
             if (account != null && amount != null && displayNumeric(amount)) {
             addNewRow(aModel, account);
             depositRowNo++;
             }
             }
             if (evt.isControlDown() && evt.getKeyCode() == 83) {
             saveDepositButton(null);
             }*/
        }//GEN-LAST:event_depositsAccountsTableKeyReleased

        private void jButton32refreshButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton32refreshButtonsClicked
            // TODO add your handling code here:
            jDateChooser14.setDate(null);
            jDateChooser15.setDate(null);
        }//GEN-LAST:event_jButton32refreshButtonsClicked

        private void depositsSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsSearchButtonMouseClicked
            try {
                String sql = "";
                String searchFilter = "";
                String depositSearch = depositsSearchTextField.getText().toString();
                int depositSearchInt = 0;

                if (depositSearch == null || depositSearch.isEmpty()) {
                    searchFilter = "(BankAccount = '202' OR BankAccount = '203') ";
                } else {
                    if (AccountingUtil.isNumeric(depositSearch)) {
                        depositSearchInt = Integer.parseInt(depositSearch);
                        searchFilter = "DepositNumber LIKE '%" + depositSearchInt + "%' "
                                + "OR BankAccount LIKE '%" + depositSearchInt + "%' "
                                + "OR Memo LIKE '%" + depositSearchInt + "%' ";
                    } else {
                        searchFilter = "PaidTo LIKE '%" + depositSearch + "%' "
                                + "OR Memo LIKE '%" + depositSearch + "%' ";
                    }
                }

                sql = "SELECT CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description,DepositNumber,"
                        + "CAST(ROUND(Amount,2) AS NUMERIC(10,2)),Date,Memo "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingCOATable B "
                        + "ON A.GLAccount = B.AccountNumber "
                        + "LEFT JOIN AccountingCOATable C "
                        + "ON A.BankAccount = C.AccountNumber "
                        + "WHERE DepositNumber <> 0 AND "
                        + searchFilter
                        + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + "ORDER BY DepositNumber Desc ";

                sql = "SELECT "
                        + " CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description, A.DepositNumber, "
                        + "CAST(ROUND(A.Amount,2) AS NUMERIC(10,2)) AS Amount,A.Date,A.Memo, A.Type, A.Status "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingCOATable C ON (A.BankAccount = C.AccountNumber) "
                        + "WHERE "
                        + searchFilter
                        + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + "AND A.Type LIKE '%Deposit%' "
                        + "ORDER BY CheckNumber DESC";

                System.out.println("search Sql = " + sql);
                ResultSet rs;
                rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                DefaultTableModel aModel = (DefaultTableModel) depositsBottomTable.getModel();
                AccountingUtil.clearTableModel(aModel);

                ResultSetMetaData rsmd = rs.getMetaData();
                int colNo = rsmd.getColumnCount();
                while (rs.next()) {
                    Object[] values = new Object[colNo];
                    for (int i = 0; i < colNo; i++) {
                        if (i == AccountingUtil.getColumnByName(depositsBottomTable, "Deposit Date")) {
                            values[i] = AccountingUtil.dateFormat1.format(rs.getObject("Date"));
                        } else if (i == AccountingUtil.getColumnByName(depositsBottomTable, "Amount")) {
                            values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject("Amount").toString());
                        } else {
                            values[i] = rs.getObject(i + 1);
                        }
                    }
                    aModel.addRow(values);
                }

                rs.getStatement().close();

            } catch (SQLException ex) {
                Logger.getLogger(BankingPanel.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }//GEN-LAST:event_depositsSearchButtonMouseClicked

        private void jButton38checkButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton38checkButtonsClicked
            // TODO add your handling code here:
        }//GEN-LAST:event_jButton38checkButtonsClicked

        private void voidDepositBtncheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_voidDepositBtncheckButtonsClicked
            if (!voidDepositBtn.isEnabled()) {
                return;
            }
            boolean sureToVoid;
            String depositMemo = "";
            String voidDepositMemo = "";
            String updateSql[] = new String[1];
            String accNumFromTable = depositsBottomTable.getValueAt(depositsBottomTable.getSelectedRow(), depositsBottomTable.getColumnModel().getColumnIndex("Bank")).toString().split("-")[0].trim();
            String accountNumber;
            Double depositDebit;
            Double depositCredit;
            String depositControlNo;
            String depositGLType;
            boolean reverseEntries;

            if (depositsBottomTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(depositsBottomTable, "Please select a record to void", JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    depositNo = Integer.parseInt(depositsBottomTable.getValueAt(depositsBottomTable.getSelectedRow(), depositsBottomTable.getColumnModel().getColumnIndex("Deposit #")).toString());
                    int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to void the selected deposit?", "Confirm",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.NO_OPTION) {
                        return;
                    } else {
                        sureToVoid = true;
                    }
                    if (sureToVoid) {
                        // To:Do the Queries here
                        //First Get the Status of Deposit from AccountingCBTable (it should be clear - to be set as VOID)
                        String query = "SELECT BankAccount,Date,Amount,Memo,DepositNumber, Status FROM AccountingCBTable "
                                + "Where DepositNumber = '" + depositNo + "' AND BankAccount = '" + accNumFromTable + "'";
                        System.out.println("Query : " + query);

                        if (depositsMemo != null || !depositsMemo.toString().isEmpty()) {
                            voidDepositMemo = "VOID: " + depositMemo;
                        } else {
                            voidDepositMemo = "VOID: ";
                        }

                        // Update CBTable (Memo = void : Status = VOID)
                        updateSql[0] = "UPDATE AccountingCBTable "
                                + "SET Status = 'Void',Memo = '" + voidDepositMemo + "' "
                                + "WHERE DepositNumber = '" + depositNo + "' AND BankAccount = '" + accNumFromTable + "'";

                        System.out.println("Voiding Deposit >>>>>> " + updateSql[0]);
                        dms.DMSApp.getApplication().getDBConnection().executeStatements(updateSql, this);
                    }
                    reverseEntries = true;
                    //Case
                    if (reverseEntries) {
                        String query1 = "SELECT AccountNumber,Debit,Credit,ControlNumber,GLType, EntryID FROM AccountingGLTable "
                                + "WHERE ReferenceNumber = '" + depositNo + "' AND AccountNumber = '" + accNumFromTable + "' "
                                + "AND  GLTYpe = 'Deposit'";

                        System.out.println("Query := " + query1);
                        ResultSet res = dms.DMSApp.getApplication().getDBConnection().getResultSet(query1);

                        while (res.next()) {
                            accountNumber = res.getString("AccountNumber");
                            depositCredit = res.getDouble("Debit");
                            depositControlNo = res.getString("ControlNumber");
                            depositGLType = res.getString("GLType");

                            // Insert Qry
                            String insertQry1[] = new String[1];
                            insertQry1[0] = "INSERT INTO AccountingGLTable "
                                    + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName) "
                                    + "VALUES ("
                                    + "'" + accountNumber + "','0.00', '" + depositCredit + "', "
                                    + "'" + depositControlNo + "', '" + depositNo + "', "
                                    + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + depositGLType + "', "
                                    + "'" + voidDepositMemo + "', "
                                    + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                                    + ")";

                            System.out.println("Query 1 := " + insertQry1[0]);
                            dms.DMSApp.getApplication().getDBConnection().executeStatements(insertQry1, this);
                        }
                        // CASE -2 : Insert 2nd Reversal
                        String query2 = "SELECT AccountNumber,Debit,Credit,ControlNumber,GLType from AccountingGLTable "
                                + "WHERE ReferenceNumber = '" + depositNo + "' AND GLType IN ('Deposit Line','ACH Deposit Line')";

                        System.out.println("Query 2 : " + query2);
                        ResultSet resultSet = dms.DMSApp.getApplication().getDBConnection().getResultSet(query2);

                        while (resultSet.next()) {
                            depositDebit = resultSet.getDouble("Credit");
                            depositControlNo = resultSet.getString("ControlNumber");
                            depositGLType = resultSet.getString("GLType");
                            accountNumber = resultSet.getString("AccountNumber");

                            String insertQry2[] = new String[1];
                            insertQry2[0] = "INSERT INTO AccountingGLTable "
                                    + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName) "
                                    + "VALUES ("
                                    + "'" + accountNumber + "','" + depositDebit + "', '0.00', "
                                    + "'" + depositControlNo + "', '" + depositNo + "', "
                                    + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + depositGLType + "', "
                                    + "'" + voidDepositMemo + "', "
                                    + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                                    + ")";

                            System.out.println("Update Qry 2:= " + insertQry2[0]);
                            dms.DMSApp.getApplication().getDBConnection().executeStatements(insertQry2, this);
                        }
                    }//End Reverse
                    reloadDeposits();
                    dms.DMSApp.displayMessage(this, "Selected deposit voided successfully", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }//GEN-LAST:event_voidDepositBtncheckButtonsClicked

        private void apDepositsPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_apDepositsPanelComponentShown
            setRefNumValueForTable(depositsAccountsTable, depositsNumber.getText().toString());
        }//GEN-LAST:event_apDepositsPanelComponentShown

        private void depositsBankComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_depositsBankComboItemStateChanged
            bankDepositsSelected();
        }//GEN-LAST:event_depositsBankComboItemStateChanged

        private void depositsBankCombobankSelected(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_depositsBankCombobankSelected
            // TODO add your handling code here:
        }//GEN-LAST:event_depositsBankCombobankSelected

    private void depositsAccountsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsAccountsTableMouseClicked
        //System.out.println("Inside depositsAccountsTableMouseClicked()..");
        int row = depositsAccountsTable.rowAtPoint(evt.getPoint());
        int col = depositsAccountsTable.columnAtPoint(evt.getPoint());
        //System.out.println("Row := " + row);
        //System.out.println("Col := " + col);
        if (col == 1) {               // if Amount Column is modified/clicked
            //System.out.println("Amount Column clicked !");
            ((javax.swing.DefaultCellEditor) depositsAccountsTable.getDefaultEditor(new Object().getClass())).setClickCountToStart(1);
        }
    }//GEN-LAST:event_depositsAccountsTableMouseClicked

        private void checkbookAllDatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkbookAllDatesMouseClicked
            AccountingUtil.dateFilter(jPanel159, checkbookAllDates); // checkbookDateFilter
            reloadChecks();
        }//GEN-LAST:event_checkbookAllDatesMouseClicked

        private void depositsAllDatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsAllDatesMouseClicked
            AccountingUtil.dateFilter(jPanel161, depositsAllDates); // depositsDateFilter
            reloadDeposits();
        }//GEN-LAST:event_depositsAllDatesMouseClicked

        private void jButton40checkButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton40checkButtonsClicked
            if (depositsBottomTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(editDepositDialog1, "Please select a record to edit", JOptionPane.WARNING_MESSAGE);
            }
            try {
                depositNo = Integer.parseInt(depositsBottomTable.getValueAt(depositsBottomTable.getSelectedRow(), depositsBottomTable.getColumnModel().getColumnIndex("Deposit #")).toString());
                setDepositNo(depositNo);
                int bankAccount = 0;
                String glType = "";
                String query = "Select BankAccount,Date,Amount,Memo,DepositNumber from AccountingCBTable "
                        + "Where DepositNumber = '" + depositNo + "' ";
                ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
                if (rs.next()) {
                    depositsDate1.setDate(AccountingUtil.getUtilDateFormatFromSqlDate(rs.getDate("Date")));
                    depositsAmount1.setText(AccountingUtil.formatAmountForDisplay(rs.getBigDecimal("Amount").toString()));
                    depositsTotalAccounts1.setText(AccountingUtil.formatAmountForDisplay(rs.getBigDecimal("Amount").toString()));
                    depositsNumber1.setText(String.valueOf(rs.getInt("DepositNumber")));
                    depositsMemo2.setText(rs.getString("Memo"));
                    bankAccount = rs.getInt("BankAccount");
                }
                //System.out.println("Bank Account : " + bankAccount);
                if (bankAccount == 0) {
                    depositsBankCombo2.setSelectedItem("0 -");
                    editBankDepositsSelected();
                } else if (bankAccount == 202) {
                    depositsBankCombo2.setSelectedItem("202 - Cash in Bank-BOA");
                    editBankDepositsSelected();
                } else if (bankAccount == 203) {
                    depositsBankCombo2.setSelectedItem("203 - Cash on hand");
                    editBankDepositsSelected();
                }
                depositsDeposit1.setSelected(true);

                // Load the bottom table
                int accountNumber = 0;
                String depositDetailsQuery = "SELECT A.AccountNumber AS AccountNo, CAST(ROUND(A.Credit,2) "
                        + "AS NUMERIC(10,2)), A.ControlNumber, A.ReferenceNumber, A.Memo, A.Class,A.GLType "
                        + "FROM AccountingGLTable A RIGHT JOIN AccountingCBTable B ON "
                        + "A.ReferenceNumber = B.DepositNumber "
                        + "Where A.ReferenceNumber = '" + depositNo + "' "
                        + " AND A.GLType IN ('Deposit Line','ACH Deposit Line')";

                //System.out.println("DepositDetailsQuery >>>>>>>> " + depositDetailsQuery);
                ResultSet rsAccountDetails = dms.DMSApp.getApplication().getDBConnection().getResultSet(depositDetailsQuery);

                DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable1.getModel();
                AccountingUtil.clearTableModel(aModel);

                ResultSetMetaData rsmd = rsAccountDetails.getMetaData();
                int colNo = rsmd.getColumnCount();
                while (rsAccountDetails.next()) {
                    Object[] values = new Object[colNo];
                    for (int i = 0; i < colNo; i++) {
                        accountNumber = rsAccountDetails.getInt("AccountNo");
                        classForeditDeposit = rsAccountDetails.getString("Class");
                        glType = rsAccountDetails.getString("GLType");
                        if (i == 0) {
                            values[i] = AccountingUtil.getAccountFull(accountNumber);
                        } else if (i == 1) {
                            values[i] = AccountingUtil.formatAmountForDisplay(rsAccountDetails.getObject(i + 1).toString());
                        } else {
                            values[i] = rsAccountDetails.getObject(i + 1);
                        }
                    }
                    //System.out.println("6666666666666");
                    accountsToBeShownEditDepositList.add(AccountingUtil.getAccountFull(accountNumber));
                    aModel.addRow(values);
                }
                // set Class
                if (classForeditDeposit.equals("Miami")) {
                    billClass_deposit1.setSelectedItem("Miami");
                } else if (classForeditDeposit.equals("Hollywood")) {
                    billClass_deposit1.setSelectedItem("Hollywood");
                } else if (classForeditDeposit.equals("Wholesale")) {
                    billClass_deposit1.setSelectedItem("Wholesale");
                } else if (classForeditDeposit.equals("Miami:Service")) {
                    billClass_deposit1.setSelectedItem("Miami:Service");
                } else if (classForeditDeposit.equals("Admin")) {
                    billClass_deposit1.setSelectedItem("Admin");
                }
                // set radio button option
                if (glType.equals("Deposit Line")) {
                    depositsDeposit1.setSelected(true);
                } else {
                    depositsACH1.setSelected(true);
                }
                rsAccountDetails.getStatement().close();
                dms.DMSApp.getApplication().show(editDepositDialog1);
            } catch (Exception e) {
                Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
            }
            setDepositNo(depositNo);
        }//GEN-LAST:event_jButton40checkButtonsClicked

    public void setDepositNo(int depositNumber) {
        this.depositNo = depositNumber;
    }

    public int getDepositNo() {
        return this.depositNo;
    }

    private void addComboBoxesToTableForEditDeposit() {

        if (AccountingUtil.getAllAccountsFull() != null) {
            Object[] accounts = AccountingUtil.getAllAccountsFull();
            TableColumn accountColumn = depositsAccountsTable1.getColumnModel().getColumn(0);
            JComboBox comboBox = new JComboBox();
            for (int j = 0; j < accounts.length; j++) {
                comboBox.addItem(accounts[j]);
            }
            accountColumn.setCellEditor(new DefaultCellEditor(comboBox));
            accountColumn.setCellRenderer(new MyComboBoxRendererForEditDepositCase(accounts));
        }
    }

    private void addComboBoxesToTableForEditCheck() {

        if (AccountingUtil.getAllAccountsFull() != null) {
            Object[] accounts = AccountingUtil.getAllAccountsFull();

            TableColumn accountColumn = checkAccountsTable1.getColumnModel().getColumn(0);
            JComboBox comboBox = new JComboBox();
            for (int j = 0; j < accounts.length; j++) {
                comboBox.addItem(accounts[j]);
            }
            accountColumn.setCellEditor(new DefaultCellEditor(comboBox));
            accountColumn.setCellRenderer(new MyComboBoxRendererForEditCase(accounts));
        }
    }

    private void clearTableModelForBankingPanel(DefaultTableModel aModel) {
        if (aModel.getRowCount() > 0) {
            for (int i = aModel.getRowCount(); i > 1; i--) {
                aModel.removeRow(i - 1);
            }
        }
        for (int i = 1; i < aModel.getColumnCount(); i++) {
            aModel.setValueAt("", 0, i);
        }
    }

    private void deleteVoidedDeposit(int depositNum) {
        String queriesArray[] = new String[3];
        queriesArray[0] = "DELETE  from AccountingCBTable WHERE DepositNumber ='" + depositNum + "' ";
        queriesArray[1] = "DELETE from AccountingGLTable WHERE ReferenceNumber = '" + depositNum + "' AND GLType IN ('Deposit','ACH Deposit')";
        queriesArray[2] = "DELETE from AccountingGLTable WHERE ReferenceNumber = '" + depositNum + "' AND GLType IN ('Deposit Line','ACH Deposit Line')";
        dms.DMSApp.getApplication().getDBConnection().executeStatements(queriesArray, this);
        dms.DMSApp.displayMessage(this, "The selected 'voided' deposit has been deleted.", JOptionPane.INFORMATION_MESSAGE);


    }

    public class MyComboBoxRendererForEditCase extends JComboBox implements TableCellRenderer {

        public MyComboBoxRendererForEditCase(Object[] items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (column == 0) {
                this.setBackground(new java.awt.Color(255, 255, 255));
            }
            for (int i = 0; i < accountsToBeShownList.size(); i++) {
                Object currentElement = accountsToBeShownList.get(i);
                if (currentElement != null && value != null) {
                    if (currentElement.toString().startsWith(value.toString())) {
                        setSelectedItem(currentElement);
                    }
                }
            }
            if (accountsToBeShownList.isEmpty()) {
                setSelectedItem(value);
            }
            return this;
        }
    }

    public class MyComboBoxEditorForEditCase extends DefaultCellEditor {

        public MyComboBoxEditorForEditCase(Object[] items) {
            super(new JComboBox(items));
        }
    }

// copy the above 2 classes for Edit Deposit Case
    public class MyComboBoxRendererForEditDepositCase extends JComboBox implements TableCellRenderer {

        public MyComboBoxRendererForEditDepositCase(Object[] items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (column == 0) {
                this.setBackground(new java.awt.Color(255, 255, 255));
            }
            for (int i = 0; i < accountsToBeShownEditDepositList.size(); i++) {
                Object currentElement = accountsToBeShownEditDepositList.get(i);
                if (currentElement != null && value != null) {
                    if (currentElement.toString().startsWith(value.toString())) {
                        setSelectedItem(currentElement);
                    }
                }
            }
            if (accountsToBeShownEditDepositList.isEmpty()) {
                setSelectedItem(value);
            }
            return this;
        }
    }

    public class MyComboBoxEditorForEditDepositCase extends DefaultCellEditor {

        public MyComboBoxEditorForEditDepositCase(Object[] items) {
            super(new JComboBox(items));
        }
    }
    private void depositsBankCombo2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_depositsBankCombo2ItemStateChanged
        // TODO add your handling code here:
        editBankDepositsSelected();
    }//GEN-LAST:event_depositsBankCombo2ItemStateChanged

    private void depositsBankCombo2bankSelected(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_depositsBankCombo2bankSelected
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsBankCombo2bankSelected

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        int rowCount = depositsAccountsTable1.getRowCount();
        if (rowCount == 1) {
            dms.DMSApp.displayMessage(this, "At least one account is required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultTableModel tableModel = (DefaultTableModel) depositsAccountsTable1.getModel();
        tableModel.removeRow(tableModel.getRowCount() - 1);
        rowCount--;
        boolean rowRemoved = true;
        double sum = 0.00;
        for (int i = 0; i < rowCount; i++) {
            if (depositsAccountsTable1.getValueAt(i, 1) != null && !depositsAccountsTable1.getValueAt(i, 1).toString().isEmpty()) {
                if (AccountingUtil.displayNumeric(depositsAccountsTable1.getValueAt(i, 1).toString())) {
                    if (depositsAccountsTable1.getValueAt(i, 1).toString().contains(",")) {
                        sum += Double.parseDouble(depositsAccountsTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                    } else {
                        sum += Double.parseDouble(depositsAccountsTable1.getValueAt(i, 1).toString());
                    }
                }
            }
        }
        depositsTotalAccounts1.setValue(sum);
    }//GEN-LAST:event_jButton27ActionPerformed

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable1.getModel();
        int rowCount = depositsAccountsTable1.getRowCount();
        int depositRowNum = rowCount;
        String accountFromTable = null;
        String amountFromTable = null;
        if (rowCount == 0) {
            addNewDepositsRow(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
        }

        if (isRowRemoved) {
            depositRowNum = rowCount;
            isRowRemoved = false;
        }

        if (depositRowNum == 0) {
            return;
        }
        if (depositsAccountsTable1.getValueAt(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Account")) != null) {
            accountFromTable = depositsAccountsTable1.getValueAt(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Account")).toString();
        }
        if (depositsAccountsTable1.getValueAt(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Amount")) != null) {
            amountFromTable = depositsAccountsTable1.getValueAt(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Amount")).toString();
        }
        if (accountFromTable == null) {
            dms.DMSApp.displayMessage(this, "Please select an account.", JOptionPane.WARNING_MESSAGE);
            depositsAccountsTable1.setCellSelectionEnabled(true);
            depositsAccountsTable1.changeSelection(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Account"), false, false);
            depositsAccountsTable1.requestFocus();
        }
        if (amountFromTable == null || amountFromTable.isEmpty()) {
            dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.WARNING_MESSAGE);
            depositsAccountsTable1.setCellSelectionEnabled(true);
            depositsAccountsTable1.changeSelection(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Amount"), false, false);
            depositsAccountsTable1.requestFocus();
        } else if (amountFromTable != null) {
            if (!AccountingUtil.displayNumeric(amountFromTable)) {
                //System.out.println("Amount is not Numeric");
                dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                depositsAccountsTable1.setCellSelectionEnabled(true);
                depositsAccountsTable1.changeSelection(depositRowNum - 1, depositsAccountsTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                depositsAccountsTable1.requestFocus();
            }
        }
        if (isControlNumRequiredForTable(depositsAccountsTable1)) {
            dms.DMSApp.displayMessage(this,
                    "Please provide Control # value for current row", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // check to insert a row
        if (accountFromTable != null && amountFromTable != null && AccountingUtil.displayNumeric(amountFromTable)) {
            addNewDepositsRow(aModel, accountFromTable);
            depositRowNum++;
        }
    }//GEN-LAST:event_jButton26ActionPerformed

    private void clearCheck3(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearCheck3
        // TODO add your handling code here:
        DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable1.getModel();
        clearTableModelForBankingPanel(aModel);
        depositsAmount1.setText("0.00");
        AccountingUtil.showMessageBalloon("Deposit data cleared, keeping the minimum required one account line intact", depositsAccountsTable1);
    }//GEN-LAST:event_clearCheck3

    private void jButton50saveCheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton50saveCheckButtonsClicked
        try {
            String bankNum = depositsBankCombo2.getSelectedItem().toString().trim();
            String bankNumber = bankNum.substring(0, bankNum.indexOf("-"));
            if (validateDataForEditDeposit()) {
                sql = new String[4];
                String deleteSql[] = new String[1];
                String delSql[] = new String[1];
                // Delete Vendor Invoice
                deleteSql[0] = "DELETE From AccountingCBTable "
                        + "Where DepositNumber = '" + getDepositNo() + "' ";

                //System.out.println("Delete Sql 1 := " + deleteSql[0]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteSql, depositsAccountsTable1);

                // Delete Invoice Lines
                delSql[0] = "DELETE From AccountingGLTable "
                        + "Where ReferenceNumber = '" + getDepositNo() + "' ";
                //System.out.println("Delete Sql 2 := " + delSql[0]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(delSql, depositsAccountsTable1);

                String depositType = "";
                String depositLineType = "";
                if (depositsDeposit1.isSelected()) {
                    depositType = "Deposit";
                    depositLineType = "Deposit Line";
                } else {
                    depositType = "ACH Deposit";
                    depositLineType = "ACH Deposit Line";
                }
                // Insert New Record: enter check information in the checkbook table
                sql[0] = "INSERT INTO AccountingCBTable "
                        + "(EntryID, DepositNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Type) "
                        + "VALUES ("
                        + "'1', '" + depositsNumber1.getText() + "', '" + bankNumber + "', '0', "
                        + "'', '" + AccountingUtil.dateFormat.format(depositsDate1.getDate()) + "', '"
                        + depositsAmount1.getText() + "', '" + depositsMemo2.getText() + "', "
                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                        + "'" + depositType + "' "
                        + ") ";

                //System.out.println("AccountingCBTable Qry Edit = " + sql[0]);
                // Journal Entry for check. Control = Check Number and Reference is Paid To Id
                sql[1] = "INSERT INTO AccountingGLTable "
                        + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                        + "VALUES ("
                        + "'" + bankNumber + "', '" + depositsAmount1.getText() + "','0.00', '', "
                        + "'" + depositsNumber1.getText() + "', " + "'" + AccountingUtil.dateFormat.format(depositsDate1.getDate()) + "', '" + depositType + "', "
                        + "'" + depositsMemo2.getText() + "', "
                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                        + "'" + billClass_deposit1.getSelectedItem().toString() + "' "
                        + ")";

                //System.out.println("AccountingGLTable 1 Edit = " +sql[1]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                String[] sql1 = new String[1];


                int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Account");
                int amountColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Amount");
                int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Control #");
                int memoColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Memo");

                for (int i = 0; i <= depositsAccountsTable1.getRowCount() - 1; i++) {
                    String accountNumber = null;
                    Double amountValue = 0.00;
                    String controlNumber = null;
                    String memo = null;

                    if (depositsAccountsTable1.getValueAt(i, accountColumn) != null) {
                        accountNumber = depositsAccountsTable1.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
                    }
                    Object amountVal = depositsAccountsTable1.getValueAt(i, amountColumn);
                    if (amountVal != null && !amountVal.toString().isEmpty()) {
                        if (amountVal.toString().contains(",")) {
                            amountVal = amountVal.toString().replaceAll(",", "");

                        }
                        amountValue = Double.parseDouble(amountVal.toString());
                    } else {
                        amountValue = 0.00;
                    }

                    for (int j = 0; j < depositsAccountsTable1.getRowCount(); j++) {
                        int accountNum = -1;
                        Object accNumString = depositsAccountsTable1.getValueAt(j, accountColumn);
                        if (accNumString == null) {
                            dms.DMSApp.displayMessage(this,
                                    "Please select account first", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (!accNumString.toString().isEmpty()) {
                            accountNum = Integer.valueOf(accNumString.toString().split("-")[0].trim());
                        }
                        //System.out.println("Account Number : " + accountNumber);
                        Boolean isControlled = AccountingUtil.getControlNumMap().get(accountNum);
                        if (isControlled && depositsAccountsTable1.getValueAt(j, controlColumn) == null) {
                            dms.DMSApp.displayMessage(this,
                                    "Please provide Control # value for Row." + ++j, JOptionPane.WARNING_MESSAGE);
                        } else if (isControlled
                                && depositsAccountsTable1.getValueAt(j, controlColumn) != null
                                && depositsAccountsTable1.getValueAt(j, controlColumn).toString().isEmpty()) {
                            dms.DMSApp.displayMessage(this,
                                    "Please provide Control # value for Row " + ++j, JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    if (depositsAccountsTable1.getValueAt(i, controlColumn) != null) {
                        controlNumber = depositsAccountsTable1.getValueAt(i, controlColumn).toString();
                    } else {
                        controlNumber = "";
                    }


                    if (depositsAccountsTable1.getValueAt(i, memoColumn) != null) {
                        memo = depositsAccountsTable1.getValueAt(i, memoColumn).toString();
                    } else {
                        memo = "";
                    }

                    sql1[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                            + "ReferenceNumber, PostDate, Memo, GLType, LotName,Class) VALUES("
                            + "'" + accountNumber + "', '0.00', '" + amountValue + "', '" + controlNumber + "', '"
                            + depositsNumber1.getText() + "', '" + AccountingUtil.dateFormat.format(depositsDate1.getDate()) + "', '"
                            + memo + "', '" + depositLineType + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'" + billClass_deposit1.getSelectedItem().toString() + "' "
                            + ")";

                    //System.out.println("HERE 2 Edit: " + sql1[0]);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql1, this);
                }
                clearDeposits();
                reloadDeposits();
                dms.DMSApp.displayMessage(depositsAccountsTable1, "Deposit updated successfully ", JOptionPane.INFORMATION_MESSAGE);
                editDepositDialog1.dispose();
            }
        } catch (Exception e) {
            dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton50saveCheckButtonsClicked

    private boolean validateDataForEditDeposit() {
        boolean good = true;
        int depositsRowCount = depositsAccountsTable1.getRowCount();
        if (depositsRowCount == 0) {
            dms.DMSApp.displayMessage(editDepositDialog1, "Please select an account first", dms.DMSApp.WARNING_MESSAGE);
            addNewDepositsRow((DefaultTableModel) depositsAccountsTable1.getModel(), account);
            good = false;
            return good;
        }

        if (depositsAmount1.getText().isEmpty()) {
            good = false;
            dms.DMSApp.displayMessage(editDepositDialog1, "The desposit amount is empty", dms.DMSApp.WARNING_MESSAGE);
        } else {
            if (depositsTotalAccounts1.getText() != null && !depositsTotalAccounts1.getText().isEmpty()) {
                String depositsTotalAccounts1StringVal = depositsTotalAccounts1.getText().toString();
                if (depositsTotalAccounts1StringVal.contains(",")) {
                    depositsTotalAccounts1StringVal = depositsTotalAccounts1StringVal.replaceAll(",", "");
                }

                String depositsAmount1StringVal = depositsAmount1.getText().toString();
                if (depositsAmount1StringVal.contains(",")) {
                    depositsAmount1StringVal = depositsAmount1StringVal.replaceAll(",", "");
                }

                if (Double.parseDouble(depositsTotalAccounts1StringVal)
                        != Double.parseDouble(depositsAmount1StringVal)) {
                    good = false;
                    dms.DMSApp.displayMessage(editDepositDialog1, "The deposit amount and accounts total are not the same", dms.DMSApp.WARNING_MESSAGE);
                }
            } else {
                String depositsAmount1StringVal = depositsAmount1.getText().toString();
                if (depositsAmount1StringVal.contains(",")) {
                    depositsAmount1StringVal = depositsAmount1StringVal.replaceAll(",", "");
                }
                double sum = 0.00;
                String amountTotalLines = "";
                String finalLinesAmount = "";
                int rowCount = depositsAccountsTable1.getRowCount();
                if (rowCount > 0) {
                    for (int i = 0; i < rowCount; i++) {
                        if (depositsAccountsTable1.getValueAt(i, 1) != null && !depositsAccountsTable1.getValueAt(i, 1).toString().isEmpty()) {
                            if (AccountingUtil.displayNumeric(depositsAccountsTable1.getValueAt(i, 1).toString())) {
                                if (depositsAccountsTable1.getValueAt(i, 1).toString().contains(",")) {
                                    sum += Double.parseDouble(depositsAccountsTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                                } else {
                                    sum += Double.parseDouble(depositsAccountsTable1.getValueAt(i, 1).toString());
                                }
                            }
                        }

                    } // end for loop
                    amountTotalLines = Double.valueOf(sum).toString();
                    finalLinesAmount = AccountingUtil.formatAmountForDisplay(amountTotalLines);
                }
                if (depositsAmount1StringVal.equals(finalLinesAmount)) {
                    depositsTotalAccounts1.setText(finalLinesAmount);
                } else {
                    good = false;
                    dms.DMSApp.displayMessage(editDepositDialog1, "The deposit accounts total amount is empty", dms.DMSApp.WARNING_MESSAGE);
                }
            }
        }

        if (depositsNumber1.getText().isEmpty()) {
            good = false;
            dms.DMSApp.displayMessage(editDepositDialog1, "The deposit# is blank", dms.DMSApp.WARNING_MESSAGE);
            return good;
        }
        if (!AccountingUtil.isNumeric(depositsNumber1.getText().toString())) {
            good = false;
            dms.DMSApp.displayMessage(editDepositDialog1, "The deposit# can only be Numeric ", dms.DMSApp.WARNING_MESSAGE);
            return good;

        }
        if (isDepositNumberAlreadyExists(Integer.parseInt(depositsNumber1.getText().toString()))) {
            good = false;
            dms.DMSApp.displayMessage(editDepositDialog1, "The deposit# " + depositsNumber1.getText().toString() + " already exists ", dms.DMSApp.WARNING_MESSAGE);
            return good;
        }
        if (depositsBankCombo2.getSelectedItem().toString().isEmpty()) {
            good = false;
            dms.DMSApp.displayMessage(editDepositDialog1, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
            return good;
        }

        if (depositsRowCount > 0) {
            int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Account");
            int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Control #");

            for (int i = 0; i <= depositsAccountsTable1.getRowCount() - 1; i++) {
                int accountNumber = -1;
                Object accNumString = depositsAccountsTable1.getValueAt(i, accountColumn);
                if (accNumString == null) {
                    dms.DMSApp.displayMessage(editDepositDialog1,
                            "Please select account first", JOptionPane.WARNING_MESSAGE);
                    return good;
                }
                if (!accNumString.toString().isEmpty()) {
                    accountNumber = Integer.valueOf(accNumString.toString().split("-")[0].trim());
                }
                System.out.println("Account Number : " + accountNumber);
                Boolean isControlled = AccountingUtil.getControlNumMap().get(accountNumber);
                if (isControlled && depositsAccountsTable1.getValueAt(i, controlColumn) == null) {
                    good = false;
                    dms.DMSApp.displayMessage(this,
                            "Please provide Control # value for Row." + ++i, JOptionPane.WARNING_MESSAGE);
                } else if (isControlled
                        && depositsAccountsTable1.getValueAt(i, controlColumn) != null
                        && depositsAccountsTable1.getValueAt(i, controlColumn).toString().isEmpty()) {
                    good = false;
                    dms.DMSApp.displayMessage(editDepositDialog1,
                            "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                    return good;
                }
            }
        }
        return good;
    }

    public boolean isDepositNumberAlreadyExists(int depositNum) {
        boolean depositNoExists = false;
        boolean checkForDuplicate = true;
        try {
            String orignalDepositNumber = String.valueOf(getDepositNo());
            String depositNumberToBeEdited = depositsNumber1.getText().toString();
            if (depositNumberToBeEdited.equals(orignalDepositNumber)) {
                checkForDuplicate = false;
            }
            //System.out.println("checkForDup dep : = " + checkForDuplicate);

            if (checkForDuplicate) {
                String uniquedepositNumberQuery = "select count(*) as Count from AccountingCBTable where depositNumber = " + depositNum;
                ResultSet rsUniquedepositNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniquedepositNumberQuery);
                while (rsUniquedepositNumber.next()) {
                    int count = rsUniquedepositNumber.getInt("Count");
                    if (count > 0) {
                        depositNoExists = true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return depositNoExists;
    }

    private void saveDepositButton2(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveDepositButton2
        try {
            String bankNum = depositsBankCombo2.getSelectedItem().toString().trim();
            String bankNumber = bankNum.substring(0, bankNum.indexOf("-"));
            if (validateDataForEditDeposit()) {
                sql = new String[4];
                String deleteSql[] = new String[1];
                String delSql[] = new String[1];
                // Delete Vendor Invoice
                deleteSql[0] = "DELETE From AccountingCBTable "
                        + "Where DepositNumber = '" + getDepositNo() + "' ";

                //System.out.println("Delete Sql 1 := " + deleteSql[0]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteSql, depositsAccountsTable1);

                // Delete Invoice Lines
                delSql[0] = "DELETE From AccountingGLTable "
                        + "Where ReferenceNumber = '" + getDepositNo() + "' ";
                //System.out.println("Delete Sql 2 := " + delSql[0]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(delSql, depositsAccountsTable1);

                String depositType = "";
                String depositLineType = "";
                if (depositsDeposit1.isSelected()) {
                    depositType = "Deposit";
                    depositLineType = "Deposit Line";
                } else {
                    depositType = "ACH Deposit";
                    depositLineType = "ACH Deposit Line";
                }

                // Insert New Record: enter check information in the checkbook table
                sql[0] = "INSERT INTO AccountingCBTable "
                        + "(EntryID, DepositNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Type) "
                        + "VALUES ("
                        + "'1', '" + depositsNumber1.getText() + "', '" + bankNumber + "', '0', "
                        + "'', '" + AccountingUtil.dateFormat.format(depositsDate1.getDate()) + "', '"
                        + depositsAmount1.getText() + "', '" + depositsMemo2.getText() + "', "
                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                        + "'" + depositType + "' "
                        + ") ";

                //System.out.println("AccountingCBTable Qry Edit = " + sql[0]);
                // Journal Entry for check. Control = Check Number and Reference is Paid To Id
                sql[1] = "INSERT INTO AccountingGLTable "
                        + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                        + "VALUES ("
                        + "'" + bankNumber + "', '" + depositsAmount1.getText() + "','0.00', '', "
                        + "'" + depositsNumber1.getText() + "', " + "'" + AccountingUtil.dateFormat.format(depositsDate1.getDate()) + "', '" + depositType + "', "
                        + "'" + depositsMemo2.getText() + "', "
                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                        + "'" + billClass_deposit1.getSelectedItem().toString() + "' "
                        + ")";

                //System.out.println("AccountingGLTable 1 Edit = " +sql[1]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                String[] sql1 = new String[1];


                int accountColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Account");
                int amountColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Amount");
                int controlColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Control #");
                int memoColumn = AccountingUtil.getColumnByName(depositsAccountsTable1, "Memo");

                for (int i = 0; i <= depositsAccountsTable1.getRowCount() - 1; i++) {
                    String accountNumber = null;
                    Double amountValue = 0.00;
                    String controlNumber = null;
                    String memo = null;

                    if (depositsAccountsTable1.getValueAt(i, accountColumn) != null) {
                        accountNumber = depositsAccountsTable1.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
                    }
                    Object amountVal = depositsAccountsTable1.getValueAt(i, amountColumn);
                    if (amountVal != null && !amountVal.toString().isEmpty()) {
                        if (amountVal.toString().contains(",")) {
                            amountVal = amountVal.toString().replaceAll(",", "");

                        }
                        amountValue = Double.parseDouble(amountVal.toString());
                    } else {
                        amountValue = 0.00;
                    }

                    for (int j = 0; j < depositsAccountsTable1.getRowCount(); j++) {
                        int accountNum = -1;
                        Object accNumString = depositsAccountsTable1.getValueAt(j, accountColumn);
                        if (accNumString == null) {
                            dms.DMSApp.displayMessage(this,
                                    "Please select account first", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (!accNumString.toString().isEmpty()) {
                            accountNum = Integer.valueOf(accNumString.toString().split("-")[0].trim());
                        }
                        //System.out.println("Account Number : " + accountNumber);
                        Boolean isControlled = AccountingUtil.getControlNumMap().get(accountNum);
                        if (isControlled && depositsAccountsTable1.getValueAt(j, controlColumn) == null) {
                            dms.DMSApp.displayMessage(this,
                                    "Please provide Control # value for Row." + ++j, JOptionPane.WARNING_MESSAGE);
                        } else if (isControlled
                                && depositsAccountsTable1.getValueAt(j, controlColumn) != null
                                && depositsAccountsTable1.getValueAt(j, controlColumn).toString().isEmpty()) {
                            dms.DMSApp.displayMessage(this,
                                    "Please provide Control # value for Row " + ++j, JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    if (depositsAccountsTable1.getValueAt(i, controlColumn) != null) {
                        controlNumber = depositsAccountsTable1.getValueAt(i, controlColumn).toString();
                    } else {
                        controlNumber = "";
                    }


                    if (depositsAccountsTable1.getValueAt(i, memoColumn) != null) {
                        memo = depositsAccountsTable1.getValueAt(i, memoColumn).toString();
                    } else {
                        memo = "";
                    }

                    sql1[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                            + "ReferenceNumber, PostDate, Memo, GLType, LotName,Class) VALUES("
                            + "'" + accountNumber + "', '0.00', '" + amountValue + "', '" + controlNumber + "', '"
                            + depositsNumber1.getText() + "', '" + AccountingUtil.dateFormat.format(depositsDate1.getDate()) + "', '"
                            + memo + "', '" + depositLineType + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'" + billClass_deposit1.getSelectedItem().toString() + "' "
                            + ")";

                    //System.out.println("HERE 2 Edit: " + sql1[0]);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql1, this);
                }
                clearDeposits();
                reloadDeposits();
                dms.DMSApp.displayMessage(depositsAccountsTable1, "Deposit updated successfully ", JOptionPane.INFORMATION_MESSAGE);
                editDepositDialog1.dispose();
            }
        } catch (Exception e) {
            dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveDepositButton2

    private void depositsNumber1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_depositsNumber1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsNumber1FocusLost

    private void depositsNumber1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_depositsNumber1InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsNumber1InputMethodTextChanged

    private void depositsNumber1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_depositsNumber1KeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsNumber1KeyReleased

    private void depositsAmount1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositsAmount1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsAmount1ActionPerformed

    private void depositsDeposit1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsDeposit1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsDeposit1MouseClicked

    private void depositsACH1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsACH1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsACH1MouseClicked

    private void depositsTotalAccounts1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositsTotalAccounts1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsTotalAccounts1ActionPerformed

    private void checkTypeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkTypeComboItemStateChanged
        // TODO add your handling code here:
        typeSelected();
    }//GEN-LAST:event_checkTypeComboItemStateChanged

    private void billClass_checkbookItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_billClass_checkbookItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_billClass_checkbookItemStateChanged

    private void billClass_depositItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_billClass_depositItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_billClass_depositItemStateChanged

    private void editCheckButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editCheckButtonMouseClicked
        String checkType = checksBottomTable.getValueAt(
                checksBottomTable.getSelectedRow(),
                checksBottomTable.getColumnModel().getColumnIndex("Type")).toString();

        String checkNum = checksBottomTable.getValueAt(
                checksBottomTable.getSelectedRow(),
                checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString();
        System.out.println("CheckNum : "+checkNum);
        setOriginalCheckNumber(checkNum);

        if ("Check - Vendor".equals(checkType) || "Check - Customer".equals(checkType) || "Check - Employee".equals(checkType)) {
            editCheckForVendorOrCustomerOrEmployee();
        } else if ("Bill Payment Check - Vendor".equals(checkType)) {
            editBillPaymentCheck();
        }

    }//GEN-LAST:event_editCheckButtonMouseClicked

    private List<String> fetchDataForSelectedChequeOLD() {
        ArrayList<String> checkDetailsList = new ArrayList<String>();
        try {
            checkNo = Integer.parseInt(checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString());
            bankAccountNo = Integer.parseInt(checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Bank")).toString().split("-")[0].replaceAll("\\s+", ""));

            String query = "SELECT BankAccount,PaidTo,B.VendorAddress,VendorCity,VendorState,VendorZip,Amount,Memo, [Date] "
                    + "FROM AccountingCBTable A LEFT JOIN VendorListTable B "
                    + "ON (A.PaidTo = B.VendorName) "
                    + "WHERE CheckNumber = '" + checkNo + "' AND BankAccount = '" + bankAccountNo + "'";

            System.out.println("Query : " + query);
            int count = 0;
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

            while (rs.next()) {
                if (count == 15) {
                    break;
                }

                String bankAccount = rs.getString("BankAccount");
                String paidTo = rs.getString("paidTo");
                String vendorAddress = rs.getString("VendorAddress");
                String vendorCity = rs.getString("VendorCity");
                String vendorState = rs.getString("VendorState");
                String vendorZip = rs.getString("VendorZip");
                String checkDate = rs.getDate("Date").toString();
                String checkAmount = AccountingUtil.formatAmountForDisplay(rs.getBigDecimal("Amount").toString());
                String memo = rs.getString("Memo");

                checkDetailsList.add(
                        bankAccount + "***"
                        + paidTo + "***"
                        + vendorAddress + "***"
                        + vendorCity + "***"
                        + vendorState + "***"
                        + vendorZip + "***"
                        + checkAmount + "***"
                        + memo + "***"
                        + checkDate);
                count++;


            }
        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return checkDetailsList;

    }

    private List<String> fetchDataForSelectedCheque(int checkNo) {
        ArrayList<String> checkDetailsList = new ArrayList<String>();

        try {
            String query = "SELECT ControlNumber, AccountNumber, Debit, Memo "
                    + "FROM AccountingGLTable "
                    + " WHERE ReferenceNumber = '" + checkNo + "' AND GLType LIKE 'Check Line%'";

            System.out.println("Query: " + query);

            int count = 0;
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

            while (rs.next()) {
                if (count == 15) {
                    break;
                }
                String controlNumber = rs.getString("ControlNumber");
                String accountNumber = rs.getString("AccountNumber");
                String lineAmount = AccountingUtil.formatAmountForDisplay(rs.getBigDecimal("Debit").toString());
                String lineMemo = rs.getString("Memo");
                checkDetailsList.add(
                        controlNumber + "***"
                        + accountNumber + "***"
                        + lineMemo + "***"
                        + lineAmount);
                count++;
            }

        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return checkDetailsList;
    }

    private void checkCombo1bankSelected(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_checkCombo1bankSelected
        // TODO add your handling code here:
    }//GEN-LAST:event_checkCombo1bankSelected

    private void checkCombo1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkCombo1ItemStateChanged
        // TODO add your handling code here:
        loadEndingBalanceForEditCheckDialog();
    }//GEN-LAST:event_checkCombo1ItemStateChanged

    private void checkNumber1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_checkNumber1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_checkNumber1FocusLost

    private void checkNumber1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_checkNumber1InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_checkNumber1InputMethodTextChanged

    private void checkNumber1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checkNumber1KeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_checkNumber1KeyReleased

    private void checkAmount1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAmount1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkAmount1ActionPerformed

    private void depositsDeposit2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsDeposit2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsDeposit2MouseClicked

    private void depositsACH2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsACH2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_depositsACH2MouseClicked

    private void saveDepositButton3(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveDepositButton3
        // TODO add your handling code here:
        boolean good = true;
        boolean isCheckUnique = true;
        boolean checkForDuplicate = true;
        String bankNumber;
        int depositsRowCount = checkAccountsTable1.getRowCount();
        if (checkAmount1.getText().isEmpty()) {
            good = false;
            dms.DMSApp.displayMessage(this, "The check amount is empty", dms.DMSApp.WARNING_MESSAGE);
        } else {
            if (checkAccount1.getText() != null && !checkAccount1.getText().isEmpty()) {
                String depositsTotalAccountsStringVal = checkAccount1.getText().toString();
                if (depositsTotalAccountsStringVal.contains(",")) {
                    depositsTotalAccountsStringVal = depositsTotalAccountsStringVal.replaceAll(",", "");
                }

                String depositsAmountStringVal = checkAmount1.getText().toString();
                if (depositsAmountStringVal.contains(",")) {
                    depositsAmountStringVal = depositsAmountStringVal.replaceAll(",", "");
                }

                Double billAmountDoubleVal = Double.parseDouble(depositsAmountStringVal);
                Double billTotalExpensesDoubleVal = Double.parseDouble(depositsTotalAccountsStringVal);
                System.out.println("billAmountDoubleVal " + billAmountDoubleVal);
                System.out.println("billTotalExpensesDoubleVal " + billTotalExpensesDoubleVal);

                int retval = Double.compare(billAmountDoubleVal, billTotalExpensesDoubleVal);
                retval = 0;
                if (retval != 0) {
                    good = false;
                    dms.DMSApp.displayMessage(this, "The check amount and accounts total are not the same", dms.DMSApp.WARNING_MESSAGE);
                    return;
                }
            }
        }

        if (!checkCombo1.getSelectedItem().equals("")) {
            bankNumber = "" + checkCombo1.getSelectedItem();
            bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
        } else {
            dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
            return;
        }

        int statusColumn = AccountingUtil.getColumnByName(checksBottomTable, "Status");
        Object status = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(), statusColumn);
        if (status == null) {
            status = "";
        }
        String checkStatus = status.toString();
        System.out.println("&&&&&&&&&&&& "+getOriginalCheckNumber());
        if (!getOriginalCheckNumber().equalsIgnoreCase(checkNumber1.getText())) {
            checkStatus = "Posted";
        }
        System.out.println("Status : " + checkStatus);

        if (depositsRowCount > 0) {

            int accountColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Account");
            int controlColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Control #");
            for (int i = 0; i < checkAccountsTable1.getRowCount(); i++) {
                if (checkAccountsTable1.getValueAt(i, accountColumn) != null) {
                    String accountNumber = checkAccountsTable1.getValueAt(i, accountColumn).toString().split("-")[0];
                    Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                    if (isControlled && checkAccountsTable1.getValueAt(i, controlColumn) == null) {
                        dms.DMSApp.displayMessage(this,
                                "Please provide Control # value for Row." + ++i, JOptionPane.WARNING_MESSAGE);
                        return;
                    } else if (isControlled
                            && checkAccountsTable1.getValueAt(i, controlColumn) != null
                            && checkAccountsTable1.getValueAt(i, controlColumn).toString().isEmpty()) {
                        dms.DMSApp.displayMessage(this,
                                "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

        } else {
            good = false;
        }

        try {
            if (good) {

                String orignalCheckNumber = String.valueOf(getDepositNo());
                //System.out.println("orignal chkno = " + orignalCheckNumber);
                String checkNumberToBeEdited = checkNumber1.getText().toString();
                //System.out.println("edited chkno = " + checkNumberToBeEdited);
                if (checkNumberToBeEdited.equals(orignalCheckNumber)) {
                    //System.out.println("if.. same chk no");
                    checkForDuplicate = false;
                }
                //System.out.println("checkForDup = " + checkForDuplicate);

                if (checkForDuplicate) {
                    // Check for Duplicate CheckNumber
                    int checkNum;
                    if (checkNumber1.getText().isEmpty()) {
                        dms.DMSApp.displayMessage(this, "The check# is blank", dms.DMSApp.WARNING_MESSAGE);
                        return;
                    } else {
                        try {
                            System.out.println("CheckNum ********* "+checkNumber1.getValue());
                            checkNum = Integer.parseInt(checkNumber1.getValue().toString());
                            String uniqueCheckNumberQuery = "select count(*) as Count from AccountingCBTable where CheckNumber = " + checkNum;

                            ResultSet rsUniqueCheckNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniqueCheckNumberQuery);

                            while (rsUniqueCheckNumber.next()) {

                                int count = rsUniqueCheckNumber.getInt("Count");
                                if (count > 0) {
                                    isCheckUnique = false;
                                    dms.DMSApp.displayMessage(this, "The check# " + checkNum + " already exists", dms.DMSApp.WARNING_MESSAGE);
                                    return;
                                }
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(BankingPanel.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
                if (isCheckUnique) {
                    String deleteQueries[] = new String[2];
                    String insertQueries[] = new String[2];

                    // Delete Vendor Invoice
                    deleteQueries[0] = "DELETE From AccountingCBTable "
                            + "Where CheckNumber = '" + getDepositNo() + "' ";

                    System.out.println("Delete 1111111 := " + deleteQueries[0]);

                    // Delete Invoice Lines
                    deleteQueries[1] = "DELETE From AccountingGLTable "
                            + "Where ReferenceNumber = '" + getDepositNo() + "' ";


                    System.out.println("Delete 2222222222 := " + deleteQueries[1]);


                    dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteQueries, checkAccountsTable1);

                    String checkPaidToType = "";
                    String checkLinePaidToType = "";
                    if (checkTypeCombo1.getSelectedItem() == "Vendor") {
                        if (depositsDeposit2.isSelected()) {
                            checkPaidToType = "Check - Vendor";
                            checkLinePaidToType = "Check Line - Vendor";
                        } else {
                            checkPaidToType = "ACH Check - Vendor";
                            checkLinePaidToType = "ACH Check Line - Vendor";
                        }
                    } else if (checkTypeCombo1.getSelectedItem() == "Customer") {
                        if (depositsDeposit2.isSelected()) {
                            checkPaidToType = "Check - Customer";
                            checkLinePaidToType = "Check Line - Customer";
                        } else {
                            checkPaidToType = "ACH Check - Customer";
                            checkLinePaidToType = "ACH Check Line - Customer";
                        }
                    } else if (checkTypeCombo1.getSelectedItem() == "Employee") {
                        if (depositsDeposit2.isSelected()) {
                            checkPaidToType = "Check - Employee";
                            checkLinePaidToType = "Check Line - Employee";
                        } else {
                            checkPaidToType = "ACH Check - Employee";
                            checkLinePaidToType = "ACH Check Line - Employee";
                        }
                    }


                    // Insert New Record: enter check information in the checkbook table
                    insertQueries[0] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, CheckNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Status, Type) "
                            + "VALUES ("
                            + "'1', '" + checkNumber1.getText() + "', '" + bankNumber + "', '', "
                            + "'" + checkbookPayto1.getSelectedItem().toString() + "', '"
                            + AccountingUtil.dateFormat.format(checkDate1.getDate()) + "', '"
                            + checkAmount1.getText() + "', '" + checkMemo1.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "','" + checkStatus + "', '" + checkPaidToType + "')";

                    System.out.println("Insert 11111111 >>>>>>>>> " + insertQueries[0]);

                    // Journal Entry for check. Control = Check Number and Reference is Paid To Id

                    /*sqlQueries[1] = "INSERT INTO AccountingGLTable "
                     + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                     + "VALUES ("
                     + "'" + bankNumber + "', '0.00','" + depositsAmount2.getText() + "', '" + reference + "', "
                     + "'" + checkNumber1.getText() + "', " + "'" + AccountingUtil.dateFormat.format(depositsDate2.getDate()) + "', '" + checkPaidToType + "', "
                     + "'" + depositsMemo3.getText() + "', "
                     + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                     + "'" + billClass_checkbook1.getSelectedItem().toString() + "' "
                     + ")";
                     */


                    //System.out.println("AccountingGLTable 1 Edit = " +sqlInsert2[0]);

                    String accountNumber;
                    Double amountValue;
                    String controlNumber;
                    String memo;

                    int accountColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Account");
                    int amountColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Amount");
                    int controlColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Control #");
                    int memoColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Memo");

                    for (int i = 0; i <= checkAccountsTable1.getRowCount() - 1; i++) {
                        accountNumber = null;
                        controlNumber = null;
                        if (checkAccountsTable1.getValueAt(i, accountColumn) != null) {
                            accountNumber = checkAccountsTable1.getValueAt(i, accountColumn).toString();
                            accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
                        }
                        Object amountVal = checkAccountsTable1.getValueAt(i, amountColumn);
                        if (amountVal != null && !amountVal.toString().isEmpty()) {
                            if (amountVal.toString().contains(",")) {
                                amountVal = amountVal.toString().replaceAll(",", "");

                            }
                            amountValue = Double.parseDouble(amountVal.toString());
                        } else {
                            amountValue = 0.00;
                        }
                        if (checkAccountsTable1.getValueAt(i, controlColumn) != null) {
                            controlNumber = checkAccountsTable1.getValueAt(i, controlColumn).toString();
                        } else {
                            controlNumber = "";
                        }

                        if (checkAccountsTable1.getValueAt(i, memoColumn) != null) {
                            memo = checkAccountsTable1.getValueAt(i, memoColumn).toString();
                        } else {
                            memo = "";
                        }

                        insertQueries[1] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                                + "ReferenceNumber, PostDate, Memo, GLType, LotName, Class) VALUES("
                                + "'" + accountNumber + "', '" + amountValue + "', '0.00', '" + controlNumber + "', '"
                                + checkNumber1.getText() + "', '" + AccountingUtil.dateFormat.format(checkDate1.getDate()) + "', '"
                                + memo + "', '" + checkLinePaidToType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                                + "'" + checkClass1.getSelectedItem().toString() + "'"
                                + ")";

                        System.out.println("Query 222222222  " + insertQueries[1]);
                        dms.DMSApp.getApplication().getDBConnection().executeStatements(insertQueries, this);
                    }
                    reloadChecks();
                    clearDeposits();
                    dms.DMSApp.displayMessage(checkAccountsTable1, "Record Updated Successfully ", JOptionPane.INFORMATION_MESSAGE);
                    editCheckDialog.dispose();
                }
            }

        } catch (Exception e) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }//GEN-LAST:event_saveDepositButton3

    private void jButton51saveCheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton51saveCheckButtonsClicked
        // TODO add your handling code here:
        // Save - edit check
        boolean good = true;
        String bankNumber = "";
        String glNumber = "";
        boolean isCheckUnique = true;
        boolean checkForDuplicate = true;
        int depositsRowCount = checkAccountsTable1.getRowCount();

        if (checkAmount1.getText().isEmpty()) {
            good = false;
            dms.DMSApp.displayMessage(this, "The check amount is empty", dms.DMSApp.WARNING_MESSAGE);
        } else {
            if (checkAccount1.getText() != null && !checkAccount1.getText().isEmpty()) {
                String depositsTotalAccountsStringVal = checkAccount1.getText().toString();
                if (depositsTotalAccountsStringVal.contains(",")) {
                    depositsTotalAccountsStringVal = depositsTotalAccountsStringVal.replaceAll(",", "");
                }

                String depositsAmountStringVal = checkAmount1.getText().toString();
                if (depositsAmountStringVal.contains(",")) {
                    depositsAmountStringVal = depositsAmountStringVal.replaceAll(",", "");
                }
                if (Double.parseDouble(depositsTotalAccountsStringVal)
                        != Double.parseDouble(depositsAmountStringVal)) {
                    good = false;
                    dms.DMSApp.displayMessage(this, "The check amount and accounts total are not the same", dms.DMSApp.WARNING_MESSAGE);
                }
            }
        }

        if (!checkCombo1.getSelectedItem().equals("")) {
            bankNumber = "" + checkCombo1.getSelectedItem();
            bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
        } else {
            good = false;
            dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
        }
        int statusColumn = AccountingUtil.getColumnByName(checksBottomTable, "Status");
        Object status = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(), statusColumn);
        if (status == null) {
            status = "";
        }
        String checkStatus = status.toString();
        System.out.println("Status : " + checkStatus);

        if (depositsRowCount > 0) {

            int accountColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Account");
            int controlColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Control #");

            for (int i = 0; i <= checkAccountsTable1.getRowCount() - 1; i++) {
                String accountNumber = null;
                accountNumber = null;

                if (checkAccountsTable1.getValueAt(i, accountColumn) != null) {
                    accountNumber = checkAccountsTable1.getValueAt(i, accountColumn).toString();
                    accountNumber = accountNumber.split("-")[0];
                }

                Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                //System.out.println("isControlled : " + isControlled);
                if (isControlled && checksBottomTable.getValueAt(i, controlColumn) == null) {
                    good = false;
                    dms.DMSApp.displayMessage(this,
                            "Please provide Control # value for Row." + ++i, JOptionPane.WARNING_MESSAGE);
                } else if (isControlled
                        && checkAccountsTable1.getValueAt(i, controlColumn) != null
                        && checkAccountsTable1.getValueAt(i, controlColumn).toString().isEmpty()) {
                    good = false;
                    dms.DMSApp.displayMessage(this,
                            "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

        } else {
            good = false;
        }

        try {
            if (good) {
                String orignalCheckNumber = String.valueOf(getDepositNo());
                //System.out.println("orignal chkno = " + orignalCheckNumber);
                String checkNumberToBeEdited = checkNumber1.getText().toString();
                //System.out.println("edited chkno = " + checkNumberToBeEdited);
                if (checkNumberToBeEdited.equals(orignalCheckNumber)) {
                    //System.out.println("if.. same chk no");
                    checkForDuplicate = false;
                }
                //System.out.println("checkForDup = " + checkForDuplicate);

                if (checkForDuplicate) {
                    // Check for Duplicate CheckNumber
                    int checkNum = 0;
                    if (checkNumber1.getText().isEmpty()) {
                        dms.DMSApp.displayMessage(this, "The check# is blank", dms.DMSApp.WARNING_MESSAGE);
                        return;
                    } else {
                        try {
                            checkNum = Integer.parseInt(checkNumber1.getValue().toString());
                            String uniqueCheckNumberQuery = "select count(*) as Count from AccountingCBTable where CheckNumber = " + checkNum;

                            ResultSet rsUniqueCheckNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniqueCheckNumberQuery);

                            while (rsUniqueCheckNumber.next()) {

                                int count = rsUniqueCheckNumber.getInt("Count");
                                if (count > 0) {
                                    good = false;
                                    isCheckUnique = false;
                                    dms.DMSApp.displayMessage(this, "The check# " + checkNum + " already exists", dms.DMSApp.WARNING_MESSAGE);
                                    return;


                                }
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(BankingPanel.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }
                //System.out.println("check for unique chk no. = " + isCheckUnique);

                if (isCheckUnique) {
                    String sqlInsert1[] = new String[1];
                    String sqlInsert2[] = new String[1];

                    // Delete the record first
                    String deleteSql[] = new String[1];
                    String delSql[] = new String[1];
                    // Delete Vendor Invoice
                    deleteSql[0] = "DELETE From AccountingCBTable "
                            + "Where CheckNumber = '" + getDepositNo() + "' ";

                    //System.out.println("Delete Sql 1 := " + deleteSql[0]);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteSql, checkAccountsTable1);

                    // Delete Invoice Lines
                    delSql[0] = "DELETE From AccountingGLTable "
                            + "Where ReferenceNumber = '" + getDepositNo() + "' ";
                    //System.out.println("Delete Sql 2 := " + delSql[0]);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(delSql, checkAccountsTable1);

                    String checkPaidToType = "";
                    String checkLinePaidToType = "";
                    String reference = "";
                    if (checkTypeCombo1.getSelectedItem() == "Vendor") {
                        checkPaidToType = "Check - Vendor";
                        checkLinePaidToType = "Check Line - Vendor";
                        reference = AccountingUtil.getVendorID(checkbookPayto1.getSelectedItem().toString()).toString();
                    } else if (checkTypeCombo1.getSelectedItem() == "Customer") {
                        checkPaidToType = "Check - Customer";
                        checkLinePaidToType = "Check Line - Customer";
                        reference = AccountingUtil.getCustomerID(checkbookPayto1.getSelectedItem().toString()).toString();
                    } else if (checkTypeCombo1.getSelectedItem() == "Employee") {
                        checkPaidToType = "Check - Employee";
                        checkLinePaidToType = "Check Line - Employee";
                        reference = AccountingUtil.getEmployeeID(checkbookPayto1.getSelectedItem().toString()).toString();
                    }

                    // Insert New Record: enter check information in the checkbook table
                    sqlInsert1[0] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, CheckNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Status, Type) "
                            + "VALUES ("
                            + "'1', '" + checkNumber1.getText() + "', '" + bankNumber + "', '', "
                            + "'" + checkbookPayto1.getSelectedItem().toString() + "', '"
                            + AccountingUtil.dateFormat.format(checkDate1.getDate()) + "', '"
                            + checkAmount1.getText() + "', '" + checkMemo1.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "','" + checkStatus + "', '" + checkPaidToType + "')";

                    //System.out.println("AccountingCBTable Qry Edit = " + sqlInsert1[0]);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sqlInsert1, this);
                    // Journal Entry for check. Control = Check Number and Reference is Paid To Id

                    sqlInsert2[0] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                            + "VALUES ("
                            + "'" + bankNumber + "', '0.00','" + checkAmount1.getText() + "', '" + reference + "', "
                            + "'" + checkNumber1.getText() + "', " + "'" + AccountingUtil.dateFormat.format(checkDate1.getDate()) + "', '" + checkPaidToType + "', "
                            + "'" + checkMemo1.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'" + checkClass1.getSelectedItem().toString() + "' "
                            + ")";

                    //System.out.println("AccountingGLTable 1 Edit = " +sqlInsert2[0]);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sqlInsert2, this);

                    String[] sql1 = new String[1];

                    String accountNumber;
                    Double amount;
                    String controlNumber;
                    String referenceNumber;
                    String memo;

                    int accountColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Account");
                    int amountColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Amount");
                    int controlColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Control #");
                    int referenceColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Reference #");
                    int memoColumn = AccountingUtil.getColumnByName(checkAccountsTable1, "Memo");

                    for (int i = 0; i <= checkAccountsTable1.getRowCount() - 1; i++) {
                        accountNumber = null;
                        amount = 0.00;
                        controlNumber = null;
                        referenceNumber = null;
                        memo = null;

                        if (checkAccountsTable1.getValueAt(i, accountColumn) != null) {
                            accountNumber = checkAccountsTable1.getValueAt(i, accountColumn).toString();
                            accountNumber = accountNumber.substring(0, accountNumber.indexOf("-"));
                        }
                        Object amountVal = checkAccountsTable1.getValueAt(i, amountColumn);
                        if (amountVal != null && !amountVal.toString().isEmpty()) {
                            if (amountVal.toString().contains(",")) {
                                amountVal = amountVal.toString().replaceAll(",", "");

                            }
                            amount = Double.parseDouble(amountVal.toString());
                        } else {
                            amount = 0.00;
                        }
                        if (checkAccountsTable1.getValueAt(i, controlColumn) != null) {
                            controlNumber = checkAccountsTable1.getValueAt(i, controlColumn).toString();
                        } else {
                            controlNumber = "";
                        }

                        if (checkAccountsTable1.getValueAt(i, memoColumn) != null) {
                            memo = checkAccountsTable1.getValueAt(i, memoColumn).toString();
                        } else {
                            memo = "";
                        }

                        sql1[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                                + "ReferenceNumber, PostDate, Memo, GLType, LotName, Class) VALUES("
                                + "'" + accountNumber + "', '" + amount + "', '0.00', '" + controlNumber + "', '"
                                + checkNumber1.getText() + "', '" + AccountingUtil.dateFormat.format(checkDate1.getDate()) + "', '"
                                + memo + "', '" + checkLinePaidToType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                                + "'" + checkClass1.getSelectedItem().toString() + "'"
                                + ")";

                        //System.out.println("HERE 2 Edit: " + sql1[0]);
                        dms.DMSApp.getApplication().getDBConnection().executeStatements(sql1, this);
                    }
                    dms.DMSApp.displayMessage(checkAccountsTable1, "Record Updated Successfully ", JOptionPane.INFORMATION_MESSAGE);
                    reloadChecks();
                    clearDeposits();
                }
            }

        } catch (Exception e) {
            dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
        }
        // Print check
    }//GEN-LAST:event_jButton51saveCheckButtonsClicked

    private void clearCheck4(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearCheck4
        // TODO add your handling code here:
        clearCheck1();
        AccountingUtil.showMessageBalloon("Check data cleared, keeping the minimum required one account line intact", checkAccountsTable1);
    }//GEN-LAST:event_clearCheck4

    private boolean isControlNumRequiredForTable(JTable table) {
        int accountColumn = AccountingUtil.getColumnByName(table, "Account");
        int controlColumn = AccountingUtil.getColumnByName(table, "Control #");

        for (int i = 0; i < table.getRowCount(); i++) {
            int accountNumber = -1;
            Object accNumString = table.getValueAt(i, accountColumn);
            if (!accNumString.toString().isEmpty()) {
                accountNumber = Integer.valueOf(accNumString.toString().split("-")[0].trim());
            }
            Boolean isControlled = AccountingUtil.getControlNumMap().get(accountNumber);
            if (isControlled && table.getValueAt(i, controlColumn) == null) {
                return true;
            } else if (isControlled
                    && table.getValueAt(i, controlColumn) != null
                    && table.getValueAt(i, controlColumn).toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        // TODO add your handling code here:

        DefaultTableModel aModel = (DefaultTableModel) checkAccountsTable1.getModel();
        int rowCount = checkAccountsTable1.getRowCount();
        checkbookRowNo = rowCount;
        if (rowCount == 0) {
            addNewRowForEditCheck(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
        }

        if (isRowRemoved) {
            checkbookRowNo = rowCount;
            isRowRemoved = false;
        }
        if (checkAccountsTable1.getValueAt(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Account")) != null) {
            account = checkAccountsTable1.getValueAt(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Account")).toString();
        }
        if (checkAccountsTable1.getValueAt(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Amount")) != null) {
            amount = checkAccountsTable1.getValueAt(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Amount")).toString();
        }
        if (account == null) {
            dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.WARNING_MESSAGE);
            checkAccountsTable1.setCellSelectionEnabled(true);
            checkAccountsTable1.changeSelection(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Account"), false, false);
            checkAccountsTable1.requestFocus();
        }
        if (amount == null || amount.isEmpty()) {
            dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.WARNING_MESSAGE);
            checkAccountsTable1.setCellSelectionEnabled(true);
            checkAccountsTable1.changeSelection(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Amount"), false, false);
            checkAccountsTable1.requestFocus();
        } else if (amount != null) {
            if (!AccountingUtil.displayNumeric(amount)) {
                //System.out.println("Amount is not Numeric");
                dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.WARNING_MESSAGE);
                checkAccountsTable1.setCellSelectionEnabled(true);
                checkAccountsTable1.changeSelection(checkbookRowNo - 1, checkAccountsTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                checkAccountsTable1.requestFocus();
            }
        }
        if (isControlNumRequiredForTable(checkAccountsTable1)) {
            dms.DMSApp.displayMessage(this,
                    "Please provide Control # value for current row", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // check to insert a row
        if (account != null && amount != null && AccountingUtil.displayNumeric(amount)) {
            addNewRowForEditCheck(aModel, account);
            checkbookRowNo++;
        }
        //addNewRowCheckbookaddNewRow(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
    }//GEN-LAST:event_jButton28ActionPerformed

    private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
        int rowCount = checkAccountsTable1.getRowCount();
        if (rowCount == 1) {
            dms.DMSApp.displayMessage(this, "At least one account is required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultTableModel tableModel = (DefaultTableModel) checkAccountsTable1.getModel();
        tableModel.removeRow(tableModel.getRowCount() - 1);
        rowCount--;
        isRowRemoved = true;
        double sum = 0.00;
        for (int i = 0; i < rowCount; i++) {
            if (checkAccountsTable1.getValueAt(i, 1) != null && !checkAccountsTable1.getValueAt(i, 1).toString().isEmpty()) {
                if (AccountingUtil.displayNumeric(checkAccountsTable1.getValueAt(i, 1).toString())) {
                    if (checkAccountsTable1.getValueAt(i, 1).toString().contains(",")) {
                        sum += Double.parseDouble(checkAccountsTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                    } else {
                        sum += Double.parseDouble(checkAccountsTable1.getValueAt(i, 1).toString());
                        //System.out.println("sum 2:= " + sum);
                    }
                }
            }
        } // end for loop
        checkAccount1.setValue(sum);
    }//GEN-LAST:event_jButton29ActionPerformed

    private void checkTypeCombo1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkTypeCombo1ItemStateChanged
        typeSelected1();
    }//GEN-LAST:event_checkTypeCombo1ItemStateChanged

    private void checkAccountsTable1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_checkAccountsTable1InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_checkAccountsTable1InputMethodTextChanged

    private void billClass_deposit1ItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_billClass_deposit1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_billClass_deposit1ItemStateChanged

    private void checkAccountsTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_checkAccountsTable1FocusGained
        // TODO add your handling code here:
        accountsToBeShownList.clear();
    }//GEN-LAST:event_checkAccountsTable1FocusGained

    private void depositsAccountsTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_depositsAccountsTable1FocusGained
        // TODO add your handling code here:
        accountsToBeShownEditDepositList.clear();
    }//GEN-LAST:event_depositsAccountsTable1FocusGained

    private void deleteDepositMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteDepositMouseClicked
        String memo = depositsBottomTable.getValueAt(depositsBottomTable.getSelectedRow(), depositsBottomTable.getColumnModel().getColumnIndex("Memo")).toString();
        String accountNumber;
        String query[] = new String[2];
        if (depositsBottomTable.getSelectedRowCount() == 0) {
            dms.DMSApp.displayMessage(depositsBottomTable, "Please select a record to delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int depositNum = Integer.parseInt(depositsBottomTable.getValueAt(depositsBottomTable.getSelectedRow(), depositsBottomTable.getColumnModel().getColumnIndex("Deposit #")).toString());
            String bank = depositsBottomTable.getValueAt(depositsBottomTable.getSelectedRow(), depositsBottomTable.getColumnModel().getColumnIndex("Bank")).toString();
            accountNumber = bank.split("-")[0].trim();
            if (memo.contains("VOID")) {
                int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected 'voided' deposit?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                } else if (response == JOptionPane.YES_OPTION) {
                    deleteVoidedDeposit(depositNum);
                    reloadDeposits();
                    return;
                }
            }
            int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected deposit?", "Confirm",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
            } else if (response == JOptionPane.YES_OPTION) {
                query[0] = "delete from AccountingCBTable Where DepositNumber = '" + depositNum + "' ";
                query[1] = "delete from AccountingGLTable where referencenumber = '" + depositNum + "' AND AccountNumber = '" + accountNumber + "' ";
                dms.DMSApp.getApplication().getDBConnection().executeStatements(query, this);
                dms.DMSApp.displayMessage(depositsAccountsTable1, "Selected deposit deleted successfully ", JOptionPane.INFORMATION_MESSAGE);
                reloadDeposits();
            }
        } catch (Exception e) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }//GEN-LAST:event_deleteDepositMouseClicked

    private void deleteCheckButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteCheckButtonMouseClicked

        boolean sureToDel = false;
        String checkNumber;
        String delSql[] = {"", ""};
        if (checksBottomTable.getSelectedRowCount() == 0) {
            dms.DMSApp.displayMessage(editCheckDialog, "Please select a record to edit", JOptionPane.WARNING_MESSAGE);
        } else if (checksBottomTable.getSelectedRowCount() > 1) {
            dms.DMSApp.displayMessage(editCheckDialog, "Please select only one record to edit", JOptionPane.WARNING_MESSAGE);
        } else {
            try {
                int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to Delete?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    sureToDel = false;
                } else if (response == JOptionPane.YES_OPTION) {
                    sureToDel = true;
                }
                if (sureToDel) {
                    checkNumber = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(), checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString();
                    //System.out.println("chq number: "+checkNumber);
                    delSql[0] = "delete from AccountingCBTable Where CheckNumber = '" + checkNumber + "' ";
                    delSql[1] = "delete from AccountingGLTable where referencenumber = '" + checkNumber + "' ";
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(delSql, this);
                    // Insert the message code inlcuding the reloading
                    reloadChecks();
                    dms.DMSApp.displayMessage(depositsAccountsTable1, "Selected check deleted successfully ", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }//GEN-LAST:event_deleteCheckButtonMouseClicked

    private void depositsAmountFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_depositsAmountFocusLost
        setTotalAccountsSumForDeposit();
    }//GEN-LAST:event_depositsAmountFocusLost

    private void checkbookAmountFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_checkbookAmountFocusLost
        setTotalAccountsSumForCheck();
    }//GEN-LAST:event_checkbookAmountFocusLost

    private void checksBottomTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checksBottomTableMouseClicked
        int selectedRow = checksBottomTable.getSelectedRow();
        if (checksStatusMapForDepositsPanel.get(selectedRow) != null && checksStatusMapForDepositsPanel.get(selectedRow).contains("Void")) {
            voidCheckButton.setEnabled(false);
        } else {
            voidCheckButton.setEnabled(true);
        }
    }//GEN-LAST:event_checksBottomTableMouseClicked

    private void depositsBottomTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_depositsBottomTableMouseClicked
        int selectedRow = depositsBottomTable.getSelectedRow();
        if (depositsStatusMapForDepositsPanel.get(selectedRow) != null && depositsStatusMapForDepositsPanel.get(selectedRow).contains("Void")) {
            voidDepositBtn.setEnabled(false);
        } else {
            voidDepositBtn.setEnabled(true);
        }
    }//GEN-LAST:event_depositsBottomTableMouseClicked

    private void saveCheckMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveCheckMouseClicked
        checkSave();
    }//GEN-LAST:event_saveCheckMouseClicked

    private void checkbookPaytoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkbookPaytoItemStateChanged
    }//GEN-LAST:event_checkbookPaytoItemStateChanged

        private void depositsSearchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_depositsSearchTextFieldKeyReleased
            if (evt.getKeyCode() == 10) {
                depositsSearchButtonMouseClicked(null);
            }
        }//GEN-LAST:event_depositsSearchTextFieldKeyReleased

        private void checksSearchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_checksSearchTextFieldKeyReleased
            if (evt.getKeyCode() == 10) {
                checksSearchButtonMouseClicked(null);
            }
        }//GEN-LAST:event_checksSearchTextFieldKeyReleased

        private void checkbookAchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkbookAchActionPerformed
            checkbookAchMouseClicked(null);
        }//GEN-LAST:event_checkbookAchActionPerformed

        private void checkbookCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkbookCheckActionPerformed
            checkbookCheckMouseClicked(null);
        }//GEN-LAST:event_checkbookCheckActionPerformed

    private void jideTabbedPane4ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jideTabbedPane4ComponentShown
    }//GEN-LAST:event_jideTabbedPane4ComponentShown

    public void typeSelected() {
        String type = checkTypeCombo.getSelectedItem().toString();
        checkbookPayto.removeAllItems();
        System.out.println("Type : " + type);

        try {
            if (type.equalsIgnoreCase("Vendor")) {
                vendorNames = AccountingHelper.getAllVendors();
                for (int i = 0; i < vendorNames.size(); i++) {
                    //System.out.println("item = " + vendorNames.get(i));
                    checkbookPayto.addItem(vendorNames.get(i));
                }
            } else if (type.equalsIgnoreCase("Customer")) {
                customerNames = AccountingHelperToLoadCustomers.getAllCustomers();
                for (int i = 0; i < customerNames.size(); i++) {
                    //System.out.println("item = " + customerNames.get(i));
                    checkbookPayto.addItem(customerNames.get(i));
                }
                customerNames1 = AccountingHelperToLoadCustomerLastRecords.getAllCustomers();
                for (int i = 0; i < customerNames1.size(); i++) {
                    //System.out.println("item = " + customerNames1.get(i));
                    checkbookPayto.addItem(customerNames1.get(i));
                }
            } else {
                employeeNames = AccountingHelper.getAllEmployees();
                for (int i = 0; i < employeeNames.size(); i++) {
                    //System.out.println("item = " + employeeNames.get(i));
                    checkbookPayto.addItem(employeeNames.get(i));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void typeSelected1() {
        String type = checkTypeCombo1.getSelectedItem().toString();
        checkbookPayto1.removeAllItems();

        try {
            if (type.equals("Vendor")) {
                vendorNames = AccountingHelper.getAllVendors();
                for (int i = 0; i < vendorNames.size(); i++) {
                    //System.out.println("item = " + vendorNames.get(i));
                    checkbookPayto1.addItem(vendorNames.get(i));
                }
            } else if (type.equals("Customer")) {
                customerNames = AccountingHelperToLoadCustomers.getAllCustomers();
                for (int i = 0; i < customerNames.size(); i++) {
                    //System.out.println("item = " + customerNames.get(i));
                    checkbookPayto1.addItem(customerNames.get(i));
                }
                customerNames1 = AccountingHelperToLoadCustomerLastRecords.getAllCustomers();
                for (int i = 0; i < customerNames1.size(); i++) {
                    //System.out.println("item = " + customerNames1.get(i));
                    checkbookPayto1.addItem(customerNames1.get(i));
                }
            } else {
                employeeNames = AccountingHelper.getAllEmployees();
                for (int i = 0; i < employeeNames.size(); i++) {
                    //System.out.println("item = " + employeeNames.get(i));
                    checkbookPayto1.addItem(employeeNames.get(i));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void addNewRowCheckbook(DefaultTableModel tableModel, String account) {
        String accTypeDesc = account;
        tableModel.addRow(new Object[]{
            //accTypeDesc, "", AccountingUtil.getVendorID(checkbookPayto.getSelectedItem().toString()), checkbookNumber.getText(), ""
            accTypeDesc, "", "", checkBookNumber.getText(), ""
        });
    }

    private void addNewRowForEditCheck(DefaultTableModel tableModel, String account) {
        String accTypeDesc = account;
        tableModel.addRow(new Object[]{
            accTypeDesc, "", "", checkNumber1.getText(), ""
        });
    }

    public void clearCheck() {
        checkbookPayto.setSelectedItem("");
        checkbookAmount.setText("0.00");
        checkBookNumber.setText("");
        checkbookMemo.setText("");
        checksTotalAccounts.setText("0.00");
        bankSelected();

        DefaultTableModel aModel = (DefaultTableModel) checkbookAccountsTable.getModel();
        clearTableModelForBankingPanel(aModel);

        setRefNumValueForTable(checkbookAccountsTable, checkBookNumber.getText().toString());
    }

    public void clearCheck1() {
        checkAmount1.setText("0.00");
        bankSelected();

        DefaultTableModel aModel = (DefaultTableModel) checkAccountsTable1.getModel();
        clearTableModelForBankingPanel(aModel);
    }

    public void clearDeposits() {
        depositsAmount.setText("0.00");
        depositsNumber.setText("");
        depositsMemo.setText("");
        depositsTotalAccounts.setText("0.00");
        DefaultTableModel aModel = (DefaultTableModel) depositsAccountsTable.getModel();
        clearTableModelForBankingPanel(aModel);
        bankDepositsSelected();
    }

    private void bankSelected() {
        if (checkbookBankCombo.getSelectedItem() != null) {
            if (!checkbookBankCombo.getSelectedItem().equals("")) {
                try {
                    String bankNumber = "" + checkbookBankCombo.getSelectedItem();

                    bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
                    String query = "select "
                            + "sum(Credit-Debit) AS Balance,"
                            + "(Select Top 1 CheckNumber From AccountingCBTable "
                            + "Where BankAccount = " + bankNumber + " AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                            + "ORDER BY CheckNumber Desc) AS CheckNumber, "
                            + "(Select Top 1 AchCheckNumber From AccountingCBTable "
                            + "Where BankAccount = " + bankNumber + " AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                            + "ORDER BY AchCheckNumber Desc) AS AchCheckNumber "
                            + "From AccountingGLTable "
                            + "Where AccountNumber = " + bankNumber + " "
                            + "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                    ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                    System.out.println("Bank sel query = " + query);
                    if (rs.next()) {
                        checkbookEndingBalance.setValue(rs.getDouble("Balance"));

                        if (checkbookCheck.isSelected()) {
                            if (rs.getInt("CheckNumber") == 0) {
                                checkBookNumber.setValue(1);
                            } else {
                                checkBookNumber.setValue(rs.getInt("CheckNumber") + 1);
                            }
                        }
                        if (checkbookAch.isSelected()) {
                            if (rs.getInt("AchCheckNumber") == 0) {
                                checkBookNumber.setValue(1);
                            } else {
                                checkBookNumber.setValue(rs.getInt("AchCheckNumber") + 1);
                            }
                        }
                    }

                    rs.getStatement().close();
                } catch (Exception e) {
                    Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {
                checkbookEndingBalance.setValue(0.00);
            }
        }
    }

    private void bankDepositsSelected() {
        if (depositsBankCombo.getSelectedItem() != null) {
            if (!depositsBankCombo.getSelectedItem().equals("")) {
                try {
                    String bankNumber = "" + depositsBankCombo.getSelectedItem();

                    bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
                    String sql = "select "
                            + "sum(Credit-Debit) AS Balance,"
                            + "(Select Top 1 DepositNumber From AccountingCBTable "
                            + "Where BankAccount = " + bankNumber + " AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' Order By DepositNumber Desc) AS DepositNumber "
                            + "From AccountingGLTable "
                            + "Where AccountNumber = " + bankNumber + " "
                            + "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                    ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                    //System.out.println("Dep sql = " + sql);
                    if (rs.next()) {
                        depositsEndingBalance.setValue(rs.getDouble("Balance"));
                        if (rs.getInt("DepositNumber") == 0) {
                            depositsNumber.setValue(1);
                        } else {
                            depositsNumber.setValue(rs.getInt("DepositNumber") + 1);
                        }
                    }

                    rs.getStatement().close();
                } catch (Exception e) {
                }
            } else {
                depositsEndingBalance.setValue(0.00);
            }
        }

    }

    private void editBankDepositsSelected() {
        if (depositsBankCombo2.getSelectedItem() != null) {
            if (!depositsBankCombo2.getSelectedItem().equals("")) {
                try {
                    String bankNumber = "" + depositsBankCombo2.getSelectedItem();

                    bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
                    String query = "select "
                            + "sum(Credit-Debit) AS Balance,"
                            + "(Select Top 1 DepositNumber From AccountingCBTable "
                            + "Where BankAccount = " + bankNumber + " AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' Order By DepositNumber Desc) AS DepositNumber "
                            + "From AccountingGLTable "
                            + "Where AccountNumber = " + bankNumber + " "
                            + "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                    ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                    //System.out.println(sql);
                    if (rs.next()) {
                        depositsEndingBalance1.setValue(rs.getDouble("Balance"));
                    }

                    rs.getStatement().close();
                } catch (Exception e) {
                }
            } else {
                depositsEndingBalance1.setValue(0.00);
            }
        }

    }

    private void loadEndingBalanceForEditCheckDialog() {
        if (checkCombo1.getSelectedItem() != null) {
            if (!checkCombo1.getSelectedItem().equals("")) {
                try {
                    String bankNumber = "" + checkCombo1.getSelectedItem();

                    bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
                    String sql = "select "
                            + "sum(Credit-Debit) AS Balance,"
                            + "(Select Top 1 CheckNumber From AccountingCBTable "
                            + "Where BankAccount = " + bankNumber + " AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' Order By CheckNumber Desc) AS CheckNumber "
                            + "From AccountingGLTable "
                            + "Where AccountNumber = " + bankNumber + " "
                            + "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                    ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                    //System.out.println(sql);
                    if (rs.next()) {
                        depositsEndingBalance2.setValue(rs.getDouble("Balance"));
                    }

                    rs.getStatement().close();
                } catch (Exception e) {
                }
            } else {
                depositsEndingBalance2.setValue(0.00);
            }
        }

    }

    private void reloadDeposits() {
        if (!dms.DMSApp.getApplication().getCurrentlotName().isEmpty()) {
            try {
                ResultSet rs;

                String dateFilter = "";

                if (!depositsAllDates.isSelected()) {
                    dateFilter = "AND Date BETWEEN '" + AccountingUtil.dateFormat.format(jDateChooser14.getDate()) + "' AND '" + AccountingUtil.dateFormat.format(jDateChooser15.getDate()) + "' ";
                }

                String query = "SELECT CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description,DepositNumber,CAST(ROUND(Amount,2) AS NUMERIC(10,2)),WrittenDate,Memo, A.Type, A.Status  "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingCOATable B "
                        + "ON A.GLAccount = B.AccountNumber "
                        + "LEFT JOIN AccountingCOATable C "
                        + "ON A.BankAccount = C.AccountNumber "
                        + "WHERE A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + "AND A.DepositNumber <> 0 "
                        + dateFilter
                        + "ORDER BY DepositNumber Desc ";

                query = "SELECT "
                        + " CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description, A.DepositNumber,  "
                        + "CAST(ROUND(A.Amount,2) AS NUMERIC(10,2)) AS Amount,A.Date,A.Memo, A.Type, A.Status "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingCOATable C ON (A.BankAccount = C.AccountNumber) "
                        + "WHERE A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + dateFilter
                        + "AND A.Type LIKE '%Deposit%' "
                        + "ORDER BY CheckNumber DESC";

                System.out.println("Deposits: " + query);

                rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                DefaultTableModel aModel = (DefaultTableModel) depositsBottomTable.getModel();
                AccountingUtil.clearTableModel(aModel);

                ResultSetMetaData rsmd = rs.getMetaData();
                int colNo = rsmd.getColumnCount();
                int row = 0;
                while (rs.next()) {
                    Object[] values = new Object[colNo];
                    depositsStatusMapForDepositsPanel.put(row, rs.getString("Status"));
                    for (int i = 0; i < colNo; i++) {
                        if (i == AccountingUtil.getColumnByName(depositsBottomTable, "Deposit Date")) {
                            values[i] = AccountingUtil.dateFormat1.format(rs.getObject("Date"));
                        } else if (i == AccountingUtil.getColumnByName(depositsBottomTable, "Amount")) {
                            values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject("Amount").toString());
                        } else {
                            values[i] = rs.getObject(i + 1);
                        }
                    }
                    aModel.addRow(values);
                    row++;
                }

                rs.getStatement().close();

            } catch (SQLException ex) {
                Logger.getLogger(AccountingWindow.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void reloadChecks() {
        if (!dms.DMSApp.getApplication().getCurrentlotName().isEmpty()) {
            try {
                ResultSet rs;
                String dateFilter = "";

                if (!checkbookAllDates.isSelected()) {
                    dateFilter = "AND Date BETWEEN '" + AccountingUtil.dateFormat.format(jDateChooser12.getDate()) + "' AND '" + AccountingUtil.dateFormat.format(jDateChooser13.getDate()) + "' ";
                }
                String query;
                query = "SELECT "
                        + " CAST(C.AccountNumber AS nvarchar(100)) + ' - ' + C.Description, A.CheckNumber, A.AchCheckNumber, "
                        + "A.PaidTo,CAST(ROUND(A.Amount,2) AS NUMERIC(10,2)) AS Amount,A.Date,A.Memo, A.Type, A.Status "
                        + "FROM AccountingCBTable A "
                        + "LEFT JOIN AccountingCOATable C ON (A.BankAccount = C.AccountNumber) "
                        + "WHERE A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' "
                        + dateFilter
                        + "AND A.Type LIKE '%Check%' "
                        + "ORDER BY CheckNumber DESC";

                System.out.println("Reloading Checks : " + query);
                rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                DefaultTableModel aModel = (DefaultTableModel) checksBottomTable.getModel();
                AccountingUtil.clearTableModel(aModel);

                ResultSetMetaData rsmd = rs.getMetaData();
                int colNo = rsmd.getColumnCount();

                int row = 0;
                while (rs.next()) {
                    Object[] values = new Object[colNo];
                    checksStatusMapForDepositsPanel.put(row, rs.getString("Status"));
                    for (int i = 0; i < colNo; i++) {
                        if (i == AccountingUtil.getColumnByName(checksBottomTable, "Check Date")) {
                            values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                        } else if (i == AccountingUtil.getColumnByName(checksBottomTable, "Amount")) {
                            values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject("Amount").toString());
                        } else {
                            values[i] = rs.getObject(i + 1);
                        }
                    }
                    aModel.addRow(values);
                    row++;
                }
                rs.getStatement().close();

            } catch (SQLException ex) {
                Logger.getLogger(AccountingWindow.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void printChecks(String checkNumber, String bankNumber) {
        try {
            String l = "select WrittenDate AS date,"
                    + "PaidTo AS payto,"
                    + "Amount AS dollarvalue,"
                    + "<PutDollarAmountInWordsFieldHere> AS dollarwords,"
                    + "Memo AS memo "
                    + "From AccountingCBTable "
                    + "Where CheckNumber = " + checkNumber + " "
                    + "AND BankAccount = " + bankNumber + "";

            ResultSet rs1 = dms.DMSApp.getApplication().getDBConnection().getResultSet("Select FormCompiled From FormsTable Where FormName = 'CheckWriter'");
            if (rs1.next()) {
                InputStream formXML = rs1.getBlob("FormCompiled").getBinaryStream();
                rs1.close();
                rs1 = dms.DMSApp.getApplication().getDBConnection().getResultSet(l);

                //IF Previewing
          /*PrintingUtil.previewReport(previewPanel, rs1, formXML);
                 previewPopup.setPreferredSize(new Dimension(600, 700));
                 previewPopup.setMinimumSize(new Dimension(600, 700));
                 previewPopup.setMaximumSize(new Dimension(600, 700));
                 previewPopup.setLocationRelativeTo(this);
                 dms.DMSApp.getApplication().show(previewPopup);*/
                //IF Printing Directly
                PrintingUtil.printReport(rs1, formXML);
            }
        } catch (Exception e) {
            dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel apCheckbookPanel;
    private javax.swing.JPanel apDepositsPanel;
    public com.jidesoft.swing.AutoCompletionComboBox billClass_checkbook;
    public com.jidesoft.swing.AutoCompletionComboBox billClass_deposit;
    public com.jidesoft.swing.AutoCompletionComboBox billClass_deposit1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JFormattedTextField checkAccount1;
    private javax.swing.JTable checkAccountsTable1;
    private javax.swing.JFormattedTextField checkAmount1;
    private javax.swing.JFormattedTextField checkBookNumber;
    private javax.swing.JComboBox checkClass1;
    public javax.swing.JComboBox checkCombo1;
    private com.toedter.calendar.JDateChooser checkDate1;
    private javax.swing.JTextField checkMemo1;
    private javax.swing.JFormattedTextField checkNumber1;
    private javax.swing.JComboBox checkTypeCombo;
    private javax.swing.JComboBox checkTypeCombo1;
    private javax.swing.JTable checkbookAccountsTable;
    private javax.swing.JRadioButton checkbookAch;
    private javax.swing.JCheckBox checkbookAllDates;
    private javax.swing.JFormattedTextField checkbookAmount;
    public static javax.swing.JComboBox checkbookBankCombo;
    private javax.swing.JRadioButton checkbookCheck;
    private com.toedter.calendar.JDateChooser checkbookDate;
    private javax.swing.JFormattedTextField checkbookEndingBalance;
    private javax.swing.JLabel checkbookEntryID;
    private javax.swing.JLabel checkbookEntryID1;
    private javax.swing.JLabel checkbookEntryID3;
    private javax.swing.JLabel checkbookEntryID4;
    private javax.swing.JTextField checkbookMemo;
    public static com.jidesoft.swing.AutoCompletionComboBox checkbookPayto;
    public static javax.swing.JComboBox checkbookPayto1;
    private javax.swing.JTable checksBottomTable;
    private javax.swing.JButton checksSearchButton;
    private javax.swing.JTextField checksSearchTextField;
    private javax.swing.JFormattedTextField checksTotalAccounts;
    private javax.swing.JButton clearCheck;
    private javax.swing.JButton clearCheck3;
    private javax.swing.JButton clearCheck4;
    private javax.swing.JButton clearDeposit;
    private javax.swing.JButton deleteCheckButton;
    private javax.swing.JButton deleteDeposit;
    private javax.swing.JRadioButton depositsACH;
    private javax.swing.JRadioButton depositsACH1;
    private javax.swing.JRadioButton depositsACH2;
    private javax.swing.JTable depositsAccountsTable;
    private javax.swing.JTable depositsAccountsTable1;
    private javax.swing.JCheckBox depositsAllDates;
    private javax.swing.JFormattedTextField depositsAmount;
    private javax.swing.JFormattedTextField depositsAmount1;
    public static javax.swing.JComboBox depositsBankCombo;
    public javax.swing.JComboBox depositsBankCombo2;
    private javax.swing.JTable depositsBottomTable;
    private com.toedter.calendar.JDateChooser depositsDate;
    private com.toedter.calendar.JDateChooser depositsDate1;
    private javax.swing.JRadioButton depositsDeposit;
    private javax.swing.JRadioButton depositsDeposit1;
    private javax.swing.JRadioButton depositsDeposit2;
    private javax.swing.JFormattedTextField depositsEndingBalance;
    private javax.swing.JFormattedTextField depositsEndingBalance1;
    private javax.swing.JFormattedTextField depositsEndingBalance2;
    private javax.swing.JTextField depositsMemo;
    private javax.swing.JTextField depositsMemo2;
    private javax.swing.JFormattedTextField depositsNumber;
    private javax.swing.JFormattedTextField depositsNumber1;
    private javax.swing.JButton depositsSearchButton;
    private javax.swing.JTextField depositsSearchTextField;
    private javax.swing.JFormattedTextField depositsTotalAccounts;
    private javax.swing.JFormattedTextField depositsTotalAccounts1;
    private javax.swing.JButton editCheckButton;
    private javax.swing.JDialog editCheckDialog;
    private javax.swing.JDialog editDepositDialog1;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private com.toedter.calendar.JDateChooser jDateChooser12;
    private com.toedter.calendar.JDateChooser jDateChooser13;
    private com.toedter.calendar.JDateChooser jDateChooser14;
    private com.toedter.calendar.JDateChooser jDateChooser15;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel153;
    private javax.swing.JLabel jLabel155;
    private javax.swing.JLabel jLabel156;
    private javax.swing.JLabel jLabel158;
    private javax.swing.JLabel jLabel159;
    private javax.swing.JLabel jLabel160;
    private javax.swing.JLabel jLabel161;
    private javax.swing.JLabel jLabel162;
    private javax.swing.JLabel jLabel163;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel100;
    private javax.swing.JPanel jPanel101;
    private javax.swing.JPanel jPanel102;
    private javax.swing.JPanel jPanel110;
    private javax.swing.JPanel jPanel111;
    private javax.swing.JPanel jPanel112;
    private javax.swing.JPanel jPanel113;
    private javax.swing.JPanel jPanel115;
    private javax.swing.JPanel jPanel116;
    private javax.swing.JPanel jPanel117;
    private javax.swing.JPanel jPanel118;
    private javax.swing.JPanel jPanel119;
    private javax.swing.JPanel jPanel121;
    private javax.swing.JPanel jPanel122;
    private javax.swing.JPanel jPanel123;
    private javax.swing.JPanel jPanel124;
    private javax.swing.JPanel jPanel125;
    private javax.swing.JPanel jPanel126;
    private javax.swing.JPanel jPanel128;
    private javax.swing.JPanel jPanel129;
    private javax.swing.JPanel jPanel130;
    private javax.swing.JPanel jPanel131;
    private javax.swing.JPanel jPanel132;
    private javax.swing.JPanel jPanel133;
    private javax.swing.JPanel jPanel134;
    private javax.swing.JPanel jPanel135;
    private javax.swing.JPanel jPanel136;
    private javax.swing.JPanel jPanel137;
    private javax.swing.JPanel jPanel138;
    private javax.swing.JPanel jPanel139;
    private javax.swing.JPanel jPanel145;
    private javax.swing.JPanel jPanel146;
    private javax.swing.JPanel jPanel148;
    private javax.swing.JPanel jPanel149;
    private javax.swing.JPanel jPanel154;
    private javax.swing.JPanel jPanel155;
    private javax.swing.JPanel jPanel158;
    private javax.swing.JPanel jPanel159;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel160;
    private javax.swing.JPanel jPanel161;
    private javax.swing.JPanel jPanel169;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel170;
    private javax.swing.JPanel jPanel172;
    private javax.swing.JPanel jPanel173;
    private javax.swing.JPanel jPanel174;
    private javax.swing.JPanel jPanel175;
    private javax.swing.JPanel jPanel176;
    private javax.swing.JPanel jPanel177;
    private javax.swing.JPanel jPanel178;
    private javax.swing.JPanel jPanel179;
    private javax.swing.JPanel jPanel180;
    private javax.swing.JPanel jPanel185;
    private javax.swing.JPanel jPanel186;
    private javax.swing.JPanel jPanel187;
    private javax.swing.JPanel jPanel188;
    private javax.swing.JPanel jPanel189;
    private javax.swing.JPanel jPanel190;
    private javax.swing.JPanel jPanel191;
    private javax.swing.JPanel jPanel192;
    private javax.swing.JPanel jPanel193;
    private javax.swing.JPanel jPanel194;
    private javax.swing.JPanel jPanel196;
    private javax.swing.JPanel jPanel197;
    private javax.swing.JPanel jPanel199;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel90;
    private javax.swing.JPanel jPanel91;
    private javax.swing.JPanel jPanel93;
    private javax.swing.JPanel jPanel94;
    private javax.swing.JPanel jPanel95;
    private javax.swing.JPanel jPanel96;
    private javax.swing.JPanel jPanel97;
    private javax.swing.JPanel jPanel98;
    private javax.swing.JPanel jPanel99;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane25;
    private javax.swing.JScrollPane jScrollPane3;
    private com.jidesoft.swing.JideTabbedPane jideTabbedPane4;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JDialog previewPopup;
    private javax.swing.JButton printCheckButton;
    private javax.swing.JButton saveCheck;
    private javax.swing.JButton saveDepositButton;
    private javax.swing.JButton saveDepositButton2;
    private javax.swing.JButton saveDepositButton3;
    private javax.swing.JButton voidCheckButton;
    private javax.swing.JButton voidDepositBtn;
    // End of variables declaration//GEN-END:variables

    public void updateCheckStatus(int checkNo, int bankAccount, String status) {
        String[] updateQuery = new String[1];
        // AccountingCBTable
        updateQuery[0] = "UPDATE AccountingCBTable "
                + "SET Status = '" + status + "' "
                + "WHERE CheckNumber = '" + checkNo + "' AND BankAccount = '" + bankAccount + "'";

        dms.DMSApp.getApplication().getDBConnection().executeStatements(updateQuery, this);
    }

    private void addComboBoxesToTables() {

        if (AccountingUtil.getAllAccountsFull() != null) {
            Object[] accounts = AccountingUtil.getAllAccountsFull();

            TableColumn column = depositsAccountsTable.getColumnModel().getColumn(0);
            column.setCellRenderer(new MyComboBoxRenderer(accounts));
            column.setCellEditor(new MyComboBoxEditor(accounts));

            TableColumn column1 = checkbookAccountsTable.getColumnModel().getColumn(0);
            column1.setCellRenderer(new MyComboBoxRenderer(accounts));
            column1.setCellEditor(new MyComboBoxEditor(accounts));

            addComboBoxesToTableForEditDeposit();
            addComboBoxesToTableForEditCheck();

        }
    }

    public class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {

        public MyComboBoxRenderer(Object[] items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (column == 0) {
                this.setBackground(new java.awt.Color(255, 255, 255));
            }

            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }

    public class MyComboBoxEditor extends DefaultCellEditor {

        public MyComboBoxEditor(Object[] items) {
            super(new JComboBox(items));
        }
    }

    private void addNewDepositsRow(DefaultTableModel tableModel, String account) {
        String accTypeDesc = account;
        tableModel.addRow(new Object[]{
            //null, "0.00", "0.00", "", ""
            accTypeDesc, "", "", depositsNumber.getText(), ""
        });
    }

    public static void reloadBankAccounts() {
        String[] accounts = AccountingUtil.getBankAccountsNumberAndDesc();
        if (accounts != null) {
            accountsPayablePanel.paybillsBankCombo.removeAllItems();
            accountsPayablePanel.paybillsBankCombo.addItem("");
            accountsPayablePanel.paidbillsBankCombo.removeAllItems();
            accountsPayablePanel.paidbillsBankCombo.addItem("");
            depositsBankCombo.removeAllItems();
            checkbookBankCombo.removeAllItems();

            for (int j = 0; j < accounts.length; j++) {
                accountsPayablePanel.paybillsBankCombo.addItem(accounts[j]);
                accountsPayablePanel.paidbillsBankCombo.addItem(accounts[j]);
                depositsBankCombo.addItem(accounts[j]);
                checkbookBankCombo.addItem(accounts[j]);
            }

            depositsBankCombo.setSelectedItem("202 - Cash in Bank-BOA");
            checkbookBankCombo.setSelectedItem("202 - Cash in Bank-BOA");
        }
    }

    private boolean checkSave() {
        boolean good = true;
        String bankNumber;
        String checkPaidToType = "";
        String checkLinePaidToType = "";
        String payerId = "";
        int journalPostingRowCount = checkbookAccountsTable.getRowCount();
        int checkNum = 0;

        if (checkBookNumber.getText().isEmpty()) {
            dms.DMSApp.displayMessage(this, "The check# is blank", dms.DMSApp.WARNING_MESSAGE);
            return false;
        } else {
            try {
                if (checkbookCheck.isSelected()) {
                    checkNum = Integer.parseInt(checkBookNumber.getValue().toString());
                    String uniqueCheckNumberQuery = "select count(*) as Count from AccountingCBTable where CheckNumber = " + checkNum;
                    ResultSet rsUniqueCheckNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniqueCheckNumberQuery);
                    while (rsUniqueCheckNumber.next()) {
                        int count = rsUniqueCheckNumber.getInt("Count");
                        if (count > 0) {
                            dms.DMSApp.displayMessage(this, "The check# " + checkNum + " already exists ", dms.DMSApp.WARNING_MESSAGE);
                            return false;
                        }
                    }
                } else {
                    checkNum = Integer.parseInt(checkBookNumber.getValue().toString());
                    String uniqueCheckNumberQuery = "select count(*) as Count from AccountingCBTable where AchCheckNumber = " + checkNum;
                    ResultSet rsUniqueCheckNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniqueCheckNumberQuery);
                    while (rsUniqueCheckNumber.next()) {
                        int count = rsUniqueCheckNumber.getInt("Count");
                        if (count > 0) {
                            dms.DMSApp.displayMessage(this, "The check# " + checkNum + " already exists ", dms.DMSApp.WARNING_MESSAGE);
                            return false;
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(BankingPanel.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }
        if (!checkbookCheck.isSelected() && !checkbookAch.isSelected()) {
            dms.DMSApp.displayMessage(this, "Please select either Check or Adjustment ", dms.DMSApp.WARNING_MESSAGE);
            return false;
        }
        if (!checkbookBankCombo.getSelectedItem().equals("")) {
            bankNumber = "" + checkbookBankCombo.getSelectedItem();
            bankNumber = bankNumber.split("-")[0];
        } else {
            good = false;
            dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
            return false;
        }


        if (!checkBookNumber.getText().isEmpty()) {
            checkNum = Integer.parseInt(checkBookNumber.getValue().toString());
        }

        if (checkbookPayto.getSelectedItem() == null || checkbookPayto.getSelectedItem().toString().isEmpty()) {
            dms.DMSApp.displayMessage(this, "Please select account to pay to", dms.DMSApp.WARNING_MESSAGE);
            return false;
        } else if (checkbookAmount.getText().isEmpty()) {
            dms.DMSApp.displayMessage(this, "The check amount is empty", dms.DMSApp.WARNING_MESSAGE);
            return false;
        } else {
            if (!checksTotalAccounts.getText().isEmpty()) {
                String checkbookAmountStringVal = checkbookAmount.getValue().toString();
                if (checkbookAmountStringVal.contains(",")) {
                    checkbookAmountStringVal = checkbookAmountStringVal.replaceAll(",", "");
                }

                String checkbookTotalAccountsStringVal = checksTotalAccounts.getValue().toString();
                if (checkbookTotalAccountsStringVal.contains(",")) {
                    checkbookTotalAccountsStringVal = checkbookTotalAccountsStringVal.replaceAll(",", "");
                }
                Double billAmountDoubleVal = Double.parseDouble(checkbookAmountStringVal);
                Double billTotalExpensesDoubleVal = Double.parseDouble(checkbookTotalAccountsStringVal);

                System.out.println("Amount: " + (billAmountDoubleVal.toString()));
                System.out.println("Lines: " + (billTotalExpensesDoubleVal.toString()));

                int retval = Double.compare(billAmountDoubleVal, billTotalExpensesDoubleVal);
                retval = 0;
                if (retval != 0) {
                    dms.DMSApp.displayMessage(this, "The check amount and accounts total are not the same", dms.DMSApp.WARNING_MESSAGE);
                    return false;
                }
            } else {
                dms.DMSApp.displayMessage(this, "The checkbook accounts total amount is empty", dms.DMSApp.WARNING_MESSAGE);
                return false;
            }
        }

        if (journalPostingRowCount > 0) {

            int accountColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Account");
            int controlColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Control #");

            for (int i = 0; i <= checkbookAccountsTable.getRowCount() - 1; i++) {
                String accountNumber;
                if (checkbookAccountsTable.getValueAt(i, accountColumn) != null) {
                    accountNumber = checkbookAccountsTable.getValueAt(i, accountColumn).toString().split("-")[0];
                } else {
                    dms.DMSApp.displayMessage(this, "Please select at least one account", dms.DMSApp.WARNING_MESSAGE);
                    return false;
                }

                Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                //System.out.println("isControlled : " + isControlled);
                if (isControlled && checkbookAccountsTable.getValueAt(i, controlColumn) == null) {
                    good = false;
                    dms.DMSApp.displayMessage(this,
                            "Please provide Control # value for Row." + ++i, JOptionPane.WARNING_MESSAGE);
                    return false;
                } else if (isControlled
                        && checkbookAccountsTable.getValueAt(i, controlColumn) != null
                        && checkbookAccountsTable.getValueAt(i, controlColumn).toString().isEmpty()) {
                    dms.DMSApp.displayMessage(this,
                            "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }

        } else {
            good = false;
        }

        if (checkTypeCombo.getSelectedItem() == "Vendor") {
            if (checkbookCheck.isSelected()) {
                checkPaidToType = "Check - Vendor";
                checkLinePaidToType = "Check Line - Vendor";
            } else {
                checkPaidToType = "ACH Check - Vendor";
                checkLinePaidToType = "ACH Check Line - Vendor";
            }
            payerId = AccountingUtil.getVendorID(checkbookPayto.getSelectedItem().toString()).toString();
        } else if (checkTypeCombo.getSelectedItem() == "Customer") {
            if (checkbookCheck.isSelected()) {
                checkPaidToType = "Check - Customer";
                checkLinePaidToType = "Check Line - Customer";
            } else {
                checkPaidToType = "ACH Check - Customer";
                checkLinePaidToType = "ACH Check Line - Customer";
            }
            System.out.println(">>>>>>>>> "+checkbookPayto.getSelectedItem().toString());
            payerId =
                    AccountingHelperToLoadCustomers.getCustomersIDsMap().get(checkbookPayto.getSelectedItem().toString()).toString();
            System.out.println("Payer Id ==================== " + payerId);
            if (payerId == null || payerId.equals("") || payerId.isEmpty()) {
                System.out.println("if chk..");
                payerId = AccountingHelperToLoadCustomerLastRecords.getCustomersIDsMap().get(checkbookPayto.getSelectedItem().toString()).toString();
            }
            System.out.println("payer id final --- " + payerId);
        } else if (checkTypeCombo.getSelectedItem() == "Employee") {
            if (checkbookCheck.isSelected()) {
                checkPaidToType = "Check - Employee";
                checkLinePaidToType = "Check Line - Employee";
            } else {
                checkPaidToType = "ACH Check - Employee";
                checkLinePaidToType = "ACH Check Line - Employee";
            }
            payerId = AccountingUtil.getEmployeeID(checkbookPayto.getSelectedItem().toString()).toString();
        }

        try {
            if (good) {
                sql = new String[4];
                String class_checkbook = "";
                if (checkbookCheck.isSelected()) {
                    // Checkbook Table: enter check information in the checkbook table
                    sql[0] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, CheckNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Status, Type) "
                            + "VALUES ("
                            + "'" + checkbookEntryID.getText() + "', '" + checkNum + "', '" + bankNumber + "', "
                            + "'0', '" + checkbookPayto.getSelectedItem().toString() + "', "
                            + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + checkbookAmount.getText() + "', "
                            + "'" + checkbookMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', 'Posted', "
                            + "'" + checkPaidToType + "' "
                            + ") ";
                    class_checkbook = billClass_checkbook.getSelectedItem().toString();

                    sql[1] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                            + "VALUES ("
                            + "'" + bankNumber + "','0.00', '" + checkbookAmount.getText() + "', "
                            + "'" + payerId + "', '" + checkBookNumber.getText() + "', "
                            + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + checkPaidToType + "', "
                            + "'" + checkbookMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' , '" + class_checkbook + "' "
                            + ")";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                } else if (checkbookAch.isSelected()) {
                    sql[0] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, CheckNumber, AchCheckNumber, BankAccount, GLAccount, PaidTo, Date, Amount, Memo, LotName, Status, Type) "
                            + "VALUES ("
                            + "'" + checkbookEntryID.getText() + "', NULL , '" + checkNum + "', '" + bankNumber + "', "
                            + "'0', '" + checkbookPayto.getSelectedItem().toString() + "', "
                            + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + checkbookAmount.getText() + "', "
                            + "'" + checkbookMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', 'Posted', "
                            + "'" + checkPaidToType + "' "
                            + ") ";

                    sql[1] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, GLType, Memo, LotName, Class) "
                            + "VALUES ("
                            + "'" + bankNumber + "','0.00', '" + checkbookAmount.getText() + "', "
                            + "'" + payerId + "', '" + checkBookNumber.getText() + "', "
                            + "'" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '" + checkPaidToType + "', "
                            + "'" + checkbookMemo.getText() + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "' , '" + class_checkbook + "' "
                            + ")";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
                }

                String[] sql1 = new String[1];

                String accountNumber;
                Double checkAmount;
                String controlNumber;
                String referenceNumber;
                String memo;

                int accountColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Account");
                int amountColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Amount");
                int controlColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Control #");
                int referenceColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Reference #");
                int memoColumn = AccountingUtil.getColumnByName(checkbookAccountsTable, "Memo");

                for (int i = 0; i <= checkbookAccountsTable.getRowCount() - 1; i++) {
                    accountNumber = null;
                    if (checkbookAccountsTable.getValueAt(i, accountColumn) != null) {
                        accountNumber = checkbookAccountsTable.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.split("-")[0];
                        //System.out.println("accountNumber :: " + accountNumber);
                        Boolean isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                        //System.out.println("isControlled : " + isControlled);
                        if (isControlled && checkbookAccountsTable.getValueAt(i, controlColumn).toString().isEmpty()) {
                            dms.DMSApp.displayMessage(this, "Please provide Control # value for Row " + ++i, JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                    }
                    Object amountVal = checkbookAccountsTable.getValueAt(i, amountColumn);
                    if (amountVal != null && !amountVal.toString().isEmpty()) {
                        if (amountVal.toString().contains(",")) {
                            amountVal = amountVal.toString().replaceAll(",", "");
                        }
                        checkAmount = Double.parseDouble(amountVal.toString());
                    } else {
                        checkAmount = 0.00;
                    }
                    if (checkbookAccountsTable.getValueAt(i, controlColumn) != null) {
                        controlNumber = checkbookAccountsTable.getValueAt(i, controlColumn).toString();
                    } else {
                        controlNumber = "";
                    }
                    if (checkbookAccountsTable.getValueAt(i, referenceColumn) != null) {
                        referenceNumber = checkbookAccountsTable.getValueAt(i, referenceColumn).toString();
                    } else {
                        referenceNumber = "";
                    }
                    if (checkbookAccountsTable.getValueAt(i, memoColumn) != null) {
                        memo = checkbookAccountsTable.getValueAt(i, memoColumn).toString();
                    } else {
                        memo = "";
                    }
                    String checkType = null;
                    if (checkbookCheck.isSelected()) {
                        checkType = "Check";
                    } else if (checkbookAch.isSelected()) {
                        checkType = "Adjustment";
                    }

                    sql1[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, "
                            + "ReferenceNumber, PostDate, Memo, GLType, LotName,Class ) VALUES("
                            + "'" + accountNumber + "', '" + checkAmount + "','0.00', '" + controlNumber + "', '"
                            + referenceNumber + "', '" + AccountingUtil.dateFormat.format(checkbookDate.getDate()) + "', '"
                            + memo + "', '" + checkLinePaidToType + "', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + class_checkbook + "' "
                            + ")";
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql1, this);
                }
                reloadChecks();
                clearCheck();
                dms.DMSApp.displayMessage(this, "Check saved successfully", dms.DMSApp.INFORMATION_MESSAGE);
                if (checkbookAch.isSelected()) {
                    checkbookAchMouseClicked(null);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
            dms.DMSApp.displayMessage(this, e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
        }
        return true;
    }

    private boolean editCheckForVendorOrCustomerOrEmployee() {
        if (checksBottomTable.getSelectedRowCount() == 0) {
            dms.DMSApp.displayMessage(editCheckDialog, "Please select a record to edit", JOptionPane.WARNING_MESSAGE);
        } else {
            String paidToFromTable = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                    checksBottomTable.getColumnModel().getColumnIndex("Paid To")).toString();
            String type = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                        checksBottomTable.getColumnModel().getColumnIndex("Type")).toString();
            populateCheckbookPayToComboAndSelectPaidToItem(checkbookPayto1, paidToFromTable, type);
            try {
                int checkNum = Integer.parseInt(checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                        checksBottomTable.getColumnModel().getColumnIndex("Check #")).toString());
                setDepositNo(checkNum);
                int bankAccount = 0;
                int chkNumber = 0;
                int achCheckNumber = 0;
                String chkType = "";
                String query = "Select BankAccount,Date,Amount,Memo,CheckNumber,AchCheckNumber,Type "
                        + "from AccountingCBTable "
                        + "Where CheckNumber = '" + checkNum + "' ";

                ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
                if (rs.next()) {
                    checkDate1.setDate(AccountingUtil.getUtilDateFormatFromSqlDate(rs.getDate("Date")));
                    chkNumber = rs.getInt("CheckNumber");
                    achCheckNumber = rs.getInt("AchCheckNumber");
                    checkMemo1.setText(rs.getString("Memo"));
                    bankAccount = rs.getInt("BankAccount");
                    chkType = rs.getString("Type");
                }
                if (bankAccount == 0) {
                    checkCombo1.setSelectedItem("0 -");
                } else if (bankAccount == 202) {
                    checkCombo1.setSelectedItem("202 - Cash in Bank-BOA");
                } else if (bankAccount == 203) {
                    checkCombo1.setSelectedItem("203 - Cash on hand");
                }
                loadEndingBalanceForEditCheckDialog();

                // chk for checkNumber [Ach/normal]
                if (chkType.startsWith("Check")) {
                    checkNumber1.setText(String.valueOf(chkNumber));
                } else {
                    checkNumber1.setText(String.valueOf(achCheckNumber));
                    checkNumber1.setEditable(false);
                }
                depositsDeposit2.setSelected(true);

                // Load the bottom table
                int accountNumber = 0;
                String sqlQuery = "SELECT A.AccountNumber AS AccountNo, "
                        + "CAST(ROUND(A.Debit,2) AS NUMERIC(10,2)) AS Amount, A.ControlNumber, A.ReferenceNumber, A.Memo, "
                        + "A.GLType, A.Class "
                        + "From AccountingGLTable A "
                        + "LEFT JOIN AccountingCBTable B ON A.ReferenceNumber = B.DepositNumber "
                        + "WHERE A.ReferenceNumber = '" + checkNum + "' AND GLType Like 'Check Line%' ";

                ResultSet rsAccountDetails;
                rsAccountDetails = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);

                DefaultTableModel aModel = (DefaultTableModel) checkAccountsTable1.getModel();
                AccountingUtil.clearTableModel(aModel);

                ResultSetMetaData rsmd = rsAccountDetails.getMetaData();
                int colNo = rsmd.getColumnCount();

                double totalAmount = 0.0;
                while (rsAccountDetails.next()) {
                    Object[] values = new Object[colNo];
                    for (int i = 0; i < colNo; i++) {
                        accountNumber = rsAccountDetails.getInt("AccountNo");
                        gLTypeForEditCheck = rsAccountDetails.getString("GLType");
                        accountClass = rsAccountDetails.getString("Class");
                        if (i == 0) {
                            values[i] = AccountingUtil.getAccountFull(accountNumber);
                        } else if (i == 1) {
                            values[i] = AccountingUtil.formatAmountForDisplay(
                                    rsAccountDetails.getObject(i + 1).toString());
                        } else {
                            values[i] = rsAccountDetails.getObject(i + 1);
                        }
                    }
                    accountsToBeShownList.add(AccountingUtil.getAccountFull(accountNumber));
                    double amountVal = rsAccountDetails.getDouble("Amount");
                    totalAmount += amountVal;

                    aModel.addRow(values);
                }
                checkAccount1.setText(AccountingUtil.formatAmountForDisplay(String.valueOf(totalAmount)));
                checkAmount1.setText(String.valueOf(totalAmount));
                rsAccountDetails.getStatement().close();
                dms.DMSApp.getApplication().show(editCheckDialog);
            } catch (Exception e) {
                Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, e);
            }
            checkbookPayto1.removeAllItems();
            // set Class
            checkClass1.setSelectedItem(accountClass);
        }
        return true;
    }

    private void populateCheckbookPayToComboAndSelectPaidToItem(JComboBox checkbookPayto1,String paidToFromTable, String type) {
        if (type.endsWith("Vendor")) {
            checkTypeCombo1.setSelectedItem("Vendor");
            vendorNames = AccountingHelper.getAllVendors();
            System.out.println("Number of vendors : "+vendorNames.size());
            for (int i = 0; i < vendorNames.size(); i++) {
                checkbookPayto1.addItem(vendorNames.get(i));
            }
        } else if (type.endsWith("Customer")) {
            checkTypeCombo1.setSelectedItem("Customer");
            customerNames = AccountingHelperToLoadCustomers.getAllCustomers();
            for (int i = 0; i < customerNames.size(); i++) {
                checkbookPayto1.addItem(customerNames.get(i));
            }
            customerNames1 = AccountingHelperToLoadCustomerLastRecords.getAllCustomers();
            for (int i = 0; i < customerNames1.size(); i++) {
                checkbookPayto1.addItem(customerNames1.get(i));
            }
        } else if (type.endsWith("Employee")) {
            checkTypeCombo1.setSelectedItem("Employee");
            employeeNames = AccountingHelper.getAllEmployees();
            for (int i = 0; i < employeeNames.size(); i++) {
                checkbookPayto1.addItem(employeeNames.get(i));
            }
        }
        System.out.println("paidToFromTable : " + paidToFromTable);
        checkbookPayto1.setSelectedItem(paidToFromTable);


    }

    private void editBillPaymentCheck() {
        if (checksBottomTable.getSelectedRowCount() == 0) {
            dms.DMSApp.displayMessage(checksBottomTable, "Please select at least 1 paid bill to edit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int selectedRow = checksBottomTable.getSelectedRow();
            Object checkColVal = checksBottomTable.getValueAt(selectedRow, checksBottomTable.getColumnModel().getColumnIndex("Check #"));
            if (checkColVal == null) {
                checkColVal = "";
            }
            String checkNumberFromSelectedRow = checkColVal.toString();
            String paidToFromTable = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                    checksBottomTable.getColumnModel().getColumnIndex("Paid To")).toString();
            String type = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                        checksBottomTable.getColumnModel().getColumnIndex("Type")).toString();
            populateCheckbookPayToComboAndSelectPaidToItem(checkbookPayto1, paidToFromTable, type);

            Calendar c = Calendar.getInstance();
            if (checkDate1.getDate() == null) {
                checkDate1.setDate(c.getTime());
            }

            int bankAccount = Integer.valueOf(checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                    checksBottomTable.getColumnModel().getColumnIndex("Bank")).toString().split("-")[0].trim());
            String memo = checksBottomTable.getValueAt(checksBottomTable.getSelectedRow(),
                    checksBottomTable.getColumnModel().getColumnIndex("Memo")).toString();
            checkMemo1.setText(memo);

            if (bankAccount == 202) {
                checkCombo1.setSelectedItem("202 - Cash in Bank-BOA");
            } else {
                checkCombo1.setSelectedItem("203 - Cash on hand");
            }

            DefaultTableModel tableModel = (DefaultTableModel) checkAccountsTable1.getModel();
            AccountingUtil.clearTableModel(tableModel);
            checkNumber1.setText(checkNumberFromSelectedRow);

            if (checkNumberFromSelectedRow != null && !checkNumberFromSelectedRow.isEmpty()) {
                fetchAllInvoicesForSelectedCheck(checkNumberFromSelectedRow, checkAccountsTable1, checkAmount1,
                        editCheckDialog, tableModel, checkClass1, checkAccount1);
            } else {
                checkAmount1.setValue(Double.parseDouble(checksBottomTable.getValueAt(selectedRow,
                        checksBottomTable.getColumnModel().getColumnIndex("Amount")).toString()));
                editCheckDialog.setVisible(true);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fetchAllInvoicesForSelectedCheck(String checkNumberFromSelectedRow, JTable table,
            JFormattedTextField amountField, JDialog targetDialog, DefaultTableModel tableModel,
            JComboBox classValueCombo, JFormattedTextField accountField) {
        String query = "SELECT EntryID from AccountingCBTable WHERE CheckNumber = '" + checkNumberFromSelectedRow + "'";
        setTotalAmountForMultipleInvoices(0.0);
        try {
            ResultSet resultSet = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            if (resultSet.next()) {
                String invoiceNum = resultSet.getString("EntryID");
                if (invoiceNum == null || invoiceNum.isEmpty()) {
                    System.out.println("No invoice found for selected check");
                    return;
                }
                System.out.println("InvoiceNum : " + invoiceNum);
                if (invoiceNum.contains(",")) {
                    String[] invoiceNumArr = invoiceNum.split(",");
                    for (int i = 0; i < invoiceNumArr.length; i++) {
                        String currentInvoiceNum = invoiceNumArr[i].trim();
                        fecthInvoiceForSingleEntryId(currentInvoiceNum, tableModel, table, classValueCombo);
                    }
                } else {
                    fecthInvoiceForSingleEntryId(invoiceNum, tableModel, table, classValueCombo);
                }
            }
            System.out.println("Addition of Invoice lines complete.");
            amountField.setValue(getTotalAmountForMultipleInvoices());
            accountField.setValue(getTotalAmountForMultipleInvoices());
            targetDialog.setVisible(true);
            setTotalAmountForMultipleInvoices(0.0);
        } catch (SQLException ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fecthInvoiceForSingleEntryId(String invoiceNum, DefaultTableModel tableModel, JTable table,
            JComboBox classValueCombo) {
        try {
            int accountNumber = 0;
            String sqlQuery = "SELECT A.AccountNumber AS AccountNo, "
                    + "CAST(ROUND(A.Debit,2) AS NUMERIC(10,2)) AS Amount, A.ControlNumber, A.ReferenceNumber, A.Memo, "
                    + "A.GLType, A.Class "
                    + "From AccountingGLTable A "
                    + "LEFT JOIN AccountingCBTable B ON A.ReferenceNumber = B.DepositNumber "
                    + "WHERE A.ReferenceNumber IN ('" + invoiceNum + "') AND GLType Like 'Bill Line%' ";

            ResultSet resultSet = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);

            ResultSetMetaData rsmd = resultSet.getMetaData();
            int colNo = rsmd.getColumnCount();

            String classFromDB = "";
            System.out.println("Adding line for invoice : " + invoiceNum);

            while (resultSet.next()) {
                Object[] values = new Object[colNo];
                classFromDB = resultSet.getString("Class");
                for (int i = 0; i < colNo; i++) {
                    accountNumber = resultSet.getInt("AccountNo");
                    gLTypeForEditCheck = resultSet.getString("GLType");
                    accountClass = resultSet.getString("Class");
                    if (i == 0) {
                        values[i] = AccountingUtil.getAccountFull(accountNumber);
                    } else if (i == 1) {
                        values[i] = AccountingUtil.formatAmountForDisplay(
                                resultSet.getObject(i + 1).toString());
                    } else {
                        values[i] = resultSet.getObject(i + 1);
                    }
                }
                Double currentInvoiceAmount = resultSet.getDouble("Amount");
                setTotalAmountForMultipleInvoices(currentInvoiceAmount + getTotalAmountForMultipleInvoices());
                tableModel.addRow(values);
            }
            resultSet.getStatement().close();
            classValueCombo.setSelectedItem(classFromDB.toString());
        } catch (SQLException ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
