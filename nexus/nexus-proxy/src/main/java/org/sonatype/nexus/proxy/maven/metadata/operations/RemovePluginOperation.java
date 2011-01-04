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

import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;

/**
 * removes a Plugin from Metadata
 * 
 * @author Oleg Gusakov
 * @version $Id: RemovePluginOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class RemovePluginOperation
    implements MetadataOperation
{

    private Plugin plugin;

    /**
     * @throws MetadataException
     */
    public RemovePluginOperation( PluginOperand data )
        throws MetadataException
    {
        setOperand( data );
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
     * remove version to the in-memory metadata instance
     * 
     * @param metadata
     * @param version
     * @return
     */
    public boolean perform( Metadata metadata )
        throws MetadataException
    {
        if ( metadata == null )
        {
            return false;
        }

        List<Plugin> plugins = metadata.getPlugins();

        if ( plugins != null && plugins.size() > 0 )
        {
            for ( Iterator<Plugin> pi = plugins.iterator(); pi.hasNext(); )
            {
                Plugin p = pi.next();

                if ( p.getArtifactId().equals( plugin.getArtifactId() ) )
                {
                    pi.remove();

                    return true;
                }
            }
        }

        return false;
    }
}
