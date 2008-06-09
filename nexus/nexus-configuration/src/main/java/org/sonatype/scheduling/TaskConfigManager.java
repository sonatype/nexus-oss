package org.sonatype.scheduling;


/**
 * Manage the storage and loading of ScheduledTask objects
 */
public interface TaskConfigManager
{
    String ROLE = TaskConfigManager.class.getName();
    
    /**
     * Add a new scheduled task
     * 
     * @param <T>
     * @param task
     */
    public <T> void addTask( ScheduledTask<T> task );
    
    /**
     * Remove an existing scheduled task
     * 
     * @param <T>
     * @param task
     */
    public <T> void removeTask( ScheduledTask<T> task );
    
    /**
     * Create and start all tasks, usually done once upon starting system
     * (to start tasks that should be recurring)
     * 
     * @param scheduler
     */
    public void initializeTasks( Scheduler scheduler );
}
