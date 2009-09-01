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

package javax.resource.cci;

import javax.resource.ResourceException;


/** The RecordFactory interface is used for creating MappedRecord and 
 *  IndexedRecord instances. Note that the RecordFactory is only used 
 *  for creation of generic record instances. A CCI implementation 
 *  provides an implementation class for the RecordFactory interface.
 *
 *  @author   Rahul Sharma
 *  @since    0.8
 *  @see      javax.resource.cci.IndexedRecord
 *  @see      javax.resource.cci.MappedRecord
**/
public interface RecordFactory 
{
  
   /** Creates a MappedRecord. The method takes the name of the record
    *  that is to be created by the RecordFactory. The name of the 
    *  record acts as a pointer to the meta information (stored in 
    *  the metadata repository) for a specific record type.
    *
    *  @param  recordName   Name of the Record
    *  @return MappedRecord
    *  @throws ResourceException  Failed to create a MappedRecord.
    *                             Example error cases are:
    *                              
    *          <UL>
    *             <LI> Invalid specification of record name
    *             <LI> Resource adapter internal error
    *             <LI> Failed to access metadata repository
    *          </UL>
    *  @throws javax.resource.NotSupportedException Operation not supported          
    *                            
    **/
   public MappedRecord createMappedRecord(String recordName) 
      throws ResourceException;

   /** Creates a IndexedRecord. The method takes the name of the record
    *  that is to be created by the RecordFactory. The name of the 
    *  record acts as a pointer to the meta information (stored in 
    *  the metadata repository) for a specific record type.
    *
    *  @param  recordName   Name of the Record
    *  @return IndexedRecord
    *  @throws ResourceException  Failed to create an IndexedRecord.
    *                             Example error cases are:
    *                              
    *          <UL>
    *             <LI> Invalid specification of record name
    *             <LI> Resource adapter internal error
    *             <LI> Failed to access metadata repository
    *          </UL>
    *  @throws javax.resource.NotSupportedException Operation not supported          
    **/
   public IndexedRecord createIndexedRecord(String recordName) 
      throws ResourceException;
}
