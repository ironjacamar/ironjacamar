/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The core bundle.
 *
 * Message ids ranging from 000000 to 009999 inclusively.
 */
@MessageBundle(projectCode = "IJ2")
public interface CoreBundle
{
   // RECOVERY (900)
   
   /**
    * Error during connection close
    * @return The value
    */
   @Message(id = 951, value = "Error during connection close")
   public String errorDuringConnectionClose();
   
   /**
    * Error during recovery initialization
    * @return The value
    */
   @Message(id = 952, value = "Error during recovery initialization")
   public String errorDuringRecoveryInitialization();
   
   /**
    * Error during recovery shutdown
    * @return The value
    */
   @Message(id = 953, value = "Error during recovery shutdown")
   public String errorDuringRecoveryShutdown();

   // TRANSCATION (1100)

   /**
    * Trying to start a new tx when old is not complete
    * @param oldXid old xid
    * @param newXid new xid
    * @param flags flags
    * @return The value
    */
   @Message(id = 1151, value = "Trying to start a new transaction when old is not complete: Old: %s, New %s, Flags %s")
   public String tryingStartNewTxWhenOldNotComplete(Object oldXid, Object newXid, int flags);

   /**
    * Trying to start a new tx with wrong flags
    * @param xid xid
    * @param flags flags
    * @return The value
    */
   @Message(id = 1152, value = "Trying to start a new transaction with wrong flags: New %s, Flags %s")
   public String tryingStartNewTxWithWrongFlags(Object xid, int flags);
   
   /**
    * Error trying to start local tx
    * @return The value
    */
   @Message(id = 1153, value = "Error trying to start local transaction")
   public String errorTryingStartLocalTx();
   
   /**
    * Throwable trying to start local transaction
    * @return The value
    */
   @Message(id = 1154, value = "Throwable trying to start local transaction")
   public String throwableTryingStartLocalTx();

   /**
    * Wrong xid in commit
    * @param currentXid current xid
    * @param xid xid
    * @return The value
    */
   @Message(id = 1155, value = "Wrong xid in commit: Expected: %s, Got: %s")
   public String wrongXidInCommit(Object currentXid, Object xid);
   
   /**
    * Could not commit local tx
    * @return The value
    */
   @Message(id = 1156, value = "Could not commit local transaction")
   public String couldNotCommitLocalTx();
   
   /**
    * Forget not supported in local tx
    * @return The value
    */
   @Message(id = 1157, value = "Forget not supported in local transaction")
   public String forgetNotSupportedInLocalTx();
   
   /**
    * No recover with local-tx only resource managers
    * @return The value
    */
   @Message(id = 1158, value = "No recovery for LocalTransaction only resource manager")
   public String noRecoverWithLocalTxResourceManagers();
   
   /**
    * Wrong xid in rollback
    * @param currentXid current xid
    * @param xid xid
    * @return The value
    */
   @Message(id = 1159, value = "Wrong xid in rollback: Expected: %s, Got: %s")
   public String wrongXidInRollback(Object currentXid, Object xid);

   
   /**
    * Could not rollback local tx
    * @return The value
    */
   @Message(id = 1160, value = "Could not rollback local transaction")
   public String couldNotRollbackLocalTx();
   
}
