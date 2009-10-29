package org.sonatype.security.legacyadapter;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

@Component(role = LegacySecurityAdapter.class )
public class LegacySecurityAdapter implements Startable
{
    
    @Requirement
    private PlexusContainer plexusContainer;

    public void start()
        throws StartingException
    {
        // generate the components....
       System.out.println( "GENERATE" );
       
       
       
    }

    public void stop()
        throws StoppingException
    {
        // nothing to do
    }

}
