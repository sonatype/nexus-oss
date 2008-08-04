/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.smtp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;

/**
 * @plexus.component
 */
public class DefaultSmtpClient
    implements
        SmtpClient
{    
    /**
     * The nexus configuration.
     * 
     * @plexus.requirement
     */
    private NexusConfiguration nexusConfiguration;
    
    public void sendEmail( String to, String from, String subject, String body )
        throws SmtpClientException
    {
        List<String> toList = new ArrayList<String>();
        toList.add( to );
        
        sendEmail( toList, from, subject, body );
    }

    public void sendEmail( List<String> toList, String from, String subject, String body )
        throws SmtpClientException
    {   
        try
        {
            CSmtpConfiguration smtp = nexusConfiguration.getConfiguration().getSmtpConfiguration();
            SimpleEmail email = new SimpleEmail();
            email.setHostName( smtp.getHost() );
            email.setSmtpPort( smtp.getPort() );
            email.setAuthentication( smtp.getUsername(), smtp.getPassword() );
            email.setDebug( smtp.isDebugMode() );
            email.setSSL( smtp.isTlsEnabled() );
            email.setTLS( smtp.isTlsEnabled() );
            
            for ( String to : toList )
            {
                email.addTo( to );
            }
            
            email.setFrom( from == null ? smtp.getSystemEmailAddress() : from );
            email.setSubject( subject );
            email.setMsg( body );
    
            email.send();
        }
        catch ( EmailException e )
        {
            throw new SmtpClientException( "Error handling smtp request", e );
        }
    }
}
