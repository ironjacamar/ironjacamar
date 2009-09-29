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
 * This class models the possible error conditions that might occur during
 * associating an <code>WorkContext</code> with a <code>Work</code>
 * instance.
 * 
 * <p> This class is not designed as an Enumerated type (Enum), as the error codes
 * listed below could be expanded to accommodate custom error conditions for
 * custom <code>WorkContext</code> types.
 * 
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
public class WorkContextErrorCodes 
{
   /**
    * Indicates that a <code>WorkContext</code> type, that was not
    * specified as optional, passed in by the <code>Work</code> instance is
    * not supported by the container.
    * 
    * @since 1.6
    */
   public static final String UNSUPPORTED_CONTEXT_TYPE = "1";
   
   /**
    * Indicates that there are more than one instance of a <code>WorkContext</code>
    * type passed in by the <code>Work</code>
    * instance.
    * <p>
    * 
    * @since 1.6
    */
   public static final String DUPLICATE_CONTEXTS = "2";
   
   /**
    * Indicates a failure in recreating the <code>WorkContext</code>
    * instance. For <code>TransactionWorkContext</code> instances, the
    * <code>WorkManager</code> must use this failure code when it should have
    * used {@link WorkException#TX_RECREATE_FAILED} as the error code.
    * 
    * @since 1.6
    */
   public static final String CONTEXT_SETUP_FAILED = "3";
   
   /**
    * Indicates that the container cannot support recreating the
    * <code>WorkContext</code> instance. For
    * <code>TransactionWorkContext</code> instances, the
    * <code>WorkManager</code> must use this failure code when it should have
    * used {@link WorkException#TX_CONCURRENT_WORK_DISALLOWED} as the error
    * code.
    * 
    * @since 1.6
    */
   public static final String CONTEXT_SETUP_UNSUPPORTED = "4";

   /**
    * Private constructor
    */
   private WorkContextErrorCodes()
   {
   }
}
