/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.rars.generic.mcf.jdbc;

import java.sql.SQLException;
import java.util.Set;

import javax.sql.DataSource;

import org.jboss.rars.generic.cf.GenericConnectionFactory;
import org.jboss.rars.generic.mcf.direct.DirectGenericManagedConnectionFactory;

/**
 * JDBCManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class JDBCManagedConnectionFactory extends DirectGenericManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -710101414887209297L;

   public Throwable error(Object context, Throwable t)
   {
      if (t instanceof SQLException)
         return t;
      SQLException e = new SQLException("Error during " + context);
      e.initCause(t);
      return e;
   }

   protected void addConnectionFactoryInterfaces(GenericConnectionFactory impl, Set<Class<?>> interfaces)
   {
      interfaces.add(DataSource.class);
   }
}
