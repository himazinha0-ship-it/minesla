import dev.minesla.djinn.client.RoyalPose;
import dev.minesla.djinn.RoyalBalance;

public final class PoseChecks {
    static void require(boolean x,String why){if(!x)throw new AssertionError(why);}
    public static void main(String[] args){
        int samples=0;
        for(int role=0;role<4;role++)for(boolean king:new boolean[]{false,true})for(int frame=0;frame<=1200;frame++){
            float age=frame/3f,swing=(frame%181)/180f;
            for(float value:RoyalPose.sample(age,swing,frame%61,role,king,.4f))require(Float.isFinite(value)&&Math.abs(value)<6,"Non-finite/unbounded pose");
            samples++;
        }
        for(float key:new float[]{0,.22f,.58f,1}){
            float[] left=RoyalPose.sample(100,Math.max(0,key-.00001f),0,2,true,0);
            float[] right=RoyalPose.sample(100,Math.min(1,key+.00001f),0,2,true,0);
            for(int n=0;n<left.length;n++)require(Math.abs(left[n]-right[n])<.0003f,"Pose discontinuity at "+key+" channel "+n);
        }
        float[] idle=RoyalPose.sample(100,0,0,2,true,0),end=RoyalPose.sample(100,1,0,2,true,0);
        for(int n=0;n<idle.length;n++)require(Math.abs(idle[n]-end[n])<.000001f,"Swing does not return to idle");
        require(RoyalBalance.SWORD_DAMAGE==10&&RoyalBalance.SWING_TICKS==18,"Wrong balance");
        require(RoyalBalance.attackBase(11,true)+3+6==10,"King's sword does not total ten");
        require(RoyalBalance.attackBase(6,false)==6,"Guard damage changed");
        System.out.println("PASS: "+samples+" animation samples; continuous keyframes, idle recovery, 18-tick swing and ten-damage balance.");
    }
}
