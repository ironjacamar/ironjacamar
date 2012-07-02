/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.spec.chapter10.api;

import org.jboss.jca.core.workmanager.rars.wm.ContextConnection;
import org.jboss.jca.core.workmanager.rars.wm.ContextConnectionFactory;
import org.jboss.jca.core.workmanager.rars.wm.ContextConnectionFactoryImpl;
import org.jboss.jca.core.workmanager.rars.wm.ContextConnectionImpl;
import org.jboss.jca.core.workmanager.rars.wm.ContextManagedConnection;
import org.jboss.jca.core.workmanager.rars.wm.ContextManagedConnectionFactory;
import org.jboss.jca.core.workmanager.rars.wm.ContextManagedConnectionMetaData;
import org.jboss.jca.core.workmanager.rars.wm.ContextRaMetaData;
import org.jboss.jca.core.workmanager.rars.wm.ContextResourceAdapter;
import org.jboss.jca.core.workmanager.spec.chapter10.common.AssociationWork;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkContextDeploymentTestCase
 *
 * Tests for Work instantion, implementing ResourceAdapterAssociation
 * JCA 1.6 spec, chapter 10.3.10
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
public class WorkResourceAdapterAssociationTestCase
{
   private static Logger log = Logger.getLogger(WorkResourceAdapterAssociationTestCase.class);

   @Resource(mappedName = "java:/eis/raa")
   private ContextConnectionFactory cf;

   /**
    * Creates deployment
    * @return .rar archive
    */
   @Deployment
   public static ResourceAdapterArchive createAnnotationDeployment()
   {

      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "raa.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "raa.jar");
      ja.addClasses(ContextConnection.class, ContextConnectionFactory.class, ContextConnectionFactoryImpl.class,
            ContextConnectionImpl.class, ContextManagedConnection.class, ContextManagedConnectionFactory.class,
            ContextManagedConnectionMetaData.class, ContextRaMetaData.class, ContextResourceAdapter.class);

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * test 
    * @throws Exception in case of error
    */
   @Test
   public void test() throws Exception
   {
      ContextConnectionFactoryImpl impl = (ContextConnectionFactoryImpl) cf;
      ContextResourceAdapter ra = (ContextResourceAdapter) impl.getResourceAdapter();
      AssociationWork work = new AssociationWork();
      ra.doWork(work);
      log.info("//////ra:" + ra + "//work:" + work.getResourceAdapter());
      assertTrue(ra.equals(work.getResourceAdapter()));
   }
}
