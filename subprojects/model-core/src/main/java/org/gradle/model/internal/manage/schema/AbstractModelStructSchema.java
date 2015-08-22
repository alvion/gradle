/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.internal.manage.schema;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import org.gradle.internal.Cast;
import org.gradle.model.internal.manage.schema.extract.ModelSchemaAspect;
import org.gradle.model.internal.type.ModelType;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractModelStructSchema<T> extends AbstractModelSchema<T> implements ModelStructSchema<T> {
    private final ImmutableSortedMap<String, ModelProperty<?>> properties;
    private final Map<Class<? extends ModelSchemaAspect>, ModelSchemaAspect> aspects;

    public AbstractModelStructSchema(ModelType<T> type, Iterable<ModelProperty<?>> properties, Iterable<ModelSchemaAspect> aspects) {
        super(type);
        ImmutableSortedMap.Builder<String, ModelProperty<?>> builder = ImmutableSortedMap.naturalOrder();
        for (ModelProperty<?> property : properties) {
            if (!builder.containsKey(property.getName())) {
                builder.put(property.getName(), property);
            } else {
                ModelProperty<?> modelProperty = builder.get(property.getName());
                if (modelProperty.getType().getRawClass()!=boolean.class) {
                    throw new IllegalStateException("Duplicate property "+property.getName()+ " found in model. Only boolean properties may have two getters.");
                }
            }
        }
        this.properties = builder.build();
        this.aspects = Maps.uniqueIndex(aspects, new Function<ModelSchemaAspect, Class<? extends ModelSchemaAspect>>() {
            @Override
            public Class<? extends ModelSchemaAspect> apply(ModelSchemaAspect aspect) {
                return aspect.getClass();
            }
        });
    }

    @Override
    public Collection<ModelProperty<?>> getProperties() {
        return properties.values();
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public ModelProperty<?> getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public boolean hasAspect(Class<? extends ModelSchemaAspect> aspectType) {
        return aspects.containsKey(aspectType);
    }

    @Override
    public <A extends ModelSchemaAspect> A getAspect(Class<A> aspectType) {
        return Cast.uncheckedCast(aspects.get(aspectType));
    }

    @Override
    public Collection<ModelSchemaAspect> getAspects() {
        return aspects.values();
    }
}
