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

/**
 * This class models the various events that occur during the processing of the
 * <code>WorkContext</code>s associated with a <code>Work</code>
 * instance. This interface may be implemented by a <code>WorkContext</code>
 * instance to receive notifications from the <code>WorkManager</code> when
 * the <code>WorkContext</code> is set as the execution context of the
 * <code>Work</code> instance it is associated with.
 * <p>
 * 
 * When a <code>WorkManager</code> sets up the execution context of a
 * <code>Work</code> instance that implements
 * <code>WorkContextProvider</code>, the <code>WorkManager</code> must
 * make the relevant lifecycle notifications if an <code>WorkContext</code>
 * instance implements this interface.
 * <p>
 * 
 * When a <code>Work</code> instance is submitted to the Connector
 * <code>WorkManager</code> using one of the methods that passes in a
 * <code>WorkListener</code> as a parameter, the <code>WorkManager</code>
 * must send <code>Work</code> related notifications to the
 * <code>WorkListener</code> and <code>WorkContext</code> setup related
 * notifications to this interface.
 * <p>
 * 
 * The possible error conditions that might occur during associating an
 * <code>WorkContext</code> with a <code>Work</code> instance is captured
 * in {@link WorkContextErrorCodes}.
 * <p>
 * 
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */

public interface WorkContextLifecycleListener 
{
   /**
    * Invoked when the <code>WorkContext</code> instance was successfully
    * set as the execution context for the <code>Work</code> instance.
    * 
    * @since 1.6
    */
   void contextSetupComplete();
   
   /**
    * Invoked when the <code>WorkContext</code> instance was set as the
    * execution context for the <code>Work</code> instance it was associated
    * with.
    * 
    * @param errorCode
    *            One of the error-codes defined in or subclasses of
    *            {@link WorkContextErrorCodes WorkContextErrorCodes}
    * @since 1.6
    * @see WorkContextErrorCodes
    */
   void contextSetupFailed(String errorCode);
}
