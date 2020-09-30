/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.security.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * An implementation of the callback SPI for explicit security settings
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CallbackImpl extends AbstractCallback implements Callback
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, CallbackImpl.class.getName());

   /** Is mapping required */
   private boolean mappingRequired;

   /** The domain */
   private String domain;

   /** The default principal */
   private Principal defaultPrincipal;

   /** The default groups */
   private String[] defaultGroups;

   /** The principal map */
   private Map<String, String> principals;

   /** The groups map */
   private Map<String, String> groups;

   /**
    * Constructor
    */
   CallbackImpl()
   {
   }

   /**
    * Constructor
    * @param mappingRequired Is a mapping required
    * @param domain The domain
    * @param defaultPrincipal The default principal
    * @param defaultGroups The default groups
    * @param principals The principal mappings
    * @param groups The group mappings
    */
   public CallbackImpl(boolean mappingRequired, String domain,
                       String defaultPrincipal, String[] defaultGroups,
                       Map<String, String> principals, Map<String, String> groups)
   {
      this.mappingRequired = mappingRequired;
      this.domain = domain;

      if (defaultPrincipal != null)
      {
         this.defaultPrincipal = new SimplePrincipal(defaultPrincipal);
      }
      else
      {
         this.defaultPrincipal = null;
      }

      if (defaultGroups != null)
      {
         this.defaultGroups = Arrays.copyOf(defaultGroups, defaultGroups.length);
      }
      else
      {
         this.defaultGroups = null;
      }

      if (principals != null)
      {
         this.principals = Collections.synchronizedMap(new HashMap<String, String>(principals));
      }
      else
      {
         this.principals = null;
      }

      if (groups != null)
      {
         this.groups = Collections.synchronizedMap(new HashMap<String, String>(groups));;
      }
      else
      {
         this.groups = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMappingRequired()
   {
      return mappingRequired;
   }

   /**
    * {@inheritDoc}
    */
   public String getDomain()
   {
      return domain;
   }

   /**
    * {@inheritDoc}
    */
   public Principal getDefaultPrincipal()
   {
      return defaultPrincipal;
   }

   /**
    * {@inheritDoc}
    */
   public String[] getDefaultGroups()
   {
      if (defaultGroups == null)
         return null;

      return Arrays.copyOf(defaultGroups, defaultGroups.length);
   }

   /**
    * {@inheritDoc}
    */
   public Principal mapPrincipal(String name)
   {
      if (principals != null)
      {
         String mapping = principals.get(name);

         if (mapping != null)
         {
            return new SimplePrincipal(mapping);
         }
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String mapGroup(String name)
   {
      if (groups == null)
         return null;

      return groups.get(name);
   }

   /**
    * {@inheritDoc}
    */
   public void start() throws Throwable
   {
   }

   /**
    * {@inheritDoc}
    */
   public void stop() throws Throwable
   {
   }

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.writeBoolean(mappingRequired);
      out.writeUTF(domain);

      if (defaultPrincipal != null)
      {
         out.writeBoolean(true);
         out.writeUTF(defaultPrincipal.getName());
      }
      else
      {
         out.writeBoolean(false);
      }

      out.writeObject(defaultGroups);
      
      if (principals != null && principals.size() > 0)
      {
         out.writeInt(principals.size());
         Iterator<Map.Entry<String, String>> it = principals.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
         }
      }
      else
      {
         out.writeInt(0);
      }

      if (groups != null && groups.size() > 0)
      {
         out.writeInt(groups.size());
         Iterator<Map.Entry<String, String>> it = groups.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
         }
      }
      else
      {
         out.writeInt(0);
      }
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      mappingRequired = in.readBoolean();
      domain = in.readUTF();

      if (in.readBoolean())
         defaultPrincipal = new SimplePrincipal(in.readUTF());

      defaultGroups = (String[])in.readObject();

      int i = in.readInt();
      if (i > 0)
      {
         for (int j = 1; j <= i; j++)
         {
            String from = in.readUTF();
            String to = in.readUTF();

            principals.put(from, to);
         }
      }

      i = in.readInt();
      if (i > 0)
      {
         for (int j = 1; j <= i; j++)
         {
            String from = in.readUTF();
            String to = in.readUTF();

            groups.put(from, to);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("CallbackImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[mappingRequired=").append(mappingRequired);
      sb.append(" domain=").append(domain);
      sb.append(" defaultPrincipal=").append(defaultPrincipal);
      sb.append(" defaultGroups=").append(defaultGroups == null ? "null" : Arrays.toString(defaultGroups));
      sb.append(" principals=").append(principals);
      sb.append(" groups=").append(groups);
      sb.append("]");

      return sb.toString();
   }
}
