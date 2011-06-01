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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * A Definition.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class McfDef
{
   /** id */
   private int id;
   /** Definition */
   private Definition def;
   
   /** managed connection factory class name */
   @XmlElement(name = "ManagedConnectionFactory")
   private String mcfClass;
   /** resource adapter configuration properties */
   @XmlElement(name = "McfConfigProp") 
   private List<ConfigPropType> mcfConfigProps;
   
   /** managed connection class name */
   @XmlElement(name = "ManagedConnection")
   private String mcClass;
   /** connection interface name */
   @XmlElement(name = "ConnectionInterface")
   private String connInterfaceClass;
   /** connection impl class name */
   @XmlElement(name = "ConnectionImpl")
   private String connImplClass;
   /** connection factory interface name */
   @XmlElement(name = "ConnectionFactoryInterface")
   private String cfInterfaceClass;
   /** connection factory class name */
   @XmlElement(name = "ConnectionFactoryImpl")
   private String cfClass;

   /** ResourceAdapterAssociation optional  */
   @XmlElement(name = "ImplRaAssociation")
   private boolean implRaAssociation;
   /** ResourceAdapterAssociation optional  */
   @XmlElement(name = "UseCciConnection")
   private boolean useCciConnection;
   
   /** cci connection factory class name */
   private String cciConnFactoryClass;
   /** cci connection class name */
   private String cciConnClass;
   /** managed connection metadata class name */
   private String mcMetaClass;
   /** connection manage class name */
   private String cmClass;
   
   /** connection metadata class name */
   private String connMetaClass;
   /** connection spec class name */
   private String connSpecClass;
   
   /** support self defined method in connection class  */
   @XmlElement(name = "DefineMethod") 
   private boolean defineMethodInConnection;
   /** define methods */
   @XmlElement(name = "Method") 
   private List<MethodForConnection> methods;

   /**
    * Construct
    * 
    * @param id MCF id
    * @param def Definition
    */
   public McfDef(int id, Definition def)
   {
      this.id = id;
      this.def = def;
   }
   
   /**
    * getDefaultValue
    * 
    * @return default name
    */
   private String getDefaultValue()
   {
      if (id == 1)
         return def.getDefaultValue();
      else
         return def.getDefaultValue() + id;
   }
   
   /**
    * getMcfPackage
    * 
    * @return default name
    */
   public String getMcfPackage()
   {
      return "mcf" + id;
   }
   
   /**
    * Set the mcfClass.
    * 
    * @param mcfClass The mcfClass to set.
    */
   public void setMcfClass(String mcfClass)
   {
      this.mcfClass = mcfClass;
   }

   /**
    * Get the mcfClass.
    * 
    * @return the mcfClass.
    */
   public String getMcfClass()
   {
      if (mcfClass == null || mcfClass.equals(""))
      {
         mcfClass = getDefaultValue() + "ManagedConnectionFactory";
      }
      return mcfClass;
   }

   /**
    * Set the mcClass.
    * 
    * @param mcClass The mcClass to set.
    */
   public void setMcClass(String mcClass)
   {
      this.mcClass = mcClass;
   }

   /**
    * Get the mcClass.
    * 
    * @return the mcClass.
    */
   public String getMcClass()
   {
      if (mcClass == null || mcClass.equals(""))
      {
         mcClass = getDefaultValue() + "ManagedConnection";
      }
      return mcClass;
   }

   /**
    * Set the connInterfaceClass.
    * 
    * @param connInterfaceClass The connInterfaceClass to set.
    */
   public void setConnInterfaceClass(String connInterfaceClass)
   {
      this.connInterfaceClass = connInterfaceClass;
   }

   /**
    * Get the connInterfaceClass.
    * 
    * @return the connInterfaceClass.
    */
   public String getConnInterfaceClass()
   {
      if (connInterfaceClass == null || connInterfaceClass.equals(""))
      {
         connInterfaceClass = getDefaultValue() + "Connection";
      }
      return connInterfaceClass;
   }

   /**
    * Set the connImplClass.
    * 
    * @param connImplClass The connImplClass to set.
    */
   public void setConnImplClass(String connImplClass)
   {
      this.connImplClass = connImplClass;
   }

   /**
    * Get the connImplClass.
    * 
    * @return the connImplClass.
    */
   public String getConnImplClass()
   {
      if (connImplClass == null || connImplClass.equals(""))
      {
         connImplClass = getDefaultValue() + "ConnectionImpl";
      }
      return connImplClass;
   }

   /**
    * Set the mcfConfigProps.
    * 
    * @param mcfConfigProps The mcfConfigProps to set.
    */
   public void setMcfConfigProps(List<ConfigPropType> mcfConfigProps)
   {
      this.mcfConfigProps = mcfConfigProps;
   }

   /**
    * Get the mcfConfigProps.
    * 
    * @return the mcfConfigProps.
    */
   public List<ConfigPropType> getMcfConfigProps()
   {
      return mcfConfigProps;
   }
   
   /**
    * Set the implRaAssociation.
    * 
    * @param implRaAssociation The implRaAssociation to set.
    */
   public void setImplRaAssociation(boolean implRaAssociation)
   {
      this.implRaAssociation = implRaAssociation;
   }

   /**
    * Get the implRaAssociation.
    * 
    * @return the implRaAssociation.
    */
   public boolean isImplRaAssociation()
   {
      return implRaAssociation;
   }
   
   /**
    * Set the useCciConnection.
    * 
    * @param useCciConnection The useCciConnection to set.
    */
   public void setUseCciConnection(boolean useCciConnection)
   {
      this.useCciConnection = useCciConnection;
   }

   /**
    * Get the useCciConnection.
    * 
    * @return the useCciConnection.
    */
   public boolean isUseCciConnection()
   {
      return useCciConnection;
   }
   
   /**
    * Set the cciConnFactoryClass.
    * 
    * @param cciConnFactoryClass The cciConnFactoryClass to set.
    */
   public void setCciConnFactoryClass(String cciConnFactoryClass)
   {
      this.cciConnFactoryClass = cciConnFactoryClass;
   }

   /**
    * Get the cciConnFactoryClass.
    * 
    * @return the cciConnFactoryClass.
    */
   public String getCciConnFactoryClass()
   {
      if (cciConnFactoryClass == null || cciConnFactoryClass.equals(""))
         cciConnFactoryClass = getDefaultValue() + "CciConnectionFactory";
      return cciConnFactoryClass;
   }

   /**
    * Set the cciConnClass.
    * 
    * @param cciConnClass The cciConnClass to set.
    */
   public void setCciConnClass(String cciConnClass)
   {
      this.cciConnClass = cciConnClass;
   }

   /**
    * Get the cciConnClass.
    * 
    * @return the cciConnClass.
    */
   public String getCciConnClass()
   {
      if (cciConnClass == null || cciConnClass.equals(""))
         cciConnClass = getDefaultValue() + "CciConnection";
      return cciConnClass;
   }

   /**
    * Set the mcMetaClass.
    * 
    * @param mcMetaClass The mcMetaClass to set.
    */
   public void setMcMetaClass(String mcMetaClass)
   {
      this.mcMetaClass = mcMetaClass;
   }

   /**
    * Get the mcMetaClass.
    * 
    * @return the mcMetaClass.
    */
   public String getMcMetaClass()
   {
      if (mcMetaClass == null || mcMetaClass.equals(""))
         mcMetaClass = getDefaultValue() + "ManagedConnectionMetaData";
      return mcMetaClass;
   }

   /**
    * Set the cmClass.
    * 
    * @param cmClass The cmClass to set.
    */
   public void setCmClass(String cmClass)
   {
      this.cmClass = cmClass;
   }

   /**
    * Get the cmClass.
    * 
    * @return the cmClass.
    */
   public String getCmClass()
   {
      if (cmClass == null || cmClass.equals(""))
         cmClass = getDefaultValue() + "ConnectionManager";
      return cmClass;
   }

   /**
    * Set the cfClass.
    * 
    * @param cfClass The cfClass to set.
    */
   public void setCfClass(String cfClass)
   {
      this.cfClass = cfClass;
   }

   /**
    * Get the cfClass.
    * 
    * @return the cfClass.
    */
   public String getCfClass()
   {
      if (cfClass == null || cfClass.equals(""))
      {
         cfClass =  getDefaultValue() + "ConnectionFactoryImpl";
      }
      return cfClass;
   }

   /**
    * Set the cfInterfaceClass.
    * 
    * @param cfInterfaceClass The cfInterfaceClass to set.
    */
   public void setCfInterfaceClass(String cfInterfaceClass)
   {
      this.cfInterfaceClass = cfInterfaceClass;
   }

   /**
    * Get the cfInterfaceClass.
    * 
    * @return the cfInterfaceClass.
    */
   public String getCfInterfaceClass()
   {
      if (cfInterfaceClass == null || cfInterfaceClass.equals(""))
      {
         cfInterfaceClass =  getDefaultValue() + "ConnectionFactory";
      }
      return cfInterfaceClass;
   }

   /**
    * Set the connMetaClass.
    * 
    * @param connMetaClass The connMetaClass to set.
    */
   public void setConnMetaClass(String connMetaClass)
   {
      this.connMetaClass = connMetaClass;
   }

   /**
    * Get the connMetaClass.
    * 
    * @return the connMetaClass.
    */
   public String getConnMetaClass()
   {
      if (connMetaClass == null || connMetaClass.equals(""))
         connMetaClass = getDefaultValue() + "ConnectionMetaData";
      return connMetaClass;
   }

   /**
    * Set the connSpecClass.
    * 
    * @param connSpecClass The connSpecClass to set.
    */
   public void setConnSpecClass(String connSpecClass)
   {
      this.connSpecClass = connSpecClass;
   }

   /**
    * Get the connSpecClass.
    * 
    * @return the connSpecClass.
    */
   public String getConnSpecClass()
   {
      if (connSpecClass == null || connSpecClass.equals(""))
         connSpecClass = getDefaultValue() + "ConnectionSpec";
      return connSpecClass;
   }

   /**
    * Set the defineMethodInConnection.
    * 
    * @param defineMethodInConnection The defineMethodInConnection to set.
    */
   public void setDefineMethodInConnection(boolean defineMethodInConnection)
   {
      this.defineMethodInConnection = defineMethodInConnection;
   }

   /**
    * Get the defineMethodInConnection.
    * 
    * @return the defineMethodInConnection.
    */
   public boolean isDefineMethodInConnection()
   {
      return defineMethodInConnection;
   }

   /**
    * Set the methods.
    * 
    * @param methods The methods to set.
    */
   public void setMethods(List<MethodForConnection> methods)
   {
      this.methods = methods;
   }

   /**
    * Get the methods.
    * 
    * @return the methods.
    */
   public List<MethodForConnection> getMethods()
   {
      return methods;
   }
}
