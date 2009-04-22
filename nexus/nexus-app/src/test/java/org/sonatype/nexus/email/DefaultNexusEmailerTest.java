/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.email;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.security.email.SecurityEmailer;

public class DefaultNexusEmailerTest
    extends AbstractNexusTestCase
{
    private DefaultNexusEmailer emailer;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        emailer = ( DefaultNexusEmailer ) lookup( SecurityEmailer.class );
    }
    
    public void testConfigChanged()
        throws Exception
    {
        CSmtpConfiguration newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname(  "1.2.3.4" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        newSmtp.setSslEnabled( true );
        
        assertTrue( emailer.configChanged( newSmtp ) );
        
        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
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
