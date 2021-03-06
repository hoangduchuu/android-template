package vn.tiki.viewholders.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static vn.tiki.viewholders.compiler.ViewHolderProcessor.DIFF_CALLBACK;
import static vn.tiki.viewholders.compiler.ViewHolderProcessor.ONLY_ADAPTER_BUILDER;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

class NoAdapterFactoryGenerator {

  JavaFile brewJava() {
    return JavaFile.builder("viewholders", createType())
        .addFileComment("Generated code from NoAdapter ViewHolder. Do not modify!")
        .build();
  }

  private MethodSpec createConstructor() {
    return MethodSpec.constructorBuilder()
        .addModifiers(PRIVATE)
        .addStatement("throw new $T()", InstantiationError.class)
        .build();
  }

  private MethodSpec createMakeAdapterMethod() {
    final ClassName typeFactory = ClassName.get("viewholders", TypeFactoryGenerator.NAME);
    final ClassName viewHolderFactory = ClassName.get("viewholders", ViewHolderFactoryGenerator.NAME);
    return MethodSpec.methodBuilder("builder")
        .addModifiers(PUBLIC, STATIC)
        .returns(ONLY_ADAPTER_BUILDER)
        .addCode(CodeBlock.builder()
            .add("return new OnlyAdapter.Builder()\n")
            .add(".typeFactory(new $T())\n", typeFactory)
            .add(".viewHolderFactory(new $T())\n", viewHolderFactory)
            .add(".diffCallback(new $T());\n", DIFF_CALLBACK)
            .build())
        .build();
  }

  private TypeSpec createType() {
    TypeSpec.Builder result = TypeSpec.classBuilder("NoAdapterFactory")
        .addModifiers(PUBLIC, FINAL);
    result.addMethod(createConstructor());
    result.addMethod(createMakeAdapterMethod());
    return result.build();
  }

}
