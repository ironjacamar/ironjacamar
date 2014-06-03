/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.ironjacamar;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.metadata.XMLParserTestBase;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;

import java.util.List;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * IronJacamarParser11WOConnDefsTestCase
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class IronJacamarParser11WOConnDefsTestCase extends XMLParserTestBase
{
   /**
   *
   * beforeClass method
   *
   */
   @BeforeClass
   public static void beforeClass()
   {
      parser = new IronJacamarParser();
      parsedFileName = "ironjacamar/unit/ironjacamar-wo-connection-definitions.xml";
   }

   /**
    * 
    * checks, if all IronJacamars 1.1 properties set correctly
    * {inheritDoc}
    * @param result of parsing
    */
   @Override
   public void checkMetadata(JCAMetadata result)
   {
      ActivationImpl ra = (ActivationImpl) result;
      
      assertEquals(null, ra.getBootstrapContext());
      assertEquals(null, ra.getBeanValidationGroups());
      assertEquals(null, ra.getConfigProperties());
      assertEquals(null, ra.getTransactionSupport());
      
      assertEquals(null, ra.getConnectionDefinitions());
      
      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(1, aos.size());
      //admin object 1
      AdminObject ao = aos.get(0);
      assertEquals(null, ao.getClassName());
      assertEquals("java:jboss/name9", ao.getJndiName());
      assertEquals(null, ao.getPoolName());
      //default values
      assertTrue(ao.isEnabled());
      assertTrue(ao.isUseJavaContext());
      
   }

}
