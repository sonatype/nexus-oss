package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.metadata.AddPluginOperation;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.mercury.repository.metadata.PluginOperand;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;

public class DefaultMetadataUpdater
    extends AbstractLogEnabled
    implements MetadataUpdater
{

    public void deployArtifact( ArtifactStoreRequest request, MetadataLocator locator )
        throws IOException
    {
        if ( !StringUtils.isEmpty( request.getClassifier() ) )
        {
            // artifacts with classifiers have no metadata
            return;
        }

        try
        {
            List<MetadataOperation> operations = null;

            Gav gav = locator.getGavForRequest( request );

            // GAV

            Metadata gavMd = locator.retrieveGAVMetadata( request );

            operations = new ArrayList<MetadataOperation>();

            // simply making it foolproof?
            gavMd.setGroupId( gav.getGroupId() );

            gavMd.setArtifactId( gav.getArtifactId() );

            gavMd.setVersion( gav.getBaseVersion() );

            if ( gav.isSnapshot() )
            {
                operations.add( new SetSnapshotOperation( new SnapshotOperand( MetadataBuilder.createSnapshot( request
                    .getVersion() ) ) ) );
            }

            MetadataBuilder.changeMetadata( gavMd, operations );

            locator.storeGAVMetadata( request, gavMd );

            // GA

            operations = new ArrayList<MetadataOperation>();

            Metadata gaMd = locator.retrieveGAMetadata( request );

            operations.add( new AddVersionOperation( new StringOperand( request.getVersion() ) ) );

            MetadataBuilder.changeMetadata( gaMd, operations );

            locator.storeGAMetadata( request, gaMd );

            // G (if is plugin)

            operations = new ArrayList<MetadataOperation>();

            if ( "maven-plugin".equals( request.getPackaging() ) )
            {
                Metadata gMd = locator.retrieveGMetadata( request );

                Plugin pluginElem = locator.extractPluginElementFromPom( request );

                if ( pluginElem != null )
                {
                    operations.add( new AddPluginOperation( new PluginOperand( pluginElem ) ) );

                    MetadataBuilder.changeMetadata( gMd, operations );

                    locator.storeGMetadata( request, gMd );
                }

            }
        }
        catch ( MetadataException e )
        {
            // ?
        }
    }

    public void undeployArtifact( ArtifactStoreRequest request, MetadataLocator locator )
        throws IOException
    {
        if ( !StringUtils.isEmpty( request.getClassifier() ) )
        {
            // artifacts with classifiers have no metadata
            return;
        }

        try
        {
            List<MetadataOperation> operations = null;

            Gav gav = locator.getGavForRequest( request );

            // GAV

            Metadata gavMd = locator.retrieveGAVMetadata( request );

            operations = new ArrayList<MetadataOperation>();

            // simply making it foolproof?
            gavMd.setGroupId( gav.getGroupId() );

            gavMd.setArtifactId( gav.getArtifactId() );

            gavMd.setVersion( gav.getBaseVersion() );

            if ( gav.isSnapshot() )
            {
                operations.add( new SetSnapshotOperation( new SnapshotOperand( MetadataBuilder.createSnapshot( request
                    .getVersion() ) ) ) );
            }

            MetadataBuilder.changeMetadata( gavMd, operations );

            locator.storeGAVMetadata( request, gavMd );

            // GA

            operations = new ArrayList<MetadataOperation>();

            Metadata gaMd = locator.retrieveGAMetadata( request );

            operations.add( new AddVersionOperation( new StringOperand( request.getVersion() ) ) );

            MetadataBuilder.changeMetadata( gaMd, operations );

            locator.storeGAMetadata( request, gaMd );

            // G (if is plugin)

            operations = new ArrayList<MetadataOperation>();

            if ( "maven-plugin".equals( request.getPackaging() ) )
            {
                Metadata gMd = locator.retrieveGMetadata( request );

                Plugin pluginElem = locator.extractPluginElementFromPom( request );

                if ( pluginElem != null )
                {
                    operations.add( new AddPluginOperation( new PluginOperand( pluginElem ) ) );

                    MetadataBuilder.changeMetadata( gMd, operations );

                    locator.storeGMetadata( request, gMd );
                }

            }
        }
        catch ( MetadataException e )
        {
            // ?
        }    }

    public void deployArtifacts( Collection<ArtifactStoreRequest> requests, MetadataLocator locator )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

}
