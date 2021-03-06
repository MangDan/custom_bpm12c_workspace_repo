package oracle.bpm.workspace.client.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.IWorkflowConstants;
import oracle.bpel.services.workflow.query.ITaskQueryService;
import oracle.bpel.services.workflow.query.model.TaskCountType;
import oracle.bpel.services.workflow.repos.Column;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.repos.table.WFTaskConstants;
import oracle.bpel.services.workflow.verification.IWorkflowContext;
import oracle.bpm.collections.Sequence;
import oracle.bpm.project.model.processes.Activity;
import oracle.bpm.project.model.processes.Event;
import oracle.bpm.project.model.processes.Subprocess;
import oracle.bpm.services.instancemanagement.model.IProcessInstance;
import oracle.bpm.services.instancequery.IAuditConstants;
import oracle.bpm.services.instancequery.IAuditInstance;
import oracle.bpm.services.internal.processmodel.model.IProcessModelPackage;
import oracle.bpm.services.processmetadata.ProcessMetadataSummary;
import oracle.bpm.services.processmetadata.StatusEnum;
import oracle.bpm.workspace.client.config.SOAServiceClient;
import oracle.bpm.workspace.client.dao.CustomBPMTrackerDAO;
import oracle.bpm.workspace.client.service.CustomBPMTrackerService;
import oracle.bpm.workspace.client.util.CommonUtility;
import oracle.bpm.workspace.client.util.OBPMUtility;
import oracle.bpm.workspace.client.util.WorkflowUtility;
import oracle.bpm.workspace.client.vo.CustomBPMTrackerDataVO;
import oracle.bpm.workspace.client.vo.CustomBPMTrackerModelVO;
import oracle.soa.management.facade.ComponentInstance;
import oracle.soa.management.util.ComponentInstanceFilter;

