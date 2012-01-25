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

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class CapabilityPropertyResourceConverter
    extends AbstractReflectionConverter
{

    public CapabilityPropertyResourceConverter( final Mapper mapper, final ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( final Class type )
    {
        return CapabilityPropertyResource.class.equals( type );
    }

    @Override
    public Object doUnmarshal( final Object source, final HierarchicalStreamReader reader,
                               final UnmarshallingContext context )
    {
        final CapabilityPropertyResource resource = (CapabilityPropertyResource) source;
        while ( reader.hasMoreChildren() )
        {
            reader.moveDown();
            if ( "key".equals( reader.getNodeName() ) )
            {
                resource.setKey( (String) context.convertAnother( source, String.class ) );
            }
            else if ( "value".equals( reader.getNodeName() ) )
            {
                resource.setValue( (String) context.convertAnother( source, String.class ) );
            }
            reader.moveUp();
        }
        return resource;
    }
}
