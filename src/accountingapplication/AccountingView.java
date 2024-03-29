/*
 * AccountingView.java
 */
package accountingapplication;

import dms.DMSApp;
import dms.util.AccountingHelper;
import dms.util.AccountingHelperToLoadCustomerLastRecords;
import dms.util.AccountingHelperToLoadCustomers;
import dms.util.AccountingUtil;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * The application's main frame.
 */
public class AccountingView extends FrameView {

    private String currentMode;
    private String targetMode;

    public String getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    public String getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(String targetMode) {
        this.targetMode = targetMode;
    }

    public AccountingView(SingleFrameApplication app) {
        super(app);

        initComponents();
        mainPanel.add(new dms.windows.AccountingWindow(this.getFrame()));
        // Call your Theard class here ( use to load data - like customers, vendor)
        AccountingHelper helper = new AccountingHelper(); 
        Thread myThread = new Thread(helper);
        myThread.start();
        // A Thread to call Customer loading
        AccountingHelperToLoadCustomers customerHelper = new AccountingHelperToLoadCustomers(); 
        Thread customerThread = new Thread(customerHelper);
        customerThread.start();
        // A Thread to call Customer loading for last records
        AccountingHelperToLoadCustomerLastRecords customerHelperForLastRecords = new AccountingHelperToLoadCustomerLastRecords(); 
        Thread customerThreadForLastRecords = new Thread(customerHelperForLastRecords);
        customerThreadForLastRecords.start();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = DMSApp.getApplication().getMainFrame();
            aboutBox = new AccountingAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        DMSApp.getApplication().show(aboutBox);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        SwitchMode_jMenu = new javax.swing.JMenu();
        SwitchMode_jMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        modeMenu = new javax.swing.JMenu();
        switchMode_Dialog = new javax.swing.JDialog();
        model_jLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        port_tf = new javax.swing.JTextField();
        server_tf = new javax.swing.JTextField();
        user_tf = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        password_pf = new javax.swing.JPasswordField();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.GridLayout(1, 0));

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText("File"); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(dms.DMSApp.class).getContext().getActionMap(AccountingView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dms.DMSApp.class).getContext().getResourceMap(AccountingView.class);
        SwitchMode_jMenu.setText(resourceMap.getString("SwitchMode_jMenu.text")); // NOI18N
        SwitchMode_jMenu.setName("SwitchMode_jMenu"); // NOI18N
        SwitchMode_jMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SwitchMode_jMenuMouseClicked(evt);
            }
        });
        SwitchMode_jMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SwitchMode_jMenuActionPerformed(evt);
            }
        });

        SwitchMode_jMenuItem.setText(resourceMap.getString("SwitchMode_jMenuItem.text")); // NOI18N
        SwitchMode_jMenuItem.setName("SwitchMode_jMenuItem"); // NOI18N
        SwitchMode_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SwitchMode_jMenuItemActionPerformed(evt);
            }
        });
        SwitchMode_jMenu.add(SwitchMode_jMenuItem);

        menuBar.add(SwitchMode_jMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        modeMenu.setText(resourceMap.getString("modeMenu.text")); // NOI18N
        modeMenu.setName("modeMenu"); // NOI18N
        modeMenu.setOpaque(true);
        modeMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeMenuActionPerformed(evt);
            }
        });
        menuBar.add(modeMenu);

        switchMode_Dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        switchMode_Dialog.setTitle(resourceMap.getString("switchMode_Dialog.title")); // NOI18N
        switchMode_Dialog.setLocationByPlatform(true);
        switchMode_Dialog.setMinimumSize(new java.awt.Dimension(359, 301));
        switchMode_Dialog.setName("switchMode_Dialog"); // NOI18N

        model_jLabel.setText(resourceMap.getString("model_jLabel.text")); // NOI18N
        model_jLabel.setName("model_jLabel"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        port_tf.setMinimumSize(new java.awt.Dimension(200, 20));
        port_tf.setName("port_tf"); // NOI18N
        port_tf.setPreferredSize(new java.awt.Dimension(200, 20));

        server_tf.setMinimumSize(new java.awt.Dimension(200, 20));
        server_tf.setName("server_tf"); // NOI18N
        server_tf.setPreferredSize(new java.awt.Dimension(200, 20));

        user_tf.setText(resourceMap.getString("user_tf.text")); // NOI18N
        user_tf.setMinimumSize(new java.awt.Dimension(200, 20));
        user_tf.setName("user_tf"); // NOI18N
        user_tf.setPreferredSize(new java.awt.Dimension(200, 20));

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        password_pf.setText(resourceMap.getString("password_pf.text")); // NOI18N
        password_pf.setName("password_pf"); // NOI18N

        javax.swing.GroupLayout switchMode_DialogLayout = new javax.swing.GroupLayout(switchMode_Dialog.getContentPane());
        switchMode_Dialog.getContentPane().setLayout(switchMode_DialogLayout);
        switchMode_DialogLayout.setHorizontalGroup(
            switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(switchMode_DialogLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(model_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addGroup(switchMode_DialogLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1)))))
            .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(switchMode_DialogLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(server_tf, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, switchMode_DialogLayout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(user_tf, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(password_pf)))
                    .addGroup(switchMode_DialogLayout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(port_tf, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(switchMode_DialogLayout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addGap(18, 18, 18)
                                .addComponent(jButton2))))))
        );
        switchMode_DialogLayout.setVerticalGroup(
            switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(switchMode_DialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(model_jLabel)
                .addGap(54, 54, 54)
                .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(switchMode_DialogLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(11, 11, 11)
                        .addComponent(jLabel3)
                        .addGap(11, 11, 11)
                        .addComponent(jLabel4)
                        .addGap(11, 11, 11)
                        .addComponent(jLabel1))
                    .addGroup(switchMode_DialogLayout.createSequentialGroup()
                        .addComponent(user_tf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(password_pf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(server_tf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(port_tf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(40, 40, 40)
                .addGroup(switchMode_DialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void SwitchMode_jMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SwitchMode_jMenuItemActionPerformed
        JMenuItem eventFrom = (JMenuItem) evt.getSource();
        String menuItem = eventFrom.getName();
        System.out.println("Menu Item : " + menuItem);
        String oldMode = "";
        String newMode = "";
        if (menuItem.equalsIgnoreCase("SwitchMode_jMenuItem")) {
            System.out.println("Mouse clicked : Switching Mode ...");
            int port = dms.DMSApp.getApplication().getDBConnection().getPort();
            if (port == 5001) {
                oldMode = "Production";
                newMode = "Development";
            } else if (port == 5002) {
                oldMode = "Development";
                newMode = "Production";
            }
            setCurrentMode(oldMode);
            setTargetMode(newMode);
            model_jLabel.setText(model_jLabel.getText() + getCurrentMode() + ". Switching to " + getTargetMode() + " mode.");
            switchMode_Dialog.setVisible(true);
        }
    }//GEN-LAST:event_SwitchMode_jMenuItemActionPerformed

    private void SwitchMode_jMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SwitchMode_jMenuActionPerformed
    }//GEN-LAST:event_SwitchMode_jMenuActionPerformed

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        switchMode_Dialog.dispose();
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        String user = user_tf.getText();
        String password = password_pf.getText();
        String server = server_tf.getText();
        String port = port_tf.getText();

        if (user.isEmpty()) {
            AccountingUtil.showMessageBalloon("User field empty", user_tf);
            return;
        } else if (password.isEmpty()) {
            AccountingUtil.showMessageBalloon("Password field empty", password_pf);
            return;
        } else if (server.isEmpty()) {
            AccountingUtil.showMessageBalloon("Server field empty", server_tf);
            return;
        } else if (port.isEmpty()) {
            AccountingUtil.showMessageBalloon("Port field empty", port_tf);
            return;
        }
        try {
            int targetPort = Integer.valueOf(port);
            if (targetPort == 5001) {
                setTargetMode("Production");
            } else if (targetPort == 5002) {
                setTargetMode("Development");
            }
        } catch (NumberFormatException nfe) {
            dms.DMSApp.displayMessage(switchMode_Dialog, "Port field cannot contain non-integer values.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (getCurrentMode().equalsIgnoreCase(getTargetMode())) {
            dms.DMSApp.displayMessage(switchMode_Dialog, "You are already running " + getTargetMode() + " mode.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dms.DMSApp.getApplication().setDBConnection(user, password, server, port);
        if (dms.DMSApp.getApplication().getDBConnection() != null && String.valueOf(dms.DMSApp.getApplication().getDBConnection().getPort()).equals(port)) {
            String dbParamsString = user + "***" + password + "***" + server + "***" + port;
            //AccountingUtil.saveDBConnectionProperties(dbParamsString);
            dms.DMSApp.displayMessage(switchMode_Dialog, "Mode switched to " + getTargetMode() + " Restarting application ...", JOptionPane.INFORMATION_MESSAGE);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AccountingView.class.getName()).log(Level.SEVERE, null, ex);
            }
            AccountingUtil.restartAcocuntingApp();
        } else {
            dms.DMSApp.displayMessage(switchMode_Dialog, "Incorrect connection details, continuing with " + getCurrentMode() + " mode.", JOptionPane.INFORMATION_MESSAGE);
        }
        switchMode_Dialog.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void SwitchMode_jMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SwitchMode_jMenuMouseClicked
    }//GEN-LAST:event_SwitchMode_jMenuMouseClicked

    private void modeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeMenuActionPerformed
    }//GEN-LAST:event_modeMenuActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu SwitchMode_jMenu;
    private javax.swing.JMenuItem SwitchMode_jMenuItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    public static javax.swing.JMenu modeMenu;
    private javax.swing.JLabel model_jLabel;
    private javax.swing.JPasswordField password_pf;
    private javax.swing.JTextField port_tf;
    private javax.swing.JTextField server_tf;
    private javax.swing.JDialog switchMode_Dialog;
    private javax.swing.JTextField user_tf;
    // End of variables declaration//GEN-END:variables
    private JDialog aboutBox;

    public static void setModeMenuValues(String mode, Color color) {
        modeMenu.setText(modeMenu.getText() + " " + mode);
        modeMenu.setBackground(color);
        modeMenu.repaint();
        modeMenu.revalidate();
    }
}
