/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.users;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.PlexusComponentListResource;
import org.sonatype.security.rest.model.PlexusComponentListResourceResponse;

import com.thoughtworks.xstream.XStream;

/**
 * Tests for UserLocatorComponentListPlexusResource.
 */
public class UserLocatorComponentListTest
    extends AbstractSecurityRestTest
{

    public void testGet()
        throws Exception
    {
        PlexusResource resource = this.lookup( PlexusResource.class, "UserLocatorComponentListPlexusResource" );
        Object result = resource.get( null, null, null, null );
        assertThat( result, instanceOf( PlexusComponentListResourceResponse.class ) );

        PlexusComponentListResourceResponse response = (PlexusComponentListResourceResponse) result;

        assertThat( "Result: " + new XStream().toXML( response ), response.getData().size(), equalTo( 5 ) );

        Map<String, String> data = new HashMap<String, String>();
        for ( PlexusComponentListResource item : response.getData() )
        {
            data.put( item.getRoleHint(), item.getDescription() );
        }

        assertThat( data.keySet(),
                    containsInAnyOrder( "default", "allConfigured", "MockUserManagerA", "MockUserManagerB", "Mock" ) );
        assertThat( data.get( "default" ), equalTo( "Default" ) );
        assertThat( data.get( "allConfigured" ), equalTo( "All Configured Users" ) );
        assertThat( data.get( "MockUserManagerA" ), equalTo( "MockUserManagerA" ) );
    }

}
