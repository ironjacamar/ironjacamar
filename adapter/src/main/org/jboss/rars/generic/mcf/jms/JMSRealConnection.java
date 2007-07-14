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
package org.jboss.rars.generic.mcf.jms;

import javax.jms.Connection;
import javax.jms.Session;

import org.jboss.rars.generic.mcf.GenericManagedConnection;
import org.jboss.rars.generic.mcf.indirect.AbstractRealConnection;
import org.jboss.rars.generic.wrapper.WrappedObject;

/**
 * JMSRealConnection.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
class JMSRealConnection extends AbstractRealConnection implements WrappedObject
{
   /** Error message for strict behaviour */
   static final String ISE = "This method is not applicable inside the application server. See the J2EE spec, e.g. J2EE1.4 Section 6.6";

   /** Error message for multiple sessions */
   static final String DUPLICATE = "Only one session is allowed per connection inside the application server. See the J2EE spec, e.g. J2EE1.4 Section 6.6";

   /** The connection */
   private Connection connection;
   
   /** The session */
   private Session session;
   
   /**
    * Create a new JMSRealConnection.
    * 
    * @param mc the managed connection
    * @param connection the connection
    */
   public JMSRealConnection(GenericManagedConnection mc, Connection connection)
   {
      super(mc);
      this.connection = connection;
   }

   /**
    * Set the session.
    * 
    * @param session the session.
    */
   protected void setSession(Session session)
   {
      this.session = session;
   }
   
   public Object getWrappedObject()
   {
      return session;
   }

   public void closeFromIndirectConnection() throws Exception
   {
      connection.close();
   }

   public String toString()
   {
      if (session == null)
         return super.toString();
      return session.toString();
   }
}
