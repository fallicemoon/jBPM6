package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.codec.binary.Base64;

public class JbpmRestEntity {	
	
	private String baseURL;
	private String user;
	private String password;
	
	public JbpmRestEntity () {
		
	}

	public JbpmRestEntity (String baseURL, String user, String password) {
		this.baseURL = baseURL;
		this.user = user;
		this.password = password;
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
	
	
	
	BufferedReader connect(String url, String method) {
		HttpURLConnection con;
		BufferedReader reader = new BufferedReader(new StringReader("remote return fail"));
		try {
	        ScriptEngineManager factory = new ScriptEngineManager();
	        ScriptEngine engine = factory.getEngineByName("JavaScript");
	        try {
				url = (String)engine.eval("encodeURI('"+url+"')");
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
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

	

}
