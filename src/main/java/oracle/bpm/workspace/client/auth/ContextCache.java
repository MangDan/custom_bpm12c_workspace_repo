// Copyright 2009-2011 Oracle Corporation. 
// All Rights Reserved. 
//
// Provided on an 'as is' basis, without warranties or conditions of any kind, 
// either express or implied, including, without limitation, any warranties or 
// conditions of title, non-infringement, merchantability, or fitness for a 
// particular purpose. You are solely responsible for determining the 
// appropriateness of using and assume any risks. You may not redistribute.
//
// Please refer to http://redstack.wordpress.com/worklist for details.

package oracle.bpm.workspace.client.auth;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.bpel.services.workflow.verification.IWorkflowContext;

/**
 * A singleton used to cache users' authentication credentials. This class
 * implements the singleton pattern, as such there will only be one instance in
 * the application. It is used to cache users' authentication credentials. When
 * the user first authenticates, an <code>IWorkflowContext</code> is created.
 * These are cached in this class to improve performance of subsequent calls to
 * APIs which require that the context be provided. The cache is stored in a
 * <code>HashMap</code> keyed on the <code>username</code> which should be
 * retrieved using the <code>HttpServletRequest.getRemoteUser()</code> API.
 */
public final class ContextCache {

	private static ContextCache contextCache;
	private static HashMap<String, IWorkflowContext> theCache;

	private Logger logger = LoggerFactory.getLogger(this.getClass());	// Logger

	private ContextCache() {
		logger.debug("ContextCache Constructed");
	}

	/**
	 * Returns a handle to the singleton <code>ContextCache</code> object.
	 * 
	 * @return the singleton <code>ContextCache</code> object.
	 */
	public static synchronized ContextCache getContextCache() {
		//MLog.log("ContextCache", "Entering getContextCache()");
		if (contextCache == null) {
			contextCache = new ContextCache();
		}
		return contextCache;
	}

	/**
	 * Prevents cloning of the object.
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Add a new <code>IWorkflowContext</code> to the cache for the given user.
	 * 
	 * @param user
	 *            The username. This is used as a key in the cache.
	 * @param ctx
	 *            The <code>IWorkflowContext</code> for the user.
	 */
	public static synchronized void put(String user, IWorkflowContext ctx) {
		//MLog.log("ContextCache", "Got put for user " + user + " ctx=" + ctx);
		if (theCache == null) {
			theCache = new HashMap<String, IWorkflowContext>();
		}
		theCache.put(user, ctx);
	}

	/**
	 * Retrieve the <code>IWorkflowContext</code> for the specified user.
	 * 
	 * @param user
	 *            The username you want to retrieve the context for.
	 * @return The <code>IWorkflowContext</code> for the user, or
	 *         <code>null</code> if there is no context in the cache for that
	 *         user.
	 */
	public static synchronized Object get(String user) {
		//MLog.log("ContextCache", "Get for user " + user);
		if (user == null)
			return null;
		if (theCache.containsKey(user)) {
			//MLog.log("ContextCache", "Found " + user);
			return theCache.get(user);
		}
		return null;
	}
	
	/**
	 * Remove the <IWorkflowContext> for the specified user from the cache. This
	 * should be called when the user's session is invalidated.
	 * 
	 * @param user
	 *            The username you want to remove the context for.
	 */
	public static synchronized void remove(String user) {
		//MLog.log("ContextCache", "Remove user " + user);
		if (user != null) {
			if (theCache.containsKey(user)) {
				theCache.remove(user);
			}
		}
	}

}
