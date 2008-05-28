package org.sonatype.scheduling;

/**
 * Enum for describing task state. It is a state machine: starting state is SCHEDULED, finishing states are FINISHED,
 * BROKEN and CANCELLED. Scheduled tasks are jumping between RUNNING and WAITING until finished, cancelled or error
 * (broken).
 * 
 * @author cstamas
 */
public enum TaskState
{
    SCHEDULED, // -> RUNNING, CANCELLED

    RUNNING, // -> WAITING, FINISHED, BROKEN, CANCELLED

    WAITING, // -> RUNNING, CANCELLED

    FINISHED, // END

    BROKEN, // END

    CANCELLED; // END
    
    public boolean isActive()
    {
        return this.equals( SCHEDULED ) || this.equals( RUNNING ) || this.equals( WAITING );
    }

    public boolean isEndingState()
    {
        return this.equals( FINISHED ) || this.equals( BROKEN ) || this.equals( CANCELLED );
    }
}
