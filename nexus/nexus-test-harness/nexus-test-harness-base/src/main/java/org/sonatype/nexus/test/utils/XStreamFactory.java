/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.test.utils;

import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{
    public static XStream getXmlXStream()
    {
        XStream xs = new XStream( new LookAheadXppDriver() );

        NexusApplication napp = new NexusApplication();

        napp.doConfigureXstream( xs );

        return xs;
    }

    public static XStream getJsonXStream()
    {
        XStream xs = new XStream( new JsonOrgHierarchicalStreamDriver() );

        // for JSON, we use a custom converter for Maps
        xs.registerConverter( new PrimitiveKeyedMapConverter( xs.getMapper() ) );

        NexusApplication napp = new NexusApplication();

        napp.doConfigureXstream( xs );

        return xs;
    }

}
