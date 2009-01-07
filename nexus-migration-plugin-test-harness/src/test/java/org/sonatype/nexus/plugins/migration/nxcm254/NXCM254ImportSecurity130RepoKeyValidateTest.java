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
package org.sonatype.nexus.plugins.migration.nxcm254;

import hidden.org.codehaus.plexus.util.StringUtils;

import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;

public class NXCM254ImportSecurity130RepoKeyValidateTest
    extends AbstractImportSecurityTest
{
    public NXCM254ImportSecurity130RepoKeyValidateTest()
    {
        super();
    }

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
        List<PrivilegeBaseStatusResource> privilegeList = getImportedPrivilegeList();

        for ( PrivilegeBaseStatusResource priv : privilegeList )
        {
            PrivilegeTargetStatusResource targetPriv = (PrivilegeTargetStatusResource) priv;

            String repoId = targetPriv.getRepositoryId();

            String groupId = targetPriv.getRepositoryGroupId();

            if ( !StringUtils.isEmpty( repoId ) )
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

}
