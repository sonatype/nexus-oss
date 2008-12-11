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
package org.sonatype.nexus.jsecurity.locators;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.web.WebRememberMeManager;
import org.jsecurity.web.attr.CookieAttribute;
import org.sonatype.jsecurity.locators.RememberMeLocator;

/**
 * The nexus remember me locator, will simply return the default JSecurity WebRememberMeManager
 */
@Component( role = RememberMeLocator.class )
public class NexusRememberMeLocator
    implements RememberMeLocator
{
    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "nxRememberMe";

    public RememberMeManager getRememberMeManager()
    {
        WebRememberMeManager rmm = new WebRememberMeManager();

        CookieAttribute<String> attr = new CookieAttribute<String>( DEFAULT_REMEMBER_ME_COOKIE_NAME );

        // 7 days (seconds!)
        attr.setMaxAge( 60 * 60 * 24 * 7 );

        rmm.setIdentityAttribute( attr );

        return rmm;
    }

}
