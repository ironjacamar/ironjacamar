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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.fungal.deployers.DeployException;

import java.lang.reflect.Array;
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

import org.jboss.metadata.rar.spec.AdminObjectMetaData;
import org.jboss.metadata.rar.spec.AuthenticationMechanismMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.InboundRaMetaData;
import org.jboss.metadata.rar.spec.JCA16DTDMetaData;
import org.jboss.metadata.rar.spec.JCA16DefaultNSMetaData;
import org.jboss.metadata.rar.spec.JCA16MetaData;
import org.jboss.metadata.rar.spec.LicenseMetaData;
import org.jboss.metadata.rar.spec.OutboundRaMetaData;
import org.jboss.metadata.rar.spec.ResourceAdapterMetaData;
import org.jboss.metadata.rar.spec.SecurityPermissionMetaData;
import org.jboss.metadata.rar.spec.TransactionSupportMetaData;
import org.jboss.papaki.Annotation;
import org.jboss.papaki.AnnotationRepository;

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
   private Annotations()
   {
   }

   /**
    * Process annotations
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   public static ConnectorMetaData process(ConnectorMetaData md, AnnotationRepository annotationRepository)
      throws Exception
   {
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

      // @ConfigProperty
      md = processConfigProperty(md, annotationRepository);

      // @AuthenticationMechanism
      md = processAuthenticationMechanism(md, annotationRepository);

      // @AdministeredObject
      md = processAdministeredObject(md, annotationRepository);

      // @Activation
      md = processActivation(md, annotationRepository);

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
   private static ConnectorMetaData processConnector(ConnectorMetaData md, AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(Connector.class);
      if (values != null)
      {
         if (values.size() == 1)
         {
            Annotation annotation = values.iterator().next();
            Connector c = (Connector)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + c);

            md = attachConnector(md, c);
         }
         else
            throw new DeployException("More than one @Connector defined");
      }

      return md;
   }

   /**
    * Attach @Connector
    * @param md The metadata
    * @param c The connector
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private static ConnectorMetaData attachConnector(ConnectorMetaData md, Connector c)
      throws Exception
   {
      // AuthenticationMechanism
      AuthenticationMechanism[] authMechanisms = c.authMechanisms();
      if (authMechanisms != null)
      {
         for (AuthenticationMechanism authMechanism : authMechanisms)
         {
            attachAuthenticationMechanism(md, authMechanism);
         }
      }

      // Description
      String[] description = c.description();
      if (description != null)
      {
         // TODO
      }

      // Display name
      String[] displayName = c.displayName();
      if (displayName != null)
      {
         // TODO
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
         // TODO
      }

      // License description
      String[] licenseDescription = c.licenseDescription();
      if (licenseDescription != null)
      {
         if (md.getLicense() == null)
            md.setLicense(new LicenseMetaData());

         // TODO
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
            if (md instanceof JCA16MetaData)
            {
               JCA16MetaData jmd = (JCA16MetaData)md;
               if (jmd.getRequiredWorkContexts() == null)
                  jmd.setRequiredWorkContexts(new ArrayList<String>());

               if (!jmd.getRequiredWorkContexts().contains(requiredWorkContext.getName()))
               {
                  if (trace)
                     log.trace("RequiredWorkContext=" + requiredWorkContext.getName());

                  jmd.getRequiredWorkContexts().add(requiredWorkContext.getName());
               }
            }
            else if (md instanceof JCA16DefaultNSMetaData)
            {
               JCA16DefaultNSMetaData jmd = (JCA16DefaultNSMetaData)md;
               if (jmd.getRequiredWorkContexts() == null)
                  jmd.setRequiredWorkContexts(new ArrayList<String>());

               if (!jmd.getRequiredWorkContexts().contains(requiredWorkContext.getName()))
               {
                  if (trace)
                     log.trace("RequiredWorkContext=" + requiredWorkContext.getName());

                  jmd.getRequiredWorkContexts().add(requiredWorkContext.getName());
               }
            }
            else if (md instanceof JCA16DTDMetaData)
            {
               JCA16DTDMetaData jmd = (JCA16DTDMetaData)md;
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
         // TODO
      }

      // Spec version
      String specVersion = c.specVersion();
      md.setVersion("1.6");

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
   private static ConnectorMetaData processConnectionDefinitions(ConnectorMetaData md, 
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

            md = attachConnectionDefinitions(md , c);
         }
         else
            throw new DeployException("More than one @ConnectionDefinitions defined");
      }

      return md;
   }

   /**
    * Attach @ConnectionDefinitions
    * @param md The metadata
    * @param cds The connection definitions
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private static ConnectorMetaData attachConnectionDefinitions(ConnectorMetaData md, 
                                                                ConnectionDefinitions cds)
      throws Exception
   {
      return md;
   }

   /**
    * Process: @ConnectionDefinition
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private static ConnectorMetaData processConnectionDefinition(ConnectorMetaData md, 
                                                                AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(ConnectionDefinition.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            ConnectionDefinition c = (ConnectionDefinition)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + c);

            md = attachConnectionDefinition(md, c);
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
   private static ConnectorMetaData attachConnectionDefinition(ConnectorMetaData md, 
                                                               ConnectionDefinition cd)
      throws Exception
   {
      return md;
   }

   /**
    * Process: @ConfigProperty
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private static ConnectorMetaData processConfigProperty(ConnectorMetaData md, 
                                                          AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(ConfigProperty.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            ConfigProperty c = (ConfigProperty)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + c);

            md = attachConfigProperty(md, c);
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
   private static ConnectorMetaData attachConfigProperty(ConnectorMetaData md, 
                                                         ConfigProperty configProperty)
      throws Exception
   {
      return md;
   }

   /**
    * Process: @AuthenticationMechanism
    * @param md The metadata
    * @param annotationRepository The annotation repository
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private static ConnectorMetaData processAuthenticationMechanism(ConnectorMetaData md, 
                                                                   AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(AuthenticationMechanism.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            AuthenticationMechanism a = (AuthenticationMechanism)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + a);

            md = attachAuthenticationMechanism(md, a);
         }
      }

      return md;
   }

   /**
    * Attach @AuthenticationMechanism
    * @param md The metadata
    * @param authenticationmechanism The authentication mechanism
    * @return The updated metadata
    * @exception Exception Thrown if an error occurs
    */
   private static ConnectorMetaData attachAuthenticationMechanism(ConnectorMetaData md, 
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
   private static ConnectorMetaData processAdministeredObject(ConnectorMetaData md, 
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
   private static ConnectorMetaData attachAdministeredObject(ConnectorMetaData md, AdministeredObject a)
      throws Exception
   {
      if (md.getRa() == null)
      {
         md.setRa(new ResourceAdapterMetaData());
      }
      if (md.getRa().getAdminObjects() == null)
      {
         md.getRa().setAdminObjects(new ArrayList<AdminObjectMetaData>());
      }
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
   private static ConnectorMetaData processActivation(ConnectorMetaData md, 
                                                      AnnotationRepository annotationRepository)
      throws Exception
   {
      Collection<Annotation> values = annotationRepository.getAnnotation(Activation.class);
      if (values != null)
      {
         for (Annotation annotation : values)
         {
            Activation a = (Activation)annotation.getAnnotation();

            if (trace)
               log.trace("Processing: " + a);

            md = attachActivation(md, a);
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
   private static ConnectorMetaData attachActivation(ConnectorMetaData md, Activation activation)
      throws Exception
   {
      if (md.getRa() == null)
      {
         md.setRa(new ResourceAdapterMetaData());
      }
      if (md.getRa().getInboundRa() == null)
      {
         md.getRa().setInboundRa(new InboundRaMetaData());
      }
      //TODO
      return md;
   }
}
