package org.omarchy.tvhome;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends Activity {

    // Omarchy Osaka Jade Palette
    private static final int COL_BG_DARKER = 0xFF090F0D;
    private static final int COL_BG_DARK   = 0xEB0C1512;
    private static final int COL_BG_PANEL  = 0xE0111C18;
    private static final int COL_BG_TILE   = 0xEB15241E;
    private static final int COL_BG_FOCUS  = 0xFF23372B;
    private static final int COL_SELECTION = 0xFF32473B;
    private static final int COL_MUTED     = 0xFF53685B;
    private static final int COL_ACCENT    = 0xFF509475;
    private static final int COL_CYAN      = 0xFF2DD5B7;
    private static final int COL_GREEN     = 0xFF549E6A;
    private static final int COL_FG        = 0xFFC1C497;
    private static final int COL_FG_LIGHT  = 0xFFD6D5BC;
    private static final int COL_FG_BRIGHT = 0xFFF7E8B2;
    private static final int COL_ORANGE    = 0xFFA2734B;
    private static final int COL_RED       = 0xFFFF5345;

    private static final String PREFS = "omarchy_tv_prefs";
    private static final String TCL_SHOW_WINDOW = "com.tcl.settings.SHOW_WINDOW";
    private static final String INPUT_PLAYER = "com.google.android.tv.inputplayer";
    private static final List<String> DEFAULT_APPS = Arrays.asList(
            "com.netflix.ninja",
            "com.google.android.youtube.tv",
            "com.amazon.amazonvideo.livingroom",
            "com.globo.globotv",
            "com.disney.disneyplus",
            "com.crunchyroll.crunchyroid",
            "org.videolan.vlc",
            "com.android.vending"
    );
    private static final List<String> HIDDEN_PACKAGES = Arrays.asList(
            "com.android.tv.settings",
            "com.android.tv",
            "com.google.android.tvlauncher",
            "com.google.android.leanbacklauncher",
            "com.ansub.tvhome",
            "org.omarchy.tvhome"
    );
    private static final String[] BG_ASSETS = {"bg1.jpg", "bg2.jpg", "bg3.jpg", "solid"};
    private static final String[] BG_NAMES = {
            "1: Glowing City (Osaka Jade)",
            "2: Shaded Entrance (Osaka Jade)",
            "3: Mountain Moon (Osaka Jade)",
            "4: Pure Terminal (#090f0d)"
    };

    private static final long IDLE_TIMEOUT_MS = 40000L;
    private static final long IDLE_FADE_DURATION_MS = 3500L;
    private static final long VERSE_ROTATE_MS = 20000L;

    private static final String[][] BIBLE_VERSES = {
            {"\"O Senhor é o meu pastor; nada me faltará.\"", "Salmos 23:1"},
            {"\"Tudo posso naquele que me fortalece.\"", "Filipenses 4:13"},
            {"\"Entrega o teu caminho ao Senhor; confia nele, e ele tudo fará.\"", "Salmos 37:5"},
            {"\"O Senhor te abençoe e te guarde; o Senhor faça resplandecer o seu rosto sobre ti.\"", "Números 6:24-25"},
            {"\"Porque sou eu que conheço os planos que tenho para vocês, diz o Senhor, planos de paz.\"", "Jeremias 29:11"},
            {"\"Deixo-vos a paz, a minha paz vos dou; não vo-la dou como o mundo a dá.\"", "João 14:27"},
            {"\"Lâmpada para os meus pés é tua palavra e luz, para o meu caminho.\"", "Salmos 119:105"},
            {"\"Buscai primeiro o Reino de Deus, e a sua justiça, e todas estas coisas vos serão acrescentadas.\"", "Mateus 6:33"},
            {"\"Esforça-te, e tem bom ânimo; não pasmes, nem te espantes, porque o Senhor teu Deus é contigo.\"", "Josué 1:9"},
            {"\"O choro pode durar uma noite, mas a alegria vem pela manhã.\"", "Salmos 30:5"},
            {"\"Mas os que esperam no Senhor renovarão as suas forças; subirão com asas como águias.\"", "Isaías 40:31"},
            {"\"Sabemos que todas as coisas contribuem juntamente para o bem daqueles que amam a Deus.\"", "Romanos 8:28"},
            {"\"O amor é paciente, o amor é bondoso. Não inveja, não se vangloria, não se orgulha.\"", "1 Coríntios 13:4"},
            {"\"Deus é o nosso refúgio e fortaleza, socorro bem presente na angústia.\"", "Salmos 46:1"},
            {"\"Aquietai-vos e sabei que eu sou Deus.\"", "Salmos 46:10"},
            {"\"Lancem sobre ele toda a sua ansiedade, porque ele tem cuidado de vocês.\"", "1 Pedro 5:7"},
            {"\"Alegrem-se sempre no Senhor. Novamente direi: alegrem-se!\"", "Filipenses 4:4"},
            {"\"O coração alegre aformoseia o rosto, mas pela dor do coração o espírito se abate.\"", "Provérbios 15:13"},
            {"\"Confia no Senhor de todo o teu coração e não te estribes no teu próprio entendimento.\"", "Provérbios 3:5"},
            {"\"Aquele que habita no esconderijo do Altíssimo, à sombra do Onipotente descansará.\"", "Salmos 91:1"},
            {"\"Eu sou o caminho, a verdade e a vida. Ninguém vem ao Pai, a não ser por mim.\"", "João 14:6"},
            {"\"Se Deus é por nós, quem será contra nós?\"", "Romanos 8:31"},
            {"\"Ensina-nos a contar os nossos dias, de tal maneira que alcancemos corações sábios.\"", "Salmos 90:12"},
            {"\"Provai e vede que o Senhor é bom; bem-aventurado o homem que nele confia.\"", "Salmos 34:8"}
    };

    private float scale = 1.0f;
    private Typeface monoRegular;
    private Typeface monoBold;

    private FrameLayout rootLayout;
    private View wallpaperDimOverlay;
    private LinearLayout masterVertical;
    private GridLayout appGrid;
    private LinearLayout quickRow;
    private LinearLayout versePopupCard;
    private TextView verseBodyText;
    private TextView verseRefText;

    private boolean isIdleAmbient = false;
    private int lastVerseIndex = -1;
    private final Random random = new Random();

    // Top bar & btop telemetry views
    private TextView topNetText;
    private TextView btopUptimeView;
    private TextView btopRamText;
    private ProgressBarView btopRamBar;
    private TextView btopSwapText;
    private ProgressBarView btopSwapBar;
    private TextView btopDiskText;
    private ProgressBarView btopDiskBar;
    private TextView btopNetIpText;
    private TextView btopProcsText;
    private SparklineView btopSparkline;

    private final List<String> pinnedApps = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Float> memHistory = new ArrayList<>();
    private String lastFocusedTag = null;

    private final Runnable statsUpdater = new Runnable() {
        @Override
        public void run() {
            updateSystemTelemetry();
            handler.postDelayed(this, 2500);
        }
    };

    private final Runnable enterIdleRunnable = new Runnable() {
        @Override
        public void run() {
            enterAmbientMode();
        }
    };

    private final Runnable rotateVerseRunnable = new Runnable() {
        @Override
        public void run() {
            if (isIdleAmbient) {
                crossfadeRandomVerse();
                handler.postDelayed(this, VERSE_ROTATE_MS);
            }
        }
    };

    private final BroadcastReceiver pkgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadPinnedApps();
            rebuildAppGrid();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scale = getResources().getDisplayMetrics().widthPixels / 1920.0f;
        initFonts();
        loadPinnedApps();

        rootLayout = new FrameLayout(this);

        // Dim overlay over the background image so that when idle happens, the wallpaper shines at 100% brightness
        wallpaperDimOverlay = new View(this);
        wallpaperDimOverlay.setBackgroundColor(0x88090F0D);
        rootLayout.addView(wallpaperDimOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        applyWallpaper(getWallpaperIndex());

        masterVertical = new LinearLayout(this);
        masterVertical.setOrientation(LinearLayout.VERTICAL);
        masterVertical.setPadding(px(36), px(24), px(36), px(28));

        // 1. OMARCHY WAYBAR (TOP BAR)
        masterVertical.addView(buildTopBar(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(58)));

        // 2. MAIN HYPRLAND TILING AREA (2 WINDOWS)
        LinearLayout tilingArea = new LinearLayout(this);
        tilingArea.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tilingLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        tilingLp.topMargin = px(22);
        masterVertical.addView(tilingArea, tilingLp);

        // Left Tiling Window: Apps + Quick Inputs (Weight 66%)
        LinearLayout leftPane = buildLeftWorkspaceWindow();
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 0.66f);
        leftLp.rightMargin = px(22);
        tilingArea.addView(leftPane, leftLp);

        // Right Tiling Window: btop++ System Telemetry + Omarchy Actions (Weight 34%)
        LinearLayout rightPane = buildRightBtopWindow();
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 0.34f);
        tilingArea.addView(rightPane, rightLp);

        // 3. BOTTOM STATUS / KEYBINDINGS HINT BAR
        masterVertical.addView(buildBottomStatusBar(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(40)));

        rootLayout.addView(masterVertical, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 4. BOTTOM-RIGHT BIBLE VERSE AMBIENT POP-UP CARD
        versePopupCard = buildBibleVersePopup();
        FrameLayout.LayoutParams verseLp = new FrameLayout.LayoutParams(
                px(620), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.END);
        verseLp.rightMargin = px(44);
        verseLp.bottomMargin = px(40);
        rootLayout.addView(versePopupCard, verseLp);

        setContentView(rootLayout);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(pkgReceiver, filter);

        rebuildAppGrid();
        rebuildQuickBar();
        resetIdleTimer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("preview_idle", false)) {
            enterAmbientMode();
        } else {
            wakeFromAmbientMode();
            resetIdleTimer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(statsUpdater);
        handler.post(statsUpdater);
        resetIdleTimer();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(statsUpdater);
        handler.removeCallbacks(enterIdleRunnable);
        handler.removeCallbacks(rotateVerseRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(statsUpdater);
        handler.removeCallbacks(enterIdleRunnable);
        handler.removeCallbacks(rotateVerseRunnable);
        try {
            unregisterReceiver(pkgReceiver);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isIdleAmbient) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                wakeFromAmbientMode();
            }
            resetIdleTimer();
            return true;
        }
        resetIdleTimer();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (isIdleAmbient) {
            wakeFromAmbientMode();
        }
        resetIdleTimer();
    }

    private void resetIdleTimer() {
        handler.removeCallbacks(enterIdleRunnable);
        handler.postDelayed(enterIdleRunnable, IDLE_TIMEOUT_MS);
    }

    private void enterAmbientMode() {
        if (isIdleAmbient) return;
        isIdleAmbient = true;
        pickNewRandomVerse();

        startSmoothFade(masterVertical, masterVertical.getAlpha(), 0f, IDLE_FADE_DURATION_MS, 0L, null);
        if (wallpaperDimOverlay != null) {
            startSmoothFade(wallpaperDimOverlay, wallpaperDimOverlay.getAlpha(), 0.08f, IDLE_FADE_DURATION_MS, 0L, null);
        }

        versePopupCard.setVisibility(View.VISIBLE);
        versePopupCard.setAlpha(0f);
        startSmoothFade(versePopupCard, 0f, 0.94f, 1600L, 900L, null);

        handler.removeCallbacks(rotateVerseRunnable);
        handler.postDelayed(rotateVerseRunnable, VERSE_ROTATE_MS);
    }

    private void wakeFromAmbientMode() {
        if (!isIdleAmbient) return;
        isIdleAmbient = false;
        handler.removeCallbacks(rotateVerseRunnable);

        startSmoothFade(masterVertical, masterVertical.getAlpha(), 1f, 220L, 0L, null);
        if (wallpaperDimOverlay != null) {
            startSmoothFade(wallpaperDimOverlay, wallpaperDimOverlay.getAlpha(), 1f, 220L, 0L, null);
        }
        startSmoothFade(versePopupCard, versePopupCard.getAlpha(), 0f, 180L, 0L, new Runnable() {
            @Override
            public void run() {
                if (!isIdleAmbient) {
                    versePopupCard.setVisibility(View.GONE);
                }
            }
        });
    }

    private void startSmoothFade(final View target, final float fromAlpha, final float toAlpha,
                                 final long durationMs, final long delayMs, final Runnable onEnd) {
        if (target == null) return;
        Object oldTask = target.getTag(0x7f000001);
        if (oldTask instanceof Runnable) {
            handler.removeCallbacks((Runnable) oldTask);
        }
        final long startTime = SystemClock.uptimeMillis() + delayMs;
        Runnable stepper = new Runnable() {
            @Override
            public void run() {
                long now = SystemClock.uptimeMillis();
                if (now < startTime) {
                    handler.postDelayed(this, 16L);
                    return;
                }
                float t = durationMs <= 0 ? 1f : Math.min(1f, (float) (now - startTime) / (float) durationMs);
                // Smooth cubic ease-in-out
                float eased = t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
                target.setAlpha(fromAlpha + (toAlpha - fromAlpha) * eased);
                if (t < 1f) {
                    handler.postDelayed(this, 16L);
                } else if (onEnd != null) {
                    onEnd.run();
                }
            }
        };
        target.setTag(0x7f000001, stepper);
        handler.post(stepper);
    }

    private LinearLayout buildBibleVersePopup() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xD8090F0D);
        bg.setCornerRadius(px(10));
        bg.setStroke(Math.max(1, px(2)), COL_ACCENT);
        card.setBackground(bg);
        card.setPadding(px(22), px(16), px(22), px(16));
        card.setVisibility(View.GONE);
        card.setAlpha(0f);

        TextView header = monoText("┌─ ❖ palavra ───────────────────────────────", 15, COL_CYAN, true);
        header.setPadding(0, 0, 0, px(6));
        card.addView(header);

        verseBodyText = monoText("", 18, COL_FG_BRIGHT, false);
        verseBodyText.setLineSpacing(px(4), 1.0f);
        card.addView(verseBodyText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        verseRefText = monoText("", 16, COL_ACCENT, true);
        verseRefText.setGravity(Gravity.END);
        LinearLayout.LayoutParams refLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        refLp.topMargin = px(8);
        card.addView(verseRefText, refLp);

        return card;
    }

    private void pickNewRandomVerse() {
        if (BIBLE_VERSES.length == 0 || verseBodyText == null || verseRefText == null) return;
        int idx = random.nextInt(BIBLE_VERSES.length);
        if (BIBLE_VERSES.length > 1 && idx == lastVerseIndex) {
            idx = (idx + 1) % BIBLE_VERSES.length;
        }
        lastVerseIndex = idx;
        verseBodyText.setText(BIBLE_VERSES[idx][0]);
        verseRefText.setText("— " + BIBLE_VERSES[idx][1]);
    }

    private void crossfadeRandomVerse() {
        if (versePopupCard == null || !isIdleAmbient) return;
        startSmoothFade(versePopupCard, versePopupCard.getAlpha(), 0f, 600L, 0L, new Runnable() {
            @Override
            public void run() {
                if (!isIdleAmbient) return;
                pickNewRandomVerse();
                startSmoothFade(versePopupCard, 0f, 0.94f, 800L, 0L, null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Home screen ignores Back button
    }

    private void initFonts() {
        monoRegular = Typeface.MONOSPACE;
        monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
        try {
            File f = new File("/system/fonts/DroidSansMono.ttf");
            if (f.exists()) {
                monoRegular = Typeface.createFromFile(f);
                monoBold = Typeface.create(monoRegular, Typeface.BOLD);
            }
        } catch (Exception ignored) {
        }
    }

    private View buildTopBar() {
        FrameLayout bar = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COL_BG_DARK);
        bg.setCornerRadius(px(8));
        bg.setStroke(Math.max(1, px(2)), COL_ACCENT);
        bar.setBackground(bg);
        bar.setPadding(px(20), 0, px(20), 0);

        // Left: Omarchy Logo + Workspaces
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.HORIZONTAL);
        left.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = monoText(" ❖ OMARCHY ", 20, COL_BG_DARKER, true);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(COL_ACCENT);
        badgeBg.setCornerRadius(px(5));
        badge.setBackground(badgeBg);
        badge.setPadding(px(10), px(3), px(10), px(3));
        left.addView(badge);

        String[] wsLabels = {" 1:apps ", " 2:hdmi ", " 3:btop ", " 4:sys "};
        for (int i = 0; i < wsLabels.length; i++) {
            TextView ws = monoText(wsLabels[i], 19, i == 0 ? COL_FG_BRIGHT : COL_MUTED, i == 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = px(12);
            if (i == 0) {
                GradientDrawable wsBg = new GradientDrawable();
                wsBg.setColor(COL_SELECTION);
                wsBg.setCornerRadius(px(4));
                ws.setBackground(wsBg);
            }
            left.addView(ws, lp);
        }
        bar.addView(left, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START | Gravity.CENTER_VERTICAL));

        // Center: Clock (dddd HH:mm)
        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("EEEE  HH:mm");
        clock.setFormat24Hour("EEEE  HH:mm");
        clock.setTextSize(0, px(22));
        clock.setTextColor(COL_FG_BRIGHT);
        clock.setTypeface(monoBold);
        bar.addView(clock, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        // Right: Theme + Network + Tray indicators
        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.HORIZONTAL);
        right.setGravity(Gravity.CENTER_VERTICAL);

        TextView themeTag = monoText("[theme: osaka-jade]", 18, COL_CYAN, false);
        right.addView(themeTag);

        topNetText = monoText("  NET: Wi-Fi", 18, COL_FG, true);
        right.addView(topNetText);

        bar.addView(right, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END | Gravity.CENTER_VERTICAL));

        return bar;
    }

    private LinearLayout buildLeftWorkspaceWindow() {
        LinearLayout win = createTitledWindow("┌─ 1:omarchy-launcher ── [apps & inputs] ", COL_ACCENT);

        // Prompt header
        LinearLayout promptRow = new LinearLayout(this);
        promptRow.setOrientation(LinearLayout.HORIZONTAL);
        promptRow.setGravity(Gravity.CENTER_VERTICAL);
        promptRow.setPadding(px(8), px(4), px(8), px(12));

        TextView promptUser = monoText("omarchy@tcl-tv", 20, COL_GREEN, true);
        TextView promptSep = monoText(" in ", 20, COL_MUTED, false);
        TextView promptDir = monoText("~/applications", 20, COL_CYAN, true);
        TextView promptArrow = monoText(" ❯ ", 20, COL_ACCENT, true);
        TextView promptCmd = monoText("ls --grid --no-ads", 20, COL_FG_LIGHT, false);

        promptRow.addView(promptUser);
        promptRow.addView(promptSep);
        promptRow.addView(promptDir);
        promptRow.addView(promptArrow);
        promptRow.addView(promptCmd);
        win.addView(promptRow);

        // App Grid inside ScrollView
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setClipChildren(false);
        scroll.setClipToPadding(false);

        appGrid = new GridLayout(this);
        appGrid.setColumnCount(4);
        appGrid.setClipChildren(false);
        appGrid.setClipToPadding(false);
        scroll.addView(appGrid, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        win.addView(scroll, scrollLp);

        // Section divider for inputs & system controls
        TextView inputHeader = monoText("├─ [2:inputs & quick-ctl] ──────────────────────────────────────────", 17, COL_MUTED, false);
        inputHeader.setPadding(px(6), px(10), px(6), px(8));
        win.addView(inputHeader);

        quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        quickRow.setClipChildren(false);
        quickRow.setClipToPadding(false);
        win.addView(quickRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(88)));

        return win;
    }

    private LinearLayout buildRightBtopWindow() {
        LinearLayout win = createTitledWindow("┌─ 2:btop++ ── [tcl-uniontv @ realtek] ", COL_ACCENT);

        // Uptime & Kernel banner
        btopUptimeView = monoText("up 0d 00:00  │  Android 9  │  Zero-Ads", 18, COL_FG_LIGHT, false);
        btopUptimeView.setPadding(px(8), px(4), px(8), px(10));
        win.addView(btopUptimeView);

        // Sparkline Graph Box (Memory / Load history)
        TextView graphTitle = monoText("┌─ mem & activity graph ──────────────────────┐", 16, COL_MUTED, false);
        win.addView(graphTitle);

        btopSparkline = new SparklineView(this);
        LinearLayout.LayoutParams sparkLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(110));
        sparkLp.topMargin = px(4);
        sparkLp.bottomMargin = px(12);
        win.addView(btopSparkline, sparkLp);

        // Memory Meter
        btopRamText = monoText("MEM   Used: --- MB / --- MB", 19, COL_FG_BRIGHT, true);
        win.addView(btopRamText);
        btopRamBar = new ProgressBarView(this, COL_CYAN);
        LinearLayout.LayoutParams barLp1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(22));
        barLp1.topMargin = px(4);
        barLp1.bottomMargin = px(12);
        win.addView(btopRamBar, barLp1);

        // Swap / ZRAM Meter
        btopSwapText = monoText("ZRAM  Avail: --- MB", 18, COL_FG, false);
        win.addView(btopSwapText);
        btopSwapBar = new ProgressBarView(this, COL_GREEN);
        LinearLayout.LayoutParams barLp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(18));
        barLp2.topMargin = px(4);
        barLp2.bottomMargin = px(12);
        win.addView(btopSwapBar, barLp2);

        // Disk /data Meter
        btopDiskText = monoText("DISK  /data: --- GB / --- GB", 18, COL_FG, false);
        win.addView(btopDiskText);
        btopDiskBar = new ProgressBarView(this, COL_ACCENT);
        LinearLayout.LayoutParams barLp3 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, px(18));
        barLp3.topMargin = px(4);
        barLp3.bottomMargin = px(14);
        win.addView(btopDiskBar, barLp3);

        // Network & Process info box
        LinearLayout netBox = new LinearLayout(this);
        netBox.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable netBg = new GradientDrawable();
        netBg.setColor(0x99090F0D);
        netBg.setCornerRadius(px(6));
        netBg.setStroke(1, COL_SELECTION);
        netBox.setBackground(netBg);
        netBox.setPadding(px(12), px(10), px(12), px(10));

        btopNetIpText = monoText("IPv4 : --- (wlan0)", 17, COL_CYAN, false);
        btopProcsText = monoText("DEBLOAT: 22 pkgs disabled │ Anim: 0.0x", 17, COL_GREEN, false);
        netBox.addView(btopNetIpText);
        netBox.addView(btopProcsText);

        LinearLayout.LayoutParams netBoxLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        netBoxLp.bottomMargin = px(14);
        win.addView(netBox, netBoxLp);

        // Spacer
        View spacer = new View(this);
        win.addView(spacer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // Action Buttons inside btop pane (Boost RAM & Cycle Wallpaper)
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);

        View boostBtn = createActionPill("⚡ BOOST RAM", "action:boost", COL_CYAN, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boostMemoryNow();
            }
        });
        LinearLayout.LayoutParams b1Lp = new LinearLayout.LayoutParams(0, px(68), 1.0f);
        b1Lp.rightMargin = px(10);
        actionRow.addView(boostBtn, b1Lp);

        View bgBtn = createActionPill("❖ TEMA / BG", "action:bg", COL_FG, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWallpaperDialog();
            }
        });
        LinearLayout.LayoutParams b2Lp = new LinearLayout.LayoutParams(0, px(68), 1.0f);
        actionRow.addView(bgBtn, b2Lp);

        win.addView(actionRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return win;
    }

    private View buildBottomStatusBar() {
        FrameLayout status = new FrameLayout(this);
        status.setPadding(px(12), px(6), px(12), 0);

        TextView leftMode = monoText(" NORMAL > omarchy-tv-1.0 > Zero-Telemetry ", 17, COL_ACCENT, true);
        status.addView(leftMode, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL));

        TextView rightHint = monoText("[OK] Abrir   [Segurar OK / Menu] Editar App   [⚡ Boost] Limpar Cache", 17, COL_FG, false);
        status.addView(rightHint, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL));

        return status;
    }

    private LinearLayout createTitledWindow(String title, int borderColor) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COL_BG_PANEL);
        bg.setCornerRadius(px(10));
        bg.setStroke(Math.max(1, px(2)), borderColor);
        container.setBackground(bg);
        container.setPadding(px(20), px(14), px(20), px(18));
        container.setClipChildren(false);
        container.setClipToPadding(false);

        TextView header = monoText(title, 19, COL_CYAN, true);
        header.setPadding(px(4), 0, px(4), px(8));
        container.addView(header);

        return container;
    }

    private void rebuildAppGrid() {
        if (appGrid == null) return;
        appGrid.removeAllViews();
        PackageManager pm = getPackageManager();

        int totalItems = pinnedApps.size() + 1; // +1 for "+ Add App"
        int cols = 4;
        for (int i = 0; i < totalItems; i++) {
            final boolean isAddTile = (i == pinnedApps.size());
            final String pkg = isAddTile ? "action:add_app" : pinnedApps.get(i);
            View tile = isAddTile ? createAddAppTile() : createAppTile(pm, pkg, i);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = px(262);
            lp.height = px(152);
            lp.columnSpec = GridLayout.spec(i % cols);
            lp.rowSpec = GridLayout.spec(i / cols);
            lp.setMargins(px(8), px(8), px(8), px(8));
            appGrid.addView(tile, lp);
        }

        appGrid.post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentFocus() == null && appGrid.getChildCount() > 0) {
                    appGrid.getChildAt(0).requestFocus();
                }
            }
        });
    }

    private View createAppTile(PackageManager pm, final String pkg, final int index) {
        FrameLayout card = new FrameLayout(this);
        final GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(COL_BG_TILE);
        cardBg.setCornerRadius(px(8));
        cardBg.setStroke(Math.max(1, px(2)), COL_SELECTION);
        card.setBackground(cardBg);
        card.setFocusable(true);
        card.setTag(pkg);

        String label = labelOf(pm, pkg);
        Drawable banner = null;
        Drawable icon = null;
        try {
            banner = pm.getApplicationBanner(pkg);
            if (banner == null) {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                banner = ai.loadBanner(pm);
            }
            icon = pm.getApplicationIcon(pkg);
        } catch (Exception ignored) {
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(px(12), px(10), px(12), px(8));

        ImageView img = new ImageView(this);
        if (banner != null) {
            img.setImageDrawable(banner);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            box.addView(img, new LinearLayout.LayoutParams(px(210), px(86)));
        } else if (icon != null) {
            img.setImageDrawable(icon);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            box.addView(img, new LinearLayout.LayoutParams(px(68), px(68)));
        }

        final TextView titleView = monoText(label, 18, COL_FG, true);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = px(6);
        box.addView(titleView, titleLp);

        card.addView(box, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) lastFocusedTag = pkg;
                cardBg.setColor(hasFocus ? COL_BG_FOCUS : COL_BG_TILE);
                cardBg.setStroke(px(hasFocus ? 3 : 2), hasFocus ? COL_CYAN : COL_SELECTION);
                titleView.setTextColor(hasFocus ? COL_FG_BRIGHT : COL_FG);
                v.animate().scaleX(hasFocus ? 1.06f : 1.0f).scaleY(hasFocus ? 1.06f : 1.0f).setDuration(110).start();
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPackage(pkg);
            }
        });

        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAppEditDialog(pkg, index);
                return true;
            }
        });

        card.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
                    showAppEditDialog(pkg, index);
                    return true;
                }
                return false;
            }
        });

        return card;
    }

    private View createAddAppTile() {
        return createActionPill("+ ADICIONAR APP", "action:add_app", COL_ACCENT, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAppDialog(-1);
            }
        });
    }

    private void rebuildQuickBar() {
        if (quickRow == null) return;
        quickRow.removeAllViews();

        // 1. Dynamic HDMI Inputs from TvInputManager
        List<TvInputInfo> hdmis = getHdmiInputs();
        if (!hdmis.isEmpty()) {
            int idx = 1;
            for (final TvInputInfo info : hdmis) {
                String label = getInputLabel(info, "HDMI " + idx);
                View btn = createActionPill("▸ " + label, info.getId(), COL_FG_LIGHT, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openTvInput(info.getId());
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                lp.rightMargin = px(10);
                quickRow.addView(btn, lp);
                idx++;
            }
        } else {
            // Fallback HDMI 1 & HDMI 2 buttons using discovered UnionTV passthrough IDs
            final String[][] fallbackHdmis = {
                    {"HDMI 1", "com.tcl.tvpassthrough/.TvPassThroughService/HW1413744128"},
                    {"HDMI 2", "com.tcl.tvpassthrough/.TvPassThroughService/HW1413744384"}
            };
            for (final String[] h : fallbackHdmis) {
                View btn = createActionPill("▸ " + h[0], h[1], COL_FG_LIGHT, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openTvInput(h[1]);
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                lp.rightMargin = px(10);
                quickRow.addView(btn, lp);
            }
        }

        // 2. Picture / Brilho TCL Overlay
        View picBtn = createActionPill("◈ IMAGEM", "quick:picture", COL_FG_LIGHT, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTclQuickSetting("picture");
            }
        });
        LinearLayout.LayoutParams picLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        picLp.rightMargin = px(10);
        quickRow.addView(picBtn, picLp);

        // 3. Sound TCL Overlay
        View sndBtn = createActionPill("♫ SOM", "quick:sound", COL_FG_LIGHT, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTclQuickSetting("sound");
            }
        });
        LinearLayout.LayoutParams sndLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        sndLp.rightMargin = px(10);
        quickRow.addView(sndBtn, sndLp);

        // 4. Wi-Fi / Network
        View netBtn = createActionPill("⇄ REDE", "quick:net", COL_FG_LIGHT, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNetworkSettings();
            }
        });
        LinearLayout.LayoutParams netLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        netLp.rightMargin = px(10);
        quickRow.addView(netBtn, netLp);

        // 5. Full Android TV Settings
        View cfgBtn = createActionPill("⚙ CONFIG", "quick:settings", COL_FG_LIGHT, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSystemSettings();
            }
        });
        LinearLayout.LayoutParams cfgLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        quickRow.addView(cfgBtn, cfgLp);
    }

    private View createActionPill(String text, final String tag, final int textColor, View.OnClickListener onClick) {
        FrameLayout pill = new FrameLayout(this);
        final GradientDrawable bg = new GradientDrawable();
        bg.setColor(COL_BG_TILE);
        bg.setCornerRadius(px(8));
        bg.setStroke(Math.max(1, px(2)), COL_SELECTION);
        pill.setBackground(bg);
        pill.setFocusable(true);
        pill.setTag(tag);

        final TextView label = monoText(text, 18, textColor, true);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        pill.addView(label, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        pill.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                bg.setColor(hasFocus ? COL_BG_FOCUS : COL_BG_TILE);
                bg.setStroke(px(hasFocus ? 3 : 2), hasFocus ? COL_CYAN : COL_SELECTION);
                label.setTextColor(hasFocus ? COL_FG_BRIGHT : textColor);
                v.animate().scaleX(hasFocus ? 1.05f : 1.0f).scaleY(hasFocus ? 1.05f : 1.0f).setDuration(100).start();
            }
        });

        pill.setOnClickListener(onClick);
        return pill;
    }

    private void updateSystemTelemetry() {
        try {
            // 1. Uptime
            long uptimeSec = SystemClock.elapsedRealtime() / 1000;
            long days = uptimeSec / 86400;
            long hours = (uptimeSec % 86400) / 3600;
            long mins = (uptimeSec % 3600) / 60;
            btopUptimeView.setText(String.format(Locale.US,
                    "up %dd %02d:%02d  │  Android %s  │  Zero-Ads",
                    days, hours, mins, Build.VERSION.RELEASE));

            // 2. Memory & ZRAM via ActivityManager + /proc/meminfo
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            if (am != null) {
                am.getMemoryInfo(mi);
            }
            long totalMb = mi.totalMem / (1024 * 1024);
            long availMb = mi.availMem / (1024 * 1024);
            long usedMb = Math.max(0, totalMb - availMb);
            float memRatio = totalMb > 0 ? (float) usedMb / (float) totalMb : 0.5f;

            btopRamText.setText(String.format(Locale.US,
                    "MEM   Used: %d MB / %d MB (%d%%)",
                    usedMb, totalMb, Math.round(memRatio * 100)));
            btopRamBar.setProgress(memRatio);

            // Track in sparkline
            memHistory.add(memRatio);
            if (memHistory.size() > 28) {
                memHistory.remove(0);
            }
            btopSparkline.setValues(memHistory);

            // Parse SwapTotal & SwapFree from /proc/meminfo
            long swapTotalKb = 0;
            long swapFreeKb = 0;
            BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("SwapTotal:")) {
                    swapTotalKb = parseMemKb(line);
                } else if (line.startsWith("SwapFree:")) {
                    swapFreeKb = parseMemKb(line);
                }
            }
            br.close();
            long swapUsedMb = Math.max(0, (swapTotalKb - swapFreeKb) / 1024);
            long swapTotalMb = Math.max(1, swapTotalKb / 1024);
            float swapRatio = (float) swapUsedMb / (float) swapTotalMb;
            btopSwapText.setText(String.format(Locale.US,
                    "ZRAM  Swap: %d MB / %d MB (%d%%)  │  Free RAM: %d MB",
                    swapUsedMb, swapTotalMb, Math.round(swapRatio * 100), availMb));
            btopSwapBar.setProgress(swapRatio);

            // 3. Disk /data
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long totalBytes = statFs.getTotalBytes();
            long availBytes = statFs.getAvailableBytes();
            long usedBytes = totalBytes - availBytes;
            float diskRatio = totalBytes > 0 ? (float) usedBytes / (float) totalBytes : 0f;
            btopDiskText.setText(String.format(Locale.US,
                    "DISK  /data: %.2f GB / %.2f GB (%d%%)",
                    usedBytes / 1e9, totalBytes / 1e9, Math.round(diskRatio * 100)));
            btopDiskBar.setProgress(diskRatio);

            // 4. Network IP
            String ip = getLocalIpv4();
            boolean wifi = isWifiConnected();
            topNetText.setText(wifi ? "  ● Wi-Fi (" + ip + ")" : "  ● NET (" + ip + ")");
            btopNetIpText.setText("IPv4 : " + ip + "  │  ADB: :5555 (Ativo)");

        } catch (Exception ignored) {
        }
    }

    private long parseMemKb(String line) {
        try {
            String[] parts = line.replaceAll("[^0-9]+", " ").trim().split("\\s+");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getLocalIpv4() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null && info.getIpAddress() != 0) {
                    int ip = info.getIpAddress();
                    return String.format(Locale.US, "%d.%d.%d.%d",
                            (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
                }
            }
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = en.nextElement();
                Enumeration<InetAddress> addrs = intf.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "Offline";
    }

    private boolean isWifiConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && Build.VERSION.SDK_INT >= 23) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    private void boostMemoryNow() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return;
        ActivityManager.MemoryInfo before = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(before);

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        int killed = 0;
        for (ApplicationInfo ai : apps) {
            if (!ai.packageName.equals(getPackageName()) && !ai.packageName.startsWith("com.android.systemui")) {
                try {
                    am.killBackgroundProcesses(ai.packageName);
                    killed++;
                } catch (Exception ignored) {
                }
            }
        }
        System.gc();

        ActivityManager.MemoryInfo after = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(after);
        long diffMb = Math.max(0, (after.availMem - before.availMem) / (1024 * 1024));
        long availNow = after.availMem / (1024 * 1024);
        updateSystemTelemetry();
        Toast.makeText(this,
                String.format(Locale.US, "⚡ Omarchy Boost: Processos em cache limpos! RAM Livre: %d MB (+%d MB)", availNow, diffMb),
                Toast.LENGTH_SHORT).show();
    }

    private void loadPinnedApps() {
        pinnedApps.clear();
        PackageManager pm = getPackageManager();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString("apps", null);
        List<String> candidates = saved != null
                ? Arrays.asList(saved.split(","))
                : DEFAULT_APPS;

        for (String pkg : candidates) {
            String trimmed = pkg.trim();
            if (!trimmed.isEmpty() && !pinnedApps.contains(trimmed) && getLaunchIntent(pm, trimmed) != null) {
                pinnedApps.add(trimmed);
            }
        }

        if (saved == null) {
            Intent leanback = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            for (ResolveInfo ri : pm.queryIntentActivities(leanback, 0)) {
                String p = ri.activityInfo.packageName;
                if (!pinnedApps.contains(p) && !HIDDEN_PACKAGES.contains(p)) {
                    pinnedApps.add(p);
                }
            }
        }
    }

    private void savePinnedApps() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString("apps", TextUtils.join(",", pinnedApps))
                .apply();
    }

    private int getWallpaperIndex() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getInt("bg_index", 0);
    }

    private void applyWallpaper(int idx) {
        if (idx < 0 || idx >= BG_ASSETS.length) idx = 0;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("bg_index", idx).apply();
        String assetName = BG_ASSETS[idx];
        if ("solid".equals(assetName)) {
            getWindow().setBackgroundDrawable(new ColorDrawable(COL_BG_DARKER));
            return;
        }
        try {
            InputStream is = getAssets().open(assetName);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            if (bmp != null) {
                BitmapDrawable bd = new BitmapDrawable(getResources(), bmp);
                getWindow().setBackgroundDrawable(bd);
                return;
            }
        } catch (Exception ignored) {
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(COL_BG_DARKER));
    }

    private void showWallpaperDialog() {
        showOmarchyMenu("❖ Selecionar Background (Osaka Jade)", Arrays.asList(BG_NAMES), new MenuCallback() {
            @Override
            public void onSelect(int which) {
                applyWallpaper(which);
            }
        });
    }

    private void showAppEditDialog(final String pkg, final int index) {
        String label = labelOf(getPackageManager(), pkg);
        List<String> options = Arrays.asList(
                "Mover para o início",
                "Mover para a esquerda",
                "Mover para a direita",
                "Substituir aplicativo",
                "Remover da tela inicial",
                "Informações / Forçar parada do App"
        );
        showOmarchyMenu("❖ Editar: " + label, options, new MenuCallback() {
            @Override
            public void onSelect(int which) {
                if (which == 0 && index > 0) {
                    pinnedApps.remove(index);
                    pinnedApps.add(0, pkg);
                    savePinnedApps();
                    rebuildAppGrid();
                } else if (which == 1 && index > 0) {
                    Collections.swap(pinnedApps, index, index - 1);
                    savePinnedApps();
                    rebuildAppGrid();
                } else if (which == 2 && index < pinnedApps.size() - 1) {
                    Collections.swap(pinnedApps, index, index + 1);
                    savePinnedApps();
                    rebuildAppGrid();
                } else if (which == 3) {
                    showAddAppDialog(index);
                } else if (which == 4) {
                    pinnedApps.remove(index);
                    savePinnedApps();
                    rebuildAppGrid();
                } else if (which == 5) {
                    try {
                        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                                Uri.parse("package:" + pkg));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    private void showAddAppDialog(final int replaceIndex) {
        PackageManager pm = getPackageManager();
        TreeMap<String, String> available = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String[] cats = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
        for (String c : cats) {
            List<ResolveInfo> list = pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(c), 0);
            for (ResolveInfo ri : list) {
                String p = ri.activityInfo.packageName;
                if (!pinnedApps.contains(p) && !HIDDEN_PACKAGES.contains(p)) {
                    available.put(labelOf(pm, p), p);
                }
            }
        }
        if (available.isEmpty()) {
            Toast.makeText(this, "Todos os aplicativos instalados já estão na tela inicial!", Toast.LENGTH_SHORT).show();
            return;
        }
        final List<String> labels = new ArrayList<>(available.keySet());
        final List<String> pkgs = new ArrayList<>(available.values());
        showOmarchyMenu("❖ Adicionar Aplicativo", labels, new MenuCallback() {
            @Override
            public void onSelect(int which) {
                String chosen = pkgs.get(which);
                if (replaceIndex >= 0 && replaceIndex < pinnedApps.size()) {
                    pinnedApps.set(replaceIndex, chosen);
                } else {
                    pinnedApps.add(chosen);
                }
                savePinnedApps();
                rebuildAppGrid();
            }
        });
    }

    private interface MenuCallback {
        void onSelect(int index);
    }

    private void showOmarchyMenu(String title, final List<String> items, final MenuCallback callback) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Dialog_NoActionBar);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(px(28), px(24), px(28), px(24));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF50C1512);
        bg.setCornerRadius(px(10));
        bg.setStroke(px(3), COL_CYAN);
        box.setBackground(bg);

        TextView header = monoText(title, 22, COL_CYAN, true);
        header.setPadding(px(8), 0, px(8), px(16));
        box.addView(header);

        ListView lv = new ListView(this);
        lv.setDivider(null);
        GradientDrawable sel = new GradientDrawable();
        sel.setColor(COL_SELECTION);
        sel.setCornerRadius(px(6));
        sel.setStroke(px(2), COL_ACCENT);
        lv.setSelector(sel);

        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public Object getItem(int position) {
                return items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = monoText(" ❯  " + items.get(position), 20, COL_FG_BRIGHT, false);
                tv.setPadding(px(16), px(16), px(16), px(16));
                return tv;
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                callback.onSelect(position);
            }
        });

        box.addView(lv, new LinearLayout.LayoutParams(px(680), ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.setContentView(box);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void launchPackage(String pkg) {
        Intent intent = getLaunchIntent(getPackageManager(), pkg);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception ignored) {
            }
        }
    }

    private static Intent getLaunchIntent(PackageManager pm, String pkg) {
        Intent lb = pm.getLeanbackLaunchIntentForPackage(pkg);
        return lb != null ? lb : pm.getLaunchIntentForPackage(pkg);
    }

    private String labelOf(PackageManager pm, String pkg) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception e) {
            return pkg;
        }
    }

    private List<TvInputInfo> getHdmiInputs() {
        List<TvInputInfo> list = new ArrayList<>();
        try {
            TvInputManager tim = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
            if (tim == null) return list;
            for (TvInputInfo info : tim.getTvInputList()) {
                if (info.getType() == TvInputInfo.TYPE_HDMI && info.getParentId() == null && !info.isHidden(this)) {
                    list.add(info);
                }
            }
            Collections.sort(list, new Comparator<TvInputInfo>() {
                @Override
                public int compare(TvInputInfo a, TvInputInfo b) {
                    return a.getId().compareTo(b.getId());
                }
            });
        } catch (Exception ignored) {
        }
        return list.size() > 3 ? list.subList(0, 3) : list;
    }

    private String getInputLabel(TvInputInfo info, String fallback) {
        try {
            CharSequence custom = info.loadCustomLabel(this);
            if (custom != null && custom.length() > 0) return custom.toString();
            CharSequence label = info.loadLabel(this);
            if (label != null && label.length() > 0) return label.toString();
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private void openTvInput(String inputId) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId));
        PackageManager pm = getPackageManager();
        List<Intent> candidates = new ArrayList<>();

        ResolveInfo ri = pm.resolveActivity(viewIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (ri != null && ri.activityInfo != null && INPUT_PLAYER.equals(ri.activityInfo.packageName)) {
            candidates.add(new Intent(viewIntent).setClassName(INPUT_PLAYER, ri.activityInfo.name));
        }
        candidates.add(new Intent(viewIntent).setClassName("com.tcl.tv", "com.tcl.tv.TVActivity"));
        candidates.add(new Intent(viewIntent).setClassName("com.android.tv", "com.android.tv.MainActivity"));
        candidates.add(viewIntent);

        for (Intent i : candidates) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(i);
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private void openTclQuickSetting(String type) {
        try {
            String extra = "sound".equals(type) ? "SoundPreset" : "picture";
            Intent svc = new Intent(TCL_SHOW_WINDOW)
                    .setClassName("com.tcl.settings", "com.tcl.settings.ShowWindowService")
                    .putExtra("Type", extra);
            if (getPackageManager().resolveService(svc, 0) != null) {
                startService(svc);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            String action = "sound".equals(type)
                    ? "android.settings.SOUND_SETTINGS"
                    : "android.settings.DISPLAY_SETTINGS";
            startActivity(new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            openSystemSettings();
        }
    }

    private void openNetworkSettings() {
        String[] actions = {"android.settings.WIFI_SETTINGS", "android.settings.WIRELESS_SETTINGS", "android.settings.SETTINGS"};
        for (String a : actions) {
            try {
                startActivity(new Intent(a).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private void openSystemSettings() {
        try {
            startActivity(new Intent("android.settings.SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) {
        }
    }

    private TextView monoText(String str, int sizePx, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(str);
        tv.setTextSize(0, px(sizePx));
        tv.setTextColor(color);
        tv.setTypeface(bold ? monoBold : monoRegular);
        return tv;
    }

    private int px(int v) {
        return Math.round(v * scale);
    }

    // Custom btop-style Progress Bar View
    private class ProgressBarView extends View {
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private float progress = 0.0f;

        ProgressBarView(Context ctx, int fgColor) {
            super(ctx);
            bgPaint.setColor(0xFF1A2A22);
            fgPaint.setColor(fgColor);
        }

        void setProgress(float p) {
            this.progress = Math.max(0f, Math.min(1f, p));
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            int blocks = 24;
            float gap = px(3);
            float blockW = (w - (gap * (blocks - 1))) / blocks;
            int activeBlocks = Math.round(progress * blocks);

            for (int i = 0; i < blocks; i++) {
                float left = i * (blockW + gap);
                rect.set(left, 0, left + blockW, h);
                canvas.drawRoundRect(rect, px(2), px(2), i < activeBlocks ? fgPaint : bgPaint);
            }
        }
    }

    // Custom btop-style Sparkline Graph View
    private class SparklineView extends View {
        private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private List<Float> values = new ArrayList<>();

        SparklineView(Context ctx) {
            super(ctx);
            gridPaint.setColor(0xFF1E3128);
            gridPaint.setStrokeWidth(1f);

            linePaint.setColor(COL_CYAN);
            linePaint.setStrokeWidth(px(3));
            linePaint.setStyle(Paint.Style.STROKE);

            fillPaint.setColor(0x442DD5B7);
            fillPaint.setStyle(Paint.Style.FILL);
        }

        void setValues(List<Float> vals) {
            this.values = new ArrayList<>(vals);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();

            // Draw background grid lines
            for (int i = 1; i <= 3; i++) {
                float y = (h / 4f) * i;
                canvas.drawLine(0, y, w, y, gridPaint);
            }

            if (values.isEmpty()) return;
            int n = values.size();
            float step = w / 28f;
            float prevX = 0;
            float prevY = h - (values.get(0) * h * 0.85f);

            for (int i = 1; i < n; i++) {
                float x = i * step;
                float y = h - (values.get(i) * h * 0.85f);
                canvas.drawLine(prevX, prevY, x, y, linePaint);
                canvas.drawRect(prevX, y, x, h, fillPaint);
                prevX = x;
                prevY = y;
            }
        }
    }
}
