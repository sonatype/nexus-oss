package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.v1_0_8.upgrade.BasicVersionUpgrade;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.upgrade.Upgrader;

@Component( role = Upgrader.class, hint = "1.0.7" )
public class Upgrade107to108
    extends AbstractLogEnabled
    implements Upgrader
{

    private BasicVersionUpgrade converter = new BasicVersionUpgrade() {
        @Override
        public org.sonatype.nexus.configuration.model.CRepositoryGroup upgradeCRepositoryGroup(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup repositoryGroup, org.sonatype.nexus.configuration.model.CRepositoryGroup value )
        {
            org.sonatype.nexus.configuration.model.CRepositoryGroup group = super.upgradeCRepositoryGroup( repositoryGroup, value );
            group.setType( "maven2" );
            return group;
        }
    };

    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        org.sonatype.nexus.configuration.model.v1_0_7.Configuration conf = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            org.sonatype.nexus.configuration.model.v1_0_7.io.xpp3.NexusConfigurationXpp3Reader reader = new org.sonatype.nexus.configuration.model.v1_0_7.io.xpp3.NexusConfigurationXpp3Reader();

            conf = reader.read( fr );
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }

        return conf;
    }

    public void upgrade( UpgradeMessage message )
    {
        org.sonatype.nexus.configuration.model.v1_0_7.Configuration oldc = (org.sonatype.nexus.configuration.model.v1_0_7.Configuration) message.getConfiguration();

        org.sonatype.nexus.configuration.model.Configuration newc = converter.upgradeConfiguration( oldc );

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

}
