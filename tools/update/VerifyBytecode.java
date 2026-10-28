import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.util.*;
import java.util.zip.*;
import java.nio.file.*;
import java.net.*;
import javax.tools.ToolProvider;

public final class VerifyBytecode {
    public static void main(String[] args)throws Exception{
        int classes=0,methods=0;
        try(ZipFile z=new ZipFile(args[0])){
            for(ZipEntry e:Collections.list(z.entries()))if(e.getName().endsWith(".class")){
                ClassNode c=new ClassNode();new ClassReader(z.getInputStream(e)).accept(c,0);
                for(MethodNode m:c.methods)if((m.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0){
                    new Analyzer<>(new BasicVerifier()).analyze(c.name,m);methods++;
                }
                classes++;
            }
        }
        System.out.println("PASS: bytecode stack/data-flow verification for "+classes+" classes and "+methods+" methods.");
        // Execute the exact new swing method on the JVM with a narrow parent fixture.
        Path test=Files.createTempDirectory("djinn-swing-check-");
        Path parent=test.resolve("SwingParent.java");
        Files.writeString(parent,"public class SwingParent {public boolean field_6252;public int field_6279;public float field_6251;protected void method_6119(){if(field_6252){if(++field_6279>=6){field_6279=0;field_6252=false;}}else field_6279=0;field_6251=field_6279/6f;}} ");
        if(ToolProvider.getSystemJavaCompiler().run(null,null,null,"-d",test.toString(),parent.toString())!=0)throw new AssertionError("Fixture compilation failed");
        ClassNode fixture=new ClassNode();fixture.version=Opcodes.V21;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="SwingFixture";fixture.superName="SwingParent";
        fixture.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"king","Z",null,null));
        MethodNode init=new MethodNode(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);init.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,"SwingParent","<init>","()V",false));init.instructions.add(new InsnNode(Opcodes.RETURN));fixture.methods.add(init);
        MethodNode king=new MethodNode(Opcodes.ACC_PUBLIC,"isKing","()Z",null,null);king.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));king.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,"SwingFixture","king","Z"));king.instructions.add(new InsnNode(Opcodes.IRETURN));fixture.methods.add(king);
        try(ZipFile zip=new ZipFile(args[0])){
            ClassNode entity=new ClassNode();new ClassReader(zip.getInputStream(zip.getEntry("dev/minesla/djinn/DjinnEntity.class"))).accept(entity,0);
            MethodNode swing=entity.methods.stream().filter(m->m.name.equals("method_6119")).findFirst().orElseThrow();
            for(AbstractInsnNode n:swing.instructions){
                if(n instanceof FieldInsnNode f&&f.owner.equals(entity.name))f.owner="SwingFixture";
                if(n instanceof MethodInsnNode m){if(m.owner.equals(entity.name))m.owner="SwingFixture";else if(m.owner.equals(entity.superName))m.owner="SwingParent";}
            }
            fixture.methods.add(swing);
        }
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);fixture.accept(writer);Files.write(test.resolve("SwingFixture.class"),writer.toByteArray());
        try(URLClassLoader loader=new URLClassLoader(new URL[]{test.toUri().toURL()})){
            Class<?> type=loader.loadClass("SwingFixture");var method=type.getMethod("method_6119");Object mob=type.getConstructor().newInstance();
            type.getField("king").setBoolean(mob,true);type.getField("field_6252").setBoolean(mob,true);type.getField("field_6279").setInt(mob,-1);
            for(int tick=0;tick<18;tick++){method.invoke(mob);if(!type.getField("field_6252").getBoolean(mob))throw new AssertionError("King swing ended early");}
            method.invoke(mob);if(type.getField("field_6252").getBoolean(mob)||type.getField("field_6251").getFloat(mob)!=0)throw new AssertionError("King swing did not complete");
            type.getField("king").setBoolean(mob,false);type.getField("field_6252").setBoolean(mob,true);type.getField("field_6279").setInt(mob,-1);
            for(int tick=0;tick<7;tick++)method.invoke(mob);
            if(type.getField("field_6252").getBoolean(mob))throw new AssertionError("Normal swing did not delegate to its parent");
        }
        System.out.println("PASS: production 18-tick swing method loaded and executed on JVM; ordinary mobs retain parent timing (API fixture, not Minecraft).");
    }
}
