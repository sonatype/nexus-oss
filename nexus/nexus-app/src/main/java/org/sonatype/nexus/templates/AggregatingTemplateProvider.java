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
public class AggregatingTemplateProvider<I>
    extends AbstractTemplateProvider<I>
{
    private final Class<I> targetClass;

    private final List<TemplateProvider<I>> providers;

    public AggregatingTemplateProvider( Class<I> targetClass, List<TemplateProvider<I>> providers )
    {
        this.targetClass = targetClass;

        this.providers = providers;
    }

    public Class<I> getTargetClass()
    {
        return targetClass;
    }

    public List<Template<I>> getTemplates()
    {
        ArrayList<Template<I>> result = new ArrayList<Template<I>>();

        for ( TemplateProvider<I> provider : providers )
        {
            result.addAll( provider.getTemplates() );
        }

        return result;
    }
}
