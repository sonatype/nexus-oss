package org.sonatype.nexus.util;

import org.codehaus.plexus.PlexusContainer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a ClassLoader which exposes all classes in Nexus core and in installed plugins.
 *
 * @since 2.6
 */
@Named("nexus-uber")
@Singleton
public class NexusUberClassLoaderProvider
    implements Provider<ClassLoader>
{
    private final WholeWorldClassloader classloader;

    // FIXME: ATM we have to use plexus-specific API here to access the ClassWorld

    @Inject
    public NexusUberClassLoaderProvider(final PlexusContainer container) {
        checkNotNull(container);
        this.classloader = new WholeWorldClassloader(container.getContainerRealm().getWorld());
    }

    @Override
    public ClassLoader get() {
        return classloader;
    }
}
