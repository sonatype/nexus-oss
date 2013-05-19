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
package org.sonatype.security.realms.tools;

import javax.inject.Singleton;

import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegePermissionPropertyDescriptor;

@Singleton
public class UnitTestDynamicSecurityResource
    extends AbstractDynamicSecurityResource
{
    private boolean configCalledAfterSetDirty = false;

    private static int INSTANCE_COUNT = 1;

    private String privId = "priv-" + INSTANCE_COUNT++;

    public String getId()
    {
        return privId;
    }

    protected Configuration doGetConfiguration()
    {
        configCalledAfterSetDirty = true;

        setConfigCalledAfterSetDirty( true );
        Configuration config = new Configuration();

        CPrivilege priv = new CPrivilege();
        priv.setId( privId );
        priv.setName( privId );
        priv.setReadOnly( true );
        priv.setType( ApplicationPrivilegeDescriptor.TYPE );
        CProperty method = new CProperty();
        method.setKey( ApplicationPrivilegeMethodPropertyDescriptor.ID );
        method.setValue( "read" );
        priv.addProperty( method );

        CProperty permission = new CProperty();
        permission.setKey( ApplicationPrivilegePermissionPropertyDescriptor.ID );
        permission.setValue( "foo:bar:" + privId );
        priv.addProperty( permission );

        config.addPrivilege( priv );

        return config;
    }

    public void setConfigCalledAfterSetDirty( boolean configCalledAfterSetDirty )
    {
        this.configCalledAfterSetDirty = configCalledAfterSetDirty;
    }

    public boolean isConfigCalledAfterSetDirty()
    {
        return configCalledAfterSetDirty;
    }

    @Override
    protected void setDirty( boolean dirty )
    {
        if ( dirty )
        {
            this.configCalledAfterSetDirty = false;
        }
        super.setDirty( dirty );
    }

}
