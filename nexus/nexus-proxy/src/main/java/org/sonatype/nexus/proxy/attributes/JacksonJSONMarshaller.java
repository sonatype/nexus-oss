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
package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sonatype.nexus.proxy.attributes.internal.DefaultAttributes;

/**
 * Jackson JSON Attribute marshaller. Part of NEXUS-4628 "alternate" AttributeStorage implementations.
 */
public class JacksonJSONMarshaller
    implements Marshaller
{
    private final ObjectMapper objectMapper;

    public JacksonJSONMarshaller()
    {
        this.objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public void marshal( final Attributes item, final OutputStream outputStream )
        throws IOException
    {
        final Map<String, String> attrs = new HashMap<String, String>( item.asMap() );
        objectMapper.writeValue( outputStream, attrs );
        outputStream.flush();
    }

    @Override
    public Attributes unmarshal( final InputStream inputStream )
        throws IOException, InvalidInputException
    {
        try
        {
            final Map<String, String> attributesMap =
                objectMapper.readValue( inputStream, new TypeReference<Map<String, String>>()
                {
                } );
            return new DefaultAttributes( attributesMap );
        }
        catch ( JsonParseException e )
        {
            throw new InvalidInputException( "Persisted attribute malformed!", e );
        }
    }

    // ==

    public String toString()
    {
        return "JacksonJSON";
    }
}
