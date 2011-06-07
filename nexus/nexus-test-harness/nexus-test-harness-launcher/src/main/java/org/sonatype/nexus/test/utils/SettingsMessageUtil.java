/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.test.utils;

import com.google.common.base.Preconditions;
import java.io.IOException;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.rest.model.SmtpSettingsResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class SettingsMessageUtil
{
    public static final String GLOBAL_SETTINGS_CURRENT_SERVICE = "service/local/global_settings/current";
    public static final String CHECK_SMTP_SETTINGS_SERVICE = "service/local/check_smtp_settings/";

    public static GlobalConfigurationResource getCurrentSettings()
        throws IOException
    {
        return getData(new GlobalConfigurationResourceResponse()); 
    }

    public static GlobalConfigurationResource getData(final GlobalConfigurationResourceResponse wrapper)
        throws IOException
    {
        Preconditions.checkNotNull(wrapper);
        final XStreamRepresentation xmlRepresentation = RequestFacade.doGetForXStreamRepresentationWithSuccess(GLOBAL_SETTINGS_CURRENT_SERVICE);
        final GlobalConfigurationResourceResponse configResponse = (GlobalConfigurationResourceResponse) xmlRepresentation.getPayload( wrapper );
        return configResponse.getData();        
    }
    
    public static Status save( final GlobalConfigurationResource globalConfig )
        throws IOException
    {
        Preconditions.checkNotNull(globalConfig);
        final GlobalConfigurationResourceResponse configResponse = new GlobalConfigurationResourceResponse();
        configResponse.setData( globalConfig );
        return RequestFacade.doPutForStatus(GLOBAL_SETTINGS_CURRENT_SERVICE, configResponse);
    }

    public static Status save( final SmtpSettingsResource smtpSettings)
        throws IOException
    {
        Preconditions.checkNotNull(smtpSettings);
        SmtpSettingsResourceRequest configResponse = new SmtpSettingsResourceRequest();
        configResponse.setData( smtpSettings );
        return RequestFacade.doPutForStatus(CHECK_SMTP_SETTINGS_SERVICE, configResponse);
    }
    
    /**
     * Wrap a {@link GlobalConfigurationResource} in a {@link GlobalConfigurationResourceResponse} and return it.
     * @param resource the resource to wrap
     * @return a wrapper containing the resource
     */
    public static GlobalConfigurationResourceResponse setData( final GlobalConfigurationResource resource)
    {
        Preconditions.checkNotNull(resource);
        final GlobalConfigurationResourceResponse wrapper = new GlobalConfigurationResourceResponse();
        wrapper.setData( resource );
        return wrapper;
    }
    
    public static SmtpSettingsResourceRequest setData( final SmtpSettingsResource resource)
    {
        Preconditions.checkNotNull(resource);
        final SmtpSettingsResourceRequest wrapper = new SmtpSettingsResourceRequest();
        wrapper.setData( resource );
        return wrapper;
    }
    
}
