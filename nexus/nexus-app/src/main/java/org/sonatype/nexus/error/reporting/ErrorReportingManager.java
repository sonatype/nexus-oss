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
package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

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

    ErrorReportResponse handleError( ErrorReportRequest request )
        throws IssueSubmissionException, IOException, GeneralSecurityException;

    ErrorReportResponse handleError( ErrorReportRequest request, String jiraUsername, String jiraPassword,
                                     boolean useGlobalHttpProxy )
        throws IssueSubmissionException, IOException, GeneralSecurityException;

    File assembleBundle( ErrorReportRequest request )
        throws IOException;
}
