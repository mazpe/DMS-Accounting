/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import accountingapplication.AccountingView;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class DMSDBConnection {

    private static Connection lConnection = null;
    private static SQLServerDataSource dataSource;
    //private String dataBaseName = "0_Development";
    private String dataBaseName = "1_FLORIDAFINECARS";
    private int port;
    private Connection existingConnection = null;

    public Connection getExistingConnection() {
        return existingConnection;
    }

    public void setExistingConnection(Connection existingConnection) {
        this.existingConnection = existingConnection;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setData(String cDBName) throws SQLServerException, SQLException {
        dataBaseName = cDBName;
        dataSource.setDatabaseName(dataBaseName);
        lConnection = dataSource.getConnection();
        lConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }

    public void startInitialConnection(Map<String, String> dbConnectionProperties) {
        try {
            dataSource = new SQLServerDataSource();
            String mode = "Development";
            if (dbConnectionProperties.get("port").equals("5001")) {
                mode = "Production";
            }
            int portNum = Integer.valueOf(dbConnectionProperties.get("port"));
            dataSource.setServerName(dbConnectionProperties.get("serverName"));
            dataSource.setUser(dbConnectionProperties.get("user"));
            dataSource.setPassword(dbConnectionProperties.get("password"));
            dataSource.setPortNumber(portNum);
            System.out.println("Mode: " + mode);

            Color color = Color.yellow;
            if (mode.equalsIgnoreCase("Development")) {
                System.out.println("Setting green ...");
                color = Color.green;
            } else if (mode.equalsIgnoreCase("Production")) {
                color = Color.red;
                System.out.println("Setting red ...");
            }
            AccountingView.setModeMenuValues(mode, color);

            /*
             if (production) {
             dataSource.setServerName("localhost");
             dataSource.setUser("user");
             dataSource.setPassword("password");
             dataSource.setPortNumber(5001);
             } else {
             dataSource.setServerName("localhost");
             dataSource.setUser("user");
             dataSource.setPassword("password");
             dataSource.setPortNumber(5002);
             }
             */

            //dataSource.setDatabaseName("0_Development");
            dataSource.setDatabaseName("1_DEALER");
            dataSource.setLoginTimeout(5);
            dataSource.setIntegratedSecurity(false);
            dataSource.setApplicationName("Automatrix " + dms.DMSApp.appVersion);
            dataSource.setWorkstationID(dms.DMSApp.localHostName + ": " + dms.DMSApp.localHostAddress);
            dataSource.setInstanceName("Automatrix");
            dataSource.setTrustServerCertificate(true);
            dataSource.setLockTimeout(45);

            lConnection = dataSource.getConnection();
            lConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            setExistingConnection(lConnection);
            setPort(portNum);
        } catch (SQLException e) {
            StringTokenizer tokens = new StringTokenizer(e.getLocalizedMessage());
            String finalMessage = "";
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
            if (lConnection == null) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Database connection could not be established.\nError Code: " + finalMessage + "\nClose the System?", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {
                    System.exit(0);
                } else {
                    startInitialConnection(dbConnectionProperties);
                }
            } else {
                System.out.println("Continuing with existing connection ...");
            }
        }
    }

    public boolean executeStatements(String[] sqls, Component component) {
        boolean returnValue = false;
        Savepoint save = null;
        Connection con = null;
        PreparedStatement[] ps = new PreparedStatement[sqls.length];
        try {
            dms.DMSApp.setSystemBusy(true);
            con = getLotConnection(true);
            con.clearWarnings();
            con.setAutoCommit(false);
            save = con.setSavepoint();
            for (int i = 0; i < ps.length; i++) {
                if (!sqls[i].isEmpty() && !sqls[i].equals(" ") && sqls[i] != null) {
                    ps[i] = con.prepareStatement(con.nativeSQL(sqls[i]), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    ps[i].executeUpdate();
                }
            }
            con.commit();

            returnValue = true;
        } catch (SQLException e) {
            String errorMessage = e.getLocalizedMessage();
            try {
                if (save != null && con != null) {
                    con.rollback(save);
                } else if (save == null && con != null) {
                    con.rollback();
                }

                if (con != null) {
                    con.setAutoCommit(true);
                }

            } catch (SQLException ez) {
                errorMessage = errorMessage + "\n " + ez.getLocalizedMessage();
            } finally {
                returnValue = false;
                dms.DMSApp.displayMessage(component, errorMessage, JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try {
                for (int i = 0; i < ps.length; i++) {
                    if (ps[i] != null) {
                        ps[i].close();
                    }
                }

                if (con != null) {
                    con.setAutoCommit(true);
                }

            } catch (SQLException eh) {
            }

            dms.DMSApp.setSystemBusy(false);
            return returnValue;
        }
    }

    public boolean executeStatements(String[] sqls, Component component, boolean reset) {
        boolean returnValue = false;
        Savepoint save = null;
        Connection con = null;
        PreparedStatement[] ps = new PreparedStatement[sqls.length];
        try {
            dms.DMSApp.setSystemBusy(true);
            con = getLotConnection(reset);
            con.clearWarnings();
            con.setAutoCommit(false);
            save = con.setSavepoint();
            for (int i = 0; i < ps.length; i++) {
                if (!sqls[i].isEmpty() && !sqls[i].equals(" ") && sqls[i] != null) {
                    ps[i] = con.prepareStatement(con.nativeSQL(sqls[i]), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    ps[i].executeUpdate();
                }
            }
            con.commit();

            returnValue = true;
        } catch (SQLException e) {
            String errorMessage = e.getLocalizedMessage();
            try {
                if (save != null && con != null) {
                    con.rollback(save);
                } else if (save == null && con != null) {
                    con.rollback();
                }

                if (con != null) {
                    con.setAutoCommit(true);
                }

            } catch (SQLException ez) {
                errorMessage = errorMessage + "\n " + ez.getLocalizedMessage();
            } finally {
                returnValue = false;
                dms.DMSApp.displayMessage(component, errorMessage, JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try {
                for (int i = 0; i < ps.length; i++) {
                    if (ps[i] != null) {
                        ps[i].close();
                    }
                }

                if (con != null) {
                    con.setAutoCommit(true);
                }

            } catch (SQLException eh) {
            }

            dms.DMSApp.setSystemBusy(false);
            return returnValue;
        }
    }

    public void closeConnection() throws SQLException {
        if (!lConnection.isClosed()) {
            lConnection.close();
        }
    }

    public Connection getLotConnection(boolean reset) throws SQLException {
        if (!lConnection.isValid(10)) {
            lConnection = dataSource.getConnection();
            lConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        }

        return lConnection;
    }

    public ResultSet getResultSet(String sql) throws SQLException {
        return getLotConnection(true).prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery();
    }

    public ResultSet getResultSet(String sql, boolean reset) throws SQLException {
        return getLotConnection(reset).prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery();
    }

    public String getDatabaseName() {
        return dataBaseName;
    }
}
