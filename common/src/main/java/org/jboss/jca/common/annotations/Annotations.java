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

package org.jboss.jca.common.annotations;

import org.jboss.jca.common.validator.ValidateException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.resource.spi.Activation;
import javax.resource.spi.AdministeredObject;
import javax.resource.spi.AuthenticationMechanism;
import javax.resource.spi.AuthenticationMechanism.CredentialInterface;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionDefinitions;
import javax.resource.spi.Connector;
import javax.resource.spi.SecurityPermission;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.resource.spi.work.WorkContext;

import org.jboss.logging.Logger;

import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.DescriptionImpl;
import org.jboss.metadata.javaee.spec.DescriptionsImpl;
import org.jboss.metadata.javaee.spec.DisplayNameImpl;
import org.jboss.metadata.javaee.spec.DisplayNamesImpl;
import org.jboss.metadata.javaee.spec.IconImpl;
import org.jboss.metadata.javaee.spec.IconsImpl;

import org.jboss.metadata.rar.spec.ActivationspecMetaData;
import org.jboss.metadata.rar.spec.AdminObjectMetaData;
import org.jboss.metadata.rar.spec.AuthenticationMechanismMetaData;
import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConnectionDefinitionMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.InboundRaMetaData;
import org.jboss.metadata.rar.spec.JCA16Base;
import org.jboss.metadata.rar.spec.JCA16MetaData;
import org.jboss.metadata.rar.spec.LicenseMetaData;
import org.jboss.metadata.rar.spec.MessageAdapterMetaData;
import org.jboss.metadata.rar.spec.MessageListenerMetaData;
import org.jboss.metadata.rar.spec.OutboundRaMetaData;
import org.jboss.metadata.rar.spec.ResourceAdapterMetaData;
import org.jboss.metadata.rar.spec.SecurityPermissionMetaData;
import org.jboss.metadata.rar.spec.TransactionSupportMetaData;

import org.jboss.papaki.Annotation;
import org.jboss.papaki.AnnotationRepository;
import org.jboss.papaki.AnnotationScanner;
import org.jboss.papaki.AnnotationScannerFactory;
import org.jboss.papaki.AnnotationType;

