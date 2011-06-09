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
package util;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;
import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptResponseDTO;
import com.thoughtworks.xstream.XStream;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.*;

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
        String responseText = RequestFacade.doGetForText("service/local/groovy_console", isSuccessful());

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
