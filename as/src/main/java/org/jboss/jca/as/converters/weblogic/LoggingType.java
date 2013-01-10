/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.as.converters.weblogic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Java class for loggingType complex type.
 * 
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loggingType", propOrder =
      { "logFilename", "loggingEnabled", "rotationType", "numberOfFilesLimited", "fileCount", "fileSizeLimit",
      "rotateLogOnStartup", "logFileRotationDir", "rotationTime", "fileTimeSpan", "dateFormatPattern" })
public class LoggingType
{

   @XmlElement(name = "log-filename")
   private String logFilename;

   @XmlElement(name = "logging-enabled")
   private Boolean loggingEnabled;

   @XmlElement(name = "rotation-type")
   private String rotationType;

   @XmlElement(name = "number-of-files-limited")
   private Boolean numberOfFilesLimited;

   @XmlElement(name = "file-count")
   private Integer fileCount;

   @XmlElement(name = "file-size-limit")
   private Integer fileSizeLimit;

   @XmlElement(name = "rotate-log-on-startup")
   private Boolean rotateLogOnStartup;

   @XmlElement(name = "log-file-rotation-dir")
   private String logFileRotationDir;

   @XmlElement(name = "rotation-time")
   private String rotationTime;

   @XmlElement(name = "file-time-span")
   private Integer fileTimeSpan;

   @XmlElement(name = "date-format-pattern")
   private String dateFormatPattern;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private java.lang.String id;

   /**
    * Gets the value of the logFilename property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getLogFilename()
   {
      return logFilename;
   }

   /**
    * Sets the value of the logFilename property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setLogFilename(String value)
   {
      this.logFilename = value;
   }

   /**
    * Gets the value of the loggingEnabled property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getLoggingEnabled()
   {
      return loggingEnabled;
   }

   /**
    * Sets the value of the loggingEnabled property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setLoggingEnabled(Boolean value)
   {
      this.loggingEnabled = value;
   }

   /**
    * Gets the value of the rotationType property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getRotationType()
   {
      return rotationType;
   }

   /**
    * Sets the value of the rotationType property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setRotationType(String value)
   {
      this.rotationType = value;
   }

   /**
    * Gets the value of the numberOfFilesLimited property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getNumberOfFilesLimited()
   {
      return numberOfFilesLimited;
   }

   /**
    * Sets the value of the numberOfFilesLimited property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setNumberOfFilesLimited(Boolean value)
   {
      this.numberOfFilesLimited = value;
   }

   /**
    * Gets the value of the fileCount property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getFileCount()
   {
      return fileCount;
   }

   /**
    * Sets the value of the fileCount property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setFileCount(Integer value)
   {
      this.fileCount = value;
   }

   /**
    * Gets the value of the fileSizeLimit property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getFileSizeLimit()
   {
      return fileSizeLimit;
   }

   /**
    * Sets the value of the fileSizeLimit property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setFileSizeLimit(Integer value)
   {
      this.fileSizeLimit = value;
   }

   /**
    * Gets the value of the rotateLogOnStartup property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getRotateLogOnStartup()
   {
      return rotateLogOnStartup;
   }

   /**
    * Sets the value of the rotateLogOnStartup property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setRotateLogOnStartup(Boolean value)
   {
      this.rotateLogOnStartup = value;
   }

   /**
    * Gets the value of the logFileRotationDir property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getLogFileRotationDir()
   {
      return logFileRotationDir;
   }

   /**
    * Sets the value of the logFileRotationDir property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setLogFileRotationDir(String value)
   {
      this.logFileRotationDir = value;
   }

   /**
    * Gets the value of the rotationTime property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getRotationTime()
   {
      return rotationTime;
   }

   /**
    * Sets the value of the rotationTime property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setRotationTime(String value)
   {
      this.rotationTime = value;
   }

   /**
    * Gets the value of the fileTimeSpan property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getFileTimeSpan()
   {
      return fileTimeSpan;
   }

   /**
    * Sets the value of the fileTimeSpan property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setFileTimeSpan(Integer value)
   {
      this.fileTimeSpan = value;
   }

   /**
    * Gets the value of the dateFormatPattern property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getDateFormatPattern()
   {
      return dateFormatPattern;
   }

   /**
    * Sets the value of the dateFormatPattern property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setDateFormatPattern(String value)
   {
      this.dateFormatPattern = value;
   }

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link java.lang.String }
    *     
    */
   public java.lang.String getId()
   {
      return id;
   }

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link java.lang.String }
    *     
    */
   public void setId(java.lang.String value)
   {
      this.id = value;
   }

}
