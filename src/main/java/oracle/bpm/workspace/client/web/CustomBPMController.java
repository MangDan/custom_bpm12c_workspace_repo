package oracle.bpm.workspace.client.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;

import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpm.services.instancemanagement.model.IActivityInfo;
import oracle.bpm.services.instancemanagement.model.IFlowChangeItem;
import oracle.bpm.services.instancemanagement.model.IGrabInstanceContextRequest;
import oracle.bpm.services.instancemanagement.model.IGrabInstanceContextResponse;
import oracle.bpm.services.instancemanagement.model.IGrabInstanceRequest;
import oracle.bpm.services.instancemanagement.model.IGrabInstanceResponse;
import oracle.bpm.services.instancemanagement.model.IInstanceContextConfiguration;
import oracle.bpm.services.instancemanagement.model.IInstanceSummary;
import oracle.bpm.services.instancemanagement.model.IOpenActivityInfo;
import oracle.bpm.services.instancemanagement.model.IProcessInstance;
import oracle.bpm.services.instancemanagement.model.IVariableItem;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.ActivityInfo;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.FlowChangeItem;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.GrabInstanceContextRequest;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.GrabInstanceRequest;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.InstanceContextConfiguration;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.LocationInfo;
import oracle.bpm.services.instancemanagement.model.impl.alterflow.VariableItem;
import oracle.bpm.services.instancequery.IInstanceQueryInput;
import oracle.bpm.services.instancequery.IInstanceQueryService;
import oracle.bpm.workspace.client.config.SOAServiceClient;
import oracle.bpm.workspace.client.service.CustomBPMService;
import oracle.bpm.workspace.client.vo.GrabVO;
import oracle.bpm.workspace.client.vo.InstanceVO;

@Controller
public class CustomBPMController {
	@Resource(name = "soaClient")
	protected SOAServiceClient soaClient;
	
