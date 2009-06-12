package org.sonatype.nexus.templates;

import java.util.List;

import org.sonatype.plexus.plugin.ExtensionPoint;

/**
 * A template provider provides a set of templates for one implementation.
 * 
 * @author cstamas
 * @param <C>
 * @param <I>
 */
@ExtensionPoint
public interface TemplateProvider<I>
{
    /**
     * Returns the implementation class for which this provider provides templates.
     * 
     * @return
     */
    Class<I> getImplementationClass();

    /**
     * Lists the templates.
     * 
     * @return
     */
    List<Template<I>> getTemplates();

    /**
     * Search a template by it's ID.
     * 
     * @param id
     * @return
     * @throws NoSuchTemplateIdException
     */
    Template<I> getTemplateById( String id )
        throws NoSuchTemplateIdException;
}
