/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.embedded.junit4;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.Embedded;

import java.net.URL;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit4 support tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class JUnit4TestCase
{
   /** The embedded instance */
   @Inject
   private static Embedded embedded;

   /** The transaction manager */
   @Inject
   private static TransactionManager tm;

   /** The deployment repository */
   @Inject
   private static DeploymentRepository dr;

   /** The user transaction */
   @Resource(mappedName = "java:/UserTransaction")
   private static UserTransaction ut;

   /**
    * Deployment 1
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static URL deploymentStaticOne() throws Throwable
   {
      return new URL("http://blah.com/static");
   }
  
   /**
    * Deployment 1
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private URL deploymentOne() throws Throwable
   {
      return new URL("http://blah.com");
   }
   
   /**
    * Basic
    * @throws Throwable In case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      System.out.println("testBasic");
   }
   
   /**
    * Before
    * @exception Throwable If an error occurs
    */
   @Before
   public void before() throws Throwable
   {
      System.out.println("before");
   }

   /**
    * After
    * @exception Throwable If an error occurs
    */
   @After
   public void after() throws Throwable
   {
      System.out.println("after");
   }

   /**
    * BeforeClass
    * @exception Throwable If an error occurs
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      System.out.println("beforeClass");
      assertNotNull(embedded);
      assertTrue(embedded.isRunning());
      assertNotNull(tm);
      assertNotNull(ut);
      assertNotNull(dr);
   }

   /**
    * AfterClass
    * @exception Throwable If an error occurs
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      System.out.println("afterClass");
      assertNotNull(embedded);
      assertTrue(embedded.isRunning());
      assertNotNull(tm);
      assertNotNull(ut);
      assertNotNull(dr);
   }
}
