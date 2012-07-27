/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.common.api.metadata.ds.v11.DataSource;
import org.jboss.jca.common.api.metadata.ds.v11.DsPool;
import org.jboss.jca.common.api.metadata.ds.v11.DsXaPool;
import org.jboss.jca.common.api.metadata.ds.v11.XaDataSource;
import org.jboss.jca.common.metadata.ds.v11.DsParser;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 * Test case for parsing the allow-multiple-users functionality parameter
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 *
 */
public class DsParserForAllowMultipleUsersTestCase extends DsParserTestBase
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
   }

   /**
    *
    * allow-multiple-users enabled for a datasource
    *
    * @throws Exception in case of parser error
    */
   @Test
   public void shouldHaveAllowMultipleUsersEnabled() throws Exception
   {

      DataSources dses = parseDsFromFile("ds/unit/allow-multiple-users-ds.xml");
      //then
      DataSource ds = (DataSource)dses.getDataSource().get(0);
      DsPool dsPool = ds.getPool();
      boolean actualAllowMultipleUsers = dsPool.isAllowMultipleUsers();
      assertThat(actualAllowMultipleUsers, is(true));

      //then
      XaDataSource xads = (XaDataSource)dses.getXaDataSource().get(0);
      DsXaPool dsXaPool = xads.getXaPool();
      boolean actualXaAllowMultipleUsers = dsXaPool.isAllowMultipleUsers();
      assertThat(actualXaAllowMultipleUsers, is(true));
   }
}
