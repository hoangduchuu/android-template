package vn.tiki.noadapterviewholder.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import vn.tiki.noadapterviewholder.ViewHolder;

@AutoService(Processor.class)
public final class ViewHolderProcessor extends AbstractProcessor {

  static final ClassName VIEW_HOLDER_DELEGATE = ClassName.get(
      "vn.tiki.noadapterviewholder",
      "ViewHolderDelegate");

  static final ClassName VIEW = ClassName.get("android.view", "View");
  static final ClassName VIEW_GROUP = ClassName.get("android.view", "ViewGroup");
  private Filer filer;

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(ViewHolder.class.getCanonicalName());
    return types;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    filer = env.getFiler();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
    final List<ViewHolderInfo> viewHolderInfoList = findAndParseTargets(env);
    if (viewHolderInfoList.isEmpty()) {
      return false;
    }

    for (ViewHolderInfo viewHolderInfo : viewHolderInfoList) {
      try {
        new ViewHolderDelegateGenerator(viewHolderInfo)
            .brewJava()
            .writeTo(filer);

      } catch (IOException e) {
        error("Unable to write delegate for type %s: %s", viewHolderInfo.getTargetType(), e.getMessage());
      }
    }

    try {
      new TypeFactoryGenerator(viewHolderInfoList)
          .brewJava()
          .writeTo(filer);
    } catch (Exception e) {
      error("Unable to write TypeFactory %s", e.getMessage());
    }
    try {
      new ViewHolderFactoryGenerator(viewHolderInfoList)
          .brewJava()
          .writeTo(filer);
    } catch (Exception e) {
      error("Unable to write ViewHolderFactory  %s", e.getMessage());
    }

    return false;
  }

  private void error(String message, Object... args) {
    printMessage(Diagnostic.Kind.ERROR, message, args);
  }

  private List<ViewHolderInfo> findAndParseTargets(RoundEnvironment env) {
    List<ViewHolderInfo> viewHolderInfoList = new LinkedList<>();
    // Process each @BindExtra element.
    for (Element element : env.getElementsAnnotatedWith(ViewHolder.class)) {
      // we don't SuperficialValidation.validateElement(element)
      // so that an unresolved View type can be generated by later processing rounds
      try {
        final ViewHolder annotation = element.getAnnotation(ViewHolder.class);
        final int layout = annotation.layout();
        final int[] onClick = annotation.onClick();
        final ExecutableElement bindMethod = findMethod(element, "bind");
        final List<? extends VariableElement> parameters = bindMethod.getParameters();
        final TypeElement targetType = (TypeElement) processingEnv.getTypeUtils().asElement(parameters.get(0).asType());
        final ClassName itemClassName = ClassName.get(targetType);
        final TypeElement typeElement = (TypeElement) element;
        final ViewHolderInfo viewHolderInfo = new ViewHolderInfo(
            layout,
            onClick,
            itemClassName,
            ClassName.get(typeElement));
        viewHolderInfoList.add(viewHolderInfo);
      } catch (Exception e) {
        logParsingError(ViewHolder.class, e);
      }
    }

    return viewHolderInfoList;
  }

  private ExecutableElement findMethod(Element element, String name) {
    final List<? extends Element> elements = element.getEnclosedElements();
    for (Element e : elements) {
      if (e instanceof ExecutableElement && name.equals(e.getSimpleName().toString())) {
        return (ExecutableElement) e;
      }
    }
    throw new IllegalArgumentException("method " + name + " not found in class " + element);
  }

  private void logParsingError(
      Class<? extends Annotation> annotation,
      Exception e) {
    StringWriter stackTrace = new StringWriter();
    e.printStackTrace(new PrintWriter(stackTrace));
    error("Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
  }

  private void printMessage(Diagnostic.Kind kind, String message, Object[] args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }

    processingEnv.getMessager().printMessage(kind, message);
  }

}