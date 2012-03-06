/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.DtoTranslationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.XmlSchemaType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Set;

/**
 * @author lazyman
 */
@Entity
@Table(name = "connector")
public class RConnectorType extends RObjectType {

    private static final Trace LOGGER = TraceManager.getTrace(RConnectorType.class);
    private String framework;
    private String connectorType;
    private String connectorVersion;
    private String connectorBundle;
    private Set<String> targetSystemType;
    private String namespace;
    private RObjectReferenceType connectorHostRef;
    private String xmlSchema;

    @ManyToOne
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public RObjectReferenceType getConnectorHostRef() {
        return connectorHostRef;
    }

    @Type(type = "org.hibernate.type.TextType")
    public String getXmlSchema() {
        return xmlSchema;
    }

    public String getConnectorBundle() {
        return connectorBundle;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public String getFramework() {
        return framework;
    }

    public String getNamespace() {
        return namespace;
    }

    @ElementCollection
    @CollectionTable(name = "connector_target_system", joinColumns =
            {@JoinColumn(name = "connectorId")})
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public Set<String> getTargetSystemType() {
        return targetSystemType;
    }

    public void setConnectorBundle(String connectorBundle) {
        this.connectorBundle = connectorBundle;
    }

    public void setConnectorHostRef(RObjectReferenceType connectorHostRef) {
        this.connectorHostRef = connectorHostRef;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public void setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setTargetSystemType(Set<String> targetSystemType) {
        this.targetSystemType = targetSystemType;
    }

    public void setXmlSchema(String xmlSchema) {
        this.xmlSchema = xmlSchema;
    }

    public static void copyToJAXB(RConnectorType repo, ConnectorType jaxb, PrismContext prismContext) throws
            DtoTranslationException {
        RObjectType.copyToJAXB(repo, jaxb, prismContext);

        jaxb.setConnectorBundle(repo.getConnectorBundle());
        jaxb.setConnectorType(repo.getConnectorType());
        jaxb.setConnectorVersion(repo.getConnectorVersion());
        jaxb.setFramework(repo.getFramework());
        jaxb.setNamespace(repo.getNamespace());

        try {
            jaxb.setSchema(RUtil.toJAXB(repo.getXmlSchema(), XmlSchemaType.class, prismContext));

            if (repo.getConnectorHostRef() != null) {
                jaxb.setConnectorHostRef(repo.getConnectorHostRef().toJAXB(prismContext));
            }

            jaxb.getTargetSystemType().addAll(RUtil.safeSetToList(repo.getTargetSystemType()));
        } catch (Exception ex) {
            throw new DtoTranslationException(ex.getMessage(), ex);
        }
    }

    public static void copyFromJAXB(ConnectorType jaxb, RConnectorType repo, PrismContext prismContext) throws
            DtoTranslationException {
        RObjectType.copyFromJAXB(jaxb, repo, prismContext);

        repo.setConnectorBundle(jaxb.getConnectorBundle());
        repo.setConnectorType(jaxb.getConnectorType());
        repo.setConnectorVersion(jaxb.getConnectorVersion());
        repo.setFramework(jaxb.getFramework());
        repo.setNamespace(jaxb.getNamespace());
        repo.setConnectorHostRef(RUtil.jaxbRefToRepo(jaxb.getConnectorHostRef(), jaxb, prismContext));

        if (jaxb.getConnectorHost() != null) {
            LOGGER.warn("Connector host from connector type won't be saved. It should be " +
                    "translated to connector host reference.");
        }

        try {
            repo.setXmlSchema(RUtil.toRepo(jaxb.getSchema(), prismContext));
            repo.setTargetSystemType(RUtil.listToSet(jaxb.getTargetSystemType()));
        } catch (Exception ex) {
            throw new DtoTranslationException(ex.getMessage(), ex);
        }
    }

    @Override
    public ConnectorType toJAXB(PrismContext prismContext) throws DtoTranslationException {
        ConnectorType object = new ConnectorType();
        RConnectorType.copyToJAXB(this, object, prismContext);
        RUtil.revive(object.asPrismObject(), ConnectorType.class, prismContext);
        return object;
    }
}
