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
package org.sonatype.nexus.email;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;

public class DefaultNexusEmailerTest
    extends AbstractNexusTestCase
{
    private DefaultNexusEmailer emailer;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        emailer = ( DefaultNexusEmailer ) lookup( NexusEmailer.class );
    }
    
    public void testConfigChanged()
        throws Exception
    {
        CSmtpConfiguration newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        newSmtp.setSslEnabled( true );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHost( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        newSmtp.setSslEnabled( true );
        newSmtp.setTlsEnabled( true );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        assertFalse( emailer.configChanged( newSmtp ) );
    }
}
