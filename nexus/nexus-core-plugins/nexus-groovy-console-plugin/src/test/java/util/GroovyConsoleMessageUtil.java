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
package util;

import static org.sonatype.nexus.test.utils.NexusRequestMatchers.*;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;
import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptResponseDTO;
import com.thoughtworks.xstream.XStream;

public class GroovyConsoleMessageUtil
{

    private static final XStream xs;
    static
    {
        xs = XStreamFactory.getXmlXStream();
        xs.processAnnotations( GroovyScriptDTO.class );
        xs.processAnnotations( GroovyScriptResponseDTO.class );
    }

    public static List<GroovyScriptDTO> getScripts()
        throws IOException
    {
        String responseText = RequestFacade.doGetForText( "service/local/groovy_console" );

        XStreamRepresentation representation = new XStreamRepresentation( xs, responseText, MediaType.APPLICATION_XML );
        GroovyScriptResponseDTO listRepsonse =
            (GroovyScriptResponseDTO) representation.getPayload( new GroovyScriptResponseDTO() );

        return listRepsonse.getData();

    }

    public static void addScript( GroovyScriptDTO groovyScriptDTO )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xs, "", MediaType.APPLICATION_XML );
        representation.setPayload( groovyScriptDTO );
        // FIXME use logger?
        System.out.println( xs.toXML( groovyScriptDTO ) );

        RequestFacade.doPost("service/local/groovy_console", representation, isSuccessful());

    }
}
