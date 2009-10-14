/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package javax.resource.spi.work;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EventObject;

/**
 * This class models the various events that occur during the processing of
 * a <code>Work</code> instance.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public class WorkEvent extends EventObject 
{
   /** Serial version uid */
   private static final long serialVersionUID;

   /** Persistence fields information */
   private static final ObjectStreamField[] serialPersistentFields;
   private static final int TYPE_IDX = 0;
   private static final int WORK_IDX = 1;
   private static final int EXCPEPTION_IDX = 2;
   private static final int DURATION_IDX = 2;

   /**
    * Indicates <code>Work</code> instance has been accepted.
    */
   public static final int WORK_ACCEPTED = 1;
   
   /**
    * Indicates <code>Work</code> instance has been rejected.
    */
   public static final int WORK_REJECTED = 2;
   
   /**
    * Indicates <code>Work</code> instance has started execution.
    */
   public static final int WORK_STARTED = 3;
   
   /**
    * Indicates <code>Work</code> instance has completed execution.
    */
   public static final int WORK_COMPLETED = 4;
   
   /**
    * The event type.
    */
   private int type;
   
   /**
    * The <code>Work</code> object on which the event occured.
    */
   private Work work;
   
   /**
    * The exception that occured during <code>Work</code> processing.
    */
   private WorkException e;
   
   /**
    * The start delay duration (in milliseconds).
    */
   private long startDuration = WorkManager.UNKNOWN;
   

   static
   {
      Boolean legacy = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Boolean run()
         {
            try
            {
               if (System.getProperty("org.jboss.j2ee.LegacySerialization") != null)
                  return Boolean.TRUE;
            }
            catch (Throwable ignored)
            {
               // Ignore
            }
            return Boolean.FALSE;
         }
      });

      if (Boolean.TRUE.equals(legacy))
      {
         serialVersionUID = 6971276136970053051L;
         serialPersistentFields = new ObjectStreamField[] {
            /** @serialField type int */
            new ObjectStreamField("type", int.class),
            /** @serialField work Work */
            new ObjectStreamField("work", Work.class),
            /** @serialField exception WorkException */
            new ObjectStreamField("e", WorkException.class),
            /** @serialField startDuration long */
            new ObjectStreamField("startDuration", long.class)
         };
      }
      else
      {
         serialVersionUID = -3063612635015047218L;
         serialPersistentFields = new ObjectStreamField[] {
            /** @serialField type int */
            new ObjectStreamField("type", int.class),
            /** @serialField work Work */
            new ObjectStreamField("work", Work.class),
            /** @serialField exception WorkException */
            new ObjectStreamField("exception", WorkException.class),
            /** @serialField startDuration long */
            new ObjectStreamField("startDuration", long.class)
         };
      }
   }

   /**
    * Constructor.
    *
    * @param source The object on which the event initially 
    * occurred.
    *
    * @param type The event type.
    *
    * @param work The <code>Work</code> object on which 
    * the event occured.
    *
    * @param exc The exception that occured during 
    * <code>Work</code> processing.
    */
   public WorkEvent(Object source, int type, Work work, WorkException exc) 
   {
      super(source);
      this.type = type;
      this.work =  work;
      this.e = exc;
   }
   
   /**
    * Constructor.
    *
    * @param source The object on which the event initially 
    * occurred.
    *
    * @param type The event type.
    *
    * @param work The <code>Work</code> object on which 
    * the event occured.
    *
    * @param exc The exception that occured during 
    * <code>Work</code> processing.
    *
    * @param startDuration The start delay duration 
    * (in milliseconds).
    */
   public WorkEvent(Object source, int type, Work work, WorkException exc,
                    long startDuration) 
   {
      this(source, type, work, exc);
      this.startDuration = startDuration;
   }
   
   /**
    * Return the type of this event.
    *
    * @return the event type.
    */
   public int getType()
   {
      return this.type; 
   }
   
   /**
    * Return the <code>Work</code> instance which is the cause of the event.
    *
    * @return the <code>Work</code> instance.
    */
   public Work getWork()
   {
      return this.work; 
   }

   /**
    * Return the start interval duration.
    *
    * @return the time elapsed (in milliseconds) since the <code>Work</code>
    * was accepted, until the <code>Work</code> execution started. Note, 
    * this does not offer real-time guarantees. It is valid to return -1, if
    * the actual start interval duration is unknown.
    */
   public long getStartDuration()
   {
      return this.startDuration;
   }

   /**
    * Return the <code>WorkException</code>. The actual 
    * <code>WorkException</code> subtype returned depends on the type of the
    * event.
    *
    * @return a <code>WorkRejectedException</code> or a 
    * <code>WorkCompletedException</code>, if any.
    */
   public WorkException getException()
   {
      return this.e;
   }

   /**
    * Read object
    * @param ois The object input stream
    * @exception ClassNotFoundException If a class can not be found
    * @exception IOException Thrown if an error occurs
    */
   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
   {
      ObjectInputStream.GetField fields = ois.readFields();
      String name = serialPersistentFields[TYPE_IDX].getName();
      this.type = fields.get(name, 0);
      name = serialPersistentFields[WORK_IDX].getName();
      this.work = (Work) fields.get(name, null);
      name = serialPersistentFields[EXCPEPTION_IDX].getName();
      this.e = (WorkException) fields.get(name, null);
      name = serialPersistentFields[DURATION_IDX].getName();
      this.startDuration = fields.get(name, 0L);
   }

   /**
    * Write object
    * @param oos The object output stream
    * @exception IOException Thrown if an error occurs
    */
   private void writeObject(ObjectOutputStream oos) throws IOException
   {
      ObjectOutputStream.PutField fields =  oos.putFields();
      String name = serialPersistentFields[TYPE_IDX].getName();
      fields.put(name, type);
      name = serialPersistentFields[WORK_IDX].getName();
      fields.put(name, work);
      name = serialPersistentFields[EXCPEPTION_IDX].getName();
      fields.put(name, e);
      name = serialPersistentFields[DURATION_IDX].getName();
      fields.put(name, startDuration);
      oos.writeFields();
   }
}
