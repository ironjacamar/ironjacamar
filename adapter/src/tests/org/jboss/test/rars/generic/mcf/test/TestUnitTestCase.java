/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.rars.generic.mcf.test;

import junit.framework.Test;

import org.jboss.test.rars.RARTest;
import org.jboss.test.rars.generic.mcf.support.TestConnection;
import org.jboss.test.rars.generic.mcf.support.TestConnectionFactory;

/**
 * NoTx Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class TestUnitTestCase extends RARTest
{
   public TestUnitTestCase (String name)
   {
      super(name);
   }

   public void testNoTx() throws Throwable
   {
      TestConnectionFactory cf = (TestConnectionFactory) getBean("ConnectionFactory");
      TestConnection c = cf.getConnection();
      assertNotNull("Should have a connection", c);
      c.close();
      c = cf.getConnection();
      assertNotNull("Should have a connection", c);
      c.close();
   }

   public static Test suite() throws Exception
   {
      return suite(TestUnitTestCase.class);
   }
}
