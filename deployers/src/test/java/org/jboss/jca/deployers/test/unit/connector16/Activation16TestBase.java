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
package org.jboss.jca.deployers.test.unit.connector16;

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.common.metadata.spec.ConnectorImpl;
import org.jboss.jca.core.spi.rar.Endpoint;
import org.jboss.jca.core.spi.rar.MessageListener;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.test.rars.anno.AnnoActivationSpec;
import org.jboss.jca.deployers.test.rars.anno.AnnoAdminObject;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnectionFactory;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnectionImpl;
import org.jboss.jca.deployers.test.rars.anno.AnnoManagedConnectionFactory;
import org.jboss.jca.deployers.test.rars.anno.AnnoResourceAdapter;
import org.jboss.jca.deployers.test.unit.connector15.Activation15TestBase;

import java.util.List;
import java.util.Set;

import javax.resource.spi.ActivationSpec;

import static org.junit.Assert.*;

/**
 * 
 * A ActivationTestBase for 1.6 spec. tests
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */

public abstract class Activation16TestBase extends Activation15TestBase
{
   /**
    * RA repository
    */
   @Inject(name = "ResourceAdapterRepository")
   protected ResourceAdapterRepository raRepository;

   /**
    * 
    * checks, if metadata is of appropriate type
    * 
    * @param piId - metadata name
    * @throws Exception in case of error
    */
   @Override
   public void checkMetadata(String piId) throws Exception
   {
      assertTrue(mdr.getResourceAdapter(piId) instanceof ConnectorImpl);
   }

   /**
    * Test connection
    * @param connectionFactory to test
    * @param first config property
    * @param second config property
    *
    * @exception Throwable Thrown if case of an error
    */
   public void testConnection(AnnoConnectionFactory connectionFactory, byte first, short second) throws Throwable
   {
      assertNotNull(connectionFactory);
      AnnoConnectionImpl connection1 = (AnnoConnectionImpl) connectionFactory.getConnection();
      assertNotNull(connection1);
      AnnoManagedConnectionFactory mcf = connection1.getMCF();
      assertNotNull(mcf);
      log.info("MCF:" + mcf + "//1//" + mcf.getFirst() + "//2//" + mcf.getSecond());
      assertEquals(first, (byte) mcf.getFirst());
      assertEquals(second, (short) mcf.getSecond());
      connection1.close();
   }

   /**
    * Test admin object
    * @param adminObject to test 
    * @param first config property
    * @param second config property
   *
   * @exception Throwable Thrown if case of an error
   */
   public void testAdminOjbect(AnnoAdminObject adminObject, long first, boolean second) throws Throwable
   {
      assertNotNull(adminObject);
      log.info("AO:" + adminObject + "//1//" + adminObject.getFirst() + "//2//" + adminObject.getSecond());
      assertEquals(first, (long) adminObject.getFirst());
      assertEquals(second, adminObject.getSecond());
      /* 
       assertNotNull(adminObject1);
       assertEquals( 3.14f, (float) adminObject1.getFirst());
       assertEquals("B", adminObject1.getSecond());
      */
   }

   /**
    * Test activation and check parameters values
    * 
    * @param clazz   class name for MessageListener
    * @param a1 first ActivationSpec config property
    * @param a2 second ActivationSpec config property
    * @param r1 first ResourceAdapter config property
    * @param r2 first ResourceAdapter config property
    * 
    * @exception Throwable
    *                Thrown if case of an error
    */
   public void testActivation(Class clazz, char a1, double a2, String r1, int r2) throws Throwable
   {

      Set<String> ids = raRepository.getResourceAdapters(clazz);

      assertNotNull(ids);
      //assertEquals(1, ids.size());

      String piId = ids.iterator().next();
      assertNotNull(piId);

      Endpoint endpoint = raRepository.getEndpoint(piId);
      assertNotNull(endpoint);

      List<MessageListener> listeners = raRepository.getMessageListeners(piId);
      assertNotNull(listeners);

      MessageListener listener = listeners.get(0);

      ActivationSpec as = listener.getActivation().createInstance();
      assertNotNull(as);
      assertNotNull(as.getResourceAdapter());

      AnnoActivationSpec tas = (AnnoActivationSpec) as;
      log.info("AS:" + tas + "//1//" + tas.getFirst() + "//2//" + tas.getSecond());
      assertEquals(new Character(a1), tas.getFirst());
      assertEquals(new Double(a2), tas.getSecond());
      assertTrue(tas.getResourceAdapter() instanceof AnnoResourceAdapter);
      AnnoResourceAdapter tra = (AnnoResourceAdapter) tas.getResourceAdapter();
      log.info("RA:" + tra + "//1//" + tra.getFirst() + "//2//" + tra.getSecond());
      assertEquals(r1, tra.getFirst());
      assertEquals(new Integer(r2), tra.getSecond());
   }

}
