/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
//package org.sonatype.nexus.integrationtests.runner;

//import java.lang.reflect.Method;

//import org.junit.runner.notification.Failure;
//import org.junit.runner.notification.RunListener;
//import org.junit.runner.notification.RunNotifier;
//import org.junit.runners.model.FrameworkMethod;
//import org.junit.runners.model.InitializationError;

//public class ConsoleLoggingRunner extends org.junit.runners.BlockJUnit4ClassRunner
//
//{
//
//    public ConsoleLoggingRunner( Class<?> klass )
//        throws InitializationError
//    {
//        super( klass );
//    }
//
//    @Override
//    protected void runChild( FrameworkMethod frameworkMethod, RunNotifier runNotifier )
//    {
//        Method method = frameworkMethod.getMethod();
//        String description = method.getDeclaringClass().getName() +": "+ frameworkMethod.getName();
//        
//        System.out.println( "Running: " + description );
//        
//        ConsoleLoggingRunListener listener = new ConsoleLoggingRunListener( description );
//        runNotifier.addListener( listener );
//     
//        long startTime  = System.currentTimeMillis();
//        
//        super.runChild( frameworkMethod, runNotifier );
//        
//        String durration = (System.currentTimeMillis() - startTime) +"ms";
//        
//        System.out.println( "Test Finished in: " + durration );
//        
//        runNotifier.removeListener( listener );
//    }
//
//
//    class ConsoleLoggingRunListener extends RunListener
//    {
//     
//        private String description;
//        
//        public ConsoleLoggingRunListener( String description )
//        {
//            this.description = description;
//        }
//        
//        @Override
//        public void testAssumptionFailure( Failure failure )
//        {   
//            System.out.println( description + ": Failed");
//        }
//
//        @Override
//        public void testFailure( Failure failure )
//            throws Exception
//        {
//            System.out.println( description + ": Failed");
//        }        
//    }
//    
//}
