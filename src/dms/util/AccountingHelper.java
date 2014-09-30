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
public class AccountingHelper implements Runnable {

    public ArrayList customers = new ArrayList();
    public static ArrayList allCustomers = new ArrayList();
    ArrayList vendors = new ArrayList();
    public static ArrayList allVendors = new ArrayList();
    ArrayList employees = new ArrayList();
    public static ArrayList allEmployees = new ArrayList();
    public static Map<String, Integer> customersIDsMap = new HashMap<String, Integer>();

    public void run() {
        System.out.println("Inside run method..AccountingHelper !");
        //loadAllCustomers();
        //setAllCustomers(customers);
        loadAllVendors();
        setAllVendors(vendors);
        loadAllEmployees();
        setAllEmployees(employees);
    }

    // Load all customers
    /*public void loadAllCustomers() {
        customersIDsMap.clear();
        try {
            String query = "SELECT CustomerCode,(FirstName+ ' ' + MiddleName +' ' + LastName) as CustomerName FROM CustomerTable";
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            while (rs.next()) {
                String customerName = rs.getString("CustomerName").toString().replaceAll("\\s+", " ");
                int customerCode = rs.getInt("CustomerCode");
                customers.add(customerName);
                customersIDsMap.put(customerName, customerCode);
            }
            setCustomersIDsMap(customersIDsMap);
            System.out.println("Customers loaded ...");
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
        AccountingHelper.customersIDsMap = customersIDsMap;
    }*/

    // Load All Vendors
    public void loadAllVendors() {

        try {
            String query = "SELECT "
                    + "VendorName "
                    + "FROM VendorListTable ";

            //System.out.println("Vendor List " + sql);
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            while (rs.next()) {
                vendors.add(rs.getString("VendorName").toString().replaceAll("\\s+", " "));
            }
            System.out.println("Vendors loaded ...");
        } catch (SQLException e) {
            Logger.getLogger(AccountingHelper.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    // Load all Employees
    public void loadAllEmployees() {
        try {
            String query = "SELECT "
                    + "FirstName, LastName "
                    + "FROM [DMSData]..[UsersTable] ";

            //System.out.println("User List " + sql);
            ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet(query);
            while (rs.next()) {
                employees.add(rs.getString("FirstName") + " " + rs.getString("LastName"));
            }
            System.out.println("Employees loaded ...");
        } catch (SQLException e) {
            Logger.getLogger(AccountingHelper.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    // Setter Methods
    /*public void setAllCustomers(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            allCustomers.add(list.get(i));
        }
    }*/

    public void setAllVendors(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            allVendors.add(list.get(i));
        }
    }

    public void setAllEmployees(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            allEmployees.add(list.get(i));
        }
    }

    // getter Methods
    /*public static ArrayList getAllCustomers() {
        return allCustomers;
    }*/

    public static ArrayList getAllVendors() {
        return allVendors;
    }

    public static ArrayList getAllEmployees() {
        return allEmployees;
    }
}
