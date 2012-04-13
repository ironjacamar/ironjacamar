/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.ironjacamar.v10;

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.v10.CommonConnDef;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.metadata.common.v10.CommonIronJacamarImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * A concrete IronJacamarImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class IronJacamarImpl extends CommonIronJacamarImpl implements IronJacamar
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -8994120864846088078L;

   /**
    *
    * Create a new IronJacamarImpl.
    *
    * @param transactionSupport transactionSupport
    * @param configProperties configProperties
    * @param adminObjects adminObjects
    * @param connectionDefinitions connectionDefinitions
    * @param beanValidationGroups beanValidationGroups
    * @param bootstrapContext bootstrapContext
    */
   public IronJacamarImpl(TransactionSupportEnum transactionSupport, Map<String, String> configProperties,
      List<CommonAdminObject> adminObjects, List<CommonConnDef> connectionDefinitions,
      List<String> beanValidationGroups, String bootstrapContext)
   {
      super(transactionSupport, configProperties, adminObjects, connectionDefinitions, beanValidationGroups,
            bootstrapContext);
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<ironjacamar>");

      if (beanValidationGroups != null && beanValidationGroups.size() > 0)
      {
         sb.append("<").append(IronJacamar.Tag.BEAN_VALIDATION_GROUPS).append(">");
         for (String bvg : beanValidationGroups)
         {
            sb.append("<").append(IronJacamar.Tag.BEAN_VALIDATION_GROUP).append(">");
            sb.append(bvg);
            sb.append("</").append(IronJacamar.Tag.BEAN_VALIDATION_GROUP).append(">");
         }
         sb.append("</").append(IronJacamar.Tag.BEAN_VALIDATION_GROUPS).append(">");
      }

      if (bootstrapContext != null)
      {
         sb.append("<").append(IronJacamar.Tag.BOOTSTRAP_CONTEXT).append(">");
         sb.append(bootstrapContext);
         sb.append("</").append(IronJacamar.Tag.BOOTSTRAP_CONTEXT).append(">");
      }

      if (configProperties != null && configProperties.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = configProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append(IronJacamar.Tag.CONFIG_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(IronJacamar.Tag.CONFIG_PROPERTY).append(">");
         }
      }
      
      if (transactionSupport != null)
      {
         sb.append("<").append(IronJacamar.Tag.TRANSACTION_SUPPORT).append(">");
         sb.append(transactionSupport);
         sb.append("</").append(IronJacamar.Tag.TRANSACTION_SUPPORT).append(">");
      }

      if (connectionDefinitions != null && connectionDefinitions.size() > 0)
      {
         sb.append("<").append(IronJacamar.Tag.CONNECTION_DEFINITIONS).append(">");
         for (CommonConnDef cd : connectionDefinitions)
         {
            sb.append(cd);
         }
         sb.append("</").append(IronJacamar.Tag.CONNECTION_DEFINITIONS).append(">");
      }

      if (adminObjects != null && adminObjects.size() > 0)
      {
         sb.append("<").append(IronJacamar.Tag.ADMIN_OBJECTS).append(">");
         for (CommonAdminObject ao : adminObjects)
         {
            sb.append(ao);
         }
         sb.append("</").append(IronJacamar.Tag.ADMIN_OBJECTS).append(">");
      }

      sb.append("</ironjacamar>");
      
      return sb.toString();
   }
}

