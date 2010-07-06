package org.sonatype.nexus.rest.artifact;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResource;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResourceRespose;

/**
 * Returns Maven2 artifact information.
 * 
 * @author Brian Demers
 */
@Component( role = ArtifactViewProvider.class, hint = "maven2" )
public class Maven2ArtifactContentProvider
    implements ArtifactViewProvider
{

    @Requirement
    private M2GavCalculator m2GavCalculator;

    public Object retrieveView( ResourceStoreRequest storeRequest )
        throws StorageException
    {
        Gav gav;
        try
        {
            gav = m2GavCalculator.pathToGav( storeRequest.getRequestPath() );

            Maven2ArtifactInfoResourceRespose response = new Maven2ArtifactInfoResourceRespose();
            Maven2ArtifactInfoResource data = new Maven2ArtifactInfoResource();
            response.setData( data );

            data.setGroupId( gav.getGroupId() );
            data.setArtifactId( gav.getArtifactId() );
            data.setVersion( gav.getVersion() );
            data.setExtention( gav.getExtension() );
            data.setClassifier( gav.getClassifier() );
            
            data.setDependencyXmlChunk( generateDependencyXml( gav ) );

            return response;

        }
        catch ( IllegalArtifactCoordinateException e )
        {
            throw new StorageException( "Failed to resolve maven2 gav from path: " + storeRequest.getRequestPath() );
        }
    }

    private String generateDependencyXml( Gav gav )
    {

        StringBuffer buffer = new StringBuffer();
        buffer.append( "<dependency>\n" );
        buffer.append( "  <groupId>" ).append( gav.getGroupId() ).append( "</groupId>\n" );
        buffer.append( "  <groupId>" ).append( gav.getArtifactId() ).append( "</groupId>\n" );
        buffer.append( "  <version>" ).append( gav.getVersion() ).append( "</version>\n" );

        if( StringUtils.isNotEmpty( gav.getClassifier() ) )
        {
            buffer.append( "  <classifier>" ).append( gav.getClassifier() ).append( "</classifier>\n" );
        }
        
        if ( StringUtils.isNotEmpty( gav.getExtension() ) && !StringUtils.equalsIgnoreCase( "jar", gav.getExtension() ) )
        {
            buffer.append( "  <type>" ).append( gav.getExtension() ).append( "</type>\n" );
        }

        buffer.append( "</dependency>" );

        return buffer.toString();
    }

}
