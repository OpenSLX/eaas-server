/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.09 at 04:38:52 PM CEST 
//


package de.bwl.bwfla.emucomp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for drive complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="drive">
 *   &lt;complexContent>
 *     &lt;extension base="{http://bwfla.bwl.de/common/datatypes}device">
 *       &lt;sequence>
 *         &lt;element name="data" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="iface" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="unit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="cdrom"/>
 *               &lt;enumeration value="disk"/>
 *               &lt;enumeration value="floppy"/>
 *               &lt;enumeration value="iso"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="boot" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="plugged" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "drive", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "data",
    "iface",
    "bus",
    "unit",
    "type",
    "filesystem",
    "boot",
    "plugged",
    "transientDrive"
})
@JsonIgnoreProperties(ignoreUnknown=true)
public class Drive
    extends Device
{

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected String data;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected String iface;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected String bus;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected String unit;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected String filesystem;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected Drive.DriveType type;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
    protected Boolean boot = false;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = false)
    protected boolean plugged = false;

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", defaultValue = "false")
    protected boolean transientDrive;

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setData(String value) {
        this.data = value;
    }

    /**
     * Gets the value of the iface property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIface() {
        return iface;
    }

    /**
     * Sets the value of the iface property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIface(String value) {
        this.iface = value;
    }

    /**
     * Gets the value of the bus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBus() {
        return bus;
    }

    /**
     * Sets the value of the bus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBus(String value) {
        this.bus = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link Drive.DriveType }
     *     
     */
    public Drive.DriveType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link Drive.DriveType }
     *     
     */
    public void setType(Drive.DriveType value) {
        this.type = value;
    }

    /**
     * Gets the value of the boot property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBoot() {
        return boot;
    }

    /**
     * Sets the value of the boot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBoot(Boolean value) {
        this.boot = value;
    }

    /**
     * Gets the value of the plugged property.
     * 
     */
    public boolean isPlugged() {
        return plugged;
    }

    /**
     * Sets the value of the plugged property.
     * 
     */
    public void setPlugged(boolean value) {
        this.plugged = value;
    }

    public String getFilesystem() {
    	return filesystem;
    }
    
    public void setFilesystem(String filesystem) {
    	this.filesystem = filesystem;
    }

    public boolean isTransientDrive() {
        return transientDrive;
    }

    public void setTransientDrive(boolean transientDrive) {
        this.transientDrive = transientDrive;
    }

    /**
     * <p>Java class for null.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * <p>
     * <pre>
     * &lt;simpleType>
     *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *     &lt;enumeration value="cdrom"/>
     *     &lt;enumeration value="disk"/>
     *     &lt;enumeration value="floppy"/>
     *   &lt;/restriction>
     * &lt;/simpleType>
     * </pre>
     * 
     */
    @XmlType(name="")
    @XmlEnum
    public static enum DriveType 
    {
    	@XmlEnumValue("cdrom")
        CDROM("cdrom"),
        
        @XmlEnumValue("disk")
        DISK("disk"),
        
        @XmlEnumValue("floppy")
        FLOPPY("floppy");
        
        private final String value;

        DriveType(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static DriveType fromQID(String qid)
        {
            switch(qid)
            {
                case "Q495265":
                case "Q877050":
                    return CDROM;
                case "Q493576":
                    return FLOPPY;
                default:
                    System.out.println("unknow qid " + qid);
                    return null;
            }
        }

        public String toQID() {
            switch(this) {
                case CDROM:
                    return "Q495265";
                case FLOPPY:
                    return "Q493576";
                default:
                    return null;
            }
        }


    }
}
