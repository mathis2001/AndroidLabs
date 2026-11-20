package org.northstar.banklab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(12, 27, 49);
    private static final int INK = Color.rgb(31, 45, 65);
    private static final int MINT = Color.rgb(37, 209, 165);
    private static final int PALE = Color.rgb(242, 247, 249);
    private static final String ACCESS_PREFS = "northstar_access";
    private static LabHttpServer site;
    private enum PinMode { CREATE, CONFIRM, UNLOCK }

    private FrameLayout root;
    private LinearLayout offerPanel;
    private LinearLayout statementPanel;
    private LinearLayout documentsPanel;
    private WebView offerWeb;
    private WebView statementWeb;
    private Button offersButton;
    private FrameLayout lockPanel;
    private TextView pinTitle;
    private TextView pinSubtitle;
    private TextView pinDots;
    private TextView pinFeedback;
    private final StringBuilder pinInput = new StringBuilder();
    private String firstPin;
    private PinMode pinMode;
    private boolean unlocked;
    private Intent pendingIntent;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        buildHome();
        pendingIntent = getIntent();
        showPinScreen(hasPin() ? PinMode.UNLOCK : PinMode.CREATE, "");
        new Thread(() -> {
            String failure = null;
            try {
                synchronized (MainActivity.class) {
                    if (site == null) site = new LabHttpServer(getApplicationContext().getAssets());
                }
            } catch (Exception e) {
                failure = e.getMessage();
                Log.e("NorthstarBank", "Local server startup failed", e);
            }
            String error = failure;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                offersButton.setEnabled(error == null);
                offersButton.setText(error == null ? "Explore offers  →" : "Local site unavailable");
                if (error == null) processPendingIntent();
            });
        }, "bank-site-start").start();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        pendingIntent = intent;
        processPendingIntent();
    }

    @Override protected void onStop() {
        super.onStop();
        if (unlocked && hasPin()) {
            unlocked = false;
            closeDocuments();
            closeOffers();
            showPinScreen(PinMode.UNLOCK, "");
        }
    }

    private void processPendingIntent() {
        if (!unlocked || site == null || pendingIntent == null) return;
        Intent next = pendingIntent;
        pendingIntent = null;
        handleLink(next);
    }

    private void handleLink(Intent intent) {
        Uri data = intent.getData();
        if (data == null || !"northstar".equals(data.getScheme()) || !"offers".equals(data.getHost())) return;
        String target = data.getQueryParameter("url");
        if (target != null && target.startsWith(LabHttpServer.ORIGIN + "/rewards/")) {
            openOffers(target);
        } else {
            openOffers(LabHttpServer.ORIGIN + "/rewards/overview");
        }
    }

    private SharedPreferences accessPrefs() {
        return getSharedPreferences(ACCESS_PREFS, MODE_PRIVATE);
    }

    private boolean hasPin() {
        return accessPrefs().contains("digest");
    }

    private String pinDigest(String pin, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            digest.update(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest.digest(), Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void savePin(String pin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        accessPrefs().edit()
                .putString("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString("digest", pinDigest(pin, salt))
                .apply();
    }

    private boolean matchesPin(String pin) {
        String salt = accessPrefs().getString("salt", "");
        String expected = accessPrefs().getString("digest", "");
        if (salt.isEmpty() || expected.isEmpty()) return false;
        return expected.equals(pinDigest(pin, Base64.decode(salt, Base64.NO_WRAP)));
    }

    private void showPinScreen(PinMode mode, String message) {
        if (lockPanel != null) root.removeView(lockPanel);
        lockPanel = new FrameLayout(this);
        lockPanel.setBackgroundColor(PALE);
        root.addView(lockPanel, new FrameLayout.LayoutParams(-1, -1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        lockPanel.addView(scroll);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(28), dp(48), dp(28), dp(35));
        scroll.addView(page);

        page.addView(text("✦  NORTHSTAR", 22, NAVY, Typeface.BOLD));
        pinTitle = text("", 29, NAVY, Typeface.BOLD);
        page.addView(pinTitle, margins(0, dp(62)));
        pinSubtitle = text("", 15, INK, Typeface.NORMAL);
        page.addView(pinSubtitle, margins(0, dp(10)));
        pinDots = text("○  ○  ○  ○", 31, NAVY, Typeface.BOLD);
        pinDots.setGravity(Gravity.CENTER);
        page.addView(pinDots, margins(0, dp(35)));
        pinFeedback = text("", 14, Color.rgb(174, 48, 55), Typeface.NORMAL);
        pinFeedback.setGravity(Gravity.CENTER);
        page.addView(pinFeedback, margins(0, dp(12)));

        String[][] keys = {{"1", "2", "3"}, {"4", "5", "6"}, {"7", "8", "9"}, {"", "0", "⌫"}};
        for (String[] keyRow : keys) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            page.addView(row, margins(0, dp(10)));
            for (String key : keyRow) {
                Button button = new Button(this);
                button.setAllCaps(false);
                button.setText(key);
                button.setTextSize(23);
                button.setTextColor(NAVY);
                button.setBackground(round(Color.WHITE, dp(17)));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(68), 1);
                params.setMargins(dp(5), 0, dp(5), 0);
                row.addView(button, params);
                if (key.isEmpty()) button.setVisibility(View.INVISIBLE);
                else if ("⌫".equals(key)) button.setOnClickListener(v -> erasePinDigit());
                else button.setOnClickListener(v -> enterPinDigit(key));
            }
        }
        setPinMode(mode, message);
    }

    private void setPinMode(PinMode mode, String message) {
        pinMode = mode;
        pinInput.setLength(0);
        pinTitle.setText(mode == PinMode.CREATE ? "Create your PIN" : mode == PinMode.CONFIRM ? "Confirm your PIN" : "Welcome back");
        pinSubtitle.setText(mode == PinMode.UNLOCK ? "Enter your four-digit access PIN." : "Choose four digits to protect your account view.");
        pinFeedback.setText(message);
        updatePinDots();
    }

    private void updatePinDots() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) dots.append("  ");
            dots.append(i < pinInput.length() ? '●' : '○');
        }
        pinDots.setText(dots.toString());
    }

    private void enterPinDigit(String digit) {
        if (pinInput.length() >= 4) return;
        pinInput.append(digit);
        updatePinDots();
        if (pinInput.length() != 4) return;
        String entered = pinInput.toString();
        if (pinMode == PinMode.CREATE) {
            firstPin = entered;
            setPinMode(PinMode.CONFIRM, "");
        } else if (pinMode == PinMode.CONFIRM) {
            if (entered.equals(firstPin)) {
                savePin(entered);
                firstPin = null;
                unlock();
            } else {
                firstPin = null;
                setPinMode(PinMode.CREATE, "PINs did not match. Try again.");
            }
        } else if (matchesPin(entered)) {
            unlock();
        } else {
            setPinMode(PinMode.UNLOCK, "Incorrect PIN. Try again.");
        }
    }

    private void erasePinDigit() {
        if (pinInput.length() == 0) return;
        pinInput.deleteCharAt(pinInput.length() - 1);
        updatePinDots();
    }

    private void unlock() {
        unlocked = true;
        root.removeView(lockPanel);
        lockPanel = null;
        processPendingIntent();
    }

    private void buildHome() {
        root = new FrameLayout(this);
        root.setBackgroundColor(PALE);
        setContentView(root);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root.addView(scroll);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(25), dp(22), dp(30));
        scroll.addView(page);

        TextView brand = text("✦  NORTHSTAR", 21, NAVY, Typeface.BOLD);
        page.addView(brand);
        TextView greeting = text("Good morning, Alex", 16, INK, Typeface.NORMAL);
        LinearLayout.LayoutParams greetingParams = margins(dp(0), dp(22));
        page.addView(greeting, greetingParams);

        LinearLayout balance = card(NAVY, dp(22));
        page.addView(balance, margins(dp(0), dp(18)));
        balance.addView(text("TOTAL BALANCE", 12, MINT, Typeface.BOLD));
        balance.addView(text("€ 12,480.75", 34, Color.WHITE, Typeface.BOLD), margins(dp(0), dp(10)));
        balance.addView(text("Current account  ••  4819", 14, Color.rgb(183, 203, 217), Typeface.NORMAL), margins(dp(0), dp(10)));

        page.addView(text("Quick actions", 19, NAVY, Typeface.BOLD), margins(dp(0), dp(26)));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        page.addView(actions, margins(dp(0), dp(12)));
        action(actions, "↗", "Transfer", () -> notice("Transfers are coming soon."));
        action(actions, "◫", "Cards", () -> notice("Card controls are coming soon."));
        action(actions, "▤", "Statements", this::openDocuments);

        page.addView(text("Latest activity", 19, NAVY, Typeface.BOLD), margins(dp(0), dp(30)));
        transaction(page, "Café Marais", "Today · Card payment", "− € 8.40");
        transaction(page, "Salary payment", "Yesterday · Incoming", "+ € 3,200.00");
        transaction(page, "Metro pass", "18 Nov · Direct debit", "− € 86.40");

        LinearLayout promo = card(Color.WHITE, dp(20));
        page.addView(promo, margins(dp(0), dp(26)));
        promo.addView(text("A little more for you", 18, NAVY, Typeface.BOLD));
        promo.addView(text("See the latest Northstar offers and rewards.", 14, INK, Typeface.NORMAL), margins(dp(0), dp(8)));
        offersButton = new Button(this);
        offersButton.setAllCaps(false);
        offersButton.setText("Loading offers…");
        offersButton.setTextColor(NAVY);
        offersButton.setBackground(round(MINT, dp(14)));
        offersButton.setEnabled(false);
        offersButton.setOnClickListener(v -> openOffers(LabHttpServer.ORIGIN + "/rewards/overview"));
        promo.addView(offersButton, margins(dp(0), dp(15)));
    }

    private void action(LinearLayout parent, String icon, String name, Runnable onClick) {
        LinearLayout item = card(Color.WHITE, dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(85), 1);
        params.setMargins(dp(0), 0, dp(8), 0);
        parent.addView(item, params);
        item.addView(text(icon, 25, NAVY, Typeface.BOLD));
        item.addView(text(name, 13, INK, Typeface.NORMAL));
        item.setOnClickListener(v -> onClick.run());
    }

    private void notice(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void transaction(LinearLayout page, String name, String detail, String amount) {
        LinearLayout row = card(Color.WHITE, dp(15));
        page.addView(row, margins(dp(0), dp(8)));
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(top);
        TextView title = text(name, 15, NAVY, Typeface.BOLD);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(text(amount, 15, amount.startsWith("+") ? Color.rgb(22, 137, 99) : INK, Typeface.BOLD));
        row.addView(text(detail, 12, INK, Typeface.NORMAL), margins(dp(0), dp(4)));
    }

    private void openDocuments() {
        if (!unlocked) return;
        closeDocuments();
        documentsPanel = webPanel("Documents", this::closeDocuments);
        root.addView(documentsPanel, new FrameLayout.LayoutParams(-1, -1));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(PALE);
        documentsPanel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(30));
        scroll.addView(page);
        page.addView(text("Statements & documents", 26, NAVY, Typeface.BOLD));
        page.addView(text("Your account documents, all in one place.", 15, INK, Typeface.NORMAL), margins(0, dp(9)));

        LinearLayout item = card(Color.WHITE, dp(20));
        page.addView(item, margins(0, dp(25)));
        item.addView(text("CURRENT ACCOUNT  ····  4819", 12, Color.rgb(22, 137, 99), Typeface.BOLD));
        item.addView(text("November statement", 21, NAVY, Typeface.BOLD), margins(0, dp(12)));
        item.addView(text("1–30 November · Available now", 14, INK, Typeface.NORMAL), margins(0, dp(7)));
        Button open = new Button(this);
        open.setAllCaps(false);
        open.setText("Open statement  →");
        open.setTextColor(NAVY);
        open.setBackground(round(MINT, dp(14)));
        open.setOnClickListener(v -> openStatement(null));
        item.addView(open, margins(0, dp(18)));
    }

    private void closeDocuments() {
        if (documentsPanel == null) return;
        closeStatement();
        root.removeView(documentsPanel);
        documentsPanel = null;
    }

    private void openOffers(String url) {
        if (!unlocked) return;
        closeDocuments();
        closeOffers();
        offerPanel = webPanel("Offers", () -> closeOffers());
        root.addView(offerPanel, new FrameLayout.LayoutParams(-1, -1));
        offerWeb = new WebView(this);
        offerWeb.setBackgroundColor(Color.WHITE);
        offerWeb.getSettings().setJavaScriptEnabled(true);
        offerWeb.getSettings().setSupportMultipleWindows(true);
        offerWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        offerWeb.getSettings().setDomStorageEnabled(true);
        offerWeb.setWebViewClient(new WebViewClient());
        offerWeb.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (view != offerWeb || statementPanel != null) return false;
                openStatement(resultMsg);
                return true;
            }
        });
        offerPanel.addView(offerWeb, new LinearLayout.LayoutParams(-1, 0, 1));
        offerWeb.loadUrl(url);
    }

    private void openStatement(Message resultMsg) {
        if (!unlocked) return;
        closeStatement();
        statementPanel = webPanel("Private statement", () -> closeStatement());
        root.addView(statementPanel, new FrameLayout.LayoutParams(-1, -1));
        statementWeb = new WebView(this);
        statementWeb.setBackgroundColor(Color.WHITE);
        statementWeb.getSettings().setJavaScriptEnabled(true);
        statementWeb.getSettings().setDomStorageEnabled(true);
        statementWeb.setWebViewClient(new WebViewClient() {
            private boolean bridgeInstalled;

            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !LabHttpServer.STATEMENT_URL.equals(request.getUrl().toString());
            }

            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                if (!LabHttpServer.STATEMENT_URL.equals(url)) {
                    view.removeJavascriptInterface("StatementService");
                    bridgeInstalled = false;
                }
            }

            @Override public void onPageFinished(WebView view, String url) {
                if (LabHttpServer.STATEMENT_URL.equals(url) && !bridgeInstalled) {
                    view.addJavascriptInterface(new StatementService(), "StatementService");
                    bridgeInstalled = true;
                    view.reload();
                }
            }
        });
        statementPanel.addView(statementWeb, new LinearLayout.LayoutParams(-1, 0, 1));
        if (resultMsg == null) {
            statementWeb.loadUrl(LabHttpServer.STATEMENT_URL);
        } else {
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(statementWeb);
            resultMsg.sendToTarget();
        }
    }

    private LinearLayout webPanel(String title, Runnable back) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Color.WHITE);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(12), 0, dp(18), 0);
        bar.setBackgroundColor(NAVY);
        panel.addView(bar, new LinearLayout.LayoutParams(-1, dp(56)));
        TextView backButton = text("‹", 35, Color.WHITE, Typeface.NORMAL);
        backButton.setGravity(Gravity.CENTER);
        bar.addView(backButton, new LinearLayout.LayoutParams(dp(44), -1));
        backButton.setOnClickListener(v -> back.run());
        bar.addView(text(title, 18, Color.WHITE, Typeface.BOLD));
        return panel;
    }

    private void closeStatement() {
        if (statementWeb != null) {
            statementPanel.removeView(statementWeb);
            statementWeb.destroy();
            statementWeb = null;
        }
        if (statementPanel != null) {
            root.removeView(statementPanel);
            statementPanel = null;
        }
    }

    private void closeOffers() {
        closeStatement();
        if (offerWeb != null) {
            offerPanel.removeView(offerWeb);
            offerWeb.destroy();
            offerWeb = null;
        }
        if (offerPanel != null) {
            root.removeView(offerPanel);
            offerPanel = null;
        }
    }

    @Override public void onBackPressed() {
        if (!unlocked) moveTaskToBack(true);
        else if (statementPanel != null) closeStatement();
        else if (documentsPanel != null) closeDocuments();
        else if (offerPanel != null) closeOffers();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        closeDocuments();
        closeOffers();
        super.onDestroy();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private LinearLayout.LayoutParams margins(int left, int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(left, top, 0, 0);
        return params;
    }

    private TextView text(String content, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(content);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private LinearLayout card(int color, int padding) {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(padding, padding, padding, padding);
        view.setBackground(round(color, dp(19)));
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private static final class StatementService {
        @JavascriptInterface public String flag() {
            int[] encoded = {60,13,9,8,13,53,181,225,243,250,203,248,227,134,227,242,172,142,141,128,165,217,154,134};
            char[] result = new char[encoded.length];
            for (int i = 0; i < encoded.length; i++) result[i] = (char) (encoded[i] ^ ((0x5a + i * 7) & 0xff));
            return new String(result);
        }
    }
}
