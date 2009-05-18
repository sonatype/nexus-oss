package org.sonatype.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.sonatype.security.usermanagement.User;

public class OrderingRealmsTest
    extends AbstractSecurityTest
{

    public void testOrderedGetUser()
        throws Exception
    {

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        List<String> realmHints = new ArrayList<String>();
        realmHints.add( "MockRealmA" );
        realmHints.add( "MockRealmB" );
        securitySystem.setRealms( realmHints );

        User jcoder = securitySystem.getUser( "jcoder" );
        Assert.assertNotNull( jcoder );

        // make sure jcoder is from MockUserManagerA
        Assert.assertEquals( "MockUserManagerA", jcoder.getSource() );

        // now change the order
        realmHints.clear();
        realmHints.add( "MockRealmB" );
        realmHints.add( "MockRealmA" );
        securitySystem.setRealms( realmHints );
        
        jcoder = securitySystem.getUser( "jcoder" );
        Assert.assertNotNull( jcoder );

        // make sure jcoder is from MockUserManagerA
        Assert.assertEquals( "MockUserManagerB", jcoder.getSource() );

    }

}
