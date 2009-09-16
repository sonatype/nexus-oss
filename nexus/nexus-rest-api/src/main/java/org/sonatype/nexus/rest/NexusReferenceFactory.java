package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.plexus.rest.DefaultReferenceFactory;
import org.sonatype.plexus.rest.ReferenceFactory;

@Component( role = ReferenceFactory.class )
public class NexusReferenceFactory
    extends DefaultReferenceFactory
{
    @Requirement
    private GlobalRestApiSettings globalRestApiSettings;

    @Override
    public Reference getContextRoot( Request request )
    {
        Reference result = null;

        if ( globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
            && StringUtils.isNotEmpty( globalRestApiSettings.getBaseUrl() ) )
        {
            result = new Reference( globalRestApiSettings.getBaseUrl() );
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
