package org.sonatype.nexus.testharness.nexus1748

import org.testng.*
import org.testng.annotations.*
import org.codehaus.plexus.component.annotations.*

@Component( role = ColdFusionReactor.class, hint = "groovy" )
public class GroovyColdFusionReactor implements ColdFusionReactor {

    boolean givePower( int watts )
    {
        if ( watts <= 0 )
        {
            throw new IllegalArgumentException();
        }

        if ( watts < Short.MAX_VALUE )
        {
            return true;
        }

        return false;
    }
	
}