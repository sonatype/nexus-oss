/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

public interface ProgressListener
{
    /**
     * Marks that the amount of work (work-units) are not known in advance.
     */
    int UNKNOWN_WORKUNITS = -1;

    /**
     * Starts a new (sub)task with {@link #UNKNOWN_WORKUNITS} to be done.
     * 
     * @param name
     */
    public void beginTask( String name );

    /**
     * Starts a new (sub)task with {@code toDo} work-units to be done.
     * 
     * @param name
     * @param toDo
     */
    public void beginTask( String name, int toDo );

    /**
     * Marks work is underway without a message. It is left to {@link ProgressListener} implementor what will happen
     * with this information (like update a progress bar for example). This is NOT a setter! Work unit count sent in
     * here are accumulated (summed up).
     * 
     * @param message
     */
    public void working( int workDone );

    /**
     * Marks work is underway with a message. It is left to {@link ProgressListener} implementor what will happen with
     * this message, will it be shown in log, in UI or whatever.
     * 
     * @param message
     */
    public void working( String message );

    /**
     * Marks work is underway and {@code workDone} work-units as done. This is NOT a setter! Work unit count sent in
     * here are accumulated (summed up). It is left to {@link ProgressListener} implementor what will happen with this
     * message, will it be shown in log, in UI or whatever.
     * 
     * @param workDone
     */
    public void working( String message, int workDone );

    /**
     * Ends a (sub)task with a message.
     * 
     * @param message
     */
    public void endTask( String message );

    /**
     * Returns true if the task-run to which this progress monitor belongs to should be canceled.
     * 
     * @return
     */
    boolean isCanceled();

    /**
     * Cancels the task-run to which this progress monitor belongs to. This call will return immediately (will not block
     * to wait actual task cancellation).
     */
    void cancel();
}
