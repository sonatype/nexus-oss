package org.sonatype.nexus.bundle.launcher;

import org.sonatype.nexus.bundle.NexusBundleConfiguration;

/**
 * A service interface which launches nexus bundle instances.
 *
 * @author plynch
 */
public interface NexusBundleLauncher {

    /**
     * Start a bundle configured as per the specified config, returning a managed representation.
     * <p>
     * The bundle is grouped into the global group.
     *
     * @param config the bundle configuration to use when configuring the bundle before launch.
     * @return
     */
    ManagedNexusBundle start(NexusBundleConfiguration config);

    /**
     * Start a bundle
     * @param config
     * @param groupName
     * @return a {@link ManagedNexusBundle} providing details about the started bundle.
     */
    ManagedNexusBundle start(NexusBundleConfiguration config, String groupName);

    /**
     * Stop the specified bundle.
     * <p>
     * This operation is synchronous.
     *
     * @param managedNexusbundle
     */
    void stop(ManagedNexusBundle managedNexusbundle);

    /**
     * Stop all bundles in the specified group.
     *
     * @param groupName
     */
    void stopAll(String groupName);

    /**
     * Stop all bundles registered with this service.
     */
    void stopAll();

}
