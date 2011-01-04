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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * Describes a response from a Nexus plugin concerning a {@link PluginActivationRequest}.
 */
public final class PluginResponse
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String LS = System.getProperty( "line.separator" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final GAVCoordinate gav;

    private final PluginActivationRequest request;

    private PluginActivationResult result;

    private PluginDescriptor descriptor;

    private Throwable reason;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PluginResponse( final GAVCoordinate gav, final PluginActivationRequest request )
    {
        this.gav = gav;
        this.request = request;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public GAVCoordinate getPluginCoordinates()
    {
        return gav;
    }

    public PluginActivationRequest getWantedGoal()
    {
        return request;
    }

    public PluginActivationResult getAchievedGoal()
    {
        return result;
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return descriptor;
    }

    public Throwable getThrowable()
    {
        return reason;
    }

    public boolean isSuccessful()
    {
        return request.isSuccessful( result );
    }

    public String formatAsString( final boolean detailed )
    {
        final StringBuilder buf = new StringBuilder();

        buf.append( "... " ).append( gav );
        buf.append( " :: action=" ).append( request ).append( " result=" ).append( result ).append( LS );
        if ( !isSuccessful() && null != reason )
        {
            buf.append( "       Reason: " ).append( reason.getLocalizedMessage() ).append( LS );
            if ( detailed )
            {
                final Writer writer = new StringWriter();
                reason.printStackTrace( new PrintWriter( writer ) );
                buf.append( "Stack trace:" ).append( LS ).append( writer ).append( LS );
            }
        }

        if ( detailed && null != descriptor )
        {
            buf.append( LS ).append( descriptor.formatAsString() );
        }

        return buf.toString();
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    void setAchievedGoal( final PluginActivationResult result )
    {
        this.result = result;
    }

    void setPluginDescriptor( final PluginDescriptor descriptor )
    {
        this.descriptor = descriptor;
    }

    void setThrowable( final Throwable reason )
    {
        this.reason = reason;
        result = PluginActivationResult.BROKEN;
    }
}
