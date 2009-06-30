package org.sonatype.nexus.selenium.util;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import org.junit.Assert;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.TwinPanel;

public class NxAssert
{
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
        Assert.assertThat( panel.getErrorText(), equalTo( errorText ) );
    }

    @SuppressWarnings( "unchecked" )
    public static void noErrorText( TwinPanel panel )
    {
        Assert.assertThat( panel.getErrorText(), anyOf( equalTo( "null" ), equalTo( "" ), nullValue() ) );
    }

    public static void valueEqualsTo( TextField field, String value )
    {
        Assert.assertThat( field.getValue(), equalTo( value ) );
    }

    public static void contains( TwinPanel twinPanel, String value )
    {
        Assert.assertTrue( twinPanel.containsLeftSide( value ) );
    }

    @SuppressWarnings( "unchecked" )
    public static void noErrorText( TextField tf )
    {
        Assert.assertThat( tf.getErrorText(), anyOf( equalTo( "null" ), equalTo( "" ), nullValue() ) );
    }

    public static void hasErrorText( TextField tf, String errorText )
    {
        Assert.assertThat( tf.getErrorText(), equalTo( errorText ) );
    }

    public static void requiredField( Combobox cb, int i )
    {
        cb.setValue( "" );
        hasErrorText( cb, THIS_FIELD_IS_REQUIRED );
        cb.select( i );
        noErrorText( cb );
    }

}
