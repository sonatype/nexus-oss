/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
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