/**
 * The annotation processor for JCA 1.6
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Annotations
{
   private static Logger log = Logger.getLogger(Annotations.class);
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   public Annotations()
   {
   }

   /**
    * Scan for annotations in the URLs specified
    * @param cmd The resource adapter metadata
    * @param urls The URLs to be scanned
    * @param cl The referenced classloader
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   public ConnectorMetaData scan(ConnectorMetaData cmd, URL[] urls, ClassLoader cl) throws Exception
   {
      // Process annotations
      if (cmd == null || cmd.is16())
      {
         AnnotationScanner annotationScanner = 
            AnnotationScannerFactory.getStrategy(AnnotationScannerFactory.JAVASSIST_INPUT_STREAM);
         annotationScanner.configure().constructorLevel(false).parameterLevel(false);
         AnnotationRepository annotationRepository = annotationScanner.scan(urls, cl);

         boolean isMetadataComplete = false;
         if (cmd != null && cmd instanceof JCA16Base)
         {
            JCA16Base jmd = (JCA16Base)cmd;
            isMetadataComplete = jmd.isMetadataComplete();
         }
         
         if (cmd == null || !isMetadataComplete)
         {
            cmd = process(cmd, annotationRepository);
         }
      }
      return cmd;
   }
   /**
    * Process annotations
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   public ConnectorMetaData process(ConnectorMetaData md, AnnotationRepository annotationRepository)
      throws Exception
   {
      if (annotationRepository == null)
         throw new ValidateException("AnnotationRepository reference is null");
      /* Process
         -------
         javax.resource.spi.Activation 
         javax.resource.spi.AdministeredObject 
         javax.resource.spi.AuthenticationMechanism 
         javax.resource.spi.ConfigProperty 
         javax.resource.spi.ConnectionDefinition 
         javax.resource.spi.ConnectionDefinitions 
         javax.resource.spi.Connector 
         javax.resource.spi.SecurityPermission
      */
      
      if (md == null)
      {
         JCA16MetaData jmd = new JCA16MetaData();
         jmd.setMetadataComplete(false);
         md = jmd;
      }

      // @Connector
      md = processConnector(md, annotationRepository);

      // @ConnectionDefinitions
      md = processConnectionDefinitions(md, annotationRepository);

      // @ConnectionDefinition (outside of @ConnectionDefinitions)
      md = processConnectionDefinition(md, annotationRepository);

      // @Activation
      md = processActivation(md, annotationRepository);

      // @AuthenticationMechanism
      //md = processAuthenticationMechanism(md, annotationRepository);

      // @AdministeredObject
      md = processAdministeredObject(md, annotationRepository);

      // @ConfigProperty handle at last
      md = processConfigProperty(md, annotationRepository);

      //log.debug("ConnectorMetadata " + md);

      return md;
   }

   /**
    * Process: @Connector
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData processConnector(ConnectorMetaData md, AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(Connector.class);
      if (values != null)
      {
         if (values.size() == 1)
         {
            Annotation annotation = values.iterator().next();
            String raClass = annotation.getClassName();
            Connector c = (Connector)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + c + " for " + raClass);

            md = attachConnector(md, raClass, c);
         }
         else if (values.size() == 0)
         {
            // JBJCA-240
            if (md.getRa().getRaClass() == null || md.getRa().getRaClass().equals(""))
            {
               log.fatal("No @Connector was found and no definition in the ra.xml metadata either");
               throw new ValidateException("No @Connector defined");
            }
         }
         else
         {
            // JBJCA-240
            if (md.getRa().getRaClass() == null || md.getRa().getRaClass().equals(""))
            {
               log.fatal("More than one @Connector was found but the correct one " + 
                         "wasn't defined in the ra.xml metadata");
               throw new ValidateException("More than one @Connector defined");
            }
         }
      }

      return md;
   }

   /**
    * Attach @Connector
    * @param md The metadata
    * @param raClass The class name for the resource adapter
    * @param c The connector
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachConnector(ConnectorMetaData md, String raClass, Connector c)
      throws Exception
   {
      // Class definition
      if (md.getRa() == null)
         md.setRa(new ResourceAdapterMetaData());

      md.getRa().setRaClass(raClass);

      // AuthenticationMechanism
      AuthenticationMechanism[] authMechanisms = c.authMechanisms();
      if (authMechanisms != null)
      {
         for (AuthenticationMechanism authMechanism : authMechanisms)
         {
            attachAuthenticationMechanism(md, authMechanism);
         }
      }

      DescriptionGroupMetaData descGroup = new DescriptionGroupMetaData();
      md.setDescriptionGroup(descGroup);
      
      // Description
      String[] description = c.description();
      if (description != null)
      {
         if (descGroup.getDescriptions() == null)
         {
            DescriptionsImpl descsImpl = new DescriptionsImpl();
            descGroup.setDescriptions(descsImpl);
         }
         for (String desc : description)
         {
            DescriptionImpl descImpl = new DescriptionImpl();
            descImpl.setDescription(desc);
            ((DescriptionsImpl)descGroup.getDescriptions()).add(descImpl);
         }
      }

      // Display name
      String[] displayName = c.displayName();
      if (displayName != null)
      {
         if (descGroup.getDisplayNames() == null)
         {
            DisplayNamesImpl dnsImpl = new DisplayNamesImpl();
            descGroup.setDisplayNames(dnsImpl);
         }
         for (String dn : displayName)
         {
            DisplayNameImpl dnImpl = new DisplayNameImpl();
            dnImpl.setDisplayName(dn);
            ((DisplayNamesImpl)descGroup.getDisplayNames()).add(dnImpl);
         }
      }

      // EIS type
      String eisType = c.eisType();
      if (eisType != null)
      {
         if (md.getEISType() == null)
            md.setEISType(eisType);
      }

      // Large icon
      String[] largeIcon = c.largeIcon();
      if (largeIcon != null)
      {
         if (descGroup.getIcons() == null)
         {
            IconsImpl icsImpl = new IconsImpl();
            descGroup.setIcons(icsImpl);
         }
         for (String large : largeIcon)
         {
            IconImpl icImpl = new IconImpl();
            icImpl.setLargeIcon(large);
            ((IconsImpl)descGroup.getIcons()).add(icImpl);
         }
      }

      // License description
      String[] licenseDescription = c.licenseDescription();
      if (licenseDescription != null)
      {
         if (md.getLicense() == null)
            md.setLicense(new LicenseMetaData());

         if (md.getLicense().getDescriptions() == null)
         {
            DescriptionsImpl descsImpl = new DescriptionsImpl();
            md.getLicense().setDescriptions(descsImpl);
         }
         for (String desc : licenseDescription)
         {
            DescriptionImpl descImpl = new DescriptionImpl();
            descImpl.setDescription(desc);
            ((DescriptionsImpl)md.getLicense().getDescriptions()).add(descImpl);
         }
      }

      // License required
      boolean licenseRequired = c.licenseRequired();
      if (md.getLicense() == null)
         md.setLicense(new LicenseMetaData());
      md.getLicense().setRequired(licenseRequired);

      // Reauthentication support
      boolean reauthenticationSupport = c.reauthenticationSupport();
      if (md.getRa() != null && md.getRa().getOutboundRa() != null)
      {
         md.getRa().getOutboundRa().setReAuthSupport(reauthenticationSupport);
      }

      // RequiredWorkContext
      Class<? extends WorkContext>[] requiredWorkContexts = c.requiredWorkContexts();
      if (requiredWorkContexts != null)
      {
         for (Class<? extends WorkContext> requiredWorkContext : requiredWorkContexts)
         {
            if (md instanceof JCA16Base)
            {
               JCA16Base jmd = (JCA16Base)md;
               if (jmd.getRequiredWorkContexts() == null)
                  jmd.setRequiredWorkContexts(new ArrayList<String>());

               if (!jmd.getRequiredWorkContexts().contains(requiredWorkContext.getName()))
               {
                  if (trace)
                     log.trace("RequiredWorkContext=" + requiredWorkContext.getName());

                  jmd.getRequiredWorkContexts().add(requiredWorkContext.getName());
               }
            }
         }
      }

      // Security permission
      SecurityPermission[] securityPermissions = c.securityPermissions();
      if (securityPermissions != null)
      {
         if (md.getRa() == null)
            md.setRa(new ResourceAdapterMetaData());

         if (md.getRa().getSecurityPermissions() == null)
            md.getRa().setSecurityPermissions(new ArrayList<SecurityPermissionMetaData>());

         for (SecurityPermission securityPermission : securityPermissions)
         {
            SecurityPermissionMetaData spmd = new SecurityPermissionMetaData();
            spmd.setSecurityPermissionSpec(securityPermission.permissionSpec());
            md.getRa().getSecurityPermissions().add(spmd);
         }
      }

      // Small icon
      String[] smallIcon = c.smallIcon();
      if (smallIcon != null)
      {
         IconsImpl icsImpl;
         if (descGroup.getIcons() == null)
         {
            icsImpl = new IconsImpl();
            descGroup.setIcons(icsImpl);
         }
         else
         {
            icsImpl = (IconsImpl)descGroup.getIcons();
         }
         IconImpl[] icArray = icsImpl.toArray(new IconImpl[icsImpl.size()]);
         for (int i = 0; i < smallIcon.length; i++)
         {
            if (i < icArray.length)
               icArray[i].setSmallIcon(smallIcon[i]);
            else
            {
               IconImpl icImpl = new IconImpl();
               icImpl.setLargeIcon(smallIcon[i]);
               icsImpl.add(icImpl);
            }
         }
      }

      // Transaction support
      TransactionSupport.TransactionSupportLevel transactionSupport = c.transactionSupport();
      if (md.getRa() != null && md.getRa().getOutboundRa() != null)
      {
         if (transactionSupport.equals(TransactionSupportLevel.NoTransaction))
         {
            md.getRa().getOutboundRa().setTransSupport(TransactionSupportMetaData.NoTransaction);
         }
         else if (transactionSupport.equals(TransactionSupportLevel.XATransaction))
         {
            md.getRa().getOutboundRa().setTransSupport(TransactionSupportMetaData.XATransaction);
         }
         else if (transactionSupport.equals(TransactionSupportLevel.LocalTransaction))
         {
            md.getRa().getOutboundRa().setTransSupport(TransactionSupportMetaData.LocalTransaction);
         }
      }

      // Vendor name
      String vendorName = c.vendorName();
      if (vendorName != null)
      {
         if (md.getVendorName() == null)
            md.setVendorName(vendorName);
      }

      // Version
      String version = c.version();
      if (version != null)
      {
         if (md.getRAVersion() == null)
            md.setRAVersion(version);
      }

      return md;
   }

   /**
    * Process: @ConnectionDefinitions
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData processConnectionDefinitions(ConnectorMetaData md, 
                                                          AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(ConnectionDefinitions.class);
      if (values != null)
      {
         if (values.size() == 1)
         {
            Annotation annotation = values.iterator().next();
            ConnectionDefinitions c = (ConnectionDefinitions)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + c);

            md = attachConnectionDefinitions(md , c, annotation.getClassName());
         }
         else
            throw new ValidateException("More than one @ConnectionDefinitions defined");
      }

      return md;
   }

   /**
    * Attach @ConnectionDefinitions
    * @param md The metadata
    * @param cds The connection definitions
    * @param mcf The managed connection factory
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachConnectionDefinitions(ConnectorMetaData md, 
                                                         ConnectionDefinitions cds,
                                                         String mcf)
      throws Exception
   {
      createConDefs(md);

      if (cds.value() != null)
      {
         for (ConnectionDefinition cd : cds.value())
         {
            md = attachConnectionDefinition(md, mcf, cd);
         }
      }

      return md;
   }

   /**
    * Process: @ConnectionDefinition
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData processConnectionDefinition(ConnectorMetaData md, 
                                                         AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(ConnectionDefinition.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            md = attachConnectionDefinition(md, annotation);
         }
      }

      return md;
   }

   /**
    * Attach @ConnectionDefinition
    * @param md The metadata
    * @param cd The connection definition
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachConnectionDefinition(ConnectorMetaData md, Annotation annotation)
      throws Exception
   {
      ConnectionDefinition cd = (ConnectionDefinition)annotation.getAnnotation();

      if (trace)
         log.trace("Processing: " + annotation);

      createConDefs(md);

      for (ConnectionDefinitionMetaData cdMeta : md.getRa().getOutboundRa().getConDefs())
      {
         if (cdMeta.getManagedConnectionFactoryClass().equals(annotation.getClassName()))
         {
            //ra.xml define
            return md;
         }
      }

      return attachConnectionDefinition(md, annotation.getClassName(), cd);
   }

   /**
    * Attach @ConnectionDefinition
    * @param md The metadata
    * @param mcf The managed connection factory
    * @param cd The connection definition
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachConnectionDefinition(ConnectorMetaData md, 
                                                        String mcf, 
                                                        ConnectionDefinition cd)
      throws Exception
   {
      if (trace)
         log.trace("Processing: " + cd);

      createConDefs(md);

      ConnectionDefinitionMetaData cdMeta = new ConnectionDefinitionMetaData();
      cdMeta.setManagedConnectionFactoryClass(mcf);
      cdMeta.setConnectionFactoryInterfaceClass(cd.connectionFactory().getName());
      cdMeta.setConnectionFactoryImplementationClass(cd.connectionFactoryImpl().getName());
      cdMeta.setConnectionInterfaceClass(cd.connection().getName());
      cdMeta.setConnectionImplementationClass(cd.connectionImpl().getName());
      md.getRa().getOutboundRa().getConDefs().add(cdMeta);
      return md;
   }

   /**
    * Process: @ConfigProperty
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData processConfigProperty(ConnectorMetaData md, 
                                                          AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(ConfigProperty.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            md = attachConfigProperty(md, annotation);
         }
      }

      return md;
   }

   /**
    * Attach @ConfigProperty
    * @param md The metadata
    * @param configProperty The config property
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachConfigProperty(ConnectorMetaData md, Annotation annotation)
      throws Exception
   {
      ConfigProperty configProperty = (ConfigProperty)annotation.getAnnotation();

      if (trace)
         log.trace("Processing: " + configProperty);

      // Ignore config-property which has ignore=true
      if (configProperty.ignore())
         return md;

      ConfigPropertyMetaData cfgMeta = new ConfigPropertyMetaData();
      cfgMeta.setName(getConfigPropertyName(annotation));

      if (configProperty.defaultValue() != null && !configProperty.defaultValue().equals(""))
         cfgMeta.setValue(configProperty.defaultValue());

      if (!Object.class.equals(configProperty.type()))
      {
         cfgMeta.setType(configProperty.type().getName());
      }
      else
      {
         cfgMeta.setType(getConfigPropertyType(annotation));
      }
      cfgMeta.setIgnore(configProperty.ignore());

      String[] description = configProperty.description();
      if (description != null)
      {
         if (cfgMeta.getDescriptions() == null)
         {
            DescriptionsImpl descsImpl = new DescriptionsImpl();
            cfgMeta.setDescriptions(descsImpl);
         }
         for (String desc : description)
         {
            DescriptionImpl descImpl = new DescriptionImpl();
            descImpl.setDescription(desc);
            ((DescriptionsImpl)cfgMeta.getDescriptions()).add(descImpl);
         }
      }
      
      String attachedClassName = annotation.getClassName();
      ClassLoader cl = SecurityActions.getThreadContextClassLoader();
      Class attachedClass = Class.forName(attachedClassName, true, cl);

      if (hasInterface(attachedClass, "javax.resource.spi.ResourceAdapter"))
      {
         if (md.getRa() == null)
         {
            md.setRa(new ResourceAdapterMetaData());
         }
         if (md.getRa().getConfigProperty() == null)
         {
            md.getRa().setConfigProperty(new ArrayList<ConfigPropertyMetaData>());
         }
         for (ConfigPropertyMetaData cpMeta : md.getRa().getConfigProperty())
         {
            if (cpMeta.getName().equals(cfgMeta.getName()))
            {
               return md;
            }
         }
         md.getRa().getConfigProperty().add(cfgMeta);
      }
      else if (hasInterface(attachedClass, "javax.resource.spi.ManagedConnectionFactory"))
      {
         createConDefs(md);
         for (ConnectionDefinitionMetaData cdMeta : md.getRa().getOutboundRa().getConDefs())
         {
            if (attachedClassName.equals(cdMeta.getManagedConnectionFactoryClass()))
            {
               if (cdMeta.getConfigProps() == null)
               {
                  cdMeta.setConfigProps(new ArrayList<ConfigPropertyMetaData>());
               }
               for (ConfigPropertyMetaData cpMeta : cdMeta.getConfigProps())
               {
                  if (cpMeta.getName().equals(cfgMeta.getName()))
                  {
                     return md;
                  }
               }
               cdMeta.getConfigProps().add(cfgMeta);
            }
         }
      }
      else if (hasInterface(attachedClass, "javax.resource.spi.ActivationSpec"))
      {
         createMessageListeners(md);
         for (MessageListenerMetaData mlMeta : md.getRa().getInboundRa().getMessageAdapter().getMessageListeners())
         {
            if (attachedClassName.equals(mlMeta.getActivationSpecType().getAsClass()))
            {
               if (mlMeta.getActivationSpecType().getConfigProps() == null)
               {
                  mlMeta.getActivationSpecType().setConfigProps(new ArrayList<ConfigPropertyMetaData>());
               }
               for (ConfigPropertyMetaData cpMeta : mlMeta.getActivationSpecType().getConfigProps())
               {
                  if (cpMeta.getName().equals(cfgMeta.getName()))
                  {
                     return md;
                  }
               }
               mlMeta.getActivationSpecType().getConfigProps().add(cfgMeta);
            }
         }
      }

      return md;
   }

   /**
    * hasInterface
    * 
    * @param c
    * @param targetClassName
    * @return
    */
   private boolean hasInterface(Class c, String targetClassName)
   {
      for (Class face : c.getInterfaces())
      {
         if (face.getName().equals(targetClassName))
         {
            return true;
         } 
         else
         {
            for (Class face2 : face.getInterfaces())
            {
               if (face2.getName().equals(targetClassName))
               {
                  return true;
               } 
               else if (hasInterface(face2, targetClassName))
               {
                  return true;
               }
            }
         }
      }
      if (null != c.getSuperclass())
      {
         return hasInterface(c.getSuperclass(), targetClassName);
      }
      return false;
   }

   /**
    * Attach @AuthenticationMechanism
    * @param md The metadata
    * @param authenticationmechanism The authentication mechanism
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachAuthenticationMechanism(ConnectorMetaData md, 
                                                                  AuthenticationMechanism authenticationmechanism)
      throws Exception
   {
      if (md.getRa() == null)
      {
         md.setRa(new ResourceAdapterMetaData());
      }
      if (md.getRa().getOutboundRa() == null)
      {
         md.getRa().setOutboundRa(new OutboundRaMetaData());
      }
      if (md.getRa().getOutboundRa().getAuthMechanisms() == null)
      {
         md.getRa().getOutboundRa().setAuthMechanisms(new ArrayList<AuthenticationMechanismMetaData>());
      }
      AuthenticationMechanismMetaData ammd = new AuthenticationMechanismMetaData();
      ammd.setAuthenticationMechanismType(authenticationmechanism.authMechanism());
      
      String credentialInterfaceClass = null;
      if (authenticationmechanism.credentialInterface().equals(CredentialInterface.GenericCredential))
      {
         credentialInterfaceClass = "javax.resource.spi.security.GenericCredential";
      }
      else if (authenticationmechanism.credentialInterface().equals(CredentialInterface.GSSCredential))
      {
         credentialInterfaceClass = "org.ietf.jgss.GSSCredential";
      }
      else if (authenticationmechanism.credentialInterface().equals(CredentialInterface.PasswordCredential))
      {
         credentialInterfaceClass = "javax.resource.spi.security.PasswordCredential";
      }
      ammd.setCredentialInterfaceClass(credentialInterfaceClass);
      
      String[] description = authenticationmechanism.description();
      if (description != null)
      {
         if (ammd.getDescriptions() == null)
         {
            DescriptionsImpl descsImpl = new DescriptionsImpl();
            ammd.setDescriptions(descsImpl);
         }
         for (String desc : description)
         {
            DescriptionImpl descImpl = new DescriptionImpl();
            descImpl.setDescription(desc);
            ((DescriptionsImpl)ammd.getDescriptions()).add(descImpl);
         }
      }
      
      md.getRa().getOutboundRa().getAuthMechanisms().add(ammd);

      return md;
   }

   /**
    * Process: @AdministeredObject
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData processAdministeredObject(ConnectorMetaData md, 
                                                              AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(AdministeredObject.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            AdministeredObject a = (AdministeredObject)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + a);

            md = attachAdministeredObject(md, a);
         }
      }

      return md;
   }

   /**
    * Attach @AdministeredObject
    * @param md The metadata
    * @param a The administered object
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachAdministeredObject(ConnectorMetaData md, AdministeredObject a)
      throws Exception
   {
      createAdminObject(md);
      String aoName = null;
      if (a.adminObjectInterfaces().length > 0)
      {
         aoName = ((Class)Array.get(a.adminObjectInterfaces(), 0)).getName();
      }
      AdminObjectMetaData aomd = new AdminObjectMetaData();
      aomd.setAdminObjectInterfaceClass(aoName);
      md.getRa().getAdminObjects().add(aomd);
      return md;
   }

   /**
    * Process: @Activation
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData processActivation(ConnectorMetaData md, 
                                                      AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(Activation.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            md = attachActivation(md, annotation);
         }
      }

      return md;
   }

   /**
    * Attach @Activation
    * @param md The metadata
    * @param activation The activation
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData attachActivation(ConnectorMetaData md, Annotation annotation)
      throws Exception
   {
      Activation activation = (Activation)annotation.getAnnotation();

      if (trace)
         log.trace("Processing: " + activation);
      
      createMessageListeners(md);
      for (Class asClass : activation.messageListeners())
      {
         ActivationspecMetaData asMeta = new ActivationspecMetaData();
         asMeta.setAsClass(annotation.getClassName());
         MessageListenerMetaData mlMeta = new MessageListenerMetaData();
         mlMeta.setActivationSpecType(asMeta);
         mlMeta.setType(asClass.getName());
         md.getRa().getInboundRa().getMessageAdapter().getMessageListeners().add(mlMeta);
      }
      return md;
   }
   

   /**
    * createMessageListeners
    * @param md
    * @throws Exception
    */
   private void createMessageListeners(ConnectorMetaData md) throws Exception
   {
      if (md.getRa() == null)
      {
         md.setRa(new ResourceAdapterMetaData());
      }
      if (md.getRa().getInboundRa() == null)
      {
         md.getRa().setInboundRa(new InboundRaMetaData());
      }
      if (md.getRa().getInboundRa().getMessageAdapter() == null)
      {
         md.getRa().getInboundRa().setMessageAdapter(new MessageAdapterMetaData());
      }
      if (md.getRa().getInboundRa().getMessageAdapter().getMessageListeners() == null)
      {
         md.getRa().getInboundRa().getMessageAdapter().setMessageListeners(new ArrayList<MessageListenerMetaData>());
      }
   }

   /**
    * createAdminObject
    * @param md
    * @throws Exception
    */
   private void createAdminObject(ConnectorMetaData md) throws Exception
   {
      if (md.getRa() == null)
      {
         md.setRa(new ResourceAdapterMetaData());
      }
      if (md.getRa().getAdminObjects() == null)
      {
         md.getRa().setAdminObjects(new ArrayList<AdminObjectMetaData>());
      }
   }

   /**
    * createConDefs
    * @param md
    * @throws Exception
    */
   private void createConDefs(ConnectorMetaData md) throws Exception
   {
      if (md.getRa() == null)
      {
         md.setRa(new ResourceAdapterMetaData());
      }
      if (md.getRa().getOutboundRa() == null)
      {
         md.getRa().setOutboundRa(new OutboundRaMetaData());
      }
      if (md.getRa().getOutboundRa().getConDefs() == null)
      {
         md.getRa().getOutboundRa().setConDefs(new ArrayList<ConnectionDefinitionMetaData>());
      }
   }

   /**
    * Get the config-property-name for an annotation
    * @param annotation The annotation
    * @return The name
    * @exception ClassNotFoundException Thrown if a class cannot be found
    * @exception NoSuchFieldException Thrown if a field cannot be found
    * @exception NoSuchMethodException Thrown if a method cannot be found
    */
   private String getConfigPropertyName(Annotation annotation)
      throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException
   {
      if (AnnotationType.FIELD.equals(annotation.getType()))
      {
         return annotation.getMemberName();
      }
      else if (AnnotationType.METHOD.equals(annotation.getType()))
      {
         String name = annotation.getMemberName();

         if (name.startsWith("set"))
         {
            name = name.substring(3);
         }
         else if (name.startsWith("get"))
         {
            name = name.substring(3);
         }
         else if (name.startsWith("is"))
         {
            name = name.substring(2);
         }

         if (name.length() > 1)
         {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
         }
         else
         {
            return Character.toString(Character.toLowerCase(name.charAt(0)));
         }
      }

      throw new IllegalArgumentException("Unknown annotation: " + annotation);
   }

   /**
    * Get the config-property-type for an annotation
    * @param annotation The annotation
    * @return The fully qualified classname
    * @exception ClassNotFoundException Thrown if a class cannot be found
    */
   @SuppressWarnings("unchecked") 
   private String getConfigPropertyType(Annotation annotation)
      throws ClassNotFoundException
   {
      if (AnnotationType.FIELD.equals(annotation.getType()))
      {
         ClassLoader cl = SecurityActions.getThreadContextClassLoader();
         Class clz = Class.forName(annotation.getClassName(), true, cl);

         while (!Object.class.equals(clz))
         {
            try
            {
               Field field = clz.getDeclaredField(annotation.getMemberName());

               return field.getType().getName();
            }
            catch (NoSuchFieldException nsfe)
            {
               clz = clz.getSuperclass();
            }
         }
      }
      else if (AnnotationType.METHOD.equals(annotation.getType()))
      {
         ClassLoader cl = SecurityActions.getThreadContextClassLoader();
         Class clz = Class.forName(annotation.getClassName(), true, cl);

         Class[] parameters = null;

         if (annotation.getParameterTypes() != null)
         {
            parameters = new Class[annotation.getParameterTypes().length];

            for (int i = 0; i < annotation.getParameterTypes().length; i++)
            {
               String parameter = annotation.getParameterTypes()[i];
               parameters[i] = Class.forName(parameter, true, cl);
            }
         }

         while (!Object.class.equals(clz))
         {
            try
            {
               Method method = clz.getDeclaredMethod(annotation.getMemberName(), parameters);
         
               if (void.class.equals(method.getReturnType()))
               {
                  if (parameters != null && parameters.length > 0)
                  {
                     return parameters[0].getName();
                  }
               }
               else
               {
                  return method.getReturnType().getName();
               }
            }
            catch (NoSuchMethodException nsme)
            {
               clz = clz.getSuperclass();
            }
         }
      }

      throw new IllegalArgumentException("Unknown annotation: " + annotation);
   }
}
