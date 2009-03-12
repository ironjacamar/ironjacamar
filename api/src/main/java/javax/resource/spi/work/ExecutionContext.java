/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package javax.resource.spi.work;

import javax.resource.NotSupportedException;
import javax.transaction.xa.Xid;

/**
 * This class models an execution context (transaction, security, etc) 
 * with which the <code>Work</code> instance must be executed.  
 * This class is provided as a convenience for easily creating 
 * <code>ExecutionContext</code> instances by extending this class
 * and overriding only those methods of interest.
 *
 * <p>Some reasons why it is better for <code>ExecutionContext</code> 
 * to be a class rather than an interface: 
 * <ul><li>There is no need for a resource adapter to implement this class. 
 * It only needs to implement the context information like 
 * transaction, etc.
 * <li>The resource adapter code does not have to change when the 
 * <code>ExecutionContext</code> class evolves. For example, more context 
 * types could be added to the <code>ExecutionContext</code> class 
 * (in the future) without forcing resource adapter implementations 
 * to change.</ul>
 *
 * Note: Resource adapters that are developed for Connectors 1.6 specification
 * compliant application servers and above, are recommended to use
 * the <code>TransactionWorkContext</code> interface instead of this 
 * class. See Chapter.11 Generic Work Context in the Connectors 1.6
 * specification for more details.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public class ExecutionContext 
{
   
   /**
    * transaction context.
    */
   private Xid xid;
   
   /**
    * transaction timeout value.
    */
   private long transactionTimeout = WorkManager.UNKNOWN;
   
   
   /**
    * set a transaction context.
    *
    * @param xid transaction context.
    */
   public void setXid(Xid xid)
   {
      this.xid = xid;
   }

   /**
    * @return an Xid object carrying a transaction context, 
    * if any.
    */
   public Xid getXid() 
   {
      return this.xid;
   }

   /**
    * Set the transaction timeout value for a imported transaction.
    *
    * @param timeout transaction timeout value in seconds. Only positive
    * non-zero values are accepted. Other values are illegal and are 
    * rejected with a <code>NotSupportedException</code>.
    *
    * @throws NotSupportedException thrown to indicate an illegal timeout 
    * value.
    */
   public void setTransactionTimeout(long timeout) 
      throws NotSupportedException 
   {
      if (timeout > 0)
      {
         this.transactionTimeout = timeout;
      }
      else
      {
         throw new NotSupportedException("Illegal timeout value");
      }
   }
   
   /** 
    * Get the transaction timeout value for a imported transaction.
    *
    * @return the specified transaction timeout value in seconds. When no
    * timeout value or an illegal timeout value had been specified, a value of
    * -1 (<code>WorkManager.UNKNOWN</code>) is returned; the timeout 
    * processing of such a transaction depends on the application server 
    * implementation.
    */
   public long getTransactionTimeout() 
   {
      return this.transactionTimeout;
   }
}
