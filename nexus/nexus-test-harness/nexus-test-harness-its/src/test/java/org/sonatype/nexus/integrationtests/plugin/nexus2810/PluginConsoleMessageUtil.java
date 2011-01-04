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
package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoListResponseDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.RestInfoDTO;
import org.sonatype.nexus.test.utils.XStreamConfigurator;
import org.sonatype.nexus.test.utils.plugin.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

public class PluginConsoleMessageUtil
{
    private static final String PLUGIN_INFOS_URL = "service/local/plugin_console/plugin_infos";

    private static XStream xmlXstream;

    private static final Logger LOGGER = Logger.getLogger( PluginConsoleMessageUtil.class );

    static
    {
        xmlXstream = XStreamFactory.getXmlXStream( new XStreamConfigurator()
        {
            public void configure( XStream xstream )
            {
                xstream.processAnnotations( PluginInfoDTO.class );
                xstream.processAnnotations( PluginInfoListResponseDTO.class );

                xstream.registerLocalConverter( PluginInfoListResponseDTO.class, "data", new AliasingListConverter(
                    PluginInfoDTO.class, "pluginInfo" ) );

                xstream.registerLocalConverter( PluginInfoDTO.class, "restInfos", new AliasingListConverter( RestInfoDTO.class,
                    "restInfo" ) );
            }
        });
    }

    public List<PluginInfoDTO> listPluginInfos()
        throws IOException
    {
        String serviceURI = PLUGIN_INFOS_URL;

        LOGGER.info( "HTTP GET: " + serviceURI );

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );

        if ( response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();

            LOGGER.debug( "Response Text: \n" + responseText );

            XStreamRepresentation representation = new XStreamRepresentation(
                xmlXstream,
                responseText,
                MediaType.APPLICATION_XML );

            PluginInfoListResponseDTO responseDTO = (PluginInfoListResponseDTO) representation
                .getPayload( new PluginInfoListResponseDTO() );

            return responseDTO.getData();
        }
        else
        {
            LOGGER.warn( "HTTP Error: '" + response.getStatus().getCode() + "'" );

            LOGGER.warn( response.getEntity().getText() );

            return null;
        }
    }

}
