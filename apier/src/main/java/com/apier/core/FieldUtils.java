package com.apier.core;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FieldUtils {

    public static Optional<AnnotationMirror> getAnnotation(final VariableElement field, final String annotationName) {
        return field
            .getAnnotationMirrors()
            .stream()
                .map(AnnotationMirror.class::cast)
            .filter(annotation -> annotation.getAnnotationType().toString().equals(annotationName))
            .findFirst();
    }

    public static List<AnnotationMirror> getAnnotations(
        final VariableElement field,
        final List<String> annotationNames
    ) {
        return field
            .getAnnotationMirrors()
            .stream()
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
        final Pattern pattern = Pattern.compile("^.*\\.([a-zA-Z]*\\.[a-zA-Z]*)\\.class$");
        final Set<String> viewClassNames = GeneratorUtil.getViewClassNames(field).stream()
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .collect(Collectors.toSet());
        final Set<String> listViews = new HashSet<>() {
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
