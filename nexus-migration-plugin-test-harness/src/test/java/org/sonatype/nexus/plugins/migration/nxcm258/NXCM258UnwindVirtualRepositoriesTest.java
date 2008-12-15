package org.sonatype.nexus.plugins.migration.nxcm258;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;

public class NXCM258UnwindVirtualRepositoriesTest
    extends AbstractMigrationIntegrationTest
{

    private GroupMessageUtil groupUtil;

    public NXCM258UnwindVirtualRepositoriesTest()
    {
        this.groupUtil = new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void importWindVirtualRepos()
        throws Exception
    {
        int code = ImportMessageUtil.importBackup( getTestFile( "windedBackup.zip" ) );
        Assert.assertTrue( "Unexpected result from server: " + code, Status.isSuccess( code ) );

        RepositoryGroupResource group = this.groupUtil.getGroup( "libs-snapshots" );
        Assert.assertNotNull( group );
        Assert.assertEquals( "libs-snapshots", group.getId() );

        ArrayList<RepositoryGroupMemberRepository> repositories =
            (ArrayList<RepositoryGroupMemberRepository>) group.getRepositories();
        Assert.assertEquals( 5, repositories.size() );

        ArrayList<String> reposIds = new ArrayList<String>();
        for ( RepositoryGroupMemberRepository repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, "libs-snapshots-local" );
        assertContains( reposIds, "ext-snapshots-local" );
        assertContains( reposIds, "java.net.m2" );
        assertContains( reposIds, "java.net.m1" );
        assertContains( reposIds, "repo1" );

    }
}
