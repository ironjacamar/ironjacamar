/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.embedded.byteman;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>BMRule</code> annotation defines the Byteman rule that should be
 * applied to either the class, or a method
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
public @interface BMRule
{
   /** The name of the rule (RULE) */
   public String name();

   /** Is the targetClass an interface (INTERFACE / CLASS) */
   public boolean isInterface() default false;

   /** The target class name */
   public String targetClass();
   
   /** The target method name */
   public String targetMethod();
   
   /** The target location (AT) */
   public String targetLocation() default "";

   /** The binding (BIND) */
   public String binding() default "";
   
   /** The helper (HELPER) */
   public String helper() default "";

   /** The condition of the rule */
   public String condition() default "TRUE";
  
   /** The action of the rule */
   public String action() default "NOTHING";
}
