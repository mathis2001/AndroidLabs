package org.firecracker.insurelab;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public final class EmployeeOfferActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (!PolicyStore.signedIn(this)) {
            Intent login = new Intent(this, LoginActivity.class);
            login.putExtra("pending_offer_link", getIntent().getDataString());
            startActivity(login);
            finish();
            return;
        }
        OfferResultUi.loading(this, "Checking your offer", "Please wait while we verify this link.");
        RemoteConfig.fetch(this, (config, live) -> {
            if (isFinishing() || isDestroyed()) return;
            RemoteConfig.EmployeeDiscount discount = config.employeeDiscount;
            Uri incoming = getIntent().getData();
            Uri published = Uri.parse(discount.externalDeeplink);
            if (!discount.enabled || discount.percent != 25 || incoming == null ||
                !"f1recr4ck3r".equals(incoming.getScheme()) ||
                !"offers".equals(incoming.getHost()) ||
                !"/employee".equals(incoming.getPath()) ||
                published.getQueryParameter("campaign") == null ||
                !published.getQueryParameter("campaign").equals(incoming.getQueryParameter("campaign"))) {
                OfferResultUi.show(this, "Offer unavailable", "This campaign link could not be verified.", false);
                return;
            }
            if (!incoming.toString().equals(discount.externalDeeplink)) {
                OfferResultUi.show(this, "Offer unavailable", "This campaign link could not be verified.", false);
                return;
            }
            String flag = Flags.employee(incoming.getQueryParameter("campaign"));
            if (flag == null) {
                OfferResultUi.show(this, "Offer unavailable", "This campaign link could not be verified.", false);
                return;
            }
            int effective = PolicyStore.applyDiscount(this, discount.percent);
            if (effective > discount.percent) {
                OfferResultUi.show(this, "Your VIP saving remains active",
                    "Your monthly premium is still €32 on policy FM-2084-7712.\n\n" + flag, true);
            } else {
                OfferResultUi.show(this, "25% employee saving applied",
                    "Your monthly premium is now €48 on policy FM-2084-7712.\n\n" + flag, true);
            }
        });
    }
}
