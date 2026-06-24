package clojure.storm;

import clojure.asm.*;
import clojure.lang.Keyword;
import clojure.lang.RT;

public class InstCollectingClassVisitor extends ClassWriter {

    public InstCollectingClassVisitor(final int flags) {
        super(flags);
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        Emitter.collectEmitClass(access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(final String file, final String debug) {
        super.visitSource(file, debug);
    }

    @Override
    public ModuleVisitor visitModule(final String name, final int flags, final String version) {
        return super.visitModule(name, flags, version);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String descriptor) {
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(final Attribute attribute) {
        super.visitAttribute(attribute);
    }


    @Override
    public void visitInnerClass(
            final String name, final String outerName, final String innerName, final int access) {        
        super.visitInnerClass(name, outerName, innerName, access);
    }


    @Override
    public FieldVisitor visitField(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final Object value) {
        Emitter.collectEmitField(RT.map(
                Keyword.intern("field", "access"), access,
                Keyword.intern("field", "name"), name,
                Keyword.intern("field", "signature"), signature,
                Keyword.intern("field", "descriptor"), descriptor,
                Keyword.intern("field", "value"), value
        ));

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        Emitter.collectEmitMethod(access, name, descriptor, signature, exceptions);
        return new InstCollectingMethodVisitor(
                super.visitMethod(access, name, descriptor, signature, exceptions));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
    }
