package org.sonatype.nexus.client.testsuite;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.subsystem.security.Users;

/**
 * @since 2.5
 */
public class UserIT
    extends NexusClientITSupport
{

    public UserIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    /**
     * Related to NEXUS-5037 ensure that html escaped passwords(specifically quote character in this case) can be used as credentials.
     */
    @Test
    public void testUserWithSingleQuotePassword()
    {
        Users users = client().getSubsystem( Users.class );
        String password = "\"";
        users.create( "test" ).withPassword( password ).withRole( "nx-admin" ).withEmail( "no@where.com" ).save();
        NexusClient client = createNexusClient( nexus(), "test", password );
        //will fail if can't authenticate
        assertThat( client.getNexusStatus(), is( notNullValue() ) );
    }
}
