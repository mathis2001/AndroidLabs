package org.firecracker.insurelab;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public final class VipOfferActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (!PolicyStore.signedIn(this)) {
            OfferResultUi.show(this, "Sign in to continue",
                "Sign in to your policy before using this offer.", false);
            return;
        }
        Uri incoming = getIntent().getData();
        OfferResultUi.loading(this, "Checking your VIP offer", "Please wait while we verify this offer.");
        RemoteConfig.fetch(this, (config, live) -> {
            if (isFinishing() || isDestroyed()) return;
            RemoteConfig.VipOffer vip = config.vipOffer;
            if (!vip.enabled || vip.percent != 50 || incoming == null ||
                !incoming.toString().equals(vip.internalDeeplink) ||
                incoming.getQueryParameter("offer") == null) {
                OfferResultUi.show(this, "Offer unavailable", "This VIP offer could not be applied.", false);
                return;
            }
            String flag = Flags.vip(incoming.getQueryParameter("offer"));
            if (flag == null) {
                OfferResultUi.show(this, "Offer unavailable", "This VIP offer could not be applied.", false);
                return;
            }
            PolicyStore.applyDiscount(this, vip.percent);
            OfferResultUi.show(this, "50% VIP saving applied",
                "Your monthly premium is now €32 on policy FM-2084-7712.\n\n" + flag, true);
        });
    }
}
