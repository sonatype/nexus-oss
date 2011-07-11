/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2bridge.internal.guice;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.p2bridge.internal.ArtifactRepositoryProvider;
import org.sonatype.nexus.plugins.p2bridge.internal.MetadataRepositoryProvider;
import org.sonatype.nexus.plugins.p2bridge.internal.PublisherProvider;
import org.sonatype.p2.bridge.ArtifactRepository;
import org.sonatype.p2.bridge.MetadataRepository;
import org.sonatype.p2.bridge.Publisher;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

@Named
@Singleton
public class GuiceModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( ArtifactRepository.class ).toProvider( ArtifactRepositoryProvider.class ).in( Scopes.SINGLETON );
        bind( MetadataRepository.class ).toProvider( MetadataRepositoryProvider.class ).in( Scopes.SINGLETON );
        bind( Publisher.class ).toProvider( PublisherProvider.class ).in( Scopes.SINGLETON );
    }
}