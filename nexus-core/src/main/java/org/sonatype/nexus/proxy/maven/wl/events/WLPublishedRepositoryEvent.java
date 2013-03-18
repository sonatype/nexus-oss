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
package org.sonatype.nexus.proxy.maven.wl.events;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.PrefixSource;
import org.sonatype.nexus.proxy.maven.wl.WLManager;

/**
 * Event fired when a {@link MavenRepository} publishes it's WL. The WL is carried along with this event in form of
 * {@link PrefixSource} but you can also use {@link WLManager} to get repository entry source, as it is already available
 * in the moment you process this event.
 * 
 * @author cstamas
 * @since 2.4
 */
public class WLPublishedRepositoryEvent
    extends AbstractWLRepositoryEvent
{
    private final PrefixSource prefixSource;

    /**
     * Constructor.
     * 
     * @param mavenRepository the repository published it's WL.
     * @param prefixSource the WL in form of {@link PrefixSource}.
     */
    public WLPublishedRepositoryEvent( final MavenRepository mavenRepository, final PrefixSource prefixSource )
    {
        super( mavenRepository );
        this.prefixSource = checkNotNull( prefixSource );
    }

    /**
     * The {@link PrefixSource} that gives access to published entries.
     * 
     * @return the entry source.
     */
    public PrefixSource getPrefixSource()
    {
        return prefixSource;
    }
}
