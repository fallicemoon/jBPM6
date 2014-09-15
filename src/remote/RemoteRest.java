package remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;


public class RemoteRest {
	
	String baseURL;
	String user;
	String password;

	public RemoteRest() {
		setBaseURL("http://10.16.140.105:8080/jbpm-console/rest");
		setUser("henryManager");
		setPassword("123");
	}
	
	
	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	//private method
	private BufferedReader connect(String url, String method) {
		HttpURLConnection con;
		BufferedReader reader = new BufferedReader(new StringReader("remote return fail"));
		try {
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setRequestMethod(method);
			String authHeader = Base64.encodeBase64String(
					String.format("%s:%s", user, password).getBytes("UTF-8"))
					.trim();
			con.setRequestProperty("Authorization", "Basic ".concat(authHeader));
			con.setRequestProperty("Accept", "application/json");
			
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
		} catch (IOException e) {
			System.out.println("connection fail, please check internet config");
			e.printStackTrace();
		}
		
		return reader;
	}


	//----------
	public BufferedReader createProcessInstance(String deploymentId, String processDefId) {
		String url = String.format("%s/runtime/%s/process/%s/start?map_reason=aaa&map_days=3", baseURL, deploymentId, processDefId,"3");
		return connect(url,"POST");
	}
	

	
	public BufferedReader startTask(String taskId) {
		String url = String.format("%s/task/%s/start", baseURL, taskId);
		return connect(url, "POST");
	}
	
	public BufferedReader completeTask(String taskId) {
		String url = String.format("%s/task/%s/complete?map_isApproved_output=true", baseURL, taskId);
		return connect(url, "POST");
	}

	
	//test
	public static void main(String[] args) throws IOException {
		//BufferedReader reader = new RemoteRest().createProcessInstance("com.newegg.henry:henry_proj:1.0", "henry_project.leave_request");
        //BufferedReader reader = new RemoteRest().startTask("146");
		BufferedReader reader = new RemoteRest().completeTask("146");
		System.out.println(reader.readLine());
		
	}

	
	

}
