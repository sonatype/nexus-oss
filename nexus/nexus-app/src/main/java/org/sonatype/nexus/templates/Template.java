package org.sonatype.nexus.templates;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;

/**
 * A template for creation of various objects.
 * 
 * @author cstamas
 */
public interface Template
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
     * Instantianates this template.
     * 
     * @return
     * @throws ConfigurationException
     * @throws IOException
     */
    Object create()
        throws ConfigurationException, IOException;
}
