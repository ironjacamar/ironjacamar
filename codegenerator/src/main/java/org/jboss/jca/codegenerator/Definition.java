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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A Definition.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definition
{
   /** jca version  */
   @XmlElement(name = "version")
   private String version = "1.6";
   /** output directory  */
   private String outputDir;
   /** resource adapter package name */
   @XmlElement(name = "package")
   private String raPackage;
   
   /** default value for proper input */
   private String defaultValue = "Acme";
   
   /** use annotation or ra.xml  */
   @XmlElement(name = "annotation")
   private boolean useAnnotation;
   /** use resource adapter  */
   @XmlElement(name = "UseResourceAdapter")
   private boolean useRa;

   /** resource adapter class name */
   @XmlElement(name = "ResourceAdapter")
   private String raClass;
   /** resource adapter configuration properties */
   @XmlElement(name = "RaConfigProp") 
   private List<ConfigPropType> raConfigProps;
   
   /** managed connection factory class name */
   @XmlElement(name = "ManagedConnectionFactory")
   private String mcfClass;
   /** resource adapter configuration properties */
   @XmlElement(name = "McfConfigProp") 
   private List<ConfigPropType> mcfConfigProps;
   
   /** support transaction  */
   @XmlElement(name = "Transaction") 
   private String supportTransaction;
   /** support re-authentication */
   @XmlElement(name = "SupportReauthen")
   private boolean supportReauthen = false;
   /** define SecurityPermission */
   @XmlElement(name = "SecurityPermission") 
   private List<SecurityPermissionType> securityPermissions;
   /** define AuthenMechanism */
   @XmlElement(name = "AuthenMechanism") 
   private List<AuthenMechanismType> authenMechanisms;
   
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
   /** resource adapater metadata class name */
   private String raMetaClass;

   /** support outbound  */
   @XmlElement(name = "SupportOutbound")
   private boolean supportOutbound;
   /** support inbound  */
   @XmlElement(name = "SupportInbound")
   private boolean supportInbound;
   /** connection message listener class name */
   @XmlElement(name = "MessageListener")
   private String mlClass;
   /** ActivationSpec class name */
   @XmlElement(name = "ActivationSpec")
   private String asClass;
   /** ActivationSpec configuration properties */
   @XmlElement(name = "AsConfigProp")
   private List<ConfigPropType> asConfigProps;
   /** Activation class name */
   @XmlElement(name = "Activation")
   private String activationClass;
   
   /** support self defined method in connection class  */
   @XmlElement(name = "DefineMethod") 
   private boolean defineMethodInConnection;
   /** define methods */
   @XmlElement(name = "Method") 
   private List<MethodForConnection> methods;
   

   /**
    * Set the version.
    * 
    * @param version The version to set.
    */
   public void setVersion(String version)
   {
      this.version = version;
   }

   /**
    * Get the version.
    * 
    * @return the version.
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * Set the outputDir.
    * 
    * @param outputDir The outputDir to set.
    */
   public void setOutputDir(String outputDir)
   {
      this.outputDir = outputDir;
   }

   /**
    * Get the outputDir.
    * 
    * @return the outputDir.
    */
   public String getOutputDir()
   {
      return outputDir;
   }

   /**
    * Set the raPackage.
    * 
    * @param raPackage The raPackage to set.
    */
   public void setRaPackage(String raPackage)
   {
      this.raPackage = raPackage;
   }

   /**
    * Get the raPackage.
    * 
    * @return the raPackage.
    */
   public String getRaPackage()
   {
      return raPackage;
   }

   /**
    * Set the raClass.
    * 
    * @param raClass The raClass to set.
    */
   public void setRaClass(String raClass)
   {
      this.raClass = raClass;
   }

   /**
    * Get the raClass.
    * 
    * @return the raClass.
    */
   public String getRaClass()
   {
      if (raClass == null || raClass.equals(""))
      {
         raClass = getDefaultValue() + "ResourceAdpater";
      }
      return raClass;
   }

   /**
    * Set the raConfigProps.
    * 
    * @param raConfigProps The raConfigProps to set.
    */
   public void setRaConfigProps(List<ConfigPropType> raConfigProps)
   {
      this.raConfigProps = raConfigProps;
   }

   /**
    * Get the raConfigProps.
    * 
    * @return the raConfigProps.
    */
   public List<ConfigPropType> getRaConfigProps()
   {
      return raConfigProps;
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
    * Set the raMetaClass.
    * 
    * @param raMetaClass The raMetaClass to set.
    */
   public void setRaMetaClass(String raMetaClass)
   {
      this.raMetaClass = raMetaClass;
   }

   /**
    * Get the raMetaClass.
    * 
    * @return the raMetaClass.
    */
   public String getRaMetaClass()
   {
      if (raMetaClass == null || raMetaClass.equals(""))
         raMetaClass = getDefaultValue() + "RaMetaData";
      return raMetaClass;
   }

   /**
    * Set the useAnnotation.
    * 
    * @param useAnnotation The useAnnotation to set.
    */
   public void setUseAnnotation(boolean useAnnotation)
   {
      this.useAnnotation = useAnnotation;
   }

   /**
    * Get the useAnnotation.
    * 
    * @return the useAnnotation.
    */
   public boolean isUseAnnotation()
   {
      return useAnnotation;
   }

   /**
    * Set the supportOutbound.
    * 
    * @param supportOutbound The supportOutbound to set.
    */
   public void setSupportOutbound(boolean supportOutbound)
   {
      this.supportOutbound = supportOutbound;
   }

   /**
    * Get the supportOutbound.
    * 
    * @return the supportOutbound.
    */
   public boolean isSupportOutbound()
   {
      return supportOutbound;
   }
   
   /**
    * Set the supportInbound.
    * 
    * @param supportInbound The supportInbound to set.
    */
   public void setSupportInbound(boolean supportInbound)
   {
      this.supportInbound = supportInbound;
   }

   /**
    * Get the supportInbound.
    * 
    * @return the supportInbound.
    */
   public boolean isSupportInbound()
   {
      return supportInbound;
   }

   /**
    * Set the messageListenerClass.
    * 
    * @param messageListenerClass The messageListenerClass to set.
    */
   public void setMlClass(String messageListenerClass)
   {
      this.mlClass = messageListenerClass;
   }

   /**
    * Get the messageListenerClass.
    * 
    * @return the messageListenerClass.
    */
   public String getMlClass()
   {
      if (mlClass == null || mlClass.equals(""))
      {
         mlClass =  getDefaultValue() + "MessageListener";
      }
      return mlClass;
   }

   /**
    * Set the activationSpecClass.
    * 
    * @param activationSpecClass The activationSpecClass to set.
    */
   public void setAsClass(String activationSpecClass)
   {
      this.asClass = activationSpecClass;
   }

   /**
    * Get the activationSpecClass.
    * 
    * @return the activationSpecClass.
    */
   public String getAsClass()
   {
      if (asClass == null || asClass.equals(""))
      {
         asClass =  getDefaultValue() + "ActivationSpec";
      }
      return asClass;
   }

   /**
    * Set the asConfigProps.
    * 
    * @param asConfigProps The asConfigProps to set.
    */
   public void setAsConfigProps(List<ConfigPropType> asConfigProps)
   {
      this.asConfigProps = asConfigProps;
   }

   /**
    * Get the asConfigProps.
    * 
    * @return the asConfigProps.
    */
   public List<ConfigPropType> getAsConfigProps()
   {
      return asConfigProps;
   }

   /**
    * Set the useRa.
    * 
    * @param useRa The useRa to set.
    */
   public void setUseRa(boolean useRa)
   {
      this.useRa = useRa;
   }

   /**
    * Get the useRa.
    * 
    * @return the useRa.
    */
   public boolean isUseRa()
   {
      return useRa;
   }

   /**
    * Set the activationClass.
    * 
    * @param activationClass The activationClass to set.
    */
   public void setActivationClass(String activationClass)
   {
      this.activationClass = activationClass;
   }

   /**
    * Get the activationClass.
    * 
    * @return the activationClass.
    */
   public String getActivationClass()
   {
      if (activationClass == null || activationClass.equals(""))
      {
         activationClass =  getDefaultValue() + "Activation";
      }
      return activationClass;
   }

   /**
    * Set the supportTransaction.
    * 
    * @param supportTransaction The supportTransaction to set.
    */
   public void setSupportTransaction(String supportTransaction)
   {
      this.supportTransaction = supportTransaction;
   }

   /**
    * Get the supportTransaction.
    * 
    * @return the supportTransaction.
    */
   public String getSupportTransaction()
   {
      return supportTransaction;
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

   /**
    * Set the defaultValue.
    * 
    * @param defaultValue The defaultValue to set.
    */
   public void setDefaultValue(String defaultValue)
   {
      this.defaultValue = defaultValue;
   }

   /**
    * Get the defaultValue.
    * 
    * @return the defaultValue.
    */
   public String getDefaultValue()
   {
      return defaultValue;
   }

   /**
    * Set the supportReauthen.
    * 
    * @param supportReauthen The supportReauthen to set.
    */
   public void setSupportReauthen(boolean supportReauthen)
   {
      this.supportReauthen = supportReauthen;
   }

   /**
    * Get the supportReauthen.
    * 
    * @return the supportReauthen.
    */
   public boolean isSupportReauthen()
   {
      return supportReauthen;
   }

   /**
    * Set the securityPermissions.
    * 
    * @param securityPermissions The securityPermissions to set.
    */
   public void setSecurityPermissions(List<SecurityPermissionType> securityPermissions)
   {
      this.securityPermissions = securityPermissions;
   }

   /**
    * Get the securityPermissions.
    * 
    * @return the securityPermissions.
    */
   public List<SecurityPermissionType> getSecurityPermissions()
   {
      return securityPermissions;
   }

   /**
    * Set the authenMechanisms.
    * 
    * @param authenMechanisms The authenMechanisms to set.
    */
   public void setAuthenMechanisms(List<AuthenMechanismType> authenMechanisms)
   {
      this.authenMechanisms = authenMechanisms;
   }

   /**
    * Get the authenMechanisms.
    * 
    * @return the authenMechanisms.
    */
   public List<AuthenMechanismType> getAuthenMechanisms()
   {
      return authenMechanisms;
   }

}
