/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.listener;

import org.jboss.jca.core.tx.rars.txlog.TxLogConnection;
import org.jboss.jca.core.tx.rars.txlog.TxLogConnectionFactory;
import org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * An enlist / delist XA interleaving test case
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class EnlistDelistXAInterleavingTestCase extends EnlistDelist
{
   private static Logger log = Logger.getLogger(EnlistDelistXAInterleavingTestCase.class);

   /**
    * Basic
    *
    * @throws Throwable Thrown in case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      Context context = null;

      ResourceAdapterArchive raa = createRar();
      ResourceAdaptersDescriptor dashRaXml = createXATxDeployment(true);

      try
      {
         embedded.deploy(raa);
         embedded.deploy(dashRaXml);

         context = new InitialContext();

         UserTransaction ut = (UserTransaction)context.lookup("java:/UserTransaction");
         assertNotNull(ut);

         TxLogConnectionFactory cf = (TxLogConnectionFactory)context.lookup("java:/eis/TxLogConnectionFactory");
         assertNotNull(cf);

         ut.begin();

         TxLogConnection c = cf.getConnection();
         c.close();

         c = cf.getConnection();
         c.close();

         ut.commit();

         // Verify
         c = cf.getConnection();

         log.infof("Connection=%s", c);

         assertEquals("3D5D", c.getState());
         c.clearState();
         c.close();
      }
      catch (Throwable t)
      {
         log.error(t.getMessage(), t);
         fail(t.getMessage());
      }
      finally
      {
         embedded.undeploy(dashRaXml);
         embedded.undeploy(raa);

         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }
}
