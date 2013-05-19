/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.configuration.source;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

/**
 * FIXME This needs to be abstracted, as this is just a copy of the class in nexus. The problem is if we move this to
 * base-configuration (or something) it becomes less secure, as we are using the same key for everything)
 */
@Singleton
@Typed( PasswordHelper.class )
@Named( "default" )
public class PasswordHelper
{
    private static final String ENC = "CMMDwoV";

    private final PlexusCipher plexusCipher;

    @Inject
    public PasswordHelper( PlexusCipher plexusCipher )
    {
        this.plexusCipher = plexusCipher;
    }

    public String encrypt( String password )
        throws PlexusCipherException
    {
        return encrypt( password, ENC );
    }

    public String encrypt( String password, String encoding )
        throws PlexusCipherException
    {
        if ( password != null )
        {
            return plexusCipher.encryptAndDecorate( password, encoding );
        }

        return null;
    }

    public String decrypt( String encodedPassword )
        throws PlexusCipherException
    {
        return decrypt( encodedPassword, ENC );
    }

    public String decrypt( String encodedPassword, String encoding )
        throws PlexusCipherException
    {
        // check if the password is encrypted
        if ( !plexusCipher.isEncryptedString( encodedPassword ) )
        {
            return encodedPassword;
        }

        if ( encodedPassword != null )
        {
            return plexusCipher.decryptDecorated( encodedPassword, encoding );
        }
        return null;
    }
}
