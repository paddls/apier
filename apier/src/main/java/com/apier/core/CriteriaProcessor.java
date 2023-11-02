package com.apier.core;

import com.apier.core.criteria.CriteriaBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes({"com.apier.core.Criteria"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class CriteriaProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final TypeElement annotation : annotations) {
            final Set<? extends Element> criterias = roundEnv.getElementsAnnotatedWith(annotation);

            try {
                criterias.stream().filter(ExecutableElement.class::isInstance).map(ExecutableElement.class::cast).forEach(this::generateCriteriaClass);
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }

        return true;
    }


    private void writeClass(final String packageName, final TypeSpec classContent) {
        try {
            final JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(String.format("%s.%s", packageName, classContent.name));
            try (final PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                final JavaFile viewFile = JavaFile.builder(packageName, classContent).build();

                out.print(viewFile);
            }
        } catch (final FilerException exception) {
            exception.printStackTrace(); // catch Attempt to recreate a file
        } catch (final Exception exception) {
            exception.printStackTrace();

            throw new RuntimeException(exception);
        }
    }

    private void generateCriteriaClass(final ExecutableElement api) {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(api);

        writeClass(api.getEnclosingElement().getEnclosingElement().toString(), criteriaBuilder.build());
        // TODO gt, goe, lt, loe, in
        // TODO filter non @ManyToOne, @OneToMany... fields
        // TODO deep link company.user.name
    }
}
