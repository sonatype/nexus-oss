package org.sonatype.nexus.configuration.security.upgrade;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.security.model.upgrade.SecurityDataUpgrader;
import org.sonatype.security.realms.tools.ConfigurationManager;

@Component( role = EventInspector.class, hint = "SecurityUpgradeEventInspector" )
public class SecurityUpgradeEventInspector
    extends AbstractLogEnabled
    implements EventInspector
{

    @Requirement
    private ApplicationStatusSource applicationStatusSource;

    @Requirement( hint = "file" )
    private SecurityModelConfigurationSource configSource;

    /**
     * Reuse the previous versions upgrader, this is normally run after the module upgrade of 2.0.1, so the module is
     * actually 2.0.2.
     */
    @Requirement( hint = "2.0.1" )
    private SecurityDataUpgrader upgrader;

    @Requirement
    private ApplicationEventMulticaster eventMulticaster;

    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof NexusStartedEvent )
        {
            return true;
        }
        return false;
    }

    public void inspect( Event<?> evt )
    {
        NexusStartedEvent startedEvent = (NexusStartedEvent) evt;

        try
        {
            // re/load the config from file
            this.configSource.loadConfiguration();

            // if Nexus was upgraded and the security version is 2.0.2 we need to update the model
            // NOTE: once the security version changes we no longer need this class
            Configuration securityConfig = this.configSource.getConfiguration();
            if ( this.applicationStatusSource.getSystemStatus().isConfigurationUpgraded()
                && securityConfig.getVersion().equals( "2.0.2" ) )
            {

                // first get the config and upgrade it

                this.upgrader.upgrade( configSource.getConfiguration() );

                // now save
                configSource.storeConfiguration();

                // because we change the configuration directly we need to tell the SecuritySystem to clear the cache,
                // although at this point nothing should be cached, but better safe the sorry
                this.eventMulticaster.notifyEventListeners( new SecurityConfigurationChangedEvent( null ) );

            }
        }
        catch ( ConfigurationIsCorruptedException e )
        {
            this.getLogger().error( "Failed to upgrade security.xml: " + e );
            startedEvent.putVeto( this, e );
        }
        catch ( ConfigurationException e )
        {
            this.getLogger().error( "Failed to upgrade security.xml: " + e );
            startedEvent.putVeto( this, e );
        }
        catch ( IOException e )
        {
            this.getLogger().error( "Failed to upgrade security.xml: " + e );
            startedEvent.putVeto( this, e );
        }
    }
}
