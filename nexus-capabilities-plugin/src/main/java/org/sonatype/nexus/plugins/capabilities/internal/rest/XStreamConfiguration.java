/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
