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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import static org.sonatype.nexus.proxy.maven.metadata.operations.MetadataUtil.isPluginEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;

/**
 * adds new plugin to metadata
 * 
 * @author Oleg Gusakov
 * @version $Id: AddPluginOperation.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class AddPluginOperation
    implements MetadataOperation
{
    private Plugin plugin;

    private static PluginComparator pluginComparator;

    {
        pluginComparator = new PluginComparator();
    }

    /**
     * @throws MetadataException
     */
    public AddPluginOperation( PluginOperand data )
        throws MetadataException
    {
        if ( data == null )
        {
            throw new MetadataException( "Operand is not correct: cannot accept null!" );
        }

        this.plugin = data.getOperand();
    }

    public void setOperand( AbstractOperand data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof PluginOperand ) )
        {
            throw new MetadataException( "Operand is not correct: expected PluginOperand, but got "
                + ( data == null ? "null" : data.getClass().getName() ) );
        }

        plugin = ( (PluginOperand) data ).getOperand();
    }

    /**
     * add plugin to the in-memory metadata instance
     * 
     * @param metadata
     * @param version
     * @return
     * @throws MetadataException
     */
    public boolean perform( Metadata metadata )
        throws MetadataException
    {
        if ( metadata == null )
        {
            return false;
        }

        List<Plugin> plugins = metadata.getPlugins();

        for ( Plugin p : plugins )
        {
            if ( p.getArtifactId().equals( plugin.getArtifactId() ) )
            {
                if ( isPluginEquals( p, plugin ) )
                {
                    // plugin already enlisted
                    return false;
                }
            }
        }

        // not found, add it
        plugins.add( plugin );
        Collections.sort( plugins, pluginComparator );
        return true;
/*
        // We have a large hack happening here
        // Originally, the code was:

        // plugins.add( plugin );
        // Collections.sort( plugins, pluginComparator );
        // return true;

        // This above resulted with "batch" operations (like Nexus metadata merge when group serves up Maven metadata)
        // high CPU usage and high response times. What was happening that Collections.sort() was invoked over and over
        // for every insertion of one new plugin.

        // Solution: we sort the list once, probably 1st time we got here -- and we "mark" that fact by using a
        // ArrayList2 class. Then, we _keep_ that sorted list _stable_, by using binarySearch on it for insertions.
        // Thus, we kept the semantics of previous solution but at
        // much less CPU and computational expense.

        if ( !( plugins instanceof ArrayList2 ) )
        {
            Collections.sort( plugins, pluginComparator );

            metadata.setPlugins( new ArrayList2( plugins ) );

            plugins = metadata.getPlugins();
        }

        final int index = Collections.binarySearch( plugins, plugin, pluginComparator );

        // um, this checks seems unnecessary, since we already checked for contains() above,
        // so if we are here, we _know_ the version to be added is NOT in the list
        if ( index < 0 )
        {
            // vs.addVersion( version );
            plugins.add( -index - 1, plugin );

            return true;
        }
        else
        {
            // we should never arrive here, se above if()
            return false;
        }
        */
    }

    class PluginComparator
        implements Comparator<Plugin>
    {
        public int compare( Plugin p1, Plugin p2 )
        {
            if ( p1 == null || p2 == null )
            {
                throw new IllegalArgumentException();
            }

            if ( p1.getArtifactId() == null || p2.getArtifactId() == null )
            {
                throw new IllegalArgumentException();
            }

            return p1.getArtifactId().compareTo( p2.getArtifactId() );
        }
    }

    // == A HACK

    public static class ArrayList2
        extends ArrayList<Plugin>
    {
        public ArrayList2( Collection<? extends Plugin> c )
        {
            super( c );
        }
    }
}
