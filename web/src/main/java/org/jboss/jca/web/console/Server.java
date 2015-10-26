/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.web.console;

import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.logging.Logger;
import org.jboss.util.Classes;
import org.jboss.util.propertyeditor.PropertyEditors;

/**
 * Utility methods related to the MBeanServer interface
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 * @author <a href="mailto:dimitris@ironjacamar.org">Dimitris Andreadis</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Server
{
   private static Logger log = Logger.getLogger(Server.class);

   private static MBeanServer mbeanServer = null;

   /**
    * Get the platform MBeanServer instance
    * @return The instance
    */
   public static MBeanServer getMBeanServer()
   {
      return ManagementFactory.getPlatformMBeanServer();
   }

   /**
    * Get the MBeanServer instance
    * @param domain The domain
    * @return The instance
    */
   public static MBeanServer getMBeanServer(String domain)
   {
      try
      {
         ArrayList<MBeanServer> l = MBeanServerFactory.findMBeanServer(null);

         if (l != null)
         {
            for (MBeanServer ms : l)
            {
               String[] domains = ms.getDomains();

               if (domains != null)
               {
                  for (String d : domains)
                  {
                     if (domain.equals(d))
                     {
                        return ms;
                     }
                  }
               }
            }
         }
      }
      catch (SecurityException se)
      {
         // Ignore
      }

      return null;
   }

   /**
    * Get the domain data
    * @return The data iterator
    * @exception JMException Thrown if an error occurs
    */
   public static Iterator getDomainData() throws JMException
   {
      MBeanServer server = getMBeanServer();
      TreeMap<String, DomainData> domainData = new TreeMap<String, DomainData>();

      if (server != null)
      {
         Set objectNames = server.queryNames(null, null);
         Iterator objectNamesIter = objectNames.iterator();

         while (objectNamesIter.hasNext())
         {
            ObjectName name = (ObjectName)objectNamesIter.next();
            MBeanInfo info = server.getMBeanInfo(name);
            String domainName = name.getDomain();
            MBeanData mbeanData = new MBeanData(name, info);
            DomainData data = domainData.get(domainName);

            if (data == null)
            {
               data = new DomainData(domainName);
               domainData.put(domainName, data);
            }

            data.addData(mbeanData);
         }
      }

      return domainData.values().iterator();
   }

   /**
    * Get MBean data
    * @param name The bean name
    * @return The data
    * @exception JMException Thrown if an error occurs
    */
   public static MBeanData getMBeanData(String name) throws JMException
   {
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);
      MBeanInfo info = server.getMBeanInfo(objName);

      return new MBeanData(objName, info);
   }

   /**
    * Get MBean attribute object
    * @param name The bean name
    * @param attrName The attribute name
    * @return The data
    * @exception JMException Thrown if an error occurs
    */
   public static Object getMBeanAttributeObject(String name, String attrName) throws JMException
   {
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);

      return server.getAttribute(objName, attrName);
   }

   /**
    * Get MBean attribute
    * @param name The bean name
    * @param attrName The attribute name
    * @return The data
    * @exception JMException Thrown if an error occurs
    */
   public static String getMBeanAttribute(String name, String attrName) throws JMException
   {
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);

      String value = null;
      try
      {
         Object attr = server.getAttribute(objName, attrName);
         if (attr != null)
            value = attr.toString();
      }
      catch (JMException e)
      {
         value = e.getMessage();
      }
      return value;
   }

   /**
    * Get MBean attribute result info
    * @param name The bean name
    * @param attrInfo The attribute information
    * @return The data
    * @exception JMException Thrown if an error occurs
    */
   public static AttrResultInfo getMBeanAttributeResultInfo(String name, MBeanAttributeInfo attrInfo)
      throws JMException
   {
      ClassLoader loader = SecurityActions.getThreadContextClassLoader();
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);
      String attrName = attrInfo.getName();
      String attrType = attrInfo.getType();
      Object value = null;
      Throwable throwable = null;

      if (attrInfo.isReadable())
      {
         try
         {
            value = server.getAttribute(objName, attrName);
         }
         catch (Throwable t)
         {
            throwable = t;
         }
      }

      Class typeClass = null;
      try
      {
         typeClass = Classes.getPrimitiveTypeForName(attrType);
         if (typeClass == null)
            typeClass = loader.loadClass(attrType);
      }
      catch (ClassNotFoundException cnfe)
      {
         // Ignore
      }

      PropertyEditor editor = null;
      if (typeClass != null)
         editor = PropertyEditorManager.findEditor(typeClass);

      return new AttrResultInfo(attrName, editor, value, throwable);
   }

   /**
    * Set MBean attributes
    * @param name The bean name
    * @param attributes The attributes
    * @return The updated attribute list
    * @exception JMException Thrown if an error occurs
    */
   public static AttributeList setAttributes(String name, HashMap attributes) throws JMException
   {
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);
      MBeanInfo info = server.getMBeanInfo(objName);
      MBeanAttributeInfo[] attributesInfo = info.getAttributes();
      AttributeList newAttributes = new AttributeList();

      for (int a = 0; a < attributesInfo.length; a++)
      {
         MBeanAttributeInfo attrInfo = attributesInfo[a];
         String attrName = attrInfo.getName();

         if (!attributes.containsKey(attrName))
            continue;

         String value = (String) attributes.get(attrName);
         if (value.equals("null") && server.getAttribute(objName, attrName) == null)
         {
            log.tracef("ignoring 'null' for %s", attrName);
            continue;
         }

         String attrType = attrInfo.getType();
         Attribute attr = null;
         try
         {
            Object realValue = PropertyEditors.convertValue(value, attrType);
            attr = new Attribute(attrName, realValue);
         }
         catch (ClassNotFoundException e)
         {
            if (log.isTraceEnabled())
               log.trace("Failed to load class for attribute: " + attrType, e);
            throw new ReflectionException(e, "Failed to load class for attribute: " + attrType);
         }
         catch (IntrospectionException e)
         {
            log.tracef("Skipped setting attribute: %s, cannot find PropertyEditor for type: %s", attrName, attrType);
            continue;
         }

         server.setAttribute(objName, attr);
         newAttributes.add(attr);
      }

      return newAttributes;
   }

   /**
    * Invoke an operation
    * @param name The bean name
    * @param index The method index
    * @param args The arguments
    * @return The result
    * @exception JMException Thrown if an error occurs
    */
   public static OpResultInfo invokeOp(String name, int index, String[] args) throws JMException
   {
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);
      MBeanInfo info = server.getMBeanInfo(objName);
      MBeanOperationInfo[] opInfo = info.getOperations();
      MBeanOperationInfo op = opInfo[index];
      MBeanParameterInfo[] paramInfo = op.getSignature();
      String[] argTypes = new String[paramInfo.length];

      for (int p = 0; p < paramInfo.length; p++)
         argTypes[p] = paramInfo[p].getType();
 
      return invokeOpByName(name, op.getName(), argTypes, args);
   }

   /**
    * Invoke an operation by name
    * @param name The bean name
    * @param opName The operation name
    * @param argTypes The argument types
    * @param args The arguments
    * @return The result
    * @exception JMException Thrown if an error occurs
    */
   public static OpResultInfo invokeOpByName(String name, String opName, String[] argTypes, String[] args)
      throws JMException
   {
      MBeanServer server = getMBeanServer();
      ObjectName objName = new ObjectName(name);
      int length = argTypes != null ? argTypes.length : 0;
      Object[] typedArgs = new Object[length];

      for (int p = 0; p < typedArgs.length; p++)
      {
         String arg = args[p];
         try
         {
            Object argValue = PropertyEditors.convertValue(arg, argTypes[p]);
            typedArgs[p] = argValue;
         }
         catch (ClassNotFoundException e)
         {
            if (log.isTraceEnabled())
               log.trace("Failed to load class for arg" + p, e);
            throw new ReflectionException(e, "Failed to load class for arg" + p);
         }
         catch (java.beans.IntrospectionException e)
         {
            // If the type is not java.lang.Object throw an exception
            if (!argTypes[p].equals("java.lang.Object"))
               throw new javax.management.IntrospectionException("Failed to find PropertyEditor for type: " +
                                                                 argTypes[p]);
            // Just use the String arg
            typedArgs[p] = arg;
            continue;
         }
      }

      Object opReturn = server.invoke(objName, opName, typedArgs, argTypes);
      return new OpResultInfo(opName, argTypes, args, opReturn);
   }
}
