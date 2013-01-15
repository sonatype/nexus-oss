/*
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
package org.sonatype.security.realms.kenai;

import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KenaiProjectsJsonGenerator
{

    private int pageSize = 10;

    private int totalProjects = 22;

    private int totalPages = 3;

    private String baseUrl = "https://java.net";

    public KenaiProjectsJsonGenerator( int pageSize, int totalProjects, String baseUrl )
    {
        this.pageSize = pageSize;
        this.totalProjects = totalProjects;
        this.baseUrl = baseUrl;

        if ( baseUrl.endsWith( "/" ) )
        {
            this.baseUrl = baseUrl.substring( 0, baseUrl.length() - 1 );
        }

        if ( this.totalProjects % this.pageSize != 0 )
        {
            totalPages = this.totalProjects / this.pageSize;
            totalPages++;
        }
        else
        {
            totalPages = this.totalProjects / this.pageSize;
        }
    }

    public String generate( int startPage, String userId, String... roles )
        throws JSONException
    {

        StringBuffer urlPart = new StringBuffer();
        if ( roles != null && roles.length > 0 )
        {
            urlPart.append( "roles=" );

            for ( Iterator<String> iter = Arrays.asList( roles ).iterator(); iter.hasNext(); )
            {
                urlPart.append( iter.next() );
                if ( iter.hasNext() )
                {
                    urlPart.append( "%2C" );
                }
            }
        }
        urlPart.append( "&size=" ).append( pageSize );
        urlPart.append( "&theme=java.net" );
        urlPart.append( "&username=" ).append( userId );

        JSONObject root = new JSONObject();
        root.put( "href", this.baseUrl + "/api/projects?" + urlPart.toString() );

        if ( startPage > 1 )
        {
            root.put( "prev", this.baseUrl + "/api/projects?page=" + ( startPage - 1 ) + "&" + urlPart );
        }
        else
        {
            root.put( "prev", JSONObject.NULL );
        }

        if ( startPage < totalPages )
        {
            root.put( "next", this.baseUrl + "/api/projects?page=" + ( startPage + 1 ) + "&" + urlPart );
        }
        else
        {
            root.put( "next", JSONObject.NULL );
        }

        root.put( "total", totalProjects );

        JSONArray projectArray = new JSONArray();
        root.put( "projects", projectArray );

        // NOTE: page index starts at '1' not '0'
        int firstProjectOnNextPage = ( ( startPage - 1 ) * pageSize ) + pageSize;
        for ( int count = ( startPage - 1 ) * pageSize; count < totalProjects && count < firstProjectOnNextPage; count++ )
        {
            projectArray.put( this.buildProject( count ) );
        }

        root.put( "content_type", "application/vnd.com.kenai.projects+json" );

        return root.toString( 2 );
    }

    private JSONObject buildProject( int number )
        throws JSONException
    {
        String projectId = "project-" + number;

        JSONObject project = new JSONObject();
        project.put( "href", this.baseUrl + "/api/projects/" + projectId + ".json" );
        project.put( "features_href", this.baseUrl + "/api/projects/" + projectId + "/features.json" );
        project.put( "members_href", this.baseUrl + "/api/projects/" + projectId + "/members.json" );
        project.put( "name", projectId );
        project.put( "display_name", projectId );
        project.put( "image", this.baseUrl + "/images/defaultProjectImage.jpg" );
        project.put( "web_url", this.baseUrl + "/projects/" + projectId );
        project.put( "tags", "" );

        return project;
    }

}
