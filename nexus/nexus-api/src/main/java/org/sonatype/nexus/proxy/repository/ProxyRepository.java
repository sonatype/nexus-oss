package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * A proxy repository is what it's name says :)
 * 
 * @author cstamas
 */
public interface ProxyRepository
    extends Repository
{
    /**
     * Gets remote status.
     */
    RemoteStatus getRemoteStatus( boolean forceCheck );

    /**
     * Gets proxy mode.
     * 
     * @return
     */
    ProxyMode getProxyMode();

    /**
     * Sets proxy mode.
     * 
     * @param val
     */
    void setProxyMode( ProxyMode val );

    /**
     * Gets the item max age in (in minutes).
     * 
     * @return the item max age in (in minutes)
     */
    int getItemMaxAge();

    /**
     * Sets the item max age in (in minutes).
     * 
     * @param itemMaxAgeInSeconds the new item max age in (in minutes).
     */
    void setItemMaxAge( int itemMaxAge );

    /**
     * Gets the RepositoryStatusCheckMode.
     * 
     * @return
     */
    RepositoryStatusCheckMode getRepositoryStatusCheckMode();

    /**
     * Sets the RepositoryStatusCheckMode.
     * 
     * @param mode
     */
    void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode );

    /**
     * Returns the remote URL of this repository, if any.
     * 
     * @return remote url of this repository, null otherwise.
     */
    String getRemoteUrl();

    /**
     * Sets the remote url.
     * 
     * @param url the new remote url
     */
    void setRemoteUrl( String url );

    /**
     * Returns repository specific remote connection context.
     * 
     * @return null if none
     */
    RemoteStorageContext getRemoteStorageContext();

    /**
     * Sets the repository specific remote connection context.
     * 
     * @param ctx
     */
    void setRemoteStorageContext( RemoteStorageContext ctx );

    /**
     * Returns the remoteStorage of the repository. Per repository instance may exists.
     * 
     * @return remoteStorage or null.
     */
    RemoteRepositoryStorage getRemoteStorage();

    /**
     * Sets the remote storage of the repository. May be null if this is a Local repository only. Per repository
     * instance may exists.
     * 
     * @param storage the storage
     */
    void setRemoteStorage( RemoteRepositoryStorage storage );
}
