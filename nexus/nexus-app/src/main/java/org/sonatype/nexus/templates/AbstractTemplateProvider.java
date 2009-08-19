package org.sonatype.nexus.templates;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractTemplateProvider<T extends Template>
    implements TemplateProvider
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    protected ApplicationConfiguration getApplicationConfiguration()
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
