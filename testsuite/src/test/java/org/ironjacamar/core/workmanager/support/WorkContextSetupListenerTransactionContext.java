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

package org.ironjacamar.core.workmanager.support;

import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkContextLifecycleListener;

/**
 * WorkContextSetupListenerTransactionContext.
 *
 *
 */
public class WorkContextSetupListenerTransactionContext
      extends TransactionContext implements WorkContextLifecycleListener
{
   /** Serial version UID */
   private static final long serialVersionUID = -3841107265829832325L;

   /**contextSetupComplete*/
   private boolean contextSetupComplete;

   /**contextSetupFailedErrorCode*/
   private String contextSetupFailedErrorCode;

   /**timeStamp for context setup operation */
   private long timeStamp = 0;

   /**
    * isContextSetupComplete
    * @return isContextSetupComplete
    */
   public boolean isContextSetupComplete()
   {
      return contextSetupComplete;
   }

   /**
    * getContextSetupFailedErrorCode
    * @return getContextSetupFailedErrorCode
    */
   public String getContextSetupFailedErrorCode()
   {
      return contextSetupFailedErrorCode;
   }

   /**
    * Context setup complete
    */
   public void contextSetupComplete()
   {
      contextSetupFailedErrorCode = "";
      contextSetupComplete = true;
      timeStamp = System.currentTimeMillis();
   }

   /**
    * Context setup failed
    * @param errorCode The error code
    */
   public void contextSetupFailed(String errorCode)
   {
      contextSetupFailedErrorCode = errorCode;
      contextSetupComplete = false;
      timeStamp = System.currentTimeMillis();
   }

   /**
    * getter
    * @return timestamp
    */
   public long getTimeStamp()
   {
      return timeStamp;
   }
}
