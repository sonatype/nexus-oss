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
package org.sonatype.nexus.plugins.lvo;

import java.io.IOException;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * The LVO Plugin interface.
 * 
 * @author cstamas
 */
public interface LvoPlugin
{
    /**
     * Returns the latest V (V from GAV) that fits the properties specified by key.
     * 
     * @param key
     * @return the V, null if key exists but we are unable to calculate LV.
     * @throws NoSuchKeyException
     * @throws NoSuchStrategyException
     * @throws NoSuchRepositoryException
     * @throws IOException
     */
    DiscoveryResponse getLatestVersionForKey( String key )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException;

    /**
     * Queries for the latest V (V from GAV) that fits the properties specified by key. If the passed in v is equal of
     * the latest v, returns null.
     * 
     * @param key
     * @param v current version associated with key.
     * @return the V if newer found, null if key exists but we are unable to calculate LV or no newer version exists.
     * @throws NoSuchKeyException
     * @throws NoSuchStrategyException
     * @throws NoSuchRepositoryException
     * @throws IOException
     */
    DiscoveryResponse queryLatestVersionForKey( String key, String v )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException;
}
