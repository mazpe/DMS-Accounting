/*
 * DMSApp.java
 */
package dms;

import accountingapplication.AccountingView;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import dms.util.AccountingUtil;
import java.awt.Component;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class DMSApp extends SingleFrameApplication {

		private static String user = "";
		public static String jvmVersion;
		public static String jvmSecurityPath;
		public static String jvmLibraryPathForward = "";
		public static String jvmLibraryPathReverse = "";
		public static String appVersion = "";
		public static String localHostName = "";
		public static String localHostAddress = "";
		private static boolean systemBusy = false;
		public static boolean isRemoteDB;
		public static boolean isProduction;
		private static ImageIcon errorIcon = null;
		private static ImageIcon warningIcon = null;
		private static ImageIcon infoIcon = null;
		private static ImageIcon successIcon = null;
		public static int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
		public static int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
		public static int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;

		public static void setSystemBusy(boolean busy) {
				systemBusy = busy;
		}

		public static void displayMessage(Component component, String message, int messageType) {
				String finalMessage = "";
				if (message != null) {
						if (message.contains("\n")) {
								finalMessage = message;
						} else {
								StringTokenizer tokens = new StringTokenizer(message);
								int currentCount = 0;
								while (tokens.hasMoreTokens()) {
										currentCount++;
										if (currentCount <= 10) {
												finalMessage = finalMessage + " " + tokens.nextToken();
										} else if (currentCount > 10) {
												currentCount = 0;
												finalMessage = finalMessage + " " + tokens.nextToken() + "\n";
										}
								}
						}
				}

				String messageTypeString = "";
				ImageIcon messageIcon = null;

				if (messageType == JOptionPane.ERROR_MESSAGE) {
						messageTypeString = "ERROR";
						messageIcon = errorIcon;
				} else if (messageType == JOptionPane.WARNING_MESSAGE) {
						messageTypeString = "WARNING";
						messageIcon = warningIcon;
				} else if (messageType == JOptionPane.INFORMATION_MESSAGE) {
						messageTypeString = "INFORMATION";
						if (finalMessage.toUpperCase().contains("SUCCESS") || finalMessage.toUpperCase().contains("SUCCESSFUL") || finalMessage.toUpperCase().contains("SUCCESSFUL.")) {
								messageIcon = successIcon;
						} else {
								messageIcon = infoIcon;
						}
				}

				if (component != null) {
						if (!finalMessage.contains("Lock request time out")) {
								JOptionPane.showMessageDialog(component, finalMessage, messageTypeString, messageType, messageIcon);
						}
				} else {
						if (!finalMessage.contains("Lock request time out")) {
								JOptionPane.showMessageDialog(null, "UNKNOWN Component:\n" + finalMessage, messageTypeString, messageType, messageIcon);
						}
				}
		}

		public static DMSApp getApplication() {
				return Application.getInstance(DMSApp.class);
		}
		private String fullUsername;
		private String companyC;
		private String lotName = "MIAMI"; // LAM: default " "
		private boolean isAdmin = false;
		private dms.util.DMSDBConnection dbConnection = null;
		private dms.util.DMSUserPermissions userPermissions = null;

		public void setLot(String lotname, String lottype, boolean isInHouse, String orp, boolean isTimedout) throws SQLException, InterruptedException {
				lotName = lotname;
		}

		public void setUserData(String username, String userPassword, String userShortName, String userDataLocation, boolean userActive, boolean userIsSupervisor, String companyCode, boolean userMessage, String companyDBName) throws SQLServerException, SQLException {
				user = userShortName;
				fullUsername = username;
				companyC = companyCode;
				isAdmin = userIsSupervisor;
				getDBConnection().setData(companyDBName);
		}

		public dms.util.DMSDBConnection getDBConnection() {
				if (dbConnection == null) {
						//String paramsFromFile = AccountingUtil.getDBConnectionParamsFromFile();

						String paramsFromFile = "user***password***localhost***5001";;
						System.out.println("DB settings from file ####### " + paramsFromFile);
						String[] paramsFromFileArray = paramsFromFile.split("\\*\\*\\*");
						setDBConnection(paramsFromFileArray[0], paramsFromFileArray[1], paramsFromFileArray[2], paramsFromFileArray[3]);
				}
				return dbConnection;
		}

		public void setDBConnection(String user, String password, String serverName, String port) {
				Map<String, String> dbConnectionProperties = new HashMap<String, String>();
				dbConnectionProperties.put("user", user);
				dbConnectionProperties.put("password", password);
				dbConnectionProperties.put("serverName", serverName);
				dbConnectionProperties.put("port", port);
				dbConnection = new dms.util.DMSDBConnection();
				dbConnection.startInitialConnection(dbConnectionProperties);
		}

		public dms.util.DMSUserPermissions getPermissions() {
				if (userPermissions == null) {
						userPermissions = new dms.util.DMSUserPermissions();
						try {
								userPermissions.loadPermissions(user);
						} catch (Exception e) {
								e.printStackTrace();
						}
				}

				return userPermissions;
		}

		public String getUser() {
				return user;
		}

		public String getFullUsername() {
				return fullUsername;
		}

		public String getCompanyCode() {
				return companyC;
		}

		public String getCurrentlotName() {
				return lotName;
		}

		public boolean isAdmin() {
				return isAdmin;
		}

		public boolean isSystemBusy() {
				return systemBusy;
		}

		public static void main(String[] args) {
				com.jidesoft.utils.Lm.verifyLicense("DMS", "DMS", "V7M8Z2fC8WUgUI3DY0jpveOf1X5.aCX");
				com.miginfocom.util.LicenseValidator.setLicenseKey("Cu=CS6014;Co=CS;Dm=false;Pd=C;V=6;Ex=0;SignCode=3F;Signature=302D021500820E1073DC5E560C1942DEA659E2191D53500BB002147EE76DC8C791E10C2922B8A42BAF69F7760C1461");
				launch(DMSApp.class, args);
		}

		@Override
		protected void startup() {
				show(new AccountingView(this));
		}
}
