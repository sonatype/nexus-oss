package org.sonatype.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sonatype.nexus.configuration.model.CTask;
import org.sonatype.nexus.configuration.model.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.CTaskIterating;
import org.sonatype.nexus.configuration.model.CTaskScheduled;

public class DefaultTaskConfigManager implements TaskConfigManager
{    
    private CTaskConfiguration config;
    
    public DefaultTaskConfigManager()
    {
        super();
        
        loadConfig();
    }
    
    public HashMap<String, List<SubmittedTask<?>>> getTasks()
    {
        HashMap<String, List<SubmittedTask<?>>> map = new HashMap<String, List<SubmittedTask<?>>>();
        
        for ( Iterator iter = config.getTasks().iterator(); iter.hasNext(); )
        {
            CTask storedTask = (CTask) iter.next();            
            
            if ( !map.containsKey( storedTask.getType() ))
            {
                map.put( storedTask.getType() , new ArrayList<SubmittedTask<?>>() );
            }
            
            map.get( storedTask.getType() ).add( translateFrom( storedTask ) );
        }
        
        return map;
    }
    
    public <T> void addTask( SubmittedTask<T> task )
    {
        CTask storeableTask = translateFrom( task );
        
        if ( storeableTask != null )
        {
            config.addTask( storeableTask );
            storeConfig();
        }
    }
    
    public <T> void removeTask( SubmittedTask<T> task )
    {        
        for ( Iterator iter = config.getTasks().iterator(); iter.hasNext(); )
        {
            CTask storedTask = (CTask) iter.next();
            
            if ( storedTask.getId().equals( task.getId() ))
            {
                iter.remove();
                break;
            }
        }
        
        storeConfig();
        //TODO: need to also add task to a history file
    }
    
    private <T> SubmittedTask<T> translateFrom( CTask task )
    {
        //TODO: need to translate properly
        return null;
    }
    
    private <T> CTask translateFrom ( SubmittedTask<T> task )
    {
        CTask storeableTask = null;
        
        if ( ScheduledTask.class.isAssignableFrom( task.getClass() ) )
        {
            storeableTask = new CTaskScheduled();
        }
        else if ( IteratingTask.class.isAssignableFrom( task.getClass() ) )
        {
            storeableTask = new CTaskIterating();
        }
        else if ( SubmittedTask.class.isAssignableFrom( task.getClass() ) )
        {
            storeableTask = new CTask();
        }
        
        if ( storeableTask != null )
        {
            storeableTask.setId( task.getId() );
            storeableTask.setType( task.getType() );
        }
        
        //TODO: need to complete translation
        
        return storeableTask;
    }
    
    private void loadConfig()
    {
        //TODO: need to load from file
        config = new CTaskConfiguration();
    }
    
    private void storeConfig()
    {
        //TODO: need to write to file
    }
}
