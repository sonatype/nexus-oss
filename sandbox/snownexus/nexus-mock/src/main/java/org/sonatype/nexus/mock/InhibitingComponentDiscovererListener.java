package org.sonatype.nexus.mock;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Rude listener implementation that: inhibits Nexus and all PlexusResource components from nexus-rest-api
 *
 * @author cstamas
 */
public class InhibitingComponentDiscovererListener
    implements ComponentDiscoveryListener
{
    @SuppressWarnings("unchecked")
    public void componentDiscovered( ComponentDiscoveryEvent event )
    {
        ComponentSetDescriptor set = event.getComponentSetDescriptor();

        ArrayList<ComponentDescriptor<?>> injectedComponents = new ArrayList<ComponentDescriptor<?>>();

        for ( Iterator<ComponentDescriptor<?>> i = set.getComponents().iterator(); i.hasNext(); )
        {
            ComponentDescriptor<?> comp = i.next();

            if ( PlexusResource.class.getName().equals( comp.getRole() )
                || ManagedPlexusResource.class.getName().equals( comp.getRole() ) )
            {
                String role = comp.getRole();

                // rename the original
                comp.setRole( "hidden-" + role );

                // tie in the "proxy"
                ComponentDescriptor proxy = new ComponentDescriptor();
                proxy.setRole( role );
                proxy.setRoleHint( comp.getRoleHint() );
                proxy.setImplementation( ProxyPlexusResource.class.getName() );

                ComponentRequirement req = new ComponentRequirement();
                req.setFieldName( "plexusResource" );
                req.setRole( comp.getRole() );
                req.setRoleHint( comp.getRoleHint() );

                proxy.addRequirement( req );

                injectedComponents.add( proxy );
            }
        }

        // add the new ones in
        for ( ComponentDescriptor<?> proxy : injectedComponents )
        {
            set.addComponentDescriptor( proxy );
        }
    }

    public String getId()
    {
        return "Inhibit";
    }

}
