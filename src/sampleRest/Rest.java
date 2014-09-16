package sampleRest;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import core.Task;




@Path("/login")
public class Rest {
	protected Logger logger = Logger.getLogger(this.getClass());
	

	public Rest() {
		
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
	
	protected String getFailResponseJson() {
		JSONObject responseJsonObject = new JSONObject();
		try {
			responseJsonObject.put("success", "false");
			return responseJsonObject.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"success\":\"false\"}";
		}
	}


}
