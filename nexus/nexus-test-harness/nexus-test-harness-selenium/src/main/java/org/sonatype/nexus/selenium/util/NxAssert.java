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
    public static void requiredField( Combobox cb, String validText )
    {
        cb.setValue( "" );
        Assert.assertTrue( "Expected validation", cb.hasErrorText( "This field is required" ) );
        cb.setValue( validText );
        Assert.assertFalse( "Should pass validation", cb.hasErrorText( "This field is required" ) );
    }

    public static void requiredField( TextField tf, String validText )
    {
        tf.type( "" );
        Assert.assertTrue( "Expected validation", tf.hasErrorText( "This field is required" ) );
        tf.type( validText );
        Assert.assertFalse( "Should pass validation", tf.hasErrorText( "This field is required" ) );
    }

    public static void hasErrorText( TwinPanel panel, String errorText )
    {
        Assert.assertTrue( "TwinPanel should have error: " + errorText, panel.hasErrorText( errorText ) );
    }

    @SuppressWarnings( "unchecked" )
    public static void noErrorText( TwinPanel panel )
    {
        Assert.assertThat( panel.getErrorText(), anyOf( equalTo( "null" ), equalTo( "" ), nullValue() ) );
    }

}
