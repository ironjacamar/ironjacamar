/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security.picketbox;

import org.jboss.jca.core.spi.security.SecurityContext;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

/**
 * SecurityContext implementation using PicketBox
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PicketBoxSecurityContext implements SecurityContext
{
   private org.jboss.security.SecurityContext delegator;

   /**
    * Constructor
    * @param delegator The delegator
    */
   public PicketBoxSecurityContext(org.jboss.security.SecurityContext delegator)
   {
      this.delegator = delegator;
   }

   /**
    * {@inheritDoc}
    */
   public Subject getAuthenticatedSubject()
   {
      return delegator.getSubjectInfo().getAuthenticatedSubject();
   }

   /**
    * {@inheritDoc}
    */
   public void setAuthenticatedSubject(Subject subject)
   {
      delegator.getSubjectInfo().setAuthenticatedSubject(subject);
   }

   /**
    * {@inheritDoc}
    */
   public String[] getRoles()
   {
      String[] roles = null;

      org.jboss.security.identity.RoleGroup pbRoles = delegator.getUtil().getRoles();
      if (pbRoles != null)
      {
         List<String> l = new ArrayList<String>(pbRoles.getRoles().size());
         for (org.jboss.security.identity.Role role : pbRoles.getRoles())
         {
            l.add(role.getRoleName());
         }
         roles = l.toArray(new String[l.size()]);
      }

      return roles;
   }

   /**
    * Get the delegator
    * @return The value
    */
   org.jboss.security.SecurityContext getDelegator()
   {
      return delegator;
   }
}
