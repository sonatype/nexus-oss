package org.sonatype.nexus.proxy.maven;

import org.codehaus.plexus.util.StringUtils;

public class GAVRequest
{
    private String groupId;

    private String artifactId;

    private String version;

    private String packaging;

    private String classifier;

    public GAVRequest( String g, String a, String v )
    {
        super();

        if ( StringUtils.isEmpty( g ) || StringUtils.isEmpty( a ) || StringUtils.isEmpty( v ) )
        {
            throw new IllegalArgumentException( "None of the GAV dimensions can be null or empty!" );
        }

        setGroupId( g );

        setArtifactId( a );

        setVersion( v );

        setPackaging( "jar" );

        setClassifier( null );
    }

    public GAVRequest( String g, String a, String v, String p, String c )
    {
        this( g, a, v );

        if ( !StringUtils.isEmpty( c ) )
        {
            setClassifier( c );
        }

        if ( !StringUtils.isEmpty( p ) )
        {
            setPackaging( p );
        }
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( getGroupId() );
        sb.append( ":" );
        sb.append( getArtifactId() );
        sb.append( ":" );
        sb.append( getPackaging() );
        sb.append( ":" );
        sb.append( getClassifier() );
        sb.append( ":" );
        sb.append( getVersion() );

        return sb.toString();
    }

}
