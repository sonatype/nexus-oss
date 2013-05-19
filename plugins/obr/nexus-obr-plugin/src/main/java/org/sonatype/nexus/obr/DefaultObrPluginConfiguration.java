/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

@Component( role = ObrPluginConfiguration.class )
public class DefaultObrPluginConfiguration
    extends AbstractLogEnabled
    implements ObrPluginConfiguration
{
    private static final String DEFAULT_OBR_PROPERTY_PATH = "/META-INF/nexus-obr-plugin/nexus-obr-plugin.properties";

    @Configuration( value = "${nexus-work}/conf/nexus-obr-plugin.properties" )
    private File configFile;

    private Map<String, String> configMap;

    private long lastLoaded;

    private synchronized Map<String, String> getConfiguration()
    {
        if ( null == configMap || lastLoaded < configFile.lastModified() )
        {
            if ( !configFile.exists() )
            {
                writeDefaultConfiguration();
            }

            configMap = loadConfiguration( configFile );
            lastLoaded = System.currentTimeMillis();
        }

        return configMap;
    }

    private static Map<String, String> loadConfiguration( final File file )
    {
        final Map<String, String> newConfig = new HashMap<String, String>();

        final Properties properties = PropertyUtils.loadProperties( file );
        for ( final Entry<?, ?> e : properties.entrySet() )
        {
            final String key = StringUtils.defaultString( e.getKey(), null );
            if ( key != null )
            {
                newConfig.put( key, StringUtils.defaultString( e.getValue(), null ) );
            }
        }

        return newConfig;
    }

    private void writeDefaultConfiguration()
    {
        InputStream is = null;
        OutputStream os = null;

        try
        {
            is = DefaultObrPluginConfiguration.class.getResourceAsStream( DEFAULT_OBR_PROPERTY_PATH );
            os = new FileOutputStream( configFile );

            IOUtil.copy( is, os );
        }
        catch ( final IOException e )
        {
            getLogger().warn( "Could not write the OBR plugin configuration to path " + configFile.getAbsolutePath(), e );
        }
        finally
        {
            IOUtil.close( is );
            IOUtil.close( os );
        }
    }

    private boolean getBoolean( final String key )
    {
        return Boolean.valueOf( getConfiguration().get( key ) );
    }

    public boolean isBundleCacheActive()
    {
        return getBoolean( ObrPluginConfiguration.CATEGORY_BUNDLE_CACHE + ".enabled" );
    }
}
