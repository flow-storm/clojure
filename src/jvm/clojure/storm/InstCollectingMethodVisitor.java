package clojure.storm;

import clojure.asm.*;
import clojure.lang.Compiler;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.storm.OpcodesUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InstCollectingMethodVisitor extends MethodVisitor {

    public static 
    MethodVisitor orig;

    public InstCollectingMethodVisitor(MethodVisitor orig) {
        super(Opcodes.ASM4);
        this.orig = orig;
        }

    @Override
    public void visitParameter(final String name, final int access) {
        orig.visitParameter(name,access);
        }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return orig.visitAnnotationDefault();
        }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        return orig.visitAnnotation(descriptor, visible);
        }
        
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        return orig.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }


    @Override
    public void visitAnnotableParameterCount(final int parameterCount, final boolean visible) {
        orig.visitAnnotableParameterCount(parameterCount, visible);
        }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
        return orig.visitParameterAnnotation(parameter, descriptor, visible);
        }

    @Override
    public void visitAttribute(final Attribute attribute) {
        orig.visitAttribute(attribute);
        }

    @Override
    public void visitCode() {
        orig.visitCode();
        }

    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
        orig.visitFrame(type, nLocal, local, nStack, stack);
        }

    @Override
    public void visitInsn(final int opcode) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), OpcodesUtils.opCodeKey(opcode),
                Keyword.intern("instruction", "kind"), Keyword.intern(null, "inst")
        ));
        orig.visitInsn(opcode);
        }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        orig.visitIntInsn(opcode, operand);
        }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), OpcodesUtils.opCodeKey(opcode),
                Keyword.intern("instruction", "kind"), Keyword.intern(null, "var"),
                Keyword.intern(null, "var"), var
        ));
        orig.visitVarInsn(opcode, var);
        }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        orig.visitTypeInsn(opcode, type);
        }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), OpcodesUtils.opCodeKey(opcode),
                Keyword.intern("instruction", "kind"), Keyword.intern(null, "field"),
                Keyword.intern(null, "owner"), owner,
                Keyword.intern(null, "name"), name,
                Keyword.intern(null, "descriptor"), descriptor
        ));
        orig.visitFieldInsn(opcode, owner, name, descriptor);
        }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor) {
        // Deprecated
        orig.visitMethodInsn(opcode, owner, name, descriptor);       
        }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), OpcodesUtils.opCodeKey(opcode),
                Keyword.intern("instruction", "kind"), Keyword.intern(null, "method"),
                Keyword.intern(null, "owner"), owner,
                Keyword.intern(null, "name"), name,
                Keyword.intern(null, "descriptor"), descriptor,
                Keyword.intern(null, "interface?"), isInterface
        ));
        orig.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

    @Override
    public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), Keyword.intern(null, "invoke-dynamic"),
                Keyword.intern(null, "descriptor"), descriptor
        ));
        orig.visitInvokeDynamicInsn(name,descriptor,bootstrapMethodHandle,bootstrapMethodArguments);
        }


    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), OpcodesUtils.opCodeKey(opcode),
                Keyword.intern("instruction", "kind"), Keyword.intern(null, "jump"),
                Keyword.intern(null, "label"), label.toString()
        ));
        orig.visitJumpInsn(opcode, label);
        }

    @Override
    public void visitLabel(final Label label) {
        Emitter.collectEmitLabel(label.toString());
        orig.visitLabel(label);
        }


    @Override
    public void visitLdcInsn(final Object value) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), Keyword.intern(null, "ldc"),
                Keyword.intern(null, "value"), value
        ));
        orig.visitLdcInsn(value);
        }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), Keyword.intern(null, "iinc"),
                Keyword.intern(null, "var"), var,
                Keyword.intern(null, "increment"), increment
        ));
        orig.visitIincInsn(var,increment);
        }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), Keyword.intern(null, "table-switch"),
                Keyword.intern(null, "min"), min,
                Keyword.intern(null, "max"), max,
                Keyword.intern(null, "default-label"), dflt,
                Keyword.intern(null, "labels"), Arrays.stream(labels).map(Object::toString).collect(Collectors.toList())
        ));
        orig.visitTableSwitchInsn(min,max,dflt,labels);
        }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), Keyword.intern(null, "lookup-switch"),
                Keyword.intern(null, "default-label"), dflt,
                Keyword.intern(null, "keys"), keys,
                Keyword.intern(null, "labels"), Arrays.stream(labels).map(Object::toString).collect(Collectors.toList())
        ));
        orig.visitLookupSwitchInsn(dflt, keys, labels);
        }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
        orig.visitMultiANewArrayInsn(descriptor,numDimensions);
        }

    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        return orig.visitInsnAnnotation(typeRef,typePath,descriptor,visible);
        }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        Emitter.collectEmitInst(RT.map(
                Keyword.intern("instruction", "op"), Keyword.intern(null, "try-catch-block"),
                Keyword.intern(null, "start-label"), start.toString(),
                Keyword.intern(null, "end-label"), end.toString(),
                Keyword.intern(null, "handler-label"), handler.toString(),
                Keyword.intern(null, "ex-type"), type
        ));
        orig.visitTryCatchBlock(start,end,handler,type);
        }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        return orig.visitTryCatchAnnotation(typeRef,typePath,descriptor,visible);
        }

    @Override
    public void visitLocalVariable(final String name, final String descriptor, final String signature, final Label start, final Label end, final int index) {
        Emitter.collectEmitVar(RT.map(
                Keyword.intern(null, "name"), name,
                Keyword.intern(null, "descriptor"), descriptor,
                Keyword.intern(null, "signature"), signature,
                Keyword.intern(null, "start-label"), start.toString(),
                Keyword.intern(null, "end-label"), end.toString(),
                Keyword.intern(null, "index"), index

        ));
        orig.visitLocalVariable(name,descriptor,signature,start,end,index);
        }

    
    @Override
    public void visitLineNumber(final int line, final Label start) {
        orig.visitLineNumber(line,start);
        }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        orig.visitMaxs(maxStack,maxLocals);
        }

    @Override
    public void visitEnd() {
        orig.visitEnd();
        }
    
    }
