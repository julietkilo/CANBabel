//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.12.14 at 03:39:03 PM CET
//


package com.github.canbabel.canio.kcd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Signal" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="count" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="4294967295"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "signal"
})
@XmlRootElement(name = "MuxGroup")
public class MuxGroup {

    @XmlElement(name = "Signal", required = true)
    protected List<Signal> signal;
    @XmlAttribute(name = "count", required = true)
    protected long count;

    /**
     * Gets the value of the signal property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signal property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSignal().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Signal }
     *
     *
     */
    public List<Signal> getSignal() {
        if (signal == null) {
            signal = new ArrayList<Signal>();
        }
        return this.signal;
    }

    /**
     * Gets the value of the count property.
     *
     */
    public long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     *
     */
    public void setCount(long value) {
        this.count = value;
    }

}
