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
   
   /** mcf list */
   private List<McfDef> mcfDefs;
   
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
   
   /** resource adapter metadata class name */
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
   /** default package in inbound  */
   private boolean defaultPackageInbound;

   /** build  */
   @XmlElement(name = "build")
   private String build = "A";

   /** MBean test interface  */
   @XmlElement(name = "MBeanInterface")
   private String mbeanInterfaceClass;
   /** MBean test impl  */
   @XmlElement(name = "MBeanImpl")
   private String mbeanImplClass;
   /** generate MBean or not  */
   @XmlElement(name = "GenMBean")
   private boolean genMbean = true;

   /** generate admin object or not  */
   @XmlElement(name = "GenAdminObject")
   private boolean genAdminObject = false;
   /** Admin object implement ResourceAdapterAssociation  */
   @XmlElement(name = "adminObjectImplRaAssociation")
   private boolean adminObjectImplRaAssociation;
   /** list of admin object  */
   @XmlElement(name = "AdminObject")
   private List<AdminObjectType> adminObjects;
   
   /** Resource Adapter need Serialize or not  */
   @XmlElement(name = "RaSerial")
   private boolean raSerial = true;
   
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
         raClass = getDefaultValue() + "ResourceAdapter";
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
    * Set the mcfDefs.
    * 
    * @param mcfDefs The mcfDefs to set.
    */
   public void setMcfDefs(List<McfDef> mcfDefs)
   {
      this.mcfDefs = mcfDefs;
   }

   /**
    * Get the mcfDefs.
    * 
    * @return the mcfDefs.
    */
   public List<McfDef> getMcfDefs()
   {
      return mcfDefs;
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

   /**
    * Set the build.
    * 
    * @param build The build to set.
    */
   public void setBuild(String build)
   {
      this.build = build;
   }

   /**
    * Get the build.
    * 
    * @return the build.
    */
   public String getBuild()
   {
      return build;
   }
   
   /**
    * Get the mbeanInterfaceClass.
    * 
    * @return the mbeanInterfaceClass.
    */
   public String getMbeanInterfaceClass()
   {
      if (mbeanInterfaceClass == null || mbeanInterfaceClass.equals(""))
      {
         mbeanInterfaceClass =  getDefaultValue() + "MBean";
      }
      return mbeanInterfaceClass;
   } 
   
   /**
    * Get the mbeanImplClass.
    * 
    * @return the mbeanImplClass.
    */
   public String getMbeanImplClass()
   {
      if (mbeanImplClass == null || mbeanImplClass.equals(""))
      {
         mbeanImplClass =  getDefaultValue() + "MBeanImpl";
      }
      return mbeanImplClass;
   }
   
   /**
    * Get the genMbean.
    * 
    * @return the genMbean.
    */
   public boolean isGenMbean()
   {
      return genMbean;
   }

   /**
    * Set the genMbean.
    * 
    * @param genMbean The genMbean to set.
    */
   public void setGenMbean(boolean genMbean)
   {
      this.genMbean = genMbean;
   }

   /**
    * Set the adminObjects.
    * 
    * @param adminObjects The adminObjects to set.
    */
   public void setAdminObjects(List<AdminObjectType> adminObjects)
   {
      this.adminObjects = adminObjects;
   }

   /**
    * Get the adminObjects.
    * 
    * @return the adminObjects.
    */
   public List<AdminObjectType> getAdminObjects()
   {
      return adminObjects;
   }

   /**
    * Set the genAdminObject.
    * 
    * @param genAdminObject The genAdminObject to set.
    */
   public void setGenAdminObject(boolean genAdminObject)
   {
      this.genAdminObject = genAdminObject;
   }

   /**
    * Get the genAdminObject.
    * 
    * @return the genAdminObject.
    */
   public boolean isGenAdminObject()
   {
      return genAdminObject;
   }

   /**
    * Set the adminObjectImplRaAssociation.
    * 
    * @param adminObjectImplRaAssociation The adminObjectImplRaAssociation to set.
    */
   public void setAdminObjectImplRaAssociation(boolean adminObjectImplRaAssociation)
   {
      this.adminObjectImplRaAssociation = adminObjectImplRaAssociation;
   }

   /**
    * Get the adminObjectImplRaAssociation.
    * 
    * @return the adminObjectImplRaAssociation.
    */
   public boolean isAdminObjectImplRaAssociation()
   {
      return adminObjectImplRaAssociation;
   }

   /**
    * Set the defaultPackageInbound.
    * 
    * @param defaultPackageInbound The defaultPackageInbound to set.
    */
   public void setDefaultPackageInbound(boolean defaultPackageInbound)
   {
      this.defaultPackageInbound = defaultPackageInbound;
   }

   /**
    * Get the defaultPackageInbound.
    * 
    * @return the defaultPackageInbound.
    */
   public boolean isDefaultPackageInbound()
   {
      return defaultPackageInbound;
   }

   /**
    * set raSerial
    * 
    * @param raSerial the raSerial to set
    */
   public void setRaSerial(boolean raSerial)
   {
      this.raSerial = raSerial;
   }

   /**
    * get raSerial
    * 
    * @return the raSerial
    */
   public boolean isRaSerial()
   {
      return raSerial;
   }

}
