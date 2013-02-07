package org.sonatype.nexus.client.core.subsystem.whitelist;

import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;

/**
 * Client subsystem for Nexus Whitelist feature.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface Whitelist
{
    /**
     * Returns the status of the Whitelist of a Maven Repository (proxy, hosted or group).
     * 
     * @param mavenRepositoryId the ID of the Maven repository you want get status for.
     * @return the status for given repository.
     * @throws NexusClientNotFoundException if the passed in ID does not exists.
     */
    Status getWhitelistStatus( String mavenRepositoryId );

    /**
     * Perform a forced update of the Whitelist of a Maven repository. This method returns immediately, but it spawns a
     * background operation on Nexus that will perform the update and it's outcome will be reflected in status when
     * update is done.
     * 
     * @param mavenRepositoryId the ID of the Maven Repository you want update the whitelist for (see throws for what
     *            kind of repositories this call is allowed).
     * @throws NexusClientErrorResponseException if the passed in ID is not a Maven Proxy repository.
     * @throws NexusClientNotFoundException if the passed in ID does not exists.
     */
    void updateWhitelist( String mavenRepositoryId )
        throws NexusClientErrorResponseException, NexusClientNotFoundException;

    /**
     * Returns the {@link DiscoveryConfiguration} for given Maven Proxy repository.
     * 
     * @param mavenProxyRepositoryId the ID of the Maven Proxy repository you want configuration for.
     * @return the configuration entity.
     * @throws NexusClientErrorResponseException if the passed in ID is not a Maven Proxy repository.
     * @throws NexusClientNotFoundException if the passed in ID does not exists.
     */
    DiscoveryConfiguration getDiscoveryConfigurationFor( String mavenProxyRepositoryId )
        throws NexusClientErrorResponseException, NexusClientNotFoundException;

    /**
     * Sets the {@link DiscoveryConfiguration} for given Maven Proxy repository.
     * 
     * @param mavenProxyRepositoryId the ID of the Maven Proxy repository you want configuration for.
     * @param configuration the configuration.
     * @throws NexusClientErrorResponseException if the passed in ID is not a Maven Proxy repository, or, the
     *             configuration is invalid (interval zero or negative).
     * @throws NexusClientNotFoundException if the passed in ID does not exists.
     */
    void setDiscoveryConfigurationFor( String mavenProxyRepositoryId, DiscoveryConfiguration configuration )
        throws NexusClientErrorResponseException, NexusClientNotFoundException;

}
