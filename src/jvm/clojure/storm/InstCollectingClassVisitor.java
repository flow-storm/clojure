package clojure.storm;

import clojure.asm.*;
import clojure.lang.Keyword;
import clojure.lang.RT;

public class InstCollectingClassVisitor extends ClassWriter {
    private ClassWriter origCw;
    
    public InstCollectingClassVisitor(ClassWriter cw, int flags) {
        super(flags);
        origCw=cw;
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        Emitter.addEmitted(RT.map(
                Keyword.intern("emitted","type"), Keyword.intern(null,"class"),
                Keyword.intern("class","name"), name,
                Keyword.intern("class","access"), OpcodesUtils.accessKeys(access),
                Keyword.intern("class","signature"), signature,
                Keyword.intern("class","super-name"), superName,
                Keyword.intern("class","interfaces"), interfaces));
        origCw.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(final String file, final String debug) {
        origCw.visitSource(file, debug);
    }

    @Override
    public ModuleVisitor visitModule(final String name, final int flags, final String version) {
        return origCw.visitModule(name, flags, version);
    }

    @Override
    public void visitNestHostExperimental(final String nestHost) {
        origCw.visitNestHostExperimental(nestHost);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String descriptor) {
        origCw.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        return origCw.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        return origCw.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(final Attribute attribute) {
        origCw.visitAttribute(attribute);
    }

    @Override
    public void visitNestMemberExperimental(final String nestMember) {
        origCw.visitNestMemberExperimental(nestMember);
    }

    @Override
    public void visitInnerClass(
            final String name, final String outerName, final String innerName, final int access) {        
        origCw.visitInnerClass(name, outerName, innerName, access);
    }


    @Override
    public FieldVisitor visitField(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final Object value) {
        Emitter.addEmitted(RT.map(
                Keyword.intern("emitted","type"), Keyword.intern(null,"field"),
                Keyword.intern("field", "access"), OpcodesUtils.accessKeys(access),
                Keyword.intern("field", "name"), name,
                Keyword.intern("field", "signature"), signature,
                Keyword.intern("field", "descriptor"), descriptor,
                Keyword.intern("field", "value"), value
        ));

        return origCw.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        Emitter.addEmitted(RT.map(
                Keyword.intern("emitted","type"), Keyword.intern(null,"method"),
                Keyword.intern("method","access"), OpcodesUtils.accessKeys(access),
                Keyword.intern("method","name"), name,
                Keyword.intern("method","descriptor"), descriptor,
                Keyword.intern("method","signature"), signature,
                Keyword.intern("method","exceptions"), exceptions
                ));
        return new InstCollectingMethodVisitor(
                origCw.visitMethod(access, name, descriptor, signature, exceptions));
    }

    @Override
    public void visitEnd() {
        origCw.visitEnd();
    }

    @Override
    public byte[] toByteArray() {
        return origCw.toByteArray();
    }

    @Override
    public int newConst(final Object value) {
        return origCw.newConst(value);
    }

    @Override
    public int newUTF8(final String value) {
        return origCw.newUTF8(value);
    }
    
    @Override
    public int newClass(final String value) {
        return origCw.newClass(value);
    }

  @Override
  public int newMethodType(final String methodDescriptor) {
      return origCw.newMethodType(methodDescriptor);
  }

  @Override
  public int newModule(final String moduleName) {
      return origCw.newModule(moduleName);
  }

  @Override
  public int newPackage(final String packageName) {
      return origCw.newPackage(packageName);
  }

  @Override
  public int newHandle(
      final int tag, final String owner, final String name, final String descriptor) {
      return origCw.newHandle(tag, owner, name, descriptor);
  }

  @Override
  public int newHandle(
      final int tag,
      final String owner,
      final String name,
      final String descriptor,
      final boolean isInterface) {
      return origCw.newHandle(tag, owner, name, descriptor, isInterface);
  }

    @Override
  public int newConstantDynamic(
      final String name,
      final String descriptor,
      final Handle bootstrapMethodHandle,
      final Object... bootstrapMethodArguments) {
        return origCw.newConstantDynamic(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
  }

  @Override
  public int newInvokeDynamic(
      final String name,
      final String descriptor,
      final Handle bootstrapMethodHandle,
      final Object... bootstrapMethodArguments) {
    return origCw.newInvokeDynamic(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
  }

  @Override
  public int newField(final String owner, final String name, final String descriptor) {
      return origCw.newField(owner, name, descriptor);
  }

  @Override
  public int newMethod(
      final String owner, final String name, final String descriptor, final boolean isInterface) {
      return origCw.newMethod(owner, name, descriptor, isInterface);
  }

  @Override
  public int newNameType(final String name, final String descriptor) {
      return origCw.newNameType(name, descriptor);
  }



    

    
}
