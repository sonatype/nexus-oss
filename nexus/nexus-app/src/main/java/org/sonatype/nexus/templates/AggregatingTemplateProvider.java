package org.sonatype.nexus.templates;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;

/**
 * An aggregating provider used to aggregate multiple TemplateProvider into one.
 * 
 * @author cstamas
 * @param <C>
 * @param <I>
 */
public class AggregatingTemplateProvider<I>
    implements TemplateProvider<I>
{
    private final Class<I> implementationClass;

    private final List<TemplateProvider<I>> providers;

    public AggregatingTemplateProvider( Class<I> implementationClass, List<TemplateProvider<I>> providers )
    {
        this.implementationClass = implementationClass;

        this.providers = providers;
    }

    public Class<I> getImplementationClass()
    {
        return implementationClass;
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

        throw new NoSuchTemplateIdException( "Template for implementationClass='" + getImplementationClass().getName()
            + "' with Id='" + id + "' not found!" );
    }

}
