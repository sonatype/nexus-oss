package org.sonatype.nexus.plugins;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

@Component( role = QuasiNexus.class )
public class QuasiNexus
    implements Initializable
{
    @Requirement
    private PlexusContainer plexusContainer;

    protected PlexusContainer getContainer()
    {
        return plexusContainer;
    }

    public void initialize()
        throws InitializationException
    {
        // within Nexus, all is happening in initialize() method

        try
        {
            Assertions assertions = new Assertions( getContainer() );

            assertions.doCheck();
        }
        catch ( Exception e )
        {
            throw new InitializationException( "Bad!", e );
        }
    }

}
