package org.sonatype.nexus.webapp;

import com.google.inject.AbstractModule;

import javax.inject.Named;

/**
 * Nexus webapp module.
 *
 * @since 2.5
 */
@Named
public class WebappModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        install(new MetricsModule());
    }
}
