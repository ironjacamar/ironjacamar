/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.sjc.annotationscanner;

import org.jboss.logging.Logger;

/**
 * An annotation for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Annotation
{
   private static Logger log = Logger.getLogger(Annotation.class);

   private Object a;
   private Class<?> l;

   /**
    * Constructor
    * @param a The annotation
    * @param l The class where the annotation is located
    */
   public Annotation(Object a, Class<?> l)
   {
      this.a = a;
      this.l = l;
   }

   /**
    * Get the annotation
    * @return The annotation
    */
   public Object getAnnotation()
   {
      return a;
   }

   /**
    * Get the location class
    * @return The class
    */
   public Class<?> getLocationClass()
   {
      return l;
   }

   /**
    * String representation
    * @return The representation
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb = sb.append(getClass().getName());
      sb = sb.append("[");
      sb = sb.append("Annotation=" + a + ",");
      sb = sb.append("LocationClass=" + l.getName());
      sb = sb.append("]");
      return sb.toString();
   }
}
