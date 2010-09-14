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
package org.jboss.jca.common.metadata.ironjacamar;

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.metadata.common.CommonIronJacamarImpl;

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

}

