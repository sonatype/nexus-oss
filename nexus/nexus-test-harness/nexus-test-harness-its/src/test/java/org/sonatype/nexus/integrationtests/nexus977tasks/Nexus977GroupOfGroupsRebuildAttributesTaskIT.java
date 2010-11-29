package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsRebuildAttributesTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void rebuild()
        throws Exception
    {
        // add some extra artifacts
        File dest = new File( nexusWorkDir, "storage/r1/nexus977tasks/project/1.0/project-1.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        dest = new File( nexusWorkDir, "storage/r2/nexus977tasks/project/2.0/project-2.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        dest = new File( nexusWorkDir, "storage/r3/nexus977tasks/project/3.0/project-3.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, repo );

        DirectoryScanner scan = new DirectoryScanner();
        scan.setBasedir( new File( nexusWorkDir, "storage" ) );
        scan.addDefaultExcludes();
        scan.setExcludes( new String[] { "**/.nexus/attributes/" } );
        scan.scan();
        String[] storageContent = scan.getIncludedFiles();

        scan = new DirectoryScanner();
        scan.setBasedir( new File( nexusWorkDir, "storage" ) );
        scan.addDefaultExcludes();
        scan.setIncludes( new String[] { "**/.nexus/attributes/" } );
        scan.scan();
        String[] attributesContent = scan.getIncludedFiles();

        // the paths will differ, but length should be equal
        Assert.assertEquals( attributesContent.length, storageContent.length );
    }

}
