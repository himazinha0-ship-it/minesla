import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.*;
import dev.minesla.djinn.*;
import dev.minesla.djinn.runtime.*;
import net.minecraft.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/** Behavior tests against narrow world fixtures and the real Brigadier dispatcher. */
public final class RuntimeChecks {
    static int checks;
    static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
    static void tick(){CapitalLocate.tick(null);}
    static CompletableFuture<class_9259<class_2791>> request(class_3218 world)throws Exception{
        long end=System.nanoTime()+5_000_000_000L;
        for(;;){var f=world.chunks.requests.poll();if(f!=null)return f;if(System.nanoTime()>end)throw new AssertionError("No async chunk request");Thread.sleep(1);}
    }
    static void settle(){for(int i=0;i<100;i++){tick();Thread.yield();}}
    @SuppressWarnings("unchecked") static Map<String,Object> active()throws Exception{
        Field field=CapitalLocate.class.getDeclaredField("ACTIVE");field.setAccessible(true);return (Map<String,Object>)field.get(null);
    }
    static class_2168 source(){class_3218 world=new class_3218();world.chunks.mainThread=Thread.currentThread();return new class_2168(world);}
    static void stop(){ServerLifecycleEvents.SERVER_STOPPED.callbacks.forEach(c->c.onServerStopped(null));}
    static void locate()throws Exception{
        CapitalLocate.install();CommandDispatcher<class_2168> d=new CommandDispatcher<>();
        d.register(LiteralArgumentBuilder.<class_2168>literal("locate").then(LiteralArgumentBuilder.<class_2168>literal("structure")
            .then(RequiredArgumentBuilder.<class_2168,String>argument("structure",StringArgumentType.greedyString()).executes(c->77))));
        CapitalLocate.register(d);class_2168 s=source();
        check(d.execute("locate structure minecraft:village_desert",s)==77,"Other structures retain vanilla route");
        check(d.execute("locate structure djinns:djin_city_capital",s)==1,"Exact literal overrides synchronous vanilla argument");
        check(s.world.chunks.requests.isEmpty(),"Command callback does not generate chunks");
        tick();var pending=request(s.world);settle();
        check(!pending.isDone()&&active().size()==1,"Pending generation never blocks server ticks");
        class_2791 chunk=new class_2791();chunk.start=new class_3449();chunk.start.valid=true;
        pending.complete(other->chunk);settle();
        check(active().isEmpty(),"Confirmed actual start finishes search");
        check(s.messages.stream().anyMatch(m->m.contains("X=120 Z=140")),"Coordinates come from actual start bounding box");
        check(d.execute("djinns locate",s)==1,"Safe alias starts search");tick();request(s.world);
        check(d.execute("djinns cancelar",s)==1&&active().isEmpty(),"Cancellation removes outstanding search");
        check(d.execute("djinns cancel",s)==0,"Empty cancellation handled");
        s.permission=0;boolean denied=false;try{d.execute("djinns locate",s);}catch(Exception e){denied=true;}
        check(denied,"Custom command requires permission");s.permission=2;
        CapitalLocate.start(s);Object search=active().get(s.name);Field deadline=search.getClass().getDeclaredField("deadline");deadline.setAccessible(true);deadline.setLong(search,0);
        tick();check(active().isEmpty()&&s.messages.getLast().contains("limitada encerrada"),"Deadline ends search without claiming absent city");
        for(int n=0;n<5;n++){class_2168 other=source();other.name="User"+n;check(CapitalLocate.start(other)==(n<4?1:0),"At most four concurrent searches");}stop();
        check(active().isEmpty(),"Server stop clears requests");
        s=source();CapitalLocate.start(s);
        for(int n=0;n<LocatePlan.MAX_CANDIDATES;n++){
            tick();var f=request(s.world);f.complete(other->null);
            long end=System.nanoTime()+5_000_000_000L;
            while(!active().isEmpty()){
                tick();if(!s.world.chunks.requests.isEmpty()||active().isEmpty())break;
                if(System.nanoTime()>end)throw new AssertionError("Search did not advance");Thread.sleep(1);
            }
        }
        settle();check(active().isEmpty(),"Search stops after exactly 128 candidates");
        check(s.world.chunks.requests.isEmpty(),"No extra chunk after search cap");stop();
        s=source();s.world.dimension=new class_5321();check(CapitalLocate.start(s)==0,"Capital search restricted to Overworld");
        List<LocatePlan.Candidate> a=LocatePlan.candidates(12345,-72,-37);
        check(a.size()==128&&a.equals(LocatePlan.candidates(12345,-72,-37)),"Deterministic bounded candidate plan at negative coordinates");
        long previous=-1;
        for(var p:a){int rx=Math.floorDiv(p.x(),36),rz=Math.floorDiv(p.z(),36);Random r=new Random(12345L+rx*341873128712L+rz*132897987541L+73110429);
            check(p.x()==rx*36+r.nextInt(24)&&p.z()==rz*36+r.nextInt(24),"Candidate matches vanilla linear placement");
            long distance=(long)(p.x()+72)*(p.x()+72)+(long)(p.z()+37)*(p.z()+37);check(distance>=previous,"Candidates ordered by distance");previous=distance;}
    }
    static DjinnEntity king(class_3218 world){DjinnEntity k=new DjinnEntity();k.world=world;k.target=new class_1309();k.target.world=world;return k;}
    static void advance(DjinnEntity k,int ticks){for(int i=0;i<ticks;i++)RoyalSummons.tick(k);}
    static void summons(){
        for(String color:Djinns.COLORS)Djinns.TYPES.put(color+"_guard_djinn",new class_1299<>(w->{DjinnEntity e=new DjinnEntity();e.world=w;e.color=color;return e;}));
        class_3218 world=new class_3218();world.blockPositiveX=true;DjinnEntity k=king(world);
        advance(k,59);check(world.entities.isEmpty(),"Reinforcements respect cast warning");advance(k,1);
        check(world.entities.size()==3&&k.casts==1,"First combat wave summons three guards after three seconds");
        for(var entity:world.entities){DjinnEntity guard=(DjinnEntity)entity;
            check(guard.x<0&&guard.y==1.1,"Placement retries blocked directions and uses floor height");
            check(guard.owner.equals(k.uuid)&&guard.target==k.target,"Guards belong to king and attack current opponent");
            check(guard.initialized==1&&guard.life==1200&&guard.xp==0,"Guard equipment initialized, lifetime and rewards bounded");}
        advance(k,600);check(world.entities.size()==6,"King caps active nearby reinforcements at six");
        Set<String> colors=new HashSet<>();for(var entity:world.entities)colors.add(((DjinnEntity)entity).color);
        check(colors.size()==4,"Repeated waves include all four combat colors");
        world=new class_3218();world.blocked=true;k=king(world);advance(k,400);
        check(world.entities.isEmpty()&&world.spaceChecks<20000,"Fully obstructed room retries with bounded work and no wall spawns");
        world=new class_3218();k=king(world);k.target=null;advance(k,1000);check(world.entities.isEmpty(),"Neutral king does not summon attackers");RoyalSummons.clear();
    }
    public static void main(String[] args)throws Exception{locate();summons();System.out.println("PASS: "+checks+" behavior checks, real Brigadier command routing, async chunk requests, limits, cancellation, guarded summons and target propagation.");}
}
