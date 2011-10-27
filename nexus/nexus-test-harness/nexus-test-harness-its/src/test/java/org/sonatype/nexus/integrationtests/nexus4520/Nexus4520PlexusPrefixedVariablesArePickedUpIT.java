package org.sonatype.nexus.integrationtests.nexus4520;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringOutputStream;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TODO
 *
 * @author: cstamas
 */
public class Nexus4520PlexusPrefixedVariablesArePickedUpIT
    extends AbstractNexusIntegrationTest
{

    public static final String SYSPROP_PREFIX = "plexus.";

    public static final String CUSTOM_SYSPROP_KEY = "customKey";

    public static final String CUSTOM_SYSPROP_VALUE = "customValue";

    @BeforeClass( alwaysRun = true )
    public void setSystemProperty()
        throws Exception
    {
        System.setProperty( SYSPROP_PREFIX + CUSTOM_SYSPROP_KEY, CUSTOM_SYSPROP_VALUE );
    }

    @AfterClass( alwaysRun = true )
    public void removeSystemProperty()
        throws Exception
    {
        System.getProperties().remove( SYSPROP_PREFIX + CUSTOM_SYSPROP_KEY );
    }

    private PrintStream actualOut;

    private ByteArrayOutputStream fakeOut;

    @BeforeClass( alwaysRun = true )
    public void grabOut()
        throws Exception
    {
        actualOut = System.out;

        System.setOut( new PrintStream( fakeOut = new ByteArrayOutputStream() ) );
    }

    @AfterClass( alwaysRun = true )
    public void restoreOut()
        throws Exception
    {
        System.setOut( actualOut );
    }

    @Test
    public void isCustomSyspropPresentInAppContext()
        throws IOException
    {
        final String bootLog = new String( fakeOut.toByteArray() );

        // the wrapper log should contain following line:
        // "customKey"="customValue" (raw: "customValue", src: prefixRemove(prefix:plexus., filter(keyStartsWith:[plexus.], system(properties))))
        assertThat( bootLog.contains( String.format(
            "\"%s\"=\"%s\" (raw: \"%s\", src: prefixRemove(prefix:plexus., filter(keyStartsWith:[plexus.], system(properties))))",
            CUSTOM_SYSPROP_KEY, CUSTOM_SYSPROP_VALUE, CUSTOM_SYSPROP_VALUE ) ),
                    is( true ) );

    }

}
