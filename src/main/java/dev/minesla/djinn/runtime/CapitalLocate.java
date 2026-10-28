package dev.minesla.djinn.runtime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import dev.minesla.djinn.Djinns;
import java.util.*;
import java.util.concurrent.*;

/** Bounded server-tick search; see tools/fixes for the incremental distribution build. */
public final class CapitalLocate {
    private static final Map<String,Search> ACTIVE=new LinkedHashMap<>();
    private static final ExecutorService REQUESTS=Executors.newSingleThreadExecutor(r->{
        Thread t=new Thread(r,"djinns-locate-requests");t.setDaemon(true);return t;
    });
    private static final class Search {
        final net.minecraft.server.command.ServerCommandSource source; final net.minecraft.server.world.ServerWorld world; final net.minecraft.server.world.ServerChunkManager chunks; final net.minecraft.world.gen.structure.Structure structure;
        final List<LocatePlan.Candidate> candidates; final long deadline=System.nanoTime()+30_000_000_000L;
        int next; CompletableFuture<net.minecraft.server.world.OptionalChunk<net.minecraft.world.chunk.Chunk>> pending;
        Search(net.minecraft.server.command.ServerCommandSource source,net.minecraft.world.gen.structure.Structure structure) {
            this.source=source;this.world=source.getWorld();this.structure=structure;
            this.chunks=(net.minecraft.server.world.ServerChunkManager)((net.minecraft.world.WorldAccess)world).getChunkManager();
            net.minecraft.util.math.Vec3d pos=source.getPosition();
            candidates=LocatePlan.candidates(((net.minecraft.world.StructureWorldAccess)world).getSeed(),
                Math.floorDiv((int)Math.floor(pos.x),16),Math.floorDiv((int)Math.floor(pos.z),16));
        }
    }
    private static LiteralArgumentBuilder<net.minecraft.server.command.ServerCommandSource> literal(String name) {
        return LiteralArgumentBuilder.<net.minecraft.server.command.ServerCommandSource>literal(name).requires(s->s.hasPermissionLevel(2));
    }
    public static void install() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registries,environment)->register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(CapitalLocate::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server->{
            ACTIVE.values().forEach(s->{if(s.pending!=null)s.pending.cancel(false);});ACTIVE.clear();RoyalSummons.clear();
        });
    }
    public static void register(CommandDispatcher<net.minecraft.server.command.ServerCommandSource> dispatcher) {
        // Brigadier merges this exact literal into the vanilla tree, retaining other structures.
        dispatcher.register(literal("locate").then(literal("structure").then(
            literal("djinns:djin_city_capital").executes(c->start(c.getSource())))));
        dispatcher.register(literal("djinns")
            .then(literal("locate").executes(c->start(c.getSource())))
            .then(literal("cancelar").executes(c->cancel(c.getSource())))
            .then(literal("cancel").executes(c->cancel(c.getSource()))));
    }
    private static void tell(net.minecraft.server.command.ServerCommandSource source,String text) {
        source.sendFeedback(()->net.minecraft.text.Text.literal(text),false);
    }
    public static int start(net.minecraft.server.command.ServerCommandSource source) {
        if(!source.getWorld().getRegistryKey().equals(net.minecraft.world.World.OVERWORLD)) {
            tell(source,"A capital dos djinns so e gerada no Overworld.");return 0;
        }
        String key=source.getName();
        if(ACTIVE.containsKey(key)){tell(source,"Sua busca ja esta em andamento. Use /djinns cancelar para interromper.");return 0;}
        if(ACTIVE.size()>=4){tell(source,"Ha quatro buscas em andamento. Tente novamente em instantes.");return 0;}
        net.minecraft.registry.Registry registry=((net.minecraft.world.WorldView)source.getWorld()).getRegistryManager().get(net.minecraft.registry.RegistryKeys.STRUCTURE);
        net.minecraft.world.gen.structure.Structure structure=(net.minecraft.world.gen.structure.Structure)registry.get(Djinns.id("djin_city_capital"));
        if(structure==null){tell(source,"A estrutura da capital nao esta registrada neste mundo.");return 0;}
        ACTIVE.put(key,new Search(source,structure));
        tell(source,"Buscando uma capital confirmada sem bloquear o servidor (ate 30 segundos). /djinns cancelar interrompe.");return 1;
    }
    public static int cancel(net.minecraft.server.command.ServerCommandSource source) {
        Search s=ACTIVE.remove(source.getName());
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
                    net.minecraft.server.world.OptionalChunk<net.minecraft.world.chunk.Chunk> result=s.pending.getNow(null);s.pending=null;
                    net.minecraft.world.chunk.Chunk chunk=result==null?null:result.orElse(null);
                    net.minecraft.structure.StructureStart start=chunk==null?null:((net.minecraft.world.StructureHolder)chunk).getStructureStart(s.structure);
                    if(start!=null&&start.hasChildren()) {
                        net.minecraft.util.math.BlockPos center=start.getBoundingBox().getCenter();
                        tell(s.source,"Capital confirmada em X="+center.getX()+" Z="+center.getZ()+".");
                        searches.remove();continue;
                    }
                    if(s.next%32==0)tell(s.source,"Busca em andamento: "+s.next+" candidatos verificados.");
                }
                if(s.next>=s.candidates.size())continue;
                LocatePlan.Candidate p=s.candidates.get(s.next++);
                // The public chunk-future method blocks when invoked ON the server thread.
                // Only the request is routed off-thread; Minecraft schedules generation itself.
                // No world reads or structure inspection happen in this worker.
                s.pending=CompletableFuture.supplyAsync(()->s.chunks.getChunkFuture(p.x(),p.z(),
                    net.minecraft.world.chunk.ChunkStatus.byId("minecraft:structure_starts"),true),REQUESTS).thenCompose(f->f);
            } catch(RuntimeException error) {
                tell(s.source,"A busca foi interrompida por uma falha ao consultar o chunk. O servidor continua ativo.");
                searches.remove();
            }
        }
    }
}
