/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.codegenerator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A MethodForConnection.
 *
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MethodForConnection
{
   /**
    * name
    */
   @XmlElement(name = "MethodName")
   private String methodName;
   /**
    * type
    */
   @XmlElement(name = "ReturnType")
   private String returnType = "void";
   /**
    * value
    */
   private List<MethodParam> params = new ArrayList<MethodParam>();
   /**
    * required
    */
   @XmlElement(name = "ExceptionType")
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

   /**
    * toString
    *
    * @return methodName and all params
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(methodName);
      for (MethodParam p : params)
      {
         sb.append(",");
         sb.append(p.toString());
      }
      return sb.toString();
   }
}
