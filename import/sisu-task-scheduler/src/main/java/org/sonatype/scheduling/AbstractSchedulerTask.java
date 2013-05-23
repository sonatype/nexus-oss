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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSchedulerTask<T>
    implements SchedulerTask<T>
{
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String> parameters;

    public void addParameter( String key, String value )
    {
        getParameters().put( key, value );
    }

    public String getParameter( String key )
    {
        return getParameters().get( key );
    }

    public synchronized Map<String, String> getParameters()
    {
        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        return parameters;
    }

    public abstract T call()
        throws Exception;

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    protected void checkInterruption()
        throws TaskInterruptedException
    {
        TaskUtil.checkInterruption();
    }
}
