/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
 
public class JDBCExample {
 

    public JDBCExample()
    {}
    
    public Connection connectToMysql() {
 
	System.out.println("-------- MySQL JDBC Connection Testing ------------");
 
	try {
		Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException e) {
		System.out.println("Where is your MySQL JDBC Driver?");
		e.printStackTrace();
	}
 
	System.out.println("MySQL JDBC Driver Registered!");
	Connection connection = null;
 
	try {
		connection = DriverManager
		.getConnection("jdbc:mysql://mysql.gbrnd.com:3306/automatrix","automatrix", "4ut0m4tr1x");
 
	} catch (SQLException e) {
		System.out.println("Connection Failed! Check output console");
		e.printStackTrace();
	}
 
	if (connection != null) {
		System.out.println("You made it, take control your database now!");
	} else {
		System.out.println("Failed to make connection!");
	}
        return connection;
  }
}