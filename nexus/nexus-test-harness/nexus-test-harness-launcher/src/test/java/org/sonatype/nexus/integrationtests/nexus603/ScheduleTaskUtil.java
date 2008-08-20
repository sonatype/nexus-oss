package org.sonatype.nexus.integrationtests.proxy.nexus603;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.apache.maven.it.util.FileUtils;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.nexus533.TaskScheduleUtil;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

/**
 * @author marvin
 *@see http://issues.sonatype.org/browse/NEXUS-599
 */
public class ScheduleTaskUtil
{

    public static File resolve( String repository, Gav gav, File parentDir )
        throws IOException
    {
        // r=<repoId> -- mandatory
        // g=<groupId> -- mandatory
        // a=<artifactId> -- mandatory
        // v=<version> -- mandatory
        // c=<classifier> -- optional
        // p=<packaging> -- optional, jar is taken as default
        //http://localhost:8087/nexus/service/local/artifact/maven/redirect?r=tasks-snapshot-repo&g=nexus603&a=artifact&
        // v=1.0-SNAPSHOT
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + repository + "&g=" + gav.getGroupId() + "&a="
                + gav.getArtifactId() + "&v=" + gav.getVersion();
        Response response = RequestFacade.doGetRequest( serviceURI );
        serviceURI = response.getRedirectRef().toString();

        File file = FileUtils.createTempFile( gav.getArtifactId(), '.' + gav.getExtension(), parentDir );
        RequestFacade.downloadFile( new URL( serviceURI ), file.getAbsolutePath() );

        return file;
    }

    public static ScheduledServiceBaseResource runTask( String typeId , ScheduledServicePropertyResource ...properties )
        throws Exception
    {
        ScheduledServiceBaseResource scheduledTask = new ScheduledServiceBaseResource();
        scheduledTask.setEnabled( true );
        scheduledTask.setId( null );
        scheduledTask.setName( typeId.substring( 0, typeId.lastIndexOf( '.' ) ) );
        scheduledTask.setTypeId( typeId );

        for ( ScheduledServicePropertyResource property : properties )
        {
            scheduledTask.addProperty( property );
        }

        TaskScheduleUtil.create( scheduledTask );
        String taskId = TaskScheduleUtil.getTask( scheduledTask.getName() ).getId();
        Status status = TaskScheduleUtil.run( taskId );
        Assert.assertTrue( "Unable to run task:" + scheduledTask.getTypeId(), status.isSuccess() );

        // I don't like to rely on this
        Thread.sleep( 1000 );

        return scheduledTask;
    }
}
