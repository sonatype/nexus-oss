package org.sonatype.nexus.templates;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class AbstractTemplateProvider<T extends Template>
    implements TemplateProvider
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private Nexus nexus;

    public Repository createRepository( CRepository repository ) throws ConfigurationException, IOException
    {
        return this.nexus.getNexusConfiguration().createRepository( repository );
    }
    
    public ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    protected void setApplicationConfiguration( ApplicationConfiguration conf )
    {
        this.applicationConfiguration = conf;
    }

    public Template getTemplateById( String id )
        throws NoSuchTemplateIdException
    {
        TemplateSet templates = getTemplates();

        for ( Template template : templates )
        {
            if ( StringUtils.equals( id, template.getId() ) )
            {
                return template;
            }
        }

        throw new NoSuchTemplateIdException( "Template for Id='" + id + "' not found!" );
    }
}
