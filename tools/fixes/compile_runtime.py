"""Compile pinned-intermediary runtime fixes with narrow, descriptor-checked API fixtures.
Only dev/minesla/djinn/runtime classes are copied into the distribution.
"""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT/'build/fix-api';SRC=BASE/'src';CLASSES=BASE/'classes'
SRC.mkdir(parents=True,exist_ok=True);CLASSES.mkdir(exist_ok=True)
sources={
'class_5321': 'public class class_5321 {}',
'class_2960': 'public class class_2960 {public final String id;public class_2960(String id){this.id=id;}public String toString(){return id;}}',
'class_243': 'public class class_243 {public double field_1352,field_1351,field_1350;public class_243(double x,double y,double z){field_1352=x;field_1351=y;field_1350=z;}}',
'class_2338': 'public class class_2338 {public final int x,y,z;public class_2338(int x,int y,int z){this.x=x;this.y=y;this.z=z;}public int method_10263(){return x;}public int method_10260(){return z;}}',
'class_238': 'public class class_238 {public class_238 method_1014(double d){return this;}}',
'class_2561': 'public interface class_2561 {static class_5250 method_43470(String text){return new class_5250(text);}}',
'class_5250': 'public class class_5250 implements class_2561 {public String text;public class_5250(String t){text=t;}public String toString(){return text;}}',
'class_7924': 'public class class_7924 {public static final class_5321 field_41246=new class_5321();}',
'class_2378': 'public interface class_2378 {Object method_10223(class_2960 id);}',
'class_5455': 'public interface class_5455 {class_2378 method_30530(class_5321 key);}',
'class_1922': 'public interface class_1922 {}',
'class_4538': 'public interface class_4538 {class_5455 method_30349();}',
'class_1936': 'public interface class_1936 {class_2802 method_8398();}',
'class_5281': 'public interface class_5281 {long method_8412();}',
'class_5425': 'public interface class_5425 extends class_5281 {}',
'class_2802': 'public class class_2802 {}',
'class_2806': 'public class class_2806 {public final String id;public class_2806(String id){this.id=id;}public static class_2806 method_12168(String id){return new class_2806(id);}}',
'class_9259': 'public interface class_9259<T> {T method_57130(T other);}',
'class_2810': 'public interface class_2810 {class_3449 method_12181(class_3195 structure);}',
'class_2791': 'public class class_2791 implements class_2810 {public class_3449 start;public class_3449 method_12181(class_3195 structure){return start;}}',
'class_3195': 'public class class_3195 {}',
'class_3341': 'public class class_3341 {public class_2338 center=new class_2338(120,70,140);public class_2338 method_22874(){return center;}}',
'class_3449': 'public class class_3449 {public boolean valid;public class_3341 box=new class_3341();public boolean method_16657(){return valid;}public class_3341 method_14969(){return box;}}',
'class_3215': '''public class class_3215 extends class_2802 {
 public Thread mainThread;public final java.util.Queue<java.util.concurrent.CompletableFuture<class_9259<class_2791>>> requests=new java.util.concurrent.ConcurrentLinkedQueue<>();
 public java.util.concurrent.CompletableFuture<class_9259<class_2791>> method_14134(int x,int z,class_2806 status,boolean create){
  if(Thread.currentThread()==mainThread)throw new AssertionError("Chunk request made on server thread");
  if(!status.id.endsWith("structure_starts"))throw new AssertionError("Full chunk generation requested");
  var f=new java.util.concurrent.CompletableFuture<class_9259<class_2791>>();requests.add(f);return f;
 }}''',
'class_1937': '''public class class_1937 implements class_1936,class_4538,class_1922 {
 public static final class_5321 field_25179=new class_5321();public class_5321 dimension=field_25179;
 public class_5321 method_27983(){return dimension;}public class_2802 method_8398(){return null;}
 public class_5455 method_30349(){return null;}
 }''',
'class_2680':'public class class_2680 {public boolean solid;public class_2680(boolean s){solid=s;}public boolean method_26212(class_1922 world,class_2338 pos){return solid;}}',
'class_2394':'public interface class_2394 {}',
'class_2400':'public class class_2400 implements class_2394 {}',
'class_2398':'public class class_2398 {public static final class_2400 field_22246=new class_2400();}',
'class_3414':'public class class_3414 {}',
'class_3417':'public class class_3417 {public static final class_3414 field_15193=new class_3414();}',
'class_1266':'public class class_1266 {}',
'class_3730':'public enum class_3730 {field_16471}',
'class_1315':'public interface class_1315 {}',
'class_1297':'''public class class_1297 {
 public double x,y=1,z;public class_1937 world;public boolean alive=true;public final java.util.UUID uuid=java.util.UUID.randomUUID();
 public double method_23317(){return x;}public double method_23318(){return y;}public double method_23321(){return z;}
 public float method_36454(){return 0;}public java.util.UUID method_5667(){return uuid;}public class_238 method_5829(){return new class_238();}
 public class_1937 method_37908(){return world;}public boolean method_5805(){return alive;}
 public void method_5808(double x,double y,double z,float yaw,float pitch){this.x=x;this.y=y;this.z=z;}
 public class_2338 method_24515(){return new class_2338((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));}
 }''',
'class_1309':'public class class_1309 extends class_1297 {public float health=300,maxHealth=300;public float method_6032(){return health;}public float method_6063(){return maxHealth;}}',
'class_1299':'public class class_1299<T extends class_1297> {public java.util.function.Function<class_1937,T> factory;public class_1299(java.util.function.Function<class_1937,T> f){factory=f;}public T method_5883(class_1937 world){return factory.apply(world);}}',
'class_3218':'''public class class_3218 extends class_1937 implements class_5425 {
 public final class_3215 chunks=new class_3215();public final class_3195 structure=new class_3195();
 public final java.util.List<class_1297> entities=new java.util.ArrayList<>();public boolean blocked,blockPositiveX;public int particles,spaceChecks;
 public class_2802 method_8398(){return chunks;}public long method_8412(){return 12345;}
 public class_5455 method_30349(){return key->id->id.toString().equals("djinns:djin_city_capital")?structure:null;}
 public int method_31607(){return -64;}public class_2680 method_8320(class_2338 p){return new class_2680(p.y<=0);}
 public boolean method_17892(class_1297 e){spaceChecks++;return !blocked&&e.y>=1&&(!blockPositiveX||e.x<0);}
 public class_1266 method_8404(class_2338 p){return new class_1266();}public boolean method_8649(class_1297 e){entities.add(e);return true;}
 public <T extends class_1297> java.util.List<T> method_8390(Class<T> type,class_238 box,java.util.function.Predicate<? super T> test){var result=new java.util.ArrayList<T>();for(var e:entities)if(type.isInstance(e)&&test.test(type.cast(e)))result.add(type.cast(e));return result;}
 public <T extends class_2394> int method_14199(T particle,double x,double y,double z,int count,double a,double b,double c,double d){particles+=count;return 0;}
 }''',
'class_2168':'''public class class_2168 {
 public class_3218 world;public String name="Tester";public int permission=2;public java.util.List<String> messages=new java.util.ArrayList<>();
 public class_2168(class_3218 w){world=w;}public class_3218 method_9225(){return world;}public String method_9214(){return name;}
 public boolean method_9259(int level){return permission>=level;}public class_243 method_9222(){return new class_243(0,64,0);}
 public void method_9226(java.util.function.Supplier<class_2561> text,boolean broadcast){messages.add(text.get().toString());}
 }''',
'class_7157':'public interface class_7157 {}',
'class_2170':'public class class_2170 {public enum class_5364 {ALL}}'
}
files={('net/minecraft/'+name+'.java'):'package net.minecraft; '+body for name,body in sources.items()}
files['net/minecraft/server/MinecraftServer.java']='package net.minecraft.server; public class MinecraftServer {}'
files['net/fabricmc/fabric/api/event/Event.java']='package net.fabricmc.fabric.api.event;public class Event<T> {public final java.util.List<T> callbacks=new java.util.ArrayList<>();public void register(T c){callbacks.add(c);}}'
files['net/fabricmc/fabric/api/command/v2/CommandRegistrationCallback.java']='''package net.fabricmc.fabric.api.command.v2;
 import net.fabricmc.fabric.api.event.Event;import net.minecraft.*;import com.mojang.brigadier.CommandDispatcher;
 public interface CommandRegistrationCallback {Event<CommandRegistrationCallback> EVENT=new Event<>();void register(CommandDispatcher<class_2168> d,class_7157 a,class_2170.class_5364 e);}'''
