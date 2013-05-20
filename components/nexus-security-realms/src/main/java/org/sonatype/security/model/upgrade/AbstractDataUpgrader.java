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
package org.sonatype.security.model.upgrade;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;

/**
 * An abstract SingleVersionDataUpgrader that is used to validate the <code>configuration</code> object's class and
 * correctly cast it.
 */
public abstract class AbstractDataUpgrader<C>
    implements SingleVersionDataUpgrader
{

    public abstract void doUpgrade( C configuration )
        throws ConfigurationIsCorruptedException;

    @SuppressWarnings( "unchecked" )
    public void upgrade( Object configuration )
        throws ConfigurationIsCorruptedException
    {
        // TODO type check

        // template
        this.doUpgrade( (C) configuration );
    }
}
