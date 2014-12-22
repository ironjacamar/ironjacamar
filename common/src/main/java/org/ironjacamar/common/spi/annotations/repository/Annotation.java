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
package org.ironjacamar.common.spi.annotations.repository;

import java.util.List;

/**
 *
 * A Annotation.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public interface Annotation
{
   /**
    * Get the class Name of this annotation
    * @return the classname
    */
   public String getClassName();

   /**
    * Get the Annotation Object
    * @return the java.lang.Annotation instance
    */
   public Object getAnnotation();

   /**
    * Get the list of parameters type for this annotation
    * @return list of parameters types
    */
   public List<String> getParameterTypes();

   /**
    * Get the member name
    * @return member name
    */
   public String getMemberName();

   /**
    * Check if the annotation is on a method
    * @return true if the annotation is attached to a method
    */
   public boolean isOnMethod();

   /**
    * Check if the annotation is on a field
    * @return true if the annotation is attached to a field
    */
   public boolean isOnField();
}

