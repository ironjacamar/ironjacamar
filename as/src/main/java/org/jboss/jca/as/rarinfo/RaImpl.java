/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.rarinfo;

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;

import java.util.List;
import java.util.Map;

/**
 * A Resource Adpater impl.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class RaImpl
{
   private ActivationImpl raImpl = null;

   private TransactionSupportEnum transactionSupport;
   private List<ConnectionDefinition> connectionDefinitions;
   private List<AdminObject> adminObjects;
   private Map<String, String> raConfigProperties;
   private String rarName;


   /**
    * RaImpl 
    * @param rarName rarName
    * @param transactionSupport transactionSupport
    * @param connectionDefinitions connectionDefinitions
    * @param adminObjects adminObjects
    * @param raConfigProperties raConfigProperties
    */
   public RaImpl(String rarName, TransactionSupportEnum transactionSupport,
                 List<ConnectionDefinition> connectionDefinitions,
                 List<AdminObject> adminObjects, Map<String, String> raConfigProperties)
   {
      this.rarName = rarName;
      this.transactionSupport = transactionSupport;
      this.connectionDefinitions = connectionDefinitions;
      this.raConfigProperties = raConfigProperties;
      this.adminObjects = adminObjects;
   }
   
   /**
    * buildResourceAdapterImpl
    * @throws Exception exception
    */
   public void buildResourceAdapterImpl()  throws Exception
   {
      raImpl = new ActivationImpl(null, rarName, transactionSupport, connectionDefinitions, adminObjects,
                                  raConfigProperties, null, null, null);
   }
   
   @Override
   public String toString()
   {
      String out = raImpl.toString();
      return out;
   }
}
