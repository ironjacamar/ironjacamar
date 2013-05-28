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

package org.jboss.jca.adapters.jdbc;

import java.io.Serializable;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * WrappedConnectionRequestInfo
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 */
public class WrappedConnectionRequestInfo implements ConnectionRequestInfo, Serializable
{
   private static final long serialVersionUID = 1L;

   private final String user;

   private final String password;

   /**
    * Constructor
    * @param user The user name
    * @param password The password
    */
   public WrappedConnectionRequestInfo(final String user, final String password)
   {
      this.user = user;
      this.password = password;
   }

   /**
    * Get the user name
    * @return The value
    */
   String getUserName()
   {
      return user;
   }

   /**
    * Get the password
    * @return The value
    */
   String getPassword()
   {
      return password;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return ((user == null) ? 37 : user.hashCode()) + 37 * ((password == null) ? 37 : password.hashCode());
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      if (other == this)
         return true;

      if (!(other instanceof WrappedConnectionRequestInfo))
         return false;

      WrappedConnectionRequestInfo cri = (WrappedConnectionRequestInfo) other;
      if (user == null)
      {
         if (cri.getUserName() != null)
         {
            return false;
         }
      }
      else
      {
         if (!user.equals(cri.getUserName()))
         {
            return false;
         }
      }
      if (password == null)
      {
         if (cri.getPassword() != null)
         {
            return false;
         }
      }
      else
      {
         if (!password.equals(cri.getPassword()))
         {
            return false;
         }
      }

      return true;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("WrappedConnectionRequestInfo@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[userName=").append(user);
      sb.append("]");

      return sb.toString();
   }
}
