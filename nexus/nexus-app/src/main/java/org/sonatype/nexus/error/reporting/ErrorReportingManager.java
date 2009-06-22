package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.swizzle.IssueSubmissionException;

public interface ErrorReportingManager
{
    void handleError( Throwable t )
        throws IssueSubmissionException,
            IOException;
    
    File assembleBundle()
        throws IOException;
}
