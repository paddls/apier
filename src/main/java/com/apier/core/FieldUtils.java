package com.apier.core;

import com.sun.tools.javac.code.Attribute;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

public class FieldUtils {

    public static Optional<Attribute.Compound> getAnnotation(final VariableElement field, final String annotationName) {
        return field
            .getAnnotationMirrors()
            .stream()
            .filter(Attribute.Compound.class::isInstance)
            .map(Attribute.Compound.class::cast)
            .filter(annotation -> annotation.getAnnotationType().toString().equals(annotationName))
            .findFirst();
    }

    public static List<Attribute.Compound> getAnnotations(
        final VariableElement field,
        final List<String> annotationNames
    ) {
        return field
            .getAnnotationMirrors()
            .stream()
            .filter(Attribute.Compound.class::isInstance)
            .map(Attribute.Compound.class::cast)
            .filter(annotation -> annotationNames.contains(annotation.getAnnotationType().toString()))
            .collect(Collectors.toList());
    }

    public static List<VariableElement> getFields(final DeclaredType resourceType) {
        return resourceType
            .asElement()
            .getEnclosedElements()
            .stream()
            .filter(VariableElement.class::isInstance)
            .map(VariableElement.class::cast)
            .collect(Collectors.toList());
    }

    public static boolean isViewMember(final VariableElement field, final String viewClassName) {
        final Set<String> viewClassNames = GeneratorUtil.getViewClassNames(field);
        final Set<String> listViews = new HashSet<String>() {

            {
                add(String.format("%s.%s", viewClassName, "All"));
                add(String.format("%s.%s", viewClassName, "Read"));
                add(String.format("%s.%s", viewClassName, "List"));
            }
        };

        listViews.retainAll(viewClassNames);

        return !listViews.isEmpty();
    }
}
