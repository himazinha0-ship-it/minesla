import com.google.gson.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

/** Internal reproducible incremental build; never distributed inside the mod. */
public final class BuildFixes implements Opcodes {
    static final String ENTITY="dev/minesla/djinn/DjinnEntity", RUNTIME="dev/minesla/djinn/runtime/";
    static final Map<String,Integer> counts=new TreeMap<>();
    static void count(String key){counts.merge(key,1,Integer::sum);}
    static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    static AbstractInsnNode previous(AbstractInsnNode n){do{n=n.getPrevious();}while(n.getOpcode()<0);return n;}
    static void load(InsnList b,int n){b.add(new VarInsnNode(ALOAD,n));}
    static void call(InsnList b,int op,String owner,String name,String desc){b.add(new MethodInsnNode(op,owner,name,desc,false));}
    static MethodNode method(String name,String desc){return new MethodNode(ACC_PUBLIC,name,desc,null,null);}
    static void entity(ClassNode c){
        for(MethodNode m:c.methods){
            if(m.name.equals("method_5965")){
                m.instructions.clear();m.tryCatchBlocks.clear();m.localVariables=null;
                load(m.instructions,0);load(m.instructions,1);
                call(m.instructions,INVOKESPECIAL,c.superName,m.name,m.desc);m.instructions.add(new InsnNode(ARETURN));count("ground_navigation");
            }
            for(AbstractInsnNode n:m.instructions.toArray()){
                if(n instanceof TypeInsnNode t&&t.desc.equals("net/minecraft/class_1331")){t.desc="net/minecraft/class_1335";count("ground_control_new");}
                if(n instanceof MethodInsnNode call){
                    if(call.owner.equals("net/minecraft/class_1331")&&call.name.equals("<init>")){
                        AbstractInsnNode flag=previous(n),pitch=previous(flag);
                        require(flag.getOpcode()==ICONST_1&&pitch instanceof IntInsnNode i&&i.operand==18,"Unexpected flight constructor");
                        m.instructions.remove(flag);m.instructions.remove(pitch);call.owner="net/minecraft/class_1335";call.desc="(Lnet/minecraft/class_1308;)V";count("ground_control_ctor");
                    }
                    if(call.name.equals("method_5875")&&call.desc.equals("(Z)V")){
                        AbstractInsnNode value=previous(n);require(value.getOpcode()==ICONST_1,"Unexpected gravity flag");m.instructions.set(value,new InsnNode(ICONST_0));count("gravity_enabled");
                    }
                    if(call.owner.equals("net/minecraft/class_1395")){require(call.desc.equals("(Lnet/minecraft/class_1314;D)V"),"Unexpected FlyGoal constructor");call.owner="net/minecraft/class_1394";count("ground_wander_ctor");}
                }
                if(n instanceof TypeInsnNode t&&t.desc.equals("net/minecraft/class_1395")){t.desc="net/minecraft/class_1394";count("ground_wander_new");}
            }
            if(m.name.equals("tickKing")){
                InsnList replacement=new InsnList();Map<LabelNode,LabelNode> labels=new HashMap<>();
                for(AbstractInsnNode n:m.instructions)if(n instanceof LabelNode l)labels.put(l,new LabelNode());
                boolean found=false;
                for(AbstractInsnNode n:m.instructions){
                    replacement.add(n.clone(labels));
                    if(n instanceof MethodInsnNode call&&call.owner.equals("net/minecraft/class_3213")&&call.desc.equals("(Z)V")){found=true;break;}
                }
                require(found,"Boss bar prefix missing");load(replacement,0);
                call(replacement,INVOKESTATIC,RUNTIME+"RoyalSummons","tick","(L"+ENTITY+";)V");replacement.add(new InsnNode(RETURN));
                m.instructions=replacement;m.tryCatchBlocks.clear();m.localVariables=null;count("safe_summons");
            }
        }
        require(c.methods.stream().noneMatch(m->m.name.startsWith("djinns$")),"Already patched entity");
        MethodNode getter=method("djinns$getSummoner","()Ljava/util/UUID;");InsnList b=getter.instructions;
        load(b,0);b.add(new FieldInsnNode(GETFIELD,ENTITY,"summoner","Ljava/util/UUID;"));b.add(new InsnNode(ARETURN));c.methods.add(getter);
        MethodNode bind=method("djinns$bindSummoner","(Ljava/util/UUID;Lnet/minecraft/class_1309;)V");b=bind.instructions;
        load(b,0);load(b,1);b.add(new FieldInsnNode(PUTFIELD,ENTITY,"summoner","Ljava/util/UUID;"));
        load(b,0);b.add(new IntInsnNode(SIPUSH,1200));b.add(new FieldInsnNode(PUTFIELD,ENTITY,"summonLife","I"));
        load(b,0);b.add(new InsnNode(ICONST_0));b.add(new FieldInsnNode(PUTFIELD,ENTITY,"field_6194","I"));
        load(b,0);load(b,2);call(b,INVOKEVIRTUAL,ENTITY,"becomeAngry","(Lnet/minecraft/class_1309;)V");b.add(new InsnNode(RETURN));c.methods.add(bind);
        MethodNode cast=method("djinns$startRoyalCast","()V");b=cast.instructions;
        load(b,0);b.add(new FieldInsnNode(GETFIELD,ENTITY,"field_6011","Lnet/minecraft/class_2945;"));
        b.add(new FieldInsnNode(GETSTATIC,ENTITY,"CAST","Lnet/minecraft/class_2940;"));b.add(new IntInsnNode(BIPUSH,60));
        call(b,INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;");
        call(b,INVOKEVIRTUAL,"net/minecraft/class_2945","method_12778","(Lnet/minecraft/class_2940;Ljava/lang/Object;)V");
        b.add(new InsnNode(RETURN));c.methods.add(cast);count("summon_bridges");
    }
    public static void main(String[] args)throws Exception{
        Path original=Path.of(args[0]),classes=Path.of(args[1]),resources=Path.of(args[2]),output=Path.of(args[3]);
        Map<String,byte[]> entries=new LinkedHashMap<>();
        try(ZipFile z=new ZipFile(original.toFile())){for(ZipEntry e:Collections.list(z.entries()))try(var in=z.getInputStream(e)){entries.put(e.getName(),in.readAllBytes());}}
        for(String name:List.of(ENTITY,"dev/minesla/djinn/Djinns")){
            ClassNode c=new ClassNode();new ClassReader(entries.get(name+".class")).accept(c,0);
            if(name.equals(ENTITY))entity(c);else for(MethodNode m:c.methods)if(m.name.equals("onInitialize"))for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==RETURN){
                m.instructions.insertBefore(n,new MethodInsnNode(INVOKESTATIC,RUNTIME+"CapitalLocate","install","()V",false));count("locate_install");
            }
            ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(writer);entries.put(name+".class",writer.toByteArray());
        }
        require(counts.equals(Map.of("ground_navigation",1,"ground_control_new",1,"ground_control_ctor",1,"gravity_enabled",2,
            "ground_wander_ctor",1,"ground_wander_new",1,"safe_summons",1,"summon_bridges",1,"locate_install",1)),"Unexpected transformations: "+counts);
        try(var walk=Files.walk(classes.resolve(RUNTIME))){for(Path p:walk.filter(Files::isRegularFile).toList())if(p.toString().endsWith(".class"))entries.put(classes.relativize(p).toString().replace('\\','/'),Files.readAllBytes(p));}
        for(String resource:List.of("assets/djinns/models/item/royal_sword.json","data/djinns/structure/djin_city_capital.nbt"))entries.put(resource,Files.readAllBytes(resources.resolve(resource)));
        JsonObject metadata=JsonParser.parseString(new String(entries.get("fabric.mod.json"),StandardCharsets.UTF_8)).getAsJsonObject();
        metadata.addProperty("version","1.1.1");metadata.addProperty("description","Desert capital, grounded djinns, royal reinforcements and articulated Riftblade combat.");
        entries.put("fabric.mod.json",new GsonBuilder().setPrettyPrinting().create().toJson(metadata).getBytes(StandardCharsets.UTF_8));
        var manifest=new java.util.jar.Manifest(new java.io.ByteArrayInputStream(entries.get("META-INF/MANIFEST.MF")));manifest.getMainAttributes().putValue("Djinns-Release","1.1.1");
        var bytes=new java.io.ByteArrayOutputStream();manifest.write(bytes);entries.put("META-INF/MANIFEST.MF",bytes.toByteArray());
        try(var z=new ZipOutputStream(Files.newOutputStream(output))){for(var e:entries.entrySet()){ZipEntry entry=new ZipEntry(e.getKey());entry.setTime(0);z.putNextEntry(entry);z.write(e.getValue());z.closeEntry();}}
        System.out.println("Applied "+counts);System.out.println("Built "+output+" ("+Files.size(output)+" bytes)");
    }
}
