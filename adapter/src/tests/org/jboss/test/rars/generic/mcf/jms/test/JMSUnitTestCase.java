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
package org.jboss.test.rars.generic.mcf.jms.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import junit.framework.Test;

import org.jboss.test.rars.RARTest;

/**
 * JMS Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class JMSUnitTestCase extends RARTest
{
   int iterations = 100;
   
   public void testNoTx() throws Throwable
   {
      ConnectionFactory cf = (ConnectionFactory) getBean("ConnectionFactory");
      Queue queue = (Queue) getBean("Queue");
      Connection c = cf.createConnection();
      try
      {
         Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Message m = s.createTextMessage("hello");
         
         MessageProducer p = s.createProducer(queue);
         p.send(m);

         MessageConsumer r = s.createConsumer(queue);
         m = r.receiveNoWait();
         s.close();
         
         s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
         s.close();
      }
      finally
      {
         c.close();
      }

      c = cf.createConnection();
      try
      {
         Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
         s.createTextMessage("goodbye");
      }
      finally
      {
         c.close();
      }

      c = cf.createConnection();
      try
      {
         c.createSession(false, Session.AUTO_ACKNOWLEDGE);
         try
         {
            c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            fail("Should not be here!");
         }
         catch (IllegalStateException expected)
         {
         }
      }
      finally
      {
         c.close();
      }
   }

   public void testStress1() throws Exception
   {
      ConnectionFactory cf = (ConnectionFactory) getBean("QueueConnectionFactory");
      for (int i = 0; i < iterations; ++i)
      {
         Connection c = cf.createConnection();
         try
         {
            c.createSession(false, Session.AUTO_ACKNOWLEDGE);
         }
         finally
         {
            c.close();
         }
      }
   }

   public void testStress2() throws Exception
   {
      ConnectionFactory cf = (ConnectionFactory) getBean("ConnectionFactory");
      for (int i = 0; i < iterations; ++i)
      {
         Connection c = cf.createConnection();
         try
         {
            c.createSession(false, Session.AUTO_ACKNOWLEDGE);
         }
         finally
         {
            c.close();
         }
      }
   }

   public void testStress3() throws Exception
   {
      ConnectionFactory cf = (ConnectionFactory) getBean("QueueConnectionFactory");
      Connection c = cf.createConnection();
      try
      {
         for (int i = 0; i < iterations; ++i)
         {
            Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            s.close();
         }
      }
      finally
      {
         c.close();
      }
   }

   public void testStress4() throws Exception
   {
      ConnectionFactory cf = (ConnectionFactory) getBean("ConnectionFactory");
      Connection c = cf.createConnection();
      Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
      s.close();
      try
      {
         for (int i = 0; i < iterations; ++i)
         {
            s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            s.close();
         }
      }
      finally
      {
         c.close();
      }
      
   }
   
   protected void configureLogging()
   {
      //enableTrace("org.jboss.mq");
      //enableTrace("org.jboss.rars");
   }

   public static Test suite() throws Exception
   {
      return suite(JMSUnitTestCase.class);
   }

   public JMSUnitTestCase (String name)
   {
      super(name);
   }
}
