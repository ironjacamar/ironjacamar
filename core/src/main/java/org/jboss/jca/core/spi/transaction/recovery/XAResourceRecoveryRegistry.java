/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.spi.transaction.recovery;

/**
 * The transaction management system may require assistance from resource
 * managers in order to recover crashed XA transactions. By registering
 * an XAResourceRecovery instance with the XAResourceRecoveryRegistry,
 * resource manager connectors provide a way for the recovery system to
 * callback to them and obtain the necessary information.
 *
 * This is useful for e.g. JDBC connection pools or JCA connectors that
 * don't want to expose connection parameters to the transaction system.
 * The connectors are responsible for instantiating a connection and
 * using it to instantiate a set of XAResources. These are then exposed to
 * the recovery system via the registered XAResourceRecovery instance.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 * @version $Revision$
 * @see XAResourceRecovery
 */
public interface XAResourceRecoveryRegistry
{
   /*
     Implementor's note:
     Although the transaction manager in JBossAS is pluggable, reading the JBossTS
     recovery documentation may give some insight into the design of this
     recovery system interface. The forum thread at
     http://www.jboss.com/index.html?module=bb&op=viewtopic&t=100435
     may also be of interest.
   */

   /**
    * Register an XAResourceRecovery instance with the transaction recovery system.
    * This should be called by deployers that are deploying a new XA aware
    * module that needs recovery support. For example, a database
    * connection pool, JMS adapter or JCA connector.
    *
    * @param recovery The XAResourceRecovery instance to register.
    */
   public void addXAResourceRecovery(XAResourceRecovery recovery);

   /**
    * Unregister an XAResourceRecovery instance from the transaction recovery system.
    * This should be called when an XA aware module is undeployed, to inform the
    * recovery system that recovery is no longer required or supported.
    *
    * Note this method may block whilst an ongoing recovery operation is completed.
    * Recovery behavior is undefined if the undeployment does not wait for this
    * operation to complete.
    *
    * @param recovery The XAResourceRecovery instance to unregister.
    * Implementations should fail silent if an attempt is made to unregister
    * an XAResourceRecovery instance that is not currently registered.
    */
   public void removeXAResourceRecovery(XAResourceRecovery recovery);
}
