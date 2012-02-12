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
import java.io.FileWriter;
import java.io.IOException;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;

public class SecurityConfigurationXmlHandler
    extends AbstractXmlHandler
{
    public File getFile( SecurityConfigurationSource source, NexusConfiguration nexusConfig )
        throws IOException
    {
        SecurityConfiguration configuration = ( SecurityConfiguration )cloneViaXml( source.getConfiguration() );
        
        // No config ??
        if ( configuration == null )
        {
            return null;
        }
        
        configuration.setAnonymousPassword( PASSWORD_MASK );
        
        SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();
        
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "security-configuration.xml." + System.currentTimeMillis() );
            fWriter = new FileWriter( tempFile );
            writer.write( fWriter, configuration );
        }
        finally
        {
            if ( fWriter != null )
            {
                fWriter.close();
            }
        }
        
        return tempFile;
    }
}
