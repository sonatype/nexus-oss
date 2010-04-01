package org.sonatype.nexus.notification;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CNotification;
import org.sonatype.nexus.configuration.model.CNotificationConfiguration;
import org.sonatype.nexus.configuration.model.CNotificationTarget;

@Component( role = NotificationManager.class )
public class DefaultNotificationManager
    extends AbstractConfigurable
    implements NotificationManager
{
    @Requirement
    private Logger logger;

    @Requirement
    private NexusConfiguration nexusConfig;

    @Requirement( role = Carrier.class )
    private Map<String, Carrier> carriers;

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    public String getName()
    {
        return "Notification configuration";
    }

    @Override
    protected void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return nexusConfig;
    }

    @Override
    protected Configurator getConfigurator()
    {
        // not custom configurators needed
        return null;
    }

    @Override
    protected CNotification getCurrentConfiguration( boolean forWrite )
    {
        return ( (CNotificationConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CNotificationConfiguration( getApplicationConfiguration() );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    // ==
    public boolean isEnabled()
    {
        return getCurrentConfiguration( false ).isEnabled();
    }

    public void setEnabled( boolean val )
    {
        getCurrentConfiguration( true ).setEnabled( val );
    }

    public NotificationTarget readNotificationTarget( String targetId )
    {
        if ( targetId == null )
        {
            throw new NullPointerException( "Notification target ID can't be null!" );
        }

        // TODO: we always ask for this since we have this group only
        targetId = NotificationCheat.AUTO_BLOCK_NOTIFICATION_GROUP_ID;

        List<CNotificationTarget> targets = getCurrentConfiguration( false ).getNotificationTargets();

        for ( CNotificationTarget target : targets )
        {
            if ( targetId.equals( target.getTargetId() ) )
            {
                NotificationTarget result = new NotificationTarget();

                result.setTargetId( target.getTargetId() );

                result.getTargetRoles().addAll( target.getTargetRoles() );

                result.getTargetUsers().addAll( target.getTargetUsers() );

                result.getExternalTargets().addAll( target.getTargetExternals() );

                return result;
            }
        }

        return null;
    }

    public void updateNotificationTarget( NotificationTarget target )
    {
        if ( target == null )
        {
            throw new NullPointerException( "Notification target can't be null!" );
        }

        // TODO: reimplement this to handle multiple targets!
        target.setTargetId( NotificationCheat.AUTO_BLOCK_NOTIFICATION_GROUP_ID );

        CNotificationTarget ctarget = new CNotificationTarget();

        ctarget.setTargetId( target.getTargetId() );
        ctarget.getTargetRoles().addAll( target.getTargetRoles() );
        ctarget.getTargetUsers().addAll( target.getTargetUsers() );
        ctarget.getTargetExternals().addAll( target.getExternalTargets() );

        List<CNotificationTarget> targets = getCurrentConfiguration( true ).getNotificationTargets();

        targets.clear();

        targets.add( ctarget );
    }

    public void notifyTargets( NotificationRequest request )
    {
        if ( !isEnabled() )
        {
            return;
        }

        for ( NotificationTarget target : request.getTargets() )
        {
            // TODO: should come from the group
            // for now, email is wired in
            String carrierKey = NotificationCheat.CARRIER_KEY;

            Carrier carrier = carriers.get( carrierKey );

            if ( carrier != null )
            {
                try
                {
                    carrier.notifyTarget( target, request.getMessage() );
                }
                catch ( NotificationException e )
                {
                    getLogger().warn( "Could not send out notification over carrier \"" + carrierKey + "\".", e );
                }
            }
            else
            {
                getLogger().info( "Notification carrier \"" + carrierKey + "\" is unknown!" );
            }
        }
    }

}
