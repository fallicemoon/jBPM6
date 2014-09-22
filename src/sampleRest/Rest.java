package sampleRest;

import java.util.Map;


import javax.ws.rs.Path;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;



@Path("/login")
public class Rest {
	protected Logger logger = Logger.getLogger(this.getClass());
	

	public Rest() {
		
	}
	
	protected String getSuccessResponseJson() {
		JSONObject responseJsonObject = new JSONObject();
		try {
			responseJsonObject.put("success", "true");
			return responseJsonObject.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		
	}
	
	protected String getSuccessResponseJson(Map<String, Object> responseMap) {
		JSONObject responseJsonObject = new JSONObject(responseMap);
		try {
			responseJsonObject.put("success", "true");
			return responseJsonObject.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
		
	}
	
	protected String getFailResponseJson(String msg) {
		JSONObject responseJsonObject = new JSONObject();
		try {
			responseJsonObject.put("success", "false");
			responseJsonObject.put("msg", msg);
			return responseJsonObject.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
	}


}
