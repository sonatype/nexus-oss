package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

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
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "group_g4" );
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, repo );
        TaskScheduleUtil.waitForAllTasksToStop();
        Assert.assertNotNull( "The ScheduledServicePropertyResource task didn't run", task );

        DirectoryScanner scan = new DirectoryScanner();
        scan.setBasedir( new File( nexusWorkDir, "storage" ) );
        scan.addDefaultExcludes();
        scan.scan();
        String[] storageContent = scan.getIncludedFiles();

        scan = new DirectoryScanner();
        scan.setBasedir( new File( nexusWorkDir, "proxy/attributes" ) );
        scan.addDefaultExcludes();
        scan.scan();
        String[] attributesContent = scan.getIncludedFiles();

        Assert.assertArrayEquals( storageContent, attributesContent );
    }

}
