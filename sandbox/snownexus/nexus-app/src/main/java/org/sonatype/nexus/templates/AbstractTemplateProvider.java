package org.sonatype.nexus.templates;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractTemplateProvider<T extends Template>
    implements TemplateProvider
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    public ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
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
