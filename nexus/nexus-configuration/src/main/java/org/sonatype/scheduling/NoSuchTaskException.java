package org.sonatype.scheduling;

public class NoSuchTaskException
    extends Exception
{
    public NoSuchTaskException( String id )
    {
        super( "There is no running/active task with ID=" + id );
    }

}
