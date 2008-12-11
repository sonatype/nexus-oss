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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

public class ReportWriter
{

    private File sourceDir;

    public ReportWriter( File sourceDir )
    {
        this.sourceDir = sourceDir;
    }

    public void writeReport()
    {

        // parse the java doc
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSourceTree( this.sourceDir );
        // all parsed

        // now find all of the test classes
        List<JavaClass> testClasses = new ArrayList<JavaClass>();
        JavaClass[] classes = builder.getClasses();

        List<JavaClass> classesWithoutDescriptions = new ArrayList<JavaClass>();

        for ( int ii = 0; ii < classes.length; ii++ )
        {
            JavaClass javaClass = classes[ii];
            if ( !javaClass.isAbstract() && classHasMethodWithTestAnnotation( javaClass ) )
            {
                testClasses.add( javaClass );

                // check if we have a javadoc comment
                if ( StringUtils.isEmpty( javaClass.getComment() ) )
                {
                    classesWithoutDescriptions.add( javaClass );
                }
            }
        }

        List<ReportBean> beans = new ArrayList<ReportBean>();

        for ( JavaClass javaClass : testClasses )
        {
            ReportBean bean = new ReportBean();
            bean.setJavaClass( javaClass );
            bean.setTestId( this.getTestId( javaClass ) );

            // add to the collection
            beans.add( bean );
        }

        // sort the beans.
        Collections.sort( beans );

        // now this would be nice to be configurable, but move the sample tests to the top of the list
        this.fudgeOrder( beans );

        // now write the report // TODO: get from container
        new ConsoleWikiReport().writeReport( beans );

        // print errors here.. this should be handled better, but there are no plans for this 'report' anyway.
        if ( !classesWithoutDescriptions.isEmpty() )
        {

            System.err.println( "\n\n\n\nErrors:\n" );

            for ( Iterator<JavaClass> iter = classesWithoutDescriptions.iterator(); iter.hasNext(); )
            {
                JavaClass javaClass = (JavaClass) iter.next();
                System.err.println( javaClass.getName() + " is missing a javadoc comment." );
            }
        }

    }

    private void fudgeOrder( List<ReportBean> beans )
    {
        ReportBean nexus166Sample = this.removeBeanFromList( beans, "NEXUS-166" );
        ReportBean nexus262SampleProxy = this.removeBeanFromList( beans, "NEXUS-262" );

        beans.add( 0, nexus262SampleProxy );
        beans.add( 0, nexus166Sample );
    }

    /**
     * Looks for any class that contains a Junit 4 annotation <code>@Test</code>.
     * @param javaClass
     * @return
     */
    private static boolean classHasMethodWithTestAnnotation( JavaClass javaClass )
    {
        JavaMethod[] methods = javaClass.getMethods();
        for ( JavaMethod javaMethod : methods )
        {
            List<Annotation> annotations = Arrays.asList( javaMethod.getAnnotations() );

            for ( Iterator<Annotation> iter = annotations.iterator(); iter.hasNext(); )
            {
                Annotation annotation = iter.next();

                if ( annotation.getType().getValue().equals( Test.class.getName() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    private String getTestId( JavaClass javaClass )
    {
        String packageName = javaClass.getPackage();
        String testId = packageName.substring( packageName.lastIndexOf( '.' ) + 1, packageName.length() ).toLowerCase();

        if ( testId.startsWith( "nexus" ) )
        {
            testId = testId.replace( "nexus", "NEXUS-" );
        }
        else
        {
            throw new RuntimeException(
                                        "The class: "
                                            + javaClass.getName()
                                            + " is not using the correct format for package.  It sould be something like: <org.sonatype.nexus.inegrationtests>.nexusXXX.NexusXXXDescription. it was: "
                                            + javaClass.getPackage() + "." + javaClass.getName() );
        }
        return testId;
    }

    private ReportBean removeBeanFromList( List<ReportBean> beans, String testId )
    {
        for ( ReportBean bean : beans )
        {
            if ( testId.equals( bean.getTestId() ) )
            {

                beans.remove( bean );
                return bean;
            }
        }
        return null;
    }

    public static void main( String[] args )
    {
        File currentDir = new File( "." );

        File sourceDir = null;

        File parentFile = currentDir.getAbsoluteFile();
        while ( parentFile != null )
        {

            if ( parentFile.getName().equals( "nexus-test-harness-launcher" ) )
            {
                sourceDir = new File( parentFile, "/src/test/java" );
                // we are done with this loop
                break;
            }
            // change the parent
            parentFile = parentFile.getParentFile();
        }

        // make sure we have something
        if ( sourceDir == null )
        {
            System.err.println( "Could not figre out the source dir: nexus-test-harness-launcher/src/test/java" );
        }

        // now we can write the report
        new ReportWriter( sourceDir ).writeReport();

    }

}
