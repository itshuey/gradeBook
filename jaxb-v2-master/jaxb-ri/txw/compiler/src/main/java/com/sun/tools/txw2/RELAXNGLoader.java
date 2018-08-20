/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.txw2;

import com.sun.tools.txw2.builder.relaxng.SchemaBuilderImpl;
import com.sun.tools.txw2.model.Leaf;
import com.sun.tools.txw2.model.NodeSet;
import com.sun.tools.rngom.ast.util.CheckingSchemaBuilder;
import com.sun.tools.rngom.dt.CascadingDatatypeLibraryFactory;
import com.sun.tools.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import com.sun.tools.rngom.parse.IllegalSchemaException;
import com.sun.tools.rngom.parse.Parseable;
import com.sun.tools.rngdatatype.DatatypeLibrary;
import com.sun.tools.rngdatatype.DatatypeLibraryFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * @author Kohsuke Kawaguchi
 */
class RELAXNGLoader implements SchemaBuilder {
    private final Parseable parseable;

    public RELAXNGLoader(Parseable parseable) {
        this.parseable = parseable;
    }

    public NodeSet build(TxwOptions options) throws IllegalSchemaException {
        SchemaBuilderImpl stage1 = new SchemaBuilderImpl(options.codeModel);
        DatatypeLibraryLoader loader = new DatatypeLibraryLoader(getClass().getClassLoader());
        Leaf pattern = (Leaf)parseable.parse(new CheckingSchemaBuilder(stage1,options.errorListener,
            new CascadingDatatypeLibraryFactory(
                new BuiltinDatatypeLibraryFactory(loader),loader)));

        return new NodeSet(options,pattern);
    }

    /**
     * Copyright (c) 2001, Thai Open Source Software Center Ltd
     * All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are
     * met:
     *
     *     Redistributions of source code must retain the above copyright
     *     notice, this list of conditions and the following disclaimer.
     *
     *     Redistributions in binary form must reproduce the above copyright
     *     notice, this list of conditions and the following disclaimer in
     *     the documentation and/or other materials provided with the
     *     distribution.
     *
     *     Neither the name of the Thai Open Source Software Center Ltd nor
     *     the names of its contributors may be used to endorse or promote
     *     products derived from this software without specific prior written
     *     permission.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
     * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
     * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
     * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
     * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
     * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
     * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
     * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
     * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
     * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */
    private static final class DatatypeLibraryLoader implements DatatypeLibraryFactory {
        private final Service service;

        private DatatypeLibraryLoader(ClassLoader cl) {
            service = new Service(DatatypeLibraryFactory.class,cl);
        }

        public DatatypeLibrary createDatatypeLibrary(String uri) {
            for (Enumeration e = service.getProviders();
                 e.hasMoreElements();) {
                DatatypeLibraryFactory factory
                        = (DatatypeLibraryFactory) e.nextElement();
                DatatypeLibrary library = factory.createDatatypeLibrary(uri);
                if (library != null)
                    return library;
            }
            return null;
        }

        private static class Service {
            private final Class serviceClass;
            private /*final*/ Enumeration configFiles;
            private Enumeration classNames = null;
            private final Vector providers = new Vector();
            private ClassLoader cl;

            private class ProviderEnumeration implements Enumeration {
                private int nextIndex = 0;

                public boolean hasMoreElements() {
                    return nextIndex < providers.size() || moreProviders();
                }

                public Object nextElement() {
                    try {
                        return providers.elementAt(nextIndex++);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new NoSuchElementException();
                    }
                }
            }

            public Service(Class cls, ClassLoader cl) {
                this.cl = cl;
                serviceClass = cls;
                String resName = "META-INF/services/" + serviceClass.getName();
                try {
                    configFiles = cl.getResources(resName);
                } catch (IOException e) {
                    configFiles = new Vector().elements();
                }
            }

            public Enumeration getProviders() {
                return new ProviderEnumeration();
            }

            synchronized private boolean moreProviders() {
                for (; ;) {
                    while (classNames == null) {
                        if (!configFiles.hasMoreElements())
                            return false;
                        classNames = parseConfigFile((URL) configFiles.nextElement());
                    }
                    while (classNames.hasMoreElements()) {
                        String className = (String) classNames.nextElement();
                        try {
                            Class cls = cl.loadClass(className);
                            Object obj = cls.newInstance();
                            if (serviceClass.isInstance(obj)) {
                                providers.addElement(obj);
                                return true;
                            }
                        }
                        catch (ClassNotFoundException e) {
                        }
                        catch (InstantiationException e) {
                        }
                        catch (IllegalAccessException e) {
                        }
                        catch (LinkageError e) {
                        }
                    }
                    classNames = null;
                }
            }

            private static final int START = 0;
            private static final int IN_NAME = 1;
            private static final int IN_COMMENT = 2;

            private static Enumeration parseConfigFile(URL url) {
                try {
                    InputStream in = url.openStream();
                    Reader r;
                    try {
                        r = new InputStreamReader(in, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        r = new InputStreamReader(in, "UTF8");
                    }
                    r = new BufferedReader(r);
                    Vector tokens = new Vector();
                    StringBuffer tokenBuf = new StringBuffer();
                    int state = START;
                    for (; ;) {
                        int n = r.read();
                        if (n < 0)
                            break;
                        char c = (char) n;
                        switch (c) {
                            case '\r':
                            case '\n':
                                state = START;
                                break;
                            case ' ':
                            case '\t':
                                break;
                            case '#':
                                state = IN_COMMENT;
                                break;
                            default:
                                if (state != IN_COMMENT) {
                                    state = IN_NAME;
                                    tokenBuf.append(c);
                                }
                                break;
                        }
                        if (tokenBuf.length() != 0 && state != IN_NAME) {
                            tokens.addElement(tokenBuf.toString());
                            tokenBuf.setLength(0);
                        }
                    }
                    if (tokenBuf.length() != 0)
                        tokens.addElement(tokenBuf.toString());
                    return tokens.elements();
                }
                catch (IOException e) {
                    return null;
                }
            }
        }

    }
}
