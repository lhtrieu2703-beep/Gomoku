package game.tools;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.classfile.*;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import static java.lang.constant.ConstantDescs.CD_void;

/** Diagnostic-only entry/normal-return counters; production sources are never instrumented on disk. */
public final class BoardPaintAgent {
    public static final AtomicInteger entries=new AtomicInteger(), returns=new AtomicInteger();
    public static void entered() { entries.incrementAndGet(); }
    public static void returned() { returns.incrementAndGet(); }
    public static void premain(String options,Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader,String name,Class<?> type,ProtectionDomain domain,byte[] bytes) {
                if(!name.equals("game/view/BoardPanel")) return null;
                ClassFile cf=ClassFile.of();ClassDesc tracer=ClassDesc.of("game.tools.BoardPaintAgent");
                return cf.transformClass(cf.parse(bytes),ClassTransform.transformingMethodBodies(
                        method->method.methodName().equalsString("paintComponent")&&method.methodType().equalsString("(Ljava/awt/Graphics;)V"),
                        new CodeTransform() {
                            public void atStart(CodeBuilder builder) { builder.invokestatic(tracer,"entered",MethodTypeDesc.of(CD_void)); }
                            public void accept(CodeBuilder builder,CodeElement element) {
                                if(element instanceof ReturnInstruction) builder.invokestatic(tracer,"returned",MethodTypeDesc.of(CD_void));
                                builder.with(element);
                            }
                        }));
            }
        });
    }
}
