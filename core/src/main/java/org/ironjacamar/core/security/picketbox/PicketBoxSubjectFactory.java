/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.security.picketbox;

import org.ironjacamar.core.CoreLogger;

import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * A SubjectFactory implementation backed by PicketBox
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PicketBoxSubjectFactory implements org.ironjacamar.core.spi.security.SubjectFactory
{
   /** Delegator */
   private org.jboss.security.SubjectFactory delegator;

   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, PicketBoxSubjectFactory.class.getName());

   private static boolean trace = log.isTraceEnabled();


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
   @Override
   public Subject createSubject(String sd)
   {
      Subject subject = delegator.createSubject(sd);

      if (trace)
      {
         log.trace("Subject=" + subject);
         log.trace("Subject identity=" + Integer.toHexString(System.identityHashCode(subject)));
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
