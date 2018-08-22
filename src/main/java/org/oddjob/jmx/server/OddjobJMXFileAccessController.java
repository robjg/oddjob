package org.oddjob.jmx.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.security.auth.Subject;

/**
 * This is a big rip off of 
 * {@link com.sun.jmx.remote.security.MBeanServerFileAccessController}
 */
public class OddjobJMXFileAccessController implements OddjobJMXAccessController {

    public static final String READONLY = "readonly";
    public static final String READWRITE = "readwrite";

    /**
     * <p>Create a new MBeanServerAccessController that forwards all the
     * MBeanServer requests to the MBeanServer set by invoking the {@link
     * #setMBeanServer} method after doing access checks based on read and
     * write permissions.</p>
     *
     * <p>This instance is initialized from the specified properties file.</p>
     *
     * @param accessFileName name of the file which denotes a properties
     * file on disk containing the username/access level entries.
     *
     * @exception IOException if the file does not exist, is a
     * directory rather than a regular file, or for some other
     * reason cannot be opened for reading.
     *
     * @exception IllegalArgumentException if any of the supplied access
     * level values differs from "readonly" or "readwrite".
     */
    public OddjobJMXFileAccessController(String accessFileName)
        throws IOException {
        super();
        this.accessFileName = accessFileName;
        props = propertiesFromFile(accessFileName);
        checkValues(props);
    }


    /**
     * <p>Create a new MBeanServerAccessController that forwards all the
     * MBeanServer requests to the MBeanServer set by invoking the {@link
     * #setMBeanServer} method after doing access checks based on read and
     * write permissions.</p>
     *
     * <p>This instance is initialized from the specified properties instance.
     * This constructor makes a copy of the properties instance using its
     * <code>clone</code> method and it is the copy that is consulted to check
     * the username and access level of an incoming connection. The original
     * properties object can be modified without affecting the copy. If the
     * {@link #refresh} method is then called, the
     * <code>MBeanServerFileAccessController</code> will make a new copy of the
     * properties object at that time.</p>
     *
     * @param accessFileProps properties list containing the username/access
     * level entries.
     *
     * @exception IllegalArgumentException if <code>accessFileProps</code> is
     * <code>null</code> or if any of the supplied access level values differs
     * from "readonly" or "readwrite".
     */
    public OddjobJMXFileAccessController(Properties accessFileProps)
        throws IOException {
        super();
        if (accessFileProps == null)
            throw new IllegalArgumentException("Null properties");
        originalProps = accessFileProps;
        props = (Properties) accessFileProps.clone();
        checkValues(props);
    }

    @Override
    public boolean isAccessable(MBeanOperationInfo opInfo) {
    	if (opInfo.getImpact() == MBeanOperationInfo.INFO) {
            return checkAccessLevel(READONLY);
    	}
    	else {
    		return checkAccessLevel(READWRITE);
    	}
    }

    /**
     * <p>Refresh the set of username/access level entries.</p>
     *
     * <p>If this instance was created using the
     * {@link #MBeanServerFileAccessController(String)} or
     * {@link #MBeanServerFileAccessController(String,MBeanServer)}
     * constructors to specify a file from which the entries are read,
     * the file is re-read.</p>
     *
     * <p>If this instance was created using the
     * {@link #MBeanServerFileAccessController(Properties)} or
     * {@link #MBeanServerFileAccessController(Properties,MBeanServer)}
     * constructors then a new copy of the <code>Properties</code> object
     * is made.</p>
     *
     * @exception IOException if the file does not exist, is a
     * directory rather than a regular file, or for some other
     * reason cannot be opened for reading.
     *
     * @exception IllegalArgumentException if any of the supplied access
     * level values differs from "readonly" or "readwrite".
     */
    public void refresh() throws IOException {
        synchronized (props) {
            if (accessFileName == null)
                props = (Properties) originalProps.clone();
            else
                props = propertiesFromFile(accessFileName);
            checkValues(props);
        }
    }

    private static Properties propertiesFromFile(String fname)
        throws IOException {
        FileInputStream fin = new FileInputStream(fname);
        Properties p = new Properties();
        p.load(fin);
        fin.close();
        return p;
    }

    private boolean checkAccessLevel(String accessLevel) {
        final AccessControlContext acc = AccessController.getContext();
        final Subject s = (Subject)
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        return Subject.getSubject(acc);
                    }
                });
        if (s == null) return true; /* security has not been enabled */
        final Set<Principal> principals = s.getPrincipals();
        for (Iterator<Principal> i = principals.iterator(); i.hasNext(); ) {
            final Principal p = (Principal) i.next();
            String grantedAccessLevel;
            synchronized (props) {
                grantedAccessLevel = props.getProperty(p.getName());
            }
            if (grantedAccessLevel != null) {
                if (accessLevel.equals(READONLY) &&
                    (grantedAccessLevel.equals(READONLY) ||
                     grantedAccessLevel.equals(READWRITE)))
                    return true;
                if (accessLevel.equals(READWRITE) &&
                    grantedAccessLevel.equals(READWRITE))
                    return true;
            }
        }
        return false;
    }

    private void checkValues(Properties props) {
        Collection<Object> c = props.values();
        for (Iterator<Object> i = c.iterator(); i.hasNext(); ) {
            final String accessLevel = (String) i.next();
            if (!accessLevel.equals(READONLY) &&
                !accessLevel.equals(READWRITE)) {
                throw new IllegalArgumentException(
                    "Syntax error in access level entry [" + accessLevel + "]");
            }
        }
    }

    private Properties props;
    private Properties originalProps;
    private String accessFileName;
}
