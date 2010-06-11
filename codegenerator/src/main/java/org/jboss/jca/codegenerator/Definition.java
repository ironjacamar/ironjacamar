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

/**
 * A Definition.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class Definition
{
   /** jca version  */
   private String version;
   /** output directory  */
   private String outputDir;
   /** resource adapter package name */
   private String raPackage;
   
   
   /** use annotation or ra.xml  */
   private boolean useAnnotation;
   /** use resource adapter  */
   private boolean useRa;
   /** support transaction  */
   private String supportTransaction;

   /** resource adapter class name */
   private String raClass;
   /** resource adapter configuration properties */
   private List<ConfigPropType> raConfigProps;
   
   /** managed connection factory class name */
   private String mcfClass;
   /** resource adapter configuration properties */
   private List<ConfigPropType> mcfConfigProps;
   
   /** managed connection class name */
   private String mcClass;
   /** connection interface name */
   private String connInterfaceClass;
   /** connection impl class name */
   private String connImplClass;
   /** connection factory interface name */
   private String cfInterfaceClass;
   /** connection factory class name */
   private String cfClass;
   
   /** ResourceAdapterAssociation optional  */
   private boolean implRaAssociation;
   /** ResourceAdapterAssociation optional  */
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
   private boolean supportOutbound;
   /** support inbound  */
   private boolean supportInbound;
   /** connection metadata class name */
   private String mlClass;
   /** ActivationSpec class name */
   private String asClass;
   /** ActivationSpec configuration properties */
   private List<ConfigPropType> asConfigProps;
   /** Activation class name */
   private String activationClass;
   
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
         cciConnFactoryClass = "MyCciConnectionFactory";
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
         cciConnClass = "MyCciConnection";
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
         mcMetaClass = "MyManagedConnectionMetaData";
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
         cmClass = "MyConnectionManager";
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
         connMetaClass = "MyConnectionMetaData";
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
         connSpecClass = "MyConnectionSpec";
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
         raMetaClass = "MyRaMetaData";
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

}
