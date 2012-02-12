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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.error.report.ErrorReportBundleContentContributor;
import org.sonatype.nexus.error.report.ErrorReportBundleEntry;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.DirBundle;
import org.sonatype.sisu.pr.bundle.ManagedBundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

@Component( role = BundleAssembler.class, hint = "legacy-contributors" )
public class LegacyBundleContributorsAssembler
    implements BundleAssembler
{
    @Requirement( role = ErrorReportBundleContentContributor.class )
    private Map<String, ErrorReportBundleContentContributor> bundleExtraContent;
    
    @Requirement
    private StorageManager storageManager;

    @Override
    public boolean isParticipating( IssueSubmissionRequest request )
    {
        return true;
    }

    @Override
    public Bundle assemble( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        DirBundle main = new DirBundle( "extra" );
        
        try
        {
            Set<Entry<String, ErrorReportBundleContentContributor>> bundleExtraContent = this.bundleExtraContent.entrySet();
            for ( Entry<String, ErrorReportBundleContentContributor> extraContent : bundleExtraContent )
            {
                DirBundle dir = new DirBundle( extraContent.getKey() );
                ErrorReportBundleEntry[] entries = extraContent.getValue().getEntries();
                for ( ErrorReportBundleEntry errorReportBundleEntry : entries )
                {
                    String entryName = errorReportBundleEntry.getEntryName();
                    InputStream content = errorReportBundleEntry.getContent();
                    
                    ManagedBundle bundle = storageManager.createBundle( entryName, "application/octet-stream" );
	                dir.addSubBundle(bundle);
                
                    OutputStream out = bundle.getOutputStream();
                    IOUtil.copy( content, out);
                    out.close();

                    errorReportBundleEntry.releaseEntry();
                }
            }
        }
        catch ( IOException e )
        {
            throw new IssueSubmissionException( "Could not assemble extra bundle contributions: " + e.getMessage(), e );
        }
        
        return main;
    }
    
}
