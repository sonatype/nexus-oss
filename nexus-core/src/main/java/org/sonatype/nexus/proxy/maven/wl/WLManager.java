/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl;

import java.io.IOException;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

/**
 * WL Manager component.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface WLManager
{
    /**
     * Key that is put into {@link ResourceStoreRequest}'s context for prefix file related operations, to mark that the
     * file operation is initiated by WL feature. Only the presence (or no presence) of this key is used for flagging,
     * the value mapped under this key is irrelevant.
     */
    String WL_INITIATED_FILE_OPERATION_FLAG_KEY = WLManager.class.getName() + ".fileOperation";

    /**
     * Key that is put into {@link ResourceStoreRequest}'s context when {@link ProxyRequestFilter} rejects a request.
     * Only the presence (or no presence) of this key is used for flagging, the value mapped under this key is
     * irrelevant.
     */
    String WL_REQUEST_REJECTED_FLAG_KEY = WLManager.class.getName() + ".requestRejected";

    /**
     * Key that when put into {@link ResourceStoreRequest}'s context, the given request becomes a
     * "not a filtering subject". WL's {@link ProxyRequestFilter} will not interfere with that request, it will be not
     * subject for filtering. It should be used sparingly, only in cases when you know that WL might interfere with your
     * request, usually because of stale WL. Only the presence (or no presence) of this key is used for flagging, the
     * value mapped under this key is irrelevant.
     */
    String WL_REQUEST_NFS_FLAG_KEY = WLManager.class.getName() + ".requestNfs";

    /**
     * Startup. This method should not be invoked by any code (maybe except in UTs).
     */
    void startup();

    /**
     * Shutdown. This method should not be invoked by any code (maybe except in UTs).
     */
    void shutdown();

    /**
     * Initializes WL of given repository (used on repository addition and on boot when called with all defined
     * repository during boot up).
     * 
     * @param mavenRepository
     * @throws IOException
     */
    void initializeWhitelist( MavenRepository mavenRepository )
        throws IOException;

    /**
     * Executes an update of WL for given repository. In case of {@link MavenProxyRepository} instance, it might not do
     * anything, depending is configuration returned by {@link #getRemoteDiscoveryConfig(MavenProxyRepository)} for it
     * enabled or not. This method invocation will spawn the update in background, and return immediately.
     * 
     * @param mavenRepository
     * @return {@code true} if the update job was actually spawned, or {@code false} if not since one is already running
     *         for same repository. Still, will the spawned background job actually update or not depends on
     *         aforementioned configuration.
     */
    boolean updateWhitelist( MavenRepository mavenRepository );

    /**
     * Executes an update of WL for given repository. In case of {@link MavenProxyRepository} instance, it might not do
     * anything, depending is configuration returned by {@link #getRemoteDiscoveryConfig(MavenProxyRepository)} for it
     * enabled or not. This method invocation will always spawn the update in background, and return immediately. Also,
     * this method will cancel any currently running updates on same repository.
     * 
     * @param mavenRepository
     * @return {@code true} if another already running update was cancelled to execute this forced update.
     */
    boolean forceUpdateWhitelist( MavenRepository mavenRepository );

    /**
     * Special version of update of WL for given Maven2 proxy repository. This method will execute <b>synchronously</b>
     * and doing "quick" update only (will never scrape, only will try prefix file fetch from remote). Usable in special
     * cases when you know remote should have prefix file published, and you are interested in results immediately (or
     * at least ASAP). Still, consider that this method does remote access (using {@link RemoteRepositoryStorage} of the
     * given repository), hence, might have longer runtime (network latency, remote server load and such).
     * 
     * @param mavenProxyRepository
     */
    void forceProxyQuickUpdateWhitelist( MavenProxyRepository mavenProxyRepository );

    /**
     * Returns the WL status for given repository.
     * 
     * @param mavenRepository
     * @return the status, never {@code null}.
     */
    WLStatus getStatusFor( MavenRepository mavenRepository );

    /**
     * Returns the current (in effect) configuration of the remote discovery for given {@link MavenProxyRepository}
     * repository instance.
     * 
     * @param mavenProxyRepository
     * @return the configuration, never {@code null}.
     */
    WLDiscoveryConfig getRemoteDiscoveryConfig( MavenProxyRepository mavenProxyRepository );

    /**
     * Sets the current (in effect) configuration of the remote discovery for given {@link MavenProxyRepository}
     * repository instance.
     * 
     * @param mavenProxyRepository
     * @param config
     * @throws IOException
     */
    void setRemoteDiscoveryConfig( MavenProxyRepository mavenProxyRepository, WLDiscoveryConfig config )
        throws IOException;

    /**
     * Maintains the WL of a hosted repository. Offers entries to WL, and method updates the WL of given hosted
     * repository if needed. If WL modified, returns {@code true}.
     * 
     * @param mavenHostedRepository the hosted repository to which WL we offer entries.
     * @param entry the entry offered.
     * @return {@code true} if WL was changed, {@code false} otherwise.
     * @throws IOException in case of some IO problem.
     */
    boolean offerWLEntry( final MavenHostedRepository mavenHostedRepository, String entry )
        throws IOException;

    /**
     * Maintains the WL of a hosted repository. Revokes entries from WL, and method updates the WL of given hosted
     * repository if needed. If WL modified, returns {@code true}.
     * 
     * @param mavenHostedRepository the hosted repository from which WL we revoke entries.
     * @param entry the entry revoked.
     * @return {@code true} if WL was changed, {@code false} otherwise.
     * @throws IOException in case of some IO problem.
     */
    boolean revokeWLEntry( final MavenHostedRepository mavenHostedRepository, String entry )
        throws IOException;

    /**
     * Returns {@link PrefixSource} for given {@link MavenRepository}.For the existence of the WL in question (if you
     * want to read it), check {@link PrefixSource#exists()} method! Never returns {@code null}.
     * 
     * @param mavenRepository
     * @return the {@link PrefixSource} for given repository.
     */
    PrefixSource getPrefixSourceFor( MavenRepository mavenRepository );

    /**
     * Publishes the passed in {@link PrefixSource} into the given {@link MavenRepository}.
     * 
     * @param mavenRepository
     * @param prefixSource
     * @throws IOException
     */
    void publish( MavenRepository mavenRepository, PrefixSource prefixSource )
        throws IOException;

    /**
     * Unpublishes (removes if published before) the entries, and marks {@link MavenRepository} as "noscrape".
     * 
     * @param mavenRepository
     * @throws IOException
     */
    void unpublish( MavenRepository mavenRepository )
        throws IOException;

    // ==

    /**
     * Checks whether the passed in item event is about WL file. In other words, is event originating from a
     * {@link MavenRepository} and has specific path.
     * 
     * @param evt
     * @return {@code true} if item event is about WL file.
     */
    boolean isEventAboutWLFile( final RepositoryItemEvent evt );
}
