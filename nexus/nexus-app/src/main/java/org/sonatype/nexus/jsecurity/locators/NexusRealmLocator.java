package org.sonatype.nexus.jsecurity.locators;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

/**
 * The nexus implementation of the realm locator will load from the nexus.xml file
 */
@Component( role = RealmLocator.class )
public class NexusRealmLocator
    extends AbstractLogEnabled
    implements RealmLocator
{
    @Requirement
    NexusConfiguration configuration;

    @Requirement
    private PlexusContainer container;

    public List<Realm> getRealms()
    {
        List<Realm> realms = new ArrayList<Realm>();

        List<String> realmIds = configuration.getRealms();

        for ( String realmId : realmIds )
        {
            try
            {
                // First will load from plexus container
                realms.add( (Realm) container.lookup( "org.jsecurity.realm.Realm", realmId ) );
            }
            catch ( ComponentLookupException e )
            {
                // If that fails, will simply use reflection to load
                try
                {
                    realms.add( (Realm) Class.forName( realmId ).newInstance() );
                }
                catch ( InstantiationException e1 )
                {
                    getLogger().error( "Unable to lookup security realms", e );
                }
                catch ( IllegalAccessException e1 )
                {
                    getLogger().error( "Unable to lookup security realms", e );
                }
                catch ( ClassNotFoundException e1 )
                {
                    getLogger().error( "Unable to lookup security realms", e );
                }
            }
        }

        return realms;
    }

}
