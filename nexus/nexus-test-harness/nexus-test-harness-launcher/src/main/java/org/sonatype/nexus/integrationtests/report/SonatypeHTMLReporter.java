package org.sonatype.nexus.integrationtests.report;

import org.apache.velocity.app.Velocity;
import org.uncommons.reportng.HTMLReporter;

public class SonatypeHTMLReporter
    extends HTMLReporter
{
    

    static
    {
        Velocity.setProperty( "runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem" );
    }

}
