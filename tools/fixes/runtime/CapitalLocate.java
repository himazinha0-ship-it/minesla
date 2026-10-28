package dev.minesla.djinn.runtime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import dev.minesla.djinn.Djinns;
import java.util.*;
import java.util.concurrent.*;

/** Intermediary API names are deliberate: this is compiled for the installed 1.21.1 JAR. */
public final class CapitalLocate {
    private static final Map<String,Search> ACTIVE=new LinkedHashMap<>();
    private static final ExecutorService REQUESTS=Executors.newSingleThreadExecutor(r->{
        Thread t=new Thread(r,"djinns-locate-requests");t.setDaemon(true);return t;
    });
    private static final class Search {
        final class_2168 source; final class_3218 world; final class_3215 chunks; final class_3195 structure;
        final List<LocatePlan.Candidate> candidates; final long deadline=System.nanoTime()+30_000_000_000L;
        int next; CompletableFuture<class_9259<class_2791>> pending;
        Search(class_2168 source,class_3195 structure) {
            this.source=source;this.world=source.method_9225();this.structure=structure;
            this.chunks=(class_3215)((class_1936)world).method_8398();
            class_243 pos=source.method_9222();
            candidates=LocatePlan.candidates(((class_5281)world).method_8412(),
                Math.floorDiv((int)Math.floor(pos.field_1352),16),Math.floorDiv((int)Math.floor(pos.field_1350),16));
        }
    }
    private static LiteralArgumentBuilder<class_2168> literal(String name) {
        return LiteralArgumentBuilder.<class_2168>literal(name).requires(s->s.method_9259(2));
    }
    public static void install() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registries,environment)->register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(CapitalLocate::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server->{
            ACTIVE.values().forEach(s->{if(s.pending!=null)s.pending.cancel(false);});ACTIVE.clear();RoyalSummons.clear();
        });
    }
    public static void register(CommandDispatcher<class_2168> dispatcher) {
        // Brigadier merges this exact literal into the vanilla tree, retaining other structures.
        dispatcher.register(literal("locate").then(literal("structure").then(
            literal("djinns:djin_city_capital").executes(c->start(c.getSource())))));
        dispatcher.register(literal("djinns")
            .then(literal("locate").executes(c->start(c.getSource())))
            .then(literal("cancelar").executes(c->cancel(c.getSource())))
            .then(literal("cancel").executes(c->cancel(c.getSource()))));
    }
    private static void tell(class_2168 source,String text) {
        source.method_9226(()->class_2561.method_43470(text),false);
    }
    public static int start(class_2168 source) {
        if(!source.method_9225().method_27983().equals(class_1937.field_25179)) {
            tell(source,"A capital dos djinns so e gerada no Overworld.");return 0;
        }
        String key=source.method_9214();
        if(ACTIVE.containsKey(key)){tell(source,"Sua busca ja esta em andamento. Use /djinns cancelar para interromper.");return 0;}
        if(ACTIVE.size()>=4){tell(source,"Ha quatro buscas em andamento. Tente novamente em instantes.");return 0;}
        class_2378 registry=((class_4538)source.method_9225()).method_30349().method_30530(class_7924.field_41246);
        class_3195 structure=(class_3195)registry.method_10223(Djinns.id("djin_city_capital"));
        if(structure==null){tell(source,"A estrutura da capital nao esta registrada neste mundo.");return 0;}
        ACTIVE.put(key,new Search(source,structure));
        tell(source,"Buscando uma capital confirmada sem bloquear o servidor (ate 30 segundos). /djinns cancelar interrompe.");return 1;
    }
    public static int cancel(class_2168 source) {
        Search s=ACTIVE.remove(source.method_9214());
        if(s!=null&&s.pending!=null)s.pending.cancel(false);
        tell(source,s==null?"Nao ha busca em andamento.":"Busca da capital cancelada.");return s==null?0:1;
    }
    public static void tick(MinecraftServer server) {
        Iterator<Search> searches=ACTIVE.values().iterator();
        while(searches.hasNext()) {
            Search s=searches.next();
            if(System.nanoTime()>=s.deadline||s.next>=s.candidates.size()&&s.pending==null) {
                if(s.pending!=null)s.pending.cancel(false);
                tell(s.source,"Busca limitada encerrada sem uma capital confirmada. Aproxime-se de um deserto e tente outra vez.");searches.remove();continue;
            }
            try {
                if(s.pending!=null) {
                    if(!s.pending.isDone())continue;
                    class_9259<class_2791> result=s.pending.getNow(null);s.pending=null;
                    class_2791 chunk=result==null?null:result.method_57130(null);
                    class_3449 start=chunk==null?null:((class_2810)chunk).method_12181(s.structure);
                    if(start!=null&&start.method_16657()) {
                        class_2338 center=start.method_14969().method_22874();
                        tell(s.source,"Capital confirmada em X="+center.method_10263()+" Z="+center.method_10260()+".");
                        searches.remove();continue;
                    }
                    if(s.next%32==0)tell(s.source,"Busca em andamento: "+s.next+" candidatos verificados.");
                }
                if(s.next>=s.candidates.size())continue;
                LocatePlan.Candidate p=s.candidates.get(s.next++);
                // The public chunk-future method blocks when invoked ON the server thread.
                // Only the request is routed off-thread; Minecraft schedules generation itself.
                // No world reads or structure inspection happen in this worker.
                s.pending=CompletableFuture.supplyAsync(()->s.chunks.method_14134(p.x(),p.z(),
                    class_2806.method_12168("minecraft:structure_starts"),true),REQUESTS).thenCompose(f->f);
            } catch(RuntimeException error) {
                tell(s.source,"A busca foi interrompida por uma falha ao consultar o chunk. O servidor continua ativo.");
                searches.remove();
            }
        }
    }
}
