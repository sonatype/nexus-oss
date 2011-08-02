/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

package org.sonatype.security.ldap.dao;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests for LdapAuthConfiguration
 */
public class LdapAuthConfigurationTest
{

    @Test
    public void testGetUserAttributes()
    {
        LdapAuthConfiguration ldapAuthConfiguration = new LdapAuthConfiguration();
        ldapAuthConfiguration.setEmailAddressAttribute( "emailAddressAttribute" );
        ldapAuthConfiguration.setPasswordAttribute( null );
        // unset the defaults (using a mix of empty strings and nulls
        ldapAuthConfiguration.setUserIdAttribute( "" );
        ldapAuthConfiguration.setUserRealNameAttribute( null );
        ldapAuthConfiguration.setUserMemberOfAttribute( "" );
        ldapAuthConfiguration.setWebsiteAttribute( null );

        String[] userAttributes = ldapAuthConfiguration.getUserAttributes();
         Assert.assertEquals( "Actual result: "+ Arrays.asList( userAttributes ), 1, userAttributes.length );
        //only non null attributes should be added to the list
        Assert.assertEquals( "emailAddressAttribute", userAttributes[0] );

        // set a few more then check the count
        ldapAuthConfiguration.setPasswordAttribute( "passwordAttribute" );
        ldapAuthConfiguration.setUserIdAttribute( "userIdAttribute" );

        userAttributes = ldapAuthConfiguration.getUserAttributes();
        Assert.assertEquals( "Actual result: "+ Arrays.asList( userAttributes ), 3, userAttributes.length );
        
    }
}
