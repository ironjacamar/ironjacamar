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

import org.jboss.jca.adapters.AdaptersBundle;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Wrapper;

import org.jboss.logging.Messages;

/**
 * JBossWrapper.
 *
 * @author <a href="abrock@redhat.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JBossWrapper implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4097918663681033085L;

   /** The bundle */
   protected static AdaptersBundle bundle = Messages.getBundle(AdaptersBundle.class);

   /**
    * Constructor
    */
   public JBossWrapper()
   {
   }

   /**
    * Is a wrapper for
    * @param iface The interface
    * @return True if wrapper; false otherwise
    * @exception SQLException Thrown if an error occurs
    */
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      if (iface == null)
         throw new IllegalArgumentException("Null interface");

      if (iface.isAssignableFrom(getClass()))
         return true;

      Object wrapped = unwrapInnerMost(getWrappedObject(), iface);

      if (wrapped == null)
         return false;

      return iface.isAssignableFrom(wrapped.getClass());
   }

   /**
    * Unwrap
    * @param <T> the type
    * @param iface The interface
    * @return The object
    * @exception SQLException Thrown if an error occurs
    */
   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      if (iface == null)
         throw new IllegalArgumentException("Null interface");

      if (iface.isAssignableFrom(getClass()))
         return iface.cast(this);

      Object wrapped = unwrapInnerMost(getWrappedObject(), iface);

      if (wrapped != null && iface.isAssignableFrom(wrapped.getClass()))
         return iface.cast(wrapped);

      throw new SQLException(bundle.notWrapperFor(iface.getName()));
   }

   /**
    * Get the wrapped object - override in sub-classes
    * @return The object
    * @exception SQLException Thrown if an error occurs
    */
   protected Object getWrappedObject() throws SQLException
   {
      return null;
   }

   /**
    * Return the inner most wrapped object
    * @param o The object
    * @param clz The target class
    * @return The result
    */
   private Object unwrapInnerMost(Object o, Class<?> clz)
   {
      if (o == null)
         return null;

      if (!(o instanceof Wrapper))
         return o;

      Wrapper w = (Wrapper)o;
      try
      {
         if (!w.isWrapperFor(clz))
            return o;
      }
      catch (SQLException se)
      {
         return o;
      }

      Object result = o;
      try
      {
         result = ((Wrapper)o).unwrap(clz);
      }
      catch (SQLException se)
      {
         // Nothing we can do
      }

      return result;
   }
}
