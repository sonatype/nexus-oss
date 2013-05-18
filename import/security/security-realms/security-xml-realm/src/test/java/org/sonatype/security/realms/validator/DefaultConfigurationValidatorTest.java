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
package org.sonatype.security.realms.validator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;

public class DefaultConfigurationValidatorTest
    extends InjectedTestCase
{
    protected SecurityConfigurationValidator configurationValidator;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.configurationValidator = (SecurityConfigurationValidator) lookup( SecurityConfigurationValidator.class );
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );
    }

    public void testBad1()
        throws Exception
    {
        ValidationResponse response =
            configurationValidator.validateModel( new ValidationRequest<Configuration>(
                                                                                        getConfigurationFromStream( getClass().getResourceAsStream( "/org/sonatype/security/configuration/validator/security-bad1.xml" ) ) ) );

        assertFalse( response.isValid() );

        assertFalse( response.isModified() );

        // emails are not longer unique!
        assertEquals( 11, response.getValidationErrors().size() );

        assertEquals( 0, response.getValidationWarnings().size() );
    }

    public void testBad2()
        throws Exception
    {
        ValidationResponse response =
            configurationValidator.validateModel( new ValidationRequest<Configuration>(
                                                                                        getConfigurationFromStream( getClass().getResourceAsStream( "/org/sonatype/security/configuration/validator/security-bad2.xml" ) ) ) );

        assertFalse( response.isValid() );

        assertTrue( response.isModified() );

        assertEquals( 3, response.getValidationWarnings().size() );

        assertEquals( 12, response.getValidationErrors().size() );
    }

    public void testBad3()
        throws Exception
    {
        ValidationResponse response =
            configurationValidator.validateModel( new ValidationRequest<Configuration>(
                                                                                        getConfigurationFromStream( getClass().getResourceAsStream( "/org/sonatype/security/configuration/validator/security-bad3.xml" ) ) ) );

        assertFalse( response.isValid() );

        assertTrue( response.isModified() );

        assertEquals( 2, response.getValidationWarnings().size() );

        assertEquals( 2, response.getValidationErrors().size() );
    }

    public void testRoles()
        throws Exception
    {
        SecurityValidationContext context = new SecurityValidationContext();

        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "priv" );
        priv.setType( "invalid" );
        context.addExistingPrivilegeIds();
        context.getExistingPrivilegeIds().add( "priv" );

        CRole role1 = new CRole();
        role1.setId( "role1" );
        role1.setName( "role1" );
        role1.setDescription( "desc" );
        role1.setSessionTimeout( 50 );
        role1.addPrivilege( priv.getId() );
        role1.addRole( "role2" );
        ArrayList<String> containedRoles = new ArrayList<String>();
        containedRoles.add( "role2" );
        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role1" );
        context.getRoleContainmentMap().put( "role1", containedRoles );

        CRole role2 = new CRole();
        role2.setId( "role2" );
        role2.setName( "role2" );
        role2.setDescription( "desc" );
        role2.setSessionTimeout( 50 );
        role2.addPrivilege( priv.getId() );
        role2.addRole( "role3" );
        containedRoles = new ArrayList<String>();
        containedRoles.add( "role3" );
        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role2" );
        context.getRoleContainmentMap().put( "role2", containedRoles );

        CRole role3 = new CRole();
        role3.setId( "role3" );
        role3.setName( "role3" );
        role3.setDescription( "desc" );
        role3.setSessionTimeout( 50 );
        role3.addPrivilege( priv.getId() );
        role3.addRole( "role1" );
        containedRoles = new ArrayList<String>();
        containedRoles.add( "role1" );
        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role3" );
        context.getRoleContainmentMap().put( "role3", containedRoles );

        ValidationResponse vr = configurationValidator.validateRoleContainment( context );

        assertFalse( vr.isValid() );
        assertEquals( vr.getValidationErrors().size(), 3 );

    }

    /**
     * NEXUS-5040: Creating a role with an unknown privilege should not result in a validation error, just a warning.
     */
    public void testValidationOfRoleWithUnknownPrivilege()
    {
        SecurityValidationContext context = new SecurityValidationContext();

        CPrivilege priv = new CPrivilege();
        priv.setId( "priv" );
        priv.setName( "priv" );
        priv.setType( "invalid" );
        context.addExistingPrivilegeIds();
        context.getExistingPrivilegeIds().add( "priv" );

        CRole role1 = new CRole();
        role1.setId( "role1" );
        role1.setName( "role1" );
        role1.setDescription( "desc" );
        role1.setSessionTimeout( 50 );
        role1.addPrivilege( priv.getId() );
        role1.addPrivilege( "foo" );

        context.addExistingRoleIds();
        context.getExistingRoleIds().add( "role1" );

        ValidationResponse vr = configurationValidator.validateRole( context, role1, true );

        assertTrue( vr.isValid() );
        assertEquals( vr.getValidationErrors().size(), 0 );
        assertEquals( vr.getValidationWarnings().size(), 1 );
        assertEquals(
            vr.getValidationWarnings().get( 0 ).getMessage(),
            "Role ID 'role1' Invalid privilege id 'foo' found."
        );
    }
}
