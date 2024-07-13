/**
 * Copyright (C) 2024 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.client.core.o11y.trace;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class NameUtils
{
    private static final String DEFAULT = "default";

    public static String name( Class<?> klass, String... names )
    {
        return name( klass.getSimpleName(), names );
    }

    public static String name( String name, String... names )
    {
        StringBuilder builder = new StringBuilder();
        append( builder, name );
        if ( names != null )
        {
            int var4 = names.length;

            for ( String s : names )
            {
                append( builder, s );
            }
        }

        return builder.toString();
    }

    private static void append( StringBuilder builder, String part )
    {
        if ( part != null && !part.isEmpty() )
        {
            if ( builder.length() > 0 )
            {
                builder.append( '.' );
            }

            builder.append( part );
        }

    }

    /**
     * Get default metric name. Experience has shown that we don't need to include all of the package details
     * for each method metric we're gathering. The class names are unique enough to be useful without this.
     * We need to migrate to an easier format:
     * <short-class-name>.<method-or-alias>.<metric-type>
     */
    public static String getDefaultName( Class<?> declaringClass, String method )
    {
        return name( declaringClass.getSimpleName(), method );
    }

    /**
     * Get the metric fullname with no default value.
     * @param nameParts user specified name parts
     */
    public static String getSupername( String nodePrefix, String... nameParts )
    {
        return name( nodePrefix, nameParts );
    }

    /**
     * Get the metric fullname.
     * @param name user specified name
     * @param defaultName 'class name + method name', not null.
     */
    public static String getName( String nodePrefix, String name, String defaultName, String... suffix )
    {
        if ( isBlank( name ) || name.equals( DEFAULT ) )
        {
            name = defaultName;
        }
        return name( name( nodePrefix, name ), suffix );
    }
}
