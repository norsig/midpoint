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

import com.evolveum.midpoint.repo.sql.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.jaxb.XExtension;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ExtensibleObjectType;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author lazyman
 */
@Entity
@Table(name = "extensible_object")
public class RExtensibleObjectType extends RObjectType {

    private RExtension extension;

    @ManyToOne
    @JoinColumn
    public RExtension getExtension() {
        return extension;
    }

    public void setExtension(RExtension extension) {
        this.extension = extension;
    }

    public static void copyToJAXB(RExtensibleObjectType repo, ExtensibleObjectType jaxb) throws
            DtoTranslationException {
        RObjectType.copyToJAXB(repo, jaxb);

        if (repo.getExtension() != null) {
            XExtension extension = new XExtension();
            jaxb.setExtension(extension);

            RExtension.copyToJAXB(repo.getExtension(), extension);
        }
    }

    public static void copyFromJAXB(ExtensibleObjectType jaxb, RExtensibleObjectType repo) throws
            DtoTranslationException {
        RObjectType.copyFromJAXB(jaxb, repo);

        if (jaxb.getExtension() != null) {
            RExtension extension = new RExtension();
            repo.setExtension(extension);

            RExtension.copyFromJAXB(jaxb.getExtension(), extension);
        }
    }

    @Override
    public ExtensibleObjectType toJAXB() throws DtoTranslationException {
        ExtensibleObjectType object = new ExtensibleObjectType();
        RExtensibleObjectType.copyToJAXB(this, object);
        return object;
    }
}
