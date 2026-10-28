package dev.minesla.djinn.runtime;

import java.util.*;

/** Same linear random-spread formula as vanilla; actual starts are still verified. */
public final class LocatePlan {
    public static final int SPACING=36, SEPARATION=12, SALT=73110429, MAX_CANDIDATES=128;
    public record Candidate(int x,int z) {}
    public static List<Candidate> candidates(long seed,int originX,int originZ) {
        int rx=Math.floorDiv(originX,SPACING),rz=Math.floorDiv(originZ,SPACING);
        List<Candidate> candidates=new ArrayList<>();
        for(int x=rx-8;x<=rx+8;x++)for(int z=rz-8;z<=rz+8;z++) {
            Random random=new Random(seed+x*341873128712L+z*132897987541L+SALT);
            candidates.add(new Candidate(x*SPACING+random.nextInt(SPACING-SEPARATION),
                                         z*SPACING+random.nextInt(SPACING-SEPARATION)));
        }
        candidates.sort(Comparator.comparingLong(p->(long)(p.x-originX)*(p.x-originX)+(long)(p.z-originZ)*(p.z-originZ)));
        return List.copyOf(candidates.subList(0,MAX_CANDIDATES));
    }
}
