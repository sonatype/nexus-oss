/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.configuration.validation;

import org.sonatype.configuration.Configuration;

/**
 * A request for validation, holding the configuration.
 * 
 * @author cstamas
 */
public class ValidationRequest<E extends Configuration>
{
    /**
     * The configuration to validate.
     */
    private E configuration;

    public ValidationRequest( E configuration )
    {
        super();

        this.configuration = configuration;
    }

    public E getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( E configuration )
    {
        this.configuration = configuration;
    }
}
