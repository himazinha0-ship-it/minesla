import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.commons.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import javax.tools.ToolProvider;

/** Verify new API descriptors against official Yarn and execute actual entity bridges. */
public final class VerifyFixMethods implements Opcodes {
    static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static String key(String name,String desc){return name+" "+desc;}
    public static void main(String[] args)throws Exception{
        Set<String> mapped=new HashSet<>(),original=new HashSet<>();
        try(var walk=Files.walk(Path.of(args[2]))){for(Path p:walk.filter(p->p.toString().endsWith(".mapping")).toList())for(String line:Files.readAllLines(p)){
            String[] t=line.strip().split("\\s+");if(t.length>=3&&(t[0].equals("METHOD")||t[0].equals("FIELD")))mapped.add(key(t[1],t[t.length-1]));
        }}
        try(ZipFile z=new ZipFile(args[1])){for(ZipEntry e:Collections.list(z.entries()))if(e.getName().endsWith(".class")){
            ClassNode c=new ClassNode();new ClassReader(z.getInputStream(e)).accept(c,0);
            for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions){
                if(n instanceof MethodInsnNode call)original.add(key(call.name,call.desc));
                if(n instanceof FieldInsnNode f)original.add(key(f.name,f.desc));
            }
        }}
        ClassNode entity=null;int references=0;
        try(ZipFile z=new ZipFile(args[0])){for(ZipEntry e:Collections.list(z.entries()))if(e.getName().endsWith(".class")){
            ClassNode c=new ClassNode();new ClassReader(z.getInputStream(e)).accept(c,0);if(c.name.equals("dev/minesla/djinn/DjinnEntity"))entity=c;
            if(!c.name.startsWith("dev/minesla/djinn/runtime/"))continue;
            for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions){
                String owner=null,name=null,desc=null;
                if(n instanceof MethodInsnNode call){owner=call.owner;name=call.name;desc=call.desc;}
                if(n instanceof FieldInsnNode f){owner=f.owner;name=f.name;desc=f.desc;}
                if(owner!=null&&owner.startsWith("net/minecraft/")){
                    require(mapped.contains(key(name,desc))||original.contains(key(name,desc)),"Unknown API descriptor "+owner+"."+name+desc);references++;
                }
            }
        }}
        System.out.println("PASS: "+references+" runtime Minecraft API references match official 1.21.1 Yarn descriptors or unchanged original references.");
        Path dir=Files.createTempDirectory("djinn-bridge-check-");
        // Fixtures only supply inherited state; the bridge bodies are copied verbatim from the final JAR.
        Files.writeString(dir.resolve("BridgeParent.java"),"public class BridgeParent {public int field_6194=10;public net.minecraft.class_2945 field_6011=new net.minecraft.class_2945();public net.minecraft.class_1309 target;public int navigationCalls;public net.minecraft.class_1408 method_5965(net.minecraft.class_1937 w){navigationCalls++;return new net.minecraft.class_1408();}}");
        Path mc=dir.resolve("net/minecraft");Files.createDirectories(mc);
        Files.writeString(mc.resolve("class_1408.java"),"package net.minecraft;public class class_1408 {}");
        Files.writeString(mc.resolve("class_2940.java"),"package net.minecraft;public class class_2940 {}");
        Files.writeString(mc.resolve("class_2945.java"),"package net.minecraft;public class class_2945 {public Object value;public void method_12778(class_2940 key,Object value){this.value=value;}}");
        List<String> javac=new ArrayList<>(List.of("-proc:none","-cp",args[3],"-d",dir.toString(),dir.resolve("BridgeParent.java").toString()));
        try(var walk=Files.walk(mc)){javac.addAll(walk.filter(p->p.toString().endsWith(".java")).map(Path::toString).toList());}
        require(ToolProvider.getSystemJavaCompiler().run(null,null,null,javac.toArray(String[]::new))==0,"Bridge fixtures did not compile");
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(V21,ACC_PUBLIC,"BridgeEntity",null,"BridgeParent",null);
        for(String[] f:new String[][]{{"summoner","Ljava/util/UUID;"},{"summonLife","I"},{"CAST","Lnet/minecraft/class_2940;"}})writer.visitField(ACC_PUBLIC|(f[0].equals("CAST")?ACC_STATIC:0),f[0],f[1],null,null).visitEnd();
        MethodVisitor ctor=writer.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);ctor.visitCode();ctor.visitVarInsn(ALOAD,0);ctor.visitMethodInsn(INVOKESPECIAL,"BridgeParent","<init>","()V",false);ctor.visitInsn(RETURN);ctor.visitMaxs(1,1);ctor.visitEnd();
        MethodVisitor angry=writer.visitMethod(ACC_PUBLIC,"becomeAngry","(Lnet/minecraft/class_1309;)V",null,null);angry.visitCode();angry.visitVarInsn(ALOAD,0);angry.visitVarInsn(ALOAD,1);angry.visitFieldInsn(PUTFIELD,"BridgeParent","target","Lnet/minecraft/class_1309;");angry.visitInsn(RETURN);angry.visitMaxs(2,2);angry.visitEnd();
        Remapper remap=new SimpleRemapper(Map.of(entity.name,"BridgeEntity",entity.superName,"BridgeParent"));
        for(MethodNode m:entity.methods)if(m.name.startsWith("djinns$")||m.name.equals("method_5965"))m.accept(new MethodRemapper(writer.visitMethod(m.access,m.name,m.desc,null,null),remap));
        writer.visitEnd();Files.write(dir.resolve("BridgeEntity.class"),writer.toByteArray());
        try(var loader=new URLClassLoader(new URL[]{dir.toUri().toURL(),Path.of(args[3]).toUri().toURL()},ClassLoader.getPlatformClassLoader())){
            Class<?> type=loader.loadClass("BridgeEntity"),living=loader.loadClass("net.minecraft.class_1309"),world=loader.loadClass("net.minecraft.class_1937");Object mob=type.getConstructor().newInstance(),target=living.getConstructor().newInstance();UUID owner=UUID.randomUUID();
            type.getMethod("djinns$bindSummoner",UUID.class,living).invoke(mob,owner,target);
            require(type.getMethod("djinns$getSummoner").invoke(mob).equals(owner),"Summoner owner not stored");
            require(type.getField("summonLife").getInt(mob)==1200&&type.getField("field_6194").getInt(mob)==0&&type.getField("target").get(mob)==target,"Binding state incorrect");
            type.getMethod("djinns$startRoyalCast").invoke(mob);Object tracker=type.getField("field_6011").get(mob);require(tracker.getClass().getField("value").get(tracker).equals(60),"Cast not synchronized");
            require(type.getMethod("method_5965",world).invoke(mob,world.getConstructor().newInstance())!=null&&type.getField("navigationCalls").getInt(mob)==1,"Ground navigation did not delegate to parent");
        }
        System.out.println("PASS: final-JAR summon bridges and ground navigation executed on JVM; owner, target, lifetime, XP, cast tracker and parent delegation verified with API fixtures.");
    }
}
