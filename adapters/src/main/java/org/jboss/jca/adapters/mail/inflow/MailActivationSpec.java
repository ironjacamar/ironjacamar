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

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/** 
 * The encapsulation of the mail folder endpoint specification
 * 
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MailActivationSpec implements ActivationSpec, Serializable
{
   /** @since 1.0 */
   private static final long serialVersionUID = -3034364895936568423L;

   /** The resource adapter */
   private transient ResourceAdapter ra;

   /** The mail server hostname/address */
   private String mailServer = "mailhost";

   /** The mail store protocol */
   private String storeProtocol = "imap";

   /** The mail folder name */
   private String mailFolder;

   /** The message selector */
   private String messageSelector;

   /** The mail store user */
   private String userName;

   /** The mail store password */
   private String password;

   /** The new messages check delay in MS */
   private Long pollingInterval = Long.valueOf(60000L); 

   /** The maximum number of messages */
   private Integer maxMessages = Integer.valueOf(1);

   /** Enable JavaMail debugging*/
   private Boolean debug;

   /** Flush - for pop3 flush the mailbox after checking */
   private Boolean flush = Boolean.TRUE;

   /** Starttls - ssl */
   private Boolean starttls;

   /** The port */
   private Integer port;

   /**
    * Constructor
    */
   public MailActivationSpec()
   {
   }

   /**
    * Get the mail server
    * @return The value
    */
   public String getMailServer()
   {
      return mailServer;
   }

   /**
    * Set the mail server
    * @param mailServer The value
    */
   public void setMailServer(String mailServer)
   {
      this.mailServer = mailServer;
   }

   /**
    * Get the store protocol
    * @return The value
    */
   public String getStoreProtocol()
   {
      return storeProtocol;
   }

   /**
    * Set the store protocol
    * @param storeProtocol The value
    */
   public void setStoreProtocol(String storeProtocol)
   {
      this.storeProtocol = storeProtocol;
   }

   /**
    * Get the mail folder
    * @return The value
    */
   public String getMailFolder()
   {
      return mailFolder;
   }

   /**
    * Set the mail folder
    * @param mailFolder The value
    */
   public void setMailFolder(String mailFolder)
   {
      this.mailFolder = mailFolder;
   }

   /**
    * Get the message selector
    * @return The value
    */
   public String getMessageSelector()
   {
      return messageSelector;
   }

   /**
    * Set the message selector
    * @param messageSelector The value
    */
   public void setMessageSelector(String messageSelector)
   {
      this.messageSelector = messageSelector;
   }

   /**
    * Get the user name
    * @return The value
    */
   public String getUserName()
   {
      return userName;
   }

   /**
    * Set the user name
    * @param userName The value
    */
   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   /**
    * Get the password
    * @return The value
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * Set the password
    * @param password The value
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * Get the polling interval
    * @return The value
    */
   public Long getPollingInterval()
   {
      return pollingInterval;
   }

   /**
    * Set the polling interval
    * @param pollingInterval The value
    */
   public void setPollingInterval(Long pollingInterval)
   {
      this.pollingInterval = pollingInterval;
   }

   /**
    * Get the max messages
    * @return The value
    */
   public Integer getMaxMessages()
   {
      return maxMessages;
   }

   /**
    * Set the max messages
    * @param maxMessages The value
    */
   public void setMaxMessages(Integer maxMessages)
   {
      this.maxMessages = maxMessages;
   }
   
   /**
    * Get the max messages
    * @return The value
    */
   public Boolean isDebug() 
   {
      return debug;
   }
   
   /**
    * Set the debug
    * @param debug The value
    */
   public void setDebug(Boolean debug) 
   {
      this.debug = debug;
   }
   
   /**
    * Get the port
    * @return The value
    */
   public Integer getPort() 
   {
      return port;
   }
   
   /**
    * Set the port
    * @param port The value
    */
   public void setPort(Integer port) 
   {
      this.port = port;
   }
   
   /**
    * Get the starttls
    * @return The value
    */
   public Boolean isStarttls() 
   {
      return starttls;
   }

   /**
    * Set the starttls
    * @param starttls The value
    */
   public void setStarttls(Boolean starttls) 
   {
      this.starttls = starttls;
   }
   
   /**
    * Get the flush
    * @return The value
    */
   public Boolean isFlush() 
   {
      return flush;
   }
   
   /**
    * Set the flush
    * @param flush The value
    */
   public void setFlush(Boolean flush) 
   {
      this.flush = flush;
   }
   
   /**
    * Get the resource adapter
    * @return The value
    */
   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   /**
    * Set the resource adapter
    * @param ra The value
    * @exception ResourceException Thrown if an error occurs
    */
   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      this.ra = ra;
   }

   /**
    * Validate
    * @exception InvalidPropertyException Thrown if an error occurs
    */
   public void validate() throws InvalidPropertyException
   {
   }

   /**
    * String representation
    * @return The value
    */
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("MailActivationSpec(");
      tmp.append("mailServer=");
      tmp.append(mailServer);
      tmp.append(", storeProtocol=");
      tmp.append(storeProtocol);
      tmp.append(", mailFolder=");
      tmp.append(mailFolder);
      tmp.append(", pollingInterval=");
      tmp.append(pollingInterval);
      tmp.append(", messageSelector=");
      tmp.append(messageSelector);
      tmp.append(", userName=");
      tmp.append(userName);
      tmp.append(", maxMessages=");
      tmp.append(maxMessages);
      tmp.append(", debug=");
      tmp.append(debug);
      tmp.append(", starttls=");
      tmp.append(starttls);
      tmp.append(", port=");
      tmp.append(port);
      tmp.append(")");
      return tmp.toString();
   }
}
