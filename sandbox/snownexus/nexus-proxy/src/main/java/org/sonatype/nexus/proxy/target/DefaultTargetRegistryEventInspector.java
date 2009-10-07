package org.sonatype.nexus.proxy.target;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "DefaultTargetRegistryEventInspector" )
public class DefaultTargetRegistryEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;
    
    @Requirement
    private TargetRegistry targetRegistry;
    
    @Requirement
    private ApplicationConfiguration applicationConfiguration;
    
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
        try
        {
            boolean changed = false;
            
            Map<String,ContentClass> contentClasses = repositoryTypeRegistry.getContentClasses();
            
            for ( String key : contentClasses.keySet() )
            {
                boolean found = false;
                
                for ( Target target : targetRegistry.getTargetsForContentClass( contentClasses.get( key ) ) )
                {
                    // create default target for each content class that doesn't already exist
                    if ( target.getContentClass().equals( contentClasses.get( key ) ) 
                        && target.getPatternTexts().size() == 1
                        && target.getPatternTexts().iterator().next().equals( ".*" ) )
                    {
                        found = true;
                        break;
                    }
                }
                
                if ( !found )
                {
                    Target newTarget = new Target(
                        key,
                        "All (" + key + ")",
                        contentClasses.get( key ),
                        Collections.singleton( ".*" ));
                    
                    targetRegistry.addRepositoryTarget( newTarget );
                    changed = true;
                    getLogger().info( "Adding default target for " + key + " content class" );   
                }
            }
            
            if ( changed )
            {
                applicationConfiguration.saveConfiguration();
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to properly add default Repository Targets", e );
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "Unable to properly add default Repository Targets", e );
        }
    }
}
