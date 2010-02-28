/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

/**
 * Abstract {@link NexusPluginRepository} that can parse plugin metadata.
 */
abstract class AbstractNexusPluginRepository
    implements NexusPluginRepository
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final PluginModelXpp3Reader PLUGIN_METADATA_READER = new PluginModelXpp3Reader();

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    @Named( PlexusConstants.PLEXUS_KEY )
    @SuppressWarnings( "unchecked" )
    private Map variables;

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Parses a {@code plugin.xml} resource into plugin metadata.
     * 
     * @param pluginXml The plugin.xml URL
     * @return Nexus plugin metadata
     */
    final PluginMetadata getPluginMetadata( final URL pluginXml )
        throws IOException
    {
        final InputStream in = pluginXml.openStream();
        try
        {
            final Reader reader = new InterpolationFilterReader( ReaderFactory.newXmlReader( in ), variables );
            return PLUGIN_METADATA_READER.read( reader );
        }
        catch ( final XmlPullParserException e )
        {
            throw new IOException( "Problem parsing: " + pluginXml + " reason: " + e );
        }
        finally
        {
            IOUtil.close( in );
        }
    }
}
