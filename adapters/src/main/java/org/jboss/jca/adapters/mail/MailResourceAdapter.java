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

package org.jboss.jca.adapters.mail;

import org.jboss.jca.adapters.mail.inflow.MailActivation;
import org.jboss.jca.adapters.mail.inflow.MailActivationSpec;
import org.jboss.jca.adapters.mail.inflow.NewMsgsWorker;

import java.util.concurrent.ConcurrentHashMap;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * The mail resource adapter
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MailResourceAdapter implements ResourceAdapter
{
   /** The logger */
   private static Logger log = Logger.getLogger(MailResourceAdapter.class);

   /** The bootstrap context */
   private BootstrapContext ctx;

   /** The activations by activation spec */
   private ConcurrentHashMap<MailActivationSpec, MailActivation> activations;

   /** The new message worker */
   private NewMsgsWorker newMsgsWorker;

   /** Queue size */
   private Integer queueSize;

   /**
    * Constructor
    */
   public MailResourceAdapter()
   {
      this.ctx = null;
      this.activations = new ConcurrentHashMap<MailActivationSpec, MailActivation>();
      this.newMsgsWorker = null;
      this.queueSize = Integer.valueOf(1024);
   }

   /**
    * Get the queue size
    * @return The value
    */
   public Integer getQueueSize()
   {
      return queueSize;
   }

   /**
    * Set the queue size
    * @param v The value
    */
   public void setQueueSize(Integer v)
   {
      if (v != null && v.intValue() > 0)
         queueSize = v;
   }

   /**
    * {@inheritDoc}
    */
   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      log.debugf("start");

      this.ctx = ctx;

      WorkManager mgr = ctx.getWorkManager();
      newMsgsWorker = new NewMsgsWorker(mgr, queueSize);

      try
      {
         mgr.scheduleWork(newMsgsWorker);
      }
      catch (WorkException e)
      {
         throw new ResourceAdapterInternalException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
      log.debugf("stop");

      newMsgsWorker.release();
   }

   /**
    * {@inheritDoc}
    */
   public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
      throws ResourceException
   {
      log.debugf("endpointActivation: endpointFactory=%s,spec=%s", endpointFactory, spec);

      if (spec == null)
         throw new NotSupportedException("Null MailActivationSpec instance");

      if (!(spec instanceof MailActivationSpec))
         throw new NotSupportedException("Not a MailActivationSpec instance" + spec.getClass().getName());

      MailActivationSpec mailSpec = (MailActivationSpec)spec;
      MailActivation activation = new MailActivation(endpointFactory, mailSpec);

      try
      {
         newMsgsWorker.watch(activation);
         activations.put(mailSpec, activation);
      }
      catch (InterruptedException e)
      {
         throw new ResourceException("Failed to schedule new msg check", e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
   {
      log.debugf("endpointDeactivation: endpointFactory=%s,spec=%s", endpointFactory, spec);

      if (spec != null && spec instanceof MailActivationSpec)
      {
         MailActivation activation = activations.remove(spec);

         if (activation != null)
            activation.release();
      }
   }

   /**
    * {@inheritDoc}
    */
   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      return new XAResource[0];
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      return super.equals(other);
   }
}
