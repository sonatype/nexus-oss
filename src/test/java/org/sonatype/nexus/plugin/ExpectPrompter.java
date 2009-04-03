/*
 * Nexus Plugin for Maven
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.plugin;

import static junit.framework.Assert.fail;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpectPrompter
    implements Prompter
{
    
    private final Map<String, String> expectations = new LinkedHashMap<String, String>();
    private final Set<String> used = new HashSet<String>();
    
    public void addExpectation( final String promptSubstr, final String response )
    {
        expectations.put( promptSubstr, response );
    }

    public String prompt( final String prompt )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    public String prompt( final String prompt, final String defVal )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    @SuppressWarnings("unchecked")
    public String prompt( final String prompt, final List values )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    @SuppressWarnings("unchecked")
    public String prompt( final String prompt, final List values, final String defVal )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    public String promptForPassword( final String prompt )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    public void showMessage( final String prompt )
        throws PrompterException
    {
        System.out.println( prompt );
    }

    private String expectationFor( final String prompt )
    {
        System.out.print( prompt );
        
        String result = "-NOT SUPPLIED-";
        for ( Map.Entry<String, String> entry : expectations.entrySet() )
        {
            if ( prompt.indexOf( entry.getKey() ) > -1 )
            {
                used.add( entry.getKey() );
                result = entry.getValue();
                
                break;
            }
        }
        
        System.out.println( result );
        
        return result;
    }

    public void verifyPromptsUsed()
    {
        Map<String, String> remaining = new LinkedHashMap<String, String>( expectations );
        remaining.keySet().removeAll( used );
        
        if ( !remaining.isEmpty() )
        {
            StringBuilder sb = new StringBuilder();
            
            sb.append( "The following prompt/answer pairs were never used:\n" );
            for ( Map.Entry<String, String> entry : remaining.entrySet() )
            {
                sb.append( "\n-  " ).append( entry.getKey() ).append( " = " ).append( entry.getValue() );
            }
            sb.append( "\n\n" );
            
            System.out.println( sb.toString() );
            fail( sb.toString() );
        }
    }

}
