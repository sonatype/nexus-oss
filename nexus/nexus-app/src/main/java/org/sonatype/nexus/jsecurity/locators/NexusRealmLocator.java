package org.sonatype.nexus.jsecurity.locators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ServiceLocator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Serviceable;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

/**
 * The nexus implementation of the realm locator will load from the nexus.xml file
 */
@Component( role = RealmLocator.class )
public class NexusRealmLocator
    extends AbstractLogEnabled
    implements RealmLocator, Serviceable
{
    @Requirement
    NexusConfiguration configuration;

    private ServiceLocator container;
    
    /**
     * We are always going to return the same list so if we pass this list off to an API, they will pickup the changes.
     */
    private List<Realm> realms = new ArrayList<Realm>();

    public void service( ServiceLocator locator )
    {
        this.container = locator;
    }

    public List<Realm> getRealms()
    {
        // Until nexus.xml code is done, simply returning hardcoded realms

        // reset the list
        realms.clear();

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

        return Collections.unmodifiableList( realms );
    }

}
