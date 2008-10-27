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
