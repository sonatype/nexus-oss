/**
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
package org.sonatype.nexus.repository.yum.internal.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.plugin.Managed;

@Component( role = Repository.class, hint = M2YumRepository.ID, instantiationStrategy = "per-lookup",
            description = "Maven2-Yum Repository" )
@Managed
@SuppressWarnings( "deprecation" )
public class M2YumRepository
    extends M2Repository
{

    public static final String ID = "maven2yum";

    @Override
    public boolean isMavenMetadataPath( String path )
    {
        return super.isMavenMetadataPath( path ) || isYumRepoPath( path );
    }

    @Override
    public void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        if ( isYumRepoPath( item.getPath() ) )
        {
            // Ignore items within repodata folder.
            return;
        }

        super.storeItem( fromTask, item );
    }

    private boolean isYumRepoPath( final String path )
    {
        return path.startsWith( "/repodata/" );
    }

}
