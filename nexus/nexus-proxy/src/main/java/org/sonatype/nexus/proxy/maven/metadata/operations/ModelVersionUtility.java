package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.apache.maven.artifact.repository.metadata.Metadata;

/**
 * Handling model version of Maven repository metadata, with some rudimentary "version detection".
 * 
 * @author cstamas
 */
public class ModelVersionUtility
{
    public static final Version LATEST_MODEL_VERSION = Version.values()[Version.values().length - 1];

    public enum Version
    {
        V100,

        V110;
    }

    public static Version getModelVersion( final Metadata metadata )
    {
        if ( "1.1.0".equals( metadata.getModelVersion() ) )
        {
            return Version.V110;
        }
        else
        {
            return Version.V100;
        }
    }

    public static void setModelVersion( final Metadata metadata, final Version version )
    {
        switch ( version )
        {
            case V100:
                metadata.setModelVersion( null );
                metadata.getVersioning().setSnapshotVersions( null );
                break;

            case V110:
                metadata.setModelVersion( "1.1.0" );
                break;
        }
    }
}
