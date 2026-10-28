package dev.minesla.djinn.client;

/** Continuous curves shared by the model and the offline animation checks. */
public final class RoyalPose {
    private RoyalPose() {}
    public static float smooth(float x) {
        x = Math.max(0, Math.min(1, x));
        return x * x * x * (x * (x * 6 - 15) + 10);
    }
    private static float sin(float x) { return (float)Math.sin(x); }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    public static float[] sample(float age, float swing, float remaining, int role, boolean king, float speed) {
        float[] p = new float[20];
        float breath = sin(age * .055f), wave = age * .075f;
        float travel = Math.min(1, Math.abs(speed));
        p[0] = breath * (king ? .75f : .95f);
        p[1] = sin(age * .028f) * .035f;
        p[2] = sin(age * .045f) * .018f - travel * .05f;
        p[3] = sin(age * .035f) * .025f;
        p[4] = -.20f + sin(age * .055f) * .055f;
        p[5] = -.08f; p[6] = -.13f - sin(age * .045f) * .035f;
        p[7] = (role == 2 ? -.75f : role == 1 ? -.45f : -.16f) - sin(age * .055f) * .055f;
        p[8] = .08f; p[9] = .13f + sin(age * .045f) * .035f;
        p[10] = sin(wave) * (.16f + travel * .08f);
        p[11] = sin(wave - .7f) * .24f;
        p[12] = sin(wave - 1.4f) * .31f;
        p[13] = sin(age * .025f) * .35f;
        p[14] = -.16f + sin(age * .065f) * .04f;
        p[15] = -.26f;
        p[16] = .10f + sin(age * .045f - .5f) * .07f + travel * .22f;
        p[17] = sin(age * .035f - .7f) * .045f;
        p[18] = -p[2] * .45f;
        p[19] = sin(wave - 2.1f) * .34f;

        if (remaining > 0) {
            float total = king ? 60 : 20;
            float partial = age - (float)Math.floor(age);
            float t = Math.max(0, remaining - partial);
            float cast = smooth((total - t) / 5) * smooth(t / 5);
            p[4] = lerp(p[4], -1.75f, cast); p[7] = lerp(p[7], -1.75f, cast);
            p[5] = lerp(p[5], -.22f, cast); p[8] = lerp(p[8], .22f, cast);
            p[6] = lerp(p[6], -.48f, cast); p[9] = lerp(p[9], .48f, cast);
            p[14] = lerp(p[14], -.32f, cast); p[15] = lerp(p[15], -.32f, cast);
            p[0] -= cast * .8f;
        }
        if (swing > 0 && swing < 1) {
            // Anticipation, diagonal cut and recovery; zero velocity at every key pose.
            float t = Math.max(0, Math.min(1, swing));
            float arm, twist, elbow, roll;
            if (t < .22f) {
                float q = smooth(t / .22f);
                arm = lerp(0, -1.50f, q); twist = lerp(0, -.40f, q);
                elbow = lerp(0, -.72f, q); roll = lerp(0, -.32f, q);
            } else if (t < .58f) {
                float q = smooth((t - .22f) / .36f);
                arm = lerp(-1.50f, -.38f, q); twist = lerp(-.40f, .56f, q);
                elbow = lerp(-.72f, -.12f, q); roll = lerp(-.32f, .68f, q);
            } else {
                float q = smooth((t - .58f) / .42f);
                arm = lerp(-.38f, 0, q); twist = lerp(.56f, 0, q);
                elbow = lerp(-.12f, 0, q); roll = lerp(.68f, 0, q);
            }
            p[4] += arm; p[5] += twist * 1.1f; p[6] += roll;
            p[14] += elbow; p[1] += twist;
            p[7] -= sin(t * (float)Math.PI) * .18f;
            p[16] += sin(t * (float)Math.PI) * .16f;
        }
        return p;
    }
}
