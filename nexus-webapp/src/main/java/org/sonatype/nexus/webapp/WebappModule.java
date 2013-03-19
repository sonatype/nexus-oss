package org.sonatype.nexus.webapp;

import com.google.inject.AbstractModule;

import javax.inject.Named;

/**
 * ???
 *
 * @since 2.4
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
