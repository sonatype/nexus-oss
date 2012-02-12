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
import java.io.OutputStreamWriter;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.ManagedBundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

@Component(role = BundleAssembler.class, hint = "security.xml")
public class SecurityXmlHandler
    extends AbstractXmlHandler
    implements BundleAssembler
{
    @Requirement
    SecurityModelConfigurationSource source;
    
    @Requirement
    NexusConfiguration nexusConfig;
    
    @Requirement
    StorageManager storageManager;
    
    @Override
    public boolean isParticipating( IssueSubmissionRequest request )
    {
        return source.getConfiguration() != null;
    }

    @Override
    public Bundle assemble( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {        
        OutputStreamWriter out = null;
        try
        {
            ManagedBundle bundle = storageManager.createBundle( "security.xml", "application/xml" );
            Configuration configuration = 
                            ( Configuration ) cloneViaXml( source.getConfiguration() );
                                

			for ( CUser user : ( List<CUser> ) configuration.getUsers() )
			{
			    user.setPassword( PASSWORD_MASK );
			    user.setEmail( PASSWORD_MASK );
			}
            SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();
            
            out = new OutputStreamWriter( bundle.getOutputStream() );
            writer.write( out, configuration );
            out.close();
            
            return bundle;
        }
        catch ( IOException e )
        {
            IOUtil.close( out );
            throw new IssueSubmissionException( "Could not assemble security.xml: " + e.getMessage(), e );
        }
    }
}
