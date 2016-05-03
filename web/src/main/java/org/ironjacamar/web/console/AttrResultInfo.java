/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.web.console;

import java.beans.PropertyEditor;

/** 
 * A simple tuple of an mbean operation name, sigature and result.
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AttrResultInfo
{
   /** The name */
   private String name;

   /** The editor */
   private PropertyEditor editor;

   /** The result */
   private Object result;

   /** The error */
   private Throwable throwable;

   /**
    * Constructor
    * @param name The name
    * @param editor The editor
    * @param result The result
    * @param throwable The error
    */
   public AttrResultInfo(String name, PropertyEditor editor, Object result, Throwable throwable)
   {
      this.name = name;
      this.editor = editor;
      this.result = result;
      this.throwable = throwable;
   }
   
   /**
    * Get the name
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the editor
    * @return The value
    */
   public PropertyEditor getEditor()
   {
      return editor;
   }

   /**
    * Get the result
    * @return The value
    */
   public Object getResult()
   {
      return result;
   }

   /**
    * Get the error
    * @return The value
    */
   public Throwable getError()
   {
      return throwable;
   }

   /**
    * Get the text representation
    * @return The string
    */
   public String getAsText()
   {
      if (throwable != null)
         return throwable.toString();

      if (result != null)
      {
         try 
         {
            if (editor != null)
            {
               editor.setValue(result);
               return editor.getAsText();
            }
            else
            {
               return result.toString();
            }
         }
         catch (Exception e)
         {
            return "String representation of " + name + "unavailable";
         }
      }

      return null;
   }
}
