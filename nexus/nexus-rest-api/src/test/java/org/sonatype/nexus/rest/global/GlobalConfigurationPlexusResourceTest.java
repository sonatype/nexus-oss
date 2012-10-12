package org.sonatype.nexus.rest.global;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;
import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link GlobalConfigurationPlexusResource} behavior.
 */
public class GlobalConfigurationPlexusResourceTest
    extends TestSupport
{

    private GlobalConfigurationPlexusResource testSubject = new GlobalConfigurationPlexusResource();

    @Test
    public void unescapeHTMLInSMTPPassword()
    {
        // settings object as it would come in via REST, with escaped HTML
        SmtpSettings settings = new SmtpSettings();
        settings.setPassword( "asdf&amp;qwer" );
        settings.setUsername( "asdf&amp;qwer" );

        // make sure the configuration resource configures xstream to unescape
        final XStream xStream = new XStream();
        testSubject.configureXStream( xStream );

        final String xml = xStream.toXML( settings );
        settings = (SmtpSettings) xStream.fromXML( xml );

        assertThat( settings.getUsername(), is( "asdf&qwer" ) );
        assertThat( settings.getPassword(), is( "asdf&qwer" ) );
    }
}
