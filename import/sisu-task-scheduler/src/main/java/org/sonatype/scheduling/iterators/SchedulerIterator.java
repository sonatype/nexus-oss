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
package org.sonatype.scheduling.iterators;

import java.util.Date;

public interface SchedulerIterator
{
    /**
     * Returns, or "peek"s the next run without updating internal state of iterator. Calling this method simultaneously
     * will always return the same date of "next" run.
     * 
     * @return
     */
    Date peekNext();

    /**
     * Returns the date of next run and updates internal state of the iterator: "steps" over just like Iterator.next().
     * Calling this method simultaneously will always return new and new (different) dates of next run until it's
     * depleted.
     * 
     * @return
     */
    Date next();

    /**
     * Returns true when iterator is depleted, no more runs needed.
     * 
     * @return
     */
    boolean isFinished();

    /**
     * Resets the scheduler internal state to start with from date passed in as parameter.
     * 
     * @param from
     */
    void resetFrom( Date from );
}
