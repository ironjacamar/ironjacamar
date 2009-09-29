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

package javax.resource.spi;

/** 
 * This interface may be optionally implemented by a 
 * <code>ManagedConnectionFactory</code> to provide its level of transaction
 * support at runtime.
 *
 * <p>When a <code>ManagedConnectionFactory</code> implements this interface,
 * the application server uses the <code>TransactionSupportLevel</code> returned
 * by getTransactionSupport() method and not the value specified in the 
 * resource adapter deployment descriptor or deployer configuration
 *
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
public interface TransactionSupport extends java.io.Serializable 
{
   /**
    * An enumerated type that represents the levels of transaction support
    * a resource adapter may support.
    *
    * @since 1.6
    * @version Java EE Connector Architecture 1.6
    */
   public enum TransactionSupportLevel 
   {
      /**
       * The resource adapter supports neither resource manager nor JTA 
       * transactions.
       * @since 1.6
       */
      NoTransaction, 

      /**
       * The resource adapter supports resource manager local transactions 
       * by implementing the <code>LocalTransaction</code> interface.
       * @since 1.6
       */
      LocalTransaction, 

      /**
       * The resource adapter supports both resource manager local 
       * and JTA transactions by implementing the <code>LocalTransaction</code>
       * and <code>XAResource</code> interfaces.
       * @since 1.6
       */
      XATransaction 
   };

   /**
    * Get the level of transaction support, supported by the 
    * <code>ManagedConnectionFactory</code>. A resource adapter must always
    * return a level of transaction support whose ordinal value in
    * <code>TransactionSupportLevel</code> enum is equal to or lesser than
    * the resource adapter's transaction support classification.
    *
    * @return The transaction support level
    * @since 1.6
    */
   public TransactionSupportLevel getTransactionSupport();
}
