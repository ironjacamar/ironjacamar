/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.codegenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A MethodForConnection.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class MethodForConnection
{
   /** name */
   private String methodName;
   /** type */
   private String returnType = "void";
   /** value */
   private List<MethodParam> params = new ArrayList<MethodParam>();
   /** required */
   private List<String> exceptionType = new ArrayList<String>();

   /**
    * new param
    * 
    * @param name param name
    * @param type param type
    * @return new param
    */
   public MethodParam newParam(String name, String type)
   {
      MethodParam p = new MethodParam();
      p.setName(name);
      p.setType(type);
      return p;
   }
   
   /**
    * Set the methodName.
    * 
    * @param methodName The methodName to set.
    */
   public void setMethodName(String methodName)
   {
      this.methodName = methodName;
   }

   /**
    * Get the methodName.
    * 
    * @return the methodName.
    */
   public String getMethodName()
   {
      return methodName;
   }

   /**
    * Set the returnType.
    * 
    * @param returnType The returnType to set.
    */
   public void setReturnType(String returnType)
   {
      this.returnType = returnType;
   }

   /**
    * Get the returnType.
    * 
    * @return the returnType.
    */
   public String getReturnType()
   {
      return returnType;
   }

   /**
    * Set the params.
    * 
    * @param params The params to set.
    */
   public void setParams(List<MethodParam> params)
   {
      this.params = params;
   }

   /**
    * Get the params.
    * 
    * @return the params.
    */
   public List<MethodParam> getParams()
   {
      return params;
   }

   /**
    * Set the exceptionType.
    * 
    * @param exceptionType The exceptionType to set.
    */
   public void setExceptionType(List<String> exceptionType)
   {
      this.exceptionType = exceptionType;
   }

   /**
    * Get the exceptionType.
    * 
    * @return the exceptionType.
    */
   public List<String> getExceptionType()
   {
      return exceptionType;
   }

}
