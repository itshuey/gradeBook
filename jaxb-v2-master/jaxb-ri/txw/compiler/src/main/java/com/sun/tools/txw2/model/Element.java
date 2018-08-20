/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.txw2.model;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.txw2.NameUtil;
import com.sun.tools.txw2.model.prop.ElementProp;
import com.sun.tools.txw2.model.prop.LeafElementProp;
import com.sun.tools.txw2.model.prop.Prop;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * Element declaration.
 * 
 * @author Kohsuke Kawaguchi
 */
public class Element extends XmlNode {
    /**
     * True if this element can be a root element.
     */
    public boolean isRoot;

    private Strategy strategy;

    public Element(Locator location, QName name, Leaf leaf) {
        super(location, name, leaf);
    }

    /**
     * Returns true if this element should generate an interface.
     */
    private Strategy decideStrategy() {
        if(isRoot)
            return new ToInterface();

        if(hasOneChild() && leaf instanceof Ref && !((Ref)leaf).isInline())
            return new HasOneRef((Ref)leaf);

        Set<Leaf> children = collectChildren();
        for( Leaf l : children ) {
            if( l instanceof XmlNode )
                // if it has attributes/elements in children
                // generate an interface
                return new ToInterface();
        }

        // otherwise this element only has data, so just generate methods for them.
        return new DataOnly();
    }

    void declare(NodeSet nset) {
        strategy = decideStrategy();
        strategy.declare(nset);
    }

    void generate(NodeSet nset) {
        strategy.generate(nset);
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        strategy.generate(clazz,nset,props);
    }


    private JMethod generateMethod(JDefinedClass clazz, NodeSet nset, JType retT) {
        String methodName = NameUtil.toMethodName(name.getLocalPart());

        JMethod m = clazz.method(JMod.PUBLIC, retT, methodName);

        JAnnotationUse a = m.annotate(XmlElement.class);
        if(!methodName.equals(name.getLocalPart()))
            a.param("value",name.getLocalPart());
        if(nset.defaultNamespace==null || !nset.defaultNamespace.equals(name.getNamespaceURI()))
            a.param("ns",name.getNamespaceURI());

        return m;
    }

    public String toString() {
        return "Element "+name;
    }


    interface Strategy {
        void declare(NodeSet nset);
        void generate(NodeSet nset);
        void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props);
    }

    /**
     * Maps to an interface
     */
    private class ToInterface implements Strategy {
        private JDefinedClass clazz;

        public void declare(NodeSet nset) {
            String cname;
            if(alternativeName!=null)
                cname = alternativeName;
            else
                cname = name.getLocalPart();
            clazz = nset.createClass(cname);
            clazz._implements(TypedXmlWriter.class);

            clazz.annotate(XmlElement.class)
                .param("value",name.getLocalPart());
            // TODO: namespace
        }

        public void generate(NodeSet nset) {
            HashSet<Prop> props = new HashSet<Prop>();
            for( Leaf l : Element.this )
                l.generate(clazz,nset, props);
        }

        public void generate(JDefinedClass outer, NodeSet nset, Set<Prop> props) {
            if(props.add(new ElementProp(name,clazz)))
                generateMethod(outer, nset, clazz);
        }
    }

    /**
     * For things like "element foo {refToAnotherPattern}"
     */
    private class HasOneRef implements Strategy {
        private final Ref ref;

        public HasOneRef(Ref ref) {
            this.ref = ref;
        }

        public void declare(NodeSet nset) {
        }
        public void generate(NodeSet nset) {
        }

        public void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
            if(props.add(new ElementProp(name,ref.def.clazz)))
                generateMethod(clazz, nset, ref.def.clazz);
        }
    }

    private class DataOnly implements Strategy {
        public void declare(NodeSet nset) {
        }
        public void generate(NodeSet nset) {
        }

        // TODO: code share with Attribute
        public void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
            Set<JType> types = new HashSet<JType>();

            for( Leaf l : collectChildren() ) {
                if (l instanceof Text) {
                    types.add(((Text)l).getDatatype(nset));
                }
            }

            for( JType t : types ) {
                if(!props.add(new LeafElementProp(name,t)))
                    continue;
                generateMethod(clazz,
                        nset, nset.opts.chainMethod? (JType)clazz : nset.codeModel.VOID
                ).param(t,"value");
            }
        }
    }
}
