package dev.minesla.djinn.runtime;

import dev.minesla.djinn.*;
import java.util.*;

/** Ground-aware reinforcements. All world work stays on the server tick. */
public final class RoyalSummons {
    private static final Map<DjinnEntity,State> STATES=new WeakHashMap<>();
    private static final class State {
        int cooldown=40,phase=-1,wave;
        final List<Pending> pending=new ArrayList<>();
    }
    private record Pending(DjinnEntity guard,double x,double y,double z) {}
    private static Pending place(net.minecraft.server.world.ServerWorld world,DjinnEntity king,DjinnEntity guard,int offset) {
        for(int attempt=0;attempt<32;attempt++) {
            double angle=(attempt+offset*7)*2.399963229728653;
            double radius=2.5+(attempt%5)*.65;
            double x=king.getX()+Math.cos(angle)*radius,z=king.getZ()+Math.sin(angle)*radius;
            int bx=(int)Math.floor(x),bz=(int)Math.floor(z),top=(int)Math.floor(king.getY())+1;
            for(int y=top;y>=Math.max(world.getBottomY()+1,top-8);y--) {
                net.minecraft.util.math.BlockPos support=new net.minecraft.util.math.BlockPos(bx,y-1,bz);
                if(!world.getBlockState(support).isSolidBlock(world,support))continue;
                double feet=y+.1;
                guard.refreshPositionAndAngles(x,feet,z,king.getYaw(),0);
                if(world.isSpaceEmpty(guard))return new Pending(guard,x,feet,z);
            }
        }
        return null;
    }
    private static int count(net.minecraft.server.world.ServerWorld world,DjinnEntity king) {
        UUID owner=king.getUuid();
        return world.getEntitiesByClass(DjinnEntity.class,king.getBoundingBox().expand(48),
            e->e.isAlive()&&owner.equals(e.djinns$getSummoner())).size();
    }
    public static void tick(DjinnEntity king) {
        net.minecraft.server.world.ServerWorld world=(net.minecraft.server.world.ServerWorld)king.getWorld();State s=STATES.computeIfAbsent(king,k->new State());
        net.minecraft.entity.LivingEntity target=king.getTarget();
        if(target==null||!target.isAlive()) {s.phase=-1;s.pending.clear();s.cooldown=Math.min(s.cooldown,40);return;}
        if(s.phase<0) {
            if(--s.cooldown>0)return;
            int slots=Math.min(3,6-count(world,king));
            s.cooldown=king.getHealth()<king.getMaxHealth()/2?120:160;
            if(slots<=0)return;
            s.pending.clear();
            for(int i=0;i<slots;i++) {
                String color=Djinns.COLORS[(s.wave+i)%4];
                DjinnEntity guard=(DjinnEntity)Djinns.TYPES.get(color+"_guard_djinn").create(world);
                if(guard==null)continue;
                Pending p=place(world,king,guard,s.wave+i);if(p!=null)s.pending.add(p);
            }
            s.wave++;if(s.pending.isEmpty()){s.cooldown=20;return;}
            s.phase=0;king.djinns$startRoyalCast();king.playSound(net.minecraft.sound.SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON,1.5f,.6f);
        }
        if(s.phase%2==0)for(Pending p:s.pending)for(int i=0;i<8;i++) {
            double angle=i*Math.PI/4+s.phase*.12;
            world.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME,p.x+Math.cos(angle)*.6,p.y+1.15+Math.sin(angle)*1.1,
                p.z,1,.015,.015,.015,0);
        }
        if(s.phase==20) {
            int active=count(world,king);
            for(Pending p:s.pending) {
                if(active>=6)break;
                Pending safe=world.isSpaceEmpty(p.guard)?p:place(world,king,p.guard,s.wave++);
                if(safe==null)continue;
                DjinnEntity guard=safe.guard;
                guard.initialize(world,world.getLocalDifficulty(guard.getBlockPos()),net.minecraft.entity.SpawnReason.MOB_SUMMONED,null);
                guard.setBreedingAge(0);
                guard.djinns$bindSummoner(king.getUuid(),target);
                if(world.spawnEntity(guard))active++;
            }
        }
        if(++s.phase>36){s.phase=-1;s.pending.clear();}
    }
    public static void clear(){STATES.clear();}
}
