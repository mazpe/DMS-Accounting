/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Administrator
 */
public class DMSUserPermissions
{
  public boolean mainInventory = false; 
  public boolean mainDeals = false; 
  public boolean mainBHPH = false; 
  public boolean mainDesking = false; 
  public boolean mainDashboard = false; 
  public boolean mainSettings = false; 
  public boolean mainAppointments = false; 
  public boolean mainCustomer = false; 
  public boolean mainReports = false; 
  public boolean mainCRM = false; 
  public boolean mainAccounting = false; 
  public boolean mainService = false; 
  public boolean inventoryAdd = false; 
  public boolean inventoryEdit = false; 
  public boolean inventoryDelete = false; 
  public boolean dealsAdd = false; 
  public boolean dealsEdit = false; 
  public boolean dealsDelete = false; 
  public boolean dealsTransfer = false;
  public boolean dealsFunding = false; 
  public boolean dealsFlags = false; 
  public boolean dealsFullEdit = false; 
  public boolean bhphAdd = false; 
  public boolean bhphEdit = false; 
  public boolean bhphDelete = false; 
  public boolean crmAdd = false; 
  public boolean crmEdit = false; 
  public boolean crmDelete = false; 
  public boolean settingsEmployees = false; 
  public boolean settingsUsers = false; 
  public boolean settingsCompany = false; 
  public boolean settingsLots = false; 
  public boolean serviceOpenRO = false; 
  public boolean serviceDeleteCustomer = false; 
  public boolean serviceEditRO = false; 
  public boolean serviceVoidRO = false; 
  public boolean serviceTransferRO = false;
  public boolean serviceAddDeleteParts = false;
  public boolean serviceLaborHours = false;
  public boolean serviceOverrideLaborRate = false;
  public boolean serviceOverridePartPrice = false;
  public boolean accountingCollect = false; 
  public boolean accountingPayouts = false; 
  public boolean accountingTab = false; 
  public boolean accountingTakePayment = false; 
  public boolean accountingNewPayout = false; 
  public boolean accountingEditTranaction = false; 
  public boolean accountingDeleteTransaction = false; 
  public boolean accountingAssignTransaction = false;
  
  public boolean isManager = false;
  public boolean isBDC = false;
  public boolean isSalesman = false;
  
  public void loadPermissions(String user) throws IOException, SQLException
  {
    ResultSet rs = dms.DMSApp.getApplication().getDBConnection().getResultSet("Select A.*,B.EmployeeType "
    + "From DMSData..UserPermissions A "
    + "Join DMSData..UsersTable B ON B.Username = A.Username "
    + "Where A.Username = '" +user+ "'");
    
    if(rs.next())
    {
      mainInventory = rs.getBoolean("NV_Inventory");
      mainDeals = rs.getBoolean("NV_Sales");
      mainBHPH = rs.getBoolean("NV_Payments");
      mainDesking = rs.getBoolean("NV_Desking");
      mainDashboard = rs.getBoolean("NV_Dashboard");
      mainSettings = rs.getBoolean("NV_Settings");
      mainAppointments = rs.getBoolean("NV_Appoint");
      mainCustomer = rs.getBoolean("NV_Customer");
      mainReports = rs.getBoolean("NV_Reports");
      mainCRM = rs.getBoolean("NV_CRM");
      mainAccounting = rs.getBoolean("NV_Cashier");
      mainService = rs.getBoolean("NV_Service");
      inventoryAdd = rs.getBoolean("Inv_Add");
      inventoryEdit = rs.getBoolean("Inv_Edit");
      inventoryDelete = rs.getBoolean("Inv_Del");
      dealsAdd = rs.getBoolean("Sales_Add");
      dealsEdit = rs.getBoolean("Sales_Edit");
      dealsDelete = rs.getBoolean("Sales_Del");
      dealsTransfer = rs.getBoolean("Sales_Trans");
      dealsFunding = rs.getBoolean("Sales_Fund");
      dealsFlags = rs.getBoolean("Sales_Flag");
      dealsFullEdit = rs.getBoolean("Sales_FullEdit");
      bhphAdd = rs.getBoolean("Pay_Add");
      bhphEdit = rs.getBoolean("Pay_Edit");
      bhphDelete = rs.getBoolean("Pay_Del");
      crmAdd = rs.getBoolean("Cust_Add");
      crmEdit = rs.getBoolean("Cust_Edit");
      crmDelete = rs.getBoolean("Cust_Del");
      settingsEmployees = rs.getBoolean("Set_Emp");
      settingsUsers = rs.getBoolean("Set_User");
      settingsCompany = rs.getBoolean("Set_Comp");
      settingsLots = rs.getBoolean("Set_Lots");
      serviceOpenRO = rs.getBoolean("Service_OpenRO");
      serviceDeleteCustomer = rs.getBoolean("Service_DeleteCust");
      serviceEditRO = rs.getBoolean("Service_EditRO");
      serviceVoidRO = rs.getBoolean("Service_VoidRO");
      serviceTransferRO = rs.getBoolean("Service_TransferRO");
      serviceAddDeleteParts = rs.getBoolean("Service_AddDeleteParts");
      serviceLaborHours = rs.getBoolean("Service_LaborHours");
      serviceOverrideLaborRate = rs.getBoolean("Service_OverrideLaborRate");
      serviceOverridePartPrice = rs.getBoolean("Service_OverridePartPrice");
      accountingCollect = rs.getBoolean("Cashier_Collect");
      accountingPayouts = rs.getBoolean("Cashier_Pay");
      accountingTab = rs.getBoolean("Cashier_Acct");
      accountingTakePayment = rs.getBoolean("Cashier_TakePay");
      accountingNewPayout = rs.getBoolean("Cashier_NewPay");
      accountingEditTranaction = rs.getBoolean("Cashier_EditTrans");
      accountingDeleteTransaction = rs.getBoolean("Cashier_DelTrans");
      accountingAssignTransaction = rs.getBoolean("Cashier_AssignTrans");
      
      isManager = rs.getString("EmployeeType").contains("Manager") || rs.getString("EmployeeType").contains("Director");
      isBDC = rs.getString("EmployeeType").contains("BDC");
      isSalesman = rs.getString("EmployeeType").contains("BDC");
      isSalesman = rs.getString("EmployeeType").equalsIgnoreCase("Salesman");
    }
    rs.getStatement().close();
  }
  
  public boolean isManager()
  {
    return isManager;
  }
  
  public boolean isBDC()
  {
    return isBDC;
  }
  
  public boolean isSalesman()
  {
    return isSalesman;
  }
}
