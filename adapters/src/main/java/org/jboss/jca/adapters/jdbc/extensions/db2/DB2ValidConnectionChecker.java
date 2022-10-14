/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.extensions.db2;

import org.jboss.jca.adapters.jdbc.CheckValidConnectionSQL;

/**
 * A DB2ValidConnectionChecker.
 *
 * @author <a href="wprice@redhat.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class DB2ValidConnectionChecker extends CheckValidConnectionSQL
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1256537245822198702L;

   /** The VALID_QUERY */
   private static final String VALID_QUERY = "SELECT CURRENT TIMESTAMP FROM SYSIBM.SYSDUMMY1";

   /**
    * Constructor
    */
   public DB2ValidConnectionChecker()
   {
      super(VALID_QUERY);
   }
}
