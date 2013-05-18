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
package org.sonatype.security.realms.publickey;

import java.security.PublicKey;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * {@link AuthenticationToken} for a {@link PublicKey}.
 * 
 * @author hugo@josefson.org
 */
public class PublicKeyAuthenticationToken
    implements AuthenticationToken
{

    private static final long serialVersionUID = -784273150987377079L;

    private final Object principal;

    private final PublicKey key;

    public PublicKeyAuthenticationToken( Object principal, PublicKey key )
    {
        this.principal = principal;
        this.key = key;
    }

    public Object getPrincipal()
    {
        return principal;
    }

    public PublicKey getCredentials()
    {
        return key;
    }
}
