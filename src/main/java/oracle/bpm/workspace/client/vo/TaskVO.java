package oracle.bpm.workspace.client.vo;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

@SuppressWarnings("serial")
public class TaskVO implements Serializable {
	private String partition;
	private int number;
	private String id;
	private String title;
	private String state;
	private String activityName;
	private String outcome;
	private int priority;
	private String compositeName;
	private String processName;
	private String processDn;
	private List comments;
	private List attachments;
	private Map<String,String> systemActions;
	private Map<String,String> customActions;
	private Element xpalyload;
	private String creator;
	private String acquirer;
	private String participantName;
	private String assigneeUsers;
	private String assigneeDisplayName;
	private String createDate;
	private String endDate;
	private String expiryDate;
	private String updateDate;
	private String instanceId;
	private String taskDisplayUrl;
	private String taskApplicationName;
	private String taskUri;
	private String taskFormDisplayName;
	private String taskFormName;
	private String textAttribute1;
	private String textAttribute2;
	private Calendar dateAttribute1;
	private String bpmCtxToken;
	private String processDelayTime;
	
	private String textAttribute3;
	private String textAttribute4;
	private String textAttribute5;
	private String textAttribute6;
	private String textAttribute7;
	private String textAttribute8;
	private String textAttribute9;
	private String textAttribute10;
	private String textAttribute11;
	private String textAttribute12;
	private String textAttribute13;
	private String textAttribute14;
	private String textAttribute15;
	private String textAttribute16;
	
	private Calendar dateAttribute2;
	private Double numberAttribute1;
	private Double numberAttribute2;
	
	private String approvalLine;
	private Calendar assignedDate;
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	
	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	public String getProcessDn() {
		return processDn;
	}

	public void setProcessDn(String processDn) {
		this.processDn = processDn;
	}

	public String getCompositeName() {
		return compositeName;
	}

	public void setCompositeName(String compositeName) {
		this.compositeName = compositeName;
	}

	public List getComments() {
		return comments;
	}

	public void setComments(List comments) {
		this.comments = comments;
	}

	public List getAttachments() {
		return attachments;
	}

	public void setAttachments(List attachments) {
		this.attachments = attachments;
	}

	public Map<String,String> getSystemActions() {
		return systemActions;
	}

	public void setSystemActions(Map<String,String> systemActions) {
		this.systemActions = systemActions;
	}

	public Map<String,String> getCustomActions() {
		return customActions;
	}

	public void setCustomActions(Map<String,String> customActions) {
		this.customActions = customActions;
	}

	public Element getXpalyload() {
		return xpalyload;
	}

