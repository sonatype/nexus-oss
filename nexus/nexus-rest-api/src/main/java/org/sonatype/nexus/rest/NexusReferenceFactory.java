package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.plexus.rest.DefaultReferenceFactory;
import org.sonatype.plexus.rest.ReferenceFactory;

@Component( role = ReferenceFactory.class )
public class NexusReferenceFactory
    extends DefaultReferenceFactory
{

    @Requirement
    private NexusConfiguration nexusConfiguration;
    
    @Override
    public Reference getContextRoot( Request request )
    {
        Reference result = null;

        if ( this.nexusConfiguration != null && this.nexusConfiguration.isForceBaseUrl() )
        {
            result = new Reference( this.nexusConfiguration.getBaseUrl() );
        }
        else
        {
            result = request.getRootRef();
        }

        // fix for when restlet is at webapp root
        if ( StringUtils.isEmpty( result.getPath() ) )
        {
            result.setPath( "/" );
        }

        return result;
    }

}
