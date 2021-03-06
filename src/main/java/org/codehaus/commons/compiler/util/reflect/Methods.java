
/*
 * Janino - An embedded Java[TM] compiler
 *
 * Copyright (c) 2019 Arno Unkrig. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.codehaus.commons.compiler.util.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.commons.nullanalysis.Nullable;

/**
 * Utility methods related to {@link Method}.
 */
public final
class Methods {

    private Methods() {}

    /**
     * A wrapper for {@link Method#invoke(Object, Object...) <var>method</var>.invoke(<var>obj</var>,
     * <var>args</var>)} that catches any exception, wraps it in an {@link AssertionError}, and throws that.
     *
     * @throws T                  The method threw that exception
     * @throws ClassCastException The method threw an unchecked exception that is <em>not</em> a subclass of
     *                            <var>EX</var>
     * @throws ClassCastException The method returned a value that is <em>not</em> a subclass of <var>R</var>
     */
    public static <R, EX extends Throwable> R
    invoke(Method method, @Nullable Object obj, Object... args) throws EX {
        try {
            @SuppressWarnings("unchecked") R returnValue = (R) method.invoke(obj, args);
            return returnValue;
        } catch (InvocationTargetException ite) {
            @SuppressWarnings("unchecked") EX targetException = (EX) ite.getTargetException();
            throw targetException;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
