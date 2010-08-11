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

import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.metadata.ds.DsParser;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 * A RaParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class DsParserTestCase
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
               .getResource("ds/db2-ds.xml").toURI());
         is = new FileInputStream(xmlFile);
         DsParser parser = new DsParser();
         //when
         DataSources ds = parser.parse(is);
         //then
         assertThat(ds.getDatasource().size(), is(1));


      }
      finally
      {
         if (is != null)
            is.close();
      }
   }


}
