package nsu.fit;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class TestAgent {
    private static Instrumentation instrumentation;

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        TestAgent.instrumentation = instrumentation;
        instrumentation.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                        if (className.equals("nsu/fit/Main")) {
                            try {
                                CtClass mainClass = ClassPool.getDefault().getCtClass("nsu.fit.Main");

                                CtField avgCounter = new CtField(CtPrimitiveType.longType, "totalTime", mainClass);
                                avgCounter.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
                                mainClass.addField(avgCounter);

                                CtField minTime = new CtField(CtPrimitiveType.longType, "minTime", mainClass);
                                minTime.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
                                mainClass.addField(minTime);

                                CtField maxTime = new CtField(CtPrimitiveType.longType, "maxTime", mainClass);
                                maxTime.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
                                mainClass.addField(maxTime);

                                CtField startTime = new CtField(CtPrimitiveType.longType, "startTime", mainClass);
                                startTime.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
                                mainClass.addField(startTime);

                                CtField endTime = new CtField(CtPrimitiveType.longType, "endTime", mainClass);
                                endTime.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
                                mainClass.addField(endTime);

                                CtMethod mainMethod = mainClass.getDeclaredMethod("main");
                                mainMethod.insertBefore("totalTime = 0L;");
                                mainMethod.insertBefore("maxTime = 0L;");
                                mainMethod.insertBefore("minTime = 99999L;");

                                mainMethod.insertAfter("{System.out.println(\"Avg time: \" + totalTime / 10); System.out.println(\"Min time: \" + minTime); System.out.println(\"Max time: \" + maxTime);}");
                                mainMethod.insertAfter("nsu.fit.TestAgent.countClasses();");

                                CtMethod transactionMethod = mainClass.getDeclaredMethod("processTransaction");
                                transactionMethod.insertBefore("startTime = System.currentTimeMillis();");
                                transactionMethod.insertBefore("$1 += 99;");

                                transactionMethod.insertAfter("endTime = System.currentTimeMillis();");
                                transactionMethod.insertAfter("{totalTime += endTime - startTime; minTime = Math.min(minTime, endTime - startTime); maxTime = Math.max(maxTime, endTime - startTime);}");

                                return mainClass.toBytecode();
                            } catch (NotFoundException | CannotCompileException | IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }
        );
    }

    public static void agentmain(String agentArgument, Instrumentation instrumentation) {
        premain(agentArgument, instrumentation);
    }

    public static void countClasses() {
        System.out.println("Loaded classes: " + TestAgent.instrumentation.getAllLoadedClasses().length);
    }
}
