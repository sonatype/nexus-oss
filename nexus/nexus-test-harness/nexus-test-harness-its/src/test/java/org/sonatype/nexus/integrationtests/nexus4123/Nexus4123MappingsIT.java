package org.sonatype.nexus.integrationtests.nexus4123;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;
import org.testng.annotations.Test;

public class Nexus4123MappingsIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void getRoutes()
        throws Exception
    {
        RoutesMessageUtil.getList();
    }
}
