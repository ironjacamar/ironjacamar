/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.as.converters.wls;

import org.jboss.jca.as.converters.WlsRaConverter;
import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.metadata.WeblogicRaPasrer;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.resourceadapter.Activations;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.resourceadapter.WorkManager;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdapterParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A weblogic -ra.xml parser test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class WlsRaConvertTestCase
{
   private static Logger log = Logger.getLogger(WlsRaConvertTestCase.class);
   
   private String wlsRaFilesName = "weblogic-ra.xml";
   private String ijRaFilesName = "expected-outcome-wls-ra.xml";
   
   /**
    * test xa ds convert
    * @throws Throwable throwable exception 
    */
   @Test
   public void testRaConvert() throws Throwable
   {
      System.out.println("\nStart parse... " + wlsRaFilesName);

      InputStream in = WlsRaConvertTestCase.class.getClassLoader().getResourceAsStream("wlsra/" + wlsRaFilesName);
      WeblogicRaPasrer parser = new WeblogicRaPasrer();
      WeblogicConnector wlsConnector = parser.parse(in);
      assertNotNull(wlsConnector);
      
      ResourceAdapterParser raParser = new ResourceAdapterParser();
      InputStream ijRaIn = WlsRaConvertTestCase.class.getClassLoader().getResourceAsStream("wlsra/" + ijRaFilesName);
      Activations ras = raParser.parse(ijRaIn);
      List<Activation> listRa = ras.getActivations();
      assertNotNull(listRa);
      Activation target = listRa.get(0);
      assertNotNull(target);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      WlsRaConverter converter = new WlsRaConverter();
      converter.convert(wlsConnector, baos);
      
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());
      List<Activation> listRb = raParser.parse(bais).getActivations();
      assertNotNull(listRb);
      Activation source = listRb.get(0);
      assertNotNull(source);

      
      checkMapEqualSize(source.getConfigProperties(), target.getConfigProperties());
      assertConfigProps(source.getConfigProperties(), target.getConfigProperties());
      
      assertAdminObjects(source.getAdminObjects(), target.getAdminObjects());

      assertResourceAdapter(source.getConnectionDefinitions(), target.getConnectionDefinitions());
      
      asesertSecurity(source.getWorkManager(), target.getWorkManager());
   }

   private void assertAdminObjects(List<AdminObject> source, List<AdminObject> target)
   {
      checkListEqualSize(source, target);
      
      for (int i = 0; i < source.size(); i++)
      {
         AdminObject aoSource =  source.get(i);
         AdminObject aoTarget =  target.get(i);
         assertEquals(aoSource.getClassName(), aoTarget.getClassName());
         assertEquals(aoSource.getJndiName(), aoTarget.getJndiName());
         assertEquals(aoSource.getConfigProperties().size(), aoTarget.getConfigProperties().size());
         assertConfigProps(aoSource.getConfigProperties(), aoTarget.getConfigProperties());
      }
   }
   
   private void checkListEqualSize(List<?> source, List<?> target)
   {
      assertNotNull(source);
      assertNotNull(target);
      assertEquals(source.size(), target.size());
   }
   private void checkMapEqualSize(Map<?, ?> source, Map<?, ?> target)
   {
      assertNotNull(source);
      assertNotNull(target);
      assertEquals(source.size(), target.size());
   }

   private void assertConfigProps(Map<String, String> source, Map<String, String> target)
   {
      for (String key : source.keySet())
      {
         assertEquals(source.get(key), target.get(key));
      }
   }

   private void assertResourceAdapter(List<ConnectionDefinition> source, List<ConnectionDefinition> target)
   {
      checkListEqualSize(source, target);
      
      for (int i = 0; i < source.size(); i++)
      {
         ConnectionDefinition raSource =  source.get(i);
         ConnectionDefinition raTarget =  target.get(i);
         checkMapEqualSize(raSource.getConfigProperties(), raTarget.getConfigProperties());
         assertConfigProps(raSource.getConfigProperties(), raTarget.getConfigProperties());
         assertEquals(raSource.getJndiName(), raTarget.getJndiName());
         

         assertPool(raSource.getPool(), raTarget.getPool());
         assertTimeout(raSource.getTimeOut(), raTarget.getTimeOut());
         assertValidation(raSource.getValidation(), raTarget.getValidation());
      }
   }

   private void assertPool(Pool source, Pool target)
   {
      assertNotNull(source);
      assertNotNull(target);
      assertEquals(source.getMinPoolSize(), target.getMinPoolSize());
      assertEquals(source.getMaxPoolSize(), target.getMaxPoolSize());

      assertNotNull(source.getCapacity());
      assertNotNull(target.getCapacity());
      assertEquals(source.getCapacity().getIncrementer().
                   getConfigPropertiesMap().get("Size"), 
                   target.getCapacity().getIncrementer().
                   getConfigPropertiesMap().get("Size"));
   }
   
   private void assertTimeout(TimeOut source, TimeOut target)
   {
      assertNotNull(source);
      assertNotNull(target);
      assertEquals(source.getBlockingTimeoutMillis(), target.getBlockingTimeoutMillis());
      assertEquals(source.getIdleTimeoutMinutes(), target.getIdleTimeoutMinutes());
      assertEquals(source.getAllocationRetryWaitMillis(), target.getAllocationRetryWaitMillis());
   }
   
   private void assertValidation(Validation source, Validation target)
   {
      assertNotNull(source);
      assertNotNull(target);
      assertEquals(source.getBackgroundValidationMillis(), target.getBackgroundValidationMillis());
   }
   
   private void asesertSecurity(WorkManager source, WorkManager target)
   {
      assertNotNull(source);
      assertNotNull(target);
      assertEquals(source.getSecurity().isMappingRequired(), target.getSecurity().isMappingRequired());
      assertEquals(source.getSecurity().getDefaultPrincipal(), target.getSecurity().getDefaultPrincipal());
      checkListEqualSize(source.getSecurity().getDefaultGroups(), target.getSecurity().getDefaultGroups());
      checkMapEqualSize(source.getSecurity().getUserMappings(), target.getSecurity().getUserMappings());
      assertConfigProps(source.getSecurity().getUserMappings(), target.getSecurity().getUserMappings());
      checkMapEqualSize(source.getSecurity().getGroupMappings(), target.getSecurity().getGroupMappings());
      assertConfigProps(source.getSecurity().getGroupMappings(), target.getSecurity().getGroupMappings());
   }
}
