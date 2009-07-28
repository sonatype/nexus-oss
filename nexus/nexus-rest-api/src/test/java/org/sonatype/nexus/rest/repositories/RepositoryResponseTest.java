/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
        String jsonString = "{\"data\" : {\"writePolicy\":\""+RepositoryWritePolicy.ALLOW_WRITE.name()+"\", \"browseable\":true,\"defaultLocalStorageUrl\":null,\"id\":\"test1\", \"indexable\":true,\"name\":\"test1\",\"notFoundCacheTTL\":1440,\"overrideLocalStorageUrl\":null,\"repoPolicy\":\"release\", \"repoType\":\"hosted\"}}";

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
        String jsonString = "{\"data\" : {\"writePolicy\":\""+RepositoryWritePolicy.ALLOW_WRITE.name()+"\", \"artifactMaxAge\":1440,\"browseable\":true,\"defaultLocalStorageUrl\":null,\"id\":\"test1\", \"indexable\":true,\"metadataMaxAge\":1440,\"name\":\"test1\",\"notFoundCacheTTL\":1440,\"overrideLocalStorageUrl\":null,\"repoPolicy\":\"release\", \"repoType\":\"proxy\"}}";

        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        RepositoryResourceResponse response = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

    }

}
