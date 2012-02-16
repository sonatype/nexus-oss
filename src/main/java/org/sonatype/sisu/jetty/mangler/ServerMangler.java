package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;

/**
 * Provides means to "mangle" the Jetty instance used by Jetty7 class.
 * 
 * @author cstamas
 * @param <T>
 */
public interface ServerMangler<T>
{
    T mangle( final Server server );
}
