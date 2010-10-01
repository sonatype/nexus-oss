/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;


public class SimpleLdapServer extends AbstractLdapTestEnvironment
{

    /**
     * @param args
     * @throws Exception 
     */
    public static void main( String[] args ) throws Exception
    {
        try
        {
        SimpleLdapServer server = new SimpleLdapServer();
        server.setUp();
        }
        finally
        {
//        System.exit( 0 );
        }
        
    }

}
