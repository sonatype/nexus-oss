/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.user;

import com.thoughtworks.xstream.XStream;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.nexus.rest.user.dto.UserAccountDTO;
import org.sonatype.nexus.rest.user.dto.UserAccountRequestDTO;
import org.sonatype.nexus.rest.user.dto.UserAccountResponseDTO;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserStatus;

public abstract class AbstractUserAccountPlexusResource
    extends AbstractSecurityPlexusResource
{
    @Requirement
    protected UserAccountManager userAccountManager;

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( UserAccountDTO.class );
        xstream.processAnnotations( UserAccountRequestDTO.class );
        xstream.processAnnotations( UserAccountResponseDTO.class );
    }

    protected UserAccountDTO nexusToRestModel( User user, Request request )
    {
        UserAccountDTO dto = new UserAccountDTO();

        dto.setId( user.getUserId() );

        dto.setName( user.getName() );

        dto.setEmail( user.getEmailAddress() );

        dto.setResourceURI( this.createChildReference( request, "" ).toString() );

        return dto;
    }

    protected User restToNexusModel( UserAccountDTO dto )
    {
        User user = new DefaultUser();

        user.setUserId( dto.getId() );

        user.setName( dto.getName() );

        user.setEmailAddress( dto.getEmail() );

        user.setSource( DEFAULT_SOURCE );

        user.setStatus( UserStatus.active );

        return user;
    }
}
