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
package org.sonatype.nexus.plugins.migration.nexus1434;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.security.NexusViewSecurityResource;
import org.sonatype.nexus.security.RepositoryViewPrivilegeDescriptor;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

public class Nexus1434ImportSecurity130RepoKeyValidateIT
    extends AbstractImportSecurityIT
{

    @Override
    public void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-125.zip" ) );

        migrationSummary.setResolvePermission( true );

        commitMigration( migrationSummary );
    }

    @Override
    protected void verifySecurity()
        throws Exception
    {
        List<PrivilegeStatusResource> privilegeList = getImportedPrivilegeList();

        for ( PrivilegeStatusResource priv : privilegeList )
        {
            String repoId = getSecurityConfigUtil().getPrivilegeProperty(
                priv,
                TargetPrivilegeRepositoryPropertyDescriptor.ID );

            String groupId = getSecurityConfigUtil().getPrivilegeProperty(
                priv,
                TargetPrivilegeGroupPropertyDescriptor.ID );

            if ( priv.getType().equals( RepositoryViewPrivilegeDescriptor.TYPE ) )
            {
                // view permissions are created against a repository (because a group is a repository, anything that
                // says otherwise is legacy code
                assertRepoOrGroupIdExists( repoId );
            }
            else if ( !StringUtils.isEmpty( repoId ) )
            {
                assertRepoIdExists( repoId );
            }
            if ( !StringUtils.isEmpty( groupId ) )
            {
                assertRepoGroupIdExists( groupId );
            }
        }

    }

    private void assertRepoIdExists( String id )
        throws Exception
    {
        Assert.assertNotNull( repoUtil.getRepository( id ) );
    }

    private void assertRepoGroupIdExists( String id )
        throws Exception
    {
        Assert.assertNotNull( groupUtil.getGroup( id ) );
    }

    private void assertRepoOrGroupIdExists( String id )
        throws Exception
    {
        boolean found = false;
        List<String> repoIds = new ArrayList<String>();
        for ( RepositoryListResource repositoryListResource : repoUtil.getAllList() )
        {
            repoIds.add( repositoryListResource.getId() );

            if ( repositoryListResource.getId().equals( id ) )
            {
                found = true;
            }
        }

        if ( !found )
        {
            Assert.fail( "Failed to find repository or groupId in list: " + repoIds );
        }
    }

}
