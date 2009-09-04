package org.sonatype.nexus.templates;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;

/**
 * A template for creation of various objects.
 * 
 * @author cstamas
 */
public interface Template
{
    /**
     * Returns the originating template provider for this template.
     * 
     * @return
     */
    TemplateProvider getTemplateProvider();

    /**
     * The ID of this template.
     * 
     * @return
     */
    String getId();

    /**
     * The human description of this template.
     * 
     * @return
     */
    String getDescription();

    /**
     * Returns true if the supplied object does "fit" the target that this template creates (a la
     * class.isAssignableFrom(target)). The actual meaning of "fit" is left to given template and it's implementation,
     * how to "narrow" the selection.
     * 
     * @param target
     * @return
     */
    boolean targetFits( Object target );

    /**
     * Instantianates this template, creates resulting object (needs cast).
     * 
     * @return
     * @throws ConfigurationException
     * @throws IOException
     */
    Object create()
        throws ConfigurationException, IOException;
}
