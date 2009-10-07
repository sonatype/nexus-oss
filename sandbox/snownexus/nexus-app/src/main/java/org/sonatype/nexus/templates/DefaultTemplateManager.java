package org.sonatype.nexus.templates;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = TemplateManager.class )
public class DefaultTemplateManager
    implements TemplateManager
{
    @Requirement( role = TemplateProvider.class )
    private List<TemplateProvider> providers;

    public TemplateSet getTemplates()
    {
        return getTemplates( null );
    }

    public Template getTemplate( Object clazz, String id )
        throws NoSuchTemplateIdException
    {
        return getTemplates( clazz ).getTemplateById( id );
    }

    // ==

    protected TemplateSet getTemplates( Object clazz )
    {
        TemplateSet result = new TemplateSet( clazz );

        for ( TemplateProvider provider : providers )
        {
            TemplateSet chunk = provider.getTemplates( clazz );

            result.addAll( chunk );
        }

        return result;
    }
}
