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
public class AccountingHelperToLoadCustomers implements Runnable {

    public ArrayList customers = new ArrayList();
    public static ArrayList allCustomers = new ArrayList();
    public static Map<String, Integer> customersIDsMap = new HashMap<String, Integer>();
    int customerCount = 0;
    int customerRecords = 0;

    public void run() {
        System.out.println("Inside run method..AccountingHelperToLoadCustomers...");
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
            System.out.println("CustomerCount = " + customerCount);
            customerRecords = customerCount / 10;
            //System.out.println("customerRecords : " + customerRecords);
            loadFirstHalfOfCustomerRecords(customerRecords);
        } catch (SQLException ex) {
            Logger.getLogger(AccountingHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Fist Half of Customers Records
    public void loadFirstHalfOfCustomerRecords(int count) {
        customersIDsMap.clear();
        try {
            String query = "SELECT TOP " + count + " CustomerCode,(FirstName+ ' ' + MiddleName +' ' + LastName) as CustomerName FROM CustomerTable Order by CustomerCode ASC ";
            //System.out.println("Q1 Customers = " + query);
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            while (rs.next()) {
                String customerName = rs.getString("CustomerName").toString().replaceAll("\\s+", " ");
                int customerCode = rs.getInt("CustomerCode");
                customers.add(customerName);
                customersIDsMap.put(customerName, customerCode);
            }
            setCustomersIDsMap(customersIDsMap);
            System.out.println("Customers loaded fist half ...");
            //System.out.println("CustomerName : " + customers);
        } catch (SQLException ex) {
            Logger.getLogger(AccountingHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Map<String, Integer> getCustomersIDsMap() {
        return customersIDsMap;
    }

    public static void setCustomersIDsMap(Map<String, Integer> customersIDsMap) {
        AccountingHelperToLoadCustomers.customersIDsMap = customersIDsMap;
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