@Service("CustomBPMTrackerService")
public class CustomBPMTrackerServiceImpl implements CustomBPMTrackerService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());	// Logger
	
	@Autowired
	private SqlSession sqlSession;
	
	@Resource(name="soaClient")
    protected SOAServiceClient soaClient;
	
	public CustomBPMTrackerModelVO getCustomBPMTrackerModel(CustomBPMTrackerModelVO trackerModelVO) {
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
		
	    return customBPMTrackerDAO.getCustomBPMTrackerModel(trackerModelVO);
    }

	
	
	public String getProcessActivitiesXML(IBPMContext ctx, Map<String, String> params) throws Exception {
		String partition_id = (String) params.get("partition_id");
		String process_id = (String) params.get("process_id");
		String composite_id = (String) params.get("composite_id");
		String revision = (String) params.get("revision");
		
		ProcessMetadataSummary processMetadataSummary = null;
		//IBPMContext ctx = (IBPMContext) soaClient.getTaskQueryService().authenticate(null, null, null);
		
		List <ProcessMetadataSummary> summaries = soaClient.getBPMServiceClient().getProcessMetadataService().listProcessMetadataSummary(ctx, process_id, null, null);
		
		for (ProcessMetadataSummary processSummary:summaries) {
			logger.debug("processSummary.getCompositeName() : " + processSummary.getCompositeName());
			logger.debug("processSummary.getCompositeDN() : " + processSummary.getCompositeDN());	// lgd/LGD_Demo_LocalClosing!1.0*soa_756a14f3-4ef7-499f-9aea-ba297c5281e8
			logger.debug("processSummary.getProcessId() : " + processSummary.getProcessId());	
			logger.debug("processSummary.getProcessName() : " + processSummary.getProcessName());	// == componentid, 실제 프로세스명과는 다름.
			logger.debug("processSummary.getLabel() : " + processSummary.getLabel());
			logger.debug("processSummary.getProjectName() : " + processSummary.getProjectName());
			logger.debug("processSummary.getRevision() : " + processSummary.getRevision());
			
			// compositeDN 과 keyword 에 processid를 넘기면 단순해짐
			if(processSummary.getDomainName().equals(partition_id) && processSummary.getCompositeName().equals(composite_id) && processSummary.getProcessName().equals(process_id) && processSummary.getRevision().equals(revision) && processSummary.getStatus() == StatusEnum.ACTIVE) {
				
				logger.debug("------------------------------- OK?");
				processMetadataSummary = processSummary;
				break;
			}
		}
		
		Sequence <Activity> activities = soaClient.getBPMServiceClient().getProcessModelService().getProcessModel(ctx, processMetadataSummary.getCompositeDN(), processMetadataSummary.getProcessName()).getProcessModel().getActivities();
		Sequence <Event> events = soaClient.getBPMServiceClient().getProcessModelService().getProcessModel(ctx, processMetadataSummary.getCompositeDN(), processMetadataSummary.getProcessName()).getProcessModel().getEvents();
		
		Iterator ia =   activities.iterator();
		Iterator ie = events.iterator();
		
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<activity_mapping>\n");
		sb.append("<domain>"+partition_id+"</domain>\n");
		sb.append("<process_id>"+processMetadataSummary.getProcessName()+"</process_id>\n");
		sb.append("<revision_tag>"+processMetadataSummary.getRevision()+"</revision_tag>\n");
    	sb.append("<business_key></business_key>\n");
    	sb.append("<webModelRepository></webModelRepository>\n");
    	sb.append("<activities>\n");
    	while(ia.hasNext()) {
			Activity activity = (Activity) ia.next();
        	sb.append("    <activity>\n");
        	sb.append("        <activity_id>"+activity.getId()+"</activity_id>\n");	// changed by kdh (node_id -> activity_id)
        	sb.append("        <activity_name>"+activity.getDefaultLabel()+"</activity_name>\n");
        	sb.append("        <type>ACTIVITY</type>\n");
        	sb.append("    </activity>\n");
        	
    	}
    	while(ie.hasNext()) {
    		Event event = (Event) ie.next();
    		if(event.getBpmnType().getString().equals("Start") || event.getBpmnType().getString().equals("End")) {
	        	sb.append("    <activity>\n");
	        	sb.append("        <activity_id>"+event.getId()+"</activity_id>\n");	// changed by kdh (node_id -> activity_id)
	        	sb.append("        <activity_name>"+event.getDefaultLabel()+"</activity_name>\n");
	        	sb.append("        <type>"+event.getBpmnType().getString().toUpperCase()+"</type>\n");
	        	sb.append("    </activity>\n");
    		}
    	}
    	sb.append("</activities>\n");
    	sb.append("</activity_mapping>\n");
    	
    	return sb.toString();
	}
	
	public String getDummyProcessActivitiesXML(Map<String, String> params) throws Exception {
		String partition_id = (String) params.get("partition_id");
		String process_id = (String) params.get("process_id");
		String composite_id = (String) params.get("composite_id");
		String revision = (String) params.get("revision");
		
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<activity_mapping>\n");
		sb.append("<domain>"+partition_id+"</domain>\n");
		sb.append("<process_id>"+process_id+"</process_id>\n");
		sb.append("<revision_tag>"+revision+"</revision_tag>\n");
    	sb.append("<business_key></business_key>\n");
    	sb.append("<webModelRepository></webModelRepository>\n");
    	sb.append("<activities>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT000</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT000</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT001</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT001</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT002</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT002</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT003</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT003</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT004</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT004</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT005</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT005</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT006</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT006</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT007</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT007</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT008</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT008</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("    <activity>\n");
    	sb.append("        <activity_id>ACT009</activity_id>\n");	// changed by kdh (node_id -> activity_id)
    	sb.append("        <activity_name>ACT009</activity_name>\n");
    	sb.append("    </activity>\n");
    	sb.append("</activities>\n");
    	sb.append("</activity_mapping>\n");
    	
    	return sb.toString();
	}
	
	// API + DB
	public String getProcessAuditDataXML(IBPMContext ctx, Map<String, String> params) throws Exception {
		String instance_id = (String) params.get("instance_id");
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
		
		// 여기 데이터 가져오는 부분 확인하면 됨.......
	    List<CustomBPMTrackerDataVO> listCustomBPMTrackerData = customBPMTrackerDAO.getCustomBPMTrackerData(instance_id);
	    
		IProcessInstance processInstance = soaClient.getBPMServiceClient().getInstanceQueryService().getProcessInstance(ctx, instance_id);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<process> \n");
		sb.append("<cikey>"+OBPMUtility.defaultToNull(processInstance.getCubeInstanceId(), "")+"</cikey>\n");
		sb.append("<domain>"+OBPMUtility.defaultToNull(processInstance.getProcessDN().subSequence(0, processInstance.getProcessDN().indexOf("/")), "")+"</domain>\n");
		sb.append("<process_id>"+OBPMUtility.defaultToNull(processInstance.getSca().getComponentName(), "")+"</process_id>\n");
		sb.append("<process_name>"+OBPMUtility.defaultToNull(processInstance.getProcessName(), "")+"</process_name>\n");
		sb.append("<revision_tag>"+OBPMUtility.defaultToNull(processInstance.getSca().getCompositeVersion(), "")+"</revision_tag>\n");
		sb.append("<business_key></business_key>\n");
		sb.append("<creation_date>"+OBPMUtility.defaultToNull(CommonUtility.getDateString(processInstance.getSystemAttributes().getCreatedDate(), Locale.KOREA, TimeZone.getDefault()), "")+"</creation_date>\n");
		sb.append("<modify_date>"+OBPMUtility.defaultToNull(CommonUtility.getDateString(processInstance.getSystemAttributes().getUpdatedDate(), Locale.KOREA, TimeZone.getDefault()), "")+"</modify_date>\n");
		sb.append("<state>"+OBPMUtility.defaultToNull(OBPMUtility.getinstanceStateCodeForPrism(processInstance.getSystemAttributes().getState()), "")+"</state>\n");
		sb.append("<state_txt>"+OBPMUtility.getInstanceStateTxt(OBPMUtility.getinstanceStateCodeForPrism(processInstance.getSystemAttributes().getState()))+"</state_txt>\n");
		sb.append("<title>"+OBPMUtility.defaultToNull(processInstance.getTitle(), "")+"</title>\n");
		sb.append("<conversation_id></conversation_id>\n");
		sb.append("<root_id></root_id>\n");
		sb.append("<parent_id></parent_id>\n");
		sb.append("<metadata></metadata>\n");
		sb.append("<index1></index1>\n");
		sb.append("<index2></index2>\n");
		sb.append("<index3></index3>\n");
		sb.append("<index4></index4>\n");
		sb.append("<index5></index5>\n");
		sb.append("<index6></index6>\n");
		sb.append("<webModelRepository></webModelRepository>\n");
		sb.append("<activities>\n");
		
		Calendar cal = Calendar.getInstance();
		IAuditInstance auditInstance = null;
		for (CustomBPMTrackerDataVO trackerData : listCustomBPMTrackerData) {  
			
			List <IAuditInstance>auditInstancesByInstanceId = soaClient.getBPMServiceClient().getInstanceQueryService().queryAuditInstanceByProcessId(ctx, instance_id);
			List <IAuditInstance>auditInstances = soaClient.getBPMServiceClient().getInstanceQueryService().queryAuditInstanceByActivityId(ctx, auditInstancesByInstanceId, trackerData.getActivity_id());
			
			for(IAuditInstance audit : auditInstances) {
				if(audit.getAuditInstanceType().compareTo("START") == 0)
					auditInstance = audit;
			}
			
			//ss_eval_point : activation, fault, completion
			sb.append("    <activity activity_id=\""+OBPMUtility.defaultToNull(trackerData.getActivity_id(), "")+"\" activity_name=\""+OBPMUtility.defaultToNull(trackerData.getActivity_name(), "")+"\">\n");
			sb.append("        <wi_data>\n");
			sb.append("		       <wi_creation_date>"+CommonUtility.getDateString(trackerData.getCreation_date(), Locale.KOREA, TimeZone.getDefault())+"</wi_creation_date>\n");
			sb.append("		       <wi_completion_date>"+CommonUtility.getDateString(trackerData.getCompletion_date(), Locale.KOREA, TimeZone.getDefault())+"</wi_completion_date>\n");
			sb.append("		       <wi_state>"+(trackerData.getStatus() == null ? "activation" : trackerData.getStatus())+"</wi_state>\n");
			sb.append("        </wi_data>\n");
			sb.append("        <sensor_data>\n");
			sb.append("		       <sensor_name></sensor_name>\n");
			sb.append("		       <sensor_target></sensor_target>\n");
			sb.append("		       <eval_point>"+(trackerData.getStatus() == null ? "activation" : trackerData.getStatus())+"</eval_point>\n");
			sb.append("        </sensor_data>\n");
			sb.append("        <custom_data>\n");
			sb.append("		       <activity_type>"+trackerData.getActivity_type()+"</activity_type>\n");
			sb.append("		       <activity_id>"+OBPMUtility.defaultToNull(trackerData.getActivity_id(), "")+"</activity_id>\n");
			sb.append("		       <activity_name>"+OBPMUtility.defaultToNull(trackerData.getActivity_name(), "")+"</activity_name>\n");
			sb.append("		       <group_processing_type></group_processing_type>\n");	//현재 미 구현 상태
			sb.append("		       <creation_date>"+CommonUtility.getDateString(trackerData.getCreation_date(), Locale.KOREA, TimeZone.getDefault())+"</creation_date>\n");
			sb.append("		       <completion_date>"+CommonUtility.getDateString(trackerData.getCompletion_date(), Locale.KOREA, TimeZone.getDefault())+"</completion_date>\n");
			//sb.append("		       <task_id>"+(ai_start.getUserTaskNumber() == null ? "" : getTaskIdByNumber(ctx, ai_start.getUserTaskNumber().intValue()))+"</task_id>\n");
			sb.append("		       <task_id>"+(auditInstance.getUserTaskNumber() == null ? "" : auditInstance.getUserTaskNumber().intValue())+"</task_id>\n"); //. API 사용해서 구현하면 도미.
			sb.append("		       <participants_id>"+(auditInstance.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssignees(ctx, auditInstance.getUserTaskNumber().intValue()).get("id"))+"</participants_id>\n");
			//sb.append("		       <participants>"+(ai_start.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssignees(ctx, ai_start.getUserTaskNumber().intValue()).get("name"))+"</participants>\n");
			sb.append("		       <participants>"+(auditInstance.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssignees(ctx, auditInstance.getUserTaskNumber().intValue()).get("name"))+"</participants>\n");
			sb.append("		       <participants_group_id></participants_group_id>\n");	// 미규현 상태
			sb.append("		       <participants_group></participants_group>\n");	// 미규현 상태
			sb.append("		       <is_fault>"+trackerData.getIs_fault()+"</is_fault>\n");
			sb.append("		       <is_overdue>"+trackerData.getIs_overdue()+"</is_overdue>\n");
			sb.append("		       <is_skip>"+trackerData.getIs_skip()+"</is_skip>\n");
			sb.append("		       <status>"+(trackerData.getStatus().equals("activation") ? "진행중" : "완료")+"</status>\n");
			sb.append("		       <outcome></outcome>\n");
			sb.append("		       <lead_time></lead_time>\n");
			
			long temp_startDate = Long.valueOf(trackerData.getCreation_date().getTime());
			long temp_endDate = 0;
			
			if(trackerData.getCompletion_date() == null) {
				temp_endDate = Long.valueOf(Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA).getTime().getTime());
			} else {
				temp_endDate = Long.valueOf(trackerData.getCompletion_date().getTime());
			}
			
			long millisLeadTime = temp_endDate - temp_startDate;
			long leadTime = millisLeadTime / 3600;
			logger.debug("temp_startDate : " + temp_startDate);
			logger.debug("temp_endDate : " + temp_endDate);
			logger.debug("millisLeadTime : " + millisLeadTime);
			logger.debug("leadTime : " + leadTime);
			logger.debug("auditInstance.getParticipant() : " + auditInstance.getParticipant());
			logger.debug("auditInstance.getUserTaskNumber() : " + auditInstance.getUserTaskNumber());
			
			sb.append("		       <activity_lead_time>"+(leadTime == 0 ? "" : +leadTime+" (초)")+"</activity_lead_time>\n");
			//신한은행 PoC 로 잠시 주석..
			sb.append("		       <retry_count>"+(auditInstance.getLoopCount() == 0 ? "" : auditInstance.getLoopCount() + " 건")+"</retry_count>\n");	
			sb.append("		       <is_participation>N</is_participation>\n");
			sb.append("        </custom_data>\n");
			sb.append("		</activity>\n");
			
		}
		sb.append("</activities>\n");
		sb.append("</process> \n");
		
		return sb.toString();
	}
	
	// Only Using API
	public String getProcessAuditDataXML2(IBPMContext ctx, Map<String, String> params) throws Exception {
		String instance_id = (String) params.get("instance_id");
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
		
		// 여기 데이터 가져오는 부분 확인하면 됨.......
	    List<CustomBPMTrackerDataVO> listCustomBPMTrackerData = customBPMTrackerDAO.getCustomBPMTrackerData(instance_id);
	    
		IProcessInstance processInstance = soaClient.getBPMServiceClient().getInstanceQueryService().getProcessInstance(ctx, instance_id);
		
		boolean isProcessOverdue = false;
		long curTimeMillis = 0;
		if(!processInstance.getSystemAttributes().getProcessDueDate().equals("") &&  processInstance.getSystemAttributes().getProcessDueDate() != null) {
			if(processInstance.getSystemAttributes().getState().equals("OPEN")) {
				curTimeMillis = Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA).getTimeInMillis();
			} else {
				curTimeMillis = processInstance.getSystemAttributes().getUpdatedDate().getTimeInMillis();
			}
			
			long processOverdueMillis = processInstance.getSystemAttributes().getProcessDueDate().getTimeInMillis();
			
			if(processOverdueMillis < curTimeMillis)
				isProcessOverdue = true;
		}
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<process> \n");
		sb.append("<cikey>"+OBPMUtility.defaultToNull(processInstance.getCubeInstanceId(), "")+"</cikey>\n");
		sb.append("<domain>"+OBPMUtility.defaultToNull(processInstance.getProcessDN().subSequence(0, processInstance.getProcessDN().indexOf("/")), "")+"</domain>\n");
		sb.append("<process_id>"+OBPMUtility.defaultToNull(processInstance.getSca().getComponentName(), "")+"</process_id>\n");
		sb.append("<process_name>"+OBPMUtility.defaultToNull(processInstance.getProcessName(), "")+"</process_name>\n");
		sb.append("<revision_tag>"+OBPMUtility.defaultToNull(processInstance.getSca().getCompositeVersion(), "")+"</revision_tag>\n");
		sb.append("<business_key></business_key>\n");
		sb.append("<creation_date>"+OBPMUtility.defaultToNull(CommonUtility.getDateString(processInstance.getSystemAttributes().getCreatedDate(), Locale.KOREA, TimeZone.getDefault()), "")+"</creation_date>\n");
		sb.append("<modify_date>"+OBPMUtility.defaultToNull(CommonUtility.getDateString(processInstance.getSystemAttributes().getUpdatedDate(), Locale.KOREA, TimeZone.getDefault()), "")+"</modify_date>\n");
		sb.append("<overdue_date>"+OBPMUtility.defaultToNull(CommonUtility.getDateString(processInstance.getSystemAttributes().getProcessDueDate(), Locale.KOREA, TimeZone.getDefault()), "")+"</overdue_date>\n");
		//sb.append("<state>"+OBPMUtility.defaultToNull(OBPMUtility.getinstanceStateCodeForPrism(processInstance.getSystemAttributes().getState()), "")+"</state>\n");
		sb.append("<state>"+(isProcessOverdue == true ? 6 : OBPMUtility.defaultToNull(OBPMUtility.getinstanceStateCodeForPrism(processInstance.getSystemAttributes().getState()), ""))+"</state>\n");
		sb.append("<state_txt>"+OBPMUtility.getInstanceStateTxt(OBPMUtility.getinstanceStateCodeForPrism(processInstance.getSystemAttributes().getState()))+(isProcessOverdue == true ? "(지연됨)" : "")+"</state_txt>\n");
		sb.append("<title>"+OBPMUtility.defaultToNull(processInstance.getTitle(), "")+"</title>\n");
		sb.append("<conversation_id></conversation_id>\n");
		sb.append("<root_id></root_id>\n");
		sb.append("<parent_id></parent_id>\n");
		sb.append("<metadata></metadata>\n");
		sb.append("<index1></index1>\n");
		sb.append("<index2></index2>\n");
		sb.append("<index3></index3>\n");
		sb.append("<index4></index4>\n");
		sb.append("<index5></index5>\n");
		sb.append("<index6></index6>\n");
		sb.append("<webModelRepository></webModelRepository>\n");
		
		// Activity Audit View List
		List <IAuditInstance>auditInstances = soaClient.getBPMServiceClient().getInstanceQueryService().queryAuditInstanceByProcessId(ctx, instance_id); 
		
		
		HashMap<String, IAuditInstance> auditInstanceMap = new HashMap<String, IAuditInstance>();
		HashMap<String, Calendar> endDateMap = new HashMap<String, Calendar>();
		HashMap<String, Integer> activityLoopCnt = new HashMap<String, Integer>();
		
		for (IAuditInstance ai_end : auditInstances) {  
			if ((ai_end.getFlowElementType().compareTo("ACTIVITY") == 0 || ai_end.getFlowElementType().compareTo("EVENT") == 0) && ai_end.getAuditInstanceType().compareTo("END") == 0) {
				endDateMap.put(ai_end.getScopeId(), ai_end.getCreateTime());
			}
		}
		
		sb.append("<activities>\n");
		for (IAuditInstance ai_start : auditInstances) {  
			if ((ai_start.getFlowElementType().compareTo("ACTIVITY") == 0 || ai_start.getFlowElementType().compareTo("EVENT") == 0) && ai_start.getAuditInstanceType().compareTo("START") == 0) {
				
				activityLoopCnt.put(ai_start.getActivityId(), (activityLoopCnt.get(ai_start.getActivityId()) == null ? 0 : activityLoopCnt.get(ai_start.getActivityId()) + 1));
				
				
				//long temp_startDate = Long.valueOf(ai_start.getCreateTime().getTime().getTime())/1000/60;
				//long temp_endDate = 0;
				
				/*if(endDateMap.get(ai_start.getActivityId()) == null) {
					temp_endDate = Long.valueOf(Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA).getTime().getTime())/1000/60;
				} else {
					temp_endDate = Long.valueOf(endDateMap.get(ai_start.getActivityId()).getTime().getTime())/1000/60;
				}*/
				
				System.out.println("ai_start.getDueDate() : " + ai_start.getDueDate());
				long temp_startDate = Long.valueOf(ai_start.getCreateTime().getTimeInMillis())/1000;	//초
				long temp_endDate = 0;
				long temp_dueDate = 0;
				
				if(endDateMap.get(ai_start.getScopeId()) == null) {
					temp_endDate = Long.valueOf(Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA).getTimeInMillis())/1000;	//초
				} else {
					temp_endDate = Long.valueOf(endDateMap.get(ai_start.getScopeId()).getTimeInMillis())/1000;	//초
				}
				
				if(ai_start.getDueDate() == null || ai_start.getDueDate().equals("")) {
					temp_dueDate = 0;
				} else {
					temp_dueDate = Long.valueOf(ai_start.getDueDate().getTimeInMillis())/1000; //초
				}
				
				long secLeadTime = temp_endDate - temp_startDate;
				
				//ss_eval_point : activation, fault, completion
				sb.append("    <activity activity_id=\""+OBPMUtility.defaultToNull(ai_start.getActivityId(), "")+"\" activity_name=\""+OBPMUtility.defaultToNull(ai_start.getLabel(), "")+"\">\n");
				sb.append("        <wi_data>\n");
				sb.append("		       <wi_creation_date>"+CommonUtility.getDateString(ai_start.getCreateTime(), Locale.KOREA, TimeZone.getDefault())+"</wi_creation_date>\n");
				sb.append("		       <wi_completion_date>"+CommonUtility.getDateString(endDateMap.get(ai_start.getScopeId()), Locale.KOREA, TimeZone.getDefault())+"</wi_completion_date>\n");
				sb.append("		       <wi_state>"+(endDateMap.get(ai_start.getScopeId()) == null ? "activation" : "completion") +"</wi_state>\n");
				sb.append("        </wi_data>\n");
				sb.append("        <sensor_data>\n");
				sb.append("		       <sensor_name></sensor_name>\n");
				sb.append("		       <sensor_target></sensor_target>\n");
				sb.append("		       <eval_point>"+(endDateMap.get(ai_start.getScopeId()) == null ? "activation" : "completion")+"</eval_point>\n");
				sb.append("        </sensor_data>\n");
				sb.append("        <custom_data>\n");
				sb.append("		       <activity_type>"+ai_start.getFlowElementType()+"</activity_type>\n");
				sb.append("		       <activity_id>"+OBPMUtility.defaultToNull(ai_start.getActivityId(), "")+"</activity_id>\n");
				sb.append("		       <activity_name>"+OBPMUtility.defaultToNull(ai_start.getLabel(), "")+"</activity_name>\n");
				sb.append("		       <group_processing_type></group_processing_type>\n");	//현재 미 구현 상태
				sb.append("		       <creation_date>"+CommonUtility.getDateString(ai_start.getCreateTime(), Locale.KOREA, TimeZone.getDefault())+"</creation_date>\n");
				sb.append("		       <completion_date>"+CommonUtility.getDateString(endDateMap.get(ai_start.getScopeId()), Locale.KOREA, TimeZone.getDefault())+"</completion_date>\n");
				//sb.append("		       <task_id>"+(ai_start.getUserTaskNumber() == null ? "" : getTaskIdByNumber(ctx, ai_start.getUserTaskNumber().intValue()))+"</task_id>\n");
				sb.append("		       <task_id>"+(ai_start.getUserTaskNumber() == null ? "" : ai_start.getUserTaskNumber().intValue())+"</task_id>\n");
				//sb.append("		       <participants_id>"+(ai_start.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssignees(ctx, ai_start.getUserTaskNumber().intValue()).get("id"))+"</participants_id>\n");
				sb.append("		       <participants>"+(ai_start.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssigneesOnHistory(ctx, ai_start.getUserTaskNumber().intValue()).get("name"))+"</participants>\n");
				//sb.append("		       <participants>"+(ai_start.getParticipant() == null ? "" : ai_start.getParticipant())+"</participants>\n");
				sb.append("		       <participants_group_id></participants_group_id>\n");	// 미규현 상태
				sb.append("		       <participants_group></participants_group>\n");	// 미규현 상태
				sb.append("		       <is_fault>N</is_fault>\n");
				
				logger.debug("temp_dueDate : " + temp_dueDate);
				logger.debug("temp_endDate : " + temp_endDate);
				if(temp_dueDate != 0 && (temp_endDate > temp_dueDate))
					sb.append("		       <is_overdue>지연</is_overdue>\n");
				else
					sb.append("		       <is_overdue>정상</is_overdue>\n");
				
				if(ai_start.getParticipant() != null && ai_start.getParticipant().equals("")) {
					sb.append("		       <is_skip>건너뜀</is_skip>\n");
				} else {
					sb.append("		       <is_skip>수행</is_skip>\n");
				}
				
				sb.append("		       <status>"+(endDateMap.get(ai_start.getScopeId()) == null ? "진행중" : "완료") +"</status>\n");
				sb.append("		       <outcome>"+(ai_start.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskOutcome(ctx, ai_start.getUserTaskNumber().intValue()))+"</outcome>\n");
				sb.append("		       <lead_time></lead_time>\n");
				//"[h:" + (time / 3600) + ", m:" + (time % 3600 / 60) + ", s:" + (time % 3600 % 60) + "]"
				//sb.append("		       <activity_lead_time>"+String.format("%.1f", secLeadTime/60)+"(초)</activity_lead_time>\n");
				sb.append("		       <activity_lead_time>"+((secLeadTime/3600) == 0 ? "" : (secLeadTime/3600)+ "시간 ")+((secLeadTime % 3600 / 60) == 0 ? "" : (secLeadTime % 3600 / 60)+"분 ")+((secLeadTime % 3600 % 60) == 0 ? "" : (secLeadTime % 3600 % 60)+" 초")+"</activity_lead_time>\n");
				sb.append("		       <retry_count>"+(activityLoopCnt.get(ai_start.getActivityId()) == 0 ? "" : activityLoopCnt.get(ai_start.getActivityId()))+"</retry_count>\n");
				//sb.append("		       <retry_count></retry_count>\n");	//신한은행 PoC 때문에 
				sb.append("		       <is_participation>N</is_participation>\n");
				sb.append("        </custom_data>\n");
				sb.append("		</activity>\n");
			}
		}
		sb.append("</activities>\n");
		sb.append("</process> \n");
		
		return sb.toString();
	}
	
	// Only DB
	public String getProcessAuditDataXML3(Map<String, String> params) throws Exception {
		String instance_id = (String) params.get("instance_id");
		String composite_id = (String) params.get("composite_id");
		String version = (String) params.get("version");
		String process_id = (String) params.get("process_id");
		
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
		
		// 여기 데이터 가져오는 부분 확인하면 됨.......
	    List<CustomBPMTrackerDataVO> listCustomBPMTrackerData = customBPMTrackerDAO.getCustomBPMTrackerData(instance_id);
	    
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<process> \n");
		sb.append("<cikey>"+OBPMUtility.defaultToNull(instance_id, "")+"</cikey>\n");
		sb.append("<domain>"+OBPMUtility.defaultToNull(composite_id, "")+"</domain>\n");
		sb.append("<process_id>"+OBPMUtility.defaultToNull(process_id, "")+"</process_id>\n");
		sb.append("<process_name>"+OBPMUtility.defaultToNull(process_id, "")+"</process_name>\n");
		sb.append("<revision_tag>"+OBPMUtility.defaultToNull(version, "")+"</revision_tag>\n");
		sb.append("<business_key></business_key>\n");
		sb.append("<creation_date></creation_date>\n");
		sb.append("<modify_date></modify_date>\n");
		sb.append("<state>1</state>\n");
		sb.append("<state_txt></state_txt>\n");
		sb.append("<title></title>\n");
		sb.append("<conversation_id></conversation_id>\n");
		sb.append("<root_id></root_id>\n");
		sb.append("<parent_id></parent_id>\n");
		sb.append("<metadata></metadata>\n");
		sb.append("<index1></index1>\n");
		sb.append("<index2></index2>\n");
		sb.append("<index3></index3>\n");
		sb.append("<index4></index4>\n");
		sb.append("<index5></index5>\n");
		sb.append("<index6></index6>\n");
		sb.append("<webModelRepository></webModelRepository>\n");
		sb.append("<activities>\n");
		
		Calendar cal = Calendar.getInstance();
		for (CustomBPMTrackerDataVO trackerData : listCustomBPMTrackerData) {  
			
			//ss_eval_point : activation, fault, completion
			sb.append("    <activity activity_id=\""+OBPMUtility.defaultToNull(trackerData.getActivity_id(), "")+"\" activity_name=\""+OBPMUtility.defaultToNull(trackerData.getActivity_name(), "")+"\">\n");
			sb.append("        <wi_data>\n");
			sb.append("		       <wi_creation_date>"+CommonUtility.getDateString(trackerData.getCreation_date(), Locale.KOREA, TimeZone.getDefault())+"</wi_creation_date>\n");
			sb.append("		       <wi_completion_date>"+CommonUtility.getDateString(trackerData.getCompletion_date(), Locale.KOREA, TimeZone.getDefault())+"</wi_completion_date>\n");
			sb.append("		       <wi_state>"+(trackerData.getStatus() == null ? "activation" : trackerData.getStatus())+"</wi_state>\n");
			sb.append("        </wi_data>\n");
			sb.append("        <sensor_data>\n");
			sb.append("		       <sensor_name></sensor_name>\n");
			sb.append("		       <sensor_target></sensor_target>\n");
			sb.append("		       <eval_point>"+(trackerData.getStatus() == null ? "activation" : trackerData.getStatus())+"</eval_point>\n");
			sb.append("        </sensor_data>\n");
			sb.append("        <custom_data>\n");
			sb.append("		       <activity_type>"+trackerData.getActivity_type()+"</activity_type>\n");
			sb.append("		       <activity_id>"+OBPMUtility.defaultToNull(trackerData.getActivity_id(), "")+"</activity_id>\n");
			sb.append("		       <activity_name>"+OBPMUtility.defaultToNull(trackerData.getActivity_name(), "")+"</activity_name>\n");
			sb.append("		       <group_processing_type></group_processing_type>\n");	//현재 미 구현 상태
			sb.append("		       <creation_date>"+CommonUtility.getDateString(trackerData.getCreation_date(), Locale.KOREA, TimeZone.getDefault())+"</creation_date>\n");
			sb.append("		       <completion_date>"+CommonUtility.getDateString(trackerData.getCompletion_date(), Locale.KOREA, TimeZone.getDefault())+"</completion_date>\n");
			//sb.append("		       <task_id>"+(ai_start.getUserTaskNumber() == null ? "" : getTaskIdByNumber(ctx, ai_start.getUserTaskNumber().intValue()))+"</task_id>\n");
			sb.append("		       <task_id></task_id>\n"); //. API 사용해서 구현하면 도미.
			sb.append("		       <participants_id></participants_id>\n");
			//sb.append("		       <participants>"+(ai_start.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssignees(ctx, ai_start.getUserTaskNumber().intValue()).get("name"))+"</participants>\n");
			sb.append("		       <participants></participants>\n");
			sb.append("		       <participants_group_id></participants_group_id>\n");	// 미규현 상태
			sb.append("		       <participants_group></participants_group>\n");	// 미규현 상태
			sb.append("		       <is_fault>"+trackerData.getIs_fault()+"</is_fault>\n");
			sb.append("		       <is_overdue>"+trackerData.getIs_overdue()+"</is_overdue>\n");
			sb.append("		       <is_skip>"+trackerData.getIs_skip()+"</is_skip>\n");
			sb.append("		       <status>"+(trackerData.getStatus().equals("activation") ? "진행중" : "완료")+"</status>\n");
			sb.append("		       <outcome></outcome>\n");
			sb.append("		       <lead_time></lead_time>\n");
			
			long temp_startDate = Long.valueOf(trackerData.getCreation_date().getTime());
			long temp_endDate = 0;
			
			if(trackerData.getCompletion_date() == null) {
				temp_endDate = Long.valueOf(Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA).getTime().getTime());
			} else {
				temp_endDate = Long.valueOf(trackerData.getCompletion_date().getTime());
			}
			
			long millisLeadTime = temp_endDate - temp_startDate;
			long leadTime = millisLeadTime / 3600;
			logger.debug("temp_startDate : " + temp_startDate);
			logger.debug("temp_endDate : " + temp_endDate);
			logger.debug("millisLeadTime : " + millisLeadTime);
			logger.debug("leadTime : " + leadTime);
			
			sb.append("		       <activity_lead_time>"+(leadTime == 0 ? "" : +leadTime+" (초)")+"</activity_lead_time>\n");
			//신한은행 PoC 로 잠시 주석..
			sb.append("		       <retry_count></retry_count>\n");	
			sb.append("		       <is_participation>N</is_participation>\n");
			sb.append("        </custom_data>\n");
			sb.append("		</activity>\n");
			
		}
		sb.append("</activities>\n");
		sb.append("</process> \n");
		
		return sb.toString();
	}
	
	public String getProcessAggregatedTaskDataXML(IWorkflowContext wfctx, Map<String, String> params) throws Exception {
		String assignmentfilter = (String) params.get("assignmentfilter");	//MY_AND_GROUP OR ALL
		
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<process> \n");
		sb.append("<cikey></cikey>\n");
		sb.append("<domain></domain>\n");
		sb.append("<process_id></process_id>\n");
		sb.append("<process_name></process_name>\n");
		sb.append("<revision_tag></revision_tag>\n");
		sb.append("<business_key></business_key>\n");
		sb.append("<creation_date></creation_date>\n");
		sb.append("<modify_date></modify_date>\n");
		sb.append("<state></state>\n");
		sb.append("<state_txt></state_txt>\n");
		sb.append("<title></title>\n");
		sb.append("<conversation_id></conversation_id>\n");
		sb.append("<root_id></root_id>\n");
		sb.append("<parent_id></parent_id>\n");
		sb.append("<metadata></metadata>\n");
		sb.append("<index1></index1>\n");
		sb.append("<index2></index2>\n");
		sb.append("<index3></index3>\n");
		sb.append("<index4></index4>\n");
		sb.append("<index5></index5>\n");
		sb.append("<index6></index6>\n");
		sb.append("<webModelRepository></webModelRepository>\n");
		sb.append("<activities>\n");
		
		ITaskQueryService querySvc = soaClient.getWorkflowServiceClient().getTaskQueryService();
        //조회할 컬럼을 정의한다.
        List<String> inboxColumns = new ArrayList<String>();
        
        inboxColumns.add(TableConstants.WFTASK_APPLICATIONNAME_COLUMN.getName());
        inboxColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());    // Task Number
        inboxColumns.add(TableConstants.WFTASK_ACTIVITYID_COLUMN.getName());          // 액티비티
        
        ITaskQueryService.AssignmentFilter assignmentFilter = null;
        if(assignmentfilter.equals("MY_AND_GROUP"))
            assignmentFilter = ITaskQueryService.AssignmentFilter.MY_AND_GROUP;
        else
            assignmentFilter = ITaskQueryService.AssignmentFilter.ALL;
		
        Predicate predicate = null;
        predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, IWorkflowConstants.TASK_STATE_ASSIGNED);
        
        List<TaskCountType> task_aggt_assigned_count = (List<TaskCountType>)querySvc.queryAggregatedTasks(wfctx, Column.getColumn(WFTaskConstants.ACTIVITYID_COLUMN), assignmentFilter, null, predicate, false, false);
        
        predicate = null;
        //predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, IWorkflowConstants.TASK_STATE_COMPLETED);
        List<TaskCountType> task_aggt_all_count = (List<TaskCountType>)querySvc.queryAggregatedTasks(wfctx, Column.getColumn(WFTaskConstants.ACTIVITYID_COLUMN), assignmentFilter, null, predicate, false, false);
        
        for(TaskCountType taskCntType : task_aggt_all_count) {
        	int count = 0;
        	
        	// for 신한은행 PoC
        	// 전체 중 assigned 된 것만 count 해서 보여줌... 단 카운트 있는 애들은 다 데이터 생성 -> 그래야만 트래커에서 액티비티가 클릭됨.
        	// 여기는 진행중인 애들을 미리 map에 담아서 가져오는 식으로 변경
        	for(TaskCountType taskCntAssignType : task_aggt_assigned_count) {
        		if(taskCntType.getValue().equals(taskCntAssignType.getValue())) {
        			count = taskCntAssignType.getCount();
        		}
        	}
        	
        	sb.append("    <activity activity_id=\""+taskCntType.getValue()+"\" activity_name=\""+taskCntType.getValue()+"\">\n");
			sb.append("        <wi_data>\n");
			sb.append("		       <wi_creation_date></wi_creation_date>\n");
			sb.append("		       <wi_completion_date></wi_completion_date>\n");
			sb.append("		       <wi_state></wi_state>\n");
			sb.append("        </wi_data>\n");
			sb.append("        <sensor_data>\n");
			sb.append("		       <sensor_name></sensor_name>\n");
			sb.append("		       <sensor_target></sensor_target>\n");
			sb.append("		       <eval_point></eval_point>\n");
			sb.append("        </sensor_data>\n");
			sb.append("        <custom_data>\n");
			sb.append("		       <activity_type></activity_type>\n");
			sb.append("		       <activity_id>"+taskCntType.getValue()+"</activity_id>\n");
			sb.append("		       <activity_name>"+taskCntType.getValue()+"</activity_name>\n");
			sb.append("		       <group_processing_type></group_processing_type>\n");	//현재 미 구현 상태
			sb.append("		       <creation_date></creation_date>\n");
			sb.append("		       <completion_date></completion_date>\n");
			//sb.append("		       <task_id>"+(ai_start.getUserTaskNumber() == null ? "" : getTaskIdByNumber(ctx, ai_start.getUserTaskNumber().intValue()))+"</task_id>\n");
			sb.append("		       <task_id></task_id>\n"); //. API 사용해서 구현하면 도미.
			sb.append("		       <participants_id></participants_id>\n");
			//sb.append("		       <participants>"+(ai_start.getUserTaskNumber() == null ? "" : WorkflowUtility.getTaskAssignees(ctx, ai_start.getUserTaskNumber().intValue()).get("name"))+"</participants>\n");
			sb.append("		       <participants></participants>\n");
			sb.append("		       <participants_group_id></participants_group_id>\n");	// 미규현 상태
			sb.append("		       <participants_group></participants_group>\n");	// 미규현 상태
			sb.append("		       <is_fault></is_fault>\n");
			sb.append("		       <is_overdue></is_overdue>\n");
			sb.append("		       <is_skip></is_skip>\n");
			sb.append("		       <status></status>\n");
			sb.append("		       <outcome></outcome>\n");
			sb.append("		       <lead_time></lead_time>\n");
			sb.append("		       <activity_lead_time></activity_lead_time>\n");
			sb.append("		       <retry_count>"+(count == 0 ? "" : count)+"</retry_count>\n");
			sb.append("		       <is_participation></is_participation>\n");
			sb.append("        </custom_data>\n");
			sb.append("		</activity>\n");
			
			count = 0;
        }
			
		sb.append("</activities>\n");
		sb.append("</process> \n");
		
		return sb.toString();
	}
	
	public HashMap<String,Integer> getAggregatedTaskCountByUser(IWorkflowContext wfctx, Map<String, String> params) throws Exception {
		String assignmentfilter = (String) params.get("assignmentfilter");	//MY_AND_GROUP OR ALL
		
		ITaskQueryService querySvc = soaClient.getWorkflowServiceClient().getTaskQueryService();
        //조회할 컬럼을 정의한다.
        List<String> inboxColumns = new ArrayList<String>();
        
        inboxColumns.add(TableConstants.WFTASK_APPLICATIONNAME_COLUMN.getName());
        inboxColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());    // Task Number
        inboxColumns.add(TableConstants.WFTASK_ASSIGNEES_COLUMN.getName());          // 액티비티
        
        ITaskQueryService.AssignmentFilter assignmentFilter = null;
        if(assignmentfilter.equals("MY_AND_GROUP"))
            assignmentFilter = ITaskQueryService.AssignmentFilter.MY_AND_GROUP;
        else
            assignmentFilter = ITaskQueryService.AssignmentFilter.ALL;
		
        Predicate predicate = null;
        predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, IWorkflowConstants.TASK_STATE_ASSIGNED);
        predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_ACTIVITYID_COLUMN, Predicate.OP_EQ, (String) params.get("activityid"));
        
        List<TaskCountType> task_aggt_count = (List<TaskCountType>)querySvc.queryAggregatedTasks(wfctx, Column.getColumn(WFTaskConstants.ASSIGNEES_COLUMN), assignmentFilter, null, predicate, false, false);
        
        HashMap<String,Integer> aggr_task_count_by_user = new HashMap<String, Integer>();
        for(TaskCountType taskCntType : task_aggt_count) {
        	aggr_task_count_by_user.put(taskCntType.getValue(), taskCntType.getCount());
        	
        }
			
		return aggr_task_count_by_user;
	}
	
	public HashMap<String,Integer> getAggregatedTaskCountByState(IWorkflowContext wfctx, Map<String, String> params) throws Exception {
		String assignmentfilter = (String) params.get("assignmentfilter");	//MY_AND_GROUP OR ALL
		
		ITaskQueryService querySvc = soaClient.getWorkflowServiceClient().getTaskQueryService();
        //조회할 컬럼을 정의한다.
        List<String> inboxColumns = new ArrayList<String>();
        
        inboxColumns.add(TableConstants.WFTASK_APPLICATIONNAME_COLUMN.getName());
        inboxColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());    // Task Number
        inboxColumns.add(TableConstants.WFTASK_STATE_COLUMN.getName());          // 액티비티
        
        ITaskQueryService.AssignmentFilter assignmentFilter = null;
        if(assignmentfilter.equals("MY_AND_GROUP"))
            assignmentFilter = ITaskQueryService.AssignmentFilter.MY_AND_GROUP;
        else
            assignmentFilter = ITaskQueryService.AssignmentFilter.ALL;
		
        Predicate predicate = null;
        //predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, IWorkflowConstants.TASK_STATE_ASSIGNED);
        predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_ACTIVITYID_COLUMN, Predicate.OP_EQ, (String) params.get("activityid"));
        
        List<TaskCountType> task_aggt_count = (List<TaskCountType>)querySvc.queryAggregatedTasks(wfctx, Column.getColumn(WFTaskConstants.STATE_COLUMN), assignmentFilter, null, predicate, false, false);
        
        HashMap<String,Integer> aggr_task_count_by_state = new HashMap<String, Integer>();
        for(TaskCountType taskCntType : task_aggt_count) {
        	aggr_task_count_by_state.put(taskCntType.getValue(), taskCntType.getCount());
        	
        }
			
		return aggr_task_count_by_state;
	}
	
	public void mergeCustomBPMTrackerModel(CustomBPMTrackerModelVO trackerModelVO) throws Exception {
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
	    customBPMTrackerDAO.mergeCustomBPMTrackerModel(trackerModelVO);
	}
	
	public void insertCustomBPMTrackerModel(CustomBPMTrackerModelVO trackerModelVO) throws Exception {
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
	    customBPMTrackerDAO.insertCustomBPMTrackerModel(trackerModelVO);
	}
	
	public void updateCustomBPMTrackerModel(CustomBPMTrackerModelVO trackerModelVO) throws Exception {
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
	    customBPMTrackerDAO.updateCustomBPMTrackerModel(trackerModelVO);
	}
	
	public String getSubprocessInfo(IBPMContext bpmCtx, Map<String, String> params) throws Exception {
		String instance_id = (String)params.get("instance_id");
		String activity_id = (String)params.get("activity_id");
		
		CustomBPMTrackerDAO customBPMTrackerDAO = sqlSession.getMapper(CustomBPMTrackerDAO.class);
		
		// 루프를 돌 경우 두개 이상 나올 수 있음. (최신 데이터 한개만 나오도록 수정 해야 함.)
		CustomBPMTrackerDataVO customBPMTrackerDataVO = customBPMTrackerDAO.getParentTrackerData(instance_id, activity_id);
		
		logger.debug(customBPMTrackerDataVO.getPartition_id());
		logger.debug(customBPMTrackerDataVO.getComposite_id());
		logger.debug(customBPMTrackerDataVO.getProcess_id());
		logger.debug(customBPMTrackerDataVO.getRevision_tag());
		logger.debug(String.valueOf(customBPMTrackerDataVO.getInstance_id()));
		
		String subInfo = "{\"partition_id\":\""+customBPMTrackerDataVO.getPartition_id()+"\",\"composite_id\":\""+customBPMTrackerDataVO.getComposite_id()+"\",\"process_id\":\""+customBPMTrackerDataVO.getProcess_id()+"\",\"version\":\""+customBPMTrackerDataVO.getRevision_tag()+"\",\"instance_id\":\""+customBPMTrackerDataVO.getInstance_id()+"\"}";
		
		return subInfo;
		
	}
	
	public String getSubprocessInfoByAPI(IBPMContext bpmCtx, Map<String, String> params) throws Exception {
		String instanceId = (String)params.get("instance_id");
		String activityId = (String)params.get("activity_id");
		String auditInstanceType = (String)params.get("audit_instance_type");
		String subInfo = "";
		
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		
		//context, expression, parameter
		// 조회 조건을 인스턴스 아이디로 한다......
		String expression = IAuditConstants.COLUMN_COMPONENTINSTANCEID + " = :componentId "; 
		String parent_ref_id = "";
		
		// Source Activity ID로 조회하면 SEND, RECEIVE 둘다 나온다.
		expression = expression + " AND " + IAuditConstants.COLUMN_TARGETACTIVITY + " = :targetActivity ";
		expression = expression + " AND " + IAuditConstants.COLUMN_ACTIVITYID + " != :activityId ";
		expression = expression + " AND " + IAuditConstants.COLUMN_AUDITINSTANCETYPE + " = :auditInstanceType ";
		
		// 서브 프로세스 정보를 가져오기 위한 조건 (BPM_AUDIT_QUERY 테이블 참조)
		// scope id를 가져오기 위함.
		// Source (SEND/Throw) 의 Scope ID (ParentRefId)를 알아야 Sub를 가져올 수 있음.
		// 현재 알고 있는 정보는 Target ID만 알기 때문에, Target ID를 이용해 Source의 ScopeID를 가져올 수 있는 조건이 필요.
		parameters.put("componentId", instanceId);
		parameters.put("targetActivity", activityId);
		parameters.put("activityId", activityId);
		parameters.put("auditInstanceType", auditInstanceType);
		
		logger.debug("getSubprocessInfo.instanceId : " + instanceId);
		logger.debug("getSubprocessInfo.targetActivity : " + activityId);
		logger.debug("getSubprocessInfo.auditInstanceType : "+ auditInstanceType);
		
		List<IAuditInstance> auditlist = soaClient.getBPMServiceClient().getInstanceQueryService().queryAuditInstances(bpmCtx, expression, parameters);
		
		logger.debug("getSubprocessInfo.auditlist.size() : "+auditlist.size());
		
		for(IAuditInstance audit : auditlist) {
			parent_ref_id = audit.getScopeId();
			logger.debug("parent_ref_id : " +  parent_ref_id);
		}
		
		ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
		compInstFilter.setParentReferenceId(parent_ref_id);
		compInstFilter.setEngineType("bpmn");
		compInstFilter.setParentId("bpmn:" + instanceId);
		
		// componentdn
		List<ComponentInstance> compInstances = soaClient.getLocator().getComponentInstances(compInstFilter);
		logger.debug("compInstances.size() : " + compInstances.size());
		for(ComponentInstance compInstance : compInstances) {
			logger.debug(compInstance.getCikey() + "/" + compInstance.getParentReferenceId());
			subInfo = "{\"compositedn\":\""+compInstance.getCompositeDN()+"\",\"id\":\""+compInstance.getId().substring(compInstance.getId().indexOf(":")+1)+"\",\"componentnm\":\""+compInstance.getComponentName()+"\"}";	
		}
		
		return subInfo;
		
	}
}
