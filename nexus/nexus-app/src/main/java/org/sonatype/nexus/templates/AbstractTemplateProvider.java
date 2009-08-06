package org.sonatype.nexus.templates;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractTemplateProvider<T extends Template>
    implements TemplateProvider<T>
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

    public T getTemplateById( String id )
        throws NoSuchTemplateIdException
    {
        // TODO: some other selection that simple iteration?
        List<T> templates = getTemplates();

        for ( T template : templates )
        {
            if ( StringUtils.equals( id, template.getId() ) )
            {
                return template;
            }
        }

        throw new NoSuchTemplateIdException( "Template for class='" + getTemplateClass().getName() + "' with Id='" + id
            + "' not found!" );
    }
}
