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
package org.jboss.test.jca.inflow.test;

import java.lang.reflect.Method;

import javax.resource.spi.endpoint.MessageEndpoint;

import junit.framework.Test;

import org.jboss.test.jca.JCATest;
import org.jboss.test.jca.rar.support.TestMessage;
import org.jboss.test.jca.rar.support.TestMessageListener;
import org.jboss.test.jca.rar.support.TestResourceAdapter;

/**
 * Message Inflow Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class InflowUnitTestCase extends JCATest
{
   Method deliverMessage;
   
   public InflowUnitTestCase (String name) throws Exception
   {
      super(name);
      deliverMessage = TestMessageListener.class.getMethod("deliverMessage", new Class[] { TestMessage.class });
   }

   public void testInflow() throws Throwable
   {
      TestResourceAdapter adapter = (TestResourceAdapter) getBean("TestRAR");
      MessageEndpoint endpoint = adapter.getEndpoint("testInflow");
      if (endpoint == null)
         throw new Exception("Null endpoint");
      TestMessage message = new TestMessage();
      getLog().debug("before " + endpoint);
      endpoint.beforeDelivery(deliverMessage);
      getLog().debug("Sending message " + message + " to " + endpoint);
      ((TestMessageListener) endpoint).deliverMessage(message);
      getLog().debug("after " + endpoint);
      endpoint.afterDelivery();
      if (message.acknowledged == false)
         throw new Exception("MDB did not acknowledge the message");
   }

   public void testBadInflow() throws Throwable
   {
      TestResourceAdapter adapter = (TestResourceAdapter) getBean("TestRAR");
      MessageEndpoint endpoint = adapter.getEndpoint("testInflow");
      if (endpoint == null)
         throw new Exception("Null endpoint");
      TestMessage message = new TestMessage();
      getLog().debug("before " + endpoint);
      endpoint.beforeDelivery(deliverMessage);
      getLog().debug("Sending message " + message + endpoint);
      ((TestMessageListener) endpoint).deliverMessage(message);
      try
      {
         ((TestMessageListener) endpoint).deliverMessage(message);
         fail("Should not be here");
      }
      catch (Exception e)
      {
         checkThrowable(IllegalStateException.class, e);
      }
      finally
      {
         getLog().debug("after " + endpoint);
         endpoint.afterDelivery();
      }
   }
   
   public static Test suite() throws Exception
   {
      return suite(InflowUnitTestCase.class);
   }
}
