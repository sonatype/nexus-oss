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
package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.fasterxml.jackson.xml.XmlMapper;

/**
 * Jackson XML backed Attribute marshaller. Part of NEXUS-4628 "alternate" AttributeStorage implementations.
 */
@Singleton
@Named( "jackson-xml" )
public class JacksonXMLMarshaller
    implements Marshaller
{
    private final ObjectMapper objectMapper;

    public JacksonXMLMarshaller()
    {
        this.objectMapper = new XmlMapper();
        objectMapper.setVisibility( JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY ) // auto-detect all member fields
        .setVisibility( JsonMethod.GETTER, JsonAutoDetect.Visibility.NONE ) // but only public getters
        .setVisibility( JsonMethod.IS_GETTER, JsonAutoDetect.Visibility.NONE ); // and none of "is-setters"

        objectMapper.disable( DeserializationConfig.Feature.AUTO_DETECT_SETTERS );
        objectMapper.disable( SerializationConfig.Feature.AUTO_DETECT_GETTERS );
        objectMapper.disable( SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS );

        objectMapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public void marshal( final StorageItem item, final OutputStream outputStream )
        throws IOException
    {
        objectMapper.writeValue( outputStream, item );
        outputStream.flush();
    }

    @Override
    public StorageItem unmarshal( final InputStream inputStream )
        throws IOException
    {
        return objectMapper.readValue( inputStream, DefaultStorageFileItem.class );
    }

}
