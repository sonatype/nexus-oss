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