	@Resource(name="CustomBPMService")
	protected CustomBPMService customBPMService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); // Logger

	@RequestMapping("/bpm/process/instances")
	public String process_instances(HttpSession session, @RequestParam(required = false) Map<String, String> params,
			ModelMap model) throws Exception {
		
		HashMap<String, Object> accountData = (HashMap<String, Object>) session.getAttribute("accountData");
		IBPMContext bpmCtx = (IBPMContext) accountData.get("bpmContext");
		
		List<InstanceVO> instances = customBPMService.getQueryInstances(bpmCtx, params);
		
		model.addAttribute("result", instances);

		return "bpm/home/subs/processInstances";
	}
	
	/**
	 * 프로세스 현황
	 * @param session
	 * @param params
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bpm/process/wfInstances")
	public String wfInstances(HttpSession session, @RequestParam(required = false) Map<String, String> params,
			ModelMap model) throws Exception {
		
		HashMap<String, Object> accountData = (HashMap<String, Object>) session.getAttribute("accountData");
		IBPMContext bpmCtx = (IBPMContext) accountData.get("bpmContext");
		
		List<InstanceVO> instances = customBPMService.getQueryInstances(bpmCtx, params);
		
		model.addAttribute("result", instances);

		return "bpm/home/subs/taskInstances";
	}
	
	/**
	 * 프로세스 지연현황
	 * @param session
	 * @param params
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bpm/process/worstInstances")
	public String worstInstances(HttpSession session, @RequestParam(required = false) Map<String, String> params,
			ModelMap model) throws Exception {
		
		HashMap<String, Object> accountData = (HashMap<String, Object>) session.getAttribute("accountData");
		IBPMContext bpmCtx = (IBPMContext) accountData.get("bpmContext");
		
		List<InstanceVO> instances = customBPMService.getQueryInstances(bpmCtx, params);

		model.addAttribute("result", instances);

		return "bpm/home/subs/worstInstances";
	}
	
	@RequestMapping("/bpm/process/grab/info")
	public String grab_info(HttpSession session, @RequestParam(required = false) Map<String, String> params,
			ModelMap model) throws Exception {
		
		HashMap<String, Object> accountData = (HashMap<String, Object>) session.getAttribute("accountData");
		IBPMContext bpmCtx = (IBPMContext) accountData.get("bpmContext");

		String instanceId = (String) params.get("instanceId");
		IInstanceQueryService iqs = soaClient.getBPMServiceClient().getInstanceQueryService();
		// find the instance
		IProcessInstance instance = iqs.getProcessInstance(bpmCtx, instanceId);
		
		try {
			IGrabInstanceContextResponse grabContext = createGrabContext(bpmCtx, iqs, instance);
		
			if (instance == null) {
				logger.error("alterFlowForInstance() instance not found:" + instanceId);
				throw new Exception("alterFlowForInstance() instance not found:" + instanceId);
			}
	
			String processName = instance.getSca().getComponentName();
			String instanceState = instance.getSystemAttributes().getState();
	
			if (!(instanceState.equalsIgnoreCase(IInstanceQueryInput.PROCESS_STATE_OPEN)
					|| instanceState.equalsIgnoreCase(IInstanceQueryInput.PROCESS_STATE_SUSPENDED))) {
				logger.error("Instance is in state:" + instanceState + " state must be either OPEN or SUSPENDED");
				throw new Exception("Instance is in state:" + instanceState + " state must be either OPEN or SUSPENDED");
			}
	
			
			ArrayList<Object> openActivities = new ArrayList<Object>();
			// source : target = 1:n (id, name, path정보를 담아 반환한다.)
			for (IFlowChangeItem flowChange : grabContext.getAvailableFlowChanges()) { // Sources...
				HashMap<String, Object> activityMap = new HashMap<String, Object>();
				ArrayList<Object> targetActivities = new ArrayList<Object>();
	
				IOpenActivityInfo openActivity = flowChange.getSourceActivity();
				Boolean isSrcContainer = openActivity.isContainerActivity();
				String openActivityDisplayName = openActivity.getDisplayName();
				String openActivityId = openActivity.getId();
				String openActivityPath = openActivity.getStringPath();
				logger.debug("FlowChange Open Activity[isSrcContainer:" + isSrcContainer + "][display:"
						+ openActivityDisplayName + "][id:" + openActivityId + "][path:" + openActivityPath + "]");
	
				for (IActivityInfo targetActivity : flowChange.getValidGrabTargetActivities()) { // Targets....
	
					String targetName = targetActivity.getDisplayName();
					String targetId = targetActivity.getId();
	
					logger.debug("Checking Valid target [name:" + targetName + "][id:" + targetId + "]");
	
					targetActivities.add(targetActivity);
				}
	
				activityMap.put("openActivity", openActivity);
				activityMap.put("targetActivities", targetActivities);
	
				openActivities.add(activityMap);
			}
			
			// 변수 정보를 담아 반환한다.
			Iterable<IVariableItem> availableVariables = grabContext.getGrabInstanceContext().getAvailableVariables();
			for (IVariableItem availableVariable : availableVariables) {
				logger.debug("\t" + availableVariable.getName() + ":" + availableVariable.getValue());
			}
	
			model.addAttribute("openActivities", openActivities);
			model.addAttribute("availableVariables", availableVariables);
			model.addAttribute("processName", processName);
		} catch(Exception e) {
			logger.error("getGrabInfoException occurred while getting grab info! [please check this instance state(only possible running or suspend instance]");
			throw new Exception("GetGrabInfoException occurred while getting grab info! [please check this instance state(only possible running or suspend instance]");
		}
		
		return "bpm/home/subs/alterFlow";
	}

	@RequestMapping("/bpm/process/grab/action")
	public String grab_action(HttpSession session, @RequestBody GrabVO grabVO, ModelMap model) throws Exception {
		HashMap<String, Object> accountData = (HashMap<String, Object>) session.getAttribute("accountData");
		IBPMContext bpmCtx = (IBPMContext) accountData.get("bpmContext");
		
		String success = "false";
		String errorCode = "";
		String errorMessage = "";
		String jsonResult = "";
		
		Gson gson = new Gson();
		logger.debug("grabVO.json : " + gson.toJson(grabVO));
		
		String instanceId = grabVO.getInstanceId();
		IInstanceQueryService iqs = soaClient.getBPMServiceClient().getInstanceQueryService();
		// find the instance
		IProcessInstance instance = iqs.getProcessInstance(bpmCtx, instanceId);
		//IGrabInstanceContextResponse grabContext = createGrabContext(bpmCtx, iqs, instance);

		if (instance == null) {
			success = "false";
			errorCode = "";
			errorMessage = "alterFlowForInstance() instance not found:" + instanceId;
			
			logger.error("alterFlowForInstance() instance not found:" + instanceId);
			//throw new Exception("alterFlowForInstance() instance not found:" + instanceId);
		}

		String instanceState = instance.getSystemAttributes().getState();

		if (!(instanceState.equalsIgnoreCase(IInstanceQueryInput.PROCESS_STATE_OPEN)
				|| instanceState.equalsIgnoreCase(IInstanceQueryInput.PROCESS_STATE_SUSPENDED))) {
			
			success = "false";
			errorCode = "";
			errorMessage = "Instance is in state:" + instanceState + " state must be either OPEN or SUSPENDED";
			logger.error("Instance is in state:" + instanceState + " state must be either OPEN or SUSPENDED");
			
		}

		List<GrabVO.ActivityInfo> openActivities = grabVO.getOpenActivities();
		List<GrabVO.VariableInfo> changeVariables = grabVO.getChangeVariables();
		String comment = grabVO.getComment();
		boolean action = (grabVO.getMode().equals("resume") ? true : false);

		final Set<IFlowChangeItem> flowChangeItemSet = new HashSet<IFlowChangeItem>();
		final Set<IVariableItem> variableItemSet = new HashSet<IVariableItem>();

		for (GrabVO.ActivityInfo openActivity : openActivities) {
			flowChangeItemSet.add(FlowChangeItem.create(
					ActivityInfo.create(openActivity.getOpenActivityId(), openActivity.getOpenActivityName(),
							openActivity.getOpenActivityProcessId()),
					ActivityInfo.create(openActivity.getTargetActivityId(), openActivity.getTargetActivityName(),
							openActivity.getTargetActivityProcessId())));
		}

		for (GrabVO.VariableInfo changeVariable : changeVariables) {
			variableItemSet.add(VariableItem.create(changeVariable.getName(), changeVariable.getValue()));
		}

		IGrabInstanceRequest request = new GrabInstanceRequest();
		request.setProcessInstance(instance);
		request.setResumeInstanceIfRequired(action); // false일때는 저장만? true일때
														// Grab? 테스트 해봐야 함.
		request.setComments(comment); // set Comment..
		request.setValidateValue(true);
		request.setRequestedVariableValueChanges(variableItemSet);
		request.setRequestedFlowChanges(flowChangeItemSet);

		IGrabInstanceResponse grabResponse = soaClient.getBPMServiceClient().getInstanceManagementService()
				.grabInstance(bpmCtx, request);
		IInstanceSummary instanceSummary = grabResponse.getInstanceSummary();
		boolean isUpdated = instanceSummary.isSuccessfullyUpdated();
		String message = instanceSummary.getMessage();
		String exceptionMessage = instanceSummary.getExceptionMessage();
		logger.debug("Grab Response: [isUpdated:" + isUpdated + "][message:" + message + "][exception:" + exceptionMessage + "]");
		
		if(isUpdated)
			success = "true";
		
		if(exceptionMessage != null)
			errorMessage = exceptionMessage;
		
		model.addAttribute("success", success);
		model.addAttribute("errorCode", errorCode);
		model.addAttribute("errorMessage", errorMessage);
		model.addAttribute("result", jsonResult);
		
		return "common/json_response";
	}

	private static IGrabInstanceContextResponse createGrabContext(IBPMContext ctx, IInstanceQueryService iqs,
			IProcessInstance instance) throws Exception {
		InstanceContextConfiguration.Builder builder = new InstanceContextConfiguration.Builder();
		builder.includeOpenActivities().build();
		builder.includeProcessDataObjects().build();

		IInstanceContextConfiguration configuration = builder.build();

		// 두번째 파라미터 : 필요하면 Instance Suspend 후에 진행한다.
		IGrabInstanceContextRequest ctxReq = GrabInstanceContextRequest.create(instance, false,
				LocationInfo.ROOT_LOCATION, configuration);
		
		IGrabInstanceContextResponse ctxResp = iqs.createGrabInstanceContext(ctx, ctxReq);
		// IGrabInstanceContext context = ctxResp.getGrabInstanceContext();

		return ctxResp;
	}
	
	/**
	 * 프로세스BACK (admin)
	 * @param session
	 * @param params
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bpm/process/bpmadmin")
	public String bpmadmin(HttpSession session, @RequestParam(required = false) Map<String, String> params,
			ModelMap model) throws Exception {
		
		HashMap<String, Object> accountData = (HashMap<String, Object>) session.getAttribute("accountData");
		IBPMContext bpmCtx = (IBPMContext) accountData.get("bpmContext");
		
		List<InstanceVO> instances = customBPMService.getQueryInstances(bpmCtx, params);

		model.addAttribute("result", instances);

		return "bpm/home/subs/bpmadmin";
	}
}
