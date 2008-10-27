/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.repositories;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class RepositoryResponseTest
    extends AbstractRestTestCase
{

    public void testRepo()
        throws Exception
    {
        String jsonString = "{ \"repository\" : {\"data\" : {\"allowWrite\":true, \"browseable\":true,\"defaultLocalStorageUrl\":null,\"id\":\"test1\", \"indexable\":true,\"name\":\"test1\",\"notFoundCacheTTL\":1440,\"overrideLocalStorageUrl\":null,\"repoPolicy\":\"release\", \"repoType\":\"hosted\"}}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        RepositoryResourceResponse response = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

    }

    public void testProxyRepo()
        throws Exception
    {
        String jsonString = "{ \"repository\" : {\"data\" : {\"allowWrite\":true, \"artifactMaxAge\":1440,\"browseable\":true,\"defaultLocalStorageUrl\":null,\"id\":\"test1\", \"indexable\":true,\"metadataMaxAge\":1440,\"name\":\"test1\",\"notFoundCacheTTL\":1440,\"overrideLocalStorageUrl\":null,\"repoPolicy\":\"release\", \"repoType\":\"proxy\"}}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        RepositoryResourceResponse response = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

    }

}
