package oracle.bpm.workspace.client.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import oracle.bpel.services.workflow.IWorkflowConstants;
import oracle.bpel.services.workflow.query.ITaskQueryService;
import oracle.bpel.services.workflow.query.ITaskQueryService.OptionalInfo;
import oracle.bpel.services.workflow.query.model.TaskCountType;
import oracle.bpel.services.workflow.repos.Column;
import oracle.bpel.services.workflow.repos.Ordering;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.repos.table.WFTaskConstants;
import oracle.bpel.services.workflow.task.model.ActionType;
import oracle.bpel.services.workflow.task.model.CommentType;
import oracle.bpel.services.workflow.task.model.ShortHistoryTaskType;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.bpel.services.workflow.verification.IWorkflowContext;
import oracle.bpm.workspace.client.config.SOAServiceClient;
import oracle.bpm.workspace.client.constants.OracleConstants;
import oracle.bpm.workspace.client.service.CustomWorkflowService;
import oracle.bpm.workspace.client.util.CommonUtility;
import oracle.bpm.workspace.client.util.WorkflowUtility;
import oracle.bpm.workspace.client.util.XmlUtility;
import oracle.bpm.workspace.client.vo.TaskHistoryVO;
import oracle.bpm.workspace.client.vo.TaskVO;

@Service("CustomWorkflowService")
public class CustomWorkflowServiceImpl implements CustomWorkflowService {
	
	@Resource(name="soaClient")
    protected SOAServiceClient soaClient;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());	// Logger

	@Override
	public List<TaskVO> getTaskList(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		String cpage = "";
		String rowSize = "";
		
		Iterator<String> paramKeys = params.keySet().iterator();
		while( paramKeys.hasNext() ) {
            String key = paramKeys.next();
            logger.debug( String.format("task key : %s, 값 : %s", key, params.get(key)) );
        }
		
		if(params.get(OracleConstants.PARAM.PAGE.CURRENT_PAGE) == null) 
			cpage = "0";
		else
			cpage = (String) params.get(OracleConstants.PARAM.PAGE.CURRENT_PAGE);
		
		if(params.get(OracleConstants.PARAM.PAGE.CURRENT_PAGE) == null) 
			rowSize = "5";
		else
			rowSize = (String) params.get(OracleConstants.PARAM.PAGE.MAX_ROWS);
		
		int startRow = Integer.parseInt(cpage) * Integer.parseInt(rowSize) + 1;
	    int endRow = startRow + Integer.parseInt(rowSize) - 1;
	    
	    
	  //조회할 컬럼을 정의한다.
		List<String> inboxColumns = new ArrayList<String>();
		
		inboxColumns.add(TableConstants.WFTASK_APPLICATIONNAME_COLUMN.getName());
		inboxColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());	// Task Number
		inboxColumns.add(TableConstants.WFTASK_TITLE_COLUMN.getName());			// Task Title
		inboxColumns.add(TableConstants.WFTASK_ACQUIREDBY_COLUMN.getName());		// Task AcquiredBy ( 획득자 )
		inboxColumns.add(TableConstants.WFTASK_ASSIGNEEUSERS_COLUMN.getName());		// Task Assigneeusers (할당자)
		inboxColumns.add(TableConstants.WFTASK_STATE_COLUMN.getName());				// Task Status (ASSIGNED, COMPLETED...)
		inboxColumns.add(TableConstants.WFTASK_OUTCOME_COLUMN.getName());			// Task outcome (APPROVE,REJECT)
		inboxColumns.add(TableConstants.WFTASK_CREATEDDATE_COLUMN.getName());		// 생성일자
		inboxColumns.add(TableConstants.WFTASK_ENDDATE_COLUMN.getName());			// 완료일자
		inboxColumns.add(TableConstants.WFTASK_FROMUSER_COLUMN.getName());			// FromUser (위임자)
		inboxColumns.add(TableConstants.WFTASK_ACTIVITYNAME_COLUMN.getName());		// 액티비티
		inboxColumns.add(TableConstants.WFTASK_TASKGROUPID_COLUMN.getName());		// Task 유형이 Group Vote 일 경우 
		inboxColumns.add(TableConstants.WFTASK_TASKDEFINITIONNAME_COLUMN.getName());	// Task Definition 명
		inboxColumns.add(TableConstants.WFTASK_PROCESSID_COLUMN.getName()); 			// 프로세스 아이디
		inboxColumns.add(TableConstants.WFTASK_PROCESSNAME_COLUMN.getName());
		inboxColumns.add(TableConstants.WFTASK_TEXTATTRIBUTE1_COLUMN.getName());		// DocType
		inboxColumns.add(TableConstants.WFTASK_TEXTATTRIBUTE2_COLUMN.getName());		// DocNum
		inboxColumns.add(TableConstants.WFTASK_DATEATTRIBUTE1_COLUMN.getName());		// Drafting Date
		inboxColumns.add(TableConstants.WFTASK_TEXTATTRIBUTE15_COLUMN.getName());		// 
		inboxColumns.add(TableConstants.WFTASK_TEXTATTRIBUTE16_COLUMN.getName());		// 
		inboxColumns.add(TableConstants.WFTASK_ASSIGNEDDATE_COLUMN.getName());		// Task  (할당자)
		
		
		// 화면상에 테이블 컬럼명을 설정한다. (JSP에서 동적으로 컬럼을 설정하기 위함이며, 선택사항임.)
		List<String> inboxColumnLabels = new ArrayList<String>();
		inboxColumnLabels.add("Title");
		inboxColumnLabels.add("Assign Users");
		inboxColumnLabels.add("State");
		inboxColumnLabels.add("Start Date");
		inboxColumnLabels.add("End Date");
		inboxColumnLabels.add("Lead Time");
		
		// 화면에서 호출할 액션 리스트를 정의한다.
		List<OptionalInfo> optionalInfo = new ArrayList<OptionalInfo>();
		optionalInfo.add(ITaskQueryService.OptionalInfo.ALL_ACTIONS); // actions do not needed for listing page
		
		
		
		// 검색 조건을 저장한다.
		Predicate predicate = null;
		// 값 안나옴, 검토필요
		//Map<String, String> mapState = WorkflowUtility.getFilterCodeList(TableConstants.WFTASK_STATE_COLUMN.getName(), wfCtx.getIsAdmin(), wfCtx.getLocale());
		
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_NEQ, IWorkflowConstants.TASK_STATE_STALE);
		
		// 상태 조회 // ASSIGNED(진행중), COMPLETED (완료), state=ASSIGNED, state=COMPLETED (assignmentFilter = PREVIOUS)
		if(!"".equals(params.get(TableConstants.WFTASK_STATE_COLUMN.getName())) && params.get(TableConstants.WFTASK_STATE_COLUMN.getName()) != null){
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, params.get(TableConstants.WFTASK_STATE_COLUMN.getName()));
			System.out.println("@@@@@aaaa");
		}
