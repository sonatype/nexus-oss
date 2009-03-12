package org.sonatype.nexus.testharness.nexus1749

import org.testng.annotations.Test
import static org.testng.Assert.*
import org.sonatype.nexus.groovytest.NexusCompatibility
import org.codehaus.plexus.component.annotations.Component;

@Component(role = SkipTest.class)
public class SkipTest{

    @Test
    @NexusCompatibility (minVersion = "1.5")
    void skipMin() 
    {
       fail "should not run!"
    }

    @Test
    @NexusCompatibility (maxVersion = "1.2")
    void skipMax() 
    {
       fail "should not run!"
    }

    @Test
    @NexusCompatibility (minVersion = "1.1", maxVersion = "1.2")
    void skipRange() 
    {
       fail "should not run!"
    }
    
}
