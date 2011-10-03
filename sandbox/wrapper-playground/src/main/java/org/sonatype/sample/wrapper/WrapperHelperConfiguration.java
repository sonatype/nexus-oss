/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.sample.wrapper;


/**
 * A WrapperHelper configuration providing all the defaults for Sonatype Application Bundles, but still making those
 * overridable.
 * 
 * @author cstamas
 */
public class WrapperHelperConfiguration
{
    /**
     * The key used for looking into System properties to get the basedir value.
     */
    private String basedirPropertyKey = "basedir";

    /**
     * The path of the "conf" directory. If relative, it is calculated from basedir.
     */
    private String confDirPath = "conf";

    /**
     * The name of the wrapper.conf file.
     */
    private String wrapperConfName = "wrapper.conf";

    /**
     * The name of the backup of the wrapper.conf file.
     */
    private String wrapperConfBackupName = "wrapper.conf.bak";

    public String getBasedirPropertyKey()
    {
        return basedirPropertyKey;
    }

    public void setBasedirPropertyKey( String basedirPropertyKey )
    {
        this.basedirPropertyKey = basedirPropertyKey;
    }

    public String getConfDirPath()
    {
        return confDirPath;
    }

    public void setConfDirPath( String confDirPath )
    {
        this.confDirPath = confDirPath;
    }

    public String getWrapperConfName()
    {
        return wrapperConfName;
    }

    public void setWrapperConfName( String wrapperConfName )
    {
        this.wrapperConfName = wrapperConfName;
    }

    public String getWrapperConfBackupName()
    {
        return wrapperConfBackupName;
    }

    public void setWrapperConfBackupName( String wrapperConfBackupName )
    {
        this.wrapperConfBackupName = wrapperConfBackupName;
    }
}
