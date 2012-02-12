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
package org.sonatype.nexus.error.reporting.bundle;

import java.io.File;
import java.io.FileFilter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.FileBundle;

@Component( role = BundleAssembler.class, hint = "conf-dir" )
public class ConfigFilesBundleAssembler
    implements BundleAssembler
{

    @Requirement
    private NexusConfiguration nexusConfig;

    @Override
    public boolean isParticipating( IssueSubmissionRequest request )
    {
        return true;
    }

    @Override
    public Bundle assemble( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        File confDir = nexusConfig.getWorkingDirectory( "conf" );

        FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept( File pathname )
            {
                return !pathname.getName().endsWith( ".bak" ) && !pathname.getName().endsWith( "nexus.xml" )
                    && !pathname.getName().endsWith( "security.xml" )
                    && !pathname.getName().endsWith( "security-configuration.xml" );
            }
        };

        FileBundle bundle = new FileBundle( confDir );
        bundle.setFilter( filter );
        return bundle;
    }

}
