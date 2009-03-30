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
    
    private Map<String, String> expectations = new LinkedHashMap<String, String>();
    private Set<String> used = new HashSet<String>();
    
    public void addExpectation( String promptSubstr, String response )
    {
        expectations.put( promptSubstr, response );
    }

    public String prompt( String prompt )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    public String prompt( String prompt, String defVal )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    @SuppressWarnings("unchecked")
    public String prompt( String prompt, List values )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    @SuppressWarnings("unchecked")
    public String prompt( String prompt, List values, String defVal )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    public String promptForPassword( String prompt )
        throws PrompterException
    {
        return expectationFor( prompt );
    }

    public void showMessage( String prompt )
        throws PrompterException
    {
        System.out.println( prompt );
    }

    private String expectationFor( String prompt )
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
