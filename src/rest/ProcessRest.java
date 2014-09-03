package rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.*;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import utility.RemoteRest;

@Path("/process")
public class ProcessRest extends Rest{

	public ProcessRest() {
		
	}
	
	//REST
	@POST
	public String createLeave(String jsonString){
		String reason;
		Integer days;

		try {
			JSONObject json = new JSONObject(jsonString);
			reason = json.getString("reason");
			days = json.getInt("days");
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("map_reason", reason);
			map.put("map_days", days.toString());
			
			Integer processInstanceId = remoteRest.createProcessInstance("com.newegg.henry:henry_proj:1.0", "henry_project.leave_request", map);
			Map<String, Object> responseMap = new HashMap<String, Object>();
			map.put("processInstanceId", processInstanceId.toString());
			
			return getSuccessResponseJson(responseMap);
			
		} catch (JSONException e) {
			e.printStackTrace();
			return getFailResponseJson();
			
		} catch (IOException e) {
			logger.info("http return BufferReader has some problem......");
			e.printStackTrace();
			return getFailResponseJson();	
		}				
	}
	
//	@GET
//	public String getNonApproveLeave(String jsonString){
//		
//		
//	}
//	
//	@PUT
//	public String approveLeave(String jsonString){
//		Boolean isApproved;
//		
//		
//		JSONObject json = new JSONObject(jsonString);
//		json.getBoolean("isApproved");
//		
//	}

}
