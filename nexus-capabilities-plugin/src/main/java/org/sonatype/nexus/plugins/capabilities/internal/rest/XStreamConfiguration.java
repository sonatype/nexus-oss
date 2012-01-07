/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.plugins.capabilities.internal.rest;

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;
import com.thoughtworks.xstream.XStream;

public class XStreamConfiguration
{

    public static XStream applyTo( final XStream xstream )
    {
        xstream.registerConverter( new CapabilityPropertyResourceConverter(
            xstream.getMapper(),
            xstream.getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );

        xstream.processAnnotations( CapabilityRequestResource.class );
        xstream.processAnnotations( CapabilityResponseResource.class );
        xstream.processAnnotations( CapabilitiesListResponseResource.class );
        xstream.processAnnotations( CapabilityStatusResponseResource.class );

        xstream.registerLocalConverter( CapabilityResource.class, "properties", new AliasingListConverter(
            CapabilityPropertyResource.class, "feature-property" ) );

        return xstream;
    }

}
