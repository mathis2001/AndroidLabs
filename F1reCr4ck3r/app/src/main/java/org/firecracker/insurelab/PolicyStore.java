package org.firecracker.insurelab;

import android.content.Context;

final class PolicyStore {
    static boolean signedIn(Context context) {
        return context.getSharedPreferences("demo-policy", 0).getBoolean("signed_in", false);
    }

    static void signIn(Context context) {
        context.getSharedPreferences("demo-policy", 0).edit().putBoolean("signed_in", true).apply();
    }

    static void signOut(Context context) {
        context.getSharedPreferences("demo-policy", 0).edit().putBoolean("signed_in", false).apply();
    }

    static int discountPercent(Context context) {
        return context.getSharedPreferences("demo-policy", 0).getInt("discount_percent", 0);
    }

    static int applyDiscount(Context context, int percent) {
        int effective = Math.max(discountPercent(context), percent);
        context.getSharedPreferences("demo-policy", 0).edit()
            .putInt("discount_percent", effective).apply();
        return effective;
    }

    private PolicyStore() { }
}
