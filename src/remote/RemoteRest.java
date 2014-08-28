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
		this("http://10.16.140.105:8080/jbpm-console/rest", "admin", "admin");
	}
	
	public RemoteRest(String baseURL, String user, String password) {
		this.baseURL = baseURL;
		this.user = user;
		this.password = password;
	}
	
	//----------
	public BufferedReader createProcessInstance(String deploymentId, String processDefId) {
		String url = String.format("%s/runtime/%s/process/%s/start", baseURL, deploymentId, processDefId);
		
		HttpURLConnection con;
		BufferedReader reader = new BufferedReader(new StringReader("remote return fail"));
		try {
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setRequestMethod("POST");
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
	
	
	//test
//	public static void main(String[] args) {
//		BufferedReader reader = new RemoteRest().createProcessInstance("com.newegg:proj:1.0", "proj.leave_request");
//		try {
//			System.out.println(reader.readLine());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//	}

	
	

}
