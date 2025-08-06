package org.tektutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
public class Hello {

	@RequestMapping("/")
	public String sayHello() {
		String url  = "jdbc:mysql://172.17.0.2:3306/tektutor";
		String user = "root";
		String pass = "root@123";
		String msg = "";

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection( url, user, pass );

	   		Statement statement = conn.createStatement();

       	   		ResultSet resultSet = statement.executeQuery("select * from greeting"); 
           		resultSet.next();

           		msg = resultSet.getString("message"); 
           		statement.close(); 
           		conn.close(); 
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return msg;
	}

}
