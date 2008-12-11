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
package org.sonatype.nexus.store.file;

public class SimpleObject
{

    private String aString;

    private int anInt;

    private boolean aBoolean;

    public String getAString()
    {
        return aString;
    }

    public void setAString( String string )
    {
        aString = string;
    }

    public int getAnInt()
    {
        return anInt;
    }

    public void setAnInt( int anInt )
    {
        this.anInt = anInt;
    }

    public boolean isABoolean()
    {
        return aBoolean;
    }

    public void setABoolean( boolean boolean1 )
    {
        aBoolean = boolean1;
    }

}
