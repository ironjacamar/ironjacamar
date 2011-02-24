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

package org.jboss.jca.core.inflow;

import org.jboss.jca.core.inflow.support.HornetQMessageEndpoint;
import org.jboss.jca.core.inflow.support.HornetQMessageEndpointFactory;
import org.jboss.jca.core.spi.rar.Endpoint;
import org.jboss.jca.core.spi.rar.MessageListener;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.resource.spi.ActivationSpec;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for HornetQ
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class HornetQTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(HornetQTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(true);
      URL hornetq = HornetQTestCase.class.getClassLoader().getResource("hornetq.xml");
      InitialContext ic = null;

      try
      {
         embedded.startup();
         embedded.deploy(hornetq);

         ic = new InitialContext();

         ConnectionFactory cf = (ConnectionFactory)ic.lookup("/ConnectionFactory");
         Queue exampleQueue = (Queue)ic.lookup("/queue/ExampleQueue");
         Connection connection = cf.createConnection();

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(exampleQueue);

         MessageConsumer consumer = session.createConsumer(exampleQueue);
         connection.start();

         TextMessage message = session.createTextMessage("This is an example");
         producer.send(message);

         TextMessage receivedMessage = (TextMessage)consumer.receive();
         
         assertEquals(message.getText(), receivedMessage.getText());

         connection.stop();

         consumer.close();
         producer.close();
         session.close();
         connection.close();
      }
      finally
      {
         if (ic != null)
            ic.close();

         embedded.undeploy(hornetq);
         embedded.shutdown();
      }
   }

   /**
    * Inflow
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testInflow() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(true);
      URL hornetq = HornetQTestCase.class.getClassLoader().getResource("hornetq.xml");
      URL hornetqRar = HornetQTestCase.class.getClassLoader().getResource("hornetq-ra.rar");

      InitialContext ic = null;

      try
      {
         embedded.startup();
         embedded.deploy(hornetq);
         embedded.deploy(hornetqRar);

         ic = new InitialContext();

         ResourceAdapterRepository raRepository = 
            embedded.lookup("ResourceAdapterRepository", ResourceAdapterRepository.class);

         assertNotNull(raRepository);

         Set<String> ids = raRepository.getResourceAdapters();

         assertNotNull(ids);
         assertEquals(1, ids.size());

         String hqId = ids.iterator().next();
         assertNotNull(hqId);

         Endpoint endpoint = raRepository.getEndpoint(hqId);
         assertNotNull(endpoint);

         List<MessageListener> listeners = raRepository.getMessageListeners(hqId);
         assertNotNull(listeners);
         assertEquals(1, listeners.size());

         MessageListener listener = listeners.get(0);

         ActivationSpec as = listener.getActivation().getInstance();
         assertNotNull(as);
         assertNotNull(as.getResourceAdapter());

         setProperty(as, "destination", "queue/ExampleQueue");

         CountDownLatch cdl = new CountDownLatch(1);
         HornetQMessageEndpoint me = new HornetQMessageEndpoint(cdl);
         HornetQMessageEndpointFactory mef = new HornetQMessageEndpointFactory(me);
         
         endpoint.activate(mef, as);

         ConnectionFactory cf = (ConnectionFactory)ic.lookup("/ConnectionFactory");
         Queue exampleQueue = (Queue)ic.lookup("/queue/ExampleQueue");
         Connection connection = cf.createConnection();

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(exampleQueue);
         connection.start();

         String txt = "This is an example";
         TextMessage message = session.createTextMessage(txt);
         producer.send(message);

         cdl.await();

         Message receivedMessage = me.getMessage();
         assertTrue(receivedMessage instanceof TextMessage);
         assertEquals(txt, ((TextMessage)receivedMessage).getText());

         endpoint.deactivate(mef, as);

         connection.stop();

         producer.close();
         session.close();
         connection.close();
      }
      finally
      {
         embedded.undeploy(hornetqRar);
         embedded.undeploy(hornetq);
         embedded.shutdown();
      }
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
   }

   /**
    * Set property
    * @param o The object
    * @param name The property name
    * @param value The property value
    * @exception Throwable Thrown if an error occurs
    */
   private void setProperty(Object o, String name, String value) throws Throwable
   {
      Class<?> c = o.getClass();

      String methodName = "set" + name.substring(0, 1).toUpperCase(Locale.US);
      if (name.length() > 1)
         methodName += name.substring(1);

      Method m = c.getMethod(methodName, String.class);
      m.invoke(o, value);
   }
}
