package org.sonatype.nexus.templates;

import java.util.List;

import org.sonatype.plexus.plugin.ExtensionPoint;

/**
 * A template provider provides a set of templates for one implementation.
 * 
 * @author cstamas
 */
@ExtensionPoint
public interface TemplateProvider<T extends Template>
{
    /**
     * Returns the template class for which this provider provides templates.
     * 
     * @return
     */
    Class<T> getTemplateClass();

    /**
     * Lists the templates.
     * 
     * @return
     */
    List<T> getTemplates();

    /**
     * Search a template by it's ID.
     * 
     * @param id
     * @return
     * @throws NoSuchTemplateIdException
     */
    T getTemplateById( String id )
        throws NoSuchTemplateIdException;
}
