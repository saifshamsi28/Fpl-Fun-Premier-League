package com.zpl.handcricket.activities;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.zpl.handcricket.R;
import com.zpl.handcricket.utils.MatchContext;
import com.zpl.handcricket.views.HandView;
import com.zpl.handcricket.views.TimerRingView;
import com.zpl.handcricket.ws.GameSocket;

import java.util.HashSet;
import java.util.Set;

/**
 * Live match screen — top scoreboard + hands + ball dots, bottom timer + 1..6 num pad.
 * Drives UI via events from {@link GameSocket}:
 *   match_found, ball_start, opponent_locked, ball_result, innings_switch, match_end, opponent_left
 */
public class GameActivity extends AppCompatActivity implements GameSocket.Listener {

    // -- top ---
    private TextView tvP1Name, tvP1Role, tvP2Name, tvP2Role, tvChant;
    private LinearLayout p1Dots, p2Dots;
    private HandView handP1, handP2;
    private ImageView btnClose, batOverlay;
    private View ballOverlay;
    private View outFlashOverlay;
    private TextView outOverlay;
    private View introOverlay;
    private ImageView introIcon;
    private TextView introMessage;
    private TextView introCountdown;

    // -- bottom ---
    private TimerRingView timerRing;
    private TextView tvMessage;
    private Button[] numButtons;

    private final Handler ui = new Handler(Looper.getMainLooper());

    private String youUserId;
    private String oppUserId;
    private String youTeamCode;
    private String oppTeamCode;
    private boolean youBattingNow;
    private int ballsPerInnings = 6;
    private int myPick = -1;
    private boolean waitingForIntro = false;
    private JsonObject queuedBallStart;
    private long introUntilMs = 0L;
    private long currentBallTimerMs = 4500L;
    private final Set<String> processedBallKeys = new HashSet<>();
    private int introSequence = 0;

