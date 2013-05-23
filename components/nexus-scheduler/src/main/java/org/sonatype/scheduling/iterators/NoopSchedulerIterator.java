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

/**
 * NOOP impl that will be used when no schedule is available, to save some null checks
 * 
 * @since 1.4.3
 *
 */
public class NoopSchedulerIterator
    extends AbstractSchedulerIterator
{
    public NoopSchedulerIterator()
    {
        super( new Date() );
    }

    public void resetFrom( Date from )
    {
    }

    @Override
    protected Date doPeekNext()
    {
        return null;
    }

    @Override
    protected void stepNext()
    {
    }
}
