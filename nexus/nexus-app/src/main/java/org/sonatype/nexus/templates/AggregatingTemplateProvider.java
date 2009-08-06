package org.sonatype.nexus.templates;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * An aggregating provider used to aggregate multiple TemplateProvider into one.
 * 
 * @author cstamas
 * @param <C>
 * @param <I>
 */
public class AggregatingTemplateProvider<T extends Template>
    extends AbstractTemplateProvider<T>
{
    private final Class<T> templateClass;

    private final List<TemplateProvider<T>> providers;

    public AggregatingTemplateProvider( ApplicationConfiguration applicationConfiguration, Class<T> templateClass, List<TemplateProvider<T>> providers )
    {
        setApplicationConfiguration( applicationConfiguration );
        
        this.templateClass = templateClass;

        this.providers = providers;
    }

    public Class<T> getTemplateClass()
    {
        return templateClass;
    }

    public List<T> getTemplates()
    {
        ArrayList<T> result = new ArrayList<T>();

        for ( TemplateProvider<T> provider : providers )
        {
            result.addAll( provider.getTemplates() );
        }

        return result;
    }
}
