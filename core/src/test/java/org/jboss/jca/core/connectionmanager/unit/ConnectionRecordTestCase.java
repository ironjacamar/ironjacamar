/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.unit;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.common.MockConnectionRequestInfo;
import org.jboss.jca.core.connectionmanager.listener.NoTxConnectionListener;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ConnectionRecordTestCase.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev$ $Date$
 *
 */
public class ConnectionRecordTestCase
{
   private static ConnectionRecord cr = null;    
   private static Object connection = new Object();
   private static MockConnectionRequestInfo cri = new MockConnectionRequestInfo();
   
   /**
    * init.
    */
   @BeforeClass
   public static void init()
   {
      cr = new ConnectionRecord(null, connection, cri);
   }
   
   /**
    * testConnectionListenerIsNull.
    */
   @Test
   public void testConnectionListenerIsNull()
   {
      assertNull(cr.getConnectionListener());
   }

   /**
    * testConnectionListenerIsNotNull.
    */
   @Test
   public void testConnectionListenerIsNotNull()
   {
      ConnectionRecord other = 
         new ConnectionRecord(new NoTxConnectionListener(null, null, null, null,
                                                         FlushStrategy.FAILING_CONNECTION_ONLY, null),
                              connection, cri);
      assertNotNull(other.getConnectionListener());
   }
   
   /**
    * testConnectionIsNotNull.
    */
   @Test
   public void testConnectionIsNotNull()
   {
      assertNotNull(cr.getConnection());
   }
   
   /**
    * testCriIsNotNull.
    */
   @Test
   public void testCriIsNotNull()
   {
      assertNotNull(cr.getCri());
   }
}
