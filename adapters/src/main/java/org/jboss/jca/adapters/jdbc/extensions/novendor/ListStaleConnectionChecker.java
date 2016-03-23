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

package org.jboss.jca.adapters.jdbc.extensions.novendor;


import org.jboss.jca.adapters.jdbc.spi.StaleConnectionChecker;


import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author <a href="mailto:jolee@redhat.com">John Lee</a>
 * 
 * @version $Revision: 75423 $
 */
public class ListStaleConnectionChecker implements StaleConnectionChecker, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 202928214888283717L;

   private Set staleSet = null;

   /**
    * Constructor
    */
   public ListStaleConnectionChecker()
   {
      staleSet = new HashSet();
   }

   /**
    *set
    * set via <config-property name="StaleExceptions">10099,10100,10101</>
    *
    * Set the stale exceptions
    * @param s The value
    */
   public void setStaleExceptions(String s)
   {
      StringTokenizer st = new StringTokenizer(s, ",");
      while (st.hasMoreTokens()) 
      {
         staleSet.add(Integer.parseInt(st.nextToken()));
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isStaleConnection(SQLException e)
   {
      final int errorCode = Math.abs(e.getErrorCode());

      if (staleSet.contains(errorCode))
      {
         return true;
      }

      return false;   
   }
}
