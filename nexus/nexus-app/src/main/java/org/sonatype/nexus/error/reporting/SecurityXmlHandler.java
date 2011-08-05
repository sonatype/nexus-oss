/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;

public class SecurityXmlHandler
    extends AbstractXmlHandler
{
    public File getFile( SecurityModelConfigurationSource source, NexusConfiguration nexusConfig )
        throws IOException
    {
        Configuration configuration = 
            ( Configuration ) cloneViaXml( source.getConfiguration() );
        
        // No config ?
        if ( configuration == null )
        {
            return null;
        }
        
        for ( CUser user : ( List<CUser> ) configuration.getUsers() )
        {
            user.setPassword( PASSWORD_MASK );
            user.setEmail( PASSWORD_MASK );
        }
        
        SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "security.xml." + System.currentTimeMillis() );
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
