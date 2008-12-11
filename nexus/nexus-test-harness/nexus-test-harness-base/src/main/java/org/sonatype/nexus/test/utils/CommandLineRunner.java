/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.test.utils;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;

public class CommandLineRunner
{

    private static final Logger LOG = Logger.getLogger( CommandLineRunner.class );
    private final StringBuffer buffer = new StringBuffer();
    
    
    public int executeAndWait( Commandline cli ) throws CommandLineException, InterruptedException
    {
        Process p = null;
        StreamPumper outPumper = null;
        StreamPumper errPumper = null;

        StreamConsumer out = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                buffer.append( line ).append( "\n" );
            }
        };

        try
        {
            LOG.debug( "executing: " + cli.toString() );
            p = cli.execute();

            // we really don't need the stream pumps... but just in case... and if your into that whole sys-out style of
            // debugging this is for you...
            outPumper = new StreamPumper( p.getInputStream(), out );
            errPumper = new StreamPumper( p.getErrorStream(), out );

            outPumper.setPriority( Thread.MIN_PRIORITY + 1 );
            errPumper.setPriority( Thread.MIN_PRIORITY + 1 );

            outPumper.start();
            errPumper.start();

            return p.waitFor();
            
        }
        finally
        {
            if ( outPumper != null )
            {
                outPumper.close();
            }

            if ( errPumper != null )
            {
                errPumper.close();
            }
        }
    }
    
    public String getConsoleOutput()
    {
        return this.buffer.toString();
    }
    
}
