/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.test.unit.anno;

import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516Impl;
import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnection;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnection1;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnectionFactory;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnectionFactory1;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnectionFactoryImpl;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnectionFactoryImpl1;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnectionImpl;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoConnectionImpl1;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoManagedConnection;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoManagedConnectionFactory;

import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.junit.Assert.*;


/**
 * 
 * Proceeding annotations in RA, containing one ManagedConnectionFactory,
 * that participate in 2 @ConnectionDefinitions 
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class ConnectionDefinitionsTestCase extends AnnotationsTestBase
{

   private String pack = "org.jboss.jca.deployers.test.rars.anno.variants";

   @Override
   protected JavaArchive createArchive()
   {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
      jar.addClasses(AnnoManagedConnectionFactory.class, AnnoConnection.class, AnnoConnection1.class,
         AnnoConnectionImpl.class, AnnoConnectionImpl1.class, AnnoConnectionFactory.class,
         AnnoConnectionFactoryImpl.class, AnnoConnectionFactory1.class, AnnoConnectionFactoryImpl1.class,
         AnnoManagedConnection.class);
      return jar;
   }

   @Override
   protected void checkConnector(Connector connector)
   {
      assertTrue(connector instanceof Connector16Impl);
      assertEquals(connector.getVersion(), Version.V_16);

      Connector16Impl con = (Connector16Impl) connector;

      ResourceAdapter rai = con.getResourceadapter();
      assertTrue(rai instanceof ResourceAdapter1516Impl);
      ResourceAdapter1516Impl ra = (ResourceAdapter1516Impl) rai;

      OutboundResourceAdapterImpl out = (OutboundResourceAdapterImpl) ra.getOutboundResourceadapter();
      List<ConnectionDefinition> cds = out.getConnectionDefinitions();
      assertEquals(2, cds.size());
      String bc = pack + ".AnnoConnection";
      for (ConnectionDefinition cd : cds)
      {
         assertTrue(valuesAreEqual(cd.getManagedConnectionFactoryClass(), pack + ".AnnoManagedConnectionFactory"));
         checkProperties(cd.getConfigProperties(), "Byte", "4", "Short", "0");

         if (valuesAreEqual(cd.getConnectionInterface(), bc))
         {
            assertTrue(valuesAreEqual(cd.getConnectionImplClass(), bc + "Impl"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryImplClass(), bc + "FactoryImpl"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryInterface(), bc + "Factory"));
         }
         else
         {
            assertTrue(valuesAreEqual(cd.getConnectionInterface(), bc + "1"));
            assertTrue(valuesAreEqual(cd.getConnectionImplClass(), bc + "Impl1"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryImplClass(), bc + "FactoryImpl1"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryInterface(), bc + "Factory1"));
         }
      }

   }
}
