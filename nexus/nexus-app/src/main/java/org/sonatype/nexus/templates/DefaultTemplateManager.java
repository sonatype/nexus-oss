package org.sonatype.nexus.templates;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

@Component( role = TemplateManager.class )
public class DefaultTemplateManager
    implements TemplateManager
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement( role = TemplateProvider.class )
    private List<TemplateProvider<?>> providers;

    public List<Template> getTemplates()
    {
        ArrayList<Template> result = new ArrayList<Template>();

        for ( TemplateProvider<?> provider : providers )
        {
            result.addAll( provider.getTemplates() );
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Template> TemplateProvider<T> getTemplateProviderForTarget( Class<T> clazz )
    {
        ArrayList<TemplateProvider<T>> selectedProviders = new ArrayList<TemplateProvider<T>>();

        for ( TemplateProvider<?> provider : providers )
        {
            if ( provider.getTemplateClass().equals( clazz ) )
            {
                selectedProviders.add( (TemplateProvider<T>) provider );
            }
        }

        return new AggregatingTemplateProvider<T>( applicationConfiguration, clazz, selectedProviders );
    }

    public <T extends Template> T getTemplate( Class<T> clazz, String id )
        throws NoSuchTemplateIdException
    {
        return getTemplateProviderForTarget( clazz ).getTemplateById( id );
    }

}
