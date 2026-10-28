"""Execute the production model with narrow test doubles for Minecraft's model API.
This catches JVM/frame, geometry and animation integration faults, not in-game bugs.
"""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[2]
JDK=Path('/workspace/tools/jdk-21.0.9+10/bin')
DIR=ROOT/'build/model-api-tests';SRC=DIR/'src';CLASSES=DIR/'classes'
SRC.mkdir(parents=True,exist_ok=True);CLASSES.mkdir(exist_ok=True)
sources={
'net/minecraft/class_1297.java':'package net.minecraft; public class class_1297 {}',
'net/minecraft/class_1306.java':'package net.minecraft; public enum class_1306 {field_6182, RIGHT}',
'net/minecraft/class_4587.java':'package net.minecraft; public class class_4587 {public int rotations; public void method_22904(double x,double y,double z){}}',
'net/minecraft/class_3881.java':'package net.minecraft; public interface class_3881 {void method_2803(class_1306 a,class_4587 m);}',
'net/minecraft/class_3882.java':'package net.minecraft; public interface class_3882 {class_630 method_2838();}',
'net/minecraft/class_583.java':'package net.minecraft; public class class_583<T extends class_1297> {public float field_3447; public void method_2819(T e,float a,float b,float c,float d,float f){}}',
'net/minecraft/class_5597.java':'package net.minecraft; public class class_5597<T extends class_1297> extends class_583<T>{public class_630 method_32008(){return null;}}',
'net/minecraft/class_5603.java':'''package net.minecraft; public class class_5603 {
 public float x,y,z;public static final class_5603 field_27701=new class_5603();
 public static class_5603 method_32090(float x,float y,float z){var t=new class_5603();t.x=x;t.y=y;t.z=z;return t;}}''',
'net/minecraft/class_5606.java':'''package net.minecraft; import java.util.*; public class class_5606 {
 public List<float[]> boxes=new ArrayList<>();public List<int[]> uvs=new ArrayList<>();private int u,v;
 public static class_5606 method_32108(){return new class_5606();}
 public class_5606 method_32101(int u,int v){this.u=u;this.v=v;return this;}
 public class_5606 method_32097(float x,float y,float z,float w,float h,float d){boxes.add(new float[]{x,y,z,w,h,d});uvs.add(new int[]{u,v});return this;}}''',
'net/minecraft/class_5610.java':'''package net.minecraft;import java.util.*;public class class_5610 {
 public class_5606 builder;public class_5603 transform;public Map<String,class_5610> children=new LinkedHashMap<>();
 public class_5610(class_5606 b,class_5603 t){builder=b;transform=t;}
 public class_5610 method_32117(String n,class_5606 b,class_5603 t){var p=new class_5610(b,t);children.put(n,p);return p;}
 public class_5610 method_32116(String n){return Objects.requireNonNull(children.get(n),n);}
 public class_630 part(){var p=new class_630();p.boxes=builder.boxes;p.uvs=builder.uvs;p.baseX=transform.x;p.baseY=transform.y;p.baseZ=transform.z;p.method_41923();children.forEach((n,c)->p.children.put(n,c.part()));return p;}}''',
'net/minecraft/class_5609.java':'''package net.minecraft;public class class_5609 {public class_5610 root=new class_5610(class_5606.method_32108(),class_5603.field_27701);public class_5610 method_32111(){return root;}}''',
'net/minecraft/class_5607.java':'''package net.minecraft;public class class_5607 {public class_630 root;public static class_5607 method_32110(class_5609 d,int w,int h){var t=new class_5607();t.root=d.root.part();return t;}}''',
'net/minecraft/class_630.java':'''package net.minecraft;import java.util.*;import java.util.stream.*;public class class_630 {
 public float field_3654,field_3655,field_3656,field_3657,field_3674,field_3675,baseX,baseY,baseZ;
 public boolean field_3665=true;public Map<String,class_630> children=new LinkedHashMap<>();public List<float[]> boxes;public List<int[]> uvs;
 public class_630 method_32086(String n){return Objects.requireNonNull(children.get(n),"Missing child "+n);}
 public Stream<class_630> method_32088(){return Stream.concat(Stream.of(this),children.values().stream().flatMap(class_630::method_32088));}
 public void method_41923(){field_3657=baseX;field_3656=baseY;field_3655=baseZ;field_3654=field_3674=field_3675=0;}
 public void method_22703(class_4587 m){m.rotations++;}}''',
'dev/minesla/djinn/DjinnEntity.java':'''package dev.minesla.djinn;public class DjinnEntity extends net.minecraft.class_1297 {
 public int role,cast;public boolean king;public int role(){return role;}public boolean isKing(){return king;}public int castingTicks(){return cast;}}''',
'ModelChecks.java':'''import dev.minesla.djinn.client.*;import dev.minesla.djinn.*;import net.minecraft.*;import java.nio.file.*;import com.google.gson.*;
 public class ModelChecks {
 static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception {
  var tree=DjinnModel.createModel();var model=new DjinnModel(tree.root);var spirit=tree.root.method_32086("spirit");
  var crown=spirit.method_32086("head").method_32086("royal_crown");int poses=0;
  for(boolean king:new boolean[]{false,true})for(int role=0;role<4;role++){
   var entity=new DjinnEntity();entity.king=king;entity.role=role;
   for(int f=0;f<181;f++){
    entity.cast=f<60?60-f:0;model.field_3447=f/180f;model.setAngles(entity,0,.1f,f*.25f,20,5);
    require(crown.field_3665==king,"Crown visibility leaked to another variant");
    tree.root.method_32088().forEach(p->{require(Float.isFinite(p.field_3654)&&Float.isFinite(p.field_3675)&&Float.isFinite(p.field_3674),"Invalid rotation");});poses++;
   }
   for(var arm:class_1306.values()){var matrices=new class_4587();model.method_2803(arm,matrices);require(matrices.rotations==3,"Weapon missed articulated forearm");}
  }
  var king=new DjinnEntity();king.king=true;king.role=2;model.field_3447=0;model.setAngles(king,0,0,100,0,0);
  Files.writeString(Path.of(args[0]),new Gson().toJson(tree.root));
  long cubes=tree.root.method_32088().mapToLong(p->p.boxes.size()).sum();
  System.out.println("PASS: actual production model executed for "+poses+" poses; "+cubes+" cuboids; crown, forearms, hierarchy and held-item transforms valid against model API test doubles.");
 }
}'''
}
for name,source in sources.items():
 p=SRC/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(source)
jar=ROOT/'dist/djinns-fabric-1.21.1-1.1.0.jar'
gson='/workspace/tools/gradle-8.8/lib/gson-2.10.jar'
subprocess.run([str(JDK/'javac'),'-proc:none','--release','21','-cp',str(jar)+':'+gson,'-d',str(CLASSES)]+[str(p) for p in SRC.rglob('*.java')],check=True)
subprocess.run([str(JDK/'java'),'-Xverify:all','-cp',str(CLASSES)+':'+str(jar)+':'+gson,'ModelChecks',str(ROOT/'build/previews/king-geometry.json')],check=True)
