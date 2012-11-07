/**
 * *******************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.translation.langmodels;

import it.unimi.dsi.fastutil.objects.AbstractObject2DoubleMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 *
 * @author John McCrae
 */
@SuppressWarnings("serial")
public class Object2IntAsDoubleMap<E> extends AbstractObject2DoubleMap<E> {

    private final Object2IntMap<E> o;

    public Object2IntAsDoubleMap(Object2IntMap<E> o) {
        this.o = o;
    }

    @Override
    @SuppressWarnings("unchecked")
    public double getDouble(Object key) {
        return o.getInt(    key);
    }

    @Override
    public int size() {
        return o.size();
    }

    @Override
    public ObjectSet<Entry<E>> object2DoubleEntrySet() {
        return new ObjectSetImpl<E>(o.object2IntEntrySet());
    }
    
    private static class ObjectSetImpl<E> extends AbstractObjectSet<Entry<E>> {
        private final ObjectSet<Object2IntMap.Entry<E>> o;
        
        public ObjectSetImpl(ObjectSet<Object2IntMap.Entry<E>> o) {
            this.o = o;
        }
        
        @Override
        public ObjectIterator<Entry<E>> iterator() {
            return new ObjectIteratorImpl<E>(o.iterator());
        }

        @Override
        public int size() {
            return o.size();
        }
        
    }
    
    private static class ObjectIteratorImpl<E> implements ObjectIterator<Entry<E>> {
        private final ObjectIterator<Object2IntMap.Entry<E>> o;

        public ObjectIteratorImpl(ObjectIterator<Object2IntMap.Entry<E>> o) {
            this.o = o;
        }
        
        @Override
        public int skip(int n) {
            return o.skip(n);
        }

        @Override
        public boolean hasNext() {
            return o.hasNext();
        }

        @Override
        public Entry<E> next() {
            final Object2IntMap.Entry<E> e = o.next();
            return new Entry<E>() {

                @Override
                public double setValue(double value) {
                    return e.setValue((int)value);
                }

                @Override
                public double getDoubleValue() {
                    return e.getIntValue();
                }

                @Override
                public E getKey() {
                    return e.getKey();
                }

                @Override
                public Double getValue() {
                    return new Double(e.getIntValue());
                }

                @Override
                public Double setValue(Double v) {
                    return new Double(e.setValue(v.intValue()));
                }
            };
        }

        @Override
        public void remove() {
            o.remove();
        }
        
    }
}
