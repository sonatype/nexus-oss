package org.sonatype.security.realms.kenai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KenaiProjectJsonGenerator
{

    private int pageSize = 10;

    private int totalProjects = 22;

    private int totalPages = 3;

    private String baseUrl = "https://java.net";

    public KenaiProjectJsonGenerator( int pageSize, int totalProjects, String baseUrl )
    {
        super();
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

    public String generate( int startPage )
        throws JSONException
    {

        JSONObject root = new JSONObject();
        root.put( "href", this.baseUrl + "/api/projects/mine.json?theme=java.net" );

        if ( startPage > 1 )
        {
            root.put( "prev", this.baseUrl + "/api/projects/mine.json?page=" + ( startPage - 1 ) );
        }
        else
        {
            root.put( "prev", JSONObject.NULL );
        }

        if ( startPage < totalPages )
        {
            root.put( "next", this.baseUrl + "/api/projects/mine.json?page=" + ( startPage + 1 ) );
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
