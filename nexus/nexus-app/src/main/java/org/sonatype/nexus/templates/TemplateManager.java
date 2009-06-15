package org.sonatype.nexus.templates;

import java.util.List;

/**
 * Template manager aggregates various TemplateProviders, and adds means to select between them.
 * 
 * @author cstamas
 */
public interface TemplateManager
{
    /**
     * Get all templates known to manager,
     * 
     * @return
     */
    List<Template<?>> getTemplates();

    /**
     * Get templates that are able to create instances of suplied class.
     * 
     * @param <I>
     * @param clazz
     * @return
     */
    <I> TemplateProvider<I> getTemplateProviderForTarget( Class<I> clazz );

    /**
     * Get one specific template that is ablt to create instance of supplied class with given id.
     * 
     * @param <I>
     * @param clazz
     * @param id
     * @return
     * @throws NoSuchTemplateIdException
     */
    <I> Template<I> getTemplate( Class<I> clazz, String id )
        throws NoSuchTemplateIdException;
}
