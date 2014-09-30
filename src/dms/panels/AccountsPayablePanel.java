/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.panels;

import dms.util.AccountingUtil;
import dms.util.EnglishNumberToWords;
import dms.util.TableCellListener;
import static dms.windows.AccountingWindow.displayNumeric;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

/**
 *
 * @author Lester
 */
public class AccountsPayablePanel extends javax.swing.JPanel {

    private static AccountsPayablePanel instance = null;
    String account = null;
    String amount = null;
    int enterCounter = 0;
    int rowNo = 0;
    boolean isRowRemoved = false;
    double sumForDisplay = 0.00;
    double amountSumValue = 0.00;
    String vendorName = "";
    private List<Object> accountsToBeShownList = new ArrayList<Object>();
    int numberOfMatches = 0;
    int vendorId = 0;
    String invoiceNo = "";
    boolean createBillClicked = false;
    private static BankingPanel bankingPanel = BankingPanel.getInstance();
    int chkNo = 0;
    private Double TotalAmountForMultipleInvoices;

    public Double getTotalAmountForMultipleInvoices() {
        return TotalAmountForMultipleInvoices;
    }

    public void setTotalAmountForMultipleInvoices(Double TotalAmountForMultipleInvoices) {
        this.TotalAmountForMultipleInvoices = TotalAmountForMultipleInvoices;
    }

    public static AccountsPayablePanel getInstance() {
        if (instance == null) {
            instance = new AccountsPayablePanel();
        }
        return instance;
    }

    /**
     * Creates new form AccountsPayablePanel
     */
    protected AccountsPayablePanel() {
        initComponents();


        AccountingUtil.dateFilter(jPanel228, jCheckBox11);
        jPanel96.setVisible(false);

        // Dates
        Calendar c = Calendar.getInstance();
        if (billPostDate.getDate() == null) {
            billPostDate.setDate(c.getTime());
        }
        if (billDueDate.getDate() == null) {
            billDueDate.setDate(c.getTime());
        }

        // Defaults
        billAmount.setText("0.00");
        billTotalExpenses.setText("0.00");
        billLinesDiff.setText("0.00");

        //addComboBoxesToTable();
        Action action1;
        action1 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                int rowCount = billExpensesTable.getRowCount();
                int modifiedRowNo = 0;
                String amountForDisplay = null;
                TableCellListener tcl1 = (TableCellListener) e.getSource();
                modifiedRowNo = tcl1.getRow();
                for (int i = 0; i < rowCount; i++) {
                    if (billExpensesTable.getValueAt(i, 1) != null && !billExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(billExpensesTable.getValueAt(i, 1).toString())) {
                            amount = billExpensesTable.getValueAt(i, 1).toString();
                            dms.DMSApp.displayMessage(billExpensesTable, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                            billExpensesTable.setCellSelectionEnabled(true);
                            billExpensesTable.changeSelection(modifiedRowNo, billExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                            billExpensesTable.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(billExpensesTable.getValueAt(i, 1).toString())) {
                            if (billExpensesTable.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(billExpensesTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(billExpensesTable.getValueAt(i, 1).toString());
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(billExpensesTable.getValueAt(modifiedRowNo, 1).toString());
                                billExpensesTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                } // end for loop

                Double diff = 0.00;
                if (billAmount.getValue() != null) {
                    diff = sum - Double.parseDouble(billAmount.getValue().toString());
                }
                if (billAmount.getValue() == null || billAmount.getText().equals("0.00")) {
                    diff = sum;
                }
                setAmountSum(sum);
                billTotalExpenses.setValue(sum);

                if (diff < 0.00) {
                    billLinesDiff.setForeground(Color.red);
                } else if (diff > 0.00) {
                    billLinesDiff.setForeground(Color.green);
                } else {
                    billLinesDiff.setForeground(Color.black);
                }

                billLinesDiff.setValue(diff);
            }
        };
        TableCellListener tcl1 = new TableCellListener(billExpensesTable, action1);

        Action action2 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ListSelectionModel selectionModel =
                        bottomBillsTable.getSelectionModel();
                int count = 0;
                for (int i = 0; i < bottomBillsTable.getRowCount(); i++) {
                    if (bottomBillsTable.getValueAt(i, 0) == null) {
                    } else if (bottomBillsTable.getValueAt(i, 0).toString().equalsIgnoreCase("true")) {
                        if (count == 0) {
                            selectionModel.setSelectionInterval(i, i);//for first row selection
                        } else {
                            if (!billsAllVendorsCheckbox.isSelected()) {
                                selectionModel.addSelectionInterval(i, i);
                            } else {
                                selectionModel.setSelectionInterval(i, i);
                                bottomBillsTable.setValueAt(false, i, i);
                            }
                        }
                        count++;
                    } else if (bottomBillsTable.getValueAt(i, 0).toString().equalsIgnoreCase("false")) {
                    }
                }
            }
        };

        TableCellListener tcl2 = new TableCellListener(bottomBillsTable, action2);

        // Edit Bill Case
        Action action3 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                TableCellListener tcl3 = (TableCellListener) e.getSource();
                int rowCount = editBillExpensesTable.getRowCount();
                int modifiedRowNo = 0;
                String amountForDisplay = "";
                modifiedRowNo = tcl3.getRow();
                for (int i = 0; i < rowCount; i++) {
                    if (editBillExpensesTable.getValueAt(i, 1) != null && !editBillExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(editBillExpensesTable.getValueAt(i, 1).toString())) {
                            dms.DMSApp.displayMessage(editBillExpensesTable, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                            editBillExpensesTable.setCellSelectionEnabled(true);
                            editBillExpensesTable.changeSelection(modifiedRowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                            editBillExpensesTable.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(editBillExpensesTable.getValueAt(i, 1).toString())) {
                            if (editBillExpensesTable.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(editBillExpensesTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(editBillExpensesTable.getValueAt(i, 1).toString());
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(editBillExpensesTable.getValueAt(modifiedRowNo, 1).toString());
                                editBillExpensesTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                }
                Double diff = 0.00;
                if (editBillAmount.getText().toString().contains(",")) {
                    diff = Double.parseDouble(editBillAmount.getText().toString().replace(",", "")) - sum;
                } else {
                    diff = Double.parseDouble(editBillAmount.getText().toString()) - sum;
                }

                editBillTotalExpenses.setValue(sum);
                editBillLinesDiff.setValue(diff);
            }
        };

        TableCellListener tcl3 = new TableCellListener(editBillExpensesTable, action3);

        // Edit Credit Case
        Action action4 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                TableCellListener tcl4 = (TableCellListener) e.getSource();
                int rowCount = editBillExpensesTable1.getRowCount();
                int modifiedRowNo = 0;
                String amountForDisplay = "";
                modifiedRowNo = tcl4.getRow();
                for (int i = 0; i < rowCount; i++) {
                    if (editBillExpensesTable1.getValueAt(i, 1) != null && !editBillExpensesTable1.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(editBillExpensesTable1.getValueAt(i, 1).toString())) {
                            dms.DMSApp.displayMessage(editBillExpensesTable1, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                            editBillExpensesTable1.setCellSelectionEnabled(true);
                            editBillExpensesTable1.changeSelection(modifiedRowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                            editBillExpensesTable1.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(editBillExpensesTable1.getValueAt(i, 1).toString())) {
                            if (editBillExpensesTable1.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(editBillExpensesTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(editBillExpensesTable1.getValueAt(i, 1).toString());
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(editBillExpensesTable1.getValueAt(modifiedRowNo, 1).toString());
                                editBillExpensesTable1.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                }
                Double diff = 0.00;
                if (editBillAmount1.getText().toString().contains(",")) {
                    diff = Double.parseDouble(editBillAmount1.getText().toString().replace(",", "")) - sum;
                } else {
                    diff = Double.parseDouble(editBillAmount1.getText().toString()) - sum;
                }

                editBillTotalExpenses1.setValue(sum);
                editBillLinesDiff1.setValue(diff);
            }
        };

        TableCellListener tcl4 = new TableCellListener(editBillExpensesTable1, action4);
        reloadVendorsList();
        addNewRow((DefaultTableModel) billExpensesTable.getModel(), AccountingUtil.getAllAccountsFull()[0].toString());
        addComboBoxesToTableForBillExpensesTable();
        //billExpensesTable.repaint();
        //billExpensesTable.revalidate();
        //billExpensesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                paybillsPopup = new javax.swing.JDialog();
                paybillsCheckbookPanel = new javax.swing.JPanel();
                jPanel160 = new javax.swing.JPanel();
                jPanel163 = new javax.swing.JPanel();
                jPanel164 = new javax.swing.JPanel();
                jPanel190 = new javax.swing.JPanel();
                jPanel191 = new javax.swing.JPanel();
                paybillsAmount = new javax.swing.JFormattedTextField();
                paybillsPayto = new com.jidesoft.swing.AutoCompletionComboBox();
                paybillsMemo = new javax.swing.JTextField();
                paybillsClass = new com.jidesoft.swing.AutoCompletionComboBox();
                jLabel2 = new javax.swing.JLabel();
                jLabel1 = new javax.swing.JLabel();
                balancePanel4 = new javax.swing.JPanel();
                jPanel192 = new javax.swing.JPanel();
                paybillsBankCombo = new javax.swing.JComboBox();
                jLabel168 = new javax.swing.JLabel();
                jPanel193 = new javax.swing.JPanel();
                jLabel169 = new javax.swing.JLabel();
                paybillsEndingBalance = new javax.swing.JFormattedTextField();
                paybillsCheckNumber = new javax.swing.JFormattedTextField();
                jLabel163 = new javax.swing.JLabel();
                paybillsDate = new com.toedter.calendar.JDateChooser();
                jPanel165 = new javax.swing.JPanel();
                paybillsEntryID = new javax.swing.JLabel();
                paybillsEntryId = new javax.swing.JLabel();
                jLabel27 = new javax.swing.JLabel();
                paybillsCancelButton = new javax.swing.JButton();
                paybillsPayandPrint = new javax.swing.JButton();
                paybillsPayButton = new javax.swing.JButton();
                jPanel167 = new javax.swing.JPanel();
                jScrollPane33 = new javax.swing.JScrollPane();
                paybillsDueTable1 = new javax.swing.JTable();
                editBillsPopup = new javax.swing.JDialog();
                jPanel1 = new javax.swing.JPanel();
                enterBillsPanel4 = new javax.swing.JPanel();
                jPanel233 = new javax.swing.JPanel();
                jPanel234 = new javax.swing.JPanel();
                jLabel143 = new javax.swing.JLabel();
                jLabel144 = new javax.swing.JLabel();
                jLabel145 = new javax.swing.JLabel();
                jLabel151 = new javax.swing.JLabel();
                jPanel235 = new javax.swing.JPanel();
                editBillVendor = new com.jidesoft.swing.AutoCompletionComboBox();
                editBillInvoice = new javax.swing.JTextField();
                editBillMemo = new javax.swing.JTextField();
                editClass = new com.jidesoft.swing.AutoCompletionComboBox();
                jPanel236 = new javax.swing.JPanel();
                jPanel237 = new javax.swing.JPanel();
                editBillPostDate = new com.toedter.calendar.JDateChooser();
                editBillDueDate = new com.toedter.calendar.JDateChooser();
                editBillAmount = new javax.swing.JFormattedTextField();
                jPanel65 = new javax.swing.JPanel();
                editBillTotalExpenses = new javax.swing.JFormattedTextField();
                editBillLinesDiff = new javax.swing.JFormattedTextField();
                jPanel238 = new javax.swing.JPanel();
                jLabel146 = new javax.swing.JLabel();
                jLabel147 = new javax.swing.JLabel();
                jLabel148 = new javax.swing.JLabel();
                jLabel149 = new javax.swing.JLabel();
                jPanel239 = new javax.swing.JPanel();
                billsCreateButton1 = new javax.swing.JButton();
                billsClearButton1 = new javax.swing.JButton();
                billsAddLineButton1 = new javax.swing.JButton();
                billsDeleteLineButton1 = new javax.swing.JButton();
                jPanel196 = new javax.swing.JPanel();
                entryIdLabel3 = new javax.swing.JLabel();
                billsVendorId1 = new javax.swing.JLabel();
                jPanel240 = new javax.swing.JPanel();
                jScrollPane2 = new javax.swing.JScrollPane();
                editBillExpensesTable = new javax.swing.JTable();
                editBillExpensesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                buttonGroup1 = new javax.swing.ButtonGroup();
                buttonGroup2 = new javax.swing.ButtonGroup();
                editPaidBillsPopup = new javax.swing.JDialog();
                paybillsCheckbookPanel1 = new javax.swing.JPanel();
                jPanel161 = new javax.swing.JPanel();
                jPanel166 = new javax.swing.JPanel();
                jPanel168 = new javax.swing.JPanel();
                jPanel197 = new javax.swing.JPanel();
                jPanel198 = new javax.swing.JPanel();
                paidbillsAmount = new javax.swing.JFormattedTextField();
                paidbillsPayto = new com.jidesoft.swing.AutoCompletionComboBox();
                paidbillsMemo = new javax.swing.JTextField();
                paidbillsClass = new com.jidesoft.swing.AutoCompletionComboBox();
                jLabel3 = new javax.swing.JLabel();
                jLabel4 = new javax.swing.JLabel();
                jLabel5 = new javax.swing.JLabel();
                balancePanel5 = new javax.swing.JPanel();
                jPanel199 = new javax.swing.JPanel();
                paidbillsBankCombo = new javax.swing.JComboBox();
                jLabel170 = new javax.swing.JLabel();
                jPanel200 = new javax.swing.JPanel();
                jLabel171 = new javax.swing.JLabel();
                paidbillsEndingBalance = new javax.swing.JFormattedTextField();
                paidbillsNumber = new javax.swing.JFormattedTextField();
                jLabel164 = new javax.swing.JLabel();
                paidbillsDate = new com.toedter.calendar.JDateChooser();
                jPanel169 = new javax.swing.JPanel();
                paybillsEntryID1 = new javax.swing.JLabel();
                paybillsEntryId1 = new javax.swing.JLabel();
                jLabel30 = new javax.swing.JLabel();
                paybillsCancelButton1 = new javax.swing.JButton();
                paidbillsUpdateAndPrintButton = new javax.swing.JButton();
                paidbillsUpdateButton = new javax.swing.JButton();
                jPanel170 = new javax.swing.JPanel();
                jScrollPane35 = new javax.swing.JScrollPane();
                paidbillsDueTable = new javax.swing.JTable();
                jToggleButton1 = new javax.swing.JToggleButton();
                editCreditsPopup = new javax.swing.JDialog();
                jPanel5 = new javax.swing.JPanel();
                enterBillsPanel5 = new javax.swing.JPanel();
                jPanel243 = new javax.swing.JPanel();
                jPanel244 = new javax.swing.JPanel();
                jLabel152 = new javax.swing.JLabel();
                jLabel153 = new javax.swing.JLabel();
                jLabel154 = new javax.swing.JLabel();
                jLabel155 = new javax.swing.JLabel();
                jPanel245 = new javax.swing.JPanel();
                editBillVendor1 = new com.jidesoft.swing.AutoCompletionComboBox();
                editBillInvoice1 = new javax.swing.JTextField();
                editBillMemo1 = new javax.swing.JTextField();
                editClass1 = new com.jidesoft.swing.AutoCompletionComboBox();
                jPanel246 = new javax.swing.JPanel();
                jPanel247 = new javax.swing.JPanel();
                editBillPostDate1 = new com.toedter.calendar.JDateChooser();
                editBillDueDate1 = new com.toedter.calendar.JDateChooser();
                editBillAmount1 = new javax.swing.JFormattedTextField();
                jPanel66 = new javax.swing.JPanel();
                editBillTotalExpenses1 = new javax.swing.JFormattedTextField();
                editBillLinesDiff1 = new javax.swing.JFormattedTextField();
                jPanel248 = new javax.swing.JPanel();
                jLabel156 = new javax.swing.JLabel();
                jLabel157 = new javax.swing.JLabel();
                jLabel158 = new javax.swing.JLabel();
                jLabel159 = new javax.swing.JLabel();
                jPanel249 = new javax.swing.JPanel();
                billsCreateButton2 = new javax.swing.JButton();
                billsClearButton2 = new javax.swing.JButton();
                billsAddLineButton2 = new javax.swing.JButton();
                billsDeleteLineButton2 = new javax.swing.JButton();
                jPanel201 = new javax.swing.JPanel();
                entryIdLabel4 = new javax.swing.JLabel();
                billsVendorId2 = new javax.swing.JLabel();
                jPanel250 = new javax.swing.JPanel();
                jScrollPane4 = new javax.swing.JScrollPane();
                editBillExpensesTable1 = new javax.swing.JTable();
                editBillExpensesTable1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                jPanel155 = new javax.swing.JPanel();
                jPanel156 = new javax.swing.JPanel();
                billsAllVendorsCheckbox = new javax.swing.JCheckBox();
                jPanel2 = new javax.swing.JPanel();
                jScrollPane34 = new javax.swing.JScrollPane();
                billsVendorsTable = new javax.swing.JTable();
                jPanel3 = new javax.swing.JPanel();
                txt_Search = new javax.swing.JTextField();
                SearchVendorButton = new javax.swing.JButton();
                jPanel157 = new javax.swing.JPanel();
                billsVendorInfo = new javax.swing.JPanel();
                jPanel185 = new javax.swing.JPanel();
                jPanel186 = new javax.swing.JPanel();
                jLabel35 = new javax.swing.JLabel();
                jLabel52 = new javax.swing.JLabel();
                jLabel116 = new javax.swing.JLabel();
                jPanel187 = new javax.swing.JPanel();
                billVendorName = new javax.swing.JTextField();
                billVendorAddress = new javax.swing.JTextField();
                billVendorAddress2 = new javax.swing.JTextField();
                jPanel188 = new javax.swing.JPanel();
                jPanel189 = new javax.swing.JPanel();
                billVendorConatct = new javax.swing.JTextField();
                billVendorEmail = new javax.swing.JTextField();
                billVendorPhone = new javax.swing.JTextField();
                jPanel194 = new javax.swing.JPanel();
                jLabel120 = new javax.swing.JLabel();
                jLabel118 = new javax.swing.JLabel();
                jLabel119 = new javax.swing.JLabel();
                jPanel209 = new javax.swing.JPanel();
                enterBillsPanel3 = new javax.swing.JPanel();
                jPanel218 = new javax.swing.JPanel();
                jPanel219 = new javax.swing.JPanel();
                filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
                jLabel142 = new javax.swing.JLabel();
                jLabel139 = new javax.swing.JLabel();
                jLabel141 = new javax.swing.JLabel();
                jLabel150 = new javax.swing.JLabel();
                jPanel220 = new javax.swing.JPanel();
                billCreditCheck = new javax.swing.JCheckBox();
                billVendor = new com.jidesoft.swing.AutoCompletionComboBox();
                billInvoice = new javax.swing.JTextField();
                billMemo = new javax.swing.JTextField();
                billClass = new com.jidesoft.swing.AutoCompletionComboBox();
                jPanel221 = new javax.swing.JPanel();
                jPanel222 = new javax.swing.JPanel();
                billPostDate = new com.toedter.calendar.JDateChooser();
                billDueDate = new com.toedter.calendar.JDateChooser();
                billAmount = new javax.swing.JFormattedTextField();
                jPanel64 = new javax.swing.JPanel();
                billTotalExpenses = new javax.swing.JFormattedTextField();
                billLinesDiff = new javax.swing.JFormattedTextField();
                jPanel223 = new javax.swing.JPanel();
                jLabel136 = new javax.swing.JLabel();
                jLabel137 = new javax.swing.JLabel();
                jLabel138 = new javax.swing.JLabel();
                jLabel140 = new javax.swing.JLabel();
                jPanel224 = new javax.swing.JPanel();
                billsCreateButton = new javax.swing.JButton();
                billsClearButton = new javax.swing.JButton();
                billsAddLineButton = new javax.swing.JButton();
                billsDeleteLineButton = new javax.swing.JButton();
                jPanel195 = new javax.swing.JPanel();
                entryIdLabel2 = new javax.swing.JLabel();
                billsVendorId = new javax.swing.JLabel();
                jPanel225 = new javax.swing.JPanel();
                jScrollPane1 = new javax.swing.JScrollPane();
                billExpensesTable = new javax.swing.JTable();
                jPanel118 = new javax.swing.JPanel();
                billsControlPanel3 = new javax.swing.JPanel();
                jPanel226 = new javax.swing.JPanel();
                billsDueRadioButton1 = new javax.swing.JRadioButton();
                billsPaidRadioButton1 = new javax.swing.JRadioButton();
                billsCreditRadioButton = new javax.swing.JRadioButton();
                billsSearchTextField = new javax.swing.JTextField();
                billsSearchButton = new javax.swing.JButton();
                jPanel227 = new javax.swing.JPanel();
                jCheckBox11 = new javax.swing.JCheckBox();
                jPanel228 = new javax.swing.JPanel();
                jRadioButton21 = new javax.swing.JRadioButton();
                jRadioButton22 = new javax.swing.JRadioButton();
                jRadioButton23 = new javax.swing.JRadioButton();
                jLabel134 = new javax.swing.JLabel();
                jDateChooser20 = new com.toedter.calendar.JDateChooser();
                jLabel135 = new javax.swing.JLabel();
                jDateChooser21 = new com.toedter.calendar.JDateChooser();
                jButton49 = new javax.swing.JButton();
                jPanel229 = new javax.swing.JPanel();
                apVendorBillDue = new javax.swing.JPanel();
                jPanel231 = new javax.swing.JPanel();
                payBill = new javax.swing.JButton();
                editBill = new javax.swing.JButton();
                deleteBill = new javax.swing.JButton();
                jLabel25 = new javax.swing.JLabel();
                selectedInvoices = new javax.swing.JLabel();
                jPanel96 = new javax.swing.JPanel();
                jLabel28 = new javax.swing.JLabel();
                amountInvoices = new javax.swing.JLabel();
                jScrollPane37 = new javax.swing.JScrollPane();
                bottomBillsTable = new javax.swing.JTable();
                apVendorBillCredit = new javax.swing.JPanel();
                jPanel242 = new javax.swing.JPanel();
                editBill1 = new javax.swing.JButton();
                deleteBill1 = new javax.swing.JButton();
                jLabel26 = new javax.swing.JLabel();
                selectedInvoices1 = new javax.swing.JLabel();
                jScrollPane3 = new javax.swing.JScrollPane();
                apVendorBillCreditTable = new javax.swing.JTable();
                apVendorBillPaid = new javax.swing.JPanel();
                jScrollPane27 = new javax.swing.JScrollPane();
                paidBillsTable = new javax.swing.JTable();
                jPanel4 = new javax.swing.JPanel();
                editPaidBillJButton = new javax.swing.JButton();
                deletePaidBillJButton = new javax.swing.JButton();

                paybillsPopup.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dms.DMSApp.class).getContext().getResourceMap(AccountsPayablePanel.class);
                paybillsPopup.setTitle(resourceMap.getString("paybillsPopup.title")); // NOI18N
                paybillsPopup.setLocationByPlatform(true);
                paybillsPopup.setMinimumSize(new java.awt.Dimension(972, 700));
                paybillsPopup.setName("paybillsPopup"); // NOI18N
                paybillsPopup.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

                paybillsCheckbookPanel.setBackground(resourceMap.getColor("paybillsCheckbookPanel.background")); // NOI18N
                paybillsCheckbookPanel.setName("paybillsCheckbookPanel"); // NOI18N
                paybillsCheckbookPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentShown(java.awt.event.ComponentEvent evt) {
                                paybillsCheckbookPanelComponentShown(evt);
                        }
                });
                paybillsCheckbookPanel.setLayout(new javax.swing.BoxLayout(paybillsCheckbookPanel, javax.swing.BoxLayout.Y_AXIS));

                jPanel160.setName("jPanel160"); // NOI18N
                jPanel160.setOpaque(false);
                jPanel160.setPreferredSize(new java.awt.Dimension(926, 330));
                jPanel160.setLayout(new javax.swing.BoxLayout(jPanel160, javax.swing.BoxLayout.LINE_AXIS));
                paybillsCheckbookPanel.add(jPanel160);

                jPanel163.setMinimumSize(new java.awt.Dimension(747, 350));
                jPanel163.setName("jPanel163"); // NOI18N
                jPanel163.setOpaque(false);
                jPanel163.setPreferredSize(new java.awt.Dimension(747, 350));
                jPanel163.setLayout(null);

                jPanel164.setMaximumSize(new java.awt.Dimension(330, 280));
                jPanel164.setMinimumSize(new java.awt.Dimension(330, 280));
                jPanel164.setName("jPanel164"); // NOI18N
                jPanel164.setOpaque(false);
                jPanel164.setPreferredSize(new java.awt.Dimension(840, 325));

                jPanel190.setMinimumSize(new java.awt.Dimension(767, 330));
                jPanel190.setName("jPanel190"); // NOI18N
                jPanel190.setOpaque(false);

                jPanel191.setName("jPanel191"); // NOI18N
                jPanel191.setOpaque(false);

                paybillsAmount.setEditable(false);
                paybillsAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                paybillsAmount.setMaximumSize(new java.awt.Dimension(81, 2147483647));
                paybillsAmount.setMinimumSize(new java.awt.Dimension(81, 20));
                paybillsAmount.setName("paybillsAmount"); // NOI18N
                paybillsAmount.setPreferredSize(new java.awt.Dimension(81, 20));
                paybillsAmount.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                paybillsAmountActionPerformed(evt);
                        }
                });

                paybillsPayto.setEnabled(false);
                paybillsPayto.setName("paybillsPayto"); // NOI18N
                paybillsPayto.setStrict(false);
                paybillsPayto.setStrictCompletion(false);

                paybillsMemo.setMaximumSize(new java.awt.Dimension(250, 2147483647));
                paybillsMemo.setMinimumSize(new java.awt.Dimension(250, 20));
                paybillsMemo.setName("paybillsMemo"); // NOI18N
                paybillsMemo.setPreferredSize(new java.awt.Dimension(250, 20));

                paybillsClass.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", " " }));
                paybillsClass.setName("paybillsClass"); // NOI18N
                paybillsClass.setStrict(false);
                paybillsClass.setStrictCompletion(false);
                paybillsClass.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                paybillsClassItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });

                jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
                jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
                jLabel2.setName("jLabel2"); // NOI18N

                jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
                jLabel1.setName("jLabel1"); // NOI18N

                javax.swing.GroupLayout jPanel191Layout = new javax.swing.GroupLayout(jPanel191);
                jPanel191.setLayout(jPanel191Layout);
                jPanel191Layout.setHorizontalGroup(
                        jPanel191Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel191Layout.createSequentialGroup()
                                .addGroup(jPanel191Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel191Layout.createSequentialGroup()
                                                .addGap(108, 108, 108)
                                                .addComponent(paybillsPayto, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel1)
                                                .addGap(18, 18, 18)
                                                .addComponent(paybillsAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel191Layout.createSequentialGroup()
                                                .addGroup(jPanel191Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel191Layout.createSequentialGroup()
                                                                .addGap(78, 78, 78)
                                                                .addComponent(paybillsMemo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel191Layout.createSequentialGroup()
                                                                .addGap(43, 43, 43)
                                                                .addComponent(jLabel2)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(paybillsClass, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                jPanel191Layout.setVerticalGroup(
                        jPanel191Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel191Layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(jPanel191Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paybillsAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(paybillsPayto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                                .addGroup(jPanel191Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paybillsClass, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2))
                                .addGap(18, 18, 18)
                                .addComponent(paybillsMemo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );

                balancePanel4.setName("balancePanel4"); // NOI18N
                balancePanel4.setOpaque(false);

                jPanel192.setName("jPanel192"); // NOI18N
                jPanel192.setOpaque(false);

                paybillsBankCombo.setMaximumSize(new java.awt.Dimension(154, 32767));
                paybillsBankCombo.setMinimumSize(new java.awt.Dimension(154, 20));
                paybillsBankCombo.setName("paybillsBankCombo"); // NOI18N
                paybillsBankCombo.setPreferredSize(new java.awt.Dimension(154, 20));
                paybillsBankCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                paybillsBankCombobankSelected(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                paybillsBankCombo.addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                paybillsBankComboItemStateChanged(evt);
                        }
                });

                jLabel168.setText("BANK ACCOUNT :"); // NOI18N
                jLabel168.setName("jLabel168"); // NOI18N

                javax.swing.GroupLayout jPanel192Layout = new javax.swing.GroupLayout(jPanel192);
                jPanel192.setLayout(jPanel192Layout);
                jPanel192Layout.setHorizontalGroup(
                        jPanel192Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel192Layout.createSequentialGroup()
                                .addContainerGap(26, Short.MAX_VALUE)
                                .addComponent(jLabel168)
                                .addGap(18, 18, 18)
                                .addComponent(paybillsBankCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );
                jPanel192Layout.setVerticalGroup(
                        jPanel192Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel192Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel168)
                                .addComponent(paybillsBankCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                jPanel193.setName("jPanel193"); // NOI18N
                jPanel193.setOpaque(false);

                jLabel169.setText("ENDING BALANCE :"); // NOI18N
                jLabel169.setName("jLabel169"); // NOI18N

                paybillsEndingBalance.setEditable(false);
                paybillsEndingBalance.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("¤#,##0.00"))));
                paybillsEndingBalance.setMaximumSize(new java.awt.Dimension(90, 2147483647));
                paybillsEndingBalance.setMinimumSize(new java.awt.Dimension(90, 20));
                paybillsEndingBalance.setName("paybillsEndingBalance"); // NOI18N
                paybillsEndingBalance.setPreferredSize(new java.awt.Dimension(90, 20));

                paybillsCheckNumber.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
                paybillsCheckNumber.setMaximumSize(new java.awt.Dimension(81, 2147483647));
                paybillsCheckNumber.setMinimumSize(new java.awt.Dimension(81, 20));
                paybillsCheckNumber.setName("paybillsCheckNumber"); // NOI18N
                paybillsCheckNumber.setPreferredSize(new java.awt.Dimension(81, 20));

                jLabel163.setText("CHECK NUMBER :"); // NOI18N
                jLabel163.setName("jLabel163"); // NOI18N

                javax.swing.GroupLayout jPanel193Layout = new javax.swing.GroupLayout(jPanel193);
                jPanel193.setLayout(jPanel193Layout);
                jPanel193Layout.setHorizontalGroup(
                        jPanel193Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel193Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel193Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel169, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel163, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel193Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(paybillsCheckNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(paybillsEndingBalance, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                                .addGap(37, 37, 37))
                );
                jPanel193Layout.setVerticalGroup(
                        jPanel193Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel193Layout.createSequentialGroup()
                                .addGroup(jPanel193Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paybillsEndingBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel169))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel193Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paybillsCheckNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel163)))
                );

                javax.swing.GroupLayout balancePanel4Layout = new javax.swing.GroupLayout(balancePanel4);
                balancePanel4.setLayout(balancePanel4Layout);
                balancePanel4Layout.setHorizontalGroup(
                        balancePanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, balancePanel4Layout.createSequentialGroup()
                                .addContainerGap(23, Short.MAX_VALUE)
                                .addComponent(jPanel192, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(199, 199, 199)
                                .addComponent(jPanel193, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );
                balancePanel4Layout.setVerticalGroup(
                        balancePanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(balancePanel4Layout.createSequentialGroup()
                                .addContainerGap(20, Short.MAX_VALUE)
                                .addGroup(balancePanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jPanel192, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel193, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );

                paybillsDate.setDateFormatString(resourceMap.getString("paybillsDate.dateFormatString")); // NOI18N
                paybillsDate.setMaximumSize(new java.awt.Dimension(99, 2147483647));
                paybillsDate.setMinimumSize(new java.awt.Dimension(99, 20));
                paybillsDate.setName("paybillsDate"); // NOI18N
                paybillsDate.setPreferredSize(new java.awt.Dimension(99, 20));

                javax.swing.GroupLayout jPanel190Layout = new javax.swing.GroupLayout(jPanel190);
                jPanel190.setLayout(jPanel190Layout);
                jPanel190Layout.setHorizontalGroup(
                        jPanel190Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel190Layout.createSequentialGroup()
                                .addGroup(jPanel190Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(paybillsDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel190Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(balancePanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jPanel191, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap(15, Short.MAX_VALUE))
                );
                jPanel190Layout.setVerticalGroup(
                        jPanel190Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel190Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(balancePanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(paybillsDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel191, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(37, 37, 37))
                );

                javax.swing.GroupLayout jPanel164Layout = new javax.swing.GroupLayout(jPanel164);
                jPanel164.setLayout(jPanel164Layout);
                jPanel164Layout.setHorizontalGroup(
                        jPanel164Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel164Layout.createSequentialGroup()
                                .addComponent(jPanel190, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel164Layout.setVerticalGroup(
                        jPanel164Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel164Layout.createSequentialGroup()
                                .addComponent(jPanel190, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46))
                );

                jPanel163.add(jPanel164);
                jPanel164.setBounds(0, 0, 769, 320);

                jPanel165.setName("jPanel165"); // NOI18N
                jPanel165.setOpaque(false);

                paybillsEntryID.setName("paybillsEntryID"); // NOI18N

                paybillsEntryId.setName("paybillsEntryId"); // NOI18N

                javax.swing.GroupLayout jPanel165Layout = new javax.swing.GroupLayout(jPanel165);
                jPanel165.setLayout(jPanel165Layout);
                jPanel165Layout.setHorizontalGroup(
                        jPanel165Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel165Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(paybillsEntryID)
                                .addContainerGap(413, Short.MAX_VALUE))
                        .addGroup(jPanel165Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel165Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(paybillsEntryId)
                                        .addContainerGap(413, Short.MAX_VALUE)))
                );
                jPanel165Layout.setVerticalGroup(
                        jPanel165Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel165Layout.createSequentialGroup()
                                .addContainerGap(287, Short.MAX_VALUE)
                                .addComponent(paybillsEntryID)
                                .addGap(21, 21, 21))
                        .addGroup(jPanel165Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel165Layout.createSequentialGroup()
                                        .addContainerGap(296, Short.MAX_VALUE)
                                        .addComponent(paybillsEntryId)
                                        .addGap(12, 12, 12)))
                );

                jPanel163.add(jPanel165);
                jPanel165.setBounds(1733, 11, 0, 308);

                jLabel27.setIcon(resourceMap.getIcon("jLabel27.icon")); // NOI18N
                jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
                jLabel27.setName("jLabel27"); // NOI18N
                jPanel163.add(jLabel27);
                jLabel27.setBounds(0, 0, 800, 340);

                paybillsCancelButton.setText(resourceMap.getString("paybillsCancelButton.text")); // NOI18N
                paybillsCancelButton.setName("paybillsCancelButton"); // NOI18N
                paybillsCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                paybillsCancelButton(evt);
                        }
                });
                jPanel163.add(paybillsCancelButton);
                paybillsCancelButton.setBounds(810, 70, 148, 23);

                paybillsPayandPrint.setText(resourceMap.getString("paybillsPayandPrint.text")); // NOI18N
                paybillsPayandPrint.setName("paybillsPayandPrint"); // NOI18N
                paybillsPayandPrint.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                paybillsPayandPrintsaveCheckButtonsClicked(evt);
                        }
                });
                jPanel163.add(paybillsPayandPrint);
                paybillsPayandPrint.setBounds(810, 40, 147, 23);

