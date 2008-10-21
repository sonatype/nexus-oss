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
package org.sonatype.nexus.rest.groups;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class RepositoryGroupResponseTest
    extends AbstractRestTestCase
{

    public void testRepoGroup()
        throws Exception
    {
        String jsonString = "{ \"org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse\" : {\"data\":{\"id\":\"public-releases\",\"name\":\"public-releases11\",\"repositories\":[{\"@class\":\"org.sonatype.nexus.rest.model.RepositoryGroupResource\",\"id\":\"extFree\",\"name\":\"Modified OSS\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/extFree\"},{\"@class\":\"org.sonatype.nexus.rest.model.RepositoryGroupResource\",\"id\":\"extNonFree\",\"name\":\"Commerical\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/extNonFree\"},{\"@class\":\"org.sonatype.nexus.rest.model.RepositoryGroupResource\",\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/central\"},{\"@class\":\"org.sonatype.nexus.rest.model.RepositoryGroupResource\",\"id\":\"codehaus\",\"name\":\"Codehaus\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/codehaus\"},{\"@class\":\"org.sonatype.nexus.rest.model.RepositoryGroupResource\",\"id\":\"maven2-repository.dev.java.net\",\"name\":\"Java dot NET\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/maven2-repository.dev.java.net\"}]}}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        RepositoryGroupListResourceResponse response = (RepositoryGroupListResourceResponse) representation
            .getPayload( new RepositoryGroupListResourceResponse() );
    }

}
