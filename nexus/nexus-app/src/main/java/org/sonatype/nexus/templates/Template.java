package org.sonatype.nexus.templates;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;

/**
 * A template for creation of various objects.
 * 
 * @author cstamas
 * @param <C>
 * @param <I>
 */
public interface Template<I>
{
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
     * The implementation that this template will create.
     * 
     * @return
     */
    Class<I> getTargetClass();

    /**
     * Instantianates this template.
     * 
     * @return
     * @throws ConfigurationException
     * @throws IOException
     */
    I create()
        throws ConfigurationException, IOException;
}
