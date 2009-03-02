/**
 * 
 */
package org.sonatype.nexus

import org.testng.annotations.Test
import static org.testng.Assert.*
import org.sonatype.nexus.groovytest.NexusCompatibility

/**
 * @author velo
 *
 */
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
