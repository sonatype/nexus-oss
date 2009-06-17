package org.sonatype.nexus.templates;

import java.util.ArrayList;
import java.util.List;

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

    public AggregatingTemplateProvider( Class<T> templateClass, List<TemplateProvider<T>> providers )
    {
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
