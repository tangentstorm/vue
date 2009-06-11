
/**
 *  This is a component to take url requests for seasr components.
 *  This is based on code provided by Xavier
 */
package edu.tufts.component.servicehead;


import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.ConfigurableWebUIFragmentCallback;
import org.meandre.webui.WebUIException;

// ------------------------------------------------------------------------- 
@Component(
		baseURL = "meandre://seasr.org/components/demo/", 
		creator = "Xavier Llor&agrave, modified by Anoop", 
		description = "Service head for a service that gets data via posts v1.0 ", 
		name = "Service head", tags = "WebUI, process request", 
		mode = Mode.webui, firingPolicy = Component.FiringPolicy.all
)
// -------------------------------------------------------------------------

/**
 *  This class implements a component that using the WebUI accepts post requests
 * 
 * @author Xavier Lor&agrave;
 * @ Modified by Anoop Kumar for VUE
 */
public class ServiceHead 
implements ExecutableComponent, ConfigurableWebUIFragmentCallback {

	// -------------------------------------------------------------------------

	@ComponentProperty(
			description = "The URL path that the component will respond to",
			name = "url_path",
			defaultValue = "/service/ping"
	)
	public final static String PROP_URL_PATH = "url_path";
	
	@ComponentOutput(
			description = "A map object containing the key elements on the request and the assiciated values", 
			name = "value_map"
	)
	public final static String OUTPUT_VALUEMAP = "value_map";
	
	@ComponentOutput(
			description = "The response to be sent to the Service Tail Post.", 
			name = "response"
	)
	public final static String OUTPUT_RESPONSE = "response";
	
	@ComponentOutput(
			description = "The semaphore to signal the response was sent.", 
			name = "semaphore"
	)
	public final static String OUTPUT_SEMAPHORE = "semaphore";
	
	// -------------------------------------------------------------------------

	private PrintStream console;
	private ComponentContext ccHandle;

	// -------------------------------------------------------------------------

	public void initialize(ComponentContextProperties ccp) {
		console = ccp.getOutputConsole();
		console.println("[INFO] Initializing service head for " + ccp.getProperty(PROP_URL_PATH));
	}

	public void dispose(ComponentContextProperties ccp) {
		console.println("[INFO] Disposing service head for " + ccp.getProperty(PROP_URL_PATH));
	}

	// -------------------------------------------------------------------------

	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		try {
			this.ccHandle = cc;
			cc.startWebUIFragment((ConfigurableWebUIFragmentCallback)this);
			console.println("[INFO] Starting service head for " + cc.getProperty(PROP_URL_PATH));
			while (!cc.isFlowAborting()) {
				Thread.sleep(1000);
			}
			console.println("[INFO] Aborting for service head for" + cc.getProperty(PROP_URL_PATH));
			cc.stopWebUIFragment(this);
		} catch (Exception e) {
			throw new ComponentExecutionException(e);
		}
	}

	// -------------------------------------------------------------------------

	public String getContextPath() {
		return ccHandle.getProperty(PROP_URL_PATH);
	}
	
	public void emptyRequest(HttpServletResponse response)
			throws WebUIException {
//		Log.warn("Empty request should have never been called! for "+ccHandle.getProperty(PROP_URL_PATH));
	}

	@SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws WebUIException {
		Map<String,String[]> map = new Hashtable<String,String[]>();
		Enumeration mapRequest = request.getParameterNames();
		while ( mapRequest.hasMoreElements() ) {
			String sName = mapRequest.nextElement().toString();
			String [] sa = request.getParameterValues(sName);
			map.put(sName, sa);
		}
		
		try {
			Semaphore sem = new Semaphore(1, true);
			sem.acquire();
			ccHandle.pushDataComponentToOutput(OUTPUT_VALUEMAP, map);
			ccHandle.pushDataComponentToOutput(OUTPUT_RESPONSE, response);
			ccHandle.pushDataComponentToOutput(OUTPUT_SEMAPHORE, sem);
			sem.acquire();
			sem.release();
		} catch (InterruptedException e) {
			throw new WebUIException(e);
		} catch (ComponentContextException e) {
			throw new WebUIException(e);
		}		
	}

}
