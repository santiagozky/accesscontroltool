package biz.netcentric.cq.tools.actool.helper.runtime;

import javax.jcr.Node;
import javax.jcr.Session;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides basic context information about the runtime. In own package so it can be inlined with "Conditional-Package" from startup bundle */
public class RuntimeHelper {
    public static final Logger LOG = LoggerFactory.getLogger(RuntimeHelper.class);

    private static final String INSTALLER_CORE_BUNDLE_SYMBOLIC_ID = "org.apache.sling.installer.core";

    public static boolean isCompositeNodeStore(Session session) {
        
        try {
            String pathToCheck = "/apps";
            Node appsNode = session.getNode(pathToCheck);
            
            boolean hasPermission = session.hasPermission("/", Session.ACTION_SET_PROPERTY);
            if(!hasPermission) {
                // this can be ok for multitenancy cases that run with user of package installation (via install hook)
                LOG.info("AC Tool is running with a session (userID: "+session.getUserID()+") that does not have permissions '"+ Session.ACTION_SET_PROPERTY+"' at "+pathToCheck);
            }

            // see https://issues.apache.org/jira/browse/OAK-6563
            boolean hasCapability = session.hasCapability("addNode", appsNode, new Object[] { "nt:folder" });
            
            boolean isCompositeNode = hasPermission && !hasCapability;
            return isCompositeNode;
        } catch(Exception e) {
            throw new IllegalStateException("Could not check if session is connected to a composite node store: "+e, e);
        }
    }
    
    public static int getCurrentStartLevel() {
        return getCurrentStartLevel(FrameworkUtil.getBundle(RuntimeHelper.class).getBundleContext());
    }

    public static int getCurrentStartLevel(BundleContext bundleContext) {
        return bundleContext.getBundle(Constants.SYSTEM_BUNDLE_ID).adapt(FrameworkStartLevel.class).getStartLevel();
    }
    
    public static boolean isCloudReadyInstance() {
        
        boolean isCloudReadyInstance = true;
        Bundle[] bundles = FrameworkUtil.getBundle(RuntimeHelper.class).getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if(INSTALLER_CORE_BUNDLE_SYMBOLIC_ID.equals(bundle.getSymbolicName())) {
                isCloudReadyInstance = false;
                break;
            }
        }
        return isCloudReadyInstance;
    }

}
