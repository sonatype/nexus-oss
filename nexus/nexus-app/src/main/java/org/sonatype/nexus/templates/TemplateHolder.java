package org.sonatype.nexus.templates;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;

/**
 * Template holder is kinda locator pattern. It's duty is to encapsulate and hide the actual creation logic happening
 * behind the scenes.
 * 
 * @author cstamas
 * @param <C>
 * @param <I>
 */
public interface TemplateHolder<I>
{
    Class<I> getImplementationClass();

    Object getConfiguration();

    I create()
        throws ConfigurationException, IOException;
}
