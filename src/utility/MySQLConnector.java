package utility;


import java.io.IOException;

import java.sql.*;





public class MySQLConnector {
	Connection con;
	PreparedStatement workItemIdPS;

	public MySQLConnector() throws ClassNotFoundException, SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://10.16.140.105:3306/jbpm", "root", "qwerty");
			workItemIdPS = con.prepareStatement("SELECT id, workItemId, processInstanceId FROM `Task` WHERE `formName`=? and status=?");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public int getAllWorkItemId(String formName) throws SQLException, IOException {
		workItemIdPS = con.prepareStatement("SELECT workItemId FROM `Task` WHERE `formName`=?");
		workItemIdPS.setString(1, formName);
		return workItemIdPS.executeQuery().getInt("workItemId");
	}
	
	public int getReadyWorkItemId(String formName) throws SQLException, IOException {
		workItemIdPS.setString(1, formName);
		workItemIdPS.setString(2, "Ready");
		return workItemIdPS.executeQuery().getInt("workItemId");
	}
	
	public int getInProgressWorkItemId(String formName) throws SQLException, IOException {
		workItemIdPS.setString(1, formName);
		workItemIdPS.setString(2, "Progress");
		return workItemIdPS.executeQuery().getInt("workItemId");
	}
	
	public int getCompleteWorkItemId(String formName) throws SQLException, IOException {
		workItemIdPS.setString(1, formName);
		workItemIdPS.setString(2, "Complete");
		return workItemIdPS.executeQuery().getInt("workItemId");
	}

}
