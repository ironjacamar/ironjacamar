/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.adapters.mail.inflow;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

/**
 * An IMAP mail folder
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class IMAPMailFolder extends MailFolder
{
   /**
    * Constructor
    * @param spec The mail activation spec
    */
   public IMAPMailFolder(MailActivationSpec spec)
   {
      super(spec);
   }

   /**
    * Get the messages from a folder
    * @param folder The folder
    * @return The messages
    * @exception MessagingException Thrown if there is an error
    */
   protected Message[] getMessages(Folder folder) throws MessagingException
   {
      Message[] result = folder.search(new SearchTerm() {
         private static final long serialVersionUID = 1L;

         @Override
         public boolean match(Message msg)
         {
            try
            {
               return !msg.isSet(Flag.SEEN);
            }
            catch (MessagingException e)
            {
               return false;
            }
         }
      });

      if (result != null && result.length > 0)
         return result;

      return new Message[0];
   }
   
   /**
    * {@inheritDoc}
    */
   protected Store openStore(Session session) throws NoSuchProviderException
   {
      return session.getStore("imap");
   }

   /**
    * {@inheritDoc}
    */
   protected void markMessageSeen(Message message) throws MessagingException
   {
      message.setFlag(Flag.SEEN, true);
   }

   /**
    * {@inheritDoc}
    */
   protected void closeStore(boolean success, Store store, Folder folder) throws MessagingException
   {
      try
      {
         if (folder != null && folder.isOpen())
         {
            folder.close(success);
         }
      }
      finally
      {
         if (store != null && store.isConnected())
         {
            store.close();
         }
      }
   }
}
