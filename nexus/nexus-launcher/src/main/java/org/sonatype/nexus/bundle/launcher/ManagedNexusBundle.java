package org.sonatype.nexus.bundle.launcher;

import java.io.File;

/**
 * A Nexus bundle that is being managed by a {@link NexusBundleService}
 * <p>
 * We only try to expose the minimum details needed to interact with a bundle as a client.
 *
 * @author plynch
 */
public interface ManagedNexusBundle {

    /**
     * @return The unique id among all managed Nexus bundles.
     */
    String getId();

    /**
     * @return The Artifact coordinates used to resolve the original bundle file.
     */
    String getArtifactCordinates();

    /**
     *
     * @param portType the portType to get
     * @return the port value for the specified port type. -1 if not port value assigned for the type.
     */
    int getPort(NexusPort portType);

    /**
     *
     * @return the http port assigned to this bundle
     */
    int getHttpPort();

    /**
     *
     * @return The host this bundle is configured to run on, usually 'localhost'.
     */
    String getHost();

    /**
     * The context path at which the bundle is configured.
     * <p>
     * In keeping with {@link javax.servlet.ServletContext#getContextPath()}, if the bundle is configured at the root context, then this value will be "".
     * @return The context path at which the bundle is configured - usually /nexus. It is guaranteed to not be null and either equal to "" or a string starting with forward slash {@code /}
     */
    String getContextPath();

    /**
     *
     * @return the work directory configured for this bundle
     */
    File getNexusWorkDirectory();

    /**
     *
     * @return the runtime directory configured for this bundle
     */
    File getNexusRuntimeDirectory();

}
