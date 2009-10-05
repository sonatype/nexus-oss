package org.sonatype.nexus.integrationtests.nexus2797;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus2797Validate140TaskUpgrade
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validateTaskHasNextRunDate()
        throws Exception
    {
        List<ScheduledServiceListResource> tasks = TaskScheduleUtil.getTasks();
        
        Assert.assertEquals( 3, tasks.size() );
        
        //problem was simply that next run time was invalidly calculated, and never set
        //we simply want to make sure it is set
        Assert.assertNotSame( "n/a", tasks.get( 0 ).getNextRunTime() );
        Assert.assertNotSame( "n/a", tasks.get( 1 ).getNextRunTime() );
        Assert.assertNotSame( "n/a", tasks.get( 2 ).getNextRunTime() );
    }
}
