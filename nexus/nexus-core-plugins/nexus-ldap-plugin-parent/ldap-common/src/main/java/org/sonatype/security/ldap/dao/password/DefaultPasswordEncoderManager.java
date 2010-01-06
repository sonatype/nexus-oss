/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author cstamas
 */
@Component( role = PasswordEncoderManager.class )
public class DefaultPasswordEncoderManager
    extends AbstractLogEnabled
    implements PasswordEncoderManager
{

    private static final Pattern ENCODING_SPEC_PATTERN = Pattern.compile( "\\{([a-zA-Z0-9]+)\\}(.+)" );

    /**
     */
    @Requirement( role = PasswordEncoder.class )
    private List<PasswordEncoder> encoders;

    /**
     */
    @Configuration( value = "clear" )
    private String preferredEncoding;

    @Requirement( role = PasswordEncoder.class )
    private Map<String, PasswordEncoder> encodersMap;

    public String encodePassword( String password, Object salt )
    {
        PasswordEncoder encoder = getPasswordEncoder( preferredEncoding );

        if ( encoder == null )
        {
            throw new IllegalStateException( "Preferred encoding has no associated PasswordEncoder." );
        }

        return encoder.encodePassword( password, salt );
    }

    public boolean isPasswordValid( String encodedPassword, String password, Object salt )
    {
        String encoding = preferredEncoding;

        Matcher matcher = ENCODING_SPEC_PATTERN.matcher( encodedPassword );

        if ( matcher.matches() )
        {
            encoding = matcher.group( 1 );
            encodedPassword = matcher.group( 2 );
        }

        PasswordEncoder encoder = getPasswordEncoder( encoding.toLowerCase() );

        getLogger().info( "Verifying password with encoding: " + encoding + " (encoder: " + encoder + ")." );

        if ( encoder == null )
        {
            throw new IllegalStateException( "Password encoding: " + encoding + " has no associated PasswordEncoder." );
        }

        return encoder.isPasswordValid( encodedPassword, password, salt );
    }

    public String getPreferredEncoding()
    {
        return preferredEncoding;
    }

    public void setPreferredEncoding( String preferredEncoding )
    {
        this.preferredEncoding = preferredEncoding.toLowerCase();
    }

    private PasswordEncoder getPasswordEncoder( String encoding )
    {
        if ( encodersMap == null )
        {
            encodersMap = new HashMap<String, PasswordEncoder>( encoders.size() );
            for ( PasswordEncoder encoder : encoders )
            {
                encodersMap.put( encoder.getMethod().toLowerCase(), encoder );
            }
        }
        if ( encodersMap.containsKey( encoding ) )
        {
            return encodersMap.get( encoding );
        }
        else
        {
            return null;
        }
    }

}
