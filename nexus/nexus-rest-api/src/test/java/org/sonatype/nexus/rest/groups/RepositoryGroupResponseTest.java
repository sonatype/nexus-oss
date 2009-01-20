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
        String jsonString = "{\"data\":[{\"id\":\"public-releases\",\"name\":\"public-releases11\",\"repositories\":[{\"id\":\"extFree\",\"name\":\"Modified OSS\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/extFree\"},{\"id\":\"extNonFree\",\"name\":\"Commerical\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/extNonFree\"},{\"id\":\"central\",\"name\":\"Maven Central\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/central\"},{\"id\":\"codehaus\",\"name\":\"Codehaus\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/codehaus\"},{\"id\":\"maven2-repository.dev.java.net\",\"name\":\"Java dot NET\",\"resourceURI\":\"/nexus/service/local/repo_groups//repositories/maven2-repository.dev.java.net\"}]}]}";

        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        RepositoryGroupListResourceResponse response = (RepositoryGroupListResourceResponse) representation
            .getPayload( new RepositoryGroupListResourceResponse() );
    }

}
