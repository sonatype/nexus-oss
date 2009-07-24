package org.sonatype.nexus.plugins.plexus;

import static org.codehaus.plexus.component.CastUtils.isAssignableFrom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CompositionResolver;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.composition.DefaultCompositionResolver;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginDescriptor;
import org.sonatype.plugin.metadata.GAVCoordinate;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author Jason van Zyl
 */
public class NexusPluginsComponentRepository
    extends AbstractLogEnabled
    implements ComponentRepository
{
    private final Map<ClassRealm, SortedMap<String, Multimap<String, ComponentDescriptor<?>>>> index =
        new LinkedHashMap<ClassRealm, SortedMap<String, Multimap<String, ComponentDescriptor<?>>>>();

    private final CompositionResolver compositionResolver = new DefaultCompositionResolver();

    private NexusPluginManager nexusPluginManager;

    public NexusPluginsComponentRepository()
    {
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    public NexusPluginManager getNexusPluginManager()
    {
        return nexusPluginManager;
    }

    public void setNexusPluginManager( NexusPluginManager nexusPluginManager )
    {
        this.nexusPluginManager = nexusPluginManager;
    }

    private Multimap<String, ComponentDescriptor<?>> getComponentDescriptors( String role )
    {
        // verify arguments
        if ( role == null )
        {
            throw new NullPointerException( "role is null" );
        }

        // determine realms to search
        LinkedHashSet<ClassRealm> realms = new LinkedHashSet<ClassRealm>();

        // Solution: load all components from current realm and 1st level siblings only
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        PluginDescriptor pluginDescriptor = null;

        if ( classLoader != null && classLoader instanceof ClassRealm )
        {
            ClassRealm realm = (ClassRealm) classLoader;

            realms.add( realm );

            if ( realm.getParentRealm() == null )
            {
                // we are most probably in core plexus realm ("plexus.core")
                // so, to find all extension points coming from plugins (which are in child realms of this realm)
                // we need to extend component search to all 1st level siblings too

                // find all 1st level sibling for this realm above
                Collection<ClassRealm> allRealms = realm.getWorld().getRealms();

                for ( ClassRealm aRealm : allRealms )
                {
                    if ( aRealm.getParent() != null && aRealm.getParent().equals( realm ) )
                    {
                        realms.add( aRealm );
                    }
                }
            }
            else
            {
                // we are most probably in a plugin realm,
                // so we must extend the search to the parent too (to find components from plexus.core like Nexus core
                // stuff
                // and add parents too
                ClassRealm aRealm = realm.getParentRealm();

                while ( aRealm != null )
                {
                    realms.add( aRealm );

                    aRealm = aRealm.getParentRealm();
                }

                // and NOW, the imports!
                // but only when we are able to get to the plugin manager
                if ( getNexusPluginManager() != null )
                {
                    // WE ASSUME HERE that realm uses the same coord.getPluginKey()!!!!
                    // FIX THIS
                    GAVCoordinate pluginCoord = new GAVCoordinate( realm.getId() );

                    pluginDescriptor = getNexusPluginManager().getActivatedPlugins().get( pluginCoord );

                    if ( pluginDescriptor != null )
                    {
                        for ( PluginDescriptor importedPlugin : pluginDescriptor.getImportedPlugins() )
                        {
                            realms.add( importedPlugin.getPluginRealm() );
                        }
                    }
                }
            }
        }
        else
        {
            // this is not classrealm, add _all_ realms to discover components
            // usually the case in PlexusTestCase if context classloader is not set explicitly
            realms.addAll( index.keySet() );
        }

        // cstamas:
        // to be able to load _all_ registered plugin components, which are all in child realms of container realm,
        // we must scrape all registered realms instead to target them one-by-one with
        // Thread.currentThread().getContextClassloader()
        // or any other idea?

        // for ( ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); classLoader != null;
        // classLoader =
        // classLoader.getParent() )
        // {
        // if ( classLoader instanceof ClassRealm )
        // {
        // ClassRealm realm = (ClassRealm) classLoader;
        // while ( realm != null )
        // {
        // realms.add( realm );
        // realm = realm.getParentRealm();
        // }
        // }
        // }
        if ( realms.isEmpty() )
        {
            realms.addAll( index.keySet() );
        }

        // Get all valid component descriptors
        Multimap<String, ComponentDescriptor<?>> roleHintIndex = Multimaps.newLinkedHashMultimap();
        for ( ClassRealm realm : realms )
        {
            SortedMap<String, Multimap<String, ComponentDescriptor<?>>> roleIndex = index.get( realm );
            if ( roleIndex != null )
            {
                Multimap<String, ComponentDescriptor<?>> descriptors = roleIndex.get( role );
                if ( descriptors != null )
                {
                    // Filter out non-exported components, if we had contributions from plugin
                    if ( pluginDescriptor != null )
                    {
                        for ( PluginDescriptor importedPlugin : pluginDescriptor.getImportedPlugins() )
                        {
                            if ( realm.equals( importedPlugin.getPluginRealm() ) )
                            {
                                for ( Iterator<ComponentDescriptor<?>> i = descriptors.values().iterator(); i.hasNext(); )
                                {
                                    ComponentDescriptor<?> cd = i.next();

                                    if ( !importedPlugin.getExports().contains( cd.getImplementation() ) )
                                    {
                                        i.remove();
                                    }
                                }
                            }
                        }
                    }

                    roleHintIndex.putAll( descriptors );
                }
            }
        }

        return Multimaps.unmodifiableMultimap( roleHintIndex );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( Class<T> type, String role, String roleHint )
    {
        for ( ComponentDescriptor<?> descriptor : getComponentDescriptors( role ).get( roleHint ) )
        {
            if ( isAssignableFrom( type, descriptor.getImplementationClass() ) )
            {
                return (ComponentDescriptor<T>) descriptor;
            }
        }

        return null;
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( Class<T> type, String role )
    {
        Map<String, ComponentDescriptor<T>> descriptors = new TreeMap<String, ComponentDescriptor<T>>();
        for ( ComponentDescriptor<?> descriptor : getComponentDescriptors( role ).values() )
        {
            if ( !descriptors.containsKey( descriptor.getRoleHint() ) )
            {
                if ( isAssignableFrom( type, descriptor.getImplementationClass() ) )
                {
                    descriptors.put( descriptor.getRoleHint(), (ComponentDescriptor<T>) descriptor );
                }
            }
        }
        return descriptors;
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( Class<T> type, String role )
    {
        List<ComponentDescriptor<T>> descriptors = new ArrayList<ComponentDescriptor<T>>();
        for ( ComponentDescriptor<?> descriptor : getComponentDescriptors( role ).values() )
        {
            if ( isAssignableFrom( type, descriptor.getImplementationClass() ) )
            {
                descriptors.add( (ComponentDescriptor<T>) descriptor );
            }
        }
        return descriptors;
    }

    @Deprecated
    public ComponentDescriptor<?> getComponentDescriptor( String role, String roleHint, ClassRealm realm )
    {
        // find all realms from our realm to the root realm
        Set<ClassRealm> realms = new HashSet<ClassRealm>();
        for ( ClassRealm r = realm; r != null; r = r.getParentRealm() )
        {
            realms.add( r );
        }

        // get the component descriptors by roleHint
        for ( ComponentDescriptor<?> componentDescriptor : getComponentDescriptors( role ).get( roleHint ) )
        {
            // return the first descriptor from our target realms
            if ( realms.contains( componentDescriptor.getRealm() ) )
            {
                return componentDescriptor;
            }
        }

        return null;
    }

    public void removeComponentRealm( ClassRealm classRealm )
    {
        index.remove( classRealm );
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Component Descriptor processing.
    // ----------------------------------------------------------------------

    public void addComponentDescriptor( ComponentDescriptor<?> componentDescriptor )
        throws CycleDetectedInComponentGraphException
    {
        ClassRealm classRealm = componentDescriptor.getRealm();
        SortedMap<String, Multimap<String, ComponentDescriptor<?>>> roleIndex = index.get( classRealm );
        if ( roleIndex == null )
        {
            roleIndex = new TreeMap<String, Multimap<String, ComponentDescriptor<?>>>();
            index.put( classRealm, roleIndex );
        }

        String role = componentDescriptor.getRole();
        Multimap<String, ComponentDescriptor<?>> roleHintIndex = roleIndex.get( role );
        if ( roleHintIndex == null )
        {
            roleHintIndex = Multimaps.newLinkedHashMultimap();
            roleIndex.put( role, roleHintIndex );
        }
        roleHintIndex.put( componentDescriptor.getRoleHint(), componentDescriptor );

        compositionResolver.addComponentDescriptor( componentDescriptor );
    }
}
