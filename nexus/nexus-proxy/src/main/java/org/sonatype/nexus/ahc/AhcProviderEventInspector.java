package org.sonatype.nexus.ahc;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * A trivial inspector listening for configuration changes and just resetting Ahc Provider (forcing it to recreate
 * shared client, in response to possible proxy or some other affecting config change). This could be refined later, and
 * reset only in case when proxy is changed or so, but current config framework is not completed and this information
 * lacks ("what" is changed).
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "AhcProviderEventInspector" )
public class AhcProviderEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
    @Requirement
    private AhcProvider ahcProvider;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof ConfigurationChangeEvent ) || ( evt instanceof NexusStoppedEvent );
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( evt instanceof ConfigurationChangeEvent )
        {
            ahcProvider.reset();
        }
        else if ( evt instanceof NexusStoppedEvent )
        {
            ahcProvider.close();
        }
    }
}
