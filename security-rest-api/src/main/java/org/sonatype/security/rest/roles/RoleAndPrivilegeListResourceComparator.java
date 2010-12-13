package org.sonatype.security.rest.roles;

import java.util.Comparator;

import org.sonatype.security.rest.model.RoleAndPrivilegeListResource;

public final class RoleAndPrivilegeListResourceComparator
    implements Comparator<RoleAndPrivilegeListResource>
{
    private final String sort;

    private final String dir;

    public static final String SORT_NAME = "name";

    public static final String SORT_DESCRIPTION = "description";

    public static final String DIR_ASC = "ASC";

    public static final String DIR_DESC = "DESC";

    public RoleAndPrivilegeListResourceComparator( String sort, String dir )
    {
        this.sort = sort;
        this.dir = dir;
    }

    public int compare( RoleAndPrivilegeListResource o1, RoleAndPrivilegeListResource o2 )
    {
        // always put external realms first
        if ( o1.isExternal() && !o2.isExternal() )
        {
            return -1;
        }
        else if ( o2.isExternal() && !o1.isExternal() )
        {
            return 1;
        }

        if ( SORT_NAME.equals( sort ) )
        {
            int compare = doCompare( o1.getType(), o2.getType(), dir );
            if ( compare == 0 )
            {
                return doCompare( o1.getName(), o2.getName(), dir );
            }
            return compare;
        }
        else if ( SORT_DESCRIPTION.equals( sort ) )
        {
            return doCompare( o1.getDescription(), o2.getDescription(), dir );
        }

        return 0;
    }

    private int doCompare( String value1, String value2, String dir )
    {
        if ( DIR_DESC.equals( dir ) )
        {
            return value2.compareTo( value1 );
        }
        else
        {
            return value1.compareTo( value2 );
        }
    }
}