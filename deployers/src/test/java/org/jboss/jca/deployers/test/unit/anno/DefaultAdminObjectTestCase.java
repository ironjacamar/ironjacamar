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
package org.jboss.jca.deployers.test.unit.anno;

import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.metadata.spec.ConnectorImpl;
import org.jboss.jca.common.metadata.spec.ResourceAdapterImpl;
import org.jboss.jca.deployers.test.rars.anno.AnnoResourceAdapter;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoAdminObjectImpl;

import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 
 * Proceeds annotations for Admin object with interface by default
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class DefaultAdminObjectTestCase extends AnnotationsTestBase
{

   @Override
   protected JavaArchive createArchive()
   {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
      jar.addClasses(AnnoResourceAdapter.class, AnnoAdminObjectImpl.class);
      return jar;
   }

   @Override
   protected void checkConnector(Connector connector)
   {
      assertTrue(connector instanceof ConnectorImpl);
      assertEquals(connector.getVersion(), Version.V_17);

      ConnectorImpl con = (ConnectorImpl) connector;

      ResourceAdapter rai = con.getResourceadapter();
      assertTrue(rai instanceof ResourceAdapterImpl);
      ResourceAdapterImpl ra = (ResourceAdapterImpl) rai;

      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(1, aos.size());
      AdminObject ao = aos.get(0);
      assertTrue(valuesAreEqual(ao.getAdminobjectClass(),
         "org.jboss.jca.deployers.test.rars.anno.variants.AnnoAdminObjectImpl"));
      checkProperties(ao.getConfigProperties(), "Long", "12345", "Boolean", "false");

   }
}
