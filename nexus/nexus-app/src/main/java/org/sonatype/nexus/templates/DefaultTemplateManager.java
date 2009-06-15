package org.sonatype.nexus.templates;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = TemplateManager.class )
public class DefaultTemplateManager
    implements TemplateManager
{
    @Requirement( role = TemplateProvider.class )
    private List<TemplateProvider<?>> providers;

    public List<Template<?>> getTemplates()
    {
        ArrayList<Template<?>> result = new ArrayList<Template<?>>();

        for ( TemplateProvider<?> provider : providers )
        {
            result.addAll( provider.getTemplates() );
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    public <I> TemplateProvider<I> getTemplateProviderForTarget( Class<I> clazz )
    {
        ArrayList<TemplateProvider<I>> selectedProviders = new ArrayList<TemplateProvider<I>>();

        for ( TemplateProvider<?> provider : providers )
        {
            if ( provider.getTargetClass().equals( clazz ) )
            {
                selectedProviders.add( (TemplateProvider<I>) provider );
            }
        }

        return new AggregatingTemplateProvider<I>( clazz, selectedProviders );
    }

    public <I> Template<I> getTemplate( Class<I> clazz, String id )
        throws NoSuchTemplateIdException
    {
        return getTemplateProviderForTarget( clazz ).getTemplateById( id );
    }

}
