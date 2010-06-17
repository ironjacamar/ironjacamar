/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.embedded.arquillian;

import java.util.Properties;

import javax.naming.InitialContext;

import org.jboss.arquillian.testenricher.resource.ResourceInjectionEnricher;

/**
 * {@link TestEnricher} implementation specific to the EmbeddedJCA container
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class EmbeddedJCATestEnricher extends ResourceInjectionEnricher
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.testenricher.resource.ResourceInjectionEnricher#lookup(java.lang.String)
    */
   protected Object lookup(String jndiName) throws Exception 
   {
      return createContext().lookup(jndiName);
   }

   /**
    * Create a context
    * @return The context
    * @exception Exception Thrown if an error occurs
    */
   protected InitialContext createContext() throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      properties.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return new InitialContext(properties);
   }
}
