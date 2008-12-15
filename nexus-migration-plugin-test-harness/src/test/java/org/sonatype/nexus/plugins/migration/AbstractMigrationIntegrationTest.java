package org.sonatype.nexus.plugins.migration;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class AbstractMigrationIntegrationTest
    extends AbstractNexusIntegrationTest
{
    @BeforeClass
    public static void clean()
        throws IOException
    {
        cleanWorkDir();
    }

    protected <E> void assertContains( ArrayList<E> collection, E item )
    {
        Assert.assertTrue( item + " not found.\n" + collection, collection.contains( item ) );
    }

}
