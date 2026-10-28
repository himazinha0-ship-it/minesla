package dev.minesla.djinn.runtime;

import dev.minesla.djinn.*;
import net.minecraft.*;
import java.util.*;

/** Ground-aware reinforcements. All world work stays on the server tick. */
public final class RoyalSummons {
    private static final Map<DjinnEntity,State> STATES=new WeakHashMap<>();
    private static final class State {
        int cooldown=40,phase=-1,wave;
        final List<Pending> pending=new ArrayList<>();
    }
    private record Pending(DjinnEntity guard,double x,double y,double z) {}
    private static Pending place(class_3218 world,DjinnEntity king,DjinnEntity guard,int offset) {
        for(int attempt=0;attempt<32;attempt++) {
            double angle=(attempt+offset*7)*2.399963229728653;
            double radius=2.5+(attempt%5)*.65;
            double x=king.method_23317()+Math.cos(angle)*radius,z=king.method_23321()+Math.sin(angle)*radius;
            int bx=(int)Math.floor(x),bz=(int)Math.floor(z),top=(int)Math.floor(king.method_23318())+1;
            for(int y=top;y>=Math.max(world.method_31607()+1,top-8);y--) {
                class_2338 support=new class_2338(bx,y-1,bz);
                if(!world.method_8320(support).method_26212(world,support))continue;
                double feet=y+.1;
                guard.method_5808(x,feet,z,king.method_36454(),0);
                if(world.method_17892(guard))return new Pending(guard,x,feet,z);
            }
        }
        return null;
    }
    private static int count(class_3218 world,DjinnEntity king) {
        UUID owner=king.method_5667();
        return world.method_8390(DjinnEntity.class,king.method_5829().method_1014(48),
            e->e.method_5805()&&owner.equals(e.djinns$getSummoner())).size();
    }
    public static void tick(DjinnEntity king) {
        class_3218 world=(class_3218)king.method_37908();State s=STATES.computeIfAbsent(king,k->new State());
        class_1309 target=king.method_5968();
        if(target==null||!target.method_5805()) {s.phase=-1;s.pending.clear();s.cooldown=Math.min(s.cooldown,40);return;}
        if(s.phase<0) {
            if(--s.cooldown>0)return;
            int slots=Math.min(3,6-count(world,king));
            s.cooldown=king.method_6032()<king.method_6063()/2?120:160;
            if(slots<=0)return;
            s.pending.clear();
            for(int i=0;i<slots;i++) {
                String color=Djinns.COLORS[(s.wave+i)%4];
                DjinnEntity guard=(DjinnEntity)Djinns.TYPES.get(color+"_guard_djinn").method_5883(world);
                if(guard==null)continue;
                Pending p=place(world,king,guard,s.wave+i);if(p!=null)s.pending.add(p);
            }
            s.wave++;if(s.pending.isEmpty()){s.cooldown=20;return;}
            s.phase=0;king.djinns$startRoyalCast();king.method_5783(class_3417.field_15193,1.5f,.6f);
        }
        if(s.phase%2==0)for(Pending p:s.pending)for(int i=0;i<8;i++) {
            double angle=i*Math.PI/4+s.phase*.12;
            world.method_14199(class_2398.field_22246,p.x+Math.cos(angle)*.6,p.y+1.15+Math.sin(angle)*1.1,
                p.z,1,.015,.015,.015,0);
        }
        if(s.phase==20) {
            int active=count(world,king);
            for(Pending p:s.pending) {
                if(active>=6)break;
                Pending safe=world.method_17892(p.guard)?p:place(world,king,p.guard,s.wave++);
                if(safe==null)continue;
                DjinnEntity guard=safe.guard;
                guard.method_5943(world,world.method_8404(guard.method_24515()),class_3730.field_16471,null);
                guard.method_5614(0);
                guard.djinns$bindSummoner(king.method_5667(),target);
                if(world.method_8649(guard))active++;
            }
        }
        if(++s.phase>36){s.phase=-1;s.pending.clear();}
    }
    public static void clear(){STATES.clear();}
}
