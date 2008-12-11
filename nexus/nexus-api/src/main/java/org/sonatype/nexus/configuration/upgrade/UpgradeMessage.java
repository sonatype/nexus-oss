/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.upgrade;

/**
 * An upgrade message used to hold the current version and the configuration itself. Since modello generated classes
 * differs from model version to model version, it is held as Object, and the needed converter will cast it to what it
 * needs.
 * 
 * @author cstamas
 */
public class UpgradeMessage
{
    private String modelVersion;

    private Object configuration;

    public String getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion( String modelVersion )
    {
        this.modelVersion = modelVersion;
    }

    public Object getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Object configuration )
    {
        this.configuration = configuration;
    }
}
