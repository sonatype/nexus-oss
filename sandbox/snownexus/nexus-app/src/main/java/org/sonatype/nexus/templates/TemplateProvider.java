package org.sonatype.nexus.templates;

import javax.inject.Singleton;

import org.sonatype.plugin.ExtensionPoint;

/**
 * A template provider provides a set of templates for one implementation.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface TemplateProvider
{
    /**
     * Lists all templates.
     * 
     * @return
     */
    TemplateSet getTemplates();

    /**
     * Lists all templates that fits supplied filter.
     * 
     * @return
     */
    TemplateSet getTemplates( Object filter );

    /**
     * Lists all templates that fits supplied filters.
     * @param clazz
     * @return
     */
    TemplateSet getTemplates( Object... filters );

    /**
     * Search a template by it's ID.
     * 
     * @param id
     * @return
     * @throws NoSuchTemplateIdException
     */
    Template getTemplateById( String id )
        throws NoSuchTemplateIdException;
}