files['net/fabricmc/fabric/api/event/lifecycle/v1/ServerTickEvents.java']='''package net.fabricmc.fabric.api.event.lifecycle.v1;import net.fabricmc.fabric.api.event.Event;import net.minecraft.server.MinecraftServer;
 public final class ServerTickEvents {public static final Event<EndTick> END_SERVER_TICK=new Event<>();public interface EndTick {void onEndTick(MinecraftServer server);}}'''
files['net/fabricmc/fabric/api/event/lifecycle/v1/ServerLifecycleEvents.java']='''package net.fabricmc.fabric.api.event.lifecycle.v1;import net.fabricmc.fabric.api.event.Event;import net.minecraft.server.MinecraftServer;
 public final class ServerLifecycleEvents {public static final Event<ServerStopped> SERVER_STOPPED=new Event<>();public interface ServerStopped {void onServerStopped(MinecraftServer server);}}'''
files['dev/minesla/djinn/Djinns.java']='''package dev.minesla.djinn;import net.minecraft.*;public class Djinns {
 public static final String[] COLORS={"blue","red","white","black"};public static final java.util.Map<String,class_1299<DjinnEntity>> TYPES=new java.util.HashMap<>();
 public static class_2960 id(String path){return new class_2960("djinns:"+path);}}'''
files['dev/minesla/djinn/DjinnEntity.java']='''package dev.minesla.djinn;import net.minecraft.*;public class DjinnEntity extends class_1309 {
 public class_1309 target;public java.util.UUID owner;public int life,xp=10,casts,initialized;public String color;
 public class_1309 method_5968(){return target;}public java.util.UUID djinns$getSummoner(){return owner;}
 public void djinns$bindSummoner(java.util.UUID owner,class_1309 target){this.owner=owner;this.target=target;life=1200;xp=0;}
 public void djinns$startRoyalCast(){casts++;}public void method_5783(class_3414 sound,float v,float p){}
 public void method_5614(int age){}public class_1315 method_5943(class_5425 w,class_1266 d,class_3730 r,class_1315 data){initialized++;return null;}}'''
for name,body in files.items():
 p=SRC/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body)
javac='/workspace/tools/jdk-21.0.9+10/bin/javac'
api_sources=[str(p) for p in SRC.rglob('*.java')]
runtime=[str(p) for p in (ROOT/'tools/fixes/runtime').glob('*.java')]
subprocess.run([javac,'-proc:none','--release','21','-encoding','UTF-8','-cp',str(ROOT/'build/brigadier-api'),'-d',str(CLASSES)]+api_sources+runtime,check=True)
print('Runtime fixes compiled with pinned 1.21.1 API descriptors; fixtures excluded from the mod distribution.')
