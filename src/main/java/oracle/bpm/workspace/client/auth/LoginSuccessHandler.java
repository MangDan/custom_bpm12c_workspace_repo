package oracle.bpm.workspace.client.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.verification.IWorkflowContext;
import oracle.bpm.workspace.client.config.SOAServiceClient;
import oracle.bpm.workspace.client.util.WorkflowUtility;


public class LoginSuccessHandler implements AuthenticationSuccessHandler {
	
	private String defaultTargetUrl;
	
	@Resource(name="soaClient")
    protected SOAServiceClient soaClient;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());	// Logger
	
	public String getDefaultTargetUrl() {
		return defaultTargetUrl;
	}

	public void setDefaultTargetUrl(String defaultTargetUrl) {
		this.defaultTargetUrl = defaultTargetUrl;
	}
	
	@SuppressWarnings("static-access")
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication auth) throws IOException,
			ServletException {
		
		logger.debug("===========  onAuthenticationSuccess start ===============");
		
		HttpSession session = request.getSession();
		Map<String, Object> accountData = new HashMap<String, Object>();
		
		logger.debug("auth.getName() : " + auth.getName());
		IWorkflowContext wfCtx = (IWorkflowContext) ContextCache.getContextCache().get(auth.getName());
		IWorkflowContext adminCtx = (IWorkflowContext) ContextCache.getContextCache().get("adminCtx");
		
		IBPMContext bpmCtx = (IBPMContext) ContextCache.getContextCache().get("bpm_"+auth.getName());
		
		WorkflowUtility wUtil = new WorkflowUtility();
		try {
			Map<String, Object> bpm_user_info = wUtil.getBPMUserInfo(wfCtx);
			Map<String, Object> userPrefMap = wUtil.initUserPreferences(wfCtx);
			
	        accountData.put("workflowContext", wfCtx);
	        accountData.put("workflowAdminContext", adminCtx);
	        accountData.put("bpmContext", bpmCtx);
	        accountData.put("bpm_user_info", bpm_user_info);
	        accountData.put("bpm_user_preferences", userPrefMap);
	        
	        session.setAttribute("accountData", accountData);
	        
	        SavedRequest savedRequest = 
	        	    new HttpSessionRequestCache().getRequest(request, response);
	        
	    	if (savedRequest == null) {
	    	    response.sendRedirect(request.getContextPath() + this.defaultTargetUrl);
	    	}
	    	else {
	    	    response.sendRedirect(savedRequest.getRedirectUrl());
	    	}
	    	
	        logger.debug("workflowContext user : "+ wfCtx.getUser());
	        logger.debug("===========  onAuthenticationSuccess end ===============");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
