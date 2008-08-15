/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
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
    SUBMITTED, // -> RUNNING, CANCELLED

    RUNNING, // -> WAITING, FINISHED, BROKEN, CANCELLED

    WAITING, // -> RUNNING, CANCELLED

    FINISHED, // END

    BROKEN, // END

    CANCELLED; // END
    
    public boolean isActiveOrSubmitted()
    {
        return this.equals( SUBMITTED ) || this.equals( RUNNING ) || this.equals( WAITING );
    }
    
    public boolean isActive()
    {
        return this.equals( RUNNING ) || this.equals( WAITING );
    }

    public boolean isEndingState()
    {
        /* I don't think BROKEN should apply, broken simply means an exception was thrown.
         * So what?  let the user attempt to do it again, maybe an fs perm problem that they resolved */
        return this.equals( FINISHED ) || this.equals( CANCELLED );
    }
}
