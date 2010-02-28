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
package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * Describes a response from the {@link NexusPluginManager} concerning a {@link PluginActivationRequest}.
 */
public final class PluginManagerResponse
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final GAVCoordinate originator;

    private final PluginActivationRequest request;

    private final List<PluginResponse> responses = new ArrayList<PluginResponse>( 5 );

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PluginManagerResponse( final GAVCoordinate originator, final PluginActivationRequest request )
    {
        this.originator = originator;
        this.request = request;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public GAVCoordinate getOriginator()
    {
        return originator;
    }

    public PluginActivationRequest getRequest()
    {
        return request;
    }

    public List<PluginResponse> getProcessedPluginResponses()
    {
        return Collections.unmodifiableList( responses );
    }

    public boolean isSuccessful()
    {
        for ( final PluginResponse r : responses )
        {
            if ( !r.isSuccessful() )
            {
                return false;
            }
        }
        return true;
    }

    public String formatAsString( final boolean detailed )
    {
        final StringBuilder buf = new StringBuilder();
        final boolean successful = isSuccessful();

        buf.append( "Plugin manager request \"" ).append( request ).append( "\" on plugin \"" ).append( originator );
        buf.append( successful ? "\" was successful." : "\" FAILED!" );

        if ( detailed || !successful )
        {
            buf.append( LS ).append( "The following plugins were processed:" ).append( LS );
            for ( final PluginResponse r : responses )
            {
                buf.append( r.formatAsString( detailed ) );
            }
        }

        return buf.toString();
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    void addPluginResponse( final PluginResponse response )
    {
        responses.add( response );
    }

    void addPluginManagerResponse( final PluginManagerResponse managerResponse )
    {
        responses.addAll( managerResponse.responses );
    }
}
