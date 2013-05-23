/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.capabilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.capabilities.client.Filter;
import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.testsuite.capabilities.client.CapabilityA;
import org.sonatype.nexus.testsuite.capabilities.client.CapabilityB;

public class CapabilitiesIT
    extends CapabilitiesITSupport
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public CapabilitiesIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void crudTypedA()
    {
        // create
        final CapabilityA created = capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        assertThat( created.id(), is( notNullValue() ) );
        assertThat( created.notes(), is( "Some notes" ) );
        assertThat( created.property( "a1" ), is( "foo" ) );
        assertThat( created.propertyA1(), is( "foo" ) );

        // read
        final CapabilityA read = capabilities().get( CapabilityA.class, created.id() );

        assertThat( read.id(), is( created.id() ) );
        assertThat( read.notes(), is( created.notes() ) );
        assertThat( read.type(), is( created.type() ) );
        assertThat( read.properties(), is( created.properties() ) );
        assertThat( read.propertyA1(), is( created.propertyA1() ) );

        // update
        read.withNotes( "Some other notes" ).save();

        final CapabilityA updated = capabilities().get( CapabilityA.class, created.id() );

        assertThat( updated.notes(), is( "Some other notes" ) );
        assertThat( created.refresh().notes(), is( "Some other notes" ) );

        // delete
        read.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", created.id() ) );
        capabilities().get( CapabilityA.class, created.id() );
    }

    @Test
    public void crudTypedB()
    {
        // create
        final CapabilityB created = capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        assertThat( created.id(), is( notNullValue() ) );
        assertThat( created.notes(), is( "Some notes" ) );
        assertThat( created.property( "b1" ), is( "foo" ) );
        assertThat( created.propertyB1(), is( "foo" ) );

        // read
        final CapabilityB read = capabilities().get( CapabilityB.class, created.id() );

        assertThat( read.id(), is( created.id() ) );
        assertThat( read.notes(), is( created.notes() ) );
        assertThat( read.type(), is( created.type() ) );
        assertThat( read.properties(), is( created.properties() ) );
        assertThat( read.propertyB1(), is( created.propertyB1() ) );

        // update
        read.withNotes( "Some other notes" ).save();

        final CapabilityB updated = capabilities().get( CapabilityB.class, created.id() );

        assertThat( updated.notes(), is( "Some other notes" ) );
        assertThat( created.refresh().notes(), is( "Some other notes" ) );

        // delete
        read.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", created.id() ) );
        capabilities().get( CapabilityB.class, created.id() );
    }

    @Test
    public void enableAndDisableTypedA()
    {
        final CapabilityA created = capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        final CapabilityA read = capabilities().get( CapabilityA.class, created.id() );

        assertThat( read.isEnabled(), is( true ) );

        created.disable();
        read.refresh();

        assertThat( read.isEnabled(), is( false ) );

        created.enable();
        read.refresh();

        assertThat( read.isEnabled(), is( true ) );
    }

    @Test
    public void enableAndDisableTypedB()
    {
        final CapabilityB created = capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        final CapabilityB read = capabilities().get( CapabilityB.class, created.id() );

        assertThat( read.isEnabled(), is( true ) );

        created.disable();
        read.refresh();

        assertThat( read.isEnabled(), is( false ) );

        created.enable();
        read.refresh();

        assertThat( read.isEnabled(), is( true ) );
    }

    @Test
    public void getInexistentTypedA()
    {
        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( "Capability with id 'getInexistent' was not found" );
        capabilities().get( CapabilityA.class, "getInexistent" );
    }

    @Test
    public void getInexistentTypedB()
    {
        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( "Capability with id 'getInexistent' was not found" );
        capabilities().get( CapabilityB.class, "getInexistent" );
    }

    @Test
    public void updateInexistentTypedA()
    {
        final CapabilityA created = capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        final CapabilityA read = capabilities().get( CapabilityA.class, created.id() );
        created.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", created.id() ) );
        read.save();
    }

    @Test
    public void updateInexistentTypedB()
    {
        final CapabilityB created = capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        final CapabilityB read = capabilities().get( CapabilityB.class, created.id() );
        created.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", created.id() ) );
        read.save();
    }

    @Test
    public void deleteInexistentTypedA()
    {
        final CapabilityA created = capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        final CapabilityA read = capabilities().get( CapabilityA.class, created.id() );
        created.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", created.id() ) );
        read.remove();
    }

    @Test
    public void deleteInexistentTypedB()
    {
        final CapabilityB created = capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        final CapabilityB read = capabilities().get( CapabilityB.class, created.id() );
        created.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", created.id() ) );
        read.remove();
    }

    @Test
    public void getThemAll()
    {
        capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        final Collection<Capability> capabilities = capabilities().get();

        assertThat( capabilities, is( notNullValue() ) );
        assertThat( capabilities, hasSize( greaterThan( 0 ) ) );
    }

    @Test
    public void filterByType()
    {
        capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        final Collection<Capability> capabilities = capabilities().get(
            Filter.capabilitiesThat().capabilitiesThat().haveType( "[a]" )
        );

        assertThat( capabilities, is( notNullValue() ) );
        assertThat( capabilities, hasSize( greaterThan( 0 ) ) );
        for ( Capability capability : capabilities )
        {
            MatcherAssert.assertThat( capability, instanceOf( CapabilityA.class ) );
        }
    }

    @Test
    public void filterByTypeTyped()
    {
        capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();

        capabilities().create( CapabilityB.class )
            .withNotes( "Some notes" )
            .withPropertyB1( "foo" )
            .save();

        final Collection<CapabilityA> capabilities = capabilities().get(
            CapabilityA.class,
            Filter.capabilitiesThat().haveType( "[a]" )
        );

        assertThat( capabilities, is( notNullValue() ) );
        assertThat( capabilities, hasSize( greaterThan( 0 ) ) );
    }

    @Test
    public void filterByTypeAndProperty()
    {
        capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "foo" )
            .save();
        capabilities().create( CapabilityA.class )
            .withNotes( "Some notes" )
            .withPropertyA1( "bar" )
            .save();

        final Collection<Capability> capabilities = capabilities().get(
            Filter.capabilitiesThat().haveType( "[a]" ).haveProperty( "a1", "bar" )
        );

        assertThat( capabilities, is( notNullValue() ) );
        assertThat( capabilities, hasSize( greaterThan( 0 ) ) );
        for ( Capability capability : capabilities )
        {
            MatcherAssert.assertThat( capability, instanceOf( CapabilityA.class ) );
            MatcherAssert.assertThat( capability.property( "a1" ), is( "bar" ) );
        }
    }

    /**
     * Verify that create will fail if a mandatory property is not set.
     */
    @Test
    public void failIfMandatoryPropertyNotPresent()
    {
        thrown.expect( NexusClientErrorResponseException.class );
        thrown.expectMessage( "[repository] Repository/Group is required" );
        capabilities().create( "[message]" )
            .save();
    }

    /**
     * Verify that create will fail if a property validated via regexp (in this case XYZ.*) does not match.
     */
    @Test
    public void failIfNotMatchingRegexp()
    {
        thrown.expect( NexusClientErrorResponseException.class );
        thrown.expectMessage( "[message] Message does not match 'XYZ.*'" );
        capabilities().create( "[message]" )
            .withProperty( "repository", "releases" )
            .withProperty( "message", "Blah" )
            .save();
    }

    /**
     * Verify that create will fail if a property validated via regexp (in this case XYZ.*) does not match.
     */
    @Test
    public void doNotFailIfMatchingRegexp()
    {
        capabilities().create( "[message]" )
            .withProperty( "repository", "releases" )
            .withProperty( "message", "XYZ Blah" )
            .save();
    }

    /**
     * Verify that in case of an activation failure the flag is set and description contains details.
     *
     * @since 2.4
     */
    @Test
    public void checkErrorOnActivation()
    {
        final Capability capability = capabilities().create( "[withActivationError]" ).save();
        MatcherAssert.assertThat( capability.hasErrors(), is( true ) );
        MatcherAssert.assertThat( capability.isActive(), is( false ) );
        MatcherAssert.assertThat( capability.stateDescription(),
                                  containsString( "This capability always fails on activate" ) );
    }

}
