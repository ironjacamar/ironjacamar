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
package org.jboss.jca.core.connectionmanager.connections.adapter;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.ResultSetInfo;

/**
 * TestConnection
 */
public class TestConnection implements Connection
{
   private TestManagedConnection mc = null;
   private boolean mcIsNull = true;

   /**
    * Constructor
    * @param mc The managed connection
    */
   public TestConnection(TestManagedConnection mc)
   {
      this.mc = mc;
      this.mcIsNull = false;
   }

   /**
    * Is MC null
    * @return The result
    */
   public boolean getMCIsNull()
   {
      return mcIsNull;
   }

   /**
    * Set fail in prepare
    * @param fail fail
    * @param xaCode xaCode
    */
   public void setFailInPrepare(boolean fail, int xaCode)
   {
      mc.setFailInPrepare(fail, xaCode);
   }
   
   /**
    * Set fail in start
    * @param fail fail
    * @param xaCode xaCode
    */
   public void setFailInStart(boolean fail, int xaCode)
   {
   }

   /**
    * Set fail in commit
    * @param fail fail
    * @param xaCode xaCode
    */
   public void setFailInCommit(boolean fail, int xaCode)
   {
      mc.setFailInCommit(fail, xaCode);
   }

   /**
    * Fire connection error
    */
   public void fireConnectionError()
   {
      mc.connectionError(this, new Exception("ConnectionError"));
   }

   /**
    * Is in Tx
    * @return The result
    */
   public boolean isInTx()
   {
      return mc.isInTx();
   }

   /**
    * Set the MC
    * @param mc The mc
    */
   void setMc(TestManagedConnection mc)
   {
      if (mc == null)
      {
         this.mcIsNull = true;
         // We don't touch this.mc
      }
      else
      {
         this.mc = mc;
      }
   }

   /**
    * Get local state
    * @return The value
    */
   public String getLocalState()
   {
      return mc.getLocalState();
   }
   
   /**
    * Begin
    * @exception Exception If error
    */
   public void begin() throws Exception
   {
      mc.sendBegin();
   }
   
   /**
    * Commit
    * @exception Exception If error
    */
   public void commit() throws Exception
   {
      mc.sendCommit();
   }
   
   /**
    * Rollback
    * @exception Exception If error
    */
   public void rollback() throws Exception
   {
      mc.sendRollback();
   }
   
   /**
    * Close
    */
   public void close()
   {
      mc.connectionClosed(this);
      // We don't touch this.mc
   }

   /**
    * Get MC
    * @return The value
    */
   public TestManagedConnection getMC()
   {
      return mc;
   }

   /**
    * Similate a connection error
    * @exception Exception The exception
    */
   public void simulateConnectionError() throws Exception
   {
      Exception e = new Exception("Simulated exception");
      mc.connectionError(this, e);
      throw e;
   }

   /**
    * {@inheritDoc}
    */
   public Interaction createInteraction() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionMetaData getMetaData() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ResultSetInfo getResultSetInfo() throws ResourceException
   {
      return null;
   }
}
