package org.firecracker.insurelab;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class RemoteConfig {
    interface Callback { void onResult(RemoteConfig config, boolean live); }

    static final class Card {
        final String deeplink, icon, longDescription, longTitle, quotationUrl;
        final String shortDescription, shortTitle;
        final boolean enabled;

        Card(String deeplink, String icon, String longDescription, String longTitle,
             String quotationUrl, String shortDescription, String shortTitle, boolean enabled) {
            this.deeplink = deeplink;
            this.icon = icon;
            this.longDescription = longDescription;
            this.longTitle = longTitle;
            this.quotationUrl = quotationUrl;
            this.shortDescription = shortDescription;
            this.shortTitle = shortTitle;
            this.enabled = enabled;
        }
    }

    static final class EmployeeDiscount {
        final boolean enabled;
        final int percent;
        final String externalDeeplink;

        EmployeeDiscount(boolean enabled, int percent, String externalDeeplink) {
            this.enabled = enabled;
            this.percent = percent;
            this.externalDeeplink = externalDeeplink;
        }
    }

    static final class VipOffer {
        final boolean enabled;
        final int percent;
        final String internalDeeplink;

        VipOffer(boolean enabled, int percent, String internalDeeplink) {
            this.enabled = enabled;
            this.percent = percent;
            this.internalDeeplink = internalDeeplink;
        }
    }

    final boolean maintenanceMode, claimsEnabled, roadsideEnabled, paydEnabled;
    final Card card;
    final EmployeeDiscount employeeDiscount;
    final VipOffer vipOffer;

    private RemoteConfig(boolean maintenanceMode, boolean claimsEnabled,
                         boolean roadsideEnabled, boolean paydEnabled, Card card,
                         EmployeeDiscount employeeDiscount, VipOffer vipOffer) {
        this.maintenanceMode = maintenanceMode;
        this.claimsEnabled = claimsEnabled;
        this.roadsideEnabled = roadsideEnabled;
        this.paydEnabled = paydEnabled;
        this.card = card;
        this.employeeDiscount = employeeDiscount;
        this.vipOffer = vipOffer;
    }

    static RemoteConfig defaults() {
        Card card = new Card("f1recr4ck3r://payd/quote", "🚗",
            "Drive less? Your insurance should reflect it. Get an indicative price based on your expected annual mileage, with the same cover and support you expect from Firecracker Motor.",
            "Cover that follows your mileage",
            "https://quote.f1recr4ck3r.example/payd",
            "Flexible cover for drivers who don't rack up the miles.",
            "Pay As You Drive", true);
        return new RemoteConfig(false, true, true, true, card,
            new EmployeeDiscount(false, 0, ""), new VipOffer(false, 0, ""));
    }

    static void fetch(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        new Thread(() -> {
            app.deleteSharedPreferences("remote-config");
            RemoteConfig config = defaults();
            boolean live = false;
            HttpURLConnection connection = null;
            try {
                int id = app.getResources().getIdentifier("firebase_database_url", "string", app.getPackageName());
                if (id == 0) throw new IOException("Database URL unavailable");
                String base = app.getString(id);
                URL endpoint = new URL(base + (base.endsWith("/") ? "" : "/") + "remote-config.json");
                connection = (HttpURLConnection) endpoint.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                if (connection.getResponseCode() != 200) throw new IOException("Config unavailable");
                try (InputStream input = connection.getInputStream()) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        if (output.size() + count > 131072) throw new IOException("Config too large");
                        output.write(buffer, 0, count);
                    }
                    JSONObject object = new JSONObject(new String(output.toByteArray(), StandardCharsets.UTF_8));
                    config = fromJson(object);
                    live = true;
                }
            } catch (Exception ignored) { }
            finally { if (connection != null) connection.disconnect(); }
            RemoteConfig result = config;
            boolean fromNetwork = live;
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result, fromNetwork));
        }, "remote-config-fetch").start();
    }

    private static RemoteConfig fromJson(JSONObject value) {
        RemoteConfig fallback = defaults();
        JSONObject card = value.optJSONObject("login_payd_card");
        if (card == null) card = new JSONObject();
        Card item = new Card(
            card.optString("deeplink", fallback.card.deeplink),
            card.optString("icon", fallback.card.icon),
            card.optString("long_description", fallback.card.longDescription),
            card.optString("long_title", fallback.card.longTitle),
            card.optString("payd_quotation_url", fallback.card.quotationUrl),
            card.optString("short_description", fallback.card.shortDescription),
            card.optString("short_title", fallback.card.shortTitle),
            card.optBoolean("enabled", fallback.card.enabled));
        JSONObject discount = value.optJSONObject("employee_coupon");
        if (discount == null) discount = new JSONObject();
        EmployeeDiscount employee = new EmployeeDiscount(
            discount.optBoolean("enabled", false),
            discount.optInt("discount_percent", 0),
            discount.optString("external_deeplink", ""));
        JSONObject vipData = value.optJSONObject("vip_offer");
        if (vipData == null) vipData = new JSONObject();
        VipOffer vip = new VipOffer(
            vipData.optBoolean("enabled", false),
            vipData.optInt("discount_percent", 0),
            vipData.optString("internal_deeplink", ""));
        return new RemoteConfig(
            value.optBoolean("maintenance_mode", fallback.maintenanceMode),
            value.optBoolean("claims_enabled", fallback.claimsEnabled),
            value.optBoolean("roadside_assistance_enabled", fallback.roadsideEnabled),
            value.optBoolean("payd_enabled", fallback.paydEnabled), item, employee, vip);
    }

}
