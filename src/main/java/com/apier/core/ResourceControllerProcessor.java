package com.apier.core;

import com.apier.core.criteria.CriteriaBuilder;
import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.apier.core.ResourceController"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ResourceControllerProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final TypeElement annotation : annotations) {
            final Set<? extends Element> controllers = roundEnv.getElementsAnnotatedWith(annotation);
            final Set<? extends Element> apis = roundEnv.getElementsAnnotatedWith(ResourceApi.class);

            try {
                controllers.forEach(
                    controller -> {
                        final List<Element> controllerApis = apis
                            .stream()
                            .filter(api -> api.getEnclosingElement().toString().equals(controller.toString()))
                            .collect(Collectors.toList());

                        writeControllerClass((TypeElement) controller, controllerApis);
                        writeViewClass((TypeElement) controller);
                    }
                );
            } catch (final Exception exception) {
                exception.printStackTrace();

                throw new RuntimeException(exception);
            }
        }

        return true;
    }

    private void writeViewClass(final TypeElement controller) {
        final String controllerClassName = controller.getSimpleName().toString();
        final String viewClassName = controllerClassName.replace("Controller", "View");
        final String packageName = controller.getEnclosingElement().toString();

        final TypeSpec.Builder viewClass = TypeSpec.classBuilder(viewClassName).addModifiers(Modifier.PUBLIC);

        addView(viewClass, "All");
        addView(viewClass, "Read", String.format("%s.%s.%s", packageName, viewClassName, "All"));
        addView(viewClass, "Write", String.format("%s.%s.%s", packageName, viewClassName, "All"));
        addView(
            viewClass,
            OperationEnum.DETAIL.getJsonView(),
            String.format("%s.%s.%s", packageName, viewClassName, "Read")
        );
        addView(
            viewClass,
            OperationEnum.LIST.getJsonView(),
            String.format("%s.%s.%s", packageName, viewClassName, "Read")
        );
        addView(
            viewClass,
            OperationEnum.CREATE.getJsonView(),
            String.format("%s.%s.%s", packageName, viewClassName, "Write")
        );
        addView(
            viewClass,
            OperationEnum.UPDATE.getJsonView(),
            String.format("%s.%s.%s", packageName, viewClassName, "Write")
        );
        addView(
            viewClass,
            OperationEnum.PATCH.getJsonView(),
            String.format("%s.%s.%s", packageName, viewClassName, "Write")
        );
        addView(
            viewClass,
            OperationEnum.DELETE.getJsonView(),
            String.format("%s.%s.%s", packageName, viewClassName, "Write")
        );

        writeClass(packageName, viewClass.build());
    }

    private void writeClass(final String packageName, final TypeSpec classContent) {
        try {
            final JavaFileObject builderFile = processingEnv
                .getFiler()
                .createSourceFile(String.format("%s.%s", packageName, classContent.name));
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

    private void addView(final TypeSpec.Builder viewClass, final String viewName) {
        addView(viewClass, viewName, null);
    }

    private void addView(final TypeSpec.Builder viewClass, final String viewName, final String parentViewName) {
        final TypeSpec.Builder childView = TypeSpec
            .interfaceBuilder(viewName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        if (parentViewName != null) {
            childView.addSuperinterface(ClassName.bestGuess(parentViewName));
        }

        viewClass.addType(childView.build());
    }

    private Optional<ClassName> getServiceClassName(final ResourceController config) {
        try {
            return Optional.of(config.service()).map(ClassName::get);
        } catch (final MirroredTypeException exception) {
            return Optional
                .of(exception)
                .map(MirroredTypeException::getTypeMirror)
                .map(Object::toString)
                .filter(name -> !name.equals("void"))
                .map(ClassName::bestGuess);
        }
    }

    private void writeControllerClass(final TypeElement controller, final List<Element> apis) {
        final ResourceController config = controller.getAnnotation(ResourceController.class);
        final String controllerName = controller.getSimpleName().toString();
        final String packageName = controller.getEnclosingElement().toString();
        final ClassName serviceClassName = getServiceClassName(config)
            .orElseGet(() -> ClassName.get(packageName, controllerName.replace("Controller", "Service")));
        final String controllerImplName = String.format("%sImpl", controllerName);
        final String path = config.value();

        final TypeSpec.Builder controllerClass = TypeSpec
            .classBuilder(controllerImplName)
            .addModifiers(Modifier.PUBLIC)
            //                        .addSuperinterface(ClassName.get(packageName, controllerClassName)) // @Valid annotation does not support
            .addAnnotation(ClassName.get("lombok", "RequiredArgsConstructor"))
            .addAnnotation(
                AnnotationSpec.builder(ClassName.get("org.springframework.validation.annotation", "Validated")).build()
            )
            .addAnnotation(ClassName.get("org.springframework.web.bind.annotation", "RestController"))
            .addAnnotation(
                AnnotationSpec
                    .builder(ClassName.get("org.springframework.web.bind.annotation", "RequestMapping"))
                    .addMember("value", "$S", path)
                    .build()
            )
            .addField(serviceClassName, "service", Modifier.PRIVATE, Modifier.FINAL);

        apis.forEach(api -> addApi(controllerClass, api));

        writeClass(packageName, controllerClass.build());
    }

    private String normalizeMethodName(final String methodName) {
        return Arrays
            .stream(OperationEnum.values())
            .filter(operation -> methodName.startsWith(operation.getMethodName()))
            .findFirst()
            .map(OperationEnum::getMethodName)
            .orElseThrow(() -> new RuntimeException("Method name not supported: " + methodName));
    }

    private void addApi(final TypeSpec.Builder controllerClass, final Element apiElement) {
        final String methodName = normalizeMethodName(apiElement.getSimpleName().toString());

        switch (methodName) {
            case "findById":
                addMethod(
                    controllerClass,
                    (ExecutableElement) apiElement,
                    OperationEnum.DETAIL,
                    this::addMethod,
                    this::addStatement,
                    this::addJsonView,
                    this::addPermission
                );
                break;
            case "findAll":
                addMethod(
                    controllerClass,
                    (ExecutableElement) apiElement,
                    OperationEnum.LIST,
                    this::addMethod,
                    this::generateCriteriaClass,
                    this::addStatement,
                    this::addJsonView,
                    this::addPermission
                );
                break;
            case "create":
                addMethod(
                    controllerClass,
                    (ExecutableElement) apiElement,
                    OperationEnum.CREATE,
                    this::addMethod,
                    this::addStatement,
                    this::addJsonView,
                    this::addPermission,
                    this::addValidated
                );
                break;
            case "update":
                addMethod(
                    controllerClass,
                    (ExecutableElement) apiElement,
                    OperationEnum.UPDATE,
                    this::addMethod,
                    this::addStatement,
                    this::addJsonView,
                    this::addPermission,
                    this::addValidated
                );
                break;
            case "patch":
                addMethod(
                    controllerClass,
                    (ExecutableElement) apiElement,
                    OperationEnum.PATCH,
                    this::addMethod,
                    this::addStatement,
                    this::addJsonView,
                    this::addPermission,
                    this::addValidated
                );
                break;
            case "delete":
                addMethod(
                    controllerClass,
                    (ExecutableElement) apiElement,
                    OperationEnum.DELETE,
                    this::addMethod,
                    this::addStatement,
                    this::addPermission
                );
                break;
        }
    }

    private MethodSpec.Builder addPermission(
        final MethodSpec.Builder method,
        final ExecutableElement api,
        final OperationEnum operation
    ) {
        final String resourceName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, getResourceName(api));
        final boolean checkRole = api.getAnnotation(ResourceApi.class).checkRole();
        final Optional<Object> preAuthorizeOverride = GeneratorUtil
            .getAnnotationValue(api, "org.springframework.security.access.prepost.PreAuthorize")
            .map(AnnotationValue::getValue);

        final List<String> preAuthorize = new ArrayList<>();

        if (checkRole) {
            preAuthorize.add(String.format("hasAuthority('%s_%s')", operation.getPermission(), resourceName));
        }
        preAuthorizeOverride.ifPresent(o -> preAuthorize.add(o.toString()));

        if (!preAuthorize.isEmpty()) {
            method.addAnnotation(
                AnnotationSpec
                    .builder(ClassName.get("org.springframework.security.access.prepost", "PreAuthorize"))
                    .addMember("value", "$S", String.join(" && ", preAuthorize))
                    .build()
            );
        }

        return method;
    }

    private MethodSpec.Builder addMethod(
        final MethodSpec.Builder method,
        final ExecutableElement api,
        final OperationEnum operation
    ) {
        final String httpMethod = operation.getHttpMethod();
        final String[] consumes = api.getAnnotation(ResourceApi.class).consumes();

        final AnnotationSpec.Builder annotation = AnnotationSpec.builder(
                ClassName.get("org.springframework.web.bind.annotation", String.format("%sMapping", httpMethod))
        );

        if (operation.isIdMethod()) {
            annotation.addMember("value", "$S", "/{id}");
        }
        annotation.addMember("consumes", "$L", Arrays.stream(consumes)
                .map(consume -> CodeBlock.of("$S", consume))
                .collect(CodeBlock.joining(",", "{", "}")));

        return method.addAnnotation(annotation.build());
    }

    private MethodSpec.Builder addJsonView(
        final MethodSpec.Builder method,
        final ExecutableElement api,
        final OperationEnum operation
    ) {
        return method.addAnnotation(getOutputJsonView(api, operation));
    }

    private MethodSpec.Builder generateCriteriaClass(
        final MethodSpec.Builder method,
        final ExecutableElement api,
        final OperationEnum operation
    ) {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(api);

        writeClass(api.getEnclosingElement().getEnclosingElement().toString(), criteriaBuilder.build());

        return method;
        // TODO gt, goe, lt, loe, in
        // TODO filter non @ManyToOne, @OneToMany... fields
        // TODO deep link company.user.name
    }

    private MethodSpec.Builder addValidated(
        final MethodSpec.Builder method,
        final ExecutableElement api,
        final OperationEnum operation
    ) {
        final String packageName = api.getEnclosingElement().getEnclosingElement().toString();
        final String viewClassName = api.getEnclosingElement().getSimpleName().toString().replace("Controller", "View");

        return method.addAnnotation(
            AnnotationSpec
                .builder(ClassName.get("org.springframework.validation.annotation", "Validated"))
                .addMember(
                    "value",
                    "$T.$L.class",
                    ClassName.get(packageName, viewClassName),
                    operation.getInputJsonView()
                )
                .build()
        );
    }

    private MethodSpec.Builder addStatement(
        final MethodSpec.Builder method,
        final ExecutableElement api,
        final OperationEnum operation
    ) {
        final String returnType = api.getReturnType().toString();
        final String methodName = api.getSimpleName().toString();
        final String params = api.getParameters().stream().map(Object::toString).collect(Collectors.joining(", "));

        if (OperationEnum.DETAIL.equals(operation)) {
            method.addStatement(
                    "return service.$L($L).orElseThrow(() -> new $T($T.class))",
                    methodName,
                    params,
                    ClassName.bestGuess("com.apier.core.common.EntityNotFoundException"),
                    ClassName.bestGuess(returnType)
            );
        } else {
            final String statement = returnType.equals("void") ? "service.$L($L)" : "return service.$L($L)";

            method.addStatement(statement, methodName, params);
        }

        return method;
    }

    private AnnotationSpec getOutputJsonView(final ExecutableElement api, final OperationEnum operation) {
        return getJsonView(api, operation.getOutputJsonView());
    }

    private AnnotationSpec getInputJsonView(final ExecutableElement api, final OperationEnum operation) {
        return getJsonView(api, operation.getInputJsonView());
    }

    private AnnotationSpec getJsonView(final ExecutableElement api, final String jsonViewOperation) {
        final String packageName = api.getEnclosingElement().getEnclosingElement().toString();
        final String viewClassName = GeneratorUtil.getViewClassName(api);

        return AnnotationSpec
            .builder(ClassName.get("com.fasterxml.jackson.annotation", "JsonView"))
            .addMember("value", "$T.$L.class", ClassName.get(packageName, viewClassName), jsonViewOperation)
            .build();
    }

    private TypeName getTypeName(final String name) {
        final List<String> names = Arrays.asList(name.split("[<>]"));

        if (names.get(0).equals("void")) {
            return ClassName.VOID;
        } else if (names.size() == 1) {
            return ClassName.bestGuess(names.get(0));
        } else { // TODO @RMA multiple params support
            return ParameterizedTypeName.get(ClassName.bestGuess(names.get(0)), ClassName.bestGuess(names.get(1)));
        }
    }

    private String getResourceType(final String name) {
        final List<String> names = Arrays.asList(name.split("[<>]"));

        return names.get(names.size() - 1);
    }

    private String getResourceName(final ExecutableElement api) {
        return api.getEnclosingElement().getSimpleName().toString().replace("Controller", "");
    }

    private MethodSpec.Builder createMethod(final ExecutableElement api, final OperationEnum operation) {
        final String methodName = api.getSimpleName().toString();
        final String returnType = api.getReturnType().toString();

        return MethodSpec
            .methodBuilder(methodName)
            .returns(getTypeName(returnType))
            .addModifiers(Modifier.PUBLIC)
            .addParameters(
                api
                    .getParameters()
                    .stream()
                    .map(
                        param -> {
                            final ParameterSpec.Builder parameter = ParameterSpec.builder(
                                ClassName.bestGuess(param.asType().toString()),
                                param.toString(),
                                Modifier.FINAL
                            );

                            param
                                .getAnnotationMirrors()
                                .forEach(
                                    annotation -> {
                                        final ClassName annotationType = ClassName.bestGuess(
                                                annotation.getAnnotationType().toString()
                                        );
                                        final AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(annotationType);

                                        annotation.getElementValues().forEach((key, value) -> {
                                            annotationBuilder.addMember(key.getSimpleName().toString(), CodeBlock.of("$L", value));
                                        });

                                        parameter.addAnnotation(annotationBuilder.build());

                                        if (annotationType.simpleName().equals("RequestBody")) {
                                            parameter.addAnnotation(getInputJsonView(api, operation));
                                            parameter.addAnnotation(ClassName.get("javax.validation", "Valid"));
                                        }
                                    }
                                );

                            return parameter.build();
                        }
                    )
                    .collect(Collectors.toList())
            );
    }

    private void addMethod(
        final TypeSpec.Builder controllerClass,
        final ExecutableElement api,
        final OperationEnum operation,
        final MethodBuilder... methodBuilder
    ) {
        final MethodSpec.Builder method = createMethod(api, operation);

        Arrays.stream(methodBuilder).forEach(builder -> builder.build(method, api, operation));

        controllerClass.addMethod(method.build());
    }
}
