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