/*		else{
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, IWorkflowConstants.TASK_STATE_ASSIGNED);
			System.out.println("@@@@@bbbb");
		}*/
			
		
		// 제목조회
		if(!"".equals(params.get(TableConstants.WFTASK_TITLE_COLUMN.getName())) && params.get(TableConstants.WFTASK_TITLE_COLUMN.getName()) != null)
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_TITLE_COLUMN, Predicate.OP_LIKE, "%"+params.get(TableConstants.WFTASK_TITLE_COLUMN.getName())+"%", true);
		
		/*
		// 검색 검토 필요
        if("".equals(params.get(TableConstants.WFTASK_STATE_COLUMN.getName()))){
        	
        	Predicate innerPredicate_state = null;
        	Object [] stateArr =  mapState.keySet().toArray();

        	for(int i=0;i<stateArr.length;i++){
    			log.debug("state:"+ stateArr[i]);
        		innerPredicate_state = WorkflowUtility.setPredicate(innerPredicate_state, Predicate.OR, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, stateArr[i]);
        	}
        	predicate = new Predicate(predicate, Predicate.AND, innerPredicate_state);

        }else {
        	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, params.get(TableConstants.WFTASK_STATE_COLUMN.getName()));
        }
        */
		// 날짜 검색
        if(!"".equals(params.get("fromDate")) && params.get("fromDate") != null) {
        	Calendar fromDate = WorkflowUtility.parseDateString(params.get("fromDate") + " 00:00", wfCtx.getLocale(), wfCtx.getTimeZone());
        	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_CREATEDDATE_COLUMN, Predicate.OP_GTE, fromDate.getTime());
        }
        
        if(!"".equals(params.get("toDate")) && params.get("fromDate") != null) {
        	Calendar toDate = WorkflowUtility.parseDateString(params.get("toDate") + " 00:00", wfCtx.getLocale(), wfCtx.getTimeZone());
        	toDate.add(Calendar.DATE, 1);
        	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_CREATEDDATE_COLUMN, Predicate.OP_LT, toDate.getTime());
        }
        System.out.println("parameter : "+params.toString());
        //지연
        
        if( params.get("OVERDUE") != null && !"".equals(params.get("OVERDUE")) && "YES".equals(params.get("OVERDUE"))) {
        	Calendar curDate = Calendar.getInstance();
        	System.out.println("@@@@@@ OVEROVEROVEROVERYESYESYES");
        	logger.debug("curDate.getTime() : " + curDate.getTime());
        	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN, Predicate.OP_GT, curDate.getTime());
        }
        else if( params.get("OVERDUE") != null && !"".equals(params.get("OVERDUE")) && "NO".equals(params.get("OVERDUE"))) {
        	Calendar curDate = Calendar.getInstance();
        	System.out.println("@@@@@@ OVEROVEROVEROVERYESYESYES");
        	logger.debug("curDate.getTime() : " + curDate.getTime());
        	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN, Predicate.OP_LT, curDate.getTime());
        }
        
        //predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_TEXTATTRIBUTE1_COLUMN, Predicate.OP_EQ, params.get("docType"));
        
        Ordering ordering = null;
        
        if(!"".equals(params.get(OracleConstants.PARAM.PAGE.SORT_FIELD)) && params.get(OracleConstants.PARAM.PAGE.SORT_FIELD) != null) {
        	// ordering
            ordering = WorkflowUtility.createTaskOrdering((String) params.get(OracleConstants.PARAM.PAGE.SORT_FIELD), (String) params.get(OracleConstants.PARAM.PAGE.SORT_ORDER));
        } else {
        	// ordering
            ordering = WorkflowUtility.createTaskOrdering(TableConstants.WFTASK_CREATEDDATE_COLUMN.getName(), OracleConstants.CODE.SORT_ORDER.DESCENDING);
        }
        
        // get IWorkflowServiceClient -> get ITaskQueryService -> execute queryTasks()
        
        ITaskQueryService.AssignmentFilter defaultAssignmentFilter = null;
        
        // 관리자 일 경우
        if(wfCtx.getIsAdmin() && !"".equals(params.get(TableConstants.WFTASK_ASSIGNEEUSERS_COLUMN.getName()))){
        	defaultAssignmentFilter = ITaskQueryService.AssignmentFilter.ALL;//Admin
        	System.out.println("@@@@@0000");
        }
        else{
        	if(params.get("state") != null && !"".equals(params.get("state")) && "COMPLETED".equals(params.get("state"))){
        		defaultAssignmentFilter = ITaskQueryService.AssignmentFilter.PREVIOUS;
        		System.out.println("@@@@@1111");
        	}
        	else if(params.get("state") != null && !"".equals(params.get("state")) && "ALL".equals(params.get("state"))){
        		defaultAssignmentFilter = ITaskQueryService.AssignmentFilter.ALL;
        		System.out.println("@@@@@1111");
        	}
            else {
            	defaultAssignmentFilter = ITaskQueryService.AssignmentFilter.MY_AND_GROUP;//"My+Group";
            	System.out.println("@@@@@2222");
            }
            	
            
        }
        
        //logger.debug("Worklist where string : "+ predicate.getString());
        
        List<Task> tasks = soaClient.getTaskQueryService().queryTasks(
				wfCtx
		        ,inboxColumns
		        ,optionalInfo
		        ,defaultAssignmentFilter
		        ,null
		        ,predicate // predicate
		        ,ordering
		        ,startRow
		        ,endRow);
        
        int totalCount = soaClient.getTaskQueryService().countTasks(wfCtx, defaultAssignmentFilter, null, predicate);
        params.put(OracleConstants.PARAM.PAGE.TOTAL_ROWS, Integer.toString(totalCount));
        /* 페이지 더보기 기능으로 할 경우
        boolean moreTasks = true;
        tasks = new ArrayList();
        int start = 1;
        int end = 200;
        while (moreTasks) {
          List someTasks = getTaskQueryService().queryTasks(
                    ctx,
                    columns,
                    null,    // additional info
                    aFilter, // asssignment filter
                    null,    // keywords
                    pred,    // custom predicate
                    null,    // order
                    start,       // paging - start
                    end);      // paging - end
          tasks.addAll(someTasks);
          if (someTasks.size() < 200) {
            moreTasks = false;
          } else {
            start += 200;
            end += 200;
          }
       } */
        
        logger.debug("worklist task size : "+ tasks.size());
        
     // iterate over tasks and build return data
        List<TaskVO> result = new ArrayList<TaskVO>();
        for (int i = 0; i < tasks.size(); i++) {
			Task task = (Task)tasks.get(i);
			
			TaskVO taskVO = new TaskVO();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
			
			taskVO.setPartition(task.getSca().getApplicationName());
			taskVO.setNumber(task.getSystemAttributes().getTaskNumber());
			taskVO.setTitle(CommonUtility.notNull(task.getTitle()));
			taskVO.setId(task.getSystemAttributes().getTaskId());
			taskVO.setCreateDate(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getCreatedDate(), wfCtx.getTimeZone()));
			taskVO.setEndDate(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getEndDate(), wfCtx.getTimeZone()));
			taskVO.setOutcome(task.getSystemAttributes().getOutcome());
			taskVO.setPriority(task.getPriority());
			taskVO.setState(task.getSystemAttributes().getState());
			taskVO.setParticipantName(task.getSystemAttributes().getParticipantName());
			taskVO.setAssigneeUsers(task.getCreatorDisplayName());
			taskVO.setAssigneeUsers(WorkflowUtility.getAssigneeString(task));
			taskVO.setAssigneeDisplayName(WorkflowUtility.getAssigneeDisplayNameString(task));
			taskVO.setCompositeName(task.getSca().getCompositeName());
			taskVO.setActivityName(task.getSystemAttributes().getActivityName());
			taskVO.setProcessName(task.getProcessInfo().getProcessName());
			//taskVO.setProcessDn(task.getSca().getApplicationName() + "/" + task.getSca(). + "!" + task.getProcessInfo().getProcessVersion() + "*/" + task.getProcessInfo().getProcessName());
			taskVO.setTextAttribute1(task.getSystemMessageAttributes().getTextAttribute1());
			taskVO.setTextAttribute2(task.getSystemMessageAttributes().getTextAttribute2());
			//taskVO.setDateAttribute1(task.getSystemMessageAttributes().getDateAttribute1());
			taskVO.setInstanceId(task.getProcessInfo().getInstanceId());
			//taskVO.setTaskDisplayUrl(WorklistUtil.getTaskDisplayURL(soaClient.getWorkflowServiceClient(), wfCtx, task, null, "worklist", null));
			taskVO.setTextAttribute3(task.getSystemMessageAttributes().getTextAttribute3());//SAP user ID
			taskVO.setTextAttribute4(task.getSystemMessageAttributes().getTextAttribute4());//SAP user pw
			taskVO.setTextAttribute5(task.getSystemMessageAttributes().getTextAttribute5());//SAP Parameter
			taskVO.setTextAttribute6(task.getSystemMessageAttributes().getTextAttribute6());//SAP Parameter
			taskVO.setTextAttribute7(task.getSystemMessageAttributes().getTextAttribute7());//SAP Parameter
			taskVO.setTextAttribute8(task.getSystemMessageAttributes().getTextAttribute8());//SAP Parameter
			taskVO.setTextAttribute9(task.getSystemMessageAttributes().getTextAttribute9());//SAP Parameter
			taskVO.setTextAttribute10(task.getSystemMessageAttributes().getTextAttribute10());//expenseNumber
			taskVO.setTextAttribute11(task.getSystemMessageAttributes().getTextAttribute11());//requesterId
			taskVO.setTextAttribute12(task.getSystemMessageAttributes().getTextAttribute12());//paymentType
			taskVO.setTextAttribute13(task.getSystemMessageAttributes().getTextAttribute13());//expenseType
			taskVO.setTextAttribute14(task.getSystemMessageAttributes().getTextAttribute14());//justification
			
			task.getTitle();
			
			/*
			 * 2016.03.16 SOA RFC
			 */
			taskVO.setTextAttribute15(task.getSystemMessageAttributes().getTextAttribute15());//docnum SAP RFC
			taskVO.setTextAttribute16(task.getSystemMessageAttributes().getTextAttribute16());//currency SAP RFC
			
			taskVO.setDateAttribute1(task.getSystemMessageAttributes().getDateAttribute1()); //request date 최초요청일자
			taskVO.setDateAttribute2(task.getSystemMessageAttributes().getDateAttribute2()); //duedate 기한일자
			taskVO.setNumberAttribute1(task.getSystemMessageAttributes().getNumberAttribute1()); //amount
			taskVO.setAssignedDate(task.getSystemAttributes().getAssignedDate());
			result.add(taskVO);
          
       // Token 가져오는 부분 , BGF PoC 용, 업무 화면이 별도 구성되어 있고 taskId와 token을 넘겨야 함... 기본 task list 와 연결을 할 때 이런식으로 함..
          //IBPMContext adminBpmCtx = soaClient.getBPMUserAuthenticationService().authenticate("weblogic", "welcome1".toCharArray(), null);
          //IBPMContext bpmCtx = soaClient.getBPMUserAuthenticationService().authenticateOnBehalfOf(adminBpmCtx, wfCtx.getUser());
          
          //logger.debug("wfCtx.token : "+ wfCtx.getToken());
          //logger.debug("bpmCtx.token : "+ bpmCtx.getToken());
          
          //taskVO.setBpmCtxToken(bpmCtx.getToken());
          //bpmWorklistTaskId=&bpmWorklistContext
          
          
        }

        // sort the list by priority
        //Collections.sort(result, new MTaskComparator());

        return result;
	}
	
	/*@Override
	public List<TaskVO> getViewTaskList(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		
		return null;
	}
	*/
	
	@Override
	public TaskVO getTaskDetail(IWorkflowContext wfCtx, int taskNumber) throws Exception {
		Task task = soaClient.getTaskQueryService().getTaskDetailsByNumber(wfCtx, taskNumber);
		
		logger.debug("============= Task "+task.getSystemAttributes().getTaskNumber()+"=============");
		//XmlUtility.print();
		
		Element xPayload = task.getPayloadAsElement();
		
		if (xPayload == null) {
		      logger.debug("payload is null");
		} /*else {
			processdoc = WorkflowUtility.getJaxbPayload(xPayload);
		}*/
		
		List<ActionType> customActions = task.getSystemAttributes().getCustomActions();
		Map<String, String> cActions = new HashMap<String, String>();
		
		if(customActions != null && customActions.size() > 0) {
			for (int actionIdx=0; actionIdx < customActions.size(); actionIdx++) {
				ActionType actionType = (ActionType) customActions.get(actionIdx);
				
				cActions.put(actionType.getAction(), WorkflowUtility.getTaskOutcomeKo(actionType.getAction()));
				//cActions.put(actionType.getAction(), actionType.getDisplayName());
				
	        }
		}
			
		List<ActionType> systemActions = task.getSystemAttributes().getSystemActions();
		Map<String, String> sActions = new HashMap<String, String>();
		
		if(systemActions != null && systemActions.size() > 0) {
			for (int actionIdx=0; actionIdx <  systemActions.size(); actionIdx++) {
				ActionType actionType = (ActionType) systemActions.get(actionIdx);
				
				sActions.put(actionType.getAction(), actionType.getDisplayName());
				
	        }
		}
		
		TaskVO taskVO = new TaskVO();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		
		taskVO.setNumber(task.getSystemAttributes().getTaskNumber());
		taskVO.setId(task.getSystemAttributes().getTaskId());
		taskVO.setTitle(task.getTitle());
		//taskVO.setPayload(processdoc);
		taskVO.setXpalyload(xPayload);
		
		taskVO.setPartition(task.getSca().getApplicationName());
		taskVO.setNumber(task.getSystemAttributes().getTaskNumber());
		taskVO.setCreateDate(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getCreatedDate(), wfCtx.getTimeZone()));
		taskVO.setEndDate(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getEndDate(), wfCtx.getTimeZone()));
		taskVO.setOutcome(task.getSystemAttributes().getOutcome());
		taskVO.setPriority(task.getPriority());
		taskVO.setState(task.getSystemAttributes().getState());
		taskVO.setParticipantName(task.getSystemAttributes().getParticipantName());
		taskVO.setAssigneeUsers(task.getCreatorDisplayName());
		//  taskVO.setAssigneeUsers(WorkflowUtility.getAssigneeString(task));
		taskVO.setAssigneeDisplayName(WorkflowUtility.getAssigneeDisplayNameString(task));
		taskVO.setCompositeName(task.getSca().getCompositeName());
		taskVO.setActivityName(task.getSystemAttributes().getActivityName());
		taskVO.setProcessName(task.getProcessInfo().getProcessName());
		//taskVO.setProcessDn(task.getSca().getApplicationName() + "/" + task.getSca(). + "!" + task.getProcessInfo().getProcessVersion() + "*/" + task.getProcessInfo().getProcessName());
		taskVO.setTextAttribute1(task.getSystemMessageAttributes().getTextAttribute1());	//SAP or BPM
		taskVO.setTextAttribute2(task.getSystemMessageAttributes().getTextAttribute2());	//SAP tcode
		//taskVO.setDateAttribute1(task.getSystemMessageAttributes().getDateAttribute1());
		taskVO.setInstanceId(task.getProcessInfo().getInstanceId());
		//taskVO.setTaskDisplayUrl(WorklistUtil.getTaskDisplayURL(soaClient.getWorkflowServiceClient(), wfCtx, task, null, "worklist", null));
		
		taskVO.setTextAttribute3(task.getSystemMessageAttributes().getTextAttribute3());//SAP user ID
		taskVO.setTextAttribute4(task.getSystemMessageAttributes().getTextAttribute4());//SAP user pw
		taskVO.setTextAttribute5(task.getSystemMessageAttributes().getTextAttribute5());//SAP Parameter
		taskVO.setTextAttribute6(task.getSystemMessageAttributes().getTextAttribute6());//SAP Parameter
		taskVO.setTextAttribute7(task.getSystemMessageAttributes().getTextAttribute7());//SAP Parameter
		taskVO.setTextAttribute8(task.getSystemMessageAttributes().getTextAttribute8());//SAP Parameter
		taskVO.setTextAttribute9(task.getSystemMessageAttributes().getTextAttribute9());//SAP Parameter
		taskVO.setTextAttribute10(task.getSystemMessageAttributes().getTextAttribute10());//expenseNumber
		taskVO.setTextAttribute11(task.getSystemMessageAttributes().getTextAttribute11());//requesterId
		taskVO.setTextAttribute12(task.getSystemMessageAttributes().getTextAttribute12());//paymentType
		taskVO.setTextAttribute13(task.getSystemMessageAttributes().getTextAttribute13());//expenseType
		taskVO.setTextAttribute14(task.getSystemMessageAttributes().getTextAttribute14());//justification
		/*
		 * 2016.03.16 SOA RFC
		 */
		taskVO.setTextAttribute15(task.getSystemMessageAttributes().getTextAttribute15());//docnum SAP RFC
		taskVO.setTextAttribute16(task.getSystemMessageAttributes().getTextAttribute16());//currency SAP RFC
		
		taskVO.setDateAttribute1(task.getSystemMessageAttributes().getDateAttribute1()); //request date 최초요청일자
		taskVO.setDateAttribute2(task.getSystemMessageAttributes().getDateAttribute2()); //duedate 기한일자
		taskVO.setNumberAttribute1(task.getSystemMessageAttributes().getNumberAttribute1()); //amount

		
		
		
		taskVO.setCustomActions(cActions);
		taskVO.setSystemActions(sActions);
		taskVO.setInstanceId(task.getProcessInfo().getInstanceId());
		
		logger.debug("getTaskDefinitionId : " + task.getTaskDefinitionId());
		logger.debug("getApplicationName : " + task.getSca().getApplicationName());
		logger.debug("getTaskNamespace : " + task.getSystemAttributes().getTaskNamespace());
		logger.debug("getCompositeVersion : " + task.getSca().getCompositeVersion());
		logger.debug("partition : " + task.getSca().getCompositeDN().substring(0,task.getSca().getCompositeDN().indexOf("/")));
		
		//taskdefinitionid, applicationname, formname
		//List<TaskDisplayInfoType> taskDisplayInfoTypes = soaClient.getWorkflowServiceClient().getRuntimeConfigService().getTaskDisplayInfoByTaskDefinitionId(wfCtx, task.getTaskDefinitionId(), null, null);
		
		//ps6에서 deprecate 됨
		//List<TaskDisplayInfoType> taskDisplayInfoTypes = soaClient.getWorkflowServiceClient().getRuntimeConfigService().getTaskDisplayInfoByTaskDefinitionId(wfCtx, task.getTaskDefinitionId(), null);
		//List<TaskDisplayInfoType> taskDisplayInfoTypes = soaClient.getWorkflowServiceClient().getRuntimeConfigService().getTaskDisplayInfo(wfCtx, task.getSystemAttributes().getTaskNamespace(), task.getSca().getCompositeVersion(), task.getSca().getCompositeDN().substring(0,task.getSca().getCompositeDN().indexOf("/")), null);
		
		// 여러개 등록될 수 있음... 멀티로 등록되는 부분 생기면 추가 함...
		/*for(TaskDisplayInfoType taskDisplayInfoType : taskDisplayInfoTypes) {
			logger.debug("getHostname : " + taskDisplayInfoType.getHostname());
			logger.debug("getHttpPort : " + taskDisplayInfoType.getHttpPort());
			logger.debug("getUri : " + taskDisplayInfoType.getUri());
			logger.debug("getApplicationName : " + taskDisplayInfoType.getApplicationName());
			logger.debug("getFormDisplayName : " + taskDisplayInfoType.getFormDisplayName());
			logger.debug("getFormName : " + taskDisplayInfoType.getFormName());
			
			taskVO.setTaskDisplayUrl("http://"+taskDisplayInfoType.getHostname() + ":" + taskDisplayInfoType.getHttpPort() + taskDisplayInfoType.getUri());
			taskVO.setTaskApplicationName(taskDisplayInfoType.getApplicationName());
			taskVO.setTaskUri(taskDisplayInfoType.getUri());
			taskVO.setTaskFormDisplayName(taskDisplayInfoType.getFormDisplayName());
			taskVO.setTaskFormName(taskDisplayInfoType.getFormName());
		}*/
		return taskVO;
	}
	
	@Override
	public void doAction(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		String taskNumber = (String) params.get("tasknumber");
		String outcome = (String) params.get("outcome");
		String activityName =(String) params.get("activityName");
		
		Task task = soaClient.getTaskQueryService().getTaskDetailsByNumber(wfCtx, Integer.parseInt(taskNumber));
		
		if(activityName.equals("법인카드 기안")){
			
			Element payload = task.getPayloadAsElement();
			
			Map<String, String> namespacemap = new HashMap<String, String>();
	        namespacemap.put("ns0", "http://xmlns.oracle.com/bpel/workflow/task");
	        namespacemap.put("ns1", "http://www.example.org");
			
	        Element body = XmlUtility.loadDocument((String)params.get("body_xml"), "xml");
			
			// 서로 Document 가 달라서 replace 또는 append가 안됨. importNode로 해결됨.
			WorkflowUtility.setPayload(payload, "/ns0:task/ns0:payload/ns1:expense", namespacemap, payload.getOwnerDocument().importNode(body, true));
			
			task.setPayloadAsElement(payload);
		}
		
		
		soaClient.getWorkflowServiceClient().getTaskService().updateTaskOutcome(wfCtx, task, outcome);
	
	}
	
	@Override
	public List<TaskVO> getDelayTaskList(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		
		int startRow = 0;
	    int endRow = 5;
	    
	    
	  //조회할 컬럼을 정의한다.
		List<String> inboxColumns = new ArrayList<String>();
		
		inboxColumns.add(TableConstants.WFTASK_APPLICATIONNAME_COLUMN.getName());
		inboxColumns.add(TableConstants.WFTASK_INSTANCEID_COLUMN.getName());	// Task Number
		inboxColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());	// Task Number
		inboxColumns.add(TableConstants.WFTASK_TITLE_COLUMN.getName());			// Task Title
		inboxColumns.add(TableConstants.WFTASK_ACQUIREDBY_COLUMN.getName());		// Task AcquiredBy ( 획득자 )
		inboxColumns.add(TableConstants.WFTASK_ASSIGNEEUSERS_COLUMN.getName());		// Task Assigneeusers (할당자)
		inboxColumns.add(TableConstants.WFTASK_ASSIGNEESDISPLAYNAME_COLUMN.getName());		// Task Assigneeusers (할당자)
		inboxColumns.add(TableConstants.WFTASK_APPROVERS_COLUMN.getName());		// Task Assigneeusers (할당자)
		inboxColumns.add(TableConstants.WFTASK_STATE_COLUMN.getName());				// Task Status (ASSIGNED, COMPLETED...)
		inboxColumns.add(TableConstants.WFTASK_OUTCOME_COLUMN.getName());			// Task outcome (APPROVE,REJECT)
		inboxColumns.add(TableConstants.WFTASK_CREATEDDATE_COLUMN.getName());		// 생성일자
		inboxColumns.add(TableConstants.WFTASK_ENDDATE_COLUMN.getName());			// 완료일자
		inboxColumns.add(TableConstants.WFTASK_FROMUSER_COLUMN.getName());			// FromUser (위임자)
		inboxColumns.add(TableConstants.WFTASK_ACTIVITYNAME_COLUMN.getName());		// 액티비티
		inboxColumns.add(TableConstants.WFTASK_TASKGROUPID_COLUMN.getName());		// Task 유형이 Group Vote 일 경우 
		inboxColumns.add(TableConstants.WFTASK_TASKDEFINITIONNAME_COLUMN.getName());	// Task Definition 명
		inboxColumns.add(TableConstants.WFTASK_PROCESSID_COLUMN.getName()); 			// 프로세스 아이디
		inboxColumns.add(TableConstants.WFTASK_PROCESSNAME_COLUMN.getName());
		inboxColumns.add(TableConstants.WFTASK_TEXTATTRIBUTE1_COLUMN.getName());		// DocType
		inboxColumns.add(TableConstants.WFTASK_TEXTATTRIBUTE2_COLUMN.getName());		// DocNum
		inboxColumns.add(TableConstants.WFTASK_DATEATTRIBUTE1_COLUMN.getName());		// Drafting Date
		inboxColumns.add(TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN.getName());		// Drafting Date
		inboxColumns.add(TableConstants.WFTASK_APPROVALDURATION_COLUMN.getName());		// Drafting Date
		
		// 화면상에 테이블 컬럼명을 설정한다. (JSP에서 동적으로 컬럼을 설정하기 위함이며, 선택사항임.)
		List<String> inboxColumnLabels = new ArrayList<String>();
		inboxColumnLabels.add("Title");
		inboxColumnLabels.add("Assign Users");
		inboxColumnLabels.add("State");
		inboxColumnLabels.add("Start Date");
		inboxColumnLabels.add("End Date");
		inboxColumnLabels.add("Lead Time");
		
		// 화면에서 호출할 액션 리스트를 정의한다.
		//List<OptionalInfo> optionalInfo = new ArrayList<OptionalInfo>();
		//optionalInfo.add(ITaskQueryService.OptionalInfo.ALL_ACTIONS); // actions do not needed for listing page
		
		Predicate predicate = null;
		
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_NEQ, IWorkflowConstants.TASK_STATE_STALE);
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_IS_NOT_NULL, "");
		
		logger.debug("TableConstants.WFTASK_INSTANCEID_COLUMN.getName() : " + TableConstants.WFTASK_INSTANCEID_COLUMN.getName());
		if(params.get(TableConstants.WFTASK_INSTANCEID_COLUMN.getName()) != null && !params.get(TableConstants.WFTASK_INSTANCEID_COLUMN.getName()).equals(""))
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_INSTANCEID_COLUMN, Predicate.OP_EQ, params.get(TableConstants.WFTASK_INSTANCEID_COLUMN.getName()));
			
		if(params.get(TableConstants.WFTASK_STATE_COLUMN.getName()) != null && !params.get(TableConstants.WFTASK_STATE_COLUMN.getName()).equals(""))
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, params.get(TableConstants.WFTASK_STATE_COLUMN.getName()));
		
		if(!"".equals(params.get(TableConstants.WFTASK_TITLE_COLUMN.getName())) && params.get(TableConstants.WFTASK_TITLE_COLUMN.getName()) != null)
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_TITLE_COLUMN, Predicate.OP_LIKE, "%"+params.get(TableConstants.WFTASK_TITLE_COLUMN.getName())+"%", true);
		
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN, Predicate.OP_IS_NOT_NULL,null);
		
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_UPDATEDBY_COLUMN, Predicate.OP_NEQ, "workflowsystem");
		
		if( params.get("OVERDUE") != null && !"".equals(params.get("OVERDUE")) && "YES".equals(params.get("OVERDUE"))) {
			if(params.get(TableConstants.WFTASK_STATE_COLUMN.getName()).equals("COMPLETED")) {
			//	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN, Predicate.OP_LTE, TableConstants.WFTASK_ENDDATE_COLUMN.getName());
				Calendar curDate = Calendar.getInstance();
				predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN, Predicate.OP_LTE, curDate.getTime());
			} else {	// ASSIGNED
				Calendar curDate = Calendar.getInstance();
		    	predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN, Predicate.OP_LTE, curDate.getTime());
			}
			
		}
		
        Ordering ordering = new Ordering(Column.getColumn(TableConstants.WFTASK_APPROVALDURATION_COLUMN.getName()),false,true);
        ordering.addClause(Column.getColumn(TableConstants.WFTASK_ASSIGNEDDATE_COLUMN.getName()), true, true);
        
        //ordering = WorkflowUtility.createTaskOrdering(TableConstants.WFTASK_DATEATTRIBUTE2_COLUMN.getName(), OracleConstants.CODE.SORT_ORDER.DESCENDING);
        
        ITaskQueryService.AssignmentFilter defaultAssignmentFilter = null;
        
        defaultAssignmentFilter = ITaskQueryService.AssignmentFilter.ALL;
		
        
        //logger.debug("Worklist where string : "+ predicate.getString());
        
        List<Task> tasks = soaClient.getTaskQueryService().queryTasks(
				wfCtx
		        ,inboxColumns
		        ,null
		        ,defaultAssignmentFilter
		        ,null
		        ,predicate // predicate
		        ,ordering
		        ,startRow
		        ,endRow);
        
        //int totalCount = soaClient.getTaskQueryService().countTasks(wfCtx, defaultAssignmentFilter, null, predicate);
        //params.put(OracleConstants.PARAM.PAGE.TOTAL_ROWS, Integer.toString(totalCount));
        /* 페이지 더보기 기능으로 할 경우
        boolean moreTasks = true;
        tasks = new ArrayList();
        int start = 1;
        int end = 200;
        while (moreTasks) {
          List someTasks = getTaskQueryService().queryTasks(
                    ctx,
                    columns,
                    null,    // additional info
                    aFilter, // asssignment filter
                    null,    // keywords
                    pred,    // custom predicate
                    null,    // order
                    start,       // paging - start
                    end);      // paging - end
          tasks.addAll(someTasks);
          if (someTasks.size() < 200) {
            moreTasks = false;
          } else {
            start += 200;
            end += 200;
          }
       } */
        
        logger.debug("worklist task size : "+ tasks.size());
        
     // iterate over tasks and build return data
        List<TaskVO> result = new ArrayList<TaskVO>();
        for (int i = 0; i < tasks.size(); i++) {
          Task task = (Task)tasks.get(i);
          
          TaskVO taskVO = new TaskVO();
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
          
          if(task.getSystemAttributes().getState().equals("COMPLETED"))	//완료
      		  logger.debug("assigneeUser1 : " + task.getSystemAttributes().getApproversDisplayNames());
      	  else
      		  logger.debug("assigneeUser2 : " + WorkflowUtility.getAssigneeDisplayNameString(task));
          
          logger.debug("taskId : " + task.getSystemAttributes().getTaskId());
      	  logger.debug("getState : " + task.getSystemAttributes().getState());
          logger.debug("getCreatedDate : " + task.getSystemAttributes().getCreatedDate());
      	  logger.debug("duedate : " + task.getSystemMessageAttributes().getDateAttribute2());
      	  logger.debug("getUser : " + wfCtx.getUser());
      	  logger.debug("timezone : " + wfCtx.getTimeZone());
      	  
      	  
      	  logger.debug("delaytime : " + WorkflowUtility.DelayCalc(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemMessageAttributes().getDateAttribute2(), wfCtx.getTimeZone())));
      	  logger.debug("-------------------------------------------");
      	
          taskVO.setPartition(task.getSca().getApplicationName());
          taskVO.setNumber(task.getSystemAttributes().getTaskNumber());
          taskVO.setTitle(CommonUtility.notNull(task.getTitle()));
          taskVO.setId(task.getSystemAttributes().getTaskId());
          taskVO.setCreateDate(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getCreatedDate(), wfCtx.getTimeZone()));
          taskVO.setEndDate(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getEndDate(), wfCtx.getTimeZone()));
          taskVO.setOutcome(task.getSystemAttributes().getOutcome());
          taskVO.setPriority(task.getPriority());
          taskVO.setState(task.getSystemAttributes().getState());
          taskVO.setParticipantName(task.getSystemAttributes().getParticipantName());
          //taskVO.setAssigneeUsers(task.getCreatorDisplayName());
          if(task.getSystemAttributes().getState().equals("COMPLETED"))	//완료
        	  taskVO.setAssigneeDisplayName((task.getSystemAttributes().getApproversDisplayNames() == null) ? task.getSystemAttributes().getApprovers() : task.getSystemAttributes().getApproversDisplayNames());
          else
        	  taskVO.setAssigneeDisplayName(WorkflowUtility.getAssigneeDisplayNameString(task));
          
          logger.debug("taskVO.getApproversDisplayNames() : " + task.getSystemAttributes().getApproversDisplayNames());
          logger.debug("taskVO.getApprovers() : " + task.getSystemAttributes().getApprovers());
          //taskVO.setAssigneeDisplayName(WorkflowUtility.getAssigneeDisplayNameString(task));
          taskVO.setCompositeName(task.getSca().getCompositeName());
          taskVO.setActivityName(task.getSystemAttributes().getActivityName());
          taskVO.setProcessName(task.getProcessInfo().getProcessName());
          //taskVO.setProcessDn(task.getSca().getApplicationName() + "/" + task.getSca(). + "!" + task.getProcessInfo().getProcessVersion() + "*/" + task.getProcessInfo().getProcessName());
          taskVO.setTextAttribute1(task.getSystemMessageAttributes().getTextAttribute1());
          taskVO.setTextAttribute2(task.getSystemMessageAttributes().getTextAttribute2());
          //taskVO.setDateAttribute1(task.getSystemMessageAttributes().getDateAttribute1());
          taskVO.setInstanceId(task.getProcessInfo().getInstanceId());
          /*if(task.getSystemAttributes().getState().equals("COMPLETED")){
        	  taskVO.setProcessDelayTime(WorkflowUtility.DelayCalc(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemAttributes().getEndDate(), wfCtx.getTimeZone())));
          }else{
        	  taskVO.setProcessDelayTime(WorkflowUtility.DelayCalc(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemMessageAttributes().getDateAttribute2(), wfCtx.getTimeZone())));
          }*/
        
          taskVO.setDateAttribute2(task.getSystemMessageAttributes().getDateAttribute2());
          //WorkflowUtility.DelayCalc(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemMessageAttributes().getDateAttribute2(), wfCtx.getTimeZone()))
          
          //taskVO.setTaskDisplayUrl(WorklistUtil.getTaskDisplayURL(soaClient.getWorkflowServiceClient(), wfCtx, task, null, "worklist", null));
          
          long approvalDuration = 0;
  		  approvalDuration = (task.getSystemAttributes().getApprovalDuration() / 1000); //초;
  		  
  		  Date resultdate1 = new Date(System.currentTimeMillis());
  		  System.out.println("currentTimeMillis : " + df.format(resultdate1));
  		  
  		  if(task.getSystemAttributes().getEndDate() != null) {
  			  Date resultdate2 = new Date(task.getSystemAttributes().getEndDate().getTimeInMillis());
  			  System.out.println("getEndDate : " + df.format(resultdate2));
  		  }
  		  Date resultdate3 = new Date(task.getSystemAttributes().getAssignedDate().getTimeInMillis());
  		  System.out.println("getAssignedDate : " + df.format(resultdate3));
  		
  		  if(approvalDuration == 0) {
  			  if(task.getSystemAttributes().getEndDate() == null) {
  				approvalDuration = ((System.currentTimeMillis()/1000) - (task.getSystemAttributes().getAssignedDate().getTimeInMillis()/1000));
  			  } else {
  				approvalDuration = ((task.getSystemAttributes().getEndDate().getTimeInMillis()/1000) - (task.getSystemAttributes().getAssignedDate().getTimeInMillis()/1000));
  			  }
  		  }
  	
  	      logger.debug("delaytime : " + WorkflowUtility.DelayCalc(WorkflowUtility.getTimeZoneBasedDateString(df, task.getSystemMessageAttributes().getDateAttribute2(), wfCtx.getTimeZone())));
  	      //logger.debug("approvalduration(초) : " +((approvalDuration/3600) == 0 ? "" : (approvalDuration/3600)+ "시간 ")+((approvalDuration % 3600 / 60) == 0 ? "" : (approvalDuration % 3600 / 60)+"분 ")+((approvalDuration % 3600 % 60) == 0 ? "" : (approvalDuration % 3600 % 60)+" 초"));
  	      logger.debug("approvalduration() : " +((approvalDuration/3600) == 0 ? "" : (approvalDuration/3600)+ "시간 ")+((approvalDuration % 3600 / 60) == 0 ? "" : (approvalDuration % 3600 / 60)+"분 "));
  		  
  	      String processDelayTime = (((approvalDuration/3600) == 0 ? "" : (approvalDuration/3600)+ "시간 ")+((approvalDuration % 3600 / 60) == 0 ? "" : (approvalDuration % 3600 / 60)+"분 "));
  	      taskVO.setProcessDelayTime(processDelayTime.equals("") ? "1분미만" : processDelayTime);
  	      
          result.add(taskVO);
          
       // Token 가져오는 부분 , BGF PoC 용, 업무 화면이 별도 구성되어 있고 taskId와 token을 넘겨야 함... 기본 task list 와 연결을 할 때 이런식으로 함..
          //IBPMContext adminBpmCtx = soaClient.getBPMUserAuthenticationService().authenticate("weblogic", "welcome1".toCharArray(), null);
          //IBPMContext bpmCtx = soaClient.getBPMUserAuthenticationService().authenticateOnBehalfOf(adminBpmCtx, wfCtx.getUser());
          
          //logger.debug("wfCtx.token : "+ wfCtx.getToken());
          //logger.debug("bpmCtx.token : "+ bpmCtx.getToken());
          
          //taskVO.setBpmCtxToken(bpmCtx.getToken());
          //bpmWorklistTaskId=&bpmWorklistContext
          
          
        }

        // sort the list by priority
        //Collections.sort(result, new MTaskComparator());

        return result;
	}
	
	public List<TaskHistoryVO> getShortTaskHistory(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		
		String tasknumber = params.get("tasknumber");
        Task task = soaClient.getWorkflowServiceClient().getTaskQueryService().getTaskDetailsByNumber(wfCtx, Integer.parseInt(tasknumber));
        
        List<ShortHistoryTaskType> shortTaskHistorys = task.getSystemAttributes().getShortHistory().getTask();
        
        List<TaskHistoryVO> result = new ArrayList<TaskHistoryVO>();
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        
        for(ShortHistoryTaskType shortTaskHistory : shortTaskHistorys) {
        	
        	
        	if(shortTaskHistory.getVersionReason() != null && shortTaskHistory.getVersionReason().equals(IWorkflowConstants.TASK_VERSION_REASON_OUTCOME_UPDATED) && !shortTaskHistory.getUpdatedBy().getId().equals("workflowsystem")) {
        		
        		TaskHistoryVO taskHistoryVO = new TaskHistoryVO();
        		taskHistoryVO.setUpdateBy(shortTaskHistory.getUpdatedBy().getId());
        		taskHistoryVO.setUpdateByDisplayName(soaClient.getBPMIdentityService().lookupUser(shortTaskHistory.getUpdatedBy().getId()).getDisplayName());
        		taskHistoryVO.setOutcome(shortTaskHistory.getOutcome());
        		taskHistoryVO.setUpdateDate(WorkflowUtility.getTimeZoneBasedDateString(df, shortTaskHistory.getUpdatedDate(), wfCtx.getTimeZone()));
        		
        		logger.debug("shortTaskHistory.getVersion() : " + shortTaskHistory.getVersion());
            	logger.debug("shortTaskHistory.getVersionReason() : " + shortTaskHistory.getVersionReason());
            	logger.debug("shortTaskHistory.getOutcome() : " + shortTaskHistory.getOutcome());
            	logger.debug("shortTaskHistory.getState() : " + shortTaskHistory.getState());
            	logger.debug("shortTaskHistory.getUpdatedBy() : " + shortTaskHistory.getUpdatedBy().getId());
            	logger.debug("shortTaskHistory.getUpdatedByName() : " + soaClient.getBPMIdentityService().lookupUser(shortTaskHistory.getUpdatedBy().getId()).getDisplayName());
            	logger.debug("shortTaskHistory.getUpdatedDate() : " + shortTaskHistory.getUpdatedDate());
            	
            	logger.debug("-------------------------------------------------------");
            	result.add(taskHistoryVO);
        	}
        	
        }
        
        return result;
	}
	
	public String getAggregatedTasks(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		/*List taskCounts = querySvc.queryAggregatedTasks(ctx,
		Column.getColumn(WFTaskConstants.STATE_COLUMN),
		 ITaskQueryService.AssignmentFilter.MY,
		keyWordFilter,
		filterPredicate,
		false,orderByCount
		false, ascendingOrder);*/
		
		Predicate predicate = null;
		
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_COMPONENTTYPE_COLUMN, Predicate.OP_EQ, IWorkflowConstants.WORKFLOW_COMPONENT_TYPE);
		predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_HASSUBTASK_COLUMN, Predicate.OP_NEQ, true);
		
		if(params.get("state") != null)
			predicate = WorkflowUtility.setPredicate(predicate, TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, (String) params.get("state"));
		
		ITaskQueryService.AssignmentFilter assignmentfilter = null;
		
		logger.debug("assignfilter : "+ (String) params.get("assignfilter"));
		if(params.get("assignfilter") != null) {
			if(((String)params.get("assignfilter")).equals("MY_AND_GROUP")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.MY_AND_GROUP;
			} else if(((String)params.get("assignfilter")).equals("ALL")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.ALL;
			} else if(((String)params.get("assignfilter")).equals("CREATOR")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.CREATOR;
			} else if(((String)params.get("assignfilter")).equals("GROUP")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.GROUP;
			} else if(((String)params.get("assignfilter")).equals("MY_AND_GROUP_ALL")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.MY_AND_GROUP_ALL;
			} else if(((String)params.get("assignfilter")).equals("MY")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.MY;
			} else if(((String)params.get("assignfilter")).equals("OWNER")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.OWNER;
			} else if(((String)params.get("assignfilter")).equals("OWNER")) {
				assignmentfilter = ITaskQueryService.AssignmentFilter.OWNER;
			}
			
		}
		
		//args : IWorkflowContext, WFTaskConstants.STATE_COLUMN, ITaskQueryService.AssignmentFilter.MY, keyWordFilter, filterPredicate, orderByCount, ascendingOrder
		List<TaskCountType> task_aggt_count = (List<TaskCountType>)soaClient.getWorkflowServiceClient().getTaskQueryService().queryAggregatedTasks(wfCtx, Column.getColumn(WFTaskConstants.STATE_COLUMN), assignmentfilter, null, predicate, false, false);
		
		// ASSIGNED 1
		String value = "";
		int totalCnt = 0;
		for(TaskCountType type : task_aggt_count) {
			totalCnt += type.getCount();
			value += (value.equals("") ? "" : ",") + "\""+ type.getValue().toLowerCase() + "\":" + type.getCount();
		}
		
		if(!value.equals("")) 
			value += ",\"all\""+":"+totalCnt;
		
		logger.debug("value : "+ "{"+value+"}");
		
		if(value.equals(""))
			value = "\"all\":0,\"assigned\":0,\"completed\":0,\"expired\":0,\"stale\":0,\"withdrawn\":0";
		
		return "{"+value+"}";
		
	}
	
	public static List getInboxColumns(Map userPrefs) {
	    List inboxColumns = new ArrayList();
	    List prefNames = new ArrayList(userPrefs.keySet());
	    //Order the preference names to ensure we preserve the order in the list of columns.
	    Collections.sort(prefNames);
	    Iterator prefIter = prefNames.iterator();
	    while (prefIter.hasNext()) {
	    	String prefName = (String) prefIter.next();
	    	if (prefName.startsWith(OracleConstants.PARAM.USER_PREF.INBOX_COL))
	    		inboxColumns.add(userPrefs.get(prefName));
	    }
	    return inboxColumns;
	}

	public void addcomment(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		
		String taskId = (String) params.get("taskId");
		String comment = (String) params.get("comment");
		
		Task task = soaClient.getTaskService().addComment(wfCtx, taskId, comment);
	}

	public void getcomments(IWorkflowContext wfCtx, Map<String, String> params) throws Exception {
		// TODO Auto-generated method stub
		String taskId = (String) params.get("taskId");
		
		logger.debug("taskId : " + taskId);
		
		Task task = soaClient.getTaskQueryService().getTaskDetailsById(wfCtx, taskId);
		List<CommentType> comments = task.getUserComment();
		
		logger.debug("comments.size() : " + comments.size());
		
		for(CommentType comment : comments) {
			logger.debug(comment.getUpdatedBy().getId() + ":" + comment.getUpdatedBy().getDisplayName());
			logger.debug(comment.getComment());
		}
		
	}
	
}
