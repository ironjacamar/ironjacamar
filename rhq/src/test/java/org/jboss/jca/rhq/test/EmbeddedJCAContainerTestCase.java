/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jca.rhq.test;

import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import javax.naming.InitialContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * EmbeddedJCAContainerTestCase
 * 
 * @author Jeff Zhang
 *
 */
public class EmbeddedJCAContainerTestCase
{
   /** Embedded */
   private static Embedded embedded;
   
   /**
    * Basic testGetManagementRepository
    * 
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testGetManagementRepository() throws Throwable
   {
      ManagementRepository manRepo = embedded.lookup("ManagementRepository", ManagementRepository.class);
      assertNotNull(manRepo);
   }
   
   /**
    * Tests jndi bindings when undeploy rar resources.
    * 
    * @throws Throwable the exception
    */
   @Test
   public void testUnDeployRAR() throws Throwable
   {
      URL deployedUrl = XATestCase.class.getResource("/xa.rar");
      embedded.deploy(deployedUrl);
      
      InitialContext context = new InitialContext();
      assertNotNull(context.lookup("java:/eis/XA"));
      assertNotNull(context.lookup("java:/XAAdminObjectImpl"));
      
      embedded.undeploy(deployedUrl);
      try
      {
         context.lookup("java:/eis/XA");
         fail("ConnectionFactory of: java:/eis/XA should be unboundded.");
      }
      catch (Exception e)
      {
         assertEquals(javax.naming.NameNotFoundException.class, e.getClass());
      }
      
      try
      {
         context.lookup("java:/XAAdminObjectImpl");
         fail("AdminObject of: java:/XAAdminObjectImpl should be unboundded.");
      }
      catch (Exception e)
      {
         assertEquals(javax.naming.NameNotFoundException.class, e.getClass());
      }
      
   }
   
   /**
    * Tests UnDeploy DataSource
    * 
    * @throws Throwable the exception
    */
   @Test
   public void testUnDeployDataSource() throws Throwable
   {
      URL jdbcURL = DsTestCase.class.getResource("/jdbc-local.rar");
      embedded.deploy(jdbcURL);
      
      URL dsURL = DsTestCase.class.getResource("/h2-ds.xml");
      embedded.deploy(dsURL);
      
      InitialContext context = new InitialContext();
      assertNotNull(context.lookup("java:/H2DS"));
      
      embedded.undeploy(dsURL);
      try
      {
         context.lookup("java:/H2DS");
         fail("DataSource of: java:/H2DS should be unboundded.");
      }
      catch (Exception e)
      {
         assertEquals(javax.naming.NameNotFoundException.class, e.getClass());
      }
      embedded.undeploy(jdbcURL);
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();
      // Startup
      embedded.startup();

   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Shutdown embedded
      embedded.shutdown();
      // Set embedded to null
      embedded = null;
   }
}
