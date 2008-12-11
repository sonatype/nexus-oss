/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.report;

import com.thoughtworks.qdox.model.JavaClass;

public class ReportBean implements Comparable<ReportBean>
{

    private String testId;
    
    private JavaClass javaClass;
    

    public String getTestId()
    {
        return testId;
    }

    public void setTestId( String testId )
    {
        this.testId = testId;
    }

    public JavaClass getJavaClass()
    {
        return javaClass;
    }

    public void setJavaClass( JavaClass javaClass )
    {
        this.javaClass = javaClass;
    }

    public int compareTo( ReportBean bean )
    {
        return this.testId.compareTo( bean.testId );
    }
    
    
    
}
