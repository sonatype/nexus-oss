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
package org.sonatype.nexus.util.configurationreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.locks.Lock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.ConfigurationUpgrader;
import org.sonatype.configuration.upgrade.UnsupportedConfigurationVersionException;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;

@SuppressWarnings( "deprecation" )
@Component( role = ConfigurationHelper.class )
public class DefaultConfigurationHelper
    extends AbstractLogEnabled
    implements ConfigurationHelper
{

    public <E extends org.sonatype.configuration.Configuration> E load( E baseConfiguration, String modelVersion,
                                                                        File configurationFile, Lock lock,
                                                                        ConfigurationReader<E> reader,
                                                                        ConfigurationValidator<E> validator,
                                                                        ConfigurationUpgrader<E> upgrader )
    {
        lock.lock();

        Reader fr = null;
        FileInputStream is = null;

        E configuration = null;
        try
        {
            Reader r = new FileReader( configurationFile );

            Xpp3Dom dom = Xpp3DomBuilder.build( r );

            if ( upgrader != null )
            {
                if ( !modelVersion.equals( dom.getChild( "version" ).getValue() ) )
                {
                    configuration = upgrader.loadOldConfiguration( configurationFile );

                    File backup = new File( configurationFile.getParentFile(), configurationFile.getName() + ".bak" );

                    FileUtils.copyFile( configurationFile, backup );
                }
            }

            is = new FileInputStream( configurationFile );

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );

            if ( validator != null )
            {
                ValidationResponse vr = validator.validateModel( new ValidationRequest<E>( configuration ) );

                if ( vr.getValidationErrors().size() > 0 )
                {
                    // TODO need to code the handling of invalid config
                    configuration = baseConfiguration;
                }
            }
        }
        catch ( FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            configuration = baseConfiguration;
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while retrieving configuration file", e );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        catch ( ConfigurationIsCorruptedException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        catch ( UnsupportedConfigurationVersionException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        finally
        {
            if ( fr != null )
            {
                try
                {
                    fr.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }

        return configuration;
    }

    public <E> void save( E configuration, File configurationFile, ConfigurationWritter<E> writer, Lock lock )
    {
        lock.lock();

        configurationFile.getParentFile().mkdirs();

        Writer fw = null;

        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( configurationFile ) );

            writer.write( fw, configuration );
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while storing configuration file", e );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.flush();

                    fw.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }

    }

}
