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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.ds.v11.DsParser;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 * A DsParserForValidatorForTemplateReplaceTestCase. See also JBJCA-263 jira
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class DsParserForTemplateReplaceTestCase extends DsParserTestBase
{

   /**
   *
   * beforeClass method
   *
   * @throws Exception in casae of file not found
   */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      parser = new DsParser();
      System.setProperty("max.pool", "10");
      System.setProperty("jndi.name", "java:/H2DS");
      System.setProperty("jndi.name.prefix", "java:/");
      System.setProperty("jndi.name.suffix", "H2DS");

   }

   /**
    *
    * shouldReplaceTemplateElementFromSystemProperty
    *
    * @throws Exception in case of parser error
    * thrown
    */
   @Test
   public void shouldReplaceTemplateElementFromSystemProperty() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-max-pool-ds.xml");

      Integer actualMaxPoolSize = ds.getDataSource().get(0).getPool().getMaxPoolSize();
      assertThat(actualMaxPoolSize, is(10));
   }

   /**
   *
   * shouldReplaceTemplateAttributeFromSystemProperty
   *
   * @throws Exception in case of parser error
   */
   @Test
   public void shouldReplaceTemplateAttributeFromSystemProperty() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-jndi-name-ds.xml");

      String actualJndiName = ds.getDataSource().get(0).getJndiName();
      assertThat(actualJndiName, is("java:/H2DS"));
   }

   /**
   *
   * shouldReplaceTemplatePartialAttributeFromSystemProperty
   *
   * @throws Exception in case of parser error
   */
   @Test
   public void shouldReplaceTemplatePartialAttributeFromSystemProperty() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-jndi-name-partial-ds.xml");

      String actualJndiName = ds.getDataSource().get(0).getJndiName();
      assertThat(actualJndiName, is("java:/H2DS"));
   }

   /**
   *
   * shouldReplaceTemplateTwoPartsAttributeFromSystemProperty
   *
   * @throws Exception in case of parser error
   */
   @Test
   public void shouldReplaceTemplateTwoPartsAttributeFromSystemProperty() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-jndi-name-twoparts-ds.xml");

      String actualJndiName = ds.getDataSource().get(0).getJndiName();
      assertThat(actualJndiName, is("java:/H2DS"));
   }

   /**
   *
   * shouldReplaceTemplateAttributeWitNullIfSystemPropertyNotSet
   *
   * @throws Exception in case of parser error
   */
   @Test
   public void shouldReplaceTemplateAttributeWitNullIfSystemPropertyNotSet() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-jndi-name-wrong-property-ds.xml");

      String actualJndiName = ds.getDataSource().get(0).getJndiName();
      assertThat(actualJndiName, is(""));

   }

   /**
   *
   * shouldReplaceOnlyRightTemplateOnTwoPartsWithOneWrongAttributeIfSystemPropertyNotSet
   *
   * @throws Exception in case of parser error
   */
   @Test
   public void shouldReplaceOnlyRightTemplateOnTwoPartsWithOneWrongAttributeIfSystemPropertyNotSet() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-jndi-name-twoparts-onewrong-ds.xml");

      String actualJndiName = ds.getDataSource().get(0).getJndiName();
      assertThat(actualJndiName, is("H2DS"));

   }

   /**
   *
   * shouldThrowParserExceptionOnWrongSystemPropertyNotSetForNumberValue
   *
   * @throws Exception in case of parser error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowParserExceptionOnWrongSystemPropertyNotSetForNumberValue() throws Exception
   {

      DataSources ds = parseDsFromFile("ds/unit/replace-max-pool-wrong-ds.xml");

   }

}
