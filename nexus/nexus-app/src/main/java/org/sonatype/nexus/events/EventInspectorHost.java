package org.sonatype.nexus.events;

import org.sonatype.nexus.proxy.events.EventListener;

/**
 * A component that receives events and simply re-emits then to the registered EventInspectors.
 * 
 * @author cstamas
 */
public interface EventInspectorHost
    extends EventListener
{

}
