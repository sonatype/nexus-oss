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
package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.yum.repository.RepositoryUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.repository.yum.YumRegistry;

@Component( role = GroupRepository.class, hint = M2YumGroupRepository.ID, instantiationStrategy = "per-lookup", description = "Maven2-Yum Repository Group" )
public class M2YumGroupRepository
    extends M2GroupRepository
{
    public static final String ID = "maven2yum";

    @Requirement( hint = M2YumContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private YumRegistry yumRegistry;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    private RepositoryKind repositoryKind;

    @Override
    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    private boolean skipTaskGeneration = false;

    @Override
    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind =
                new DefaultRepositoryKind( GroupRepository.class, asList( new Class<?>[] { MavenGroupRepository.class,
                    M2YumGroupRepository.class } ) );
        }
        return repositoryKind;
    }

    @Override
    public void addMemberRepositoryId( String repositoryId )
        throws NoSuchRepositoryException, InvalidGroupingException
    {
        super.addMemberRepositoryId( repositoryId );
        if ( !skipTaskGeneration && isRpmRepo( repositoryId ) )
        {
            yumRegistry.createGroupRepository( this );
        }
    }

    private boolean isRpmRepo( String repositoryId )
        throws NoSuchRepositoryException
    {
        final Repository repository = repositoryRegistry.getRepository( repositoryId );
        try
        {
            if ( repository.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class )
                && new File( RepositoryUtils.getBaseDir( repository ), "repodata/repomd.xml" ).exists() )
            {
                return true;
            }
        }
        catch ( Exception e )
        {
        }
        return false;
    }

    @Override
    public void removeMemberRepositoryId( String repositoryId )
    {
        super.removeMemberRepositoryId( repositoryId );
        try
        {
            if ( isRpmRepo( repositoryId ) )
            {
                yumRegistry.createGroupRepository( this );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new RuntimeException( "Could not detect rpm repository.", e );
        }
    }

    @Override
    public void setMemberRepositoryIds( List<String> repositories )
        throws NoSuchRepositoryException, InvalidGroupingException
    {
        try
        {
            skipTaskGeneration = true;
            super.setMemberRepositoryIds( repositories );
        }
        finally
        {
            skipTaskGeneration = false;
        }
        yumRegistry.createGroupRepository( this );
    }

}
