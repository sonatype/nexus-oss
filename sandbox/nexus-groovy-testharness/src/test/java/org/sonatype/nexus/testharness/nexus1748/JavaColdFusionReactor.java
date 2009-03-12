package org.sonatype.nexus.testharness.nexus1748;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ColdFusionReactor.class, hint = "java" )
public class JavaColdFusionReactor
    implements ColdFusionReactor
{

    public boolean givePower( int watts )
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
