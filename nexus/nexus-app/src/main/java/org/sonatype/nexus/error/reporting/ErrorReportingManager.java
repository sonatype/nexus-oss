package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.sonatype.nexus.configuration.Configurable;

public interface ErrorReportingManager
    extends Configurable
{
    boolean isEnabled();

    void setEnabled( boolean value );

    String getJIRAUrl();

    void setJIRAUrl( String url );

    String getJIRAProject();

    void setJIRAProject( String pkey );

    String getJIRAUsername();

    void setJIRAUsername( String username );

    String getJIRAPassword();

    void setJIRAPassword( String password );

    boolean isUseGlobalProxy();

    void setUseGlobalProxy( boolean val );

    // ==

    void handleError( ErrorReportRequest request )
        throws IssueSubmissionException, IOException;

    File assembleBundle( ErrorReportRequest request )
        throws IOException;
}
