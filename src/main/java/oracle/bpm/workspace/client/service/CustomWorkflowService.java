package oracle.bpm.workspace.client.service;

import java.util.List;
import java.util.Map;

import oracle.bpel.services.workflow.task.model.Task;
import oracle.bpel.services.workflow.verification.IWorkflowContext;
import oracle.bpm.workspace.client.vo.TaskHistoryVO;
import oracle.bpm.workspace.client.vo.TaskVO;

public interface CustomWorkflowService {
	public List<TaskVO> getTaskList(IWorkflowContext wfCtx, Map<String, String> params) throws Exception;
	public TaskVO getTaskDetail(IWorkflowContext wfCtx, int taskNumber) throws Exception;
	public void doAction(IWorkflowContext wfCtx, Map<String, String> params) throws Exception;
	public List<TaskVO> getDelayTaskList(IWorkflowContext wfCtx, Map<String, String> params) throws Exception;
	public List<TaskHistoryVO> getShortTaskHistory(IWorkflowContext wfCtx, Map<String, String> params) throws Exception;
}
