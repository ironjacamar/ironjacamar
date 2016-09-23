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
package org.ironjacamar.common.annotations.repository.jandex;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.spi.annotations.repository.Annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * An AnnotationImpl.
 *
 * @author <a href="mailto:stefano.maestri@redhat.com">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AnnotationImpl implements Annotation
{
   private static Logger log = Logger.getLogger(AnnotationImpl.class);
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);
   private String className;
   private ClassLoader cl;
   private List<String> parameterTypes;
   private String memberName;
   private boolean onMethod;
   private boolean onField;
   private Class<? extends java.lang.annotation.Annotation> annotationClass;
   
   /**
    * Create a new AnnotationImpl.
    *
    * @param className className
    * @param cl classloader
    * @param parameterTypes parameterTypes
    * @param memberName memberName
    * @param onMethod onMethod
    * @param onField onField
    * @param annotationClass annotationClass
    */
   @SuppressWarnings("unchecked")
   public AnnotationImpl(String className, ClassLoader cl, List<String> parameterTypes, 
                         String memberName, boolean onMethod, boolean onField, Class<?> annotationClass)
   {
      this.className = className;
      this.cl = cl;
      if (parameterTypes != null)
      {
         this.parameterTypes = new ArrayList<String>(parameterTypes.size());
         this.parameterTypes.addAll(parameterTypes);
      }
      else
      {
         this.parameterTypes = new ArrayList<String>(0);
      }

      this.memberName = memberName;
      this.onMethod = onMethod;
      this.onField = onField;
      if (annotationClass.isAnnotation())
      {
         this.annotationClass = (Class<? extends java.lang.annotation.Annotation>)annotationClass;
      }
      else
      {
         throw new IllegalArgumentException(bundle.annotationClassShouldBeAnnotation());
      }
   }
   
   /**
    * Get the className.
    *
    * @return the className.
    */
   @Override
   public String getClassName()
   {
      return className;
   }

   /**
    * Get the annotation.
    *
    * @return the annotation.
    */
   @Override
   public Object getAnnotation()
   {
      try
      {
         if (isOnField())
         {
            Class<?> clazz = cl.loadClass(className);
            while (!clazz.equals(Object.class))
            {
               try
               {
                  Field field = SecurityActions.getDeclaredField(clazz, memberName);
                  return field.getAnnotation(annotationClass);
               }
               catch (Throwable t)
               {
                  clazz = clazz.getSuperclass();
               }
            }
         }
         else if (isOnMethod())
         {
            Class<?> clazz = cl.loadClass(className);
            Class<?>[] params = new Class<?>[parameterTypes.size()];
            int i = 0;
            for (String paramClazz : parameterTypes)
            {
               params[i] = cl.loadClass(paramClazz);
               i++;
            }
            while (!clazz.equals(Object.class))
            {
               try
               {
                  Method method = SecurityActions.getDeclaredMethod(clazz, memberName, params);
                  return method.getAnnotation(annotationClass);
               }
               catch (Throwable t)
               {
                  clazz = clazz.getSuperclass();
               }
            }
         }
         else
         { // onclass
            Class<?> clazz = cl.loadClass(className);
            return clazz.getAnnotation(annotationClass);
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }

      return null;
   }

   /**
    * Get the parameterTypes.
    *
    * @return the parameterTypes.
    */
   @Override
   public List<String> getParameterTypes()
   {
      return Collections.unmodifiableList(parameterTypes);
   }

   /**
    * Get the memberName.
    *
    * @return the memberName.
    */
   @Override
   public String getMemberName()
   {
      return memberName;
   }

   /**
    * Get the onMethod.
    *
    * @return the onMethod.
    */
   @Override
   public boolean isOnMethod()
   {
      return onMethod;
   }

   /**
    * Get the onField.
    *
    * @return the onField.
    */
   @Override
   public boolean isOnField()
   {
      return onField;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("AnnotationImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[className=").append(className);
      sb.append(" cl=").append(cl);
      sb.append(" parameterTypes=").append(parameterTypes);
      sb.append(" memberName=").append(memberName);
      sb.append(" onMethod=").append(onMethod);
      sb.append(" onField=").append(onField);
      sb.append(" annotationClass=").append(annotationClass);
      sb.append("]");

      return sb.toString();
   }
}
