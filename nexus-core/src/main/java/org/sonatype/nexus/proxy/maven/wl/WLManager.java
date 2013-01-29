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

import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;

/**
 * WL Manager component.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface WLManager
{
    /**
     * Initializes all WLs (used at boot).
     */
    void initializeAllWhitelists();

    /**
     * Initializes WL of given repository (used on repo add).
     * 
     * @param mavenRepositories
     * @throws IOException
     */
    void initializeWhitelist( MavenRepository... mavenRepositories )
        throws IOException;

    /**
     * Executes an update of WL for given repositories. In case of {@link MavenProxyRepository} instance, it might not
     * do anything, depends is configuration returned by {@link #getRemoteDiscoveryConfig(MavenProxyRepository)} for it
     * enabled or not. This method invocation will spawn the updates in background, and return immediately.
     * 
     * @param mavenRepositories
     */
    void updateWhitelist( MavenRepository... mavenRepositories );

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
     * @param entries the entries offered.
     * @return {@code true} if WL was changed, {@code false} otherwise.
     * @throws IOException in case of some IO problem.
     */
    boolean offerWLEntries( final MavenHostedRepository mavenHostedRepository, String... entries )
        throws IOException;

    /**
     * Maintains the WL of a hosted repository. Revokes entries from WL, and method updates the WL of given hosted
     * repository if needed. If WL modified, returns {@code true}.
     * 
     * @param mavenHostedRepository the hosted repository from which WL we revoke entries.
     * @param entries the entries revoked.
     * @return {@code true} if WL was changed, {@code false} otherwise.
     * @throws IOException in case of some IO problem.
     */
    boolean revokeWLEntries( final MavenHostedRepository mavenHostedRepository, String... entries )
        throws IOException;

    /**
     * Returns {@link EntrySource} for given {@link MavenRepository}.For the existence of the WL in question (if you
     * want to read it), check {@link EntrySource#exists()} method! Never returns {@code null}.
     * 
     * @param mavenRepository
     * @return the {@link EntrySource} for given repository.
     */
    EntrySource getEntrySourceFor( MavenRepository mavenRepository );

    /**
     * Publishes the passed in {@link EntrySource} into the given {@link MavenRepository}.
     * 
     * @param mavenRepository
     * @param entrySource
     * @throws IOException
     */
    void publish( MavenRepository mavenRepository, EntrySource entrySource )
        throws IOException;

    /**
     * Republishes the same {@link EntrySource} that already exists in repository.
     * 
     * @param mavenRepository
     * @throws IOException
     */
    void republish( MavenRepository mavenRepository )
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
     * Marks the request context, that will propagate thru events. All events marked as this will emit events with
     * having the mark in the context.
     * 
     * @param ctx
     */
    void markRequestContext( RequestContext ctx );

    /**
     * Checks whether the passed in context is marked.
     * 
     * @param ctx
     * @return {@code true} if the item event is caused by WL subsystem.
     */
    boolean isRequestContextMarked( RequestContext ctx );

    /**
     * Checks whether the passed in item event is about WL file. In other words, is event originating from a
     * {@link MavenRepository} and has specific path.
     * 
     * @param evt
     * @return {@code true} if item event is about WL file.
     */
    boolean isEventAboutWLFile( final RepositoryItemEvent evt );
}
