package org;

import java.net.URL;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.building.UrlModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

public class SimpleModelResolver
    implements ModelResolver
{

    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        // no-op, we don't care about POM repos right now
    }

    public ModelResolver newCopy()
    {
        // this class is stateless so can be reused
        return this;
    }

    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        // do your repo magic here

        String resource = "/repo/" + groupId + '/' + artifactId + '/' + version + "/pom.xml";

        URL url = getClass().getResource( resource );

        if ( url == null )
        {
            throw new UnresolvableModelException( "PANIC!", groupId, artifactId, version );
        }

        return new UrlModelSource( url );
    }

}
