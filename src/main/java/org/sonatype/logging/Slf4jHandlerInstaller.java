package org.sonatype.logging;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Simply installs the JULOverSlf4j handler.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.logging.Slf4jHandlerInstaller"
 */
public class Slf4jHandlerInstaller
    implements Initializable
{
    public void initialize()
        throws InitializationException
    {
        SLF4JBridgeHandler.install();
    }
}
