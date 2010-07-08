package org.sonatype.nexus.rest.artifact;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResource;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResourceRespose;

/**
 * Returns Maven2 artifact information.
 * 
 * @author Brian Demers
 */
@Component( role = ArtifactViewProvider.class, hint = "maven2" )
public class Maven2ArtifactViewProvider
    implements ArtifactViewProvider
{
    @Requirement
    private Logger logger;
    
    @Requirement
    private M2GavCalculator m2GavCalculator;

    public Object retrieveView( StorageItem item )
        throws IOException
    {
        Gav gav;
        try
        {
            gav = m2GavCalculator.pathToGav( item.getRepositoryItemUid().getPath() );

            Maven2ArtifactInfoResourceRespose response = new Maven2ArtifactInfoResourceRespose();
            Maven2ArtifactInfoResource data = new Maven2ArtifactInfoResource();
            response.setData( data );

            data.setGroupId( gav.getGroupId() );
            data.setArtifactId( gav.getArtifactId() );
            data.setBaseVersion( gav.getBaseVersion() );
            data.setVersion( gav.getVersion() );
            data.setExtension( gav.getExtension() );
            data.setClassifier( gav.getClassifier() );

            data.setDependencyXmlChunk( generateDependencyXml( gav ) );

            return response;

        }
        catch ( IllegalArtifactCoordinateException e )
        {
            this.logger.debug( "Failed to calculate maven 2 path." );

            // not maven, return null
            return null;
        }
    }

    private String generateDependencyXml( Gav gav )
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append( "<dependency>\n" );
        buffer.append( "  <groupId>" ).append( gav.getGroupId() ).append( "</groupId>\n" );
        buffer.append( "  <artifactId>" ).append( gav.getArtifactId() ).append( "</artifactId>\n" );
        buffer.append( "  <version>" ).append( gav.getBaseVersion() ).append( "</version>\n" );

        if ( StringUtils.isNotEmpty( gav.getClassifier() ) )
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
