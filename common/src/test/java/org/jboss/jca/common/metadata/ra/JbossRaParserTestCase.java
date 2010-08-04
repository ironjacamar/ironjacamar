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
package org.jboss.jca.common.metadata.ra;

import org.jboss.jca.common.api.metadata.jbossra.JbossRa;
import org.jboss.jca.common.api.metadata.jbossra.jbossra20.JbossRa20;
import org.jboss.jca.common.metadataimpl.jbossra.JbossRaParser;
import org.jboss.jca.common.metadataimpl.jbossra.jbossra10.JbossRa10Impl;
import org.jboss.jca.common.metadataimpl.jbossra.jbossra20.JbossRa20Impl;
import org.jboss.jca.common.metadataimpl.ra.common.RaConfigPropertyImpl;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 *
 * A RaParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class JbossRaParserTestCase
{
   /**
    * shouldParseJbossRa10WithSingleProperty
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseJbossRa10WithSingleProperty() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("jboss-ra-1.0-single-attribute.xml").toURI());
         is = new FileInputStream(xmlFile);
         JbossRaParser parser = new JbossRaParser();
         //when
         JbossRa jbossRa = parser.parse(is);
         //then
         assertThat(jbossRa, instanceOf(JbossRa10Impl.class));
         assertThat(jbossRa.getRaConfigProperties().size(), is(1));
         assertThat(
               jbossRa
                     .getRaConfigProperties()
                     .get(0)
                     .equals(
                           RaConfigPropertyImpl.buildRaConfigProperty("ra-config-property-name0",
                                 "ra-config-property-value0",
                                 "ra-config-property-type0", null)), is(true));

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    *
    * shouldParseJbossRa10WithOutProperties
    *
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseJbossRa10WithOutProperties() throws Exception
   {

      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("jboss-ra-1.0-no-attributes.xml").toURI());
         is = new FileInputStream(xmlFile);
         JbossRaParser parser = new JbossRaParser();
         //when
         JbossRa jbossRa = parser.parse(is);
         //then
         assertThat(jbossRa, instanceOf(JbossRa10Impl.class));
         assertThat(jbossRa.getRaConfigProperties().size(), is(0));

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    *
    * shouldParseJbossRa10WithMultipleProperties
    *
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseJbossRa10WithMultipleProperties() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("jboss-ra-1.0-multiple-attributes.xml").toURI());
         is = new FileInputStream(xmlFile);
         JbossRaParser parser = new JbossRaParser();
         //when
         JbossRa jbossRa = parser.parse(is);
         //then
         assertThat(jbossRa, instanceOf(JbossRa10Impl.class));
         assertThat(jbossRa.getRaConfigProperties().size(), is(2));
         assertThat(
               jbossRa
                     .getRaConfigProperties()
                     .get(0)
                     .equals(
                           RaConfigPropertyImpl.buildRaConfigProperty("ra-config-property-name0",
                                 "ra-config-property-value0",
                                 "ra-config-property-type0", null)), is(true));
         assertThat(
               jbossRa
                     .getRaConfigProperties()
                     .get(1)
                     .equals(
                           RaConfigPropertyImpl.buildRaConfigProperty("ra-config-property-name1",
                                 "ra-config-property-value1",
                                 "ra-config-property-type1", null)), is(true));
      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    * shouldParseJbossRa20WithSingleProperty
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseJbossRa20WithSingleProperty() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("jboss-ra-2.0-single-attribute.xml").toURI());
         is = new FileInputStream(xmlFile);
         JbossRaParser parser = new JbossRaParser();
         //when
         JbossRa jbossRa = parser.parse(is);
         //then
         assertThat(jbossRa, instanceOf(JbossRa20Impl.class));
         assertThat(jbossRa.getRaConfigProperties().size(), is(1));
         assertThat(
               jbossRa
                     .getRaConfigProperties()
                     .get(0)
                     .equals(
                           RaConfigPropertyImpl.buildRaConfigProperty("ra-config-property-name0",
                                 "ra-config-property-value0",
                                 "java.lang.Boolean", null)), is(true));
         assertThat(((JbossRa20) jbossRa).getBeanValidationGroups().size(), is(1));
         assertThat(((JbossRa20) jbossRa).getBootstrapContext(), is("bootstrap-context0"));

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    *
    * shouldParseJbossRa20WithOutProperties
    *
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseJbossRa20WithOutProperties() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("jboss-ra-2.0-no-attributes.xml").toURI());
         is = new FileInputStream(xmlFile);
         JbossRaParser parser = new JbossRaParser();
         //when
         JbossRa jbossRa = parser.parse(is);
         //then
         assertThat(jbossRa, instanceOf(JbossRa20Impl.class));
         assertThat(jbossRa.getRaConfigProperties().size(), is(0));
         assertThat(((JbossRa20) jbossRa).getBeanValidationGroups().size(), is(0));

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    *
    * shouldParseJbossRa20WithMultipleProperties
    *
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseJbossRa20WithMultipleProperties() throws Exception
   {

      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("jboss-ra-2.0-multiple-attributes.xml").toURI());
         is = new FileInputStream(xmlFile);
         JbossRaParser parser = new JbossRaParser();
         //when
         JbossRa jbossRa = parser.parse(is);
         //then
         assertThat(jbossRa, instanceOf(JbossRa20Impl.class));
         assertThat(jbossRa.getRaConfigProperties().size(), is(2));
         assertThat(
               jbossRa
                     .getRaConfigProperties()
                     .get(0)
                     .equals(
                           RaConfigPropertyImpl.buildRaConfigProperty("ra-config-property-name0",
                                 "ra-config-property-value0",
                                 "java.lang.Boolean", null)), is(true));
         assertThat(
               jbossRa
                     .getRaConfigProperties()
                     .get(1)
                     .equals(
                           RaConfigPropertyImpl.buildRaConfigProperty("ra-config-property-name1",
                                 "ra-config-property-value1",
                                 "java.lang.Boolean", null)), is(true));
         assertThat(((JbossRa20) jbossRa).getBeanValidationGroups().size(), is(2));
         assertThat(((JbossRa20) jbossRa).getBootstrapContext(), is("bootstrap-context0"));

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   //   @Test
   //   public void shouldParseJbossRa10WithTonsOfProperties() throws Exception
   //   {
   //      //given
   //      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
   //            .getResource("jboss-ra-1.0-tons-attributes.xml").toURI());
   //      JbossRaParser parser = new JbossRaParser();
   //      //when
   //      JbossRa jbossRa = parser.parse(new FileInputStream(xmlFile));
   //      //then
   //      assertThat(jbossRa, instanceOf(JbossRa10.class));
   //      assertThat(jbossRa.getRaConfigProperties().size(), is(200000));
   //      assertThat(
   //            jbossRa
   //                  .getRaConfigProperties()
   //                  .get(0)
   //                  .equals(
   //                        RaConfigProperty.buildRaConfigProperty("ra-config-property-name0",
   //   "ra-config-property-value0",
   //                              "ra-config-property-type0", null)), is(true));
   //      assertThat(
   //            jbossRa
   //                  .getRaConfigProperties()
   //                  .get(1)
   //                  .equals(
   //                        RaConfigProperty.buildRaConfigProperty("ra-config-property-name1",
   //"ra-config-property-value1",
   //                              "ra-config-property-type1", null)), is(true));
   //   }

}
