/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Farrukh Bashir
 */
public class AccountingHelperToLoadCustomerLastRecords implements Runnable {

    public ArrayList customers = new ArrayList();
    public static ArrayList allCustomers = new ArrayList();
    public static Map<String, Integer> customersIDsMap = new HashMap<String, Integer>();
    int customerCount = 0;
    int customerRecords = 0;
    int remainingCustomerRecords = 0;

    public void run() {
        System.out.println("Inside run method..AccountingHelperToLoadCustomers. Last Records...------");
        loadAllCustomers();
        setAllCustomers(customers);
    }

    public void loadAllCustomers() {
        try {
            String query = "SELECT count(*) as CustomerCount FROM CustomerTable";
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            while (rs.next()) {
                customerCount = rs.getInt("CustomerCount");
            }
            //System.out.println("CustomerCount 222= " + customerCount);
            customerRecords = customerCount/10;
            //System.out.println("customerRecords 222: " + customerRecords);
            remainingCustomerRecords = customerCount - customerRecords;
            //System.out.println("remainingCustomerRecords := " + remainingCustomerRecords);
            loadSecondHalfOfCustomerRecords(remainingCustomerRecords);
        } catch (SQLException ex) {
            Logger.getLogger(AccountingHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    // 2nd Half of Customers Records
    public void loadSecondHalfOfCustomerRecords(int count) {
        //customersIDsMap.clear();
        try {
            String query = "SELECT TOP "+ count +" CustomerCode,(FirstName+ ' ' + MiddleName +' ' + LastName) as CustomerName FROM CustomerTable Order by CustomerCode DESC ";
            //System.out.println("Q2 Customers qry = " + query);
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            while (rs.next()) {
                String customerName = rs.getString("CustomerName").toString().replaceAll("\\s+", " ");
                int customerCode = rs.getInt("CustomerCode");
                customers.add(customerName);
                AccountingHelperToLoadCustomers.customersIDsMap.put(customerName, customerCode);
            }
            AccountingHelperToLoadCustomers.setCustomersIDsMap(AccountingHelperToLoadCustomers.customersIDsMap);
            System.out.println("Customers loaded 2nd half...");
        } catch (SQLException ex) {
            Logger.getLogger(AccountingHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Map<String, Integer> getCustomersIDsMap() {
        return customersIDsMap;
    }

    public static void setCustomersIDsMap(Map<String, Integer> customersIDsMap) {
        AccountingHelperToLoadCustomerLastRecords.customersIDsMap = customersIDsMap;
    }

    // Setter Methods
    public void setAllCustomers(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            allCustomers.add(list.get(i));
        }
    }

    // getter Methods
    public static ArrayList getAllCustomers() {
        return allCustomers;
    }
}
