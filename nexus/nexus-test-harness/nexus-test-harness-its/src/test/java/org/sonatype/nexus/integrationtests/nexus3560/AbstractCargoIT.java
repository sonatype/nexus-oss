package org.sonatype.nexus.integrationtests.nexus3560;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.sonatype.nexus.test.utils.NexusIllegalStateException;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractCargoIT
{

    private InstalledLocalContainer container;

    public AbstractCargoIT()
    {
        super();
    }

    public abstract String getContainer();

    public abstract File getContainerLocation();

    public File getWarFile()
    {
        return new File( "target/nexus/nexus-webapp-" + TestProperties.getString( "project.version" ) + ".war" );
    }

    @BeforeClass
    public void startContainer()
    {
        WAR war = new WAR( getWarFile().getAbsolutePath() );
        war.setContext( "nexus" );

        ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
        LocalConfiguration configuration =
            (LocalConfiguration) configurationFactory.createConfiguration( getContainer(), ContainerType.INSTALLED,
                ConfigurationType.STANDALONE );
        configuration.addDeployable( war );
        configuration.setProperty( ServletPropertySet.PORT, TestProperties.getString( "nexus.application.port" ) );

        container =
            (InstalledLocalContainer) new DefaultContainerFactory().createContainer( getContainer(),
                ContainerType.INSTALLED, configuration );
        container.setHome( getContainerLocation().getAbsolutePath() );

        container.start();
    }

    @AfterClass( alwaysRun = true )
    public void stopContainer()
    {
        if ( container != null )
        {
            container.stop();
        }
    }

    @Test
    public void checkStatus()
        throws NexusIllegalStateException
    {
        assertEquals( new NexusStatusUtil().getNexusStatus().getData().getState(), "STARTED" );
    }

}