package sampleRest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.json.JSONException;
import org.json.JSONObject;

import core.JbpmRestEntity;
import core.ProcessInstance;
import core.Task;
import core.TaskLifeCycle;
import core.TaskStatus;
import core.Variables;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) throws IOException, JSONException, ScriptException {
		
		JbpmRestEntity jbpmRestEntity = new JbpmRestEntity("http://10.16.140.105:8080/jbpm-console/rest","henry","123");
		
		ProcessInstance processInstance = new ProcessInstance(jbpmRestEntity);
		Task task = new Task(jbpmRestEntity);
		TaskLifeCycle taskLifeCycle = new TaskLifeCycle(jbpmRestEntity);
		Variables variables = new Variables(jbpmRestEntity);
		
		String deploymentId = "com.newegg.henry:henry_proj:1.0";
		String processDefId = "henry_proj.leave_request";
		
		
		//1.user side, create process
		//jbpmRestEntity.setUser("henry");
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("map_reasons","i am sick");
//		map.put("map_day", 3);
//		map.put("map_isAppro", 1);
//		
//		String processInstanceId = processInstance.createProcessInstance(deploymentId, processDefId, map);
//		JSONObject json = task.getReadyTaskIdsByProcessInstanceId(processInstanceId);
//		System.out.println(json);
		
//		jbpmRestEntity.setUser("henry_manager");
//		boolean success = taskLifeCycle.startTask(json.getJSONArray(processInstanceId));
//		System.out.println(success);
		
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
//		jbpmRestEntity.setUser("henry_manager");
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("map_isApprove_", 1);
//		boolean success = taskLifeCycle.completeTask("201", map);
//		System.out.println(success);
		
		//JSONObject json = remoteRest.getProcessInstanceState("130");
		//---------------------------------------------------
		//9.get global var
//		JSONObject json = variables.getProcessInstanceVar("141");
//		System.out.println(json);
		
		//10.get local var
//		JSONObject json = variables.getTaskVar("com.newegg.fixedAssets:fixedAssets:1.0","294");
//		System.out.println(json);
		

//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("id", "a1");
//		String processInstanceId = processInstance.createProcessInstance("com.newegg.fixedAssets:fixedAssets:1.0", "fixedAssets.disposal", map);
//		JSONObject json = task.getReadyTaskIdsByProcessInstanceId(processInstanceId);
		
//		JSONObject tasks = task.getAllTasksByTaskName(deploymentId, processDefId, "manager_approve", TaskStatus.InProgress);
//		System.out.println(tasks);
		
		System.out.println(task.getReadyTaskIdsByProcessInstanceId("225"));
		System.out.println(task.getTask("380"));
		
	}


}
