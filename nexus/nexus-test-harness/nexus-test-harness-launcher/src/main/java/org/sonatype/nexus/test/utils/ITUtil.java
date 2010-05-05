package org.sonatype.nexus.test.utils;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

/**
 * Very simple superclass.
 * 
 * @author cstamas
 */
public class ITUtil
{
    protected final AbstractNexusIntegrationTest test;
    
    public ITUtil( AbstractNexusIntegrationTest test )
    {
        super();
        this.test = test;
    }

    public AbstractNexusIntegrationTest getTest()
    {
        return test;
    }
}
