package org.sonatype.nexus.selenium.util;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    private static Matcher NULL()
    {
        return anyOf( equalTo( "null" ), equalTo( "" ), nullValue() );
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
