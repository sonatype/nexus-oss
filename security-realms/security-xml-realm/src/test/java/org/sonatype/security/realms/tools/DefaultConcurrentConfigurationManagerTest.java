/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.tools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.is;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.sonatype.security.AbstractSecurityTestCase;

import com.google.common.base.Throwables;

public class DefaultConcurrentConfigurationManagerTest extends AbstractSecurityTestCase
{
    private ConfigurationManager configMgr;
    private long lockTimeout = 3;

    @Override
    public void configure( Properties properties )
    {
        properties.put( "security.configmgr.locktimeout", lockTimeout);
        super.configure( properties );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        configMgr = lookup(ConfigurationManager.class);
    }
    
    public void testMultipleReaders()
        throws Exception
    {
        final CountDownLatch threadInitLatch = new CountDownLatch(1);
        final CountDownLatch threadStopLatch = new CountDownLatch(1);
        
        try
        {
            //Start a thread that acquires read lock and waits for latch
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        configMgr.runRead(new ConfigurationManagerAction()
                        {

                            @Override
                            public void run()
                                throws Exception
                            {
                                //We hold a read lock at this point
                                try
                                {
                                    threadInitLatch.countDown();
                                    threadStopLatch.await();
                                }
                                catch (InterruptedException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                            
                        });
                    }
                    catch(Exception e)
                    {
                        throw Throwables.propagate(e);
                    }
                    
                }
            });
            
            t.start();
            
            //Wait for thread to initialize
            threadInitLatch.await();
            
            //At this point, the thread is holding a read lock. Make sure that we can acquire another read lock
            configMgr.runRead(new ConfigurationManagerAction()
            {
                @Override
                public void run()
                    throws Exception
                {
                    //We were able to acquire read lock
                    threadStopLatch.countDown();
                }
            });
        }
        catch(IllegalStateException e)
        {
            //We timed out trying to acquire 2nd read lock
            Assert.fail("Unable to have two simultaneous reader");
        }
        finally
        {
            threadInitLatch.countDown();
            threadStopLatch.countDown();
        }
    }
    
    public void testOneWriter()
        throws Exception
    {
        final CountDownLatch threadInitLatch = new CountDownLatch(1);
        final CountDownLatch threadStopLatch = new CountDownLatch(1);
        
        try
        {
            //Start a thread that acquires write lock and waits for latch
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        configMgr.runWrite(new ConfigurationManagerAction()
                        {

                            @Override
                            public void run()
                                throws Exception
                            {
                                //We hold a write lock at this point
                                try
                                {
                                    threadInitLatch.countDown();
                                    threadStopLatch.await();
                                }
                                catch (InterruptedException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                            
                        });
                    }
                    catch(Exception e)
                    {
                        throw Throwables.propagate(e);
                    }
                }
            });
            
            t.start();
            
            //Wait for thread to initialize
            threadInitLatch.await();
            
            //At this point, the thread is holding a write lock. Make sure we cannot acquire another write lock
            configMgr.runWrite(new ConfigurationManagerAction()
            {
                @Override
                public void run()
                    throws Exception
                {
                    //We were able to acquire a 2nd write lock
                    threadStopLatch.countDown();
                    Assert.fail("Multiple writers allowed");
                }
            });
        }
        catch(IllegalStateException e)
        {
            //We timed out trying to acquire 2nd write lock. This is the expected behavior
        }
        finally
        {
            threadInitLatch.countDown();
            threadStopLatch.countDown();
        }
    }
    
    public void testLockTimeout()
        throws Exception
    {
        final CountDownLatch threadInitLatch = new CountDownLatch(1);
        final CountDownLatch threadStopLatch = new CountDownLatch(1);
        long startTime = 0;
        
        try
        {
            //Start a thread that acquires write lock and waits for latch
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        configMgr.runWrite(new ConfigurationManagerAction()
                        {

                            @Override
                            public void run()
                                throws Exception
                            {
                                //We hold a write lock at this point
                                try
                                {
                                    threadInitLatch.countDown();
                                    threadStopLatch.await();
                                }
                                catch (InterruptedException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                            
                        });
                    }
                    catch(Exception e)
                    {
                        throw Throwables.propagate(e);
                    }
                }
            });
            
            t.start();
            
            //Wait for thread to initialize
            threadInitLatch.await();
            
            //Try to acquire another write lock out. This should time out
            startTime = System.currentTimeMillis();
            configMgr.runWrite(new ConfigurationManagerAction()
            {
                @Override
                public void run()
                    throws Exception
                {
                }
            });
            
            //Should not get here
            Assert.fail("Multiple writers allowed");
        }
        catch(IllegalStateException e)
        {
            //We timed out trying to acquire 2nd write lock. This is the expected behavior
            
            //Verify that that the operation timed out at the specified timeout value
            long elapsedTime = System.currentTimeMillis() - startTime;
            assertThat(elapsedTime, is(lessThan((lockTimeout + 1) * 1000)));
        }
        finally
        {
            threadInitLatch.countDown();
            threadStopLatch.countDown();
        }
    }
}
