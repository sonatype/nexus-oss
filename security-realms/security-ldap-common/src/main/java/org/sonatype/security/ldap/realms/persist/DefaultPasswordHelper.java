package org.sonatype.security.ldap.realms.persist;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.security.ldap.upgrade.cipher.PlexusCipher;
import org.sonatype.security.ldap.upgrade.cipher.PlexusCipherException;


@Component( role = PasswordHelper.class )
public class DefaultPasswordHelper
    implements PasswordHelper
{

    private static final String ENC = "CMMDwoV";

    @Requirement
    private PlexusCipher plexusCipher;

    public String encrypt( String password )
        throws PlexusCipherException
    {
        if ( password != null )
        {

            return plexusCipher.encrypt( password, ENC );
        }
        return null;
    }

    public String decrypt( String encodedPassword )
        throws PlexusCipherException
    {
        if ( encodedPassword != null )
        {
            return plexusCipher.decrypt( encodedPassword, ENC );
        }
        return null;
    }
}
