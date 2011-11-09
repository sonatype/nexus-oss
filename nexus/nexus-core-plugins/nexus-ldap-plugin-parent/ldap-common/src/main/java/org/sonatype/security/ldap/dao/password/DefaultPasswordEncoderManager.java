/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cstamas
 */
@Component( role = PasswordEncoderManager.class )
public class DefaultPasswordEncoderManager
    implements PasswordEncoderManager
{

    private static final Pattern ENCODING_SPEC_PATTERN = Pattern.compile( "\\{([a-zA-Z0-9]+)\\}(.+)" );

    private Logger logger = LoggerFactory.getLogger( getClass() );

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

    protected Logger getLogger()
    {
        return logger;
    }

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
        if( encodedPassword == null )
        {
            return false;
        }
        
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
