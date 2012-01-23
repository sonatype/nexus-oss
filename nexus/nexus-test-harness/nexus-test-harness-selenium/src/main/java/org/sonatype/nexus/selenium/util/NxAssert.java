/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.selenium.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.hamcrest.Matcher;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Tree;
import org.sonatype.nexus.mock.components.TwinPanel;

@SuppressWarnings( "unchecked" )
public class NxAssert
{

    private static Matcher<String> NULL()
    {
        return anyOf( equalTo( "null" ), equalTo( "" ), nullValue( String.class ) );
    }

    public static final String THIS_FIELD_IS_REQUIRED = "This field is required";

    public static void requiredField( Combobox cb, String validText )
    {
        cb.setValue( "" );
        hasErrorText( cb, THIS_FIELD_IS_REQUIRED );
        cb.setValue( validText );
        noErrorText( cb );
    }

    public static void requiredField( TextField tf, String validText )
    {
        tf.resetValue();
        tf.type( "" );
        hasErrorText( tf, THIS_FIELD_IS_REQUIRED );
        tf.type( validText );
        noErrorText( tf );
    }

    public static void hasErrorText( TwinPanel panel, String errorText )
    {
        assertThat( panel.getErrorText(), equalTo( errorText ) );
    }

    public static void noErrorText( TwinPanel panel )
    {
        assertThat( panel.getErrorText(), NULL() );
    }

    public static void valueEqualsTo( TextField field, String value )
    {
        assertThat( field.getValue(), equalTo( value ) );
    }

    public static void disabled( TextField field )
    {
        assertThat( field.isDisabled(), is( true ) );
    }

    public static void contains( TwinPanel twinPanel, String... values )
    {
        for ( String value : values )
        {
            assertTrue( twinPanel.containsLeftSide( value ) );
        }
    }

    public static void contains( Tree tree, String... values )
    {
        for ( String value : values )
        {
            assertTrue( "Tree does not contains " + value, tree.contains( value ) );
        }
    }

    public static void notContains( Tree tree, String... values )
    {
        for ( String value : values )
        {
            assertFalse( "Tree does contains " + value, tree.contains( value ) );
        }
    }

    public static void noErrorText( TextField tf )
    {
        assertThat( tf.getErrorText(), NULL() );
    }

    public static void hasErrorText( TextField tf, String errorText )
    {
        assertThat( tf.getErrorText(), equalTo( errorText ) );
    }

    public static void requiredField( Combobox cb, int i )
    {
        cb.setValue( "" );
        hasErrorText( cb, THIS_FIELD_IS_REQUIRED );
        cb.select( i );
        noErrorText( cb );
    }

    public static void valueNull( TextField tf )
    {
        assertThat( tf.getValue(), NULL() );
    }

}
