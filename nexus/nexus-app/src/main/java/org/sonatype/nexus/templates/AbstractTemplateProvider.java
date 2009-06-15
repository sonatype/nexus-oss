package org.sonatype.nexus.templates;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractTemplateProvider<I>
    implements TemplateProvider<I>
{
    public Template<I> getTemplateById( String id )
        throws NoSuchTemplateIdException
    {
        // TODO: some other selection that simple iteration?
        List<Template<I>> templates = getTemplates();

        for ( Template<I> template : templates )
        {
            if ( StringUtils.equals( id, template.getId() ) )
            {
                return template;
            }
        }

        throw new NoSuchTemplateIdException( "Template for implementationClass='" + getTargetClass().getName()
            + "' with Id='" + id + "' not found!" );
    }
}
