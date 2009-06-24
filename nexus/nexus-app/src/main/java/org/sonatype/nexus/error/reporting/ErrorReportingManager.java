package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.swizzle.jira.Issue;
import org.sonatype.nexus.configuration.model.CErrorReporting;

public interface ErrorReportingManager
{
    void handleError( ErrorReportRequest request )
        throws IssueSubmissionException,
            IOException;
    
    File assembleBundle( ErrorReportRequest request )
        throws IOException;
    
    List<Issue> retrieveIssues( CErrorReporting errorConfig, String description );
    
    boolean isEnabled();
}
