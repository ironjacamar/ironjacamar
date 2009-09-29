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

import java.util.Timer;

import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * This provides a mechanism to pass a bootstrap context to a resource adapter
 * instance when it is bootstrapped. That is, when 
 * (<code>start(BootstrapContext)</code>) method on the 
 * <code>ResourceAdapter</code> class is invoked. The bootstrap
 * context contains references to useful facilities that could be used by the
 * resource adapter instance.
 *
 * @version Java EE Connector Architecture 1.6
 * @author  Ram Jeyaraman, Sivakumar Thyagarajan
 */
public interface BootstrapContext 
{
   /**
    * Provides a handle to a <code>WorkManager</code> instance. The
    * <code>WorkManager</code> instance could be used by a resource adapter to
    * do its work by submitting <code>Work</code> instances for execution. 
    *
    * @return a <code>WorkManager</code> instance.
    */
   WorkManager getWorkManager();

   /**
    * Provides a handle to a <code>XATerminator</code> instance. The
    * <code>XATerminator</code> instance could be used by a resource adapter 
    * to flow-in transaction completion and crash recovery calls from an EIS.
    *
    * @return a <code>XATerminator</code> instance.
    */
   XATerminator getXATerminator();

   /**
    * Creates a new <code>java.util.Timer</code> instance. The
    * <code>Timer</code> instance could be used to perform periodic 
    * <code>Work</code> executions or other tasks.
    *
    * @throws UnavailableException indicates that a 
    * <code>Timer</code> instance is not available. The 
    * request may be retried later.
    *
    * @return a new <code>Timer</code> instance.
    */
   Timer createTimer() throws UnavailableException;
   
   /**
    * A resource adapter can check an application server’s support 
    * for a particular WorkContext type through this method. 
    * This mechanism enables a resource adapter developer to
    * dynamically change the WorkContexts submitted with a Work instance 
    * based on the support provided by the application server.
    *
    * The application server must employ an exact type equality check (that is
    * <code>java.lang.Class.equals(java.lang.Class)</code> check) in
    * this method, to check if it supports the WorkContext type provided
    * by the resource adapter. This method must be idempotent, that is all 
    * calls to this method by a resource adapter for a particular 
    * <code>WorkContext</code> type must return the same boolean value 
    * throughout the lifecycle of that resource adapter instance.
    *
    * @param workContextClass The work context class
    * @return true if the <code>workContextClass</code> is supported
    * by the application server. false if the <code>workContextClass</code>
    * is unsupported or unknown to the application server.
    *
    * @since 1.6
    */
   boolean isContextSupported(Class<? extends WorkContext> workContextClass);
   
   /**
    * Provides a handle to a <code>TransactionSynchronization</code> instance. The
    * <code>TransactionSynchronizationRegistry</code> instance could be used by a 
    * resource adapter to register synchronization objects, get transaction state and
    * status etc. This interface is implemented by the application server by a 
    * stateless service object. The same object can be used by any number of 
    * resource adapter objects with thread safety. 
    *
    * @return a <code>TransactionSynchronizationRegistry</code> instance.
    * @since 1.6
    */
   TransactionSynchronizationRegistry getTransactionSynchronizationRegistry();
}
