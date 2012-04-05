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
package org.sonatype.security.configuration;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

/**
 * A StaticSecurityResource that contributes static privileges and roles to the XML Realms.
 * 
 * @author bdemers
 */
@Singleton
@Typed( value = StaticSecurityResource.class )
@Named( value = "SecurityRestStaticSecurityResource" )
public class SecurityRestStaticSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource
{
    /*
     * (non-Javadoc)
     * @see org.sonatype.security.realms.tools.AbstractStaticSecurityResource#getResourcePath()
     */
    protected String getResourcePath()
    {
        return "/META-INF/security/static-security-rest.xml";
    }
}