    // scoreboard dot counts
    private int p1BallsPlayed = 0;
    private int p2BallsPlayed = 0;
    private int p1RunsTotal = 0;
    private int p2RunsTotal = 0;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_game);

        bindViews();
        configureHands();
        configureNumPad();

        btnClose.setOnClickListener(v -> confirmLeave());

        GameSocket.get().setListener(this);

        // Render initial state from match_found
        JsonObject mf = MatchContext.get().matchFound;
        if (mf != null) renderMatchFound(mf);
    }

    private void bindViews() {
        tvP1Name = findViewById(R.id.tvP1Name);
        tvP1Role = findViewById(R.id.tvP1Role);
        tvP2Name = findViewById(R.id.tvP2Name);
        tvP2Role = findViewById(R.id.tvP2Role);
        tvChant  = findViewById(R.id.tvChant);
        p1Dots = findViewById(R.id.p1Dots);
        p2Dots = findViewById(R.id.p2Dots);
        handP1 = findViewById(R.id.handP1);
        handP2 = findViewById(R.id.handP2);
        btnClose = findViewById(R.id.btnClose);
        batOverlay = findViewById(R.id.batOverlay);
        ballOverlay = findViewById(R.id.ballOverlay);
        outFlashOverlay = findViewById(R.id.outFlashOverlay);
        outOverlay = findViewById(R.id.outOverlay);
        introOverlay = findViewById(R.id.introOverlay);
        introIcon = findViewById(R.id.introIcon);
        introMessage = findViewById(R.id.introMessage);
        introCountdown = findViewById(R.id.introCountdown);
        timerRing = findViewById(R.id.timerRing);
        tvMessage = findViewById(R.id.tvMessage);
        numButtons = new Button[]{
                findViewById(R.id.num1),
                findViewById(R.id.num2),
                findViewById(R.id.num3),
                findViewById(R.id.num4),
                findViewById(R.id.num5),
                findViewById(R.id.num6)
        };
    }

    private void configureHands() {
        handP1.setHandColor(HandView.COLOR_BLUE);
        handP1.setMirrored(false);
        handP1.setFingers(0);
        handP1.setLabel("");
        handP2.setHandColor(HandView.COLOR_RED);
        handP2.setMirrored(true);
        handP2.setFingers(0);
        handP2.setLabel("");
    }

    private void configureNumPad() {
        for (int i = 0; i < numButtons.length; i++) {
            final int pick = i + 1;
            numButtons[i].setOnClickListener(v -> choosePick(pick));
        }
    }

    private void choosePick(int n) {
        if (myPick != -1) return;
        myPick = n;
        // Highlight selection
        for (int i = 0; i < numButtons.length; i++) {
            boolean sel = (i + 1 == n);
            numButtons[i].setBackgroundResource(
                    sel ? R.drawable.bg_numpad_selected : R.drawable.bg_numpad);
            numButtons[i].setTextColor(sel ? Color.WHITE : Color.parseColor("#0E1A3B"));
            numButtons[i].setEnabled(false);
        }
        tvMessage.setText(R.string.waiting_for_opponent_pick);
        GameSocket.get().pick(n);
    }

    private void resetNumPad() {
        myPick = -1;
        for (Button b : numButtons) {
            b.setBackgroundResource(R.drawable.bg_numpad);
            b.setTextColor(Color.parseColor("#0E1A3B"));
            b.setEnabled(true);
        }
    }

    private void confirmLeave() {
        GameSocket.get().leave();
        GameSocket.get().close();
        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    // -------- rendering --------

    private void renderMatchFound(JsonObject data) {
        JsonObject you = data.getAsJsonObject("you");
        JsonObject opp = data.getAsJsonObject("opponent");
        youUserId = you.get("userId").getAsString();
        oppUserId = opp.get("userId").getAsString();
        youTeamCode = codeFromTeamName(safeString(you, "teamName"));
        oppTeamCode = codeFromTeamName(safeString(opp, "teamName"));

        tvP1Name.setText(safeString(you, "username"));
        tvP2Name.setText(safeString(opp, "username"));

        boolean youBatFirst = data.has("youBatFirst") && data.get("youBatFirst").getAsBoolean();
        youBattingNow = youBatFirst;
        tvP1Role.setText(youBatFirst ? "Batting" : "Bowling");
        tvP2Role.setText(youBatFirst ? "Bowling" : "Batting");

        if (data.has("ballsPerInnings")) ballsPerInnings = data.get("ballsPerInnings").getAsInt();
        p1RunsTotal = 0;
        p2RunsTotal = 0;
        p1BallsPlayed = 0;
        p2BallsPlayed = 0;
        buildDotRow(p1Dots, ballsPerInnings, true);
        buildDotRow(p2Dots, ballsPerInnings, false);
        updateScoreboardRoles();

        // Paint team colors on pills
        applyTeamColor(tvP1Name, safeString(you, "color"), true);
        applyTeamColor(tvP2Name, safeString(opp, "color"), false);

        // Match found intro before the first ball starts.
        showIntroPhase(youBatFirst ? R.string.you_are_batting : R.string.you_are_bowling, 3000L);
    }

    private String safeString(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private String codeFromTeamName(String name) {
        if (name == null) return "";
        switch (name.toLowerCase()) {
            case "mumbai":    return "MUM";
            case "hyderabad": return "HYD";
            case "delhi":     return "DEL";
            case "kolkata":   return "KOL";
            case "bangalore": return "BLR";
            case "chennai":   return "CHE";
            default:          return "";
        }
    }

    private void applyTeamColor(TextView pill, String hex, boolean isBlueSide) {
        View parent = (View) pill.getParent();
        if (parent == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dp(28));
        int color;
        try { color = Color.parseColor(hex); }
        catch (Exception e) {
            color = isBlueSide ? Color.parseColor("#1D4ED8") : Color.parseColor("#E63946");
        }
        gd.setColor(color);
        parent.setBackground(gd);
    }

    private void buildDotRow(LinearLayout container, int count, boolean leftAlign) {
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            TextView dot = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(20), dp(20));
            lp.setMargins(dp(3), 0, dp(3), 0);
            dot.setLayoutParams(lp);
            dot.setGravity(Gravity.CENTER);
            dot.setTextColor(Color.WHITE);
            dot.setTextSize(10);
            dot.setText("");
            dot.setBackgroundResource(R.drawable.bg_ball_dot_empty);
            container.addView(dot);
        }
    }

    private int dp(int d) {
        return (int) (d * getResources().getDisplayMetrics().density);
    }

    // -------- event dispatch --------

    @Override
    public void onEvent(String type, JsonObject data) {
        ui.post(() -> {
            switch (type) {
                case "ball_start":    handleBallStart(data);    break;
                case "opponent_locked": /* could show pulse */   break;
                case "ball_result":   handleBallResult(data);   break;
                case "innings_switch":handleInningsSwitch(data);break;
                case "match_end":     handleMatchEnd(data);     break;
                case "opponent_left": handleOpponentLeft(data); break;
                case "error":
                    Toast.makeText(this,
                            data.has("message") ? data.get("message").getAsString() : "Error",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void handleBallStart(JsonObject d) {
        queuedBallStart = d;
        if (d != null && d.has("timerMs")) {
            try {
                currentBallTimerMs = d.get("timerMs").getAsLong();
            } catch (Exception ignored) {
                currentBallTimerMs = 4500L;
            }
        } else {
            currentBallTimerMs = 4500L;
        }
        if (waitingForIntro && System.currentTimeMillis() < introUntilMs) {
            return;
        }
        startSelectionPhase(d);
    }

    private void handleBallResult(JsonObject d) {
        String ballKey = buildBallKey(d);
        if (ballKey != null && processedBallKeys.contains(ballKey)) {
            return;
        }
        if (ballKey != null) {
            processedBallKeys.add(ballKey);
        }

        int batterPick = d.get("batterPick").getAsInt();
        int bowlerPick = d.get("bowlerPick").getAsInt();
        boolean wicket = d.get("wicket").getAsBoolean();
        int runs       = d.get("runs").getAsInt();
        int p1Runs     = d.get("p1Runs").getAsInt();
        int p2Runs     = d.get("p2Runs").getAsInt();
        String batterId= d.get("batterId").getAsString();

        boolean youAreBatter = batterId.equals(youUserId);
        int youPick = youAreBatter ? batterPick : bowlerPick;
        int oppPick = youAreBatter ? bowlerPick : batterPick;
        boolean p1IsBatter = youAreBatter; // p1 is always "you" in this screen

        // Run-hit animation: show both picks as pop badges instead of finger reveal.
        animatePickBadge(handP1, youPick, youAreBatter);
        animatePickBadge(handP2, oppPick, !youAreBatter);

        p1RunsTotal = p1Runs;
        p2RunsTotal = p2Runs;
        updateScoreboardRoles();

        // Toast banner
        String who = youAreBatter ? tvP1Name.getText().toString() : tvP2Name.getText().toString();
        if (wicket) {
            tvMessage.setText(who + " OUT!");
        } else {
            tvMessage.setText(who + " hit " + runs + " runs");
        }

        // Update timer big number to show the batter's run for this ball (or 0 on wicket)
        timerRing.setNumber(wicket ? 0 : runs);

        // Update ball dots
        int batterDotIdx = (p1IsBatter ? p1BallsPlayed++ : p2BallsPlayed++);
        LinearLayout container = p1IsBatter ? p1Dots : p2Dots;
        if (batterDotIdx < container.getChildCount()) {
            TextView dot = (TextView) container.getChildAt(batterDotIdx);
            int runForDot = wicket ? 0 : runs;
            setDotValue(dot, runForDot, wicket);
            View batterHand = p1IsBatter ? handP1 : handP2;
            animateRunToDot(batterPick, wicket, dot, batterHand);
        }

        if (wicket) {
            showOutPhase();
        }
    }

    private void setDotValue(TextView dot, int runs, boolean wicket) {
        dot.setText(String.valueOf(runs));
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        if (wicket) {
            dot.setText("W");
            gd.setColor(Color.parseColor("#EF4444"));
            gd.setStroke(dp(1), Color.WHITE);
        } else if (runs == 0) {
            gd.setColor(Color.parseColor("#0E1A3B"));
            gd.setStroke(dp(1), Color.WHITE);
        } else {
            gd.setColor(Color.parseColor("#16A34A"));
            gd.setStroke(dp(1), Color.WHITE);
        }
        dot.setBackground(gd);
    }

    private void animateRunToDot(int batterPick, boolean wicket, TextView targetDot, View sourceView) {
        ViewGroup root = findViewById(android.R.id.content);
        if (root == null) return;

        TextView flying = new TextView(this);
        int size = dp(28);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
        flying.setLayoutParams(lp);
        flying.setGravity(Gravity.CENTER);
        flying.setText(wicket ? "W" : String.valueOf(batterPick));
        flying.setTextColor(Color.WHITE);
        flying.setTextSize(12);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        if (wicket) {
            bg.setColor(Color.parseColor("#EF4444"));
        } else if (batterPick == 0) {
            bg.setColor(Color.parseColor("#0E1A3B"));
        } else {
            bg.setColor(Color.parseColor("#16A34A"));
        }
        bg.setStroke(dp(1), Color.WHITE);
        flying.setBackground(bg);

        float[] source = centerInRoot(sourceView, root);
        float[] target = centerInRoot(targetDot, root);
        flying.setX(source[0] - size / 2f);
        flying.setY(source[1] - size / 2f);
        root.addView(flying);

        ObjectAnimator moveX = ObjectAnimator.ofFloat(flying, "x", flying.getX(), target[0] - size / 2f);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(flying, "y", flying.getY(), target[1] - size / 2f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flying, "scaleX", 1.1f, 0.75f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flying, "scaleY", 1.1f, 0.75f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(flying, "alpha", 1f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(moveX, moveY, scaleX, scaleY, alpha);
        set.setDuration(420);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
        ui.postDelayed(() -> root.removeView(flying), 460);
    }

    private void animatePickBadge(View sourceView, int pick, boolean batterPick) {
        ViewGroup root = findViewById(android.R.id.content);
        if (root == null) return;

        TextView badge = new TextView(this);
        int size = dp(36);
        badge.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        badge.setGravity(Gravity.CENTER);
        badge.setText(String.valueOf(pick));
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(13);
        badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor(batterPick ? "#16A34A" : "#E63946"));
        bg.setStroke(dp(1), Color.WHITE);
        badge.setBackground(bg);

        float[] source = centerInRoot(sourceView, root);
        badge.setX(source[0] - size / 2f);
        badge.setY(source[1] - size / 2f);
        badge.setScaleX(0.25f);
        badge.setScaleY(0.25f);
        badge.setAlpha(0f);
        root.addView(badge);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(badge, "alpha", 0f, 1f, 0f);
        ObjectAnimator sx = ObjectAnimator.ofFloat(badge, "scaleX", 0.25f, 1.2f, 0.9f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(badge, "scaleY", 0.25f, 1.2f, 0.9f);
        ObjectAnimator up = ObjectAnimator.ofFloat(badge, "translationY", 0f, -dp(14));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, sx, sy, up);
        set.setDuration(420);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();

        ui.postDelayed(() -> root.removeView(badge), 460);
    }

    private float[] centerInRoot(View child, View root) {
        int[] childLoc = new int[2];
        int[] rootLoc = new int[2];
        child.getLocationOnScreen(childLoc);
        root.getLocationOnScreen(rootLoc);
        float cx = childLoc[0] - rootLoc[0] + child.getWidth() / 2f;
        float cy = childLoc[1] - rootLoc[1] + child.getHeight() / 2f;
        return new float[]{cx, cy};
    }

    private void updateScoreboardRoles() {
        tvP1Role.setText(roleText(p1RunsTotal, p1BallsPlayed, youBattingNow));
        tvP2Role.setText(roleText(p2RunsTotal, p2BallsPlayed, !youBattingNow));
    }

    private String roleText(int runs, int ballsPlayed, boolean battingNow) {
        if (battingNow) {
            return runs + " runs • Batting";
        }
        if (ballsPlayed > 0) {
            return runs + " runs • Bowling";
        }
        return "Yet to bat";
    }

    private void handleInningsSwitch(JsonObject d) {
        String newBatter = d.get("newBatterId").getAsString();
        youBattingNow = newBatter.equals(youUserId);
        updateScoreboardRoles();

        showIntroPhase(youBattingNow ? R.string.you_are_batting : R.string.you_are_bowling, 3000L);
    }

    private void handleMatchEnd(JsonObject d) {
        MatchContext.get().lastMatchEnd = d;
        GameSocket.get().close();
        Intent i = new Intent(this, ResultActivity.class);
        startActivity(i);
        finish();
    }

    private void handleOpponentLeft(JsonObject d) {
        Toast.makeText(this, "Opponent left — you win!", Toast.LENGTH_SHORT).show();
        MatchContext.get().lastMatchEnd = d;
        handleMatchEnd(d);
    }

    private void showIntroPhase(int messageResId, long durationMs) {
        final int thisIntro = ++introSequence;
        waitingForIntro = true;
        introUntilMs = System.currentTimeMillis() + durationMs;

        resetNumPad();
        handP1.setFingers(0);
        handP1.setLabel("");
        handP2.setFingers(0);
        handP2.setLabel("");

        tvMessage.setText(R.string.pick_before_timer);
        tvChant.setVisibility(View.GONE);

        timerRing.setVisibility(View.INVISIBLE);
        introOverlay.setVisibility(View.VISIBLE);
        introMessage.setText(messageResId);
        introIcon.setImageResource(messageResId == R.string.you_are_batting
                ? R.drawable.ic_bat
                : R.drawable.ic_ball);
        int steps = Math.max(1, (int) Math.ceil(durationMs / 1000.0));
        introCountdown.setText(String.valueOf(steps));

        startIntroCountdown(durationMs, thisIntro);

        ui.postDelayed(() -> {
            if (thisIntro != introSequence) return;
            waitingForIntro = false;
            introOverlay.setVisibility(View.GONE);
            timerRing.setVisibility(View.VISIBLE);
            if (queuedBallStart != null) {
                startSelectionPhase(queuedBallStart);
                queuedBallStart = null;
            }
        }, durationMs);
    }

    private void startIntroCountdown(long durationMs, int sequenceId) {
        int steps = Math.max(1, (int) Math.ceil(durationMs / 1000.0));
        for (int i = 0; i < steps; i++) {
            final int value = steps - i;
            ui.postDelayed(() -> {
                if (sequenceId == introSequence && introOverlay.getVisibility() == View.VISIBLE) {
                    introCountdown.setText(String.valueOf(value));
                }
            }, i * 1000L);
        }
    }

    private void showOutPhase() {
        triggerOutFlash();

        outOverlay.setVisibility(View.VISIBLE);
        outOverlay.setScaleX(0.35f);
        outOverlay.setScaleY(0.35f);
        outOverlay.setAlpha(0f);
        outOverlay.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(320)
                .withEndAction(() -> outOverlay.animate().scaleX(1f).scaleY(1f).setDuration(180).start())
                .start();
        ui.postDelayed(() -> outOverlay.animate().alpha(0f).setDuration(260).withEndAction(() -> outOverlay.setVisibility(View.GONE)).start(), 3000L);
    }

            private void triggerOutFlash() {
            if (outFlashOverlay == null) return;
            outFlashOverlay.setVisibility(View.VISIBLE);
            outFlashOverlay.setAlpha(0f);
            outFlashOverlay.animate().cancel();

            outFlashOverlay.animate()
                .alpha(1f)
                .setDuration(120)
                .withEndAction(() -> outFlashOverlay.animate()
                    .alpha(0f)
                    .setDuration(420)
                    .withEndAction(() -> outFlashOverlay.setVisibility(View.GONE))
                    .start())
                .start();
            }

    private String buildBallKey(JsonObject d) {
        JsonObject mf = MatchContext.get().matchFound;
        if (mf == null || !mf.has("matchId")) return null;
        if (d == null || !d.has("innings") || !d.has("ballNo")) return null;
        return mf.get("matchId").getAsString() + ":" + d.get("innings").getAsInt() + ":" + d.get("ballNo").getAsInt();
    }

    private void startSelectionPhase(JsonObject d) {
        if (d == null || d.isJsonNull()) return;
        handP1.setFingers(0); handP1.setLabel("");
        handP2.setFingers(0); handP2.setLabel("");
        resetNumPad();
        tvChant.setVisibility(View.GONE);
        introOverlay.setVisibility(View.GONE);
        ballOverlay.setVisibility(View.GONE);
        timerRing.setVisibility(View.VISIBLE);
        tvMessage.setText(youBattingNow ? R.string.you_are_batting : R.string.you_are_bowling);
        timerRing.startCountdownSequence(currentBallTimerMs, 3, 2, 1);
    }

    @Override
    public void onBackPressed() {
        confirmLeave();
    }
}
