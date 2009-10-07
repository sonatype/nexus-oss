package org.sonatype.nexus.templates;

/**
 * Template manager aggregates various TemplateProviders, and adds means to select between them.
 * 
 * @author cstamas
 */
public interface TemplateManager
{
    /**
     * Get templates.
     * 
     * @param <I>
     * @param clazz
     * @return
     */
    TemplateSet getTemplates();

    /**
     * Get one specific template that fits of supplied class with given id.
     * 
     * @param <I>
     * @param clazz
     * @param id
     * @return
     * @throws NoSuchTemplateIdException
     */
    Template getTemplate( Object clazz, String id )
        throws NoSuchTemplateIdException;
}
