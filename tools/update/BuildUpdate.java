import com.google.gson.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Internal build step. Never shipped as an installer or as code inside the mod. */
public final class BuildUpdate implements Opcodes {
    static final String MODEL="dev/minesla/djinn/client/DjinnModel", ENTITY="dev/minesla/djinn/DjinnEntity";
    static final String PART="net/minecraft/class_630", DATA="net/minecraft/class_5610", BUILDER="net/minecraft/class_5606", TRANSFORM="net/minecraft/class_5603";
    static final String PD="L"+PART+";", DD="L"+DATA+";", BD="L"+BUILDER+";", TD="L"+TRANSFORM+";";
    static JsonArray geometry;
    static Map<String,Integer> counts=new TreeMap<>();
    static void count(String key){counts.merge(key,1,Integer::sum);}
    static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    static AbstractInsnNode previous(AbstractInsnNode n,int number){while(number>0){n=n.getPrevious();if(n.getOpcode()>=0)number--;}return n;}
    static InsnList list(){return new InsnList();}
    static void f(InsnList b,float x){b.add(new LdcInsnNode(x));}
    static void i(InsnList b,int x){b.add(new LdcInsnNode(x));}
    static void aload(InsnList b,int x){b.add(new VarInsnNode(ALOAD,x));}
    static void invoke(InsnList b,int op,String owner,String name,String desc){b.add(new MethodInsnNode(op,owner,name,desc,false));}
    static void field(InsnList b,String name){aload(b,0);b.add(new FieldInsnNode(GETFIELD,MODEL,name,PD));}
    static void part(InsnList b,String path){String[] p=path.split("/");field(b,p[0]);for(int k=1;k<p.length;k++){b.add(new LdcInsnNode(p[k]));invoke(b,INVOKEVIRTUAL,PART,"method_32086","(Ljava/lang/String;)"+PD);}}
    static void sample(InsnList b,int index){aload(b,7);i(b,index);b.add(new InsnNode(FALOAD));}
    static void assign(InsnList b,String path,String property,int index){part(b,path);sample(b,index);b.add(new FieldInsnNode(PUTFIELD,PART,property,"F"));}
    static void visible(InsnList b,String path){part(b,path);aload(b,1);invoke(b,INVOKEVIRTUAL,ENTITY,"isKing","()Z");b.add(new FieldInsnNode(PUTFIELD,PART,"field_3665","Z"));}
    static void dataPart(InsnList b,String path){
        String[] p=path.split("/");int local;
        if(p[0].equals("spirit"))local=1;else if(p[0].equals("head"))local=2;else if(p[0].equals("tail"))local=3;
        else {local=1;aload(b,local);b.add(new LdcInsnNode(p[0]));invoke(b,INVOKEVIRTUAL,DATA,"method_32116","(Ljava/lang/String;)"+DD);local=-1;}
        if(local>=0)aload(b,local);
        for(int k=1;k<p.length;k++){b.add(new LdcInsnNode(p[k]));invoke(b,INVOKEVIRTUAL,DATA,"method_32116","(Ljava/lang/String;)"+DD);}
    }
    static InsnList extraGeometry(){
        InsnList b=list();
        for(JsonElement el:geometry){
            JsonObject p=el.getAsJsonObject();dataPart(b,p.get("parent").getAsString());b.add(new LdcInsnNode(p.get("name").getAsString()));
            invoke(b,INVOKESTATIC,BUILDER,"method_32108","()"+BD);
            for(JsonElement c:p.getAsJsonArray("cubes")){
                JsonObject cube=c.getAsJsonObject();for(JsonElement uv:cube.getAsJsonArray("uv"))i(b,uv.getAsInt());
                invoke(b,INVOKEVIRTUAL,BUILDER,"method_32101","(II)"+BD);
                for(JsonElement coordinate:cube.getAsJsonArray("box"))f(b,coordinate.getAsFloat());
                invoke(b,INVOKEVIRTUAL,BUILDER,"method_32097","(FFFFFF)"+BD);
            }
            for(JsonElement coordinate:p.getAsJsonArray("pivot"))f(b,coordinate.getAsFloat());
            invoke(b,INVOKESTATIC,TRANSFORM,"method_32090","(FFF)"+TD);
            invoke(b,INVOKEVIRTUAL,DATA,"method_32117","(Ljava/lang/String;"+BD+TD+")"+DD);b.add(new InsnNode(POP));
        }
        return b;
    }
    static void model(ClassNode c){
        MethodNode old=c.methods.stream().filter(m->m.name.equals("setAngles")).findFirst().orElseThrow();
        MethodNode pose=new MethodNode(old.access,old.name,old.desc,old.signature,old.exceptions.toArray(String[]::new));
        Map<LabelNode,LabelNode> labels=new HashMap<>();for(AbstractInsnNode n:old.instructions)if(n instanceof LabelNode l)labels.put(l,new LabelNode());
        for(AbstractInsnNode n:old.instructions){
            if(n.getOpcode()>=0)pose.instructions.add(n.clone(labels));
            if(n instanceof MethodInsnNode m&&m.name.equals("forEach"))break;
        }
        InsnList b=pose.instructions;
        b.add(new VarInsnNode(FLOAD,4));aload(b,0);b.add(new FieldInsnNode(GETFIELD,MODEL,"field_3447","F"));
        aload(b,1);invoke(b,INVOKEVIRTUAL,ENTITY,"castingTicks","()I");b.add(new InsnNode(I2F));
        aload(b,1);invoke(b,INVOKEVIRTUAL,ENTITY,"role","()I");aload(b,1);invoke(b,INVOKEVIRTUAL,ENTITY,"isKing","()Z");b.add(new VarInsnNode(FLOAD,3));
        invoke(b,INVOKESTATIC,"dev/minesla/djinn/client/RoyalPose","sample","(FFFIZF)[F");b.add(new VarInsnNode(ASTORE,7));
        part(b,"spirit");b.add(new InsnNode(DUP));b.add(new FieldInsnNode(GETFIELD,PART,"field_3656","F"));sample(b,0);b.add(new InsnNode(FADD));b.add(new FieldInsnNode(PUTFIELD,PART,"field_3656","F"));
        assign(b,"spirit","field_3675",1);assign(b,"spirit","field_3654",2);assign(b,"spirit","field_3674",3);
        part(b,"head");b.add(new VarInsnNode(FLOAD,5));f(b,.017453292f);b.add(new InsnNode(FMUL));sample(b,1);b.add(new InsnNode(FSUB));b.add(new FieldInsnNode(PUTFIELD,PART,"field_3675","F"));
        part(b,"head");b.add(new VarInsnNode(FLOAD,6));f(b,.010471975f);b.add(new InsnNode(FMUL));sample(b,18);b.add(new InsnNode(FADD));b.add(new FieldInsnNode(PUTFIELD,PART,"field_3654","F"));
        for(String[] v:new String[][]{{"rightArm","field_3654","4"},{"rightArm","field_3675","5"},{"rightArm","field_3674","6"},
            {"leftArm","field_3654","7"},{"leftArm","field_3675","8"},{"leftArm","field_3674","9"},
            {"tail","field_3674","10"},{"tail2","field_3674","11"},{"tail3","field_3674","12"},
            {"tail","field_3675","13"},{"rightArm/forearm","field_3654","14"},{"leftArm/forearm","field_3654","15"},
            {"spirit/royal_cape","field_3654","16"},{"spirit/royal_cape","field_3674","17"},{"tail3/tail4","field_3674","19"}})
                assign(b,v[0],v[1],Integer.parseInt(v[2]));
        part(b,"tail");f(b,-13);b.add(new FieldInsnNode(PUTFIELD,PART,"field_3656","F"));
        part(b,"guardArmor");aload(b,1);invoke(b,INVOKEVIRTUAL,ENTITY,"role","()I");invoke(b,INVOKESTATIC,"dev/minesla/djinn/RoyalBalance","isGuard","(I)Z");b.add(new FieldInsnNode(PUTFIELD,PART,"field_3665","Z"));
        visible(b,"royalArmor");
        for(String path:new String[]{"head/royal_crown","spirit/royal_cape","spirit/royal_chain","spirit/royal_tabard", "rightArm/royal_pauldron","leftArm/royal_pauldron","rightArm/forearm/royal_gauntlet","leftArm/forearm/royal_gauntlet","tail/royal_mist"})visible(b,path);
        b.add(new InsnNode(RETURN));pose.maxLocals=8;pose.maxStack=12;c.methods.set(c.methods.indexOf(old),pose);count("model_pose");
        for(MethodNode m:c.methods){
            if(m.name.equals("createModel")){
                boolean arm=false,first=false;
                for(AbstractInsnNode n:m.instructions.toArray()){
                    if(n instanceof LdcInsnNode l&&l.cst instanceof String s&&(s.equals("left_arm")||s.equals("right_arm"))){arm=true;first=true;}
                    if(arm&&first&&n instanceof MethodInsnNode call&&call.owner.equals(BUILDER)&&call.name.equals("method_32097")){
                        AbstractInsnNode height=previous(n,2);require(height instanceof LdcInsnNode l&&l.cst.equals(12f),"Unexpected upper-arm layout");
                        m.instructions.set(height,new LdcInsnNode(6f));first=false;arm=false;count("arm_shortened");
                    }
                    if(n instanceof MethodInsnNode call&&call.owner.equals("net/minecraft/class_5607")&&call.getOpcode()==INVOKESTATIC){
                        m.instructions.insertBefore(n,extraGeometry());count("geometry_added");
                    }
                }
            }
            if(m.name.equals("method_2803")){
                // Reuse the already selected upper arm left on the stack, preserving its pivot.
                int rotations=0;
                for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode call&&call.name.equals("method_22703")){
                    if(++rotations==2){
                        InsnList before=list();before.add(new VarInsnNode(ASTORE,3));before.add(new InsnNode(DUP));before.add(new VarInsnNode(ASTORE,4));aload(before,3);m.instructions.insertBefore(n,before);
                        InsnList after=list();aload(after,4);after.add(new LdcInsnNode("forearm"));invoke(after,INVOKEVIRTUAL,PART,"method_32086","(Ljava/lang/String;)"+PD);aload(after,3);invoke(after,INVOKEVIRTUAL,PART,"method_22703","(Lnet/minecraft/class_4587;)V");
                        aload(after,3);after.add(new LdcInsnNode(0d));after.add(new LdcInsnNode(-.375d));after.add(new LdcInsnNode(0d));invoke(after,INVOKEVIRTUAL,"net/minecraft/class_4587","method_22904","(DDD)V");m.instructions.insert(n,after);m.maxLocals=5;count("held_item_articulation");
                    }
                }
            }
        }
    }
    static void balance(ClassNode c){
        for(MethodNode m:c.methods){
            boolean royal=false,dimensions=false;
            for(AbstractInsnNode n:m.instructions.toArray()){
                if(m.name.equals("onInitialize")){
                    if(n instanceof LdcInsnNode l&&"royal_sword".equals(l.cst))royal=true;
                    if(royal&&n instanceof MethodInsnNode call&&call.name.equals("method_57394")){
                        AbstractInsnNode damage=previous(n,2);require(damage.getOpcode()==ICONST_5,"Unexpected sword attack modifier");m.instructions.set(damage,new IntInsnNode(BIPUSH,6));royal=false;count("sword_10_damage");
                    }
                }
                if(m.name.equals("registerEntity")){
                    if(!dimensions&&n.getOpcode()==FCONST_1){m.instructions.set(n,new LdcInsnNode(1.4f));count("king_width");}
                    if(n instanceof MethodInsnNode call&&call.name.equals("method_17687"))dimensions=true;
                    if(n instanceof LdcInsnNode l){
                        if(l.cst.equals(3.3f)){l.cst=5f;count("king_height");}
                        if(l.cst.equals(11d)){l.cst=1d;count("king_attack_base");}
                    }
                }
            }
        }
    }
    static void entity(ClassNode c){
        MethodNode swing=new MethodNode(ACC_PUBLIC,"method_6119","()V",null,null);InsnList b=swing.instructions;
        LabelNode king=new LabelNode(),idle=new LabelNode(),progress=new LabelNode();
        aload(b,0);invoke(b,INVOKEVIRTUAL,ENTITY,"isKing","()Z");b.add(new JumpInsnNode(IFNE,king));aload(b,0);invoke(b,INVOKESPECIAL,c.superName,"method_6119","()V");b.add(new InsnNode(RETURN));
        b.add(king);b.add(new FrameNode(F_SAME,0,null,0,null));aload(b,0);b.add(new FieldInsnNode(GETFIELD,ENTITY,"field_6252","Z"));b.add(new JumpInsnNode(IFEQ,idle));
        aload(b,0);b.add(new InsnNode(DUP));b.add(new FieldInsnNode(GETFIELD,ENTITY,"field_6279","I"));i(b,1);b.add(new InsnNode(IADD));b.add(new FieldInsnNode(PUTFIELD,ENTITY,"field_6279","I"));
        aload(b,0);b.add(new FieldInsnNode(GETFIELD,ENTITY,"field_6279","I"));i(b,18);b.add(new JumpInsnNode(IF_ICMPLT,progress));
        aload(b,0);i(b,0);b.add(new FieldInsnNode(PUTFIELD,ENTITY,"field_6279","I"));aload(b,0);i(b,0);b.add(new FieldInsnNode(PUTFIELD,ENTITY,"field_6252","Z"));b.add(new JumpInsnNode(GOTO,progress));
        b.add(idle);b.add(new FrameNode(F_SAME,0,null,0,null));aload(b,0);i(b,0);b.add(new FieldInsnNode(PUTFIELD,ENTITY,"field_6279","I"));
        b.add(progress);b.add(new FrameNode(F_SAME,0,null,0,null));aload(b,0);aload(b,0);b.add(new FieldInsnNode(GETFIELD,ENTITY,"field_6279","I"));b.add(new InsnNode(I2F));f(b,18);b.add(new InsnNode(FDIV));b.add(new FieldInsnNode(PUTFIELD,ENTITY,"field_6251","F"));b.add(new InsnNode(RETURN));swing.maxLocals=1;swing.maxStack=4;
        require(c.methods.stream().noneMatch(m->m.name.equals(swing.name)&&m.desc.equals(swing.desc)),"Swing override already exists");c.methods.add(swing);count("king_swing_18_ticks");
        for(MethodNode m:c.methods)if(m.name.equals("method_5749")){
            // Migrate saved kings so their base damage plus the new sword totals ten.
            for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==RETURN){
                InsnList patch=list();aload(patch,0);patch.add(new FieldInsnNode(GETSTATIC,"net/minecraft/class_5134","field_23721","Lnet/minecraft/class_6880;"));
                invoke(patch,INVOKEVIRTUAL,ENTITY,"method_5996","(Lnet/minecraft/class_6880;)Lnet/minecraft/class_1324;");patch.add(new InsnNode(DUP));invoke(patch,INVOKEVIRTUAL,"net/minecraft/class_1324","method_6201","()D");
                aload(patch,0);invoke(patch,INVOKEVIRTUAL,ENTITY,"isKing","()Z");invoke(patch,INVOKESTATIC,"dev/minesla/djinn/RoyalBalance","attackBase","(DZ)D");invoke(patch,INVOKEVIRTUAL,"net/minecraft/class_1324","method_6192","(D)V");m.instructions.insertBefore(n,patch);count("saved_king_damage_migration");
            }
        }
    }
    public static void main(String[] args)throws Exception{
        Path original=Path.of(args[0]),classes=Path.of(args[1]),resources=Path.of(args[2]),output=Path.of(args[3]);
        geometry=JsonParser.parseString(Files.readString(resources.resolve("assets/djinns/geometry/royal.json"))).getAsJsonObject().getAsJsonArray("parts");
        Map<String,byte[]> content=new LinkedHashMap<>();
        try(ZipFile z=new ZipFile(original.toFile())){for(ZipEntry e:Collections.list(z.entries()))try(var in=z.getInputStream(e)){content.put(e.getName(),in.readAllBytes());}}
        for(String name:List.of(MODEL,"dev/minesla/djinn/client/DjinnRenderer","dev/minesla/djinn/Djinns",ENTITY,"dev/minesla/djinn/DjinnCombatGoal")){
            ClassNode c=new ClassNode();new ClassReader(content.get(name+".class")).accept(c,0);
            if(name.equals(MODEL))model(c);if(name.endsWith("/Djinns"))balance(c);if(name.equals(ENTITY))entity(c);
            if(name.endsWith("/DjinnRenderer"))for(MethodNode m:c.methods)if(m.name.equals("scale"))for(AbstractInsnNode n:m.instructions)if(n instanceof LdcInsnNode l&&l.cst.equals(1.45f)){l.cst=2.05f;count("renderer_scale");}
            if(name.endsWith("/DjinnCombatGoal"))for(MethodNode m:c.methods)if(m.name.equals("method_6268"))for(AbstractInsnNode n:m.instructions){
                if(n instanceof LdcInsnNode l&&l.cst.equals(3.5d)){l.cst=4.4d;count("king_reach");}
                if(n instanceof LdcInsnNode l&&l.cst.equals(6f)){l.cst=10f;count("king_cleave_damage");}
                if(n instanceof IntInsnNode in&&in.operand==22){in.operand=28;count("king_attack_cooldown");}
            }
            ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(writer);byte[] data=writer.toByteArray();new ClassReader(data);content.put(name+".class",data);
        }
        for(String required:List.of("model_pose","arm_shortened","geometry_added","held_item_articulation","sword_10_damage","king_width","king_height","king_attack_base","king_swing_18_ticks","saved_king_damage_migration","renderer_scale","king_reach","king_cleave_damage","king_attack_cooldown"))require(counts.getOrDefault(required,0)>0,"Missing transformation: "+required);
        require(counts.get("arm_shortened")==2&&counts.get("renderer_scale")==3,"Unexpected transform counts");
        for(Path p:Files.walk(classes).filter(Files::isRegularFile).toList())content.put(classes.relativize(p).toString().replace('\\','/'),Files.readAllBytes(p));
        for(Path p:Files.walk(resources).filter(Files::isRegularFile).toList()){
            String name=resources.relativize(p).toString().replace('\\','/');
            if(name.equals("fabric.mod.json")||name.equals("djinns.client.mixins.json"))continue;
            content.put(name,Files.readAllBytes(p));
        }
        JsonObject metadata=JsonParser.parseString(new String(content.get("fabric.mod.json"),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();metadata.addProperty("version","1.1.0");metadata.addProperty("description","Desert djinn capital, a monumental king, articulated animations and a ten-damage voxel Riftblade.");
        content.put("fabric.mod.json",new GsonBuilder().setPrettyPrinting().create().toJson(metadata).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var manifest=new java.util.jar.Manifest(new java.io.ByteArrayInputStream(content.get("META-INF/MANIFEST.MF")));
        manifest.getMainAttributes().putValue("Djinns-Release","1.1.0");
        manifest.getMainAttributes().putValue("Djinns-Update-Method","incremental-verified-bytecode");
        String clientOnly=manifest.getMainAttributes().getValue("Fabric-Loom-Client-Only-Entries");
        manifest.getMainAttributes().putValue("Fabric-Loom-Client-Only-Entries",clientOnly+";dev/minesla/djinn/client/RoyalPose.class");
        var manifestBytes=new java.io.ByteArrayOutputStream();manifest.write(manifestBytes);content.put("META-INF/MANIFEST.MF",manifestBytes.toByteArray());
        try(ZipOutputStream z=new ZipOutputStream(Files.newOutputStream(output))){for(var e:content.entrySet()){z.putNextEntry(new ZipEntry(e.getKey()));z.write(e.getValue());z.closeEntry();}}
        System.out.println("Applied transformations: "+counts);System.out.println("Updated mod: "+output+" ("+Files.size(output)+" bytes)");
    }
}
