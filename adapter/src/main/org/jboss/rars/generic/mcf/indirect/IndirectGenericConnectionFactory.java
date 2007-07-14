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
package org.jboss.rars.generic.mcf.indirect;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;

import org.jboss.rars.generic.cf.GenericConnectionFactory;

/**
 * IndirectGenericConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class IndirectGenericConnectionFactory extends GenericConnectionFactory implements IndirectConnectionFactory
{
   /**
    * Create a new IndirectGenericConnectionFactory.
    * 
    * @param mcf the managed connection factory
    * @param cm the connection manager
    */
   public IndirectGenericConnectionFactory(IndirectGenericManagedConnectionFactory mcf, ConnectionManager cm)
   {
      super(mcf, cm);
   }

   public Object createConnection(ConnectionRequestInfo cri) throws Throwable
   {
      ConnectionManager cm = getConnectionManager();
      IndirectGenericManagedConnectionFactory mcf = (IndirectGenericManagedConnectionFactory) getManagedConnectionFactory();
      return mcf.createIndirectConnection(cm, cri);
   }
}