                paybillsPayButton.setText(resourceMap.getString("paybillsPayButton.text")); // NOI18N
                paybillsPayButton.setMaximumSize(new java.awt.Dimension(135, 23));
                paybillsPayButton.setMinimumSize(new java.awt.Dimension(135, 23));
                paybillsPayButton.setName("paybillsPayButton"); // NOI18N
                paybillsPayButton.setPreferredSize(new java.awt.Dimension(135, 23));
                paybillsPayButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                paybillsPayButton(evt);
                        }
                });
                jPanel163.add(paybillsPayButton);
                paybillsPayButton.setBounds(810, 10, 148, 23);

                paybillsCheckbookPanel.add(jPanel163);

                jPanel167.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel167.border.title"))); // NOI18N
                jPanel167.setMinimumSize(new java.awt.Dimension(400, 401));
                jPanel167.setName("jPanel167"); // NOI18N
                jPanel167.setOpaque(false);
                jPanel167.setPreferredSize(new java.awt.Dimension(400, 401));
                jPanel167.setLayout(new javax.swing.BoxLayout(jPanel167, javax.swing.BoxLayout.Y_AXIS));

                jScrollPane33.setName("jScrollPane33"); // NOI18N

                paybillsDueTable1.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Selected", "Invoice #", "ID", "Vendor Name", "Account", "Amount", "Control #", "Post Date", "Due Date", "Memo"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
                        };
                        boolean[] canEdit = new boolean [] {
                                true, false, false, false, false, false, true, true, false, false
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                paybillsDueTable1.setFillsViewportHeight(true);
                paybillsDueTable1.setName("paybillsDueTable1"); // NOI18N
                paybillsDueTable1.getTableHeader().setReorderingAllowed(false);
                jScrollPane33.setViewportView(paybillsDueTable1);
                paybillsDueTable1.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title5")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("paybillsDueTable.columnModel.title1")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(2).setMinWidth(0);
                paybillsDueTable1.getColumnModel().getColumn(2).setPreferredWidth(0);
                paybillsDueTable1.getColumnModel().getColumn(2).setMaxWidth(0);
                paybillsDueTable1.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("paybillsDueTable.columnModel.title2")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title4")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title6")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("paybillsDueTable.columnModel.title5")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title7")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title8")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(8).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title9")); // NOI18N
                paybillsDueTable1.getColumnModel().getColumn(9).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title3")); // NOI18N

                jPanel167.add(jScrollPane33);

                paybillsCheckbookPanel.add(jPanel167);

                paybillsPopup.getContentPane().add(paybillsCheckbookPanel);

                editBillsPopup.setTitle(resourceMap.getString("editBillsPopup.title")); // NOI18N
                editBillsPopup.setMinimumSize(new java.awt.Dimension(800, 500));
                editBillsPopup.setName("editBillsPopup"); // NOI18N
                editBillsPopup.getContentPane().setLayout(new javax.swing.BoxLayout(editBillsPopup.getContentPane(), javax.swing.BoxLayout.Y_AXIS));

                jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
                jPanel1.setMaximumSize(new java.awt.Dimension(800, 500));
                jPanel1.setMinimumSize(new java.awt.Dimension(800, 500));
                jPanel1.setName("jPanel1"); // NOI18N
                jPanel1.setPreferredSize(new java.awt.Dimension(872, 500));
                jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

                enterBillsPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("enterBillsPanel4.border.title"))); // NOI18N
                enterBillsPanel4.setMaximumSize(new java.awt.Dimension(32767, 115));
                enterBillsPanel4.setMinimumSize(new java.awt.Dimension(872, 150));
                enterBillsPanel4.setName("enterBillsPanel4"); // NOI18N
                enterBillsPanel4.setOpaque(false);
                enterBillsPanel4.setPreferredSize(new java.awt.Dimension(872, 150));
                enterBillsPanel4.setRequestFocusEnabled(false);
                enterBillsPanel4.setLayout(new java.awt.GridLayout(1, 0, 20, 0));

                jPanel233.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel233.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel233.setName("jPanel233"); // NOI18N
                jPanel233.setOpaque(false);
                jPanel233.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel233.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel234.setName("jPanel234"); // NOI18N
                jPanel234.setOpaque(false);
                jPanel234.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel143.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel143.setText(resourceMap.getString("jLabel143.text")); // NOI18N
                jLabel143.setName("jLabel143"); // NOI18N
                jPanel234.add(jLabel143);

                jLabel144.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel144.setText(resourceMap.getString("jLabel144.text")); // NOI18N
                jLabel144.setName("jLabel144"); // NOI18N
                jPanel234.add(jLabel144);

                jLabel145.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel145.setText(resourceMap.getString("jLabel145.text")); // NOI18N
                jLabel145.setName("jLabel145"); // NOI18N
                jPanel234.add(jLabel145);

                jLabel151.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel151.setText(resourceMap.getString("jLabel151.text")); // NOI18N
                jLabel151.setName("jLabel151"); // NOI18N
                jPanel234.add(jLabel151);

                jPanel233.add(jPanel234, java.awt.BorderLayout.WEST);

                jPanel235.setName("jPanel235"); // NOI18N
                jPanel235.setOpaque(false);
                jPanel235.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                editBillVendor.setEditable(false);
                editBillVendor.setName("editBillVendor"); // NOI18N
                editBillVendor.setStrict(false);
                editBillVendor.setStrictCompletion(false);
                editBillVendor.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                editBillVendorItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                editBillVendor.addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                editBillVendorItemStateChanged1(evt);
                        }
                });
                editBillVendor.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                editBillVendorActionPerformed(evt);
                        }
                });
                jPanel235.add(editBillVendor);

                editBillInvoice.setName("editBillInvoice"); // NOI18N
                editBillInvoice.addInputMethodListener(new java.awt.event.InputMethodListener() {
                        public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                        }
                        public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                                editBillInvoiceInputMethodTextChanged(evt);
                        }
                });
                jPanel235.add(editBillInvoice);

                editBillMemo.setName("editBillMemo"); // NOI18N
                jPanel235.add(editBillMemo);

                editClass.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", " " }));
                editClass.setName("editClass"); // NOI18N
                editClass.setStrict(false);
                editClass.setStrictCompletion(false);
                editClass.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                editClassItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                jPanel235.add(editClass);

                jPanel233.add(jPanel235, java.awt.BorderLayout.CENTER);

                enterBillsPanel4.add(jPanel233);

                jPanel236.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel236.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel236.setName("jPanel236"); // NOI18N
                jPanel236.setOpaque(false);
                jPanel236.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel236.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel237.setName("jPanel237"); // NOI18N
                jPanel237.setOpaque(false);
                jPanel237.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                editBillPostDate.setDateFormatString(resourceMap.getString("editBillPostDate.dateFormatString")); // NOI18N
                editBillPostDate.setName("editBillPostDate"); // NOI18N
                jPanel237.add(editBillPostDate);

                editBillDueDate.setDateFormatString(resourceMap.getString("editBillDueDate.dateFormatString")); // NOI18N
                editBillDueDate.setName("editBillDueDate"); // NOI18N
                jPanel237.add(editBillDueDate);

                editBillAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
                editBillAmount.setName("editBillAmount"); // NOI18N
                editBillAmount.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusLost(java.awt.event.FocusEvent evt) {
                                editBillAmountFocusLost(evt);
                        }
                });
                editBillAmount.addInputMethodListener(new java.awt.event.InputMethodListener() {
                        public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                        }
                        public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                                editBillAmountInputMethodTextChanged(evt);
                        }
                });
                jPanel237.add(editBillAmount);

                jPanel65.setName("jPanel65"); // NOI18N
                jPanel65.setLayout(new javax.swing.BoxLayout(jPanel65, javax.swing.BoxLayout.LINE_AXIS));

                editBillTotalExpenses.setEditable(false);
                editBillTotalExpenses.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                editBillTotalExpenses.setName("editBillTotalExpenses"); // NOI18N
                jPanel65.add(editBillTotalExpenses);

                editBillLinesDiff.setEditable(false);
                editBillLinesDiff.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                editBillLinesDiff.setName("editBillLinesDiff"); // NOI18N
                jPanel65.add(editBillLinesDiff);

                jPanel237.add(jPanel65);

                jPanel236.add(jPanel237, java.awt.BorderLayout.CENTER);

                jPanel238.setName("jPanel238"); // NOI18N
                jPanel238.setOpaque(false);
                jPanel238.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel146.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel146.setText(resourceMap.getString("jLabel146.text")); // NOI18N
                jLabel146.setName("jLabel146"); // NOI18N
                jPanel238.add(jLabel146);

                jLabel147.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel147.setText(resourceMap.getString("jLabel147.text")); // NOI18N
                jLabel147.setName("jLabel147"); // NOI18N
                jPanel238.add(jLabel147);

                jLabel148.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel148.setText(resourceMap.getString("jLabel148.text")); // NOI18N
                jLabel148.setName("jLabel148"); // NOI18N
                jPanel238.add(jLabel148);

                jLabel149.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel149.setText(resourceMap.getString("jLabel149.text")); // NOI18N
                jLabel149.setName("jLabel149"); // NOI18N
                jPanel238.add(jLabel149);

                jPanel236.add(jPanel238, java.awt.BorderLayout.WEST);

                enterBillsPanel4.add(jPanel236);

                jPanel239.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel239.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel239.setName("jPanel239"); // NOI18N
                jPanel239.setOpaque(false);
                jPanel239.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel239.setLayout(null);

                billsCreateButton1.setText(resourceMap.getString("billsCreateButton1.text")); // NOI18N
                billsCreateButton1.setToolTipText(resourceMap.getString("billsCreateButton1.toolTipText")); // NOI18N
                billsCreateButton1.setName("billsCreateButton1"); // NOI18N
                billsCreateButton1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsCreateButton1enterBillsButtonsClicked(evt);
                        }
                });
                jPanel239.add(billsCreateButton1);
                billsCreateButton1.setBounds(0, 10, 80, 23);

                billsClearButton1.setText(resourceMap.getString("billsClearButton1.text")); // NOI18N
                billsClearButton1.setMaximumSize(new java.awt.Dimension(83, 23));
                billsClearButton1.setMinimumSize(new java.awt.Dimension(83, 23));
                billsClearButton1.setName("billsClearButton1"); // NOI18N
                billsClearButton1.setPreferredSize(new java.awt.Dimension(83, 23));
                billsClearButton1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsClearButton1enterBillsButtonsClicked(evt);
                        }
                });
                jPanel239.add(billsClearButton1);
                billsClearButton1.setBounds(80, 10, 90, 23);

                billsAddLineButton1.setText(resourceMap.getString("billsAddLineButton1.text")); // NOI18N
                billsAddLineButton1.setMaximumSize(new java.awt.Dimension(80, 23));
                billsAddLineButton1.setMinimumSize(new java.awt.Dimension(80, 23));
                billsAddLineButton1.setName("billsAddLineButton1"); // NOI18N
                billsAddLineButton1.setPreferredSize(new java.awt.Dimension(80, 23));
                billsAddLineButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsAddLineButton1ActionPerformed(evt);
                        }
                });
                jPanel239.add(billsAddLineButton1);
                billsAddLineButton1.setBounds(0, 40, 80, 23);

                billsDeleteLineButton1.setText(resourceMap.getString("billsDeleteLineButton1.text")); // NOI18N
                billsDeleteLineButton1.setMaximumSize(new java.awt.Dimension(110, 23));
                billsDeleteLineButton1.setMinimumSize(new java.awt.Dimension(110, 23));
                billsDeleteLineButton1.setName("billsDeleteLineButton1"); // NOI18N
                billsDeleteLineButton1.setPreferredSize(new java.awt.Dimension(110, 23));
                billsDeleteLineButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsDeleteLineButton1ActionPerformed(evt);
                        }
                });
                jPanel239.add(billsDeleteLineButton1);
                billsDeleteLineButton1.setBounds(80, 40, 90, 23);

                enterBillsPanel4.add(jPanel239);

                jPanel196.setName("jPanel196"); // NOI18N
                jPanel196.setOpaque(false);
                jPanel196.setPreferredSize(new java.awt.Dimension(72, 106));
                jPanel196.setLayout(new javax.swing.BoxLayout(jPanel196, javax.swing.BoxLayout.LINE_AXIS));

                entryIdLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                entryIdLabel3.setMaximumSize(new java.awt.Dimension(72, 14));
                entryIdLabel3.setMinimumSize(new java.awt.Dimension(72, 14));
                entryIdLabel3.setName("entryIdLabel3"); // NOI18N
                entryIdLabel3.setPreferredSize(new java.awt.Dimension(72, 14));
                jPanel196.add(entryIdLabel3);

                billsVendorId1.setName("billsVendorId1"); // NOI18N
                jPanel196.add(billsVendorId1);

                enterBillsPanel4.add(jPanel196);

                jPanel1.add(enterBillsPanel4);

                jPanel240.setMaximumSize(new java.awt.Dimension(32767, 200));
                jPanel240.setMinimumSize(new java.awt.Dimension(872, 150));
                jPanel240.setName("jPanel240"); // NOI18N
                jPanel240.setPreferredSize(new java.awt.Dimension(872, 150));
                jPanel240.setLayout(new javax.swing.BoxLayout(jPanel240, javax.swing.BoxLayout.LINE_AXIS));

                jScrollPane2.setName("jScrollPane2"); // NOI18N

                editBillExpensesTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {
                                {null, null, null}
                        },
                        new String [] {
                                "Account", "Amount", "Control #"
                        }
                ));
                editBillExpensesTable.setCellSelectionEnabled(true);
                editBillExpensesTable.setName("editBillExpensesTable"); // NOI18N
                editBillExpensesTable.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                editBillExpensesTableMouseClicked(evt);
                        }
                });
                editBillExpensesTable.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent evt) {
                                editBillExpensesTableFocusGained(evt);
                        }
                });
                jScrollPane2.setViewportView(editBillExpensesTable);

                jPanel240.add(jScrollPane2);

                jPanel1.add(jPanel240);

                editBillsPopup.getContentPane().add(jPanel1);

        editPaidBillsPopup.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        editPaidBillsPopup.setTitle(resourceMap.getString("editPaidBillsPopup.title")); // NOI18N
        editPaidBillsPopup.setAlwaysOnTop(true);
        editPaidBillsPopup.setLocationByPlatform(true);
        editPaidBillsPopup.setMinimumSize(new java.awt.Dimension(972, 500));
        editPaidBillsPopup.setModal(true);
        editPaidBillsPopup.setName("editPaidBillsPopup"); // NOI18N
        editPaidBillsPopup.setResizable(false);
        editPaidBillsPopup.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

                paybillsCheckbookPanel1.setName("paybillsCheckbookPanel1"); // NOI18N
                paybillsCheckbookPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentShown(java.awt.event.ComponentEvent evt) {
                                paybillsCheckbookPanel1ComponentShown(evt);
                        }
                });
                paybillsCheckbookPanel1.setLayout(new javax.swing.BoxLayout(paybillsCheckbookPanel1, javax.swing.BoxLayout.Y_AXIS));

                jPanel161.setName("jPanel161"); // NOI18N
                jPanel161.setOpaque(false);
                jPanel161.setPreferredSize(new java.awt.Dimension(926, 330));
                jPanel161.setLayout(new javax.swing.BoxLayout(jPanel161, javax.swing.BoxLayout.LINE_AXIS));

                jPanel166.setBackground(resourceMap.getColor("jPanel166.background")); // NOI18N
                jPanel166.setMinimumSize(new java.awt.Dimension(747, 350));
                jPanel166.setName("jPanel166"); // NOI18N
                jPanel166.setLayout(null);

                jPanel168.setMaximumSize(new java.awt.Dimension(330, 280));
                jPanel168.setMinimumSize(new java.awt.Dimension(330, 280));
                jPanel168.setName("jPanel168"); // NOI18N
                jPanel168.setOpaque(false);
                jPanel168.setPreferredSize(new java.awt.Dimension(840, 325));

                jPanel197.setMinimumSize(new java.awt.Dimension(767, 330));
                jPanel197.setName("jPanel197"); // NOI18N
                jPanel197.setOpaque(false);

                jPanel198.setName("jPanel198"); // NOI18N
                jPanel198.setOpaque(false);

                paidbillsAmount.setEditable(false);
                paidbillsAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                paidbillsAmount.setMaximumSize(new java.awt.Dimension(81, 2147483647));
                paidbillsAmount.setMinimumSize(new java.awt.Dimension(81, 20));
                paidbillsAmount.setName("paidbillsAmount"); // NOI18N
                paidbillsAmount.setPreferredSize(new java.awt.Dimension(81, 20));
                paidbillsAmount.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                paidbillsAmountActionPerformed(evt);
                        }
                });

                paidbillsPayto.setEnabled(false);
                paidbillsPayto.setName("paidbillsPayto"); // NOI18N
                paidbillsPayto.setStrict(false);
                paidbillsPayto.setStrictCompletion(false);

                paidbillsMemo.setMaximumSize(new java.awt.Dimension(250, 2147483647));
                paidbillsMemo.setMinimumSize(new java.awt.Dimension(250, 20));
                paidbillsMemo.setName("paidbillsMemo"); // NOI18N
                paidbillsMemo.setPreferredSize(new java.awt.Dimension(250, 20));

                paidbillsClass.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", " " }));
                paidbillsClass.setName("paidbillsClass"); // NOI18N
                paidbillsClass.setStrict(false);
                paidbillsClass.setStrictCompletion(false);
                paidbillsClass.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                paidbillsClassItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });

                jLabel3.setName("jLabel3"); // NOI18N

                jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
                jLabel4.setName("jLabel4"); // NOI18N

                jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
                jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
                jLabel5.setName("jLabel5"); // NOI18N

                javax.swing.GroupLayout jPanel198Layout = new javax.swing.GroupLayout(jPanel198);
                jPanel198.setLayout(jPanel198Layout);
                jPanel198Layout.setHorizontalGroup(
                        jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel198Layout.createSequentialGroup()
                                .addGroup(jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel198Layout.createSequentialGroup()
                                                .addGap(108, 108, 108)
                                                .addComponent(paidbillsPayto, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel4)
                                                .addGap(18, 18, 18)
                                                .addComponent(paidbillsAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel198Layout.createSequentialGroup()
                                                .addGroup(jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel198Layout.createSequentialGroup()
                                                                .addGap(43, 43, 43)
                                                                .addComponent(jLabel3)
                                                                .addGap(35, 35, 35))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel198Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jLabel5)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                                .addGroup(jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(paidbillsClass, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(paidbillsMemo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                jPanel198Layout.setVerticalGroup(
                        jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel198Layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paidbillsAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(paidbillsPayto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                                .addGroup(jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(paidbillsClass, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel198Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel3)
                                                .addComponent(jLabel5)))
                                .addGap(18, 18, 18)
                                .addComponent(paidbillsMemo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );

                balancePanel5.setName("balancePanel5"); // NOI18N
                balancePanel5.setOpaque(false);

                jPanel199.setName("jPanel199"); // NOI18N
                jPanel199.setOpaque(false);

                paidbillsBankCombo.setEnabled(false);
                paidbillsBankCombo.setMaximumSize(new java.awt.Dimension(154, 32767));
                paidbillsBankCombo.setMinimumSize(new java.awt.Dimension(154, 20));
                paidbillsBankCombo.setName("paidbillsBankCombo"); // NOI18N
                paidbillsBankCombo.setPreferredSize(new java.awt.Dimension(154, 20));
                paidbillsBankCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                paidbillsBankCombobankSelected(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                paidbillsBankCombo.addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                paidbillsBankComboItemStateChanged(evt);
                        }
                });

                jLabel170.setText("BANK ACCOUNT :"); // NOI18N
                jLabel170.setName("jLabel170"); // NOI18N

                javax.swing.GroupLayout jPanel199Layout = new javax.swing.GroupLayout(jPanel199);
                jPanel199.setLayout(jPanel199Layout);
                jPanel199Layout.setHorizontalGroup(
                        jPanel199Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel199Layout.createSequentialGroup()
                                .addContainerGap(26, Short.MAX_VALUE)
                                .addComponent(jLabel170)
                                .addGap(18, 18, 18)
                                .addComponent(paidbillsBankCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );
                jPanel199Layout.setVerticalGroup(
                        jPanel199Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel199Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel170)
                                .addComponent(paidbillsBankCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                jPanel200.setName("jPanel200"); // NOI18N
                jPanel200.setOpaque(false);

                jLabel171.setText("ENDING BALANCE :"); // NOI18N
                jLabel171.setName("jLabel171"); // NOI18N

                paidbillsEndingBalance.setEditable(false);
                paidbillsEndingBalance.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("¤#,##0.00"))));
                paidbillsEndingBalance.setMaximumSize(new java.awt.Dimension(90, 2147483647));
                paidbillsEndingBalance.setMinimumSize(new java.awt.Dimension(90, 20));
                paidbillsEndingBalance.setName("paidbillsEndingBalance"); // NOI18N
                paidbillsEndingBalance.setPreferredSize(new java.awt.Dimension(90, 20));

                paidbillsNumber.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
                paidbillsNumber.setMaximumSize(new java.awt.Dimension(81, 2147483647));
                paidbillsNumber.setMinimumSize(new java.awt.Dimension(81, 20));
                paidbillsNumber.setName("paidbillsNumber"); // NOI18N
                paidbillsNumber.setPreferredSize(new java.awt.Dimension(81, 20));

                jLabel164.setText("CHECK NUMBER :"); // NOI18N
                jLabel164.setName("jLabel164"); // NOI18N

                javax.swing.GroupLayout jPanel200Layout = new javax.swing.GroupLayout(jPanel200);
                jPanel200.setLayout(jPanel200Layout);
                jPanel200Layout.setHorizontalGroup(
                        jPanel200Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel200Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel200Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel171, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel164, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel200Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(paidbillsNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(paidbillsEndingBalance, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                                .addGap(37, 37, 37))
                );
                jPanel200Layout.setVerticalGroup(
                        jPanel200Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel200Layout.createSequentialGroup()
                                .addGroup(jPanel200Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paidbillsEndingBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel171))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel200Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paidbillsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel164)))
                );

                javax.swing.GroupLayout balancePanel5Layout = new javax.swing.GroupLayout(balancePanel5);
                balancePanel5.setLayout(balancePanel5Layout);
                balancePanel5Layout.setHorizontalGroup(
                        balancePanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, balancePanel5Layout.createSequentialGroup()
                                .addContainerGap(23, Short.MAX_VALUE)
                                .addComponent(jPanel199, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(199, 199, 199)
                                .addComponent(jPanel200, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                );
                balancePanel5Layout.setVerticalGroup(
                        balancePanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(balancePanel5Layout.createSequentialGroup()
                                .addContainerGap(20, Short.MAX_VALUE)
                                .addGroup(balancePanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jPanel199, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel200, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );

                paidbillsDate.setMaximumSize(new java.awt.Dimension(99, 2147483647));
                paidbillsDate.setMinimumSize(new java.awt.Dimension(99, 20));
                paidbillsDate.setName("paidbillsDate"); // NOI18N
                paidbillsDate.setPreferredSize(new java.awt.Dimension(99, 20));

                javax.swing.GroupLayout jPanel197Layout = new javax.swing.GroupLayout(jPanel197);
                jPanel197.setLayout(jPanel197Layout);
                jPanel197Layout.setHorizontalGroup(
                        jPanel197Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel197Layout.createSequentialGroup()
                                .addGroup(jPanel197Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(paidbillsDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel197Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(balancePanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jPanel198, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap(15, Short.MAX_VALUE))
                );
                jPanel197Layout.setVerticalGroup(
                        jPanel197Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel197Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(balancePanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(paidbillsDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel198, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(37, 37, 37))
                );

                javax.swing.GroupLayout jPanel168Layout = new javax.swing.GroupLayout(jPanel168);
                jPanel168.setLayout(jPanel168Layout);
                jPanel168Layout.setHorizontalGroup(
                        jPanel168Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel168Layout.createSequentialGroup()
                                .addComponent(jPanel197, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel168Layout.setVerticalGroup(
                        jPanel168Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel168Layout.createSequentialGroup()
                                .addComponent(jPanel197, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46))
                );

                jPanel166.add(jPanel168);
                jPanel168.setBounds(0, 0, 769, 320);

                jPanel169.setName("jPanel169"); // NOI18N
                jPanel169.setOpaque(false);

                paybillsEntryID1.setName("paybillsEntryID1"); // NOI18N

                paybillsEntryId1.setName("paybillsEntryId1"); // NOI18N

                javax.swing.GroupLayout jPanel169Layout = new javax.swing.GroupLayout(jPanel169);
                jPanel169.setLayout(jPanel169Layout);
                jPanel169Layout.setHorizontalGroup(
                        jPanel169Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel169Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(paybillsEntryID1)
                                .addContainerGap(413, Short.MAX_VALUE))
                        .addGroup(jPanel169Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel169Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(paybillsEntryId1)
                                        .addContainerGap(413, Short.MAX_VALUE)))
                );
                jPanel169Layout.setVerticalGroup(
                        jPanel169Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel169Layout.createSequentialGroup()
                                .addContainerGap(287, Short.MAX_VALUE)
                                .addComponent(paybillsEntryID1)
                                .addGap(21, 21, 21))
                        .addGroup(jPanel169Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel169Layout.createSequentialGroup()
                                        .addContainerGap(296, Short.MAX_VALUE)
                                        .addComponent(paybillsEntryId1)
                                        .addGap(12, 12, 12)))
                );

        jPanel166.add(jPanel169);
        jPanel169.setBounds(1733, 11, 0, 308);

                jLabel30.setBackground(resourceMap.getColor("jLabel30.background")); // NOI18N
                jLabel30.setIcon(resourceMap.getIcon("jLabel27.icon")); // NOI18N
                jLabel30.setName("jLabel30"); // NOI18N
                jLabel30.setOpaque(true);
                jPanel166.add(jLabel30);
                jLabel30.setBounds(0, 0, 800, 340);

                paybillsCancelButton1.setText(resourceMap.getString("paybillsCancelButton1.text")); // NOI18N
                paybillsCancelButton1.setName("paybillsCancelButton1"); // NOI18N
                paybillsCancelButton1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                paybillsCancelButton1(evt);
                        }
                });
                jPanel166.add(paybillsCancelButton1);
                paybillsCancelButton1.setBounds(810, 70, 140, 23);

                paidbillsUpdateAndPrintButton.setText(resourceMap.getString("paidbillsUpdateAndPrintButton.text")); // NOI18N
                paidbillsUpdateAndPrintButton.setName("paidbillsUpdateAndPrintButton"); // NOI18N
                paidbillsUpdateAndPrintButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                paidbillsUpdateAndPrintButtonsaveCheckButtonsClicked(evt);
                        }
                });
                jPanel166.add(paidbillsUpdateAndPrintButton);
                paidbillsUpdateAndPrintButton.setBounds(810, 40, 140, 20);

                paidbillsUpdateButton.setText(resourceMap.getString("paidbillsUpdateButton.text")); // NOI18N
                paidbillsUpdateButton.setMaximumSize(new java.awt.Dimension(135, 23));
                paidbillsUpdateButton.setMinimumSize(new java.awt.Dimension(135, 23));
                paidbillsUpdateButton.setName("paidbillsUpdateButton"); // NOI18N
                paidbillsUpdateButton.setPreferredSize(new java.awt.Dimension(135, 23));
                paidbillsUpdateButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                paidbillsUpdateButton(evt);
                        }
                });
                jPanel166.add(paidbillsUpdateButton);
                paidbillsUpdateButton.setBounds(810, 10, 140, 23);

                jPanel161.add(jPanel166);

                paybillsCheckbookPanel1.add(jPanel161);

                jPanel170.setBackground(resourceMap.getColor("jPanel170.background")); // NOI18N
                jPanel170.setBorder(javax.swing.BorderFactory.createTitledBorder("Bills to Pay"));
                jPanel170.setMinimumSize(new java.awt.Dimension(400, 401));
                jPanel170.setName("jPanel170"); // NOI18N
                jPanel170.setPreferredSize(new java.awt.Dimension(400, 401));
                jPanel170.setLayout(null);

                jScrollPane35.setName("jScrollPane35"); // NOI18N

                paidbillsDueTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Selected", "Invoice #", "ID", "Vendor Name", "Account", "Amount", "Control #", "Post Date", "Due Date", "Memo"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
                        };
                        boolean[] canEdit = new boolean [] {
                                true, false, false, false, false, false, true, true, false, false
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                paidbillsDueTable.setFillsViewportHeight(true);
                paidbillsDueTable.setName("paidbillsDueTable"); // NOI18N
                paidbillsDueTable.getTableHeader().setReorderingAllowed(false);
                jScrollPane35.setViewportView(paidbillsDueTable);
                paidbillsDueTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title5")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("paybillsDueTable.columnModel.title1")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(2).setMinWidth(0);
                paidbillsDueTable.getColumnModel().getColumn(2).setPreferredWidth(0);
                paidbillsDueTable.getColumnModel().getColumn(2).setMaxWidth(0);
                paidbillsDueTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("paybillsDueTable.columnModel.title2")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title4")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title6")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("paybillsDueTable.columnModel.title5")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title7")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title8")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(8).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title9")); // NOI18N
                paidbillsDueTable.getColumnModel().getColumn(9).setHeaderValue(resourceMap.getString("paybillsDueTable1.columnModel.title3")); // NOI18N

                jPanel170.add(jScrollPane35);
                jScrollPane35.setBounds(6, 20, 914, 90);

                paybillsCheckbookPanel1.add(jPanel170);

                editPaidBillsPopup.getContentPane().add(paybillsCheckbookPanel1);

                jToggleButton1.setName("jToggleButton1"); // NOI18N

                editCreditsPopup.setTitle(resourceMap.getString("editCreditsPopup.title")); // NOI18N
                editCreditsPopup.setMinimumSize(new java.awt.Dimension(820, 500));
                editCreditsPopup.setName("editCreditsPopup"); // NOI18N
                editCreditsPopup.addInputMethodListener(new java.awt.event.InputMethodListener() {
                        public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                        }
                        public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                                editCreditsPopupInputMethodTextChanged(evt);
                        }
                });
                editCreditsPopup.getContentPane().setLayout(new javax.swing.BoxLayout(editCreditsPopup.getContentPane(), javax.swing.BoxLayout.Y_AXIS));

                jPanel5.setBackground(resourceMap.getColor("jPanel5.background")); // NOI18N
                jPanel5.setMaximumSize(new java.awt.Dimension(800, 500));
                jPanel5.setMinimumSize(new java.awt.Dimension(800, 500));
                jPanel5.setName("jPanel5"); // NOI18N
                jPanel5.setPreferredSize(new java.awt.Dimension(800, 500));
                jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

                enterBillsPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("enterBillsPanel5.border.title"))); // NOI18N
                enterBillsPanel5.setMaximumSize(new java.awt.Dimension(32767, 115));
                enterBillsPanel5.setMinimumSize(new java.awt.Dimension(872, 200));
                enterBillsPanel5.setName("enterBillsPanel5"); // NOI18N
                enterBillsPanel5.setOpaque(false);
                enterBillsPanel5.setPreferredSize(new java.awt.Dimension(872, 200));
                enterBillsPanel5.setRequestFocusEnabled(false);
                enterBillsPanel5.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

                jPanel243.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel243.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel243.setName("jPanel243"); // NOI18N
                jPanel243.setOpaque(false);
                jPanel243.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel243.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel244.setName("jPanel244"); // NOI18N
                jPanel244.setOpaque(false);
                jPanel244.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel152.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel152.setText(resourceMap.getString("jLabel152.text")); // NOI18N
                jLabel152.setName("jLabel152"); // NOI18N
                jPanel244.add(jLabel152);

                jLabel153.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel153.setText(resourceMap.getString("jLabel153.text")); // NOI18N
                jLabel153.setName("jLabel153"); // NOI18N
                jPanel244.add(jLabel153);

                jLabel154.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel154.setText(resourceMap.getString("jLabel154.text")); // NOI18N
                jLabel154.setName("jLabel154"); // NOI18N
                jPanel244.add(jLabel154);

                jLabel155.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel155.setText(resourceMap.getString("jLabel155.text")); // NOI18N
                jLabel155.setName("jLabel155"); // NOI18N
                jPanel244.add(jLabel155);

                jPanel243.add(jPanel244, java.awt.BorderLayout.WEST);

                jPanel245.setName("jPanel245"); // NOI18N
                jPanel245.setOpaque(false);
                jPanel245.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                editBillVendor1.setEditable(false);
                editBillVendor1.setName("editBillVendor1"); // NOI18N
                editBillVendor1.setStrict(false);
                editBillVendor1.setStrictCompletion(false);
                editBillVendor1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                editBillVendor1ItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                editBillVendor1.addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                editBillVendor1ItemStateChanged1(evt);
                        }
                });
                editBillVendor1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                editBillVendor1ActionPerformed(evt);
                        }
                });
                jPanel245.add(editBillVendor1);

                editBillInvoice1.setName("editBillInvoice1"); // NOI18N
                editBillInvoice1.addInputMethodListener(new java.awt.event.InputMethodListener() {
                        public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                        }
                        public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                                editBillInvoice1InputMethodTextChanged(evt);
                        }
                });
                jPanel245.add(editBillInvoice1);

                editBillMemo1.setName("editBillMemo1"); // NOI18N
                jPanel245.add(editBillMemo1);

                editClass1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", " " }));
                editClass1.setName("editClass1"); // NOI18N
                editClass1.setStrict(false);
                editClass1.setStrictCompletion(false);
                editClass1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                editClass1ItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                jPanel245.add(editClass1);

                jPanel243.add(jPanel245, java.awt.BorderLayout.CENTER);

                enterBillsPanel5.add(jPanel243);

                jPanel246.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel246.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel246.setName("jPanel246"); // NOI18N
                jPanel246.setOpaque(false);
                jPanel246.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel246.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel247.setName("jPanel247"); // NOI18N
                jPanel247.setOpaque(false);
                jPanel247.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                editBillPostDate1.setDateFormatString(resourceMap.getString("editBillPostDate1.dateFormatString")); // NOI18N
                editBillPostDate1.setName("editBillPostDate1"); // NOI18N
                jPanel247.add(editBillPostDate1);

                editBillDueDate1.setDateFormatString(resourceMap.getString("editBillDueDate1.dateFormatString")); // NOI18N
                editBillDueDate1.setName("editBillDueDate1"); // NOI18N
                jPanel247.add(editBillDueDate1);

                editBillAmount1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.00"))));
                editBillAmount1.setName("editBillAmount1"); // NOI18N
                editBillAmount1.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusLost(java.awt.event.FocusEvent evt) {
                                editBillAmount1FocusLost(evt);
                        }
                });
                editBillAmount1.addInputMethodListener(new java.awt.event.InputMethodListener() {
                        public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                        }
                        public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                                editBillAmount1InputMethodTextChanged(evt);
                        }
                });
                jPanel247.add(editBillAmount1);

                jPanel66.setName("jPanel66"); // NOI18N
                jPanel66.setLayout(new javax.swing.BoxLayout(jPanel66, javax.swing.BoxLayout.LINE_AXIS));

                editBillTotalExpenses1.setEditable(false);
                editBillTotalExpenses1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                editBillTotalExpenses1.setName("editBillTotalExpenses1"); // NOI18N
                jPanel66.add(editBillTotalExpenses1);

                editBillLinesDiff1.setEditable(false);
                editBillLinesDiff1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                editBillLinesDiff1.setName("editBillLinesDiff1"); // NOI18N
                jPanel66.add(editBillLinesDiff1);

                jPanel247.add(jPanel66);

                jPanel246.add(jPanel247, java.awt.BorderLayout.CENTER);

                jPanel248.setName("jPanel248"); // NOI18N
                jPanel248.setOpaque(false);
                jPanel248.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel156.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel156.setText(resourceMap.getString("jLabel156.text")); // NOI18N
                jLabel156.setName("jLabel156"); // NOI18N
                jPanel248.add(jLabel156);

                jLabel157.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel157.setText(resourceMap.getString("jLabel157.text")); // NOI18N
                jLabel157.setName("jLabel157"); // NOI18N
                jPanel248.add(jLabel157);

                jLabel158.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel158.setText(resourceMap.getString("jLabel158.text")); // NOI18N
                jLabel158.setName("jLabel158"); // NOI18N
                jPanel248.add(jLabel158);

                jLabel159.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel159.setText(resourceMap.getString("jLabel159.text")); // NOI18N
                jLabel159.setName("jLabel159"); // NOI18N
                jPanel248.add(jLabel159);

                jPanel246.add(jPanel248, java.awt.BorderLayout.WEST);

                enterBillsPanel5.add(jPanel246);

                jPanel249.setMaximumSize(new java.awt.Dimension(200, 100));
                jPanel249.setMinimumSize(new java.awt.Dimension(200, 100));
                jPanel249.setName("jPanel249"); // NOI18N
                jPanel249.setOpaque(false);
                jPanel249.setPreferredSize(new java.awt.Dimension(200, 100));
                jPanel249.setLayout(null);

                billsCreateButton2.setFont(resourceMap.getFont("billsCreateButton2.font")); // NOI18N
                billsCreateButton2.setText(resourceMap.getString("billsCreateButton2.text")); // NOI18N
                billsCreateButton2.setToolTipText(resourceMap.getString("billsCreateButton2.toolTipText")); // NOI18N
                billsCreateButton2.setMaximumSize(new java.awt.Dimension(100, 23));
                billsCreateButton2.setMinimumSize(new java.awt.Dimension(100, 23));
                billsCreateButton2.setName("billsCreateButton2"); // NOI18N
                billsCreateButton2.setPreferredSize(new java.awt.Dimension(100, 23));
                billsCreateButton2.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsCreateButton2enterBillsButtonsClicked(evt);
                        }
                        public void mousePressed(java.awt.event.MouseEvent evt) {
                                billsCreateButton2MousePressed(evt);
                        }
                });
                jPanel249.add(billsCreateButton2);
                billsCreateButton2.setBounds(0, 10, 100, 23);

                billsClearButton2.setFont(resourceMap.getFont("billsClearButton2.font")); // NOI18N
                billsClearButton2.setText(resourceMap.getString("billsClearButton2.text")); // NOI18N
                billsClearButton2.setMaximumSize(new java.awt.Dimension(100, 23));
                billsClearButton2.setMinimumSize(new java.awt.Dimension(100, 23));
                billsClearButton2.setName("billsClearButton2"); // NOI18N
                billsClearButton2.setPreferredSize(new java.awt.Dimension(100, 23));
                billsClearButton2.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsClearButton2enterBillsButtonsClicked(evt);
                        }
                });
                jPanel249.add(billsClearButton2);
                billsClearButton2.setBounds(90, 10, 100, 23);

                billsAddLineButton2.setFont(resourceMap.getFont("billsAddLineButton2.font")); // NOI18N
                billsAddLineButton2.setText(resourceMap.getString("billsAddLineButton2.text")); // NOI18N
                billsAddLineButton2.setMaximumSize(new java.awt.Dimension(100, 23));
                billsAddLineButton2.setMinimumSize(new java.awt.Dimension(100, 23));
                billsAddLineButton2.setName("billsAddLineButton2"); // NOI18N
                billsAddLineButton2.setPreferredSize(new java.awt.Dimension(100, 23));
                billsAddLineButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsAddLineButton2ActionPerformed(evt);
                        }
                });
                jPanel249.add(billsAddLineButton2);
                billsAddLineButton2.setBounds(0, 40, 100, 23);

                billsDeleteLineButton2.setFont(resourceMap.getFont("billsDeleteLineButton2.font")); // NOI18N
                billsDeleteLineButton2.setText(resourceMap.getString("billsDeleteLineButton2.text")); // NOI18N
                billsDeleteLineButton2.setMaximumSize(new java.awt.Dimension(100, 23));
                billsDeleteLineButton2.setMinimumSize(new java.awt.Dimension(100, 23));
                billsDeleteLineButton2.setName("billsDeleteLineButton2"); // NOI18N
                billsDeleteLineButton2.setPreferredSize(new java.awt.Dimension(100, 23));
                billsDeleteLineButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsDeleteLineButton2ActionPerformed(evt);
                        }
                });
                jPanel249.add(billsDeleteLineButton2);
                billsDeleteLineButton2.setBounds(90, 40, 100, 23);

                enterBillsPanel5.add(jPanel249);

                jPanel201.setMaximumSize(new java.awt.Dimension(30, 14));
                jPanel201.setMinimumSize(new java.awt.Dimension(30, 14));
                jPanel201.setName("jPanel201"); // NOI18N
                jPanel201.setOpaque(false);
                jPanel201.setPreferredSize(new java.awt.Dimension(30, 106));
                jPanel201.setLayout(new javax.swing.BoxLayout(jPanel201, javax.swing.BoxLayout.LINE_AXIS));

                entryIdLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                entryIdLabel4.setMaximumSize(new java.awt.Dimension(72, 14));
                entryIdLabel4.setMinimumSize(new java.awt.Dimension(72, 14));
                entryIdLabel4.setName("entryIdLabel4"); // NOI18N
                entryIdLabel4.setPreferredSize(new java.awt.Dimension(72, 14));
                jPanel201.add(entryIdLabel4);

                billsVendorId2.setName("billsVendorId2"); // NOI18N
                jPanel201.add(billsVendorId2);

                enterBillsPanel5.add(jPanel201);

                jPanel5.add(enterBillsPanel5);
                enterBillsPanel5.getAccessibleContext().setAccessibleName(resourceMap.getString("enterBillsPanel5.AccessibleContext.accessibleName")); // NOI18N

                jPanel250.setMaximumSize(new java.awt.Dimension(32767, 200));
                jPanel250.setMinimumSize(new java.awt.Dimension(872, 150));
                jPanel250.setName("jPanel250"); // NOI18N
                jPanel250.setPreferredSize(new java.awt.Dimension(872, 150));
                jPanel250.setLayout(new javax.swing.BoxLayout(jPanel250, javax.swing.BoxLayout.LINE_AXIS));

                jScrollPane4.setName("jScrollPane4"); // NOI18N

                editBillExpensesTable1.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {
                                {null, null, null}
                        },
                        new String [] {
                                "Account", "Amount", "Control #"
                        }
                ));
                editBillExpensesTable1.setCellSelectionEnabled(true);
                editBillExpensesTable1.setName("editBillExpensesTable1"); // NOI18N
                editBillExpensesTable1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                editBillExpensesTable1MouseClicked(evt);
                        }
                });
                editBillExpensesTable1.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent evt) {
                                editBillExpensesTable1FocusGained(evt);
                        }
                });
                jScrollPane4.setViewportView(editBillExpensesTable1);

                jPanel250.add(jScrollPane4);

                jPanel5.add(jPanel250);

                editCreditsPopup.getContentPane().add(jPanel5);

                setBackground(resourceMap.getColor("Form.background")); // NOI18N
                setFont(resourceMap.getFont("Form.font")); // NOI18N
                setName("Form"); // NOI18N
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentShown(java.awt.event.ComponentEvent evt) {
                                formComponentShown(evt);
                        }
                });
                setLayout(new java.awt.BorderLayout());

                jPanel155.setMaximumSize(new java.awt.Dimension(2147483647, 500));
                jPanel155.setName("jPanel155"); // NOI18N
                jPanel155.setOpaque(false);
                jPanel155.setPreferredSize(new java.awt.Dimension(250, 500));
                jPanel155.setLayout(new java.awt.BorderLayout());

                jPanel156.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel156.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
                jPanel156.setMaximumSize(new java.awt.Dimension(250, 46));
                jPanel156.setMinimumSize(new java.awt.Dimension(250, 46));
                jPanel156.setName("jPanel156"); // NOI18N
                jPanel156.setOpaque(false);
                jPanel156.setPreferredSize(new java.awt.Dimension(250, 46));
                jPanel156.setLayout(new javax.swing.BoxLayout(jPanel156, javax.swing.BoxLayout.X_AXIS));

                billsAllVendorsCheckbox.setText(resourceMap.getString("billsAllVendorsCheckbox.text")); // NOI18N
                billsAllVendorsCheckbox.setName("billsAllVendorsCheckbox"); // NOI18N
                billsAllVendorsCheckbox.setOpaque(false);
                billsAllVendorsCheckbox.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsAllVendorsCheckboxActionPerformed(evt);
                        }
                });
                jPanel156.add(billsAllVendorsCheckbox);

                jPanel155.add(jPanel156, java.awt.BorderLayout.PAGE_START);

                jPanel2.setBackground(getBackground());
                jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
                jPanel2.setAutoscrolls(true);
                jPanel2.setMaximumSize(new java.awt.Dimension(50, 50));
                jPanel2.setMinimumSize(new java.awt.Dimension(50, 50));
                jPanel2.setName("jPanel2"); // NOI18N
                jPanel2.setPreferredSize(new java.awt.Dimension(50, 50));
                jPanel2.setLayout(new java.awt.BorderLayout());

                jScrollPane34.setMaximumSize(new java.awt.Dimension(250, 800));
                jScrollPane34.setMinimumSize(new java.awt.Dimension(250, 500));
                jScrollPane34.setName("jScrollPane34"); // NOI18N
                jScrollPane34.setPreferredSize(new java.awt.Dimension(250, 800));

                billsVendorsTable.setAutoCreateRowSorter(true);
                billsVendorsTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Name", "Balance Total", "ID"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
                        };
                        boolean[] canEdit = new boolean [] {
                                false, false, false
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                billsVendorsTable.setFillsViewportHeight(true);
                billsVendorsTable.setName("billsVendorsTable"); // NOI18N
                billsVendorsTable.getTableHeader().setReorderingAllowed(false);
                billsVendorsTable.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsVendorsTableaccountsPayableVendorsClicked(evt);
                        }
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                                billsVendorsTableMouseEntered(evt);
                        }
                });
                billsVendorsTable.addKeyListener(new java.awt.event.KeyAdapter() {
                        public void keyReleased(java.awt.event.KeyEvent evt) {
                                billsVendorsTableaccountsPayableVendorsKeyed(evt);
                        }
                });
                jScrollPane34.setViewportView(billsVendorsTable);
                billsVendorsTable.getColumnModel().getColumn(0).setResizable(false);
                billsVendorsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("billsVendorsTable.columnModel.title0")); // NOI18N
                billsVendorsTable.getColumnModel().getColumn(1).setMinWidth(80);
                billsVendorsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
                billsVendorsTable.getColumnModel().getColumn(1).setMaxWidth(80);
                billsVendorsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("billsVendorsTable.columnModel.title1")); // NOI18N

                jPanel2.add(jScrollPane34, java.awt.BorderLayout.CENTER);

                jPanel3.setBackground(getBackground());
                jPanel3.setName("jPanel3"); // NOI18N

                txt_Search.setText(resourceMap.getString("searchVendor.text")); // NOI18N
                txt_Search.setToolTipText(resourceMap.getString("searchVendor.toolTipText")); // NOI18N
                txt_Search.setName("searchVendor"); // NOI18N

                SearchVendorButton.setText(resourceMap.getString("searchVendorButton.text")); // NOI18N
                SearchVendorButton.setToolTipText(resourceMap.getString("searchVendorButton.toolTipText")); // NOI18N
                SearchVendorButton.setName("searchVendorButton"); // NOI18N
                SearchVendorButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                SearchVendorButtonActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(txt_Search, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SearchVendorButton)
                                .addGap(20, 20, 20))
                );
                jPanel3Layout.setVerticalGroup(
                        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txt_Search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(SearchVendorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_START);

                jPanel155.add(jPanel2, java.awt.BorderLayout.CENTER);

                add(jPanel155, java.awt.BorderLayout.WEST);

                jPanel157.setName("jPanel157"); // NOI18N
                jPanel157.setOpaque(false);
                jPanel157.setLayout(new javax.swing.BoxLayout(jPanel157, javax.swing.BoxLayout.Y_AXIS));

                billsVendorInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("billsVendorInfo.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
                billsVendorInfo.setMaximumSize(new java.awt.Dimension(32767, 70));
                billsVendorInfo.setMinimumSize(new java.awt.Dimension(877, 70));
                billsVendorInfo.setName("billsVendorInfo"); // NOI18N
                billsVendorInfo.setOpaque(false);
                billsVendorInfo.setPreferredSize(new java.awt.Dimension(877, 70));
                billsVendorInfo.setLayout(new java.awt.GridLayout(1, 0, 20, 0));

                jPanel185.setName("jPanel185"); // NOI18N
                jPanel185.setOpaque(false);
                jPanel185.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel186.setName("jPanel186"); // NOI18N
                jPanel186.setOpaque(false);
                jPanel186.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
                jLabel35.setName("jLabel35"); // NOI18N
                jPanel186.add(jLabel35);

                jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel52.setText(resourceMap.getString("jLabel52.text")); // NOI18N
                jLabel52.setMaximumSize(new java.awt.Dimension(52, 14));
                jLabel52.setMinimumSize(new java.awt.Dimension(52, 14));
                jLabel52.setName("jLabel52"); // NOI18N
                jLabel52.setPreferredSize(new java.awt.Dimension(52, 14));
                jPanel186.add(jLabel52);

                jLabel116.setName("jLabel116"); // NOI18N
                jPanel186.add(jLabel116);

                jPanel185.add(jPanel186, java.awt.BorderLayout.WEST);

                jPanel187.setName("jPanel187"); // NOI18N
                jPanel187.setOpaque(false);
                jPanel187.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                billVendorName.setEditable(false);
                billVendorName.setBorder(null);
                billVendorName.setName("billVendorName"); // NOI18N
                billVendorName.setOpaque(false);
                jPanel187.add(billVendorName);

                billVendorAddress.setEditable(false);
                billVendorAddress.setBorder(null);
                billVendorAddress.setName("billVendorAddress"); // NOI18N
                billVendorAddress.setOpaque(false);
                jPanel187.add(billVendorAddress);

                billVendorAddress2.setEditable(false);
                billVendorAddress2.setBorder(null);
                billVendorAddress2.setName("billVendorAddress2"); // NOI18N
                billVendorAddress2.setOpaque(false);
                jPanel187.add(billVendorAddress2);

                jPanel185.add(jPanel187, java.awt.BorderLayout.CENTER);

                billsVendorInfo.add(jPanel185);

                jPanel188.setName("jPanel188"); // NOI18N
                jPanel188.setOpaque(false);
                jPanel188.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel189.setName("jPanel189"); // NOI18N
                jPanel189.setOpaque(false);
                jPanel189.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                billVendorConatct.setEditable(false);
                billVendorConatct.setBorder(null);
                billVendorConatct.setName("billVendorConatct"); // NOI18N
                billVendorConatct.setOpaque(false);
                jPanel189.add(billVendorConatct);

                billVendorEmail.setEditable(false);
                billVendorEmail.setBorder(null);
                billVendorEmail.setName("billVendorEmail"); // NOI18N
                billVendorEmail.setOpaque(false);
                jPanel189.add(billVendorEmail);

                billVendorPhone.setEditable(false);
                billVendorPhone.setBorder(null);
                billVendorPhone.setName("billVendorPhone"); // NOI18N
                billVendorPhone.setOpaque(false);
                jPanel189.add(billVendorPhone);

                jPanel188.add(jPanel189, java.awt.BorderLayout.CENTER);

                jPanel194.setName("jPanel194"); // NOI18N
                jPanel194.setOpaque(false);
                jPanel194.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel120.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel120.setText(resourceMap.getString("jLabel120.text")); // NOI18N
                jLabel120.setName("jLabel120"); // NOI18N
                jPanel194.add(jLabel120);

                jLabel118.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel118.setText(resourceMap.getString("jLabel118.text")); // NOI18N
                jLabel118.setName("jLabel118"); // NOI18N
                jPanel194.add(jLabel118);

                jLabel119.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel119.setText(resourceMap.getString("jLabel119.text")); // NOI18N
                jLabel119.setName("jLabel119"); // NOI18N
                jPanel194.add(jLabel119);

                jPanel188.add(jPanel194, java.awt.BorderLayout.WEST);

                billsVendorInfo.add(jPanel188);

                jPanel157.add(billsVendorInfo);

                jPanel209.setBackground(resourceMap.getColor("jPanel209.background")); // NOI18N
                jPanel209.setName("jPanel209"); // NOI18N
                jPanel209.setLayout(new javax.swing.BoxLayout(jPanel209, javax.swing.BoxLayout.Y_AXIS));

                enterBillsPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("enterBillsPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
                enterBillsPanel3.setMaximumSize(new java.awt.Dimension(32767, 115));
                enterBillsPanel3.setMinimumSize(new java.awt.Dimension(872, 130));
                enterBillsPanel3.setName("enterBillsPanel3"); // NOI18N
                enterBillsPanel3.setOpaque(false);
                enterBillsPanel3.setPreferredSize(new java.awt.Dimension(872, 130));
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

                filler8.setName("filler8"); // NOI18N
                jPanel219.add(filler8);

                jLabel142.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel142.setText(resourceMap.getString("jLabel142.text")); // NOI18N
                jLabel142.setName("jLabel142"); // NOI18N
                jPanel219.add(jLabel142);

                jLabel139.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel139.setText(resourceMap.getString("jLabel139.text")); // NOI18N
                jLabel139.setName("jLabel139"); // NOI18N
                jPanel219.add(jLabel139);

                jLabel141.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel141.setText(resourceMap.getString("jLabel141.text")); // NOI18N
                jLabel141.setName("jLabel141"); // NOI18N
                jPanel219.add(jLabel141);

                jLabel150.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel150.setText(resourceMap.getString("jLabel150.text")); // NOI18N
                jLabel150.setName("jLabel150"); // NOI18N
                jPanel219.add(jLabel150);

                jPanel218.add(jPanel219, java.awt.BorderLayout.WEST);

                jPanel220.setName("jPanel220"); // NOI18N
                jPanel220.setOpaque(false);
                jPanel220.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                billCreditCheck.setText(resourceMap.getString("billCreditCheck.text")); // NOI18N
                billCreditCheck.setName("billCreditCheck"); // NOI18N
                billCreditCheck.setOpaque(false);
                billCreditCheck.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billCreditCheckMouseClicked(evt);
                        }
                });
                jPanel220.add(billCreditCheck);

                billVendor.setEnabled(false);
                billVendor.setName("billVendor"); // NOI18N
                billVendor.setStrict(false);
                billVendor.setStrictCompletion(false);
                billVendor.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                                billVendorItemStateChanged(evt);
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                });
                jPanel220.add(billVendor);

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

                billClass.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Miami", "Hollywood", "Wholesale", "Miami:Service", "Admin", " " }));
                billClass.setName("billClass"); // NOI18N
                billClass.setStrict(false);
                billClass.setStrictCompletion(false);
                jPanel220.add(billClass);

                jPanel218.add(jPanel220, java.awt.BorderLayout.CENTER);

                enterBillsPanel3.add(jPanel218);

                jPanel221.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel221.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel221.setName("jPanel221"); // NOI18N
                jPanel221.setOpaque(false);
                jPanel221.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel221.setLayout(new java.awt.BorderLayout(5, 0));

                jPanel222.setName("jPanel222"); // NOI18N
                jPanel222.setOpaque(false);
                jPanel222.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                billPostDate.setDateFormatString(resourceMap.getString("billPostDate.dateFormatString")); // NOI18N
                billPostDate.setName("billPostDate"); // NOI18N
                jPanel222.add(billPostDate);

                billDueDate.setDateFormatString(resourceMap.getString("billDueDate.dateFormatString")); // NOI18N
                billDueDate.setName("billDueDate"); // NOI18N
                jPanel222.add(billDueDate);

                billAmount.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                billAmount.setName("billAmount"); // NOI18N
                billAmount.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusLost(java.awt.event.FocusEvent evt) {
                                billAmountFocusLost(evt);
                        }
                });
                billAmount.addInputMethodListener(new java.awt.event.InputMethodListener() {
                        public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                        }
                        public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                                billAmountInputMethodTextChanged(evt);
                        }
                });
                jPanel222.add(billAmount);

                jPanel64.setName("jPanel64"); // NOI18N
                jPanel64.setLayout(new javax.swing.BoxLayout(jPanel64, javax.swing.BoxLayout.LINE_AXIS));

                billTotalExpenses.setEditable(false);
                billTotalExpenses.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                billTotalExpenses.setName("billTotalExpenses"); // NOI18N
                jPanel64.add(billTotalExpenses);

                billLinesDiff.setEditable(false);
                billLinesDiff.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
                billLinesDiff.setName("billLinesDiff"); // NOI18N
                jPanel64.add(billLinesDiff);

                jPanel222.add(jPanel64);

                jPanel221.add(jPanel222, java.awt.BorderLayout.CENTER);

                jPanel223.setName("jPanel223"); // NOI18N
                jPanel223.setOpaque(false);
                jPanel223.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

                jLabel136.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel136.setText(resourceMap.getString("jLabel136.text")); // NOI18N
                jLabel136.setName("jLabel136"); // NOI18N
                jPanel223.add(jLabel136);

                jLabel137.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel137.setText(resourceMap.getString("jLabel137.text")); // NOI18N
                jLabel137.setName("jLabel137"); // NOI18N
                jPanel223.add(jLabel137);

                jLabel138.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel138.setText(resourceMap.getString("jLabel138.text")); // NOI18N
                jLabel138.setName("jLabel138"); // NOI18N
                jPanel223.add(jLabel138);

                jLabel140.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel140.setText(resourceMap.getString("jLabel140.text")); // NOI18N
                jLabel140.setName("jLabel140"); // NOI18N
                jPanel223.add(jLabel140);

                jPanel221.add(jPanel223, java.awt.BorderLayout.WEST);

                enterBillsPanel3.add(jPanel221);

                jPanel224.setAutoscrolls(true);
                jPanel224.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
                jPanel224.setFocusable(false);
                jPanel224.setMaximumSize(new java.awt.Dimension(200, 86));
                jPanel224.setMinimumSize(new java.awt.Dimension(200, 86));
                jPanel224.setName("jPanel224"); // NOI18N
                jPanel224.setOpaque(false);
                jPanel224.setPreferredSize(new java.awt.Dimension(200, 86));
                jPanel224.setLayout(null);

                billsCreateButton.setText(resourceMap.getString("billsCreateButton.text")); // NOI18N
                billsCreateButton.setToolTipText(resourceMap.getString("billsCreateButton.toolTipText")); // NOI18N
                billsCreateButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
                billsCreateButton.setFocusable(false);
                billsCreateButton.setMaximumSize(new java.awt.Dimension(105, 23));
                billsCreateButton.setName("billsCreateButton"); // NOI18N
                billsCreateButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsCreateButtonenterBillsButtonsClicked(evt);
                        }
                        public void mousePressed(java.awt.event.MouseEvent evt) {
                                billsCreateButtonMousePressed(evt);
                        }
                });
                jPanel224.add(billsCreateButton);
                billsCreateButton.setBounds(0, 0, 100, 23);

                billsClearButton.setText(resourceMap.getString("billsClearButton.text")); // NOI18N
                billsClearButton.setAutoscrolls(true);
                billsClearButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
                billsClearButton.setFocusable(false);
                billsClearButton.setMaximumSize(new java.awt.Dimension(100, 23));
                billsClearButton.setMinimumSize(new java.awt.Dimension(100, 23));
                billsClearButton.setName("billsClearButton"); // NOI18N
                billsClearButton.setPreferredSize(new java.awt.Dimension(100, 23));
                billsClearButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsClearButtonenterBillsButtonsClicked(evt);
                        }
                });
                jPanel224.add(billsClearButton);
                billsClearButton.setBounds(100, 0, 100, 23);

                billsAddLineButton.setText(resourceMap.getString("billsAddLineButton.text")); // NOI18N
                billsAddLineButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
                billsAddLineButton.setFocusable(false);
                billsAddLineButton.setMaximumSize(new java.awt.Dimension(110, 23));
                billsAddLineButton.setMinimumSize(new java.awt.Dimension(110, 23));
                billsAddLineButton.setName("billsAddLineButton"); // NOI18N
                billsAddLineButton.setPreferredSize(new java.awt.Dimension(110, 23));
                billsAddLineButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mousePressed(java.awt.event.MouseEvent evt) {
                                billsAddLineButtonMousePressed(evt);
                        }
                });
                billsAddLineButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsAddLineButtonActionPerformed(evt);
                        }
                });
                jPanel224.add(billsAddLineButton);
                billsAddLineButton.setBounds(0, 30, 100, 23);

                billsDeleteLineButton.setText(resourceMap.getString("billsDeleteLineButton.text")); // NOI18N
                billsDeleteLineButton.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
                billsDeleteLineButton.setFocusable(false);
                billsDeleteLineButton.setMaximumSize(new java.awt.Dimension(110, 23));
                billsDeleteLineButton.setMinimumSize(new java.awt.Dimension(110, 23));
                billsDeleteLineButton.setName("billsDeleteLineButton"); // NOI18N
                billsDeleteLineButton.setPreferredSize(new java.awt.Dimension(110, 23));
                billsDeleteLineButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsDeleteLineButtonMouseClicked(evt);
                        }
                });
                jPanel224.add(billsDeleteLineButton);
                billsDeleteLineButton.setBounds(100, 30, 100, 23);

                enterBillsPanel3.add(jPanel224);

                jPanel195.setName("jPanel195"); // NOI18N
                jPanel195.setOpaque(false);
                jPanel195.setPreferredSize(new java.awt.Dimension(100, 106));
                jPanel195.setLayout(new javax.swing.BoxLayout(jPanel195, javax.swing.BoxLayout.LINE_AXIS));

                entryIdLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                entryIdLabel2.setMaximumSize(new java.awt.Dimension(72, 14));
                entryIdLabel2.setMinimumSize(new java.awt.Dimension(72, 14));
                entryIdLabel2.setName("entryIdLabel2"); // NOI18N
                entryIdLabel2.setPreferredSize(new java.awt.Dimension(72, 14));
                jPanel195.add(entryIdLabel2);

                billsVendorId.setName("billsVendorId"); // NOI18N
                jPanel195.add(billsVendorId);

                enterBillsPanel3.add(jPanel195);

                jPanel209.add(enterBillsPanel3);

                jPanel225.setMaximumSize(new java.awt.Dimension(32767, 200));
                jPanel225.setMinimumSize(new java.awt.Dimension(872, 150));
                jPanel225.setName("jPanel225"); // NOI18N
                jPanel225.setPreferredSize(new java.awt.Dimension(872, 150));
                jPanel225.setLayout(new javax.swing.BoxLayout(jPanel225, javax.swing.BoxLayout.LINE_AXIS));

                jScrollPane1.setName("jScrollPane1"); // NOI18N

                billExpensesTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Account", "Amount", "Control #"
                        }
                ));
                billExpensesTable.setCellSelectionEnabled(true);
                billExpensesTable.setName("billExpensesTable"); // NOI18N
                billExpensesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                billExpensesTable.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent evt) {
                                billExpensesTableFocusGained(evt);
                        }
                });
                jScrollPane1.setViewportView(billExpensesTable);

                jPanel225.add(jScrollPane1);

                jPanel209.add(jPanel225);

                jPanel118.setMaximumSize(new java.awt.Dimension(300000, 500));
                jPanel118.setMinimumSize(new java.awt.Dimension(872, 250));
                jPanel118.setName("jPanel118"); // NOI18N
                jPanel118.setOpaque(false);
                jPanel118.setPreferredSize(new java.awt.Dimension(872, 250));
                jPanel118.setLayout(new javax.swing.BoxLayout(jPanel118, javax.swing.BoxLayout.LINE_AXIS));

                billsControlPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("billsControlPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
                billsControlPanel3.setFont(getFont());
                billsControlPanel3.setName("billsControlPanel3"); // NOI18N
                billsControlPanel3.setOpaque(false);
                billsControlPanel3.setLayout(new javax.swing.BoxLayout(billsControlPanel3, javax.swing.BoxLayout.Y_AXIS));

                jPanel226.setName("jPanel226"); // NOI18N
                jPanel226.setOpaque(false);
                jPanel226.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

                buttonGroup1.add(billsDueRadioButton1);
                billsDueRadioButton1.setSelected(true);
                billsDueRadioButton1.setText(resourceMap.getString("billsDueRadioButton1.text")); // NOI18N
                billsDueRadioButton1.setName("billsDueRadioButton1"); // NOI18N
                billsDueRadioButton1.setOpaque(false);
                billsDueRadioButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsDueRadioButton1billsTransactionsButtonsClicked(evt);
                        }
                });
                jPanel226.add(billsDueRadioButton1);

                buttonGroup1.add(billsPaidRadioButton1);
                billsPaidRadioButton1.setText(resourceMap.getString("billsPaidRadioButton1.text")); // NOI18N
                billsPaidRadioButton1.setName("billsPaidRadioButton1"); // NOI18N
                billsPaidRadioButton1.setOpaque(false);
                billsPaidRadioButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsPaidRadioButton1billsTransactionsButtonsClicked(evt);
                        }
                });
                jPanel226.add(billsPaidRadioButton1);

                buttonGroup1.add(billsCreditRadioButton);
                billsCreditRadioButton.setText(resourceMap.getString("billsCreditRadioButton.text")); // NOI18N
                billsCreditRadioButton.setName("billsCreditRadioButton"); // NOI18N
                billsCreditRadioButton.setOpaque(false);
                billsCreditRadioButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                billsCreditRadioButtonbillsTransactionsButtonsClicked(evt);
                        }
                });
                jPanel226.add(billsCreditRadioButton);

                billsSearchTextField.setMaximumSize(new java.awt.Dimension(100, 20));
                billsSearchTextField.setMinimumSize(new java.awt.Dimension(100, 20));
                billsSearchTextField.setName("billsSearchTextField"); // NOI18N
                billsSearchTextField.setPreferredSize(new java.awt.Dimension(100, 20));
                billsSearchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                        public void keyReleased(java.awt.event.KeyEvent evt) {
                                billsSearchTextFieldKeyReleased(evt);
                        }
                });
                jPanel226.add(billsSearchTextField);

                billsSearchButton.setText(resourceMap.getString("billsSearchButton.text")); // NOI18N
                billsSearchButton.setName("billsSearchButton"); // NOI18N
                billsSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                billsSearchButtonMouseClicked(evt);
                        }
                });
                jPanel226.add(billsSearchButton);

                billsControlPanel3.add(jPanel226);

                jPanel227.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
                jPanel227.setName("jPanel227"); // NOI18N
                jPanel227.setOpaque(false);
                jPanel227.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

                jCheckBox11.setSelected(true);
                jCheckBox11.setText(resourceMap.getString("jCheckBox11.text")); // NOI18N
                jCheckBox11.setName("jCheckBox11"); // NOI18N
                jCheckBox11.setOpaque(false);
                jCheckBox11.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jCheckBox11MouseClicked(evt);
                        }
                });
                jPanel227.add(jCheckBox11);

                jPanel228.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
                jPanel228.setName("jPanel228"); // NOI18N
                jPanel228.setOpaque(false);

                buttonGroup2.add(jRadioButton21);
                jRadioButton21.setSelected(true);
                jRadioButton21.setText(resourceMap.getString("jRadioButton21.text")); // NOI18N
                jRadioButton21.setName("jRadioButton21"); // NOI18N
                jRadioButton21.setOpaque(false);
                jPanel228.add(jRadioButton21);

                buttonGroup2.add(jRadioButton22);
                jRadioButton22.setText(resourceMap.getString("jRadioButton22.text")); // NOI18N
                jRadioButton22.setName("jRadioButton22"); // NOI18N
                jRadioButton22.setOpaque(false);
                jPanel228.add(jRadioButton22);

                buttonGroup2.add(jRadioButton23);
                jRadioButton23.setText(resourceMap.getString("jRadioButton23.text")); // NOI18N
                jRadioButton23.setName("jRadioButton23"); // NOI18N
                jRadioButton23.setOpaque(false);
                jPanel228.add(jRadioButton23);

                jLabel134.setText(resourceMap.getString("jLabel134.text")); // NOI18N
                jLabel134.setName("jLabel134"); // NOI18N
                jPanel228.add(jLabel134);

                jDateChooser20.setDateFormatString(resourceMap.getString("jDateChooser20.dateFormatString")); // NOI18N
                jDateChooser20.setName("jDateChooser20"); // NOI18N
                jPanel228.add(jDateChooser20);

                jLabel135.setText(resourceMap.getString("jLabel135.text")); // NOI18N
                jLabel135.setName("jLabel135"); // NOI18N
                jPanel228.add(jLabel135);

                jDateChooser21.setDateFormatString(resourceMap.getString("jDateChooser21.dateFormatString")); // NOI18N
                jDateChooser21.setName("jDateChooser21"); // NOI18N
                jPanel228.add(jDateChooser21);

                jButton49.setText(resourceMap.getString("jButton49.text")); // NOI18N
                jButton49.setName("jButton49"); // NOI18N
                jButton49.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jButton49refreshButtonsClicked(evt);
                        }
                });
                jPanel228.add(jButton49);

                jPanel227.add(jPanel228);

                billsControlPanel3.add(jPanel227);

                jPanel229.setName("jPanel229"); // NOI18N
                jPanel229.setOpaque(false);
                jPanel229.setLayout(new java.awt.CardLayout());

                apVendorBillDue.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bills", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont()));
                apVendorBillDue.setName("apVendorBillDue"); // NOI18N
                apVendorBillDue.setOpaque(false);
                apVendorBillDue.setLayout(null);

                jPanel231.setName("jPanel231"); // NOI18N
                jPanel231.setOpaque(false);
                jPanel231.setPreferredSize(new java.awt.Dimension(105, 161));
                jPanel231.setLayout(new javax.swing.BoxLayout(jPanel231, javax.swing.BoxLayout.PAGE_AXIS));

                payBill.setText(resourceMap.getString("payBill.text")); // NOI18N
                payBill.setMaximumSize(new java.awt.Dimension(105, 23));
                payBill.setMinimumSize(new java.awt.Dimension(105, 23));
                payBill.setName("payBill"); // NOI18N
                payBill.setPreferredSize(new java.awt.Dimension(105, 23));
                payBill.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                payBillbillButtonsClicked(evt);
                        }
                });
                jPanel231.add(payBill);

                editBill.setText(resourceMap.getString("editBill.text")); // NOI18N
                editBill.setMaximumSize(new java.awt.Dimension(105, 23));
                editBill.setMinimumSize(new java.awt.Dimension(105, 23));
                editBill.setName("editBill"); // NOI18N
                editBill.setPreferredSize(new java.awt.Dimension(105, 23));
                editBill.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                editBillbillButtonsClicked(evt);
                        }
                });
                jPanel231.add(editBill);

                deleteBill.setText(resourceMap.getString("deleteBill.text")); // NOI18N
                deleteBill.setMaximumSize(new java.awt.Dimension(105, 23));
                deleteBill.setMinimumSize(new java.awt.Dimension(105, 23));
                deleteBill.setName("deleteBill"); // NOI18N
                deleteBill.setPreferredSize(new java.awt.Dimension(105, 23));
                deleteBill.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                deleteBillbillButtonsClicked(evt);
                        }
                });
                jPanel231.add(deleteBill);

                jLabel25.setName("jLabel25"); // NOI18N
                jPanel231.add(jLabel25);

                selectedInvoices.setText(resourceMap.getString("selectedInvoices.text")); // NOI18N
                selectedInvoices.setToolTipText(resourceMap.getString("selectedInvoices.toolTipText")); // NOI18N
                selectedInvoices.setName("selectedInvoices"); // NOI18N
                jPanel231.add(selectedInvoices);

                jPanel96.setName("jPanel96"); // NOI18N
                jPanel96.setOpaque(false);
                jPanel96.setPreferredSize(new java.awt.Dimension(30, 92));
                jPanel96.setLayout(null);

                jLabel28.setText(resourceMap.getString("jLabel28.text")); // NOI18N
                jLabel28.setMaximumSize(new java.awt.Dimension(5, 14));
                jLabel28.setMinimumSize(new java.awt.Dimension(5, 14));
                jLabel28.setName("jLabel28"); // NOI18N
                jLabel28.setPreferredSize(new java.awt.Dimension(5, 14));
                jPanel96.add(jLabel28);
                jLabel28.setBounds(20, 0, 32, 30);

                amountInvoices.setName("amountInvoices"); // NOI18N
                jPanel96.add(amountInvoices);
                amountInvoices.setBounds(60, 0, 40, 30);

                jPanel231.add(jPanel96);

                apVendorBillDue.add(jPanel231);
                jPanel231.setBounds(10, 20, 120, 120);

                jScrollPane37.setName("jScrollPane37"); // NOI18N

                bottomBillsTable.setAutoCreateRowSorter(true);
                bottomBillsTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Select Bill", "Invoice #", "Vendor Name", "Amount", "Post Date", "Due Date", "Memo", "ID"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
                        };
                        boolean[] canEdit = new boolean [] {
                                true, false, false, false, false, false, false, false
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                bottomBillsTable.setFillsViewportHeight(true);
                bottomBillsTable.setName("bottomBillsTable"); // NOI18N
                bottomBillsTable.setOpaque(false);
                bottomBillsTable.getTableHeader().setReorderingAllowed(false);
                bottomBillsTable.setRowSelectionAllowed(true); 
                bottomBillsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                bottomBillsTable.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                bottomBillsTableMouseClicked(evt);
                        }
                });
                jScrollPane37.setViewportView(bottomBillsTable);
                bottomBillsTable.getColumnModel().getColumn(0).setMinWidth(55);
                bottomBillsTable.getColumnModel().getColumn(0).setPreferredWidth(55);
                bottomBillsTable.getColumnModel().getColumn(0).setMaxWidth(55);
                bottomBillsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title0")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(1).setMinWidth(130);
                bottomBillsTable.getColumnModel().getColumn(1).setPreferredWidth(130);
                bottomBillsTable.getColumnModel().getColumn(1).setMaxWidth(130);
                bottomBillsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title1")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title2")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(3).setMinWidth(60);
                bottomBillsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
                bottomBillsTable.getColumnModel().getColumn(3).setMaxWidth(60);
                bottomBillsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title3")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(4).setMinWidth(70);
                bottomBillsTable.getColumnModel().getColumn(4).setPreferredWidth(70);
                bottomBillsTable.getColumnModel().getColumn(4).setMaxWidth(70);
                bottomBillsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title4")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(5).setMinWidth(70);
                bottomBillsTable.getColumnModel().getColumn(5).setPreferredWidth(70);
                bottomBillsTable.getColumnModel().getColumn(5).setMaxWidth(70);
                bottomBillsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title5")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title6")); // NOI18N
                bottomBillsTable.getColumnModel().getColumn(7).setMinWidth(0);
                bottomBillsTable.getColumnModel().getColumn(7).setPreferredWidth(0);
                bottomBillsTable.getColumnModel().getColumn(7).setMaxWidth(0);
                bottomBillsTable.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("bottomBillsTable.columnModel.title7")); // NOI18N

                apVendorBillDue.add(jScrollPane37);
                jScrollPane37.setBounds(130, 10, 840, 250);

                jPanel229.add(apVendorBillDue, "card2");

                apVendorBillCredit.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("apVendorBillCredit.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
                apVendorBillCredit.setName("apVendorBillCredit"); // NOI18N
                apVendorBillCredit.setOpaque(false);
                apVendorBillCredit.setLayout(null);

                jPanel242.setName("jPanel242"); // NOI18N
                jPanel242.setOpaque(false);
                jPanel242.setLayout(new javax.swing.BoxLayout(jPanel242, javax.swing.BoxLayout.Y_AXIS));

                editBill1.setText(resourceMap.getString("editBill1.text")); // NOI18N
                editBill1.setMaximumSize(new java.awt.Dimension(105, 23));
                editBill1.setMinimumSize(new java.awt.Dimension(105, 23));
                editBill1.setName("editBill1"); // NOI18N
                editBill1.setPreferredSize(new java.awt.Dimension(105, 23));
                editBill1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                editBill1billButtonsClicked(evt);
                        }
                });
                jPanel242.add(editBill1);

                deleteBill1.setText(resourceMap.getString("deleteBill1.text")); // NOI18N
                deleteBill1.setMaximumSize(new java.awt.Dimension(105, 23));
                deleteBill1.setMinimumSize(new java.awt.Dimension(105, 23));
                deleteBill1.setName("deleteBill1"); // NOI18N
                deleteBill1.setPreferredSize(new java.awt.Dimension(105, 23));
                deleteBill1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                deleteBill1billButtonsClicked(evt);
                        }
                });
                jPanel242.add(deleteBill1);

                jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
                jLabel26.setMaximumSize(new java.awt.Dimension(50, 30));
                jLabel26.setMinimumSize(new java.awt.Dimension(50, 30));
                jLabel26.setName("jLabel26"); // NOI18N
                jLabel26.setPreferredSize(new java.awt.Dimension(50, 30));
                jPanel242.add(jLabel26);

                selectedInvoices1.setText(resourceMap.getString("selectedInvoices1.text")); // NOI18N
                selectedInvoices1.setName("selectedInvoices1"); // NOI18N
                jPanel242.add(selectedInvoices1);

        apVendorBillCredit.add(jPanel242);
        jPanel242.setBounds(10, 20, 105, 50);

                jScrollPane3.setName("jScrollPane3"); // NOI18N

                apVendorBillCreditTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Select Bill", "Invoice #", "Vendor Name", "Amount", "Post Date", "Memo", "ID"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
                apVendorBillCreditTable.setName("apVendorBillCreditTable"); // NOI18N
                apVendorBillCreditTable.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                apVendorBillCreditTableMouseClicked(evt);
                        }
                });
                jScrollPane3.setViewportView(apVendorBillCreditTable);
                apVendorBillCreditTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title0")); // NOI18N
                apVendorBillCreditTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title1")); // NOI18N
                apVendorBillCreditTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title2")); // NOI18N
                apVendorBillCreditTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title3")); // NOI18N
                apVendorBillCreditTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title4")); // NOI18N
                apVendorBillCreditTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title6")); // NOI18N
                apVendorBillCreditTable.getColumnModel().getColumn(6).setMinWidth(0);
                apVendorBillCreditTable.getColumnModel().getColumn(6).setPreferredWidth(0);
                apVendorBillCreditTable.getColumnModel().getColumn(6).setMaxWidth(0);
                apVendorBillCreditTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("apVendorBillCreditTable.columnModel.title7")); // NOI18N

        apVendorBillCredit.add(jScrollPane3);
        jScrollPane3.setBounds(130, 10, 840, 190);

                jPanel229.add(apVendorBillCredit, "card2");

                apVendorBillPaid.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("apVendorBillPaid.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, getFont())); // NOI18N
                apVendorBillPaid.setName("apVendorBillPaid"); // NOI18N
                apVendorBillPaid.setOpaque(false);
                apVendorBillPaid.setLayout(null);

                jScrollPane27.setName("jScrollPane27"); // NOI18N

                paidBillsTable.setAutoCreateRowSorter(true);
                paidBillsTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Invoice #", "Vendor Name", "Amount", "Control #", "Post Date", "Due Date", "Paid Date", "Memos", "Check #", "Account", "ID"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
                        };
                        boolean[] canEdit = new boolean [] {
                                false, false, false, false, false, false, false, false, false, false, false
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                paidBillsTable.setFillsViewportHeight(true);
                paidBillsTable.setName("paidBillsTable"); // NOI18N
                paidBillsTable.getTableHeader().setReorderingAllowed(false);
                jScrollPane27.setViewportView(paidBillsTable);
                paidBillsTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title0")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title1")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(2).setMinWidth(60);
                paidBillsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
                paidBillsTable.getColumnModel().getColumn(2).setMaxWidth(60);
                paidBillsTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title2")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(3).setMinWidth(0);
                paidBillsTable.getColumnModel().getColumn(3).setPreferredWidth(0);
                paidBillsTable.getColumnModel().getColumn(3).setMaxWidth(0);
                paidBillsTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title3")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(4).setMinWidth(80);
                paidBillsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
                paidBillsTable.getColumnModel().getColumn(4).setMaxWidth(80);
                paidBillsTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title4")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(5).setMinWidth(80);
                paidBillsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
                paidBillsTable.getColumnModel().getColumn(5).setMaxWidth(80);
                paidBillsTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title5")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(6).setMinWidth(80);
                paidBillsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
                paidBillsTable.getColumnModel().getColumn(6).setMaxWidth(80);
                paidBillsTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title6")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title7")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(8).setMinWidth(60);
                paidBillsTable.getColumnModel().getColumn(8).setPreferredWidth(60);
                paidBillsTable.getColumnModel().getColumn(8).setMaxWidth(60);
                paidBillsTable.getColumnModel().getColumn(8).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title9")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(9).setMinWidth(50);
                paidBillsTable.getColumnModel().getColumn(9).setPreferredWidth(50);
                paidBillsTable.getColumnModel().getColumn(9).setMaxWidth(50);
                paidBillsTable.getColumnModel().getColumn(9).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title10")); // NOI18N
                paidBillsTable.getColumnModel().getColumn(10).setMinWidth(0);
                paidBillsTable.getColumnModel().getColumn(10).setPreferredWidth(0);
                paidBillsTable.getColumnModel().getColumn(10).setMaxWidth(0);
                paidBillsTable.getColumnModel().getColumn(10).setHeaderValue(resourceMap.getString("paidBillsTable.columnModel.title8")); // NOI18N

        apVendorBillPaid.add(jScrollPane27);
        jScrollPane27.setBounds(130, 10, 840, 190);

                jPanel4.setName("jPanel4"); // NOI18N
                jPanel4.setOpaque(false);
                jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.PAGE_AXIS));

                editPaidBillJButton.setText(resourceMap.getString("editPaidBillJButton.text")); // NOI18N
                editPaidBillJButton.setMaximumSize(new java.awt.Dimension(108, 23));
                editPaidBillJButton.setMinimumSize(new java.awt.Dimension(108, 23));
                editPaidBillJButton.setName("editPaidBillJButton"); // NOI18N
                editPaidBillJButton.setPreferredSize(new java.awt.Dimension(108, 23));
                editPaidBillJButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                editPaidBillJButtonMouseClicked(evt);
                        }
                });
                jPanel4.add(editPaidBillJButton);

                deletePaidBillJButton.setText(resourceMap.getString("deletePaidBillJButton.text")); // NOI18N
                deletePaidBillJButton.setMaximumSize(new java.awt.Dimension(108, 23));
                deletePaidBillJButton.setMinimumSize(new java.awt.Dimension(108, 23));
                deletePaidBillJButton.setName("deletePaidBillJButton"); // NOI18N
                deletePaidBillJButton.setPreferredSize(new java.awt.Dimension(108, 23));
                deletePaidBillJButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                deletePaidBillJButtonMouseClicked(evt);
                        }
                });
                jPanel4.add(deletePaidBillJButton);

        apVendorBillPaid.add(jPanel4);
        jPanel4.setBounds(10, 20, 110, 50);

                jPanel229.add(apVendorBillPaid, "card3");

                billsControlPanel3.add(jPanel229);

                jPanel118.add(billsControlPanel3);

                jPanel209.add(jPanel118);

                jPanel157.add(jPanel209);

                add(jPanel157, java.awt.BorderLayout.CENTER);
        }// </editor-fold>//GEN-END:initComponents

    public void setAmountSum(double sumVal) {
        amountSumValue = sumVal;
    }

    public double getAmountSum() {
        return amountSumValue;
    }

        private void billsAllVendorsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsAllVendorsCheckboxActionPerformed
            TableColumn selectBillCol = bottomBillsTable.getColumnModel().getColumn(0);
            if (billsAllVendorsCheckbox.isSelected()) {
                billsVendorInfo.setVisible(false);
                billsVendorsTableaccountsPayableVendorsClicked(null);
                //bottomBillsTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                selectBillCol.setMinWidth(0);
                selectBillCol.setMaxWidth(0);
                selectBillCol.setPreferredWidth(0);
            } else if (!billsAllVendorsCheckbox.isSelected()) {
                billsVendorInfo.setVisible(true);
                billsVendorsTableaccountsPayableVendorsClicked(null);
                //bottomBillsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                selectBillCol.setMinWidth(60);
                selectBillCol.setMaxWidth(60);
                selectBillCol.setPreferredWidth(60);
            }
            bottomBillsTable.repaint();
            bottomBillsTable.revalidate();
        }//GEN-LAST:event_billsAllVendorsCheckboxActionPerformed

        private void billsVendorsTableaccountsPayableVendorsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsVendorsTableaccountsPayableVendorsClicked
            try {
                String billsDueQuery = null;
                String billsPaidQuery = null;
                String creditsQuery = null;
                String filterVendors = "";
                String filterDateType = "";
                boolean good = false;
                if (!billsAllVendorsCheckbox.isSelected()) {
                    billsVendorsTable.setEnabled(true);

                    //System.out.println("All Vendors Not Check and Vendors Table Enabled");
                    //System.out.println("billsVendorsTable.getSelectedRowCount(): " + billsVendorsTable.getSelectedRowCount());

                    if (billsVendorsTable.getSelectedRowCount() == 1 && billsVendorsTable.isEnabled()) {
                        good = true;
                        int vendorsId = Integer.parseInt(billsVendorsTable.getValueAt(billsVendorsTable.getSelectedRow(), billsVendorsTable.getColumnModel().getColumnIndex("ID")).toString());
                        String query = "Select "
                                + "VendorId,VendorName,VendorAddress,VendorCity,VendorState,VendorZip,VendorEmail,VendorPhone,VendorContact "
                                + "From VendorListTable "
                                + "Where VendorId = '" + vendorsId + "' ";

                        //System.out.println("qry = " + query);
                        ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                        if (rs.next()) {
                            billVendorName.setText(rs.getString("VendorName"));
                            billVendor.setSelectedItem(rs.getString("VendorName"));
                            billVendorAddress.setText(rs.getString("VendorAddress"));
                            billVendorAddress2.setText(rs.getString("VendorCity") + ", " + rs.getString("VendorState") + ", " + rs.getString("VendorZip"));
                            billVendorEmail.setText(rs.getString("VendorEmail"));
                            billVendorPhone.setText(rs.getString("VendorPhone"));
                            billVendorConatct.setText(rs.getString("VendorContact"));
                            billsVendorId.setText(rs.getString("VendorId"));
                        }

                        rs.getStatement().close();

                        billsDueQuery = "Select "
                                + "'',ReferenceNumber, B.VendorName, "
                                + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,DateDue,Memo, EntryID "
                                + "From AccountingGLTable A "
                                + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                                + "LEFT JOIN AccountingCOATable C "
                                + "ON A.AccountNumber = C.AccountNumber "
                                + "Where " + filterVendors + "GLType IN ('Bill') "
                                + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                                + "AND DatePaid IS NULL "
                                + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' Order by EntryId Desc ";

                        //System.out.println("x Due bill query is 1 : " + billsDueQuery);

                        creditsQuery = "Select "
                                + "'',ReferenceNumber, B.VendorName, "
                                + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,Memo, EntryID "
                                + "From AccountingGLTable A "
                                + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                                + "LEFT JOIN AccountingCOATable C "
                                + "ON A.AccountNumber = C.AccountNumber "
                                + "Where " + filterVendors + "GLType IN ('Credit') "
                                + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                                + "AND DatePaid IS NULL "
                                + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                        //System.out.println("z query is 2 : " + creditsQuery);

                        billsPaidQuery = "Select "
                                + "ReferenceNumber,VendorName,CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),"
                                + "ControlNumber,PostDate,DateDue,DatePaid,A.Memo,D.CheckNumber, D.BankAccount, A.EntryID "
                                + "From AccountingGLTable A "
                                + "LEFT Join VendorListTable B ON B.VendorID = A.ControlNumber "
                                + "LEFT JOIN AccountingCOATable C "
                                + "ON A.AccountNumber = C.AccountNumber "
                                + "LEFT JOIN AccountingCBTable D ON D.PaidTo = B.VendorName AND D.EntryID LIKE '%'+ReferenceNumber+'%' "
                                + "Where " + filterVendors + "GLType IN ('Bill','Credit') "
                                + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                                + "AND DatePaid IS NOT NULL "
                                + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                        //System.out.println("billsPaidQuery: " + billsPaidQuery);

                    } else {

                        // if Selected Vendor is selected and there are no selected or highlighted vendor in the billsVendorsTable.
                        // clean up the billsPaidTable and billsDueTable                        
                        AccountingUtil.clearTable(paidBillsTable);
                        AccountingUtil.clearTable(bottomBillsTable);
                    }
                } else if (billsAllVendorsCheckbox.isSelected()) {                    
                    good = true;
                    billsVendorsTable.setEnabled(false);

                    billsDueQuery = "Select "
                            + "'',ReferenceNumber, B.VendorName,"
                            + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,DateDue,Memo, B.VendorID "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B ON B.VendorID = A.ControlNumber "
                            + "LEFT JOIN AccountingCOATable C "
                            + "ON A.AccountNumber = C.AccountNumber "
                            + "WHERE " + filterVendors + "GLType IN ('Bill') "
                            + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                            + "AND DatePaid IS NULL "
                            + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' Order by EntryId Desc ";

                    creditsQuery = "Select "
                            + "'',ReferenceNumber, B.VendorName,"
                            + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,Memo, EntryID "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B ON B.VendorID = A.ControlNumber "
                            + "LEFT JOIN AccountingCOATable C "
                            + "ON A.AccountNumber = C.AccountNumber "
                            + "WHERE " + filterVendors + "GLType IN ('Credit') "
                            + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                            + "AND DatePaid IS NULL "
                            + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                    billsPaidQuery = "Select "
                            + "ReferenceNumber, VendorName,CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),ControlNumber,"
                            + "PostDate,DateDue,DatePaid,Memo,EntryID "
                            + "From AccountingGLTable A "
                            + "LEFT Join VendorListTable B ON B.VendorID = A.ControlNumber "
                            + "LEFT JOIN AccountingCOATable C "
                            + "ON A.AccountNumber = C.AccountNumber "
                            + "Where " + filterVendors + "GLType IN ('Bill','Credit') "
                            + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                            + "AND DatePaid IS NOT NULL "
                            + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";
                }

                if (good) {
                    //System.out.println("Here 2");
                    ResultSet rs;
                    DefaultTableModel aModel = null;
                    ResultSetMetaData rsmd = null;

                    int colNo = 0;
                    if (billsDueRadioButton1.isSelected()) {
                        //System.out.println("Due RadioButton is selected...");
                        // Only run if Bills Paid radio button is selected
                        // jRadioButton3.setVisible(false); // disable Paid Date radio button
                        aModel = (DefaultTableModel) bottomBillsTable.getModel();
                        AccountingUtil.clearTableModel(aModel);
                        rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(billsDueQuery);
                        

                        rsmd = rs.getMetaData();
                        colNo = rsmd.getColumnCount();

                        int postDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Post Date");
                        int dueDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Due Date");
                        boolean hasRecords = false;

                        while (rs.next()) {
                            Object[] values = new Object[colNo];
                            for (int i = 0; i < colNo; i++) {
                                if (i > 0) {

                                    if (i == postDateColumn) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject("PostDate"));

                                    } else if (i == dueDateColumn) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DateDue"));
                                    } else {
                                        values[i] = rs.getObject(i + 1);
                                    }
                                }
                            }                            
                            aModel.addRow(values);
                            hasRecords = true;
                        }
                        if (!hasRecords) {
                            // clear the Lables for Total and Invoices at bottom
                            jLabel25.setVisible(false);
                            selectedInvoices.setVisible(false);
                            jPanel96.setVisible(false);
                        }
                        rs.getStatement().close();

                    } else if (billsPaidRadioButton1.isSelected()) {

                        // Only run if Bills Paid radio button is selected
                        jRadioButton23.setVisible(true); // enable Paid Date radio button
                        aModel = (DefaultTableModel) paidBillsTable.getModel();
                        AccountingUtil.clearTableModel(aModel);
                        rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(billsPaidQuery);

                        rsmd = rs.getMetaData();
                        colNo = rsmd.getColumnCount();

                        int postDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Post Date");
                        int dueDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Due Date");
                        int paidDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Paid Date");

                        while (rs.next()) {
                            Object[] values = new Object[colNo];
                            for (int i = 0; i < colNo; i++) {

                                if (i == postDateColumn) {

                                    values[i] = AccountingUtil.dateFormat1.format(rs.getObject("PostDate"));
                                } else if (i == dueDateColumn) {
                                    if (rs.getObject("DateDue") != null) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DateDue"));
                                    }
                                } else if (i == paidDateColumn) {
                                    if (rs.getObject("DatePaid") != null) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DatePaid"));
                                    }
                                } else {
                                    values[i] = rs.getObject(i + 1);
                                }
                            }
                            aModel.addRow(values);
                        }

                        rs.getStatement().close();

                    } else if (billsCreditRadioButton.isSelected()) {

                        // Only run if Credit radio button is selected
                        aModel = (DefaultTableModel) apVendorBillCreditTable.getModel();
                        AccountingUtil.clearTableModel(aModel);

                        //System.out.println("START: ");
                        rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(creditsQuery);
                        //System.out.println("QYERY: " + creditsQuery);
                        //System.out.println("END");

                        rsmd = rs.getMetaData();
                        colNo = rsmd.getColumnCount();

                        //System.out.println("1");
                        int postDateColumn = AccountingUtil.getColumnByName(apVendorBillCreditTable, "Post Date");

                        //System.out.println("2");
                        while (rs.next()) {
                            Object[] values = new Object[colNo];
                            for (int i = 0; i < colNo; i++) {
                                if (i > 0) {
                                    if (i == postDateColumn) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject("PostDate"));
                                    } else {
                                        values[i] = rs.getObject(i + 1);
                                    }
                                }
                            }
                            aModel.addRow(values);
                        }
                        rs.getStatement().close();
                    }
                }

            } catch (SQLException e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            } catch (Exception e) {
                dms.DMSApp.displayMessage(this, "MESSAGE: " + e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            }
        }//GEN-LAST:event_billsVendorsTableaccountsPayableVendorsClicked

    // Method to display Bill Generated against vendor, on Create Bill Button Action
    public void displayGeneratedBills(String vendor) {
        try {
            String x = null;
            String y = null;
            String filterVendors = "";
            boolean good = false;
            if (!billsAllVendorsCheckbox.isSelected()) {
                billsVendorsTable.setEnabled(true);
                int vendorsId = 0;
                if (billsVendorId.getText().toString().isEmpty()) {
                    billsVendorId.setText("1");
                }
                vendorsId = Integer.parseInt(billsVendorId.getText().toString());
                if (vendorsId > 0) {
                    good = true;
                    ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet("Select "
                            + "VendorId,VendorName,VendorAddress,VendorCity,VendorState,VendorZip,VendorEmail,VendorPhone,VendorContact From VendorListTable "
                            + "Where VendorId = '" + vendorsId + "' ");

                    if (rs.next()) {
                        billVendorName.setText(rs.getString("VendorName"));
                        billVendor.setSelectedItem(rs.getString("VendorName"));
                        billVendorAddress.setText(rs.getString("VendorAddress"));
                        billVendorAddress2.setText(rs.getString("VendorCity") + ", " + rs.getString("VendorState") + ", " + rs.getString("VendorZip"));
                        billVendorEmail.setText(rs.getString("VendorEmail"));
                        billVendorPhone.setText(rs.getString("VendorPhone"));
                        billVendorConatct.setText(rs.getString("VendorContact"));
                        billsVendorId.setText(rs.getString("VendorId"));

                    }

                    rs.getStatement().close();

                    //if (selectedVendorRadioButton.isSelected() && !allVendorsRadioButton.isSelected()) {
                    x = "Select "
                            + "'',ReferenceNumber, B.VendorName, "
                            + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,DateDue,Memo, EntryID "
                            + "From AccountingGLTable A "
                            + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                            + "LEFT JOIN AccountingCOATable C "
                            + "ON A.AccountNumber = C.AccountNumber "
                            + "Where " + filterVendors + "GLType = 'Bill' "
                            + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                            + "AND DatePaid IS NULL "
                            + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                    y = "Select "
                            + "ReferenceNumber,VendorName,CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),"
                            + "ControlNumber,PostDate,DateDue,DatePaid,Memo,EntryID "
                            + "From AccountingGLTable A "
                            + "LEFT Join VendorListTable B ON B.VendorID = A.ControlNumber "
                            + "LEFT JOIN AccountingCOATable C "
                            + "ON A.AccountNumber = C.AccountNumber "
                            + "Where " + filterVendors + "GLType = 'Bill' "
                            + "AND ControlNumber = '" + billsVendorId.getText() + "' "
                            + "AND DatePaid IS NOT NULL "
                            + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";


                } else {

                    // if Selected Vendor is selected and there are no selected or highlighted vendor in the billsVendorsTable.
                    // clean up the billsPaidTable and billsDueTable

                    AccountingUtil.clearTable(paidBillsTable);
                    AccountingUtil.clearTable(bottomBillsTable);

                }
            } else if (billsAllVendorsCheckbox.isSelected()) {

                good = true;
                billsVendorsTable.setEnabled(false);

                x = "Select "
                        + "'',ReferenceNumber, B.VendorName,"
                        + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,DateDue,Memo, EntryID "
                        + "From AccountingGLTable A "
                        + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                        + "LEFT JOIN AccountingCOATable C "
                        + "ON A.AccountNumber = C.AccountNumber "
                        + "Where " + filterVendors + "GLType = 'Bill' "
                        + "AND DatePaid IS NULL "
                        + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

                y = "Select "
                        + "ReferenceNumber, VendorName,CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),ControlNumber,"
                        + "PostDate,DateDue,DatePaid,Memo,EntryID "
                        + "From AccountingGLTable A "
                        + "LEFT Join VendorListTable B ON B.VendorID = A.ControlNumber "
                        + "LEFT JOIN AccountingCOATable C "
                        + "ON A.AccountNumber = C.AccountNumber "
                        + "Where " + filterVendors + "GLType = 'Bill' "
                        + "AND DatePaid IS NOT NULL "
                        + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";

            }

            if (good) {
                ResultSet rs;
                DefaultTableModel aModel = null;
                ResultSetMetaData rsmd = null;

                int colNo = 0;
                if (billsDueRadioButton1.isSelected()) {
                    // Only run if Bills Paid radio button is selected
                    //                  jRadioButton3.setVisible(false); // disable Paid Date radio button
                    aModel = (DefaultTableModel) bottomBillsTable.getModel();
                    AccountingUtil.clearTableModel(aModel);
                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(x);

                    rsmd = rs.getMetaData();
                    colNo = rsmd.getColumnCount();

                    int postDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Post Date");
                    int dueDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Due Date");

                    while (rs.next()) {
                        Object[] values = new Object[colNo];
                        for (int i = 0; i < colNo; i++) {
                            if (i > 0) {

                                if (i == postDateColumn) {
                                    values[i] = AccountingUtil.dateFormat1.format(rs.getObject("PostDate"));
                                } else if (i == dueDateColumn) {
                                    values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DateDue"));
                                } else {
                                    values[i] = rs.getObject(i + 1);
                                }
                            }
                        }
                        aModel.addRow(values);
                    }

                    rs.getStatement().close();

                } else if (billsPaidRadioButton1.isSelected()) {
                    // Only run if Bills Paid radio button is selected
                    jRadioButton23.setVisible(true); // enable Paid Date radio button
                    aModel = (DefaultTableModel) paidBillsTable.getModel();
                    AccountingUtil.clearTableModel(aModel);
                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(y);

                    rsmd = rs.getMetaData();
                    colNo = rsmd.getColumnCount();

                    int postDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Post Date");
                    int dueDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Due Date");
                    int paidDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Paid Date");

                    while (rs.next()) {
                        Object[] values = new Object[colNo];
                        for (int i = 0; i < colNo; i++) {

                            if (i == postDateColumn) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject("PostDate"));
                            } else if (i == dueDateColumn) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DateDue"));
                            } else if (i == paidDateColumn) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DatePaid"));
                            } else {
                                values[i] = rs.getObject(i + 1);
                            }
                        }
                        aModel.addRow(values);
                    }

                    rs.getStatement().close();

                }
            }

        } catch (SQLException e) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            dms.DMSApp.displayMessage(this, "MESSAGE: " + e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

        private void billsVendorsTableaccountsPayableVendorsKeyed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_billsVendorsTableaccountsPayableVendorsKeyed
            billsVendorsTableaccountsPayableVendorsClicked(null);
        }//GEN-LAST:event_billsVendorsTableaccountsPayableVendorsKeyed

        private void billCreditCheckMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billCreditCheckMouseClicked
            if (billCreditCheck.isSelected()) {
                jLabel138.setText("Credit Due :");
                jLabel137.setText("Credit Date :");
                billsCreateButton.setText("Create Credit");
                billsClearButton.setText("Clear Credit");
                //jPanel230.setVisible(true);
                //jPanel232.setVisible(false);
            } else {

                jLabel138.setText("Amount Due :");
                jLabel137.setText("Due Date :");
                billsCreateButton.setText("Create Bill");
                billsClearButton.setText("Clear Bill");
                //jPanel230.setVisible(true);
                //jPanel232.setVisible(false);
            }
        }//GEN-LAST:event_billCreditCheckMouseClicked

        private void billVendorItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_billVendorItemStateChanged
        }//GEN-LAST:event_billVendorItemStateChanged

        private void billInvoiceInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_billInvoiceInputMethodTextChanged
        }//GEN-LAST:event_billInvoiceInputMethodTextChanged

        private void billAmountInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_billAmountInputMethodTextChanged

            billLinesDiff.setValue(Integer.parseInt(billAmount.getValue().toString()) - Integer.parseInt(billTotalExpenses.getValue().toString()));

            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    double sum = 0.00;
                    TableCellListener tcl = (TableCellListener) e.getSource();
                    int rowCount = billExpensesTable.getRowCount();
                    int modifiedRowNo = 0;
                    String amountForDisplay = "";
                    modifiedRowNo = tcl.getRow();
                    for (int i = 0; i < rowCount; i++) {
                        if (billExpensesTable.getValueAt(i, 1) != null && !billExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
                            if (!AccountingUtil.displayNumeric(billExpensesTable.getValueAt(i, 1).toString())) {
                                dms.DMSApp.displayMessage(billExpensesTable, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                                billExpensesTable.setCellSelectionEnabled(true);
                                billExpensesTable.changeSelection(modifiedRowNo, billExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                                billExpensesTable.requestFocus();
                            }
                            if (AccountingUtil.displayNumeric(billExpensesTable.getValueAt(i, 1).toString())) {
                                if (billExpensesTable.getValueAt(i, 1).toString().contains(",")) {
                                    sum += Double.parseDouble(billExpensesTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                                } else {
                                    sum += Double.parseDouble(billExpensesTable.getValueAt(i, 1).toString());
                                }
                                if (modifiedRowNo == i) {   // change amount format only for current row
                                    amountForDisplay = AccountingUtil.formatAmountForDisplay(billExpensesTable.getValueAt(modifiedRowNo, 1).toString());
                                    billExpensesTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
                                }
                            }
                        }
                    }

                    Double diff = Double.parseDouble(billAmount.getValue().toString()) - sum;

                    billTotalExpenses.setValue(sum);
                    billLinesDiff.setValue(diff);
                }
            };

            TableCellListener tcl = new TableCellListener(billExpensesTable, action);
        }//GEN-LAST:event_billAmountInputMethodTextChanged

        private void billsCreateButtonenterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsCreateButtonenterBillsButtonsClicked
            if (billsVendorsTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(this, "Please select vendor first", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (billAmount.getText().trim().isEmpty()) {
                dms.DMSApp.displayMessage(this, "Please input value for Amount Due field", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            boolean good = true;
            createBillClicked = true;
            int billExpensesRowCount = billExpensesTable.getRowCount();
            if (billExpensesRowCount == 0) {
                dms.DMSApp.displayMessage(this, "Please add at least one bill", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (billAmount.getText() != null && billTotalExpenses.getText() != null) {
                String billAmountStringVal = billAmount.getText().toString();
                String billTotalExpensesStringVal = billTotalExpenses.getText().toString();
                billAmountStringVal = billAmountStringVal.replaceAll(",", "");
                billTotalExpensesStringVal = billTotalExpensesStringVal.replaceAll(",", "");
                Double billAmountDoubleVal = Double.parseDouble(billAmountStringVal);
                Double billTotalExpensesDoubleVal = Double.parseDouble(billTotalExpensesStringVal);
                int retval = Double.compare(billAmountDoubleVal, billTotalExpensesDoubleVal);
                if (retval != 0) {
                    dms.DMSApp.displayMessage(this, "The amount and total lines amount do not match", dms.DMSApp.WARNING_MESSAGE);
                    return;
                }
            }
            if (billAmount.getValue() == null && billTotalExpenses.getValue() == null) {
                dms.DMSApp.displayMessage(this, "The amount and total lines amount cannot be empty", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (billAmount.getValue() != null && billTotalExpenses.getValue() == null) {
                dms.DMSApp.displayMessage(this, "The total lines amount cannot be empty", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (billAmount.getText().isEmpty()) {
                dms.DMSApp.displayMessage(this, "The amount field cannot be empty", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (billInvoice.getText() == null || "".equals(billInvoice.getText())) {
                dms.DMSApp.displayMessage(this, "An invoice number is required", dms.DMSApp.WARNING_MESSAGE);
                return;
            }

            if (billExpensesRowCount > 0) {
                int accountColumn = AccountingUtil.getColumnByName(billExpensesTable, "Account");
                int controlColumn = AccountingUtil.getColumnByName(billExpensesTable, "Control #");
                int amountColumn = AccountingUtil.getColumnByName(billExpensesTable, "Amount");

                for (int i = 0; i <= billExpensesTable.getRowCount() - 1; i++) {
                    String accountNumber = null;
                    Boolean controlled;
                    if (billExpensesTable.getValueAt(i, accountColumn) != null) {
                        accountNumber = billExpensesTable.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.split("-")[0];
                    }
                    if (accountNumber == null || accountNumber.isEmpty()) {
                        dms.DMSApp.displayMessage(this, "Please select an account first.", JOptionPane.WARNING_MESSAGE);
                        return;
                    } else {
                        controlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                        //System.out.println("isControlled : " + controlled);
                        if (AccountingUtil.isControlNumberRequired(billExpensesTable)) {
                            return;
                        }
                    }
                }
            } else {
                good = false;
            }

            if (good) {
                String accountNumber;
                String accountsPayableNumber = "300";
                Double amountVal = null;
                String controlNumber = null; // journalSalesCustomer.getText();
                String referenceNumber; //journalSalesDeal.getText();
                String memo;
                Double debit;
                Double credit;
                String glType;

                amountVal = Double.parseDouble(billAmount.getText().replace(",", ""));
                controlNumber = billVendor.getSelectedItem().toString();
                setVendorName(controlNumber);
                referenceNumber = billInvoice.getText();
                int vendorId = Integer.parseInt(billsVendorsTable.getValueAt(billsVendorsTable.getSelectedRow(), billsVendorsTable.getColumnModel().getColumnIndex("ID")).toString());
                memo = billMemo.getText().toString();

                // Check if VendorId (ControlNumber) and Invoice#(RegferenceNumber) already exists in DB
                boolean invoiceAlreadyExistsForVendor = false;
                if (controlNumber != null && referenceNumber != null) {
                    invoiceAlreadyExistsForVendor = AccountingUtil.IsInvoiceExistsForVendor(billsVendorId.getText().toString(), referenceNumber);
                    if (invoiceAlreadyExistsForVendor) {
                        dms.DMSApp.displayMessage(billExpensesTable, "Invoice# " + referenceNumber + " already exists for Vendor :" + controlNumber, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (!invoiceAlreadyExistsForVendor) {         // Generate a New Bill (if vendor and invoice# doesn't exists in DB)
                    sql = new String[1];

                    if (billCreditCheck.isSelected()) {
                        glType = "Credit";
                    } else {
                        glType = "Bill";
                    }

                    // ACCOUNTS PAYABLE
                    // If Credit is selected (i.e. Credit Case)
                    if (billCreditCheck.isSelected()) {

                        sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, "
                                + "PostDate, DateDue, Memo, GLType, LotName, Class) VALUES('" + accountsPayableNumber + "', '" + amountVal + "', '0.00', "
                                + " '" + billsVendorId.getText() + "', '" + referenceNumber + "', '"
                                + AccountingUtil.dateFormat.format(billPostDate.getDate()) + "', '"
                                + AccountingUtil.dateFormat.format(billDueDate.getDate()) + "', '" + memo + "', '" + glType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + billClass.getSelectedItem().toString() + "'"
                                + ")";
                    } else {
                        sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, "
                                + "PostDate, DateDue, Memo, GLType, LotName, Class) VALUES('" + accountsPayableNumber + "', '0.00', '"
                                + amountVal + "', '" + billsVendorId.getText() + "', '" + referenceNumber + "', '"
                                + AccountingUtil.dateFormat.format(billPostDate.getDate()) + "', '"
                                + AccountingUtil.dateFormat.format(billDueDate.getDate()) + "', '" + memo + "', '" + glType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + billClass.getSelectedItem().toString() + "'"
                                + ")";
                    }
                    System.out.println("SQL 1: " + sql[0]);

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                    int accountColumn = AccountingUtil.getColumnByName(billExpensesTable, "Account");
                    int amountColumn = AccountingUtil.getColumnByName(billExpensesTable, "Amount");
                    int controlColumn = AccountingUtil.getColumnByName(billExpensesTable, "Control #");
                    int referenceColumn = AccountingUtil.getColumnByName(billExpensesTable, "Invoice #");
                    int vendorColumn = AccountingUtil.getColumnByName(billExpensesTable, "Vendor #");

                    referenceNumber = billInvoice.getText();

                    for (int i = 0; i <= billExpensesTable.getRowCount() - 1; i++) {

                        accountNumber = null;
                        amountVal = 0.00;
                        controlNumber = null;
                        memo = null;

                        if (billExpensesTable.getValueAt(i, accountColumn) != null) {
                            accountNumber = billExpensesTable.getValueAt(i, accountColumn).toString();
                            accountNumber = accountNumber.split("-")[0];
                        }
                        if (billExpensesTable.getValueAt(i, amountColumn) != null && !billExpensesTable.getValueAt(i, amountColumn).toString().isEmpty()) {
                            if (billExpensesTable.getValueAt(i, amountColumn).toString().contains(",")) {
                                String beforeComma = billExpensesTable.getValueAt(i, amountColumn).toString().substring(0, billExpensesTable.getValueAt(i, amountColumn).toString().indexOf(","));
                                String afterComma = billExpensesTable.getValueAt(i, amountColumn).toString().substring(billExpensesTable.getValueAt(i, amountColumn).toString().indexOf(",") + 1, billExpensesTable.getValueAt(i, amountColumn).toString().length());
                                String finalAmount = beforeComma + afterComma;
                                amountVal = Double.parseDouble(finalAmount.toString());
                            } else {
                                amountVal = Double.parseDouble(billExpensesTable.getValueAt(i, amountColumn).toString());
                            }

                        } else {
                            amountVal = 0.00;
                        }
                        if (billExpensesTable.getValueAt(i, controlColumn) != null) {
                            controlNumber = billExpensesTable.getValueAt(i, controlColumn).toString();
                        } else {
                            controlNumber = "";
                        }

                        if (billCreditCheck.isSelected()) {
                            debit = 0.00;
                            credit = amountVal;
                            glType = "Credit Line";
                        } else {
                            debit = amountVal;
                            credit = 0.00;
                            glType = "Bill Line";
                        }

                        memo = billsVendorId.getText().toString();

                        sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, DateDue, Memo, GLType, LotName, Class) VALUES("
                                + "'" + accountNumber + "', '" + debit + "', '" + credit + "', '" + controlNumber + "', '" + referenceNumber + "', "
                                + "'" + AccountingUtil.dateFormat.format(billPostDate.getDate()) + "', '"
                                + AccountingUtil.dateFormat.format(billDueDate.getDate()) + "', '" + memo + "', '" + glType + "', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + billClass.getSelectedItem().toString() + "'"
                                + ")";

                        System.out.println("SQL 2: " + sql[0]);

                        dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
                    }
                }
                billPostDate.setDate(Calendar.getInstance().getTime());
                billDueDate.setDate(Calendar.getInstance().getTime());
                billAmount.setValue(0.00);
                billTotalExpenses.setValue(0.00);
                billLinesDiff.setValue(0.00);
                billMemo.setText("");
                billInvoice.setText("");

                DefaultTableModel aModel = (DefaultTableModel) billExpensesTable.getModel();

                AccountingUtil.clearTableModel(aModel);

                reloadVendorsList();
                setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                displayGeneratedBills(getVendorName());     // Display bill generated at bottom table

                if (billCreditCheck.isSelected()) {
                    billsCreditRadioButton.setSelected(true);
                    billsDueRadioButton1.setSelected(false);
                    apVendorBillDue.setVisible(false);
                    apVendorBillPaid.setVisible(false);
                    apVendorBillCredit.setVisible(true);
                    jPanel242.setVisible(true);
                    billsVendorsTableaccountsPayableVendorsKeyed(null);
                }
            }
        }//GEN-LAST:event_billsCreateButtonenterBillsButtonsClicked

        private void billsClearButtonenterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsClearButtonenterBillsButtonsClicked
            if (billCreditCheck.isSelected()) {
                System.out.println("Credit Selected..");
            }
            DefaultTableModel aModel = (DefaultTableModel) billExpensesTable.getModel();
            AccountingUtil.clearTableModel(aModel);
        }//GEN-LAST:event_billsClearButtonenterBillsButtonsClicked

        private void billsAddLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsAddLineButtonActionPerformed
            DefaultTableModel aModel = (DefaultTableModel) billExpensesTable.getModel();
            try {
                int rowCount = billExpensesTable.getRowCount();
                if (rowCount == 0 && createBillClicked) {
                    rowNo = 0;
                    addNewRow(aModel, AccountingUtil.getAllAccountsFull()[0].toString());
                    return;
                }
                if (isRowRemoved) {
                    rowNo = rowCount - 1;
                    if (rowNo < 0) {
                        rowNo = 0;
                    }
                    isRowRemoved = false;
                }
                try {
                    if (billExpensesTable.getRowCount() > 0) {
                        System.out.println("2222222222222 - rowno = " + rowNo);
                        if (billExpensesTable.getValueAt(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Account")) != null) {
                            account = billExpensesTable.getValueAt(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Account")).toString();
                            System.out.println("account is = " + account);
                        }
                        if (billExpensesTable.getValueAt(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Amount")) != null) {
                            amount = billExpensesTable.getValueAt(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Amount")).toString();
                            System.out.println("Amount : " + amount);
                        }
                        if (account == null) {
                            dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.WARNING_MESSAGE);
                            billExpensesTable.setCellSelectionEnabled(true);
                            billExpensesTable.changeSelection(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Account"), false, false);
                            billExpensesTable.requestFocus();
                            return;
                        } else {
                            if (AccountingUtil.isControlNumberRequired(billExpensesTable)) {
                                return;
                            }
                        }
                        if (amount == null || amount.isEmpty()) {
                            dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.WARNING_MESSAGE);
                            billExpensesTable.setCellSelectionEnabled(true);
                            billExpensesTable.changeSelection(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                            billExpensesTable.requestFocus();
                        } else if (amount != null && !amount.isEmpty() && !amount.equals("")) {
                            if (!displayNumeric(amount)) {
                                dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                                billExpensesTable.setCellSelectionEnabled(true);
                                billExpensesTable.changeSelection(rowNo, billExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                                billExpensesTable.requestFocus();
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ae) {
                    Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ae);
                }
                // check to insert a row
                if (account != null && amount != null && displayNumeric(amount)) {
                    addNewRow(aModel, account);
                    rowNo++;
                }
            } catch (Exception e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            }
        }//GEN-LAST:event_billsAddLineButtonActionPerformed

        private void billsDueRadioButton1billsTransactionsButtonsClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsDueRadioButton1billsTransactionsButtonsClicked
            apVendorBillDue.setVisible(true);
            apVendorBillPaid.setVisible(false);
            apVendorBillCredit.setVisible(false);
            billsVendorsTableaccountsPayableVendorsKeyed(null);
        }//GEN-LAST:event_billsDueRadioButton1billsTransactionsButtonsClicked

        private void billsPaidRadioButton1billsTransactionsButtonsClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsPaidRadioButton1billsTransactionsButtonsClicked
            apVendorBillDue.setVisible(false);
            apVendorBillPaid.setVisible(true);
            apVendorBillCredit.setVisible(false);
            billsVendorsTableaccountsPayableVendorsKeyed(null);
        }//GEN-LAST:event_billsPaidRadioButton1billsTransactionsButtonsClicked

        private void billsSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsSearchButtonMouseClicked

            try {
                String sql = "";

                String searchFilter = "";
                String billsSearch = billsSearchTextField.getText();
                int billsSearchInt = 0;                
                ResultSet rs = null;
                
                if (dms.util.StringUtil.isInteger(billsSearch)) {
                    billsSearchInt = Integer.parseInt(billsSearch);
                    searchFilter = "ReferenceNumber = '" + billsSearchInt + "' ";
                } else if (!dms.util.StringUtil.isInteger(billsSearch)) {
                    searchFilter = "Memo LIKE '%" + billsSearch + "%' ";
                }

                // if search in null
                //System.out.println("billsSearch = " + billsSearch);
                if(billsSearch == null || billsSearch.isEmpty() || billsSearch.equals("")){                    
                    billsVendorsTableaccountsPayableVendorsClicked(null);
                }else{                    
                    if (billsDueRadioButton1.isSelected()) {
                        sql = "Select "
                                + "'',ReferenceNumber, B.VendorName, "
                                + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,DateDue,Memo, EntryID "
                                + "From AccountingGLTable A "
                                + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                                + "LEFT JOIN AccountingCOATable C "
                                + "ON A.AccountNumber = C.AccountNumber "
                                + "Where "
                                + searchFilter
                                + "AND GLType = 'Bill' "                            
                                + "AND DatePaid IS NULL "
                                + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";
                    
                        //System.out.println("inside qry 1 = " + sql);
                        rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                        DefaultTableModel aModel = (DefaultTableModel) bottomBillsTable.getModel();
                        AccountingUtil.clearTableModel(aModel);

                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNo = rsmd.getColumnCount();
                        int postDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Post Date");
                        int dueDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Due Date");

                        while (rs.next()) {
                            Object[] values = new Object[colNo];
                            for (int i = 0; i < colNo; i++) {
                                if (i > 0) {

                                    if (i == postDateColumn) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                                    } else if (i == dueDateColumn) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                                    } else {
                                        values[i] = rs.getObject(i + 1);
                                    }
                                }
                            }
                            aModel.addRow(values);
                        }
                    } else if(billsCreditRadioButton.isSelected()){
                        sql = "Select "
                                + "'',ReferenceNumber, B.VendorName, "
                                + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),PostDate,Memo, EntryID "
                                + "From AccountingGLTable A "
                                + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                                + "LEFT JOIN AccountingCOATable C "
                                + "ON A.AccountNumber = C.AccountNumber "
                                + "Where "
                                + searchFilter
                                + "AND GLType = 'Credit' "                            
                                + "AND DatePaid IS NULL "
                                + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";
                    
                        //System.out.println("inside qry 2 = " + sql);
                        rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                        DefaultTableModel aModel = (DefaultTableModel) apVendorBillCreditTable.getModel();
                        AccountingUtil.clearTableModel(aModel);

                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNo = rsmd.getColumnCount();
                        int postDateColumn = AccountingUtil.getColumnByName(apVendorBillCreditTable, "Post Date");                        

                        while (rs.next()) {
                            Object[] values = new Object[colNo];
                            for (int i = 0; i < colNo; i++) {
                                if (i > 0) {

                                    if (i == postDateColumn) {
                                        values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                                    } else {
                                        values[i] = rs.getObject(i + 1);
                                    }
                                }
                            }
                            aModel.addRow(values);
                        }
                    } else if (billsPaidRadioButton1.isSelected()) {
                        sql = "Select "
                                + "A.ReferenceNumber, B.VendorName, "
                                + "CAST(ROUND(Debit+Credit,2) AS NUMERIC(10,2)),A.ControlNumber,A.PostDate,A.DateDue,A.DatePaid,A.Memo,D.CheckNumber,D.BankAccount, A.EntryID "
                                + "From AccountingGLTable A "
                                + "Left Join VendorListTable B ON B.VendorID = A.ControlNumber "
                                + "LEFT JOIN AccountingCOATable C "
                                + "ON A.AccountNumber = C.AccountNumber "
                                + "LEFT JOIN AccountingCBTable D ON D.PaidTo = B.VendorName " 
                                + "Where "
                                + searchFilter
                                + "AND A.GLType = 'Bill' " 
                                + "AND A.DatePaid IS NOT NULL "
                                + "AND A.LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'";
                    
                        //System.out.println("inside qry 3 = " + sql);
                        rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                        DefaultTableModel aModel = (DefaultTableModel) paidBillsTable.getModel();
                        AccountingUtil.clearTableModel(aModel);

                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colNo = rsmd.getColumnCount();
                        int postDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Post Date");
                        int dueDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Due Date");
                        int paidDateColumn = AccountingUtil.getColumnByName(paidBillsTable, "Paid Date");
                        
                        while (rs.next()) {
                            Object[] values = new Object[colNo];
                            for (int i = 0; i < colNo; i++) {                                                                
                                if (i == postDateColumn) {
                                     values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                                } else if (i == dueDateColumn) {
                                     values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                                } else if (i == paidDateColumn) {
                                    if (rs.getObject("DatePaid") != null) {
                                         values[i] = AccountingUtil.dateFormat1.format(rs.getObject("DatePaid"));
                                    }
                                } else {
                                     values[i] = rs.getObject(i + 1);                                     
                                }                                
                            }
                            aModel.addRow(values);
                        }
                    }
                    rs.getStatement().close();
                }                
            } catch (SQLException e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            } catch (Exception e) {
                dms.DMSApp.displayMessage(this, "MESSAGE: " + e.getLocalizedMessage(), dms.DMSApp.ERROR_MESSAGE);
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            }

        }//GEN-LAST:event_billsSearchButtonMouseClicked

        private void jButton49refreshButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton49refreshButtonsClicked
            // TODO add your handling code here:
        }//GEN-LAST:event_jButton49refreshButtonsClicked

        private void payBillbillButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_payBillbillButtonsClicked

            if (bottomBillsTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(bottomBillsTable, "Please select atleast 1 bill to pay", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int selectedRowCount = bottomBillsTable.getSelectedRowCount();

            if (selectedRowCount == 1) {
                System.out.println("selected row is 1 ");
                try {
                    ResultSet rs;
                    DefaultTableModel aModel = null;
                    ResultSetMetaData rsmd;
                    String sqlQuery;
                    int colNo = 0;
                    String vendorNumber = billsVendorId.getText().toString();
                    if (billsAllVendorsCheckbox.isSelected()) {
                        vendorNumber = bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(),
                                bottomBillsTable.getColumnModel().getColumnIndex("ID")).toString();
                    }

                    String invoiceNumber = bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(),
                            bottomBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();

                    paybillsAmount.setValue(Double.parseDouble(bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(), bottomBillsTable.getColumnModel().getColumnIndex("Amount")).toString()));

                    paybillsPayto.setSelectedItem(bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(),
                            bottomBillsTable.getColumnModel().getColumnIndex("Vendor Name")));

                    paybillsClass.setSelectedItem(aModel);

                    String memo = bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(), bottomBillsTable.getColumnModel().getColumnIndex("Memo")).toString();
                    paybillsMemo.setText(memo);

                    Calendar c = Calendar.getInstance();
                    if (paybillsDate.getDate() == null) {
                        paybillsDate.setDate(c.getTime());
                    }

                    String classSQL = "SELECT Class "
                            + "FROM AccountingGLTable WHERE GLType = 'Bill Line' AND ReferenceNumber = '" + invoiceNumber + "'";

                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(classSQL);
                    while (rs.next()) {
                        paybillsClass.setSelectedItem(rs.getString("Class"));
                    }

                    sqlQuery = "SELECT "
                            + "'', A.ReferenceNumber, EntryId, B.VendorName, A.AccountNumber, "
                            + "CAST(ROUND(Debit,2) AS NUMERIC(10,2)), A.ControlNumber, "
                            + "A.PostDate, A.DateDue, A.Memo, A.Class "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B "
                            + "ON A.Memo = CAST(B.VendorID AS VARCHAR) "
                            + "WHERE GLType = 'Bill Line' "
                            + "AND ReferenceNumber = '" + invoiceNumber + "' "
                            + "AND Memo = '" + vendorNumber + "'";

                    //System.out.println("SQL for Edit/pay Bill is: "+sql);

                    aModel = (DefaultTableModel) paybillsDueTable1.getModel();
                    AccountingUtil.clearTableModel(aModel);

                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);

                    //paybillsClass.setSelectedItem(rs.getString("Class"));


                    rsmd = rs.getMetaData();
                    colNo = rsmd.getColumnCount();

                    int x = 0;
                    while (rs.next()) {

                        Object[] values = new Object[colNo];

                        for (int i = 0; i < colNo; i++) {
                            if (i == 0) {
                                values[i] = true;
                            } else if (i == AccountingUtil.getColumnByName(paybillsDueTable1, "Post Date")) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                            } else if (i == AccountingUtil.getColumnByName(paybillsDueTable1, "Due Date")) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                            } else if (i == 5) {
                                values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject(i + 1).toString());
                            } else {
                                values[i] = rs.getObject(i + 1);
                            }
                        }
                        aModel.addRow(values);
                    }

                    rs.getStatement().close();
                    // load default BankAccount
                    paybillsBankCombo.setSelectedItem("202 - Cash in Bank-BOA");
                    paybillBankSelected();
                    paybillsPopup.setVisible(true);
                    reloadVendorsList();
                    setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                } catch (SQLException ex) {
                    Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (selectedRowCount > 1) {
                System.out.println("selected rows > 1 ");
                try {
                    int[] selectedRows = bottomBillsTable.getSelectedRows();
                    int selectedRowsCount = bottomBillsTable.getSelectedRowCount();

                    String invoice = "";
                    for (int j = 0; j <= selectedRowsCount - 1; j++) {
                        String invoicePre;
                        invoicePre = bottomBillsTable.getValueAt(selectedRows[j], bottomBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();
                        invoice = invoice + "'" + invoicePre + "'";
                        if (j != selectedRowsCount - 1) {
                            invoice = invoice + ", ";
                        }
                    }

                    ResultSet rs;
                    DefaultTableModel aModel = null;
                    ResultSetMetaData rsmd = null;
                    String sql;
                    int colNo = 0;

                    String controlNumber = billsVendorId.getText().toString();

                    paybillsAmount.setValue(Double.parseDouble(amountInvoices.getText()));

                    paybillsPayto.setSelectedItem(bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(),
                            bottomBillsTable.getColumnModel().getColumnIndex("Vendor Name")));

                    String memo = bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(), bottomBillsTable.getColumnModel().getColumnIndex("Memo")).toString();
                    paybillsMemo.setText(memo);

                    Calendar c = Calendar.getInstance();
                    if (paybillsDate.getDate() == null) {
                        paybillsDate.setDate(c.getTime());
                    }

                    sql = "SELECT "
                            + "'', A.ReferenceNumber, EntryId, B.VendorName, A.AccountNumber, "
                            + "CAST(ROUND(Debit,2) AS NUMERIC(10,2)), A.ControlNumber, "
                            + "A.PostDate, A.DateDue, A.Memo "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B "
                            + "ON A.Memo = CAST(B.VendorID AS VARCHAR) "
                            + "WHERE GLType = 'Bill Line' "
                            + "AND memo = '" + controlNumber + "' "
                            + "AND ReferenceNumber IN (" + invoice + ")";

                    /*
                     sql = "SELECT "
                     + "ReferenceNumber, EntryId, CAST(ROUND(Debits+Credit,2) AS NUMERIC(10,2)) Amount, "
                     + "GLType "
                     + "FROM AccountingGLTable "
                     + "WHERE GLType IN ('Bill', 'Vendor Credit') "
                     + "AND ReferenceNumber IN (" + invoice + ") "
                     + "AND controlNumber ='" + controlNumber + "'";
                     */
                    //System.out.println("Test sql = " + sql);
                    aModel = (DefaultTableModel) paybillsDueTable1.getModel();
                    AccountingUtil.clearTableModel(aModel);

                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                    rsmd = rs.getMetaData();
                    colNo = rsmd.getColumnCount();

                    int x = 0;
                    while (rs.next()) {

                        Object[] values = new Object[colNo];

                        for (int i = 0; i < colNo; i++) {

                            if (i == 0) {

                                values[i] = true;
                            } else if (i == AccountingUtil.getColumnByName(paybillsDueTable1, "Post Date")) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                            } else if (i == AccountingUtil.getColumnByName(paybillsDueTable1, "Due Date")) {
                                values[i] = AccountingUtil.dateFormat1.format(rs.getObject(i + 1));
                            } else {

                                values[i] = rs.getObject(i + 1);
                            }
                        }
                        aModel.addRow(values);
                    }

                    rs.getStatement().close();

                    paybillsBankCombo.setSelectedItem("202 - Cash in Bank-BOA");
                    paybillBankSelected();
                    paybillsPopup.setVisible(true);
                    reloadVendorsList();
                    setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                } catch (SQLException ex) {
                    Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }//GEN-LAST:event_payBillbillButtonsClicked

        private void editBillbillButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editBillbillButtonsClicked
            try {
                if (bottomBillsTable.getSelectedRowCount() == 0) {
                    dms.DMSApp.displayMessage(bottomBillsTable, "Please select atleast 1 bill to edit", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (bottomBillsTable.getSelectedRowCount() > 1) {
                    dms.DMSApp.displayMessage(bottomBillsTable, "Please select only 1 bill to edit", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (bottomBillsTable.getSelectedRowCount() == 1) {
                    String sql;
                    int invoiceNumberColumn = AccountingUtil.getColumnByName(bottomBillsTable, "Invoice #");
                    int vendorColumn = AccountingUtil.getColumnByName(bottomBillsTable, "Vendor Name");

                    int controlNumber = Integer.parseInt(billsVendorId.getText().toString());
                    String referenceNumber = bottomBillsTable.getValueAt(
                            bottomBillsTable.getSelectedRow(), bottomBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();

                    sql = "SELECT B.VendorName, A.ReferenceNumber, A.Memo, CAST(ROUND(A.Credit,2) AS NUMERIC(10,2)) AS Credit, A.PostDate, A.DateDue, A.Class "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B "
                            + "ON (A.ControlNumber = B.VendorID) "
                            + "WHERE GLType = 'Bill' "
                            + "AND ControlNumber = '" + controlNumber + "' "
                            + "AND ReferenceNumber = '" + referenceNumber + "'";

                    //System.out.println("sql:1" + sql);

                    ResultSet rs;
                    String vendor = "";
                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                    if (rs.next()) {
                        vendor = rs.getString("VendorName");
                        editBillInvoice.setText(rs.getString("ReferenceNumber"));
                        editBillMemo.setText(rs.getString("Memo"));
                        editBillAmount.setText(AccountingUtil.formatAmountForDisplay(rs.getString("Credit")));
                        editBillPostDate.setDate(AccountingUtil.getUtilDateFormatFromSqlDate(rs.getDate("PostDate")));
                        editBillDueDate.setDate(AccountingUtil.getUtilDateFormatFromSqlDate(rs.getDate("DateDue")));
                        editClass.setSelectedItem(rs.getString("Class"));
                    }

                    editBillTotalExpenses.setText(editBillAmount.getText());
                    editBillLinesDiff.setText("0.00");
                    //System.out.println("billsVendorId.getText() = " + billsVendorId.getText());

                    // To populate the selected value in drop-down
                    ComboBoxModel model = editBillVendor.getModel();
                    int size = model.getSize();
                    for (int i = 0; i < size; i++) {
                        Object element = model.getElementAt(i);
                        //System.out.println("Element at " + i + " = " + element);
                        if (vendor.equals(element.toString())) {
                            //System.out.println("e = " + element);
                            editBillVendor.setSelectedItem(element);
                        }
                    }
                    billsVendorId1.setText(billsVendorId.getText());
                    //System.out.println("billsVendorId1.getText() after set = " + billsVendorId1.getText());
                    ////////////////////////////////////////////////////////

                    sql = "SELECT AccountNumber,CAST(ROUND(Debit,2) AS NUMERIC(10,2)), ControlNumber "
                            + "FROM AccountingGLTable "
                            + "WHERE GLType = 'Bill Line' "
                            + "AND ReferenceNumber = '" + referenceNumber + "' "
                            + "AND Memo = '" + controlNumber + "'";

                    DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable.getModel();
                    AccountingUtil.clearTableModel(aModel);

                    //System.out.println("sql:2" + sql);

                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

                    ResultSetMetaData rsmd = null;
                    rsmd = rs.getMetaData();
                    int colNo = rsmd.getColumnCount();
                    int accountNumber = 0;
                    Object accounts[] = null;
                    while (rs.next()) {

                        Object[] values = new Object[colNo];

                        for (int i = 0; i < colNo; i++) {
                            accountNumber = rs.getInt("AccountNumber");
                            if (i == 0) {
                                values[i] = AccountingUtil.getAccountFull(accountNumber);
                            } else if (i == 1) {
                                values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject(i + 1).toString());
                            } else {
                                values[i] = rs.getObject(i + 1);
                            }
                            accounts = AccountingUtil.getFullAccountsForInvoice(accountNumber);
                        }
                        for (int k = 0; k < accounts.length; k++) {
                            if (accounts[k] != null) {
                                accountsToBeShownList.add(accounts[k]);
                            }
                        }
                        accounts = null;
                        aModel.addRow(values);
                    }

                    rs.getStatement().close();
                    setVendorId(controlNumber);
                    setInvoiceNo(referenceNumber);
                    editBillsPopup.setVisible(true);
                }

            } catch (SQLException ex) {
                Logger.getLogger(AccountsPayablePanel.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            addComboBoxesToTableForEditExpensesTable();
        }//GEN-LAST:event_editBillbillButtonsClicked

    public void setVendorId(int vendorID) {
        this.vendorId = vendorID;
    }

    public int getVendorId() {
        return this.vendorId;
    }

    public void setInvoiceNo(String invoiceNumber) {
        this.invoiceNo = invoiceNumber;
    }

    public String getInvoiceNo() {
        return this.invoiceNo;
    }

        private void deleteBillbillButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteBillbillButtonsClicked
            sql = new String[2];
            boolean sureToDelete = false;
            int selectedRowCount = bottomBillsTable.getSelectedRowCount();

            if (bottomBillsTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(bottomBillsTable, "Please select atleast 1 bill to delete", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (bottomBillsTable.getSelectedRowCount() > 1) {
                dms.DMSApp.displayMessage(bottomBillsTable, "Please select only 1 bill to delete", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedRowCount == 1) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    sureToDelete = false;
                } else if (response == JOptionPane.YES_OPTION) {
                    sureToDelete = true;
                } else if (response == JOptionPane.CLOSED_OPTION) {
                    //System.out.println("JOptionPane closed");
                }

                if (sureToDelete) {
                    String invoice = bottomBillsTable.getValueAt(bottomBillsTable.getSelectedRow(), bottomBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();

                    String vendorId = billsVendorId.getText().toString();

                    // Delete the invoice
                    sql[0] = "DELETE FROM AccountingGLTable "
                            + "WHERE GLtype = 'Bill' "
                            + "AND ControlNumber = '" + vendorId + "' "
                            + "AND ReferenceNumber = '" + invoice + "'";

                    // Delete the invoice lines
                    sql[1] = "DELETE FROM AccountingGLTable "
                            + "WHERE GLtype = 'Bill Line' "
                            + "AND Memo = '" + vendorId + "' "
                            + "AND ReferenceNumber = '" + invoice + "'";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
                    billsVendorsTableaccountsPayableVendorsClicked(null);
                    reloadVendorsList();
                    setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                    jLabel25.setVisible(false);
                    selectedInvoices.setVisible(false);
                    jLabel28.setVisible(false);
                    amountInvoices.setVisible(false);
                }
            }
        }//GEN-LAST:event_deleteBillbillButtonsClicked

        private void paybillsAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paybillsAmountActionPerformed
            // TODO add your handling code here:
        }//GEN-LAST:event_paybillsAmountActionPerformed

        private void paybillsBankCombobankSelected(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_paybillsBankCombobankSelected
            paybillBankSelected();
        }//GEN-LAST:event_paybillsBankCombobankSelected

        private void paybillsBankComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_paybillsBankComboItemStateChanged
            paybillBankSelected();
        }//GEN-LAST:event_paybillsBankComboItemStateChanged

        private void paybillsCancelButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paybillsCancelButton
            paybillsPopup.dispose();
        }//GEN-LAST:event_paybillsCancelButton

        private void paybillsPayandPrintsaveCheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paybillsPayandPrintsaveCheckButtonsClicked
            if (paybillsBankCombo.getSelectedItem() == "") {
                dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
            } else {
                // pay the bill first
                paybillsPayButton(evt);

                // gather parameters for check
                bankAccountNo = Integer.parseInt(
                        paybillsBankCombo.getSelectedItem().toString().split("-")[0].replaceAll("\\s+", ""));
                checkNo = Integer.parseInt(paybillsCheckNumber.getText());
                // print checks

                printBillPayCheck(checkNo, bankAccountNo);
            }
        }//GEN-LAST:event_paybillsPayandPrintsaveCheckButtonsClicked

        private void paybillsPayButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paybillsPayButton
            boolean checkForDuplicate = true;
            if (paybillsBankCombo.getSelectedItem() != "") {
                int[] selectedRows = paybillsDueTable1.getSelectedRows();
                sql = new String[4];
                String bankNumber = "" + paybillsBankCombo.getSelectedItem();
                String checkMemo = "";
                String entryId = "";
                int id = 0;
                Double amount = 0.00;
                String vendorIDFromDB = "";
                String accountsPayableNumber = "300";
                String invoice = "";
                int[] selectedRows1 = bottomBillsTable.getSelectedRows();
                int selectedInvoiceCount = bottomBillsTable.getSelectedRowCount();
                String invoicePre = "";
                String quoteInvoice = "";

                bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
                bankNumber = bankNumber.replaceAll("\\s+", "");
                Calendar c = Calendar.getInstance();

                vendorIDFromDB = billsVendorId.getText().toString();

//						if (selectedRows.length > 1) {
//								checkMemo = "Multi-Items:";
//						} else if (selectedRows.length == 1) {
//								entryId = Integer.parseInt(paybillsDueTable1.getValueAt(paybillsDueTable1.getSelectedRow(),
//										paybillsDueTable1.getColumnModel().getColumnIndex("ID")).toString());
//						}

                for (int j = 0; j <= selectedInvoiceCount - 1; j++) {
                    invoicePre = bottomBillsTable.getValueAt(selectedRows1[j],
                            bottomBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();
                    invoice = invoice + invoicePre;
                    quoteInvoice = quoteInvoice + "'" + invoicePre + "'";

                    if (j != selectedInvoiceCount - 1) {
                        invoice = invoice + ", ";
                        quoteInvoice = quoteInvoice + ", ";
                    }

                }

                entryId = invoice;

                checkMemo = checkMemo + " " + paybillsMemo.getText();

                // Check for Duplicate Check#
                if (paybillsCheckNumber.getText() == null || paybillsCheckNumber.getText().toString().isEmpty()) {
                    dms.DMSApp.displayMessage(this, "The check# is blank ", dms.DMSApp.WARNING_MESSAGE);
                    return;
                }
                boolean isCheckNoAlradyExists = false;
                String orignalCheckNumber = String.valueOf(getCheckNo());
                String newCheckNumber = paybillsCheckNumber.getText().toString();
                if (newCheckNumber.equals(orignalCheckNumber)) {
                    checkForDuplicate = false;
                }
                //System.out.println("checkForDup = " + checkForDuplicate);

                if (checkForDuplicate) {
                    isCheckNoAlradyExists = isCheckNumberAlreadyExists(Integer.parseInt(newCheckNumber.toString()));
                }
                if (isCheckNoAlradyExists) {
                    dms.DMSApp.displayMessage(this, "The check# " + newCheckNumber + " already exists ", dms.DMSApp.WARNING_MESSAGE);
                    return;
                } else {
                    // ACCOUNTS PAYABLE
                    sql[0] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber,Debit,Credit,ControlNumber,ReferenceNumber,PostDate,Memo,LotName,GLType,Class) "
                            + "VALUES ('" + accountsPayableNumber + "', '" + paybillsAmount.getText() + "', '0.00', "
                            + "'" + vendorIDFromDB + "', "
                            + "'" + paybillsCheckNumber.getText() + "', "
                            + "'" + AccountingUtil.dateFormat.format(paybillsDate.getDate()) + "', "
                            + "'" + checkMemo + "', '" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'Bill Payment Check - Vendor', '" + paybillsClass.getSelectedItem().toString() + "'"
                            + ")";
                    System.out.println("AP--" + sql[0]);

                    // BANK ACCOUNT
                    sql[1] = "INSERT INTO AccountingGLTable "
                            + "(AccountNumber,Debit,Credit,ControlNumber,ReferenceNumber,PostDate,Memo,LotName,GLType,Class) "
                            + "VALUES ('" + bankNumber + "', '0.00', '" + paybillsAmount.getText() + "', "
                            + "'" + vendorIDFromDB + "', "
                            + "'" + paybillsCheckNumber.getText() + "', "
                            + "'" + AccountingUtil.dateFormat.format(paybillsDate.getDate()) + "', "
                            + "'" + checkMemo + "', '" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                            + "'Bill Payment Check - Vendor', '" + paybillsClass.getSelectedItem().toString() + "'"
                            + ")";
                    System.out.println("BA------- " + sql[1]);

                    // CHECKBOOK TABLE
                    sql[2] = "INSERT INTO AccountingCBTable "
                            + "(EntryID, BankAccount,PaidTo,Amount,CheckNumber,Date,WrittenDate,Memo,LotName,Status, Type) "
                            + "VALUES ('" + entryId + "', '" + bankNumber + "', '"
                            + paybillsPayto.getSelectedItem().toString() + "', "
                            + "'" + paybillsAmount.getText() + "', '" + paybillsCheckNumber.getText() + "', "
                            + "'" + AccountingUtil.dateFormat.format(paybillsDate.getDate()) + "', '" + AccountingUtil.dateFormat.format(c.getTime()) + "', "
                            + "'" + checkMemo + "', '" + dms.DMSApp.getApplication().getCurrentlotName() + "','Posted', 'Bill Payment Check - Vendor'"
                            + ")";
                    System.out.println("CB------ " + sql[2]);

                    // Update AccountingGLTable with Check Paid Date
                    sql[3] = "UPDATE AccountingGLTable "
                            + "SET DatePaid = '" + AccountingUtil.dateFormat.format(paybillsDate.getDate()) + "' "
                            + "WHERE GLtype = 'Bill' "
                            + "AND ControlNumber = '" + vendorIDFromDB + "' "
                            + "AND ReferenceNumber IN (" + quoteInvoice + ")";
                    System.out.println("Update Bill: " + sql[3]);


                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                    // do not remove.
                    //if ((JButton) evt.getSource() == paybillsPayButton) {
                    billsVendorsTableaccountsPayableVendorsClicked(null);
                    //}

                    paybillsPopup.dispose();
                    reloadVendorsList();
                    setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                } // else of isCheckNumberAlreadyExists
            } else {
                dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
            }

        }//GEN-LAST:event_paybillsPayButton

    public boolean isCheckNumberAlreadyExists(int checkNumber) {
        boolean checkNoExists = false;
        try {
            String uniqueCheckNumberQuery = "select count(*) as Count from AccountingCBTable where CheckNumber = " + checkNumber;

            ResultSet rsUniqueCheckNumber = dms.DMSApp.getApplication().getDBConnection().getResultSet(uniqueCheckNumberQuery);

            while (rsUniqueCheckNumber.next()) {

                int count = rsUniqueCheckNumber.getInt("Count");
                if (count > 0) {
                    checkNoExists = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return checkNoExists;
    }

        private void paybillsCheckbookPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_paybillsCheckbookPanelComponentShown
        }//GEN-LAST:event_paybillsCheckbookPanelComponentShown

        private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        }//GEN-LAST:event_formComponentShown

        private void billAmountFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_billAmountFocusLost
            String billAmountStringVal = billAmount.getText();
            if (billAmountStringVal.contains(",")) {
                billAmountStringVal = billAmountStringVal.replaceAll(",", "");
            }

            String billAmountTotalExpensesStringVal = billTotalExpenses.getText();

            if (billAmountTotalExpensesStringVal.contains(",")) {
                billAmountTotalExpensesStringVal = billAmountTotalExpensesStringVal.replaceAll(",", "");
            }

            Double diff = Double.parseDouble(billAmountTotalExpensesStringVal) - Double.parseDouble(billAmountStringVal);

            if (diff < 0.00) {
                billLinesDiff.setForeground(Color.red);
            } else if (diff > 0.00) {
                billLinesDiff.setForeground(Color.green);
            } else {
                billLinesDiff.setForeground(Color.black);
            }

            billLinesDiff.setValue(diff);
        }//GEN-LAST:event_billAmountFocusLost

        private void editBillVendorItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_editBillVendorItemStateChanged
            // TODO add your handling code here:
        }//GEN-LAST:event_editBillVendorItemStateChanged

        private void editBillInvoiceInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_editBillInvoiceInputMethodTextChanged
            // TODO add your handling code here:
        }//GEN-LAST:event_editBillInvoiceInputMethodTextChanged

        private void editBillAmountFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editBillAmountFocusLost
            // TODO add your handling code here:
            String billAmountStringVal = editBillAmount.getText();
            if (billAmountStringVal.contains(",")) {
                billAmountStringVal = billAmountStringVal.replaceAll(",", "");
            }
            String billAmountTotalExpensesStringVal = editBillTotalExpenses.getText();
            if (billAmountTotalExpensesStringVal.contains(",")) {
                billAmountTotalExpensesStringVal = billAmountTotalExpensesStringVal.replaceAll(",", "");
            }
            if (!billAmountTotalExpensesStringVal.isEmpty() && !editBillAmount.getText().isEmpty()) {
                Double diff = Double.parseDouble(billAmountTotalExpensesStringVal) - Double.parseDouble(billAmountStringVal);

                if (diff < 0.00) {
                    editBillLinesDiff.setForeground(Color.red);
                } else if (diff > 0.00) {
                    editBillLinesDiff.setForeground(Color.green);
                } else {
                    editBillLinesDiff.setForeground(Color.black);
                }
                editBillLinesDiff.setValue(diff);
            }
        }//GEN-LAST:event_editBillAmountFocusLost

        private void editBillAmountInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_editBillAmountInputMethodTextChanged
            // TODO add your handling code here:
            editBillLinesDiff.setValue(Integer.parseInt(editBillAmount.getValue().toString()) - Integer.parseInt(editBillTotalExpenses.getValue().toString()));

            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    double sum = 0.00;
                    TableCellListener tcl = (TableCellListener) e.getSource();
                    int rowCount = editBillExpensesTable.getRowCount();
                    int modifiedRowNo = 0;
                    String amountForDisplay = "";
                    modifiedRowNo = tcl.getRow();
                    for (int i = 0; i < rowCount; i++) {
                        if (editBillExpensesTable.getValueAt(i, 1) != null && !editBillExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
                            if (!AccountingUtil.displayNumeric(editBillExpensesTable.getValueAt(i, 1).toString())) {
                                dms.DMSApp.displayMessage(editBillExpensesTable, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                                editBillExpensesTable.setCellSelectionEnabled(true);
                                editBillExpensesTable.changeSelection(modifiedRowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                                editBillExpensesTable.requestFocus();
                            }
                            if (AccountingUtil.displayNumeric(editBillExpensesTable.getValueAt(i, 1).toString())) {
                                if (editBillExpensesTable.getValueAt(i, 1).toString().contains(",")) {
                                    sum += Double.parseDouble(editBillExpensesTable.getValueAt(i, 1).toString().replaceAll(",", ""));
                                } else {
                                    sum += Double.parseDouble(editBillExpensesTable.getValueAt(i, 1).toString());
                                }
                                if (modifiedRowNo == i) {   // change amount format only for current row
                                    amountForDisplay = AccountingUtil.formatAmountForDisplay(editBillExpensesTable.getValueAt(modifiedRowNo, 1).toString());
                                    editBillExpensesTable.setValueAt(amountForDisplay, modifiedRowNo, 1);
                                }
                            }
                        }
                    }

                    Double diff = Double.parseDouble(editBillAmount.getValue().toString()) - sum;

                    editBillTotalExpenses.setValue(sum);
                    editBillLinesDiff.setValue(diff);
                }
            };

            TableCellListener tcl = new TableCellListener(editBillExpensesTable, action);
        }//GEN-LAST:event_editBillAmountInputMethodTextChanged

        private void billsCreateButton1enterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsCreateButton1enterBillsButtonsClicked
            boolean good = true;
            String billAmountVal = "";
            String totalExpensesAmount = "";
            int accountColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Account");
            int amountColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Amount");;
            int controlColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Control #");
            String accountNumber = null;
            String accountPayableNumber;
            Double amountVal;
            int editBillExpensesRowCount = editBillExpensesTable.getRowCount();
            if (editBillExpensesRowCount == 0) {
                dms.DMSApp.displayMessage(this, "Please add at least one bill", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (editBillAmount.getText().trim().isEmpty()) {
                dms.DMSApp.displayMessage(this, "Please input value for Amount Due field", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
            if (editBillAmount.getText() != null && editBillTotalExpenses.getText() != null) {
                String billAmountStringVal = editBillAmount.getText().toString();
                String billTotalExpensesStringVal = editBillTotalExpenses.getText().toString();
                billAmountStringVal = billAmountStringVal.replaceAll(",", "");
                billTotalExpensesStringVal = billTotalExpensesStringVal.replaceAll(",", "");
                Double billAmountDoubleVal = Double.parseDouble(billAmountStringVal);
                Double billTotalExpensesDoubleVal = Double.parseDouble(billTotalExpensesStringVal);
                int retval = Double.compare(billAmountDoubleVal, billTotalExpensesDoubleVal);
                if (retval != 0) {
                    dms.DMSApp.displayMessage(this, "The amount and total lines amount do not match", dms.DMSApp.WARNING_MESSAGE);
                    return;
                }
            }
            if (editBillAmount.getText() == null && editBillTotalExpenses.getText() == null) {
                good = false;
                dms.DMSApp.displayMessage(this, "The amount and total lines amount cannot be empty", dms.DMSApp.WARNING_MESSAGE);
            }
            if (editBillAmount.getText() != null && editBillTotalExpenses.getText() == null) {
                good = false;
                dms.DMSApp.displayMessage(this, "The total lines amount cannot be empty", dms.DMSApp.WARNING_MESSAGE);
            }
            if (editBillAmount.getText() == null && editBillTotalExpenses.getText() != null) {
                dms.DMSApp.displayMessage(this, "The amount field cannot be empty", dms.DMSApp.WARNING_MESSAGE);
            }
            if (editBillInvoice.getText() == null || "".equals(editBillInvoice.getText())) {
                good = false;
                dms.DMSApp.displayMessage(this, "An invoice number is required", dms.DMSApp.WARNING_MESSAGE);
            }
            if (editBillExpensesRowCount > 0) {
                for (int i = 0; i <= editBillExpensesTable.getRowCount() - 1; i++) {
                    if (editBillExpensesTable.getValueAt(i, accountColumn) != null) {
                        accountNumber = editBillExpensesTable.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.split("-")[0];
                    }
                    if (accountNumber != null) {
                        if (AccountingUtil.isControlNumberRequired(editBillExpensesTable)) {
                            return;
                        }
                    } else {
                        dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (editBillExpensesTable.getValueAt(i, amountColumn) == null || editBillExpensesTable.getValueAt(i, amountColumn).toString().isEmpty()) {
                        dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

            }

            if (good) {
                try {
                    String controlNumber = null;
                    String referenceNumber = null;
                    String memo = "";
                    int selectedVendorId = Integer.parseInt(billsVendorId1.getText().toString());

                    accountNumber = "";
                    accountPayableNumber = "300";
                    amountVal = Double.parseDouble(editBillAmount.getText().replace(",", ""));
                    controlNumber = editBillVendor.getSelectedItem().toString();
                    setVendorName(controlNumber);
                    referenceNumber = editBillInvoice.getText();
                    memo = editBillMemo.getText();

                    String deleteSql[] = new String[1];
                    String delSql[] = new String[1];

                    // Delete Bill
                    deleteSql[0] = "DELETE From AccountingGLTable "
                            + "Where ReferenceNumber = '" + getInvoiceNo() + "' "
                            + "AND ControlNumber = '" + billsVendorId1.getText() + "' "
                            + "AND GLType = 'Bill' ";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteSql, editBillExpensesTable);

                    // Delete Bill Line
                    delSql[0] = "DELETE From AccountingGLTable "
                            + "Where ReferenceNumber = '" + getInvoiceNo() + "' "
                            + "AND Memo = '" + billsVendorId1.getText() + "' "
                            + "AND GLType = 'Bill Line' ";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(delSql, editBillExpensesTable);

                    // Insert new record
                    sql = new String[1];

                    sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, "
                            + "PostDate, DateDue, Memo, GLType, LotName, Class) VALUES('" + accountPayableNumber + "', '0.00', '" + amountVal
                            + "', '" + billsVendorId1.getText() + "', '" + referenceNumber + "', '"
                            + AccountingUtil.dateFormat.format(editBillPostDate.getDate()) + "', '"
                            + AccountingUtil.dateFormat.format(editBillDueDate.getDate()) + "', '" + memo + "', 'Bill', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + editClass.getSelectedItem().toString() + "'"
                            + ")";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                    accountColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Account");
                    amountColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Amount");
                    controlColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Control #");
                    int referenceColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Invoice #");
                    int vendorColumn = AccountingUtil.getColumnByName(editBillExpensesTable, "Vendor #");

                    referenceNumber = editBillInvoice.getText();

                    for (int i = 0; i <= editBillExpensesTable.getRowCount() - 1; i++) {

                        accountNumber = null;
                        amountVal = 0.00;
                        controlNumber = null;
                        memo = null;

                        if (editBillExpensesTable.getValueAt(i, accountColumn) != null) {
                            accountNumber = editBillExpensesTable.getValueAt(i, accountColumn).toString();
                            accountNumber = accountNumber.split("-")[0];
                        }
                        if (editBillExpensesTable.getValueAt(i, amountColumn) != null && !editBillExpensesTable.getValueAt(i, amountColumn).toString().isEmpty()) {
                            if (editBillExpensesTable.getValueAt(i, amountColumn).toString().contains(",")) {
                                amountVal = Double.parseDouble(editBillExpensesTable.getValueAt(i, amountColumn).toString().replaceAll(",", ""));
                            } else {
                                amountVal = Double.parseDouble(editBillExpensesTable.getValueAt(i, amountColumn).toString());
                            }
                        } else {
                            amountVal = 0.00;
                        }
                        if (editBillExpensesTable.getValueAt(i, controlColumn) != null) {
                            controlNumber = editBillExpensesTable.getValueAt(i, controlColumn).toString();
                        } else {
                            controlNumber = "";
                        }

                        memo = billsVendorId1.getText().toString();

                        sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, DateDue, Memo, GLType, LotName, Class) VALUES("
                                + "'" + accountNumber + "', '" + amountVal + "', '0.00', '" + controlNumber + "', '" + referenceNumber + "', "
                                + "'" + AccountingUtil.dateFormat.format(editBillPostDate.getDate()) + "', '" + AccountingUtil.dateFormat.format(editBillDueDate.getDate()) + "', '" + memo + "', 'Bill Line', "
                                + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + editClass.getSelectedItem().toString() + "'"
                                + ")";
                        //System.out.println("sql--11 " + sql[0]);
                        dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                    }

                    editBillPostDate.setDate(Calendar.getInstance().getTime());
                    editBillDueDate.setDate(Calendar.getInstance().getTime());

                    DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable.getModel();

                    dms.DMSApp.displayMessage(editBillExpensesTable, "Record Updated Successfully ", JOptionPane.INFORMATION_MESSAGE);
                    reloadVendorsList();
                    setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                    billsVendorsTableaccountsPayableVendorsClicked(null);
                    AccountingUtil obj = new AccountingUtil();
                    //System.out.println("selectedVId = " + selectedVendorId);
                    String vendor = obj.getVendorName(selectedVendorId);
                    //System.out.println("vendor = " + vendor);
                    // To populate the selected value in drop-down
                    ComboBoxModel model = editBillVendor.getModel();
                    int size = model.getSize();
                    for (int i = 0; i < size; i++) {
                        Object element = model.getElementAt(i);
                        if (vendor.equals(element.toString())) {
                            editBillVendor.setSelectedItem(element);
                        }
                    }
                    billsVendorId1.setText(String.valueOf(selectedVendorId));
                } catch (Exception e) {
                    Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }//GEN-LAST:event_billsCreateButton1enterBillsButtonsClicked

        private void billsClearButton1enterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsClearButton1enterBillsButtonsClicked
            // TODO add your handling code here:
            DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable.getModel();
            AccountingUtil.clearTableModel(aModel);
        }//GEN-LAST:event_billsClearButton1enterBillsButtonsClicked

        private void billsAddLineButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsAddLineButton1ActionPerformed
            // TODO add your handling code here:
            DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable.getModel();
            try {
                int rowCount = editBillExpensesTable.getRowCount();
                if (isRowRemoved) {
                    rowNo = rowCount - 1;
                    isRowRemoved = false;
                }
                if (rowCount > 0) {
                    if (editBillExpensesTable.getValueAt(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Account")) != null) {
                        account = editBillExpensesTable.getValueAt(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Account")).toString();
                    }
                    if (editBillExpensesTable.getValueAt(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Amount")) != null) {
                        amount = editBillExpensesTable.getValueAt(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Amount")).toString();
                    }
                    if (account == null) {
                        dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.WARNING_MESSAGE);
                        editBillExpensesTable.setCellSelectionEnabled(true);
                        editBillExpensesTable.changeSelection(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Account"), false, false);
                        editBillExpensesTable.requestFocus();
                    } else {
                        if (AccountingUtil.isControlNumberRequired(editBillExpensesTable)) {
                            return;
                        }
                    }
                    if (amount == null || amount.isEmpty()) {
                        dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.ERROR_MESSAGE);
                        editBillExpensesTable.setCellSelectionEnabled(true);
                        editBillExpensesTable.changeSelection(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                        editBillExpensesTable.requestFocus();
                    } else if (amount != null) {
                        if (!displayNumeric(amount)) {
                            dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                            editBillExpensesTable.setCellSelectionEnabled(true);
                            editBillExpensesTable.changeSelection(rowNo, editBillExpensesTable.getColumnModel().getColumnIndex("Amount"), false, false);
                            editBillExpensesTable.requestFocus();
                        }
                    }
                }
                // check to insert a row
                if (editBillExpensesTable.getRowCount() == 0) {
                    addNewRow(aModel, account);
                    rowNo++;
                } else if (account != null && amount != null && displayNumeric(amount)) {
                    addNewRow(aModel, account);
                    rowNo++;
                    enterCounter++;
                }
            } catch (Exception e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            }
        }//GEN-LAST:event_billsAddLineButton1ActionPerformed

        private void billsDeleteLineButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsDeleteLineButton1ActionPerformed
            // TODO add your handling code here:
            DefaultTableModel tableModel = (DefaultTableModel) editBillExpensesTable.getModel();
            // Remove last row
            tableModel.removeRow(tableModel.getRowCount() - 1);
            isRowRemoved = true;
            double sum = 0.00;
            int rowCount = editBillExpensesTable.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                if (editBillExpensesTable.getValueAt(i, 1) != null && !editBillExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
                    if (displayNumeric(editBillExpensesTable.getValueAt(i, 1).toString())) {
                        if (editBillExpensesTable.getValueAt(i, 1).toString().contains(",")) {
                            sum += Double.parseDouble(editBillExpensesTable.getValueAt(i, 1).toString().replace(",", ""));
                        } else {
                            sum += Double.parseDouble(editBillExpensesTable.getValueAt(i, 1).toString());
                        }
                    }
                }
            }
            Double diff = 0.00;
            if (editBillAmount.getText() != null) {
                if (editBillAmount.getText().toString().contains(",")) {
                    diff = sum - Double.parseDouble(editBillAmount.getText().toString().replace(",", ""));
                } else {
                    diff = sum - Double.parseDouble(editBillAmount.getText().toString());
                }
            }

            editBillTotalExpenses.setValue(sum);

            if (diff < 0.00) {
                editBillLinesDiff.setForeground(Color.red);
            } else if (diff > 0.00) {
                editBillLinesDiff.setForeground(Color.green);
            } else {
                editBillLinesDiff.setForeground(Color.black);
            }

            editBillLinesDiff.setValue(diff);
        }//GEN-LAST:event_billsDeleteLineButton1ActionPerformed

        private void billsSearchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_billsSearchTextFieldKeyReleased
            if (evt.getKeyCode() == 10) {
                billsSearchButtonMouseClicked(null);
            }
        }//GEN-LAST:event_billsSearchTextFieldKeyReleased

        private void jCheckBox11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox11MouseClicked
            AccountingUtil.dateFilter(jPanel228, jCheckBox11);
        }//GEN-LAST:event_jCheckBox11MouseClicked

    private void editBillExpensesTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editBillExpensesTableFocusGained
        accountsToBeShownList.clear();
    }//GEN-LAST:event_editBillExpensesTableFocusGained

    private void editBillExpensesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editBillExpensesTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_editBillExpensesTableMouseClicked

    private void SearchVendorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchVendorButtonActionPerformed
        // TODO add your handling code here:
        String searchText = "";
        searchText = txt_Search.getText();

        if (searchText.equals("")) {
            reloadVendorsList();
        } else {
            reloadVendorsListOnSearch(searchText);
        }
    }//GEN-LAST:event_SearchVendorButtonActionPerformed

        private void editClassItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_editClassItemStateChanged
            // TODO add your handling code here:
        }//GEN-LAST:event_editClassItemStateChanged

        private void paybillsClassItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_paybillsClassItemStateChanged
            // TODO add your handling code here:
        }//GEN-LAST:event_paybillsClassItemStateChanged

        private void billsCreditRadioButtonbillsTransactionsButtonsClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsCreditRadioButtonbillsTransactionsButtonsClicked
            apVendorBillDue.setVisible(false);
            apVendorBillPaid.setVisible(false);
            apVendorBillCredit.setVisible(true);
            jPanel242.setVisible(true);
            billsVendorsTableaccountsPayableVendorsKeyed(null);
        }//GEN-LAST:event_billsCreditRadioButtonbillsTransactionsButtonsClicked

        private void editBill1billButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editBill1billButtonsClicked
            try {
                if (apVendorBillCreditTable.getSelectedRowCount() == 0) {
                    dms.DMSApp.displayMessage(apVendorBillCreditTable, "Please select at least 1 bill to edit", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (apVendorBillCreditTable.getSelectedRowCount() > 1) {
                    dms.DMSApp.displayMessage(apVendorBillCreditTable, "Please select only 1 bill to edit", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (apVendorBillCreditTable.getSelectedRowCount() == 1) {
                    String query;
                    int controlNumber = Integer.parseInt(billsVendorId.getText().toString());
                    String referenceNumber = apVendorBillCreditTable.getValueAt(
                            apVendorBillCreditTable.getSelectedRow(), apVendorBillCreditTable.getColumnModel().getColumnIndex("Invoice #")).toString();

                    query = "SELECT B.VendorName, A.ReferenceNumber, A.Memo, CAST(ROUND(A.Debit,2) AS NUMERIC(10,2)) AS Debit, A.PostDate, A.DateDue, A.Class "
                            + "FROM AccountingGLTable A "
                            + "LEFT JOIN VendorListTable B "
                            + "ON (A.ControlNumber = B.VendorID) "
                            + "WHERE GLType = 'Credit' "
                            + "AND ControlNumber = '" + controlNumber + "' "
                            + "AND ReferenceNumber = '" + referenceNumber + "'";

                    //System.out.println("sql:1" + query);

                    ResultSet rs;
                    String vendor = "";
                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                    if (rs.next()) {
                        vendor = rs.getString("VendorName");
                        editBillInvoice1.setText(rs.getString("ReferenceNumber"));
                        editBillMemo1.setText(rs.getString("Memo"));
                        editBillAmount1.setText(AccountingUtil.formatAmountForDisplay(rs.getString("Debit")));
                        editBillPostDate1.setDate(AccountingUtil.getUtilDateFormatFromSqlDate(rs.getDate("PostDate")));
                        editBillDueDate1.setDate(AccountingUtil.getUtilDateFormatFromSqlDate(rs.getDate("DateDue")));
                        editClass1.setSelectedItem(rs.getString("Class"));
                    }

                    editBillTotalExpenses1.setText(editBillAmount1.getText());
                    editBillLinesDiff1.setText("0.00");
                    billsVendorId2.setText(billsVendorId.getText());
                    // To populate the selected value in drop-down
                    ComboBoxModel model = editBillVendor1.getModel();
                    int size = model.getSize();
                    for (int i = 0; i < size; i++) {
                        Object element = model.getElementAt(i);
                        //System.out.println("Element at " + i + " = " + element);
                        if (vendor.equals(element.toString())) {
                            editBillVendor1.setSelectedItem(element);
                        }
                    }
                    ////////////////////////////////////////////////////////

                    query = "SELECT AccountNumber,CAST(ROUND(Credit,2) AS NUMERIC(10,2)), ControlNumber "
                            + "FROM AccountingGLTable "
                            + "WHERE GLType = 'Credit Line' "
                            + "AND ReferenceNumber = '" + referenceNumber + "' "
                            + "AND Memo = '" + controlNumber + "'";

                    DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable1.getModel();
                    AccountingUtil.clearTableModel(aModel);

                    //System.out.println("sql:2" + query);

                    rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

                    ResultSetMetaData rsmd = null;
                    rsmd = rs.getMetaData();
                    int colNo = rsmd.getColumnCount();
                    int accountNumber = 0;
                    Object accounts[] = null;
                    while (rs.next()) {

                        Object[] values = new Object[colNo];

                        for (int i = 0; i < colNo; i++) {
                            accountNumber = rs.getInt("AccountNumber");
                            if (i == 0) {
                                values[i] = AccountingUtil.getAccountFull(accountNumber);
                            } else if (i == 1) {
                                values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject(i + 1).toString());
                            } else {
                                values[i] = rs.getObject(i + 1);
                            }
                            accounts = AccountingUtil.getFullAccountsForInvoice(accountNumber);
                        }
                        for (int k = 0; k < accounts.length; k++) {
                            if (accounts[k] != null) {
                                accountsToBeShownList.add(accounts[k]);
                            }
                        }
                        accounts = null;
                        aModel.addRow(values);
                    }

                    rs.getStatement().close();
                    setVendorId(controlNumber);
                    setInvoiceNo(referenceNumber);
                    //editBillCreditCheck1.setSelected(true);
                    editCreditsPopup.setVisible(true);
                }

            } catch (SQLException ex) {
                Logger.getLogger(AccountsPayablePanel.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            addComboBoxesToTableForEditCreditTable();
        }//GEN-LAST:event_editBill1billButtonsClicked

        private void deleteBill1billButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteBill1billButtonsClicked
            sql = new String[2];
            boolean sureToDelete = false;
            int selectedRowCount = apVendorBillCreditTable.getSelectedRowCount();

            if (apVendorBillCreditTable.getSelectedRowCount() == 0) {
                dms.DMSApp.displayMessage(apVendorBillCreditTable, "Please select atleast 1 bill to delete", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (apVendorBillCreditTable.getSelectedRowCount() > 1) {
                dms.DMSApp.displayMessage(apVendorBillCreditTable, "Please select only 1 bill to delete", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedRowCount == 1) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    sureToDelete = false;
                } else if (response == JOptionPane.YES_OPTION) {
                    sureToDelete = true;
                } else if (response == JOptionPane.CLOSED_OPTION) {
                    //System.out.println("JOptionPane closed");
                }

                if (sureToDelete) {
                    String invoice = apVendorBillCreditTable.getValueAt(apVendorBillCreditTable.getSelectedRow(), apVendorBillCreditTable.getColumnModel().getColumnIndex("Invoice #")).toString();

                    String vendorId = billsVendorId.getText().toString();

                    // Delete the invoice
                    sql[0] = "DELETE FROM AccountingGLTable "
                            + "WHERE GLtype = 'Credit' "
                            + "AND ControlNumber = '" + vendorId + "' "
                            + "AND ReferenceNumber = '" + invoice + "'";

                    // Delete the invoice lines
                    sql[1] = "DELETE FROM AccountingGLTable "
                            + "WHERE GLtype = 'Credit Line' "
                            + "AND Memo = '" + vendorId + "' "
                            + "AND ReferenceNumber = '" + invoice + "'";

                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
                    billsVendorsTableaccountsPayableVendorsClicked(null);
                    reloadVendorsList();
                    setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                    billsVendorsTableaccountsPayableVendorsClicked(null);
                    //jTable1.remove(jTable1.getSelectedRow());
                    billsCreditRadioButton.setSelected(true);
                    billsDueRadioButton1.setSelected(false);
                    apVendorBillDue.setVisible(false);
                    apVendorBillPaid.setVisible(false);
                    apVendorBillCredit.setVisible(true);
                    jPanel242.setVisible(true);
                    billsVendorsTableaccountsPayableVendorsKeyed(null);
                }
            }
        }//GEN-LAST:event_deleteBill1billButtonsClicked

        private void bottomBillsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottomBillsTableMouseClicked

            int[] selectedRows = bottomBillsTable.getSelectedRows();

            ListSelectionModel selectionModel = bottomBillsTable.getSelectionModel();
            int count = 0;

            int selectedRow = bottomBillsTable.getSelectedRow();
            //System.out.println("selectedRow = " + selectedRow);
            if(selectedRow < 1){                
                return;
            }
            bottomBillsTable.setRowSelectionAllowed(true);
            bottomBillsTable.setRowSelectionInterval(selectedRow, selectedRow);
            int selectedBillCol = bottomBillsTable.getColumnModel().getColumnIndex("Select Bill");
            if (billsAllVendorsCheckbox.isSelected()) {
                selectionModel.setSelectionInterval(selectedRow, selectedRow);//for first row selection
            } else {
                for (int i = 0; i < bottomBillsTable.getRowCount(); i++) {
                    if (bottomBillsTable.getValueAt(i, 0) != null
                            && bottomBillsTable.getValueAt(i, 0).toString().equalsIgnoreCase("true")) {
                        if (count == 0) {
                            selectionModel.setSelectionInterval(i, i);//for first row selection
                        } else {
                            selectionModel.addSelectionInterval(i, i);
                        }
                        count++;
                    }
                }
            }
            count = bottomBillsTable.getSelectedRowCount();
            if (count <= 0) {
                selectedInvoices.setText("Invoice(s): 0");
                amountInvoices.setText("");
                bottomBillsTable.getSelectionModel().clearSelection();
            } else if (count == 1 && bottomBillsTable.getValueAt(selectedRows[0], 0) != null && bottomBillsTable.getValueAt(selectedRows[0], 0).toString().equalsIgnoreCase("true")) {
                jLabel25.setVisible(true);
                selectedInvoices.setVisible(true);
                selectedInvoices.setText("Invoice(s): 1");
                jPanel96.setVisible(true);
                int selectedRowFromBottomTable = bottomBillsTable.getSelectedRow();
                Double amountVal;
                amountVal = Double.parseDouble(bottomBillsTable.getValueAt(selectedRowFromBottomTable, bottomBillsTable.getColumnModel().getColumnIndex("Amount")).toString());
                amountInvoices.setText(amountVal.toString());

            } else if (count == 1 && bottomBillsTable.getValueAt(selectedRows[0], 0) != null && bottomBillsTable.getValueAt(selectedRows[0], 0).toString().equalsIgnoreCase("false")) {
                selectedInvoices.setText("Invoice(s): 0");
                amountInvoices.setText("");
            } else {
                jLabel25.setVisible(true);
                selectedInvoices.setVisible(true);
                selectedInvoices.setText("Invoice(s): " + count);
                jPanel96.setVisible(true);
                Double amountValue = 0.00;

                if (count > 1) {
                    Double totalAmountInvoices = 0.00;

                    for (int i = 0; i < count; i++) {
                        int[] selRows = bottomBillsTable.getSelectedRows();
                        amountValue = Double.parseDouble(bottomBillsTable.getValueAt(selRows[i], bottomBillsTable.getColumnModel().getColumnIndex("Amount")).toString());
                        totalAmountInvoices += amountValue;
                    }

                    amountInvoices.setText(totalAmountInvoices.toString());

                } else {
                    amountValue = Double.parseDouble(bottomBillsTable.getValueAt(selectedRow, bottomBillsTable.getColumnModel().getColumnIndex("Amount")).toString());
                    amountInvoices.setText(amountValue.toString());
                }

            }
        }//GEN-LAST:event_bottomBillsTableMouseClicked

    private void paidbillsAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paidbillsAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paidbillsAmountActionPerformed

    private void paidbillsClassItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_paidbillsClassItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_paidbillsClassItemStateChanged

    private void paidbillsBankCombobankSelected(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_paidbillsBankCombobankSelected
        // TODO add your handling code here:
    }//GEN-LAST:event_paidbillsBankCombobankSelected

    private void paidbillsBankComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_paidbillsBankComboItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_paidbillsBankComboItemStateChanged

    private void paybillsCancelButton1(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paybillsCancelButton1
        editPaidBillsPopup.dispose();
    }//GEN-LAST:event_paybillsCancelButton1

    private void paidbillsUpdateAndPrintButtonsaveCheckButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paidbillsUpdateAndPrintButtonsaveCheckButtonsClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_paidbillsUpdateAndPrintButtonsaveCheckButtonsClicked

    private void paidbillsUpdateButton(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paidbillsUpdateButton
        if (paidbillsBankCombo.getSelectedItem() != "") {
            int[] selectedRows = paidbillsDueTable.getSelectedRows();

            sql = new String[3];
            String bankNumber = "" + paidbillsBankCombo.getSelectedItem();
            String checkMemo = "";
            int entryId = 0;
            int id = 0;
            Double amount = 0.00;
            String vendorId = "";
            String accountspaidableNumber = "300";

            bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
            Calendar c = Calendar.getInstance();

            vendorId = billsVendorId.getText().toString();

            if (selectedRows.length > 1) {
                checkMemo = "Multi-Items:";
            } else if (selectedRows.length == 1) {
                entryId = Integer.parseInt(paidbillsDueTable.getValueAt(paidbillsDueTable.getSelectedRow(),
                        paidbillsDueTable.getColumnModel().getColumnIndex("ID")).toString());
            }

            checkMemo = checkMemo + " " + paidbillsMemo.getText();

            String deleteSql[] = new String[1];
            String delCBRecordSql[] = new String[1];

            // Delete Paid Bill
            deleteSql[0] = "DELETE From AccountingGLTable "
                    + "Where ReferenceNumber = '" + paidbillsNumber.getText() + "' "
                    + "AND ControlNumber = '" + vendorId + "' "
                    + "AND GLType = 'Bill payment Check' ";
            //System.out.println("Del chk = " + deleteSql[0]);
            dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteSql, paidbillsDueTable);

            // ACCOUNTS paidABLE
            sql[0] = "INSERT INTO AccountingGLTable "
                    + "(AccountNumber,Debit,Credit,ControlNumber,ReferenceNumber,PostDate,Memo,LotName,GLType,Class) "
                    + "VALUES ('" + accountspaidableNumber + "', '" + paidbillsAmount.getText() + "', '0.00', "
                    + "'" + vendorId + "', "
                    + "'" + paidbillsNumber.getText() + "', "
                    + "'" + AccountingUtil.dateFormat.format(paidbillsDate.getDate()) + "', "
                    + "'" + checkMemo + "', '" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                    + "'Bill payment Check', '" + paidbillsClass.getSelectedItem().toString() + "'"
                    + ")";

            // BANK ACCOUNT
            sql[1] = "INSERT INTO AccountingGLTable "
                    + "(AccountNumber,Debit,Credit,ControlNumber,ReferenceNumber,PostDate,Memo,LotName,GLType,Class) "
                    + "VALUES ('" + bankNumber + "', '0.00', '" + paidbillsAmount.getText() + "', "
                    + "'" + vendorId + "', "
                    + "'" + paidbillsNumber.getText() + "', "
                    + "'" + AccountingUtil.dateFormat.format(paidbillsDate.getDate()) + "', "
                    + "'" + checkMemo + "', '" + dms.DMSApp.getApplication().getCurrentlotName() + "', "
                    + "'Bill payment Check', '" + paidbillsClass.getSelectedItem().toString() + "'"
                    + ")";

            // CHECKBOOK TABLE
            sql[2] = "INSERT INTO AccountingCBTable "
                    + "(EntryID, BankAccount, GLAccount,PaidTo,Amount,CheckNumber,Date,WrittenDate,Memo,LotName) "
                    + "VALUES ('" + entryId + "', '" + bankNumber + "', '" + bankNumber + "', '"
                    + paidbillsPayto.getSelectedItem().toString() + "', "
                    + "'" + paidbillsAmount.getText() + "', '" + paidbillsNumber.getText() + "', "
                    + "'" + AccountingUtil.dateFormat.format(paidbillsDate.getDate()) + "', '" + AccountingUtil.dateFormat.format(c.getTime()) + "', "
                    + "'" + checkMemo + "', '" + dms.DMSApp.getApplication().getCurrentlotName() + "'"
                    + ")";

            //System.out.println("uuuuuu - " + sql[2]);
            int[] selectedRows1 = bottomBillsTable.getSelectedRows();

            int selectedRowsCount = bottomBillsTable.getSelectedRowCount();

            dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);
            billsVendorsTableaccountsPayableVendorsClicked(null);
            editPaidBillsPopup.dispose();
        } else {
            dms.DMSApp.displayMessage(this, "No bank selected.", dms.DMSApp.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_paidbillsUpdateButton

    private void paybillsCheckbookPanel1ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_paybillsCheckbookPanel1ComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_paybillsCheckbookPanel1ComponentShown

    private void editBankChecksSelected() {
        if (paidbillsBankCombo.getSelectedItem() != null) {
            if (!paidbillsBankCombo.getSelectedItem().equals("")) {
                try {
                    String bankNumber = "" + paidbillsBankCombo.getSelectedItem();

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
                        paidbillsEndingBalance.setValue(rs.getDouble("Balance"));
                    }
                    rs.getStatement().close();
                } catch (Exception e) {
                    Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {
                paidbillsEndingBalance.setValue(0.00);
            }
        }

    }

    private void editPaidBillJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editPaidBillJButtonMouseClicked
        if (paidBillsTable.getSelectedRowCount() == 0) {
            dms.DMSApp.displayMessage(paidBillsTable, "Please select at least 1 paid bill to edit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
		
        try {
            int selectedRow = paidBillsTable.getSelectedRow();
            Object columnVal = paidBillsTable.getValueAt(selectedRow, paidBillsTable.getColumnModel().getColumnIndex("Check #"));
            if (columnVal == null) {
                columnVal = "";
            }
            String checkNumberFromSelectedRow = columnVal.toString();

            ResultSet resultSet;
            String invoiceNumber = paidBillsTable.getValueAt(paidBillsTable.getSelectedRow(),
                    paidBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();
            paidbillsPayto.setSelectedItem(paidBillsTable.getValueAt(paidBillsTable.getSelectedRow(),
                    paidBillsTable.getColumnModel().getColumnIndex("Vendor Name")));
            Calendar c = Calendar.getInstance();
            if (paidbillsDate.getDate() == null) {
                paidbillsDate.setDate(c.getTime());
            }

            int bankAccount = Integer.valueOf(paidBillsTable.getValueAt(paidBillsTable.getSelectedRow(),
                    paidBillsTable.getColumnModel().getColumnIndex("Account")).toString());
            String memo = paidBillsTable.getValueAt(paidBillsTable.getSelectedRow(),
                    paidBillsTable.getColumnModel().getColumnIndex("Memos")).toString();
            paidbillsMemo.setText(memo);
			
            if (bankAccount == 202) {
                paidbillsBankCombo.setSelectedItem("202 - Cash in Bank-BOA");
            } else {
                paidbillsBankCombo.setSelectedItem("203 - Cash on hand");
            }
			
            editBankChecksSelected();
			
            DefaultTableModel tableModel = (DefaultTableModel) paidbillsDueTable.getModel();
            AccountingUtil.clearTableModel(tableModel);
            paidbillsNumber.setText(checkNumberFromSelectedRow);
			
            if (checkNumberFromSelectedRow != null && !checkNumberFromSelectedRow.isEmpty()) {
                fetchAllInvoicesForSelectedCheck(checkNumberFromSelectedRow, paidbillsDueTable, paidbillsAmount,
                        editPaidBillsPopup, tableModel);
            } else {
                paidbillsAmount.setValue(Double.parseDouble(paidBillsTable.getValueAt(selectedRow,
                        paidBillsTable.getColumnModel().getColumnIndex("Amount")).toString()));
                editPaidBillsPopup.setVisible(true);
            }
			
            reloadVendorsList();
        } catch (Exception ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_editPaidBillJButtonMouseClicked

    private void fetchAllInvoicesForSelectedCheck(String checkNumberFromSelectedRow, JTable table,
            JFormattedTextField amountField, JDialog targetDialog, DefaultTableModel tableModel) {
        String query = "SELECT EntryID from AccountingCBTable WHERE CheckNumber = '" + checkNumberFromSelectedRow + "'";
        setTotalAmountForMultipleInvoices(0.0);
        try {
            ResultSet resultSet = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            if (resultSet.next()) {
                String invoiceNum = resultSet.getString("EntryID");
                if (invoiceNum == null || invoiceNum.isEmpty()) {
                    System.out.println("No invoice found for selected bill");
                    return;
                }
                System.out.println("InvoiceNum : " + invoiceNum);
                if (invoiceNum.contains(",")) {
                    String[] invoiceNumArr = invoiceNum.split(",");
                    for (int i = 0; i < invoiceNumArr.length; i++) {
                        String currentInvoiceNum = invoiceNumArr[i].trim();
                        fecthInvoiceForSingleEntryId(currentInvoiceNum, tableModel, table);
                    }
                } else {
                    fecthInvoiceForSingleEntryId(invoiceNum, tableModel, table);
                }
            }
            System.out.println("Addition of Invoice lines complete.");
            amountField.setValue(getTotalAmountForMultipleInvoices());
            targetDialog.setVisible(true);
            setTotalAmountForMultipleInvoices(0.0);
        } catch (SQLException ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fecthInvoiceForSingleEntryId(String invoiceNum, DefaultTableModel tableModel, JTable table) {
        try {
            String sqlQuery = "SELECT '',"
                    + "A.ReferenceNumber, EntryId, B.VendorName, A.AccountNumber, "
                    + "CAST(ROUND(Debit,2) AS NUMERIC(10,2)) As Amount, A.ControlNumber, "
                    + "A.PostDate, A.DateDue, A.Memo, A.Class "
                    + "FROM AccountingGLTable A "
                    + "LEFT JOIN VendorListTable B "
                    + "ON A.Memo = CAST(B.VendorID AS VARCHAR) "
                    + "WHERE GLType = 'Bill Line' "
                    + "AND ReferenceNumber IN ('" + invoiceNum + "')";

            ResultSet resultSet = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount();
            String classValue = "";
            System.out.println("Adding line for invoice : " + invoiceNum);
            while (resultSet.next()) {
                Object[] values = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    classValue = resultSet.getString("Class");
                    Object currentVal = resultSet.getObject(i + 1);
                    if (i == 0) {
                        values[i] = true;
                    } else if (i == AccountingUtil.getColumnByName(table, "Post Date")) {
                        values[i] = AccountingUtil.dateFormat1.format(currentVal);
                    } else if (i == AccountingUtil.getColumnByName(table, "Due Date")) {
                        values[i] = AccountingUtil.dateFormat1.format(currentVal);
                    } else {
                        values[i] = resultSet.getObject(i + 1);
                    }
                }
                Double currentInvoiceAmount = resultSet.getDouble("Amount");
                setTotalAmountForMultipleInvoices(currentInvoiceAmount + getTotalAmountForMultipleInvoices());
                tableModel.addRow(values);
            }
            resultSet.getStatement().close();
            paidbillsClass.setSelectedItem(classValue.toString());
        } catch (SQLException ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deletePaidBillJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deletePaidBillJButtonMouseClicked
        sql = new String[2];
        String[] updateQuery = new String[1];
        java.sql.Timestamp myDate = null;
        boolean sureToDelete = false;
        int selectedRowCount = paidBillsTable.getSelectedRowCount();

        if (paidBillsTable.getSelectedRowCount() == 0) {
            dms.DMSApp.displayMessage(paidBillsTable, "Please select at least one paid bill to delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (paidBillsTable.getSelectedRowCount() > 1) {
            dms.DMSApp.displayMessage(paidBillsTable, "Please select only 1 bill to delete", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedRowCount == 1) {
            int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
                sureToDelete = false;
            } else if (response == JOptionPane.YES_OPTION) {
                sureToDelete = true;
            } else if (response == JOptionPane.CLOSED_OPTION) {
                //System.out.println("JOptionPane closed");
            }

            if (sureToDelete) {
                String invoice = paidBillsTable.getValueAt(paidBillsTable.getSelectedRow(), paidBillsTable.getColumnModel().getColumnIndex("Invoice #")).toString();
                String vendorIdFromTable = billsVendorId.getText().toString();
                // Delete the invoice
			    /*sql[0] = "DELETE FROM AccountingGLTable "
                 + "WHERE GLtype = 'Bill' "
                 + "AND ControlNumber = '" + vendorIdFromTable + "' "
                 + "AND ReferenceNumber = '" + invoice + "'";

                 // Delete the invoice lines
                 sql[1] = "DELETE FROM AccountingGLTable "
                 + "WHERE GLtype = 'Bill Line' "
                 + "AND Memo = '" + vendorIdFromTable + "' "
                 + "AND ReferenceNumber = '" + invoice + "'";

                 dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);*/

                // Set the paid bill to unpaid bill on Delete

                updateQuery[0] = "UPDATE AccountingGLTable SET DatePaid = " + myDate
                        + " WHERE GLtype = 'Bill' "
                        + "AND ControlNumber = '" + vendorIdFromTable + "' "
                        + "AND ReferenceNumber = '" + invoice + "'";

                //System.out.println("updateQry := " + updateQuery[0]);
                dms.DMSApp.getApplication().getDBConnection().executeStatements(updateQuery, this);
                billsVendorsTableaccountsPayableVendorsClicked(null);
                reloadVendorsList();
            }
        }
    }//GEN-LAST:event_deletePaidBillJButtonMouseClicked

    private void billsVendorsTableMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsVendorsTableMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_billsVendorsTableMouseEntered

    private void editBillVendor1ItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_editBillVendor1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_editBillVendor1ItemStateChanged

    private void editBillInvoice1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_editBillInvoice1InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_editBillInvoice1InputMethodTextChanged

    private void editClass1ItemStateChanged(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_editClass1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_editClass1ItemStateChanged

    private void editBillAmount1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editBillAmount1FocusLost
        // TODO add your handling code here:
        String billAmountStringVal = editBillAmount1.getText();
        if (billAmountStringVal.contains(",")) {
            billAmountStringVal = billAmountStringVal.replaceAll(",", "");
        }
        String billAmountTotalExpensesStringVal = editBillTotalExpenses1.getText();
        if (billAmountTotalExpensesStringVal.contains(",")) {
            billAmountTotalExpensesStringVal = billAmountTotalExpensesStringVal.replaceAll(",", "");
        }
        if (!billAmountTotalExpensesStringVal.isEmpty() && !editBillAmount1.getText().isEmpty()) {
            Double diff = Double.parseDouble(billAmountTotalExpensesStringVal) - Double.parseDouble(billAmountStringVal);

            if (diff < 0.00) {
                editBillLinesDiff1.setForeground(Color.red);
            } else if (diff > 0.00) {
                editBillLinesDiff1.setForeground(Color.green);
            } else {
                editBillLinesDiff1.setForeground(Color.black);
            }
            editBillLinesDiff1.setValue(diff);
        }
    }//GEN-LAST:event_editBillAmount1FocusLost

    private void editBillAmount1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_editBillAmount1InputMethodTextChanged
        // TODO add your handling code here:
        editBillLinesDiff1.setValue(Integer.parseInt(editBillAmount1.getValue().toString()) - Integer.parseInt(editBillTotalExpenses1.getValue().toString()));
        // Edit Credit Case
        Action action4 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                double sum = 0.00;
                TableCellListener tcl4 = (TableCellListener) e.getSource();
                int rowCount = editBillExpensesTable1.getRowCount();
                int modifiedRowNo = 0;
                String amountForDisplay;
                modifiedRowNo = tcl4.getRow();
                for (int i = 0; i < rowCount; i++) {
                    if (editBillExpensesTable1.getValueAt(i, 1) != null && !editBillExpensesTable1.getValueAt(i, 1).toString().isEmpty()) {
                        if (!AccountingUtil.displayNumeric(editBillExpensesTable1.getValueAt(i, 1).toString())) {
                            dms.DMSApp.displayMessage(editBillExpensesTable1, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                            editBillExpensesTable1.setCellSelectionEnabled(true);
                            editBillExpensesTable1.changeSelection(modifiedRowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                            editBillExpensesTable1.requestFocus();
                        }
                        if (AccountingUtil.displayNumeric(editBillExpensesTable1.getValueAt(i, 1).toString())) {
                            if (editBillExpensesTable1.getValueAt(i, 1).toString().contains(",")) {
                                sum += Double.parseDouble(editBillExpensesTable1.getValueAt(i, 1).toString().replaceAll(",", ""));
                            } else {
                                sum += Double.parseDouble(editBillExpensesTable1.getValueAt(i, 1).toString());
                            }
                            if (modifiedRowNo == i) {   // change amount format only for current row
                                amountForDisplay = AccountingUtil.formatAmountForDisplay(editBillExpensesTable1.getValueAt(modifiedRowNo, 1).toString());
                                editBillExpensesTable1.setValueAt(amountForDisplay, modifiedRowNo, 1);
                            }
                        }
                    }
                }
                Double diff = 0.00;
                if (editBillAmount1.getText().toString().contains(",")) {
                    diff = Double.parseDouble(editBillAmount1.getText().toString().replace(",", "")) - sum;
                } else {
                    diff = Double.parseDouble(editBillAmount1.getText().toString()) - sum;
                }

                editBillTotalExpenses1.setValue(sum);
                editBillLinesDiff1.setValue(diff);
            }
        };

        TableCellListener tcl4 = new TableCellListener(editBillExpensesTable1, action4);
    }//GEN-LAST:event_editBillAmount1InputMethodTextChanged

    private void editBillExpensesTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editBillExpensesTable1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_editBillExpensesTable1MouseClicked

    private void editBillExpensesTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editBillExpensesTable1FocusGained
        // TODO add your handling code here:
        accountsToBeShownList.clear();
    }//GEN-LAST:event_editBillExpensesTable1FocusGained

    private void billsDeleteLineButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsDeleteLineButton2ActionPerformed

        DefaultTableModel tableModel = (DefaultTableModel) editBillExpensesTable1.getModel();
        // Remove last row
        tableModel.removeRow(tableModel.getRowCount() - 1);
        isRowRemoved = true;
        double sum = 0.00;
        int rowCount = editBillExpensesTable1.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (editBillExpensesTable1.getValueAt(i, 1) != null && !editBillExpensesTable1.getValueAt(i, 1).toString().isEmpty()) {
                if (displayNumeric(editBillExpensesTable1.getValueAt(i, 1).toString())) {
                    if (editBillExpensesTable1.getValueAt(i, 1).toString().contains(",")) {
                        sum += Double.parseDouble(editBillExpensesTable1.getValueAt(i, 1).toString().replace(",", ""));
                    } else {
                        sum += Double.parseDouble(editBillExpensesTable1.getValueAt(i, 1).toString());
                    }
                }
            }
        }
        Double diff = 0.00;
        if (editBillAmount1.getText() != null) {
            if (editBillAmount1.getText().toString().contains(",")) {
                diff = sum - Double.parseDouble(editBillAmount1.getText().toString().replace(",", ""));
            } else {
                diff = sum - Double.parseDouble(editBillAmount1.getText().toString());
            }
        }

        editBillTotalExpenses1.setValue(sum);

        if (diff < 0.00) {
            editBillLinesDiff1.setForeground(Color.red);
        } else if (diff > 0.00) {
            editBillLinesDiff1.setForeground(Color.green);
        } else {
            editBillLinesDiff1.setForeground(Color.black);
        }

        editBillLinesDiff1.setValue(diff);
    }//GEN-LAST:event_billsDeleteLineButton2ActionPerformed

    private void billsAddLineButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billsAddLineButton2ActionPerformed

        DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable1.getModel();
        try {
            int rowCount = editBillExpensesTable1.getRowCount();
            if (isRowRemoved) {
                rowNo = rowCount - 1;
                isRowRemoved = false;
            }
            if (rowCount > 0) {
                if (editBillExpensesTable1.getValueAt(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Account")) != null) {
                    account = editBillExpensesTable1.getValueAt(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Account")).toString();
                }
                if (editBillExpensesTable1.getValueAt(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Amount")) != null) {
                    amount = editBillExpensesTable1.getValueAt(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Amount")).toString();
                }
                if (account == null) {
                    dms.DMSApp.displayMessage(this, "Please select an Account.", JOptionPane.ERROR_MESSAGE);
                    editBillExpensesTable1.setCellSelectionEnabled(true);
                    editBillExpensesTable1.changeSelection(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Account"), false, false);
                    editBillExpensesTable1.requestFocus();
                }
                if (amount == null || amount.isEmpty()) {
                    dms.DMSApp.displayMessage(this, "Please enter amount.", JOptionPane.ERROR_MESSAGE);
                    editBillExpensesTable1.setCellSelectionEnabled(true);
                    editBillExpensesTable1.changeSelection(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                    editBillExpensesTable1.requestFocus();
                } else if (amount != null) {
                    if (!displayNumeric(amount)) {
                        dms.DMSApp.displayMessage(this, "Please enter numbers only for Amount.", JOptionPane.ERROR_MESSAGE);
                        editBillExpensesTable1.setCellSelectionEnabled(true);
                        editBillExpensesTable1.changeSelection(rowNo, editBillExpensesTable1.getColumnModel().getColumnIndex("Amount"), false, false);
                        editBillExpensesTable1.requestFocus();
                    }
                }
            }
            // check to insert a row
            if (editBillExpensesTable1.getRowCount() == 0) {
                addNewRow(aModel, account);
                rowNo++;
            } else if (account != null && amount != null && displayNumeric(amount)) {
                addNewRow(aModel, account);
                rowNo++;
                enterCounter++;
            }
        } catch (Exception e) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }//GEN-LAST:event_billsAddLineButton2ActionPerformed

    private void billsClearButton2enterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsClearButton2enterBillsButtonsClicked
        // TODO add your handling code here:
        editBillAmount1.setText("0.00");
        DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable1.getModel();
        AccountingUtil.clearTableModel(aModel);
    }//GEN-LAST:event_billsClearButton2enterBillsButtonsClicked

    private void billsCreateButton2enterBillsButtonsClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsCreateButton2enterBillsButtonsClicked
        boolean good = true;
        String billAmountVal = "";
        String totalExpensesAmount = "";
        int accountColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Account");
        int amountColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Amount");
        int controlColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Control #");
        String accountNumber = null;
        String accountPayableNumber = null;
        Boolean isControlled = false;
        Double amountVal = null;
        int editBillExpensesRowCount = editBillExpensesTable1.getRowCount();
        if (editBillExpensesRowCount == 0) {
            dms.DMSApp.displayMessage(this, "Please add at least one credit", dms.DMSApp.WARNING_MESSAGE);
            return;
        }
        if (editBillAmount1.getText() != null && editBillTotalExpenses1.getText() != null) {
            String billAmountStringVal = editBillAmount1.getText().toString();
            String billTotalExpensesStringVal = editBillTotalExpenses1.getValue().toString();
            billAmountStringVal = billAmountStringVal.replaceAll(",", "");
            billTotalExpensesStringVal = billTotalExpensesStringVal.replaceAll(",", "");
            Double billAmountDoubleVal = Double.parseDouble(billAmountStringVal);
            Double billTotalExpensesDoubleVal = Double.parseDouble(billTotalExpensesStringVal);
            int retval = Double.compare(billAmountDoubleVal, billTotalExpensesDoubleVal);
            if (retval != 0) {
                dms.DMSApp.displayMessage(this, "The amount and total lines amount do not match", dms.DMSApp.WARNING_MESSAGE);
                return;
            }
        }
        if (editBillAmount1.getText() == null && editBillTotalExpenses1.getText() == null) {
            good = false;
            dms.DMSApp.displayMessage(this, "The amount and total lines amount cannot be empty", dms.DMSApp.WARNING_MESSAGE);
        }
        if (editBillAmount1.getText() != null && editBillTotalExpenses1.getText() == null) {
            good = false;
            dms.DMSApp.displayMessage(this, "The total lines amount cannot be empty", dms.DMSApp.WARNING_MESSAGE);
        }
        if (editBillAmount1.getText() == null && editBillTotalExpenses1.getText() != null) {
            good = false;
            dms.DMSApp.displayMessage(this, "The amount field cannot be empty", dms.DMSApp.WARNING_MESSAGE);
        }
        if (editBillInvoice1.getText() == null || "".equals(editBillInvoice1.getText())) {
            good = false;
            dms.DMSApp.displayMessage(this, "An invoice number is required", dms.DMSApp.WARNING_MESSAGE);
        }
        if (editBillExpensesRowCount > 0) {
            for (int i = 0; i <= editBillExpensesTable1.getRowCount() - 1; i++) {
                if (editBillExpensesTable1.getValueAt(i, accountColumn) != null) {
                    accountNumber = editBillExpensesTable1.getValueAt(i, accountColumn).toString();
                    accountNumber = accountNumber.split("-")[0];
                }

                if (accountNumber != null) {
                    isControlled = AccountingUtil.getControlNumMap().get(Integer.valueOf(accountNumber.trim()));
                    if (AccountingUtil.isControlNumberRequired(editBillExpensesTable1)) {
                        return;
                    }
                }

            }

        }

        if (good) {
            try {
                String controlNumber = null;
                String referenceNumber = null;
                String memo = "";

                accountNumber = "";
                accountPayableNumber = "300";
                amountVal = Double.parseDouble(editBillAmount1.getText().replace(",", ""));
                controlNumber = editBillVendor1.getSelectedItem().toString();
                setVendorName(controlNumber);
                referenceNumber = editBillInvoice1.getText();
                memo = editBillMemo1.getText();

                String deleteSql[] = new String[1];
                String delSql[] = new String[1];

                // Delete Bill
                deleteSql[0] = "DELETE From AccountingGLTable "
                        + "Where ReferenceNumber = '" + getInvoiceNo() + "' "
                        + "AND ControlNumber = '" + billsVendorId2.getText() + "' "
                        + "AND GLType = 'Credit' ";

                dms.DMSApp.getApplication().getDBConnection().executeStatements(deleteSql, editBillExpensesTable1);

                // Delete Bill Line
                delSql[0] = "DELETE From AccountingGLTable "
                        + "Where ReferenceNumber = '" + getInvoiceNo() + "' "
                        + "AND Memo = '" + billsVendorId2.getText() + "' "
                        + "AND GLType = 'Credit Line' ";

                dms.DMSApp.getApplication().getDBConnection().executeStatements(delSql, editBillExpensesTable1);

                // Insert new record
                sql = new String[1];

                sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, "
                        + "PostDate, DateDue, Memo, GLType, LotName, Class) VALUES('" + accountPayableNumber + "', '" + amountVal + "','0.00','"
                        + billsVendorId2.getText() + "', '" + referenceNumber + "', '"
                        + AccountingUtil.dateFormat.format(editBillPostDate1.getDate()) + "', '"
                        + AccountingUtil.dateFormat.format(editBillDueDate1.getDate()) + "', '" + memo + "', 'Credit', "
                        + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + editClass1.getSelectedItem().toString() + "'"
                        + ")";

                dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                accountColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Account");
                amountColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Amount");
                controlColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Control #");
                int referenceColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Invoice #");
                int vendorColumn = AccountingUtil.getColumnByName(editBillExpensesTable1, "Vendor #");

                referenceNumber = editBillInvoice1.getText();

                for (int i = 0; i <= editBillExpensesTable1.getRowCount() - 1; i++) {

                    accountNumber = null;
                    amountVal = 0.00;
                    controlNumber = null;
                    memo = null;

                    if (editBillExpensesTable1.getValueAt(i, accountColumn) != null) {
                        accountNumber = editBillExpensesTable1.getValueAt(i, accountColumn).toString();
                        accountNumber = accountNumber.split("-")[0];
                    }
                    if (editBillExpensesTable1.getValueAt(i, amountColumn) != null) {
                        if (editBillExpensesTable1.getValueAt(i, amountColumn).toString().contains(",")) {
                            amountVal = Double.parseDouble(editBillExpensesTable1.getValueAt(i, amountColumn).toString().replaceAll(",", ""));
                        } else {
                            amountVal = Double.parseDouble(editBillExpensesTable1.getValueAt(i, amountColumn).toString());
                        }
                    } else {
                        amountVal = 0.00;
                    }
                    if (editBillExpensesTable1.getValueAt(i, controlColumn) != null) {
                        controlNumber = editBillExpensesTable1.getValueAt(i, controlColumn).toString();
                    } else {
                        controlNumber = "";
                    }

                    memo = billsVendorId2.getText().toString();

                    sql[0] = "INSERT INTO AccountingGLTable (AccountNumber, Debit, Credit, ControlNumber, ReferenceNumber, PostDate, DateDue, Memo, GLType, LotName, Class) VALUES("
                            + "'" + accountNumber + "', '0.00', '" + amountVal + "', '" + controlNumber + "', '" + referenceNumber + "', "
                            + "'" + AccountingUtil.dateFormat.format(editBillPostDate1.getDate()) + "', '" + AccountingUtil.dateFormat.format(editBillDueDate1.getDate()) + "', '" + memo + "', 'Credit Line', "
                            + "'" + dms.DMSApp.getApplication().getCurrentlotName() + "', '" + editClass1.getSelectedItem().toString() + "'"
                            + ")";
                    //System.out.println("sql "+ sql);
                    dms.DMSApp.getApplication().getDBConnection().executeStatements(sql, this);

                }

                editBillPostDate1.setDate(Calendar.getInstance().getTime());
                editBillDueDate1.setDate(Calendar.getInstance().getTime());

                DefaultTableModel aModel = (DefaultTableModel) editBillExpensesTable1.getModel();

                dms.DMSApp.displayMessage(editBillExpensesTable1, "Record Updated Successfully ", JOptionPane.INFORMATION_MESSAGE);
                reloadVendorsList();
                setVendorAsSelected(billsVendorId.getText().toString());      // set vendor as Selected
                billsVendorsTableaccountsPayableVendorsClicked(null);

                // reload Credit table - after edit
                billsCreditRadioButton.setSelected(true);
                billsDueRadioButton1.setSelected(false);
                apVendorBillDue.setVisible(false);
                apVendorBillPaid.setVisible(false);
                apVendorBillCredit.setVisible(true);
                jPanel242.setVisible(true);
                billsVendorsTableaccountsPayableVendorsKeyed(null);

            } catch (Exception e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }//GEN-LAST:event_billsCreateButton2enterBillsButtonsClicked

    private void billsDeleteLineButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsDeleteLineButtonMouseClicked
        DefaultTableModel tableModel = (DefaultTableModel) billExpensesTable.getModel();
        // Remove last row
        if (tableModel.getRowCount() > 0) {
            tableModel.removeRow(tableModel.getRowCount() - 1);
            isRowRemoved = true;
        } else {
            isRowRemoved = false;
        }
        double sum = 0.00;
        int rowCount = billExpensesTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (billExpensesTable.getValueAt(i, 1) != null && !billExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
                if (displayNumeric(billExpensesTable.getValueAt(i, 1).toString())) {
                    if (billExpensesTable.getValueAt(i, 1).toString().contains(",")) {
                        String beforeComma = billExpensesTable.getValueAt(i, 1).toString().substring(0, billExpensesTable.getValueAt(i, 1).toString().indexOf(","));
                        String afterComma = billExpensesTable.getValueAt(i, 1).toString().substring(billExpensesTable.getValueAt(i, 1).toString().indexOf(",") + 1, billExpensesTable.getValueAt(i, 1).toString().length());
                        String finalAmount = beforeComma + afterComma;
                        sum += Double.parseDouble(finalAmount);
                    } else {
                        sum += Double.parseDouble(billExpensesTable.getValueAt(i, 1).toString());
                    }
                }
            }
        }
        Double diff = 0.00;
        if (billAmount.getValue() != null) {
            diff = sum - Double.parseDouble(billAmount.getValue().toString());
        }

        billTotalExpenses.setValue(sum);

        if (diff < 0.00) {
            billLinesDiff.setForeground(Color.red);
        } else if (diff > 0.00) {
            billLinesDiff.setForeground(Color.green);
        } else {
            billLinesDiff.setForeground(Color.black);
        }

        billLinesDiff.setValue(diff);

        /*for (int i = 0; i < rowCount; i++) {
         if (billExpensesTable.getValueAt(i, 1) != null && !billExpensesTable.getValueAt(i, 1).toString().isEmpty()) {
         }
         }
         }*/

        // Code to remove selected row, please choose the preferred approach
                /*int selectedRow = jTable4.getSelectedRow();
         if (selectedRow == -1) {
         JOptionPane.showMessageDialog(this, "Please select the row to remove");
         } else {
         tableModel.removeRow(selectedRow);
         }
         */
    }//GEN-LAST:event_billsDeleteLineButtonMouseClicked

    private void apVendorBillCreditTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_apVendorBillCreditTableMouseClicked
        // TODO add your handling code here:
        int[] selectedRows = apVendorBillCreditTable.getSelectedRows();
        int selectedRowsCount = apVendorBillCreditTable.getSelectedRowCount();

        ListSelectionModel selectionModel = apVendorBillCreditTable.getSelectionModel();
        int count = 0;
        for (int i = 0; i < apVendorBillCreditTable.getRowCount(); i++) {
            if (apVendorBillCreditTable.getValueAt(i, 0) == null) {
            } else if (apVendorBillCreditTable.getValueAt(i, 0).toString().equalsIgnoreCase("true")) {
                if (count == 0) {
                    selectionModel.setSelectionInterval(i, i);//for first row selection
                } else {
                    selectionModel.addSelectionInterval(i, i);
                }
                count++;
            } else if (apVendorBillCreditTable.getValueAt(i, 0).toString().equalsIgnoreCase("false")) {
            }
        }

        if (count <= 0) {
            apVendorBillCreditTable.getSelectionModel().clearSelection();
        } else if (count == 1 && apVendorBillCreditTable.getValueAt(selectedRows[0], 0) != null && apVendorBillCreditTable.getValueAt(selectedRows[0], 0).toString().equalsIgnoreCase("true")) {
        }
    }//GEN-LAST:event_apVendorBillCreditTableMouseClicked
    private void editBillVendorItemStateChanged1(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_editBillVendorItemStateChanged1
    }//GEN-LAST:event_editBillVendorItemStateChanged1

    private void billsAddLineButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsAddLineButtonMousePressed
        billLinesDiff.requestFocusInWindow();
    }//GEN-LAST:event_billsAddLineButtonMousePressed

    private void billsCreateButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsCreateButtonMousePressed
        billLinesDiff.requestFocusInWindow();
    }//GEN-LAST:event_billsCreateButtonMousePressed

    private void billsCreateButton2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_billsCreateButton2MousePressed
        editBillAmount1.requestFocusInWindow();
    }//GEN-LAST:event_billsCreateButton2MousePressed

    private void editCreditsPopupInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_editCreditsPopupInputMethodTextChanged
    }//GEN-LAST:event_editCreditsPopupInputMethodTextChanged

    private void editBillVendor1ItemStateChanged1(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_editBillVendor1ItemStateChanged1
    }//GEN-LAST:event_editBillVendor1ItemStateChanged1

    private void editBillVendor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBillVendor1ActionPerformed
        // TODO add your handling code here:
        JComboBox editBillVendor1 = (JComboBox) evt.getSource();
        Object item = editBillVendor1.getSelectedItem();
        String id = "";
        String value = "";
        if (item != null) {
            id = ((ComboItem) item).getValue();
            //System.out.println("id is = " + id);
            value = ((ComboItem) item).getKey();
            //System.out.println("value is = " + value);
            billsVendorId2.setText(id);
        }
    }//GEN-LAST:event_editBillVendor1ActionPerformed

    private void editBillVendorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBillVendorActionPerformed
        // TODO add your handling code here:
        JComboBox editBillVendor = (JComboBox) evt.getSource();
        String id = "";
        String value = "";
        Object item = editBillVendor.getSelectedItem();
        //System.out.println("item = " + item);
        if (item != null) {
            id = ((ComboItem) item).getValue();
            //System.out.println("id is = " + id);
            value = ((ComboItem) item).getKey();
            //System.out.println("value is = " + value);
            billsVendorId1.setText(id);
        }
    }//GEN-LAST:event_editBillVendorActionPerformed

    private void billExpensesTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_billExpensesTableFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_billExpensesTableFocusGained
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton SearchVendorButton;
        private javax.swing.JLabel amountInvoices;
        private javax.swing.JPanel apVendorBillCredit;
        private javax.swing.JTable apVendorBillCreditTable;
        private javax.swing.JPanel apVendorBillDue;
        private javax.swing.JPanel apVendorBillPaid;
        private javax.swing.JPanel balancePanel4;
        private javax.swing.JPanel balancePanel5;
        private javax.swing.JFormattedTextField billAmount;
        public com.jidesoft.swing.AutoCompletionComboBox billClass;
        private javax.swing.JCheckBox billCreditCheck;
        private com.toedter.calendar.JDateChooser billDueDate;
        private javax.swing.JTable billExpensesTable;
        private javax.swing.JTextField billInvoice;
        private javax.swing.JFormattedTextField billLinesDiff;
        private javax.swing.JTextField billMemo;
        private com.toedter.calendar.JDateChooser billPostDate;
        private javax.swing.JFormattedTextField billTotalExpenses;
        public static com.jidesoft.swing.AutoCompletionComboBox billVendor;
        private javax.swing.JTextField billVendorAddress;
        private javax.swing.JTextField billVendorAddress2;
        private javax.swing.JTextField billVendorConatct;
        private javax.swing.JTextField billVendorEmail;
        private javax.swing.JTextField billVendorName;
        private javax.swing.JTextField billVendorPhone;
        private javax.swing.JButton billsAddLineButton;
        private javax.swing.JButton billsAddLineButton1;
        private javax.swing.JButton billsAddLineButton2;
        private javax.swing.JCheckBox billsAllVendorsCheckbox;
        private javax.swing.JButton billsClearButton;
        private javax.swing.JButton billsClearButton1;
        private javax.swing.JButton billsClearButton2;
        private javax.swing.JPanel billsControlPanel3;
        private javax.swing.JButton billsCreateButton;
        private javax.swing.JButton billsCreateButton1;
        private javax.swing.JButton billsCreateButton2;
        private javax.swing.JRadioButton billsCreditRadioButton;
        private javax.swing.JButton billsDeleteLineButton;
        private javax.swing.JButton billsDeleteLineButton1;
        private javax.swing.JButton billsDeleteLineButton2;
        private javax.swing.JRadioButton billsDueRadioButton1;
        private javax.swing.JRadioButton billsPaidRadioButton1;
        private javax.swing.JButton billsSearchButton;
        private javax.swing.JTextField billsSearchTextField;
        private javax.swing.JLabel billsVendorId;
        private javax.swing.JLabel billsVendorId1;
        private javax.swing.JLabel billsVendorId2;
        private javax.swing.JPanel billsVendorInfo;
        private javax.swing.JTable billsVendorsTable;
        public javax.swing.JTable bottomBillsTable;
        private javax.swing.ButtonGroup buttonGroup1;
        private javax.swing.ButtonGroup buttonGroup2;
        private javax.swing.JButton deleteBill;
        private javax.swing.JButton deleteBill1;
        private javax.swing.JButton deletePaidBillJButton;
        private javax.swing.JButton editBill;
        private javax.swing.JButton editBill1;
        private javax.swing.JFormattedTextField editBillAmount;
        private javax.swing.JFormattedTextField editBillAmount1;
        private com.toedter.calendar.JDateChooser editBillDueDate;
        private com.toedter.calendar.JDateChooser editBillDueDate1;
        private javax.swing.JTable editBillExpensesTable;
        private javax.swing.JTable editBillExpensesTable1;
        private javax.swing.JTextField editBillInvoice;
        private javax.swing.JTextField editBillInvoice1;
        private javax.swing.JFormattedTextField editBillLinesDiff;
        private javax.swing.JFormattedTextField editBillLinesDiff1;
        private javax.swing.JTextField editBillMemo;
        private javax.swing.JTextField editBillMemo1;
        private com.toedter.calendar.JDateChooser editBillPostDate;
        private com.toedter.calendar.JDateChooser editBillPostDate1;
        private javax.swing.JFormattedTextField editBillTotalExpenses;
        private javax.swing.JFormattedTextField editBillTotalExpenses1;
        public static com.jidesoft.swing.AutoCompletionComboBox editBillVendor;
        public static com.jidesoft.swing.AutoCompletionComboBox editBillVendor1;
        private javax.swing.JDialog editBillsPopup;
        public com.jidesoft.swing.AutoCompletionComboBox editClass;
        public com.jidesoft.swing.AutoCompletionComboBox editClass1;
        private javax.swing.JDialog editCreditsPopup;
        private javax.swing.JButton editPaidBillJButton;
        private javax.swing.JDialog editPaidBillsPopup;
        private javax.swing.JPanel enterBillsPanel3;
        private javax.swing.JPanel enterBillsPanel4;
        private javax.swing.JPanel enterBillsPanel5;
        private javax.swing.JLabel entryIdLabel2;
        private javax.swing.JLabel entryIdLabel3;
        private javax.swing.JLabel entryIdLabel4;
        private javax.swing.Box.Filler filler8;
        private javax.swing.JButton jButton49;
        private javax.swing.JCheckBox jCheckBox11;
        private com.toedter.calendar.JDateChooser jDateChooser20;
        private com.toedter.calendar.JDateChooser jDateChooser21;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel116;
        private javax.swing.JLabel jLabel118;
        private javax.swing.JLabel jLabel119;
        private javax.swing.JLabel jLabel120;
        private javax.swing.JLabel jLabel134;
        private javax.swing.JLabel jLabel135;
        private javax.swing.JLabel jLabel136;
        private javax.swing.JLabel jLabel137;
        private javax.swing.JLabel jLabel138;
        private javax.swing.JLabel jLabel139;
        private javax.swing.JLabel jLabel140;
        private javax.swing.JLabel jLabel141;
        private javax.swing.JLabel jLabel142;
        private javax.swing.JLabel jLabel143;
        private javax.swing.JLabel jLabel144;
        private javax.swing.JLabel jLabel145;
        private javax.swing.JLabel jLabel146;
        private javax.swing.JLabel jLabel147;
        private javax.swing.JLabel jLabel148;
        private javax.swing.JLabel jLabel149;
        private javax.swing.JLabel jLabel150;
        private javax.swing.JLabel jLabel151;
        private javax.swing.JLabel jLabel152;
        private javax.swing.JLabel jLabel153;
        private javax.swing.JLabel jLabel154;
        private javax.swing.JLabel jLabel155;
        private javax.swing.JLabel jLabel156;
        private javax.swing.JLabel jLabel157;
        private javax.swing.JLabel jLabel158;
        private javax.swing.JLabel jLabel159;
        private javax.swing.JLabel jLabel163;
        private javax.swing.JLabel jLabel164;
        private javax.swing.JLabel jLabel168;
        private javax.swing.JLabel jLabel169;
        private javax.swing.JLabel jLabel170;
        private javax.swing.JLabel jLabel171;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel25;
        private javax.swing.JLabel jLabel26;
        private javax.swing.JLabel jLabel27;
        private javax.swing.JLabel jLabel28;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel30;
        private javax.swing.JLabel jLabel35;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel52;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel118;
        private javax.swing.JPanel jPanel155;
        private javax.swing.JPanel jPanel156;
        private javax.swing.JPanel jPanel157;
        private javax.swing.JPanel jPanel160;
        private javax.swing.JPanel jPanel161;
        private javax.swing.JPanel jPanel163;
        private javax.swing.JPanel jPanel164;
        private javax.swing.JPanel jPanel165;
        private javax.swing.JPanel jPanel166;
        private javax.swing.JPanel jPanel167;
        private javax.swing.JPanel jPanel168;
        private javax.swing.JPanel jPanel169;
        private javax.swing.JPanel jPanel170;
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
        private javax.swing.JPanel jPanel195;
        private javax.swing.JPanel jPanel196;
        private javax.swing.JPanel jPanel197;
        private javax.swing.JPanel jPanel198;
        private javax.swing.JPanel jPanel199;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel200;
        private javax.swing.JPanel jPanel201;
        private javax.swing.JPanel jPanel209;
        private javax.swing.JPanel jPanel218;
        private javax.swing.JPanel jPanel219;
        private javax.swing.JPanel jPanel220;
        private javax.swing.JPanel jPanel221;
        private javax.swing.JPanel jPanel222;
        private javax.swing.JPanel jPanel223;
        private javax.swing.JPanel jPanel224;
        private javax.swing.JPanel jPanel225;
        private javax.swing.JPanel jPanel226;
        private javax.swing.JPanel jPanel227;
        private javax.swing.JPanel jPanel228;
        private javax.swing.JPanel jPanel229;
        private javax.swing.JPanel jPanel231;
        private javax.swing.JPanel jPanel233;
        private javax.swing.JPanel jPanel234;
        private javax.swing.JPanel jPanel235;
        private javax.swing.JPanel jPanel236;
        private javax.swing.JPanel jPanel237;
        private javax.swing.JPanel jPanel238;
        private javax.swing.JPanel jPanel239;
        private javax.swing.JPanel jPanel240;
        private javax.swing.JPanel jPanel242;
        private javax.swing.JPanel jPanel243;
        private javax.swing.JPanel jPanel244;
        private javax.swing.JPanel jPanel245;
        private javax.swing.JPanel jPanel246;
        private javax.swing.JPanel jPanel247;
        private javax.swing.JPanel jPanel248;
        private javax.swing.JPanel jPanel249;
        private javax.swing.JPanel jPanel250;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPanel jPanel4;
        private javax.swing.JPanel jPanel5;
        private javax.swing.JPanel jPanel64;
        private javax.swing.JPanel jPanel65;
        private javax.swing.JPanel jPanel66;
        private javax.swing.JPanel jPanel96;
        private javax.swing.JRadioButton jRadioButton21;
        private javax.swing.JRadioButton jRadioButton22;
        private javax.swing.JRadioButton jRadioButton23;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JScrollPane jScrollPane27;
        private javax.swing.JScrollPane jScrollPane3;
        private javax.swing.JScrollPane jScrollPane33;
        private javax.swing.JScrollPane jScrollPane34;
        private javax.swing.JScrollPane jScrollPane35;
        private javax.swing.JScrollPane jScrollPane37;
        private javax.swing.JScrollPane jScrollPane4;
        private javax.swing.JToggleButton jToggleButton1;
        private javax.swing.JTable paidBillsTable;
        private javax.swing.JFormattedTextField paidbillsAmount;
        public static javax.swing.JComboBox paidbillsBankCombo;
        public com.jidesoft.swing.AutoCompletionComboBox paidbillsClass;
        private com.toedter.calendar.JDateChooser paidbillsDate;
        private javax.swing.JTable paidbillsDueTable;
        public javax.swing.JFormattedTextField paidbillsEndingBalance;
        private javax.swing.JTextField paidbillsMemo;
        public javax.swing.JFormattedTextField paidbillsNumber;
        private static com.jidesoft.swing.AutoCompletionComboBox paidbillsPayto;
        private javax.swing.JButton paidbillsUpdateAndPrintButton;
        private javax.swing.JButton paidbillsUpdateButton;
        private javax.swing.JButton payBill;
        private javax.swing.JFormattedTextField paybillsAmount;
        public static javax.swing.JComboBox paybillsBankCombo;
        private javax.swing.JButton paybillsCancelButton;
        private javax.swing.JButton paybillsCancelButton1;
        public javax.swing.JFormattedTextField paybillsCheckNumber;
        private javax.swing.JPanel paybillsCheckbookPanel;
        private javax.swing.JPanel paybillsCheckbookPanel1;
        public com.jidesoft.swing.AutoCompletionComboBox paybillsClass;
        private com.toedter.calendar.JDateChooser paybillsDate;
        private javax.swing.JTable paybillsDueTable1;
        public javax.swing.JFormattedTextField paybillsEndingBalance;
        private javax.swing.JLabel paybillsEntryID;
        private javax.swing.JLabel paybillsEntryID1;
        private javax.swing.JLabel paybillsEntryId;
        private javax.swing.JLabel paybillsEntryId1;
        private javax.swing.JTextField paybillsMemo;
        private javax.swing.JButton paybillsPayButton;
        private javax.swing.JButton paybillsPayandPrint;
        private static com.jidesoft.swing.AutoCompletionComboBox paybillsPayto;
        public static javax.swing.JDialog paybillsPopup;
        private javax.swing.JLabel selectedInvoices;
        private javax.swing.JLabel selectedInvoices1;
        private javax.swing.JTextField txt_Search;
        // End of variables declaration//GEN-END:variables
    private String[] sql = null;
    boolean accountModified = false;
    int checkNo = 0;
    int bankAccountNo = 0;

    private void addNewRow(DefaultTableModel tableModel, String account) {
        String accountName = account;
        tableModel.addRow(new Object[]{
            accountName, "", "", AccountingUtil.getVendorID(billVendor.getSelectedItem().toString())
        });
    }

    private void addComboBoxesToTableForBillExpensesTable() {

        if (AccountingUtil.getAllAccountsFull() != null) {
            Object[] accounts = AccountingUtil.getAllAccountsFull();
            TableColumn column = billExpensesTable.getColumnModel().getColumn(0);

            JComboBox comboBox = new JComboBox();
            for (int j = 0; j < accounts.length; j++) {
                comboBox.addItem(accounts[j]);
            }
            //column.setCellEditor(new DefaultCellEditor(comboBox));
            column.setCellRenderer(new MyComboBoxRenderer(accounts));
            column.setCellEditor(new MyComboBoxEditor(accounts));

        }

    }

    private void addComboBoxesToTableForEditExpensesTable() {

        if (AccountingUtil.getAllAccountsFull() != null) {
            Object[] accounts = AccountingUtil.getAllAccountsFull();

            TableColumn accountColumn = editBillExpensesTable.getColumnModel().getColumn(0);
            JComboBox comboBox = new JComboBox();
            for (int j = 0; j < accounts.length; j++) {
                comboBox.addItem(accounts[j]);
            }
            accountColumn.setCellEditor(new DefaultCellEditor(comboBox));
            accountColumn.setCellRenderer(new MyComboBoxRendererForEditCase(accounts));
        }
    }

    private void addComboBoxesToTableForEditCreditTable() {

        if (AccountingUtil.getAllAccountsFull() != null) {
            Object[] accounts = AccountingUtil.getAllAccountsFull();

            TableColumn accountColumn = editBillExpensesTable1.getColumnModel().getColumn(0);
            JComboBox comboBox = new JComboBox();
            for (int j = 0; j < accounts.length; j++) {
                comboBox.addItem(accounts[j]);
            }
            accountColumn.setCellEditor(new DefaultCellEditor(comboBox));
            accountColumn.setCellRenderer(new MyComboBoxRendererForEditCreditCase(accounts));
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
            if (accountsToBeShownList.isEmpty()) {
                // Select the current value
                setSelectedItem(value);
            }
            return this;
        }
    }

    public class MyComboBoxEditor extends DefaultCellEditor {

        public MyComboBoxEditor(Object[] items) {
            super(new JComboBox(items));
        }
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

    public class MyComboBoxRendererForEditCreditCase extends JComboBox implements TableCellRenderer {

        public MyComboBoxRendererForEditCreditCase(Object[] items) {
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

    public class MyComboBoxEditorForEditCreditCase extends DefaultCellEditor {

        public MyComboBoxEditorForEditCreditCase(Object[] items) {
            super(new JComboBox(items));
        }
    }

    public void reloadVendorsList() {
        //String sql = "Select Distinct VendorName,0.00 From VendorListTable Order By VendorName ASC";
        // TODO: Investigate what is more performance costly, to the calculate the balance everytime or
        // to update the balance when transactions are done by having a balance field on the vendorsTable
        String vendorValue = "";
        String vendorId = "";
        try {

            String sql = "SELECT "
                    //+ "DISTINCT(VendorName) as VendorName, "
                    + "VendorName, "
                    + "(SELECT CASE WHEN SUM(credit - debit) IS NULL THEN 0.00 ELSE "
                    + "CAST(ROUND(SUM(credit - debit),2) AS NUMERIC(10,2)) END FROM AccountingGLTable "
                    + "WHERE GLType IN ('Bill', 'Credit') AND DatePaid IS NULL "
                    + "AND ControlNumber = CONVERT(varchar,A.VendorId)), VendorID "
                    + "FROM VendorListTable A "
                    + "ORDER BY VendorName Asc";

            System.out.println("Reload Vendor List: " + sql);

            //CAST(ROUND(SUM(Debit+Credit),2) AS NUMERIC(10,2))
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);

            //DefaultTableModel aMode   l;
            DefaultTableModel aModel2 = (DefaultTableModel) billsVendorsTable.getModel();

            //aModel = (DefaultTableModel) billsVendorsTable.getModel();
            //AccountingUtil.clearTableModel(aModel)
            AccountingUtil.clearTableModel(aModel2);

            editBillVendor.removeAllItems();
            editBillVendor1.removeAllItems();
            billVendor.removeAllItems();

            BankingPanel.checkbookPayto.addItem("");

            ResultSetMetaData rsmd = rs.getMetaData();
            int colNo = rsmd.getColumnCount();

            while (rs.next()) {
                Object[] values = new Object[colNo];
                for (int i = 0; i < colNo; i++) {
                    values[i] = rs.getObject(i + 1);

                    if (i == 0) {
                        vendorValue = rs.getObject(i + 1).toString();
                        BankingPanel.checkbookPayto.addItem(rs.getObject(i + 1));
                        billVendor.addItem(rs.getObject(i + 1));
                    } else if (i == 1) {
                        values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject(i + 1).toString());
                    } else if (i == 2) {
                        values[i] = rs.getObject(i + 1);
                        vendorId = rs.getObject(i + 1).toString();
                    }
                }
                //System.out.println("Value = " + vendorValue + " ID = " + vendorId);
                if (vendorValue != null && vendorId != null) {
                    editBillVendor.addItem(new ComboItem(vendorValue, vendorId));
                    editBillVendor1.addItem(new ComboItem(vendorValue, vendorId));
                }
                aModel2.addRow(values);
                // REMOVE ID Column
                billsVendorsTable.getColumnModel().getColumn(2).setMinWidth(0);
                billsVendorsTable.getColumnModel().getColumn(2).setMaxWidth(0);
                billsVendorsTable.getColumnModel().getColumn(2).setWidth(0);
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void reloadVendorsListOnSearch(String searchText) {
        try {
            String sql = "SELECT "
                    + "VendorName, "
                    + "(SELECT CASE WHEN SUM(credit - debit) IS NULL THEN 0.00 ELSE "
                    + "CAST(ROUND(SUM(credit - debit),2) AS NUMERIC(10,2)) END FROM AccountingGLTable "
                    + "WHERE GLType IN ('Bill','Credit') AND DatePaid IS NULL "
                    + "AND ControlNumber = CONVERT(varchar,A.VendorId)), VendorID "
                    + "FROM VendorListTable A Where VendorName like '%" + searchText + "%' "
                    + "ORDER BY VendorName Asc";

            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sql);
            DefaultTableModel aModel2 = (DefaultTableModel) billsVendorsTable.getModel();
            AccountingUtil.clearTableModel(aModel2);

            editBillVendor.removeAllItems();
            billVendor.removeAllItems();

            BankingPanel.checkbookPayto.addItem("");

            ResultSetMetaData rsmd = rs.getMetaData();
            int colNo = rsmd.getColumnCount();
            while (rs.next()) {
                Object[] values = new Object[colNo];

                for (int i = 0; i < colNo; i++) {
                    values[i] = rs.getObject(i + 1);

                    if (i == 0) {
                        editBillVendor.addItem(rs.getObject(i + 1));
                        BankingPanel.checkbookPayto.addItem(rs.getObject(i + 1));
                        billVendor.addItem(rs.getObject(i + 1));
                    } else if (i == 1) {
                        values[i] = AccountingUtil.formatAmountForDisplay(rs.getObject(i + 1).toString());
                    } else if (i == 2) {
                        values[i] = rs.getObject(i + 1);
                    }
                }
                aModel2.addRow(values);
                // REMOVE ID Column
                billsVendorsTable.getColumnModel().getColumn(2).setMinWidth(0);
                billsVendorsTable.getColumnModel().getColumn(2).setMaxWidth(0);
                billsVendorsTable.getColumnModel().getColumn(2).setWidth(0);
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void setCheckNo(int checkNumber) {
        this.chkNo = checkNumber;
    }

    public int getCheckNo() {
        return this.chkNo;
    }

    public void setVendorName(String vendor) {
        vendorName = vendor;
    }

    public String getVendorName() {
        return vendorName;
    }
    // Method to set Vendor as selected (who's generated the last Bill)

    public void setVendorAsSelected(String vendorName) {
        String vendor = vendorName;
        int vendorRows = billsVendorsTable.getRowCount();
        for (int i = 0; i < vendorRows; i++) {
            if (vendor.equals(billsVendorsTable.getValueAt(i, 2).toString())) {
                billsVendorsTable.setRowSelectionInterval(i, i);
            }
        }
        billVendor.setSelectedItem(vendorName);
    }

    private void paybillBankSelected() {
        if (!paybillsBankCombo.getSelectedItem().equals("")) {
            try {
                String bankNumber = "" + paybillsBankCombo.getSelectedItem();

                if (paybillsPopup.isActive()) {
                }

                bankNumber = bankNumber.substring(0, bankNumber.indexOf("-"));
                ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet("select "
                        + "sum(Credit-Debit) AS Balance,"
                        + "(Select Top 1 CheckNumber From AccountingCBTable Where BankAccount = " + bankNumber + " AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "' Order By CheckNumber Desc) AS CheckNumber "
                        + "From AccountingGLTable "
                        + "Where AccountNumber = " + bankNumber + " "
                        + "AND LotName = '" + dms.DMSApp.getApplication().getCurrentlotName() + "'");

                if (rs.next()) {
                    paybillsEndingBalance.setValue(rs.getDouble("Balance"));

                    if (rs.getInt("CheckNumber") == 0) {
                        paybillsCheckNumber.setValue(1);
                    } else {
                        paybillsCheckNumber.setValue(rs.getInt("CheckNumber") + 1);
                    }

                }
                rs.getStatement().close();
                //System.out.println("paybillsCheckNumber.getVal := " + paybillsCheckNumber.getValue().toString());
                setCheckNo(Integer.parseInt(paybillsCheckNumber.getValue().toString()));
            } catch (SQLException e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            } catch (Exception e) {
                Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            paybillsEndingBalance.setValue(0.00);
            paidbillsEndingBalance.setValue(0.00);
            // jFormattedTextField33.setValue(0);
        }
    }

    private void showReport(int bankAccountNo, int checkNumber, String checkMemo, String checkDate, String checkAmount, String paidTo, String vendorAddress, String vendorCity, String vendorState, String vendorZip, List<String> checkDetailsList) {

        String reportName = "dms/panels/Bills_Payment_Check.jasper";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(reportName);

        String checkAmountWithoutComma = checkAmount.replaceAll(",", "");
        checkAmountWithoutComma = checkAmountWithoutComma.replaceAll("-", "");
        String amountInWords = EnglishNumberToWords.convert(Long.parseLong(checkAmountWithoutComma.substring(0, checkAmountWithoutComma.indexOf("."))));

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
                String currentElement = checkDetailsList.get(i);
                System.out.println("Element ; " + currentElement);
                String[] currentValArray = currentElement.split("\\*\\*\\*");
                String controlNumber = currentValArray[0];
                String type = currentValArray[1];
                String date = currentValArray[2];
                String lineAmount = currentValArray[3];
                System.out.println("lineAmount: " + currentValArray[3]);

                String memo = currentValArray[4];

                parameters.put("controlNumber" + i, controlNumber);
                parameters.put("type" + i, type);
                parameters.put("date" + i, date);
                parameters.put("amount" + i, lineAmount);
                parameters.put("memo" + i, memo);
            } else {
                parameters.put("controlNumber" + i, " ");
                parameters.put("type" + i, " ");
                parameters.put("date" + i, " ");
                parameters.put("amount" + i, " ");
                parameters.put("memo" + i, " ");
            }
        }

        JasperPrint jp = null;
        try {
            jp = JasperFillManager.fillReport(inputStream, parameters, new JREmptyDataSource());
        } catch (JRException ex) {
            Logger.getLogger(BankingPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        //Viewer for JasperReport
        JRViewer jv = new JRViewer(jp);

        //Insert viewer to a JFrame to make it showable
        JFrame jf = new JFrame();
        jf.getContentPane().add(jv);
        jf.validate();
        jf.setVisible(true);
        jf.setSize(new Dimension(800, 600));
        jf.setLocation(300, 100);
        jf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public List<String> fetchDataForSelectedCheque(int checkNo, int bankAccount) {
        ArrayList<String> checkDetailsList = new ArrayList<String>();
        String memo = "";
        String subQuery = "";
        int paidTo = 0;
        String[] invoices = null;
        String query = "";
        ResultSet rs;

        try {
            query = "SELECT * FROM AccountingCBTable WHERE CheckNumber = '" + checkNo + "' AND BankAccount = '" + bankAccount + "'";

            rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

            while (rs.next()) {
                paidTo = AccountingUtil.getVendorID(rs.getString("PaidTo"));
                invoices = rs.getString("EntryID").split(",");
            }

            for (int i = 0; i < invoices.length; i++) {
                subQuery = "SELECT * FROM AccountingGLTable "
                        + "WHERE ControlNumber = '" + paidTo + "' AND ReferenceNumber = '"
                        + invoices[i].replaceAll("\\s+", "") + "' ";

                rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(subQuery);

                while (rs.next()) {
                    String memoFromDB = rs.getString("Memo");
                    if (memoFromDB == null || memoFromDB.isEmpty()) {
                        memo = " ";
                    } else {
                        memo = memoFromDB;
                    }

                    checkDetailsList.add(
                            invoices[i].replaceAll("\\s+", "") + "***"
                            + rs.getString("GLType") + "***"
                            + AccountingUtil.getStringDateFromUtilDate(rs.getDate("PostDate")) + "***"
                            + AccountingUtil.formatAmountForDisplay(rs.getString("Credit")) + "***"
                            + memo);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountsPayablePanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return checkDetailsList;
    }

    public void printBillPayCheck(int checkNo, int bankAccount) {
        try {
            String checkMemo = "";
            String checkAmount = "";
            String checkDate = "";
            String paidTo = "";
            String query = "";
            String sqlQuery = "";
            String vendorAddress = "";
            String vendorCity = "";
            String vendorState = "";
            String vendorZip = "";

            query = "SELECT * FROM AccountingCBTable WHERE CheckNumber = '" + checkNo + "' AND BankAccount = '" + bankAccount + "'";

            ResultSet rs;
            rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);

            while (rs.next()) {
                checkMemo = rs.getString("Memo");
                checkAmount = AccountingUtil.formatAmountForDisplay(rs.getString("Amount"));
                checkDate = AccountingUtil.getStringDateFromUtilDate(rs.getDate("Date"));
                paidTo = rs.getString("paidTo");
            }

            // fetch vendor information
            sqlQuery = "SELECT VendorAddress, VendorCity, VendorState, VendorZip "
                    + "FROM VendorListTable "
                    + "WHERE VendorName = '" + paidTo + "'";

            rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(sqlQuery);

            while (rs.next()) {
                vendorAddress = rs.getString("VendorAddress");
                vendorCity = rs.getString("VendorCity");
                vendorState = rs.getString("VendorState");
                vendorZip = rs.getString("VendorZip");
            }
            rs.getStatement().close();

            // get the vendors address based on paidTo
            showReport(
                    bankAccount, checkNo, checkMemo, checkDate, checkAmount, paidTo, vendorAddress,
                    vendorCity, vendorState, vendorZip, fetchDataForSelectedCheque(checkNo, bankAccount));

            bankingPanel.updateCheckStatus(checkNo, bankAccount, "Printed");

        } catch (SQLException ex) {
            Logger.getLogger(BankingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
// Class to populate Edit-Bill's Vendors drop-down with ID-Name

class ComboItem {

    private String key;
    private String value;

    public ComboItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
