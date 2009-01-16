/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author juven
 */
@Component( role = LogConfiguration.class )
public class Log4jLogConfiguration
    implements LogConfiguration<Properties>
{

    @Requirement
    private LogConfigurationSource<File> logConfigurationSource;

    private Properties config = new Properties();

    public void apply()
    {
        PropertyConfigurator.configure( config );
    }

    public Properties getConfig()
    {
        return config;
    }

    public void setConfig( Properties config )
    {
        this.config = config;
    }

    public void load()
        throws IOException
    {
        FileInputStream inputStream = new FileInputStream( logConfigurationSource.getSource() );

        try
        {
            config.load( inputStream );
        }
        finally
        {
            inputStream.close();
        }
    }

    public void save()
        throws IOException
    {
        FileOutputStream outputStream = new FileOutputStream( logConfigurationSource.getSource() );

        try
        {
            config.store( outputStream, "Autumanically created by nexus" );
        }
        finally
        {
            outputStream.close();
        }
    }

}
