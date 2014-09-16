package sampleRest;

import java.io.IOException;

import javax.script.ScriptException;

import org.json.JSONException;
import org.json.JSONObject;

import core.JbpmRestEntity;
import core.Task;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) throws IOException, JSONException, ScriptException {
		JbpmRestEntity jbpmRestEntity = new JbpmRestEntity();
		String deploymentId = "com.newegg.henry:henry_proj:1.0";
		String processDefId = "henry_proj.take_off_request";
		
		String taskName = "manager_approve";
		
		//1.user side, create process
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("map_reasons","i am sick");
//		map.put("map_day", 3);
//		map.put("map_isAppro", 1);
//		
//		String processInstanceId = remoteRest.createProcessInstance(deploymentId, processDefId, map);
		JSONObject json = remoteRest.getReadyTaskIdByProcessInstanceId("103");
		
		//remoteRest.startTask(json.getString("103"));
		
		//2.user side, get my process
		//JSONObject json = remoteRest.getTasksByProcessInstanceId("103");
		//JSONObject json = remoteRest.getProcessInstanceByCreator("john");
		
		//skip
		//remoteRest.skipTask(json.getString(processInstanceId));
		
		//abort
		//JSONObject json = remoteRest.abortProcess(deploymentId, "129");
		//-------------------------------------------------------
		
		//3.approver side, list all (id is taskId)
		//JSONObject json = remoteRest.getAllTasks(deploymentId, processDefId);
		
		//4.approver side, list task by my role(ex:Manager approve)
		//JSONObject json = remoteRest.getAllTasksByTaskName(deploymentId, processDefId, taskName);

		//5.approver side, list none approve task by my role
		//JSONObject json = remoteRest.getAllTasksByTaskName(deploymentId, processDefId, taskName, TaskStatus.Ready.toString());
		
		//6.approve side, list my task by me
		//JSONObject json = remoteRest.getAllTasksByApprover(deploymentId, processDefId, "henry");
		
		
		//7.approve side, list none approve my task by me
		//JSONObject json = remoteRest.getAllTasksByApprover(deploymentId, processDefId, taskName, TaskStatus.Completed);
		
		//8.approve
//		remoteRest.setUser("henry_hr");
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("map_isApprove_", 1);
//		JSONObject json = remoteRest.completeTask("129", map);
		
		//JSONObject json = remoteRest.getProcessInstanceState("94");
		//---------------------------------------------------
		//9.get global var
		//JSONObject json = remoteRest.getProcessInstanceVar(processInstanceId);
		
		//10.get local var
		//JSONObject json = remoteRest.getTaskVar("63");
		
		System.out.println(json);
		
		
	}


}
