package sampleRest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.script.ScriptException;
import javax.ws.rs.*;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import core.JbpmRestEntity;
import core.ProcessInstance;
import core.Task;
import core.TaskLifeCycle;
import core.Variables;




@Path("/process")
public class ProcessRest extends Rest{
	
	JbpmRestEntity jbpmRestEntity = new JbpmRestEntity();
	ProcessInstance processInstance = new ProcessInstance(jbpmRestEntity);
	Task task = new Task(jbpmRestEntity);
	TaskLifeCycle taskLifeCycle = new TaskLifeCycle(jbpmRestEntity);
	Variables variables = new Variables(jbpmRestEntity);
	

	public ProcessRest() {
		
	}
	
	//REST
	@POST
	public String createLeave(String jsonString) throws ScriptException{
		String reason;
		Integer days;

		try {
			JSONObject json = new JSONObject(jsonString);
			reason = json.getString("reason");
			days = json.getInt("days");
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("map_reason", reason);
			map.put("map_days", days.toString());
			
			String processInstanceId = taskLifeCycle.createProcessInstance("com.newegg.henry:henry_proj:1.0", "henry_project.leave_request", map);
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
	
	@GET
	public String getTaskOfLeaveList(String jsonString){
		Task remoteRest = new Task(jbpmRestEntity);
		String taskName;
		
		try {
			JSONObject json = new JSONObject(jsonString);
			taskName = json.getString("taskName");
			JSONObject responseJson = remoteRest.getAllTasksByTaskName("com.henry:henry_proj:1.0", "henry_project.leave_request",taskName);
			return responseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} 
		
	}
	
	@GET
	public String getLeave(String jsonString){
		Variables remoteRest = new Variables(jbpmRestEntity);
		String processInstanceId;
		
		try {
			JSONObject json = new JSONObject(jsonString);
			processInstanceId = json.getString("processInstanceId");
			JSONObject responseJson = remoteRest.getProcessInstanceVar(processInstanceId);
			return responseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} catch (IOException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} 
		
	}
	
	@GET
	public String getLeaveTask(String jsonString){
		Variables remoteRest = new Variables(jbpmRestEntity);
		String processInstanceId;
		
		try {
			JSONObject json = new JSONObject(jsonString);
			processInstanceId = json.getString("processInstanceId");
			JSONObject responseJson = remoteRest.getProcessInstanceVar(processInstanceId);
			return responseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} catch (IOException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} 
		
	}
	
	
	
	@PUT
	public String assignLeave(String jsonString){
		Task remoteRest = new Task(jbpmRestEntity);
		String taskId;
		
		try {
			JSONObject json = new JSONObject(jsonString);
			taskId = json.getString("taskId");
			JSONObject responseJson = remoteRest.getAllTasksByTaskName("com.henry:henry_project:1.0", "henry_project.leave_request",taskId);
			return responseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} 
		
	}
	
	@PUT
	public String approveLeave(String jsonString){
		TaskLifeCycle remoteRest = new TaskLifeCycle(jbpmRestEntity);
		String isApproved;
		String taskId;
		
		try {
			JSONObject json = new JSONObject(jsonString);
			isApproved = String.valueOf(json.getBoolean("isApproved"));
			taskId = json.getString("taskId");
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("map_isApproved", isApproved);
			remoteRest.completeTask(taskId, map);
			
			return getSuccessResponseJson(new HashMap<String, Object>());
		} catch (JSONException e) {
			e.printStackTrace();
			return getFailResponseJson();
		} catch (IOException e) {
			e.printStackTrace();
			return getFailResponseJson();
		}
		
	}

}