	public void setXpalyload(Element xpalyload) {
		this.xpalyload = xpalyload;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	
	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
	
	public String getAssigneeUsers() {
		return assigneeUsers;
	}

	public void setAssigneeUsers(String assigneeUsers) {
		this.assigneeUsers = assigneeUsers;
	}
	
	public String getAssigneeDisplayName() {
		return assigneeDisplayName;
	}

	public void setAssigneeDisplayName(String assigneeDisplayName) {
		this.assigneeDisplayName = assigneeDisplayName;
	}
	
	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	
	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getTaskDisplayUrl() {
		return taskDisplayUrl;
	}

	public void setTaskDisplayUrl(String taskDisplayUrl) {
		this.taskDisplayUrl = taskDisplayUrl;
	}

	public String getTaskApplicationName() {
		return taskApplicationName;
	}

	public void setTaskApplicationName(String taskApplicationName) {
		this.taskApplicationName = taskApplicationName;
	}

	public String getTaskUri() {
		return taskUri;
	}

	public void setTaskUri(String taskUri) {
		this.taskUri = taskUri;
	}

	public String getTaskFormDisplayName() {
		return taskFormDisplayName;
	}

	public void setTaskFormDisplayName(String taskFormDisplayName) {
		this.taskFormDisplayName = taskFormDisplayName;
	}

	public String getTaskFormName() {
		return taskFormName;
	}

	public void setTaskFormName(String taskFormName) {
		this.taskFormName = taskFormName;
	}

	public String getTextAttribute1() {
		return textAttribute1;
	}

	public void setTextAttribute1(String textAttribute1) {
		this.textAttribute1 = textAttribute1;
	}

	public String getTextAttribute2() {
		return textAttribute2;
	}

	public void setTextAttribute2(String textAttribute2) {
		this.textAttribute2 = textAttribute2;
	}

	public Calendar getDateAttribute1() {
		return dateAttribute1;
	}

	public void setDateAttribute1(Calendar dateAttribute1) {
		this.dateAttribute1 = dateAttribute1;
	}

	public String getBpmCtxToken() {
		return bpmCtxToken;
	}

	public void setBpmCtxToken(String bpmCtxToken) {
		this.bpmCtxToken = bpmCtxToken;
	}

	
	
	public String getTextAttribute3() {
		return textAttribute3;
	}

	public void setTextAttribute3(String textAttribute3) {
		this.textAttribute3 = textAttribute3;
	}

	public String getTextAttribute4() {
		return textAttribute4;
	}

	public void setTextAttribute4(String textAttribute4) {
		this.textAttribute4 = textAttribute4;
	}

	public String getTextAttribute5() {
		return textAttribute5;
	}

	public void setTextAttribute5(String textAttribute5) {
		this.textAttribute5 = textAttribute5;
	}

	public String getTextAttribute6() {
		return textAttribute6;
	}

	public void setTextAttribute6(String textAttribute6) {
		this.textAttribute6 = textAttribute6;
	}

	public String getTextAttribute7() {
		return textAttribute7;
	}

	public void setTextAttribute7(String textAttribute7) {
		this.textAttribute7 = textAttribute7;
	}

	public String getTextAttribute8() {
		return textAttribute8;
	}

	public void setTextAttribute8(String textAttribute8) {
		this.textAttribute8 = textAttribute8;
	}

	public String getTextAttribute9() {
		return textAttribute9;
	}

	public void setTextAttribute9(String textAttribute9) {
		this.textAttribute9 = textAttribute9;
	}

	public String getTextAttribute10() {
		return textAttribute10;
	}

	public void setTextAttribute10(String textAttribute10) {
		this.textAttribute10 = textAttribute10;
	}

	public String getTextAttribute11() {
		return textAttribute11;
	}

	public void setTextAttribute11(String textAttribute11) {
		this.textAttribute11 = textAttribute11;
	}

	public String getTextAttribute12() {
		return textAttribute12;
	}

	public void setTextAttribute12(String textAttribute12) {
		this.textAttribute12 = textAttribute12;
	}

	public String getTextAttribute13() {
		return textAttribute13;
	}

	public void setTextAttribute13(String textAttribute13) {
		this.textAttribute13 = textAttribute13;
	}

	public String getTextAttribute14() {
		return textAttribute14;
	}

	public void setTextAttribute14(String textAttribute14) {
		this.textAttribute14 = textAttribute14;
	}

	public String getTextAttribute15() {
		return textAttribute15;
	}

	public void setTextAttribute15(String textAttribute15) {
		this.textAttribute15 = textAttribute15;
	}

	public String getTextAttribute16() {
		return textAttribute16;
	}

	public void setTextAttribute16(String textAttribute16) {
		this.textAttribute16 = textAttribute16;
	}

	public Calendar getDateAttribute2() {
		return dateAttribute2;
	}

	public void setDateAttribute2(Calendar dateAttribute2) {
		this.dateAttribute2 = dateAttribute2;
	}

	public Double getNumberAttribute1() {
		return numberAttribute1;
	}

	public void setNumberAttribute1(Double numberAttribute1) {
		this.numberAttribute1 = numberAttribute1;
	}

	
	public String getApprovalLine() {
		return approvalLine;
	}

	public void setApprovalLine(String approvalLine) {
		this.approvalLine = approvalLine;
	}

	public String getProcessDelayTime() {
		return processDelayTime;
	}

	public void setProcessDelayTime(String processDelayTime) {
		this.processDelayTime = processDelayTime;
	}
	
	public Double getNumberAttribute2() {
		return numberAttribute2;
	}

	public void setNumberAttribute2(Double numberAttribute2) {
		this.numberAttribute2 = numberAttribute2;
	}
	
	public Calendar getAssignedDate() {
		return assignedDate;
	}

	public void setAssignedDate(Calendar calendar) {
		this.assignedDate = calendar;
	}

	@Override
	public String toString() {
		return "TaskVO [partition=" + partition + ", number=" + number + ", id=" + id + ", title=" + title + ", state="
				+ state + ", activityName=" + activityName + ", outcome=" + outcome + ", priority=" + priority
				+ ", compositeName=" + compositeName + ", processName=" + processName + ", processDn=" + processDn
				+ ", comments=" + comments + ", attachments=" + attachments + ", systemActions=" + systemActions
				+ ", customActions=" + customActions + ", xpalyload=" + xpalyload + ", creator=" + creator
				+ ", acquirer=" + acquirer + ", participantName=" + participantName + ", assigneeUsers=" + assigneeUsers
				+ ", assigneeDisplayName=" + assigneeDisplayName + ", createDate=" + createDate + ", endDate=" + endDate
				+ ", expiryDate=" + expiryDate + ", updateDate=" + updateDate + ", instanceId=" + instanceId
				+ ", taskDisplayUrl=" + taskDisplayUrl + ", taskApplicationName=" + taskApplicationName + ", taskUri="
				+ taskUri + ", taskFormDisplayName=" + taskFormDisplayName + ", taskFormName=" + taskFormName
				+ ", textAttribute1=" + textAttribute1 + ", textAttribute2=" + textAttribute2 + ", dateAttribute1="
				+ dateAttribute1 + ", bpmCtxToken=" + bpmCtxToken + ", processDelayTime=" + processDelayTime
				+ ", textAttribute3=" + textAttribute3 + ", textAttribute4=" + textAttribute4 + ", textAttribute5="
				+ textAttribute5 + ", textAttribute6=" + textAttribute6 + ", textAttribute7=" + textAttribute7
				+ ", textAttribute8=" + textAttribute8 + ", textAttribute9=" + textAttribute9 + ", textAttribute10="
				+ textAttribute10 + ", textAttribute11=" + textAttribute11 + ", textAttribute12=" + textAttribute12
				+ ", textAttribute13=" + textAttribute13 + ", textAttribute14=" + textAttribute14 + ", textAttribute15="
				+ textAttribute15 + ", textAttribute16=" + textAttribute16 + ", dateAttribute2=" + dateAttribute2
				+ ", numberAttribute1=" + numberAttribute1 + ", numberAttribute2=" + numberAttribute2
				+ ", approvalLine=" + approvalLine + ", assignedDate=" + assignedDate + "]";
	}

}