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

import org.jboss.jca.core.CoreLogger;

import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * A SubjectFactory implementation backed by PicketBox
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PicketBoxSubjectFactory implements org.jboss.jca.core.spi.security.SubjectFactory
{
   /** Delegator */
   private org.jboss.security.SubjectFactory delegator;

   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, PicketBoxSubjectFactory.class.getName());

   /**
    * Constructor
    * @param delegator The delegator
    */
   public PicketBoxSubjectFactory(org.jboss.security.SubjectFactory delegator)
   {
      this.delegator = delegator;
   }

   /**
    * {@inheritDoc}
    */
   public Subject createSubject()
   {
      return delegator.createSubject();
   }

   /**
    * {@inheritDoc}
    */
   public Subject createSubject(String sd)
   {
      Subject subject = delegator.createSubject(sd);

      if (log.isTraceEnabled())
      {
         log.tracef("Subject=%s", subject);
         log.tracef("Subject identity=%s", Integer.toHexString(System.identityHashCode(subject)));
      }
      return subject;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("PicketBoxSubjectFactory@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[delegator=").append(delegator);
      sb.append("]");

      return sb.toString();
   }
}
