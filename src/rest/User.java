package rest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/user")
public class User {
	String usersPropertiesPath;
	String rolesPropertiesPath;

	public User() {
		this("/home/el43/jbpm-installer/jboss-as-7.1.1.Final/standalone/configuration/users.properties", "/home/el43/jbpm-installer/jboss-as-7.1.1.Final/standalone/configuration/roles.properties");
	}
	
	public User(String usersPropertiesPath, String rolesPropertiesPath) {
		this.usersPropertiesPath = usersPropertiesPath;
		this.rolesPropertiesPath = rolesPropertiesPath;
	} 
	
	//properties with I/O	
	private Properties[] getProperties() throws IOException {
		Properties usersProperties = new Properties();
		Properties rolesProperties = new Properties();
		
		FileInputStream usersFis = new FileInputStream(usersPropertiesPath);
		FileInputStream rolesFis = new FileInputStream(rolesPropertiesPath);
			
		usersProperties.load(usersFis);
		rolesProperties.load(rolesFis);
		
		return new Properties[]{usersProperties, rolesProperties};
	}
	
	private void setProperties(Properties usersProperties, Properties rolesProperties) throws IOException {
		FileOutputStream usersFos = new FileOutputStream(usersPropertiesPath);
		FileOutputStream rolesFos = new FileOutputStream(rolesPropertiesPath);
			
		usersProperties.store(usersFos, null);
		rolesProperties.store(rolesFos, null);
	}
	
	private String getSuccessResponseJson(Map<String, Object> responseMap) {
		JSONObject responseJsonObject = new JSONObject(responseMap);
		try {
			responseJsonObject.put("success", "true");
			return responseJsonObject.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		
	}
	
	private String getFailResponseJson() {
		JSONObject responseJsonObject = new JSONObject();
		try {
			responseJsonObject.put("success", "false");
			return responseJsonObject.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
	}

	
	//REST
	@GET
	@Produces("application/json")
	public String listUsers() {
		try {
			Properties[] properties = getProperties();
			Properties usersProperties = properties[0];
			Set<Object> users = usersProperties.keySet();
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put("users", new JSONArray(users));
			
			return getSuccessResponseJson(responseMap);
			
		} catch (IOException e) {
			e.printStackTrace();
			return getFailResponseJson();
		}
	}
	
	@POST
	@Produces("application/json")
	public String addUser(String jsonString) {
		String user = "";
		String password = "";
		List<String> roles = new ArrayList<String>(); 
		JSONObject response = new JSONObject();
		
		try {
			JSONObject json = new JSONObject(jsonString);
			user = (String)json.get("user");
			password = (String)json.get("password");
			JSONArray rolesJsonArray = (JSONArray)json.getJSONArray("role");
			
			for (int i = 0; i < rolesJsonArray.length(); i++) {
				roles.add(rolesJsonArray.getString(i));
			}
			
		} catch (JSONException e1) {
			e1.printStackTrace();
			return getFailResponseJson();
		}
		
		try {
			Properties[] properties = getProperties();
			Properties usersProperties = properties[0];
			Properties rolesProperties = properties[1];
			
			usersProperties.put(user, password);
			String role = "admin";
			for (String string : roles) {
				role = role + "," + string;
			}
			rolesProperties.put(user, role);
			
			setProperties(usersProperties, rolesProperties);
			
			return getSuccessResponseJson(new HashMap<String, Object>());
			
		} catch (IOException e){
			e.printStackTrace();
			return getFailResponseJson();
		}
	}
	
	@DELETE
	@Path("/{user}")
	@Produces("application/json")
	public String deleteUser(@PathParam("user") String user){
		try {
			Properties[] properties = getProperties();
			Properties usersProperties = properties[0];
			Properties rolesProperties = properties[1];
			
			usersProperties.remove(user);
			rolesProperties.remove(user);
			
			setProperties(usersProperties, rolesProperties);
			return getSuccessResponseJson(new HashMap<String, Object>());
			
		} catch (IOException e){
			e.printStackTrace();
			return getFailResponseJson();
		}
	}

}
