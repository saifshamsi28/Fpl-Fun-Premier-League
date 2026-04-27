package com.zpl.handcricket.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zpl.handcricket.R;
import com.zpl.handcricket.adapters.LeaderboardAdapter;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.LeaderboardEntry;
import com.zpl.handcricket.models.PageResponse;
import com.zpl.handcricket.models.User;
import com.zpl.handcricket.utils.AppState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String PERIOD_ALL_TIME = "all_time";
    private static final String PERIOD_WEEKLY = "weekly";
    private static final String PERIOD_MONTHLY = "monthly";

    private RecyclerView recycler;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private View emptyState;
    private View leaderboardContent;
    private TextView txtYourRank;
    private TextView headerRank;
    private TextView headerName;
    private TextView headerWinRate;
    private TextView headerPlayed;
    private TextView headerWon;
    private TextView headerRuns;

    private TextView tabAllTime;
    private TextView tabWeekly;
    private TextView tabMonthly;

    private LinearLayout podiumFirstSlot;
    private LinearLayout podiumSecondSlot;
    private LinearLayout podiumThirdSlot;
    private TextView podiumFirstAvatar;
    private TextView podiumSecondAvatar;
    private TextView podiumThirdAvatar;
    private TextView podiumFirstName;
    private TextView podiumSecondName;
    private TextView podiumThirdName;
    private TextView podiumFirstRate;
    private TextView podiumSecondRate;
    private TextView podiumThirdRate;

    private final LeaderboardAdapter adapter = new LeaderboardAdapter();
    private final List<LeaderboardEntry> allItems = new ArrayList<>();
    private String currentPeriod = PERIOD_ALL_TIME;

    private SortColumn sortColumn = SortColumn.RANK;
    private boolean sortAscending = true;

    private enum SortColumn {
        RANK,
        NAME,
        WIN_RATE,
        MATCHES_PLAYED,
        MATCHES_WON,
        TOTAL_RUNS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        ImageView btnBack = findViewById(R.id.btnBack);
        recycler = findViewById(R.id.recyclerLeaderboard);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progress = findViewById(R.id.progress);
        emptyState = findViewById(R.id.emptyState);
        leaderboardContent = findViewById(R.id.leaderboardContent);
        txtYourRank = findViewById(R.id.txtYourRank);
        headerRank = findViewById(R.id.headerRank);
        headerName = findViewById(R.id.headerName);
        headerWinRate = findViewById(R.id.headerWinRate);
        headerPlayed = findViewById(R.id.headerPlayed);
        headerWon = findViewById(R.id.headerWon);
        headerRuns = findViewById(R.id.headerRuns);

        tabAllTime = findViewById(R.id.tabAllTime);
        tabWeekly = findViewById(R.id.tabWeekly);
        tabMonthly = findViewById(R.id.tabMonthly);

        podiumFirstSlot = findViewById(R.id.podiumFirstSlot);
        podiumSecondSlot = findViewById(R.id.podiumSecondSlot);
        podiumThirdSlot = findViewById(R.id.podiumThirdSlot);
        podiumFirstAvatar = findViewById(R.id.podiumFirstAvatar);
        podiumSecondAvatar = findViewById(R.id.podiumSecondAvatar);
        podiumThirdAvatar = findViewById(R.id.podiumThirdAvatar);
        podiumFirstName = findViewById(R.id.podiumFirstName);
        podiumSecondName = findViewById(R.id.podiumSecondName);
        podiumThirdName = findViewById(R.id.podiumThirdName);
        podiumFirstRate = findViewById(R.id.podiumFirstRate);
        podiumSecondRate = findViewById(R.id.podiumSecondRate);
        podiumThirdRate = findViewById(R.id.podiumThirdRate);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        recycler.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());

        headerRank.setOnClickListener(v -> onSortSelected(SortColumn.RANK));
        headerName.setOnClickListener(v -> onSortSelected(SortColumn.NAME));
        headerWinRate.setOnClickListener(v -> onSortSelected(SortColumn.WIN_RATE));
        headerPlayed.setOnClickListener(v -> onSortSelected(SortColumn.MATCHES_PLAYED));
        headerWon.setOnClickListener(v -> onSortSelected(SortColumn.MATCHES_WON));
        headerRuns.setOnClickListener(v -> onSortSelected(SortColumn.TOTAL_RUNS));

        tabAllTime.setOnClickListener(v -> onPeriodSelected(PERIOD_ALL_TIME));
        tabWeekly.setOnClickListener(v -> onPeriodSelected(PERIOD_WEEKLY));
        tabMonthly.setOnClickListener(v -> onPeriodSelected(PERIOD_MONTHLY));

        swipeRefresh.setColorSchemeColors(0xFF1D4ED8);
        swipeRefresh.setOnRefreshListener(() -> loadLeaderboard(false));

        updateSortHeaderText();
        updatePeriodTabs();
        setRankFromCache();
        loadLeaderboard(true);
    }

    private void onPeriodSelected(String period) {
        if (period.equals(currentPeriod)) {
            return;
        }
        currentPeriod = period;
        updatePeriodTabs();
        loadLeaderboard(true);
    }

    private void loadLeaderboard(boolean firstLoad) {
        if (firstLoad) {
            progress.setVisibility(View.VISIBLE);
        }

        ApiClient.get().leaderboard(0, 100, currentPeriod).enqueue(new Callback<PageResponse<LeaderboardEntry>>() {
            @Override
            public void onResponse(Call<PageResponse<LeaderboardEntry>> call, Response<PageResponse<LeaderboardEntry>> response) {
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (!response.isSuccessful() || response.body() == null || response.body().items == null) {
                    showEmptyState();
                    return;
                }

                allItems.clear();
                allItems.addAll(response.body().items);

                updateYourRankForPeriod();
                bindTopThree(allItems);
                applySort();
                showContentState();
            }

            @Override
            public void onFailure(Call<PageResponse<LeaderboardEntry>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        leaderboardContent.setVisibility(View.GONE);
    }

    private void showContentState() {
        boolean hasAnyData = !allItems.isEmpty();
        emptyState.setVisibility(hasAnyData ? View.GONE : View.VISIBLE);
        leaderboardContent.setVisibility(hasAnyData ? View.VISIBLE : View.GONE);
    }

    private void onSortSelected(SortColumn selectedColumn) {
        if (sortColumn == selectedColumn) {
            sortAscending = !sortAscending;
        } else {
            sortColumn = selectedColumn;
            sortAscending = selectedColumn == SortColumn.RANK || selectedColumn == SortColumn.NAME;
        }
        applySort();
    }

    private void applySort() {
        List<LeaderboardEntry> tableRows = new ArrayList<>(allItems);
        tableRows.sort(buildComparator());
        adapter.setItems(tableRows);
        updateSortHeaderText();
    }

    private Comparator<LeaderboardEntry> buildComparator() {
        Comparator<LeaderboardEntry> comparator;
        switch (sortColumn) {
            case NAME:
                comparator = Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER);
                break;
            case WIN_RATE:
                comparator = Comparator.comparingDouble(e -> e.winRate);
                break;
            case MATCHES_PLAYED:
                comparator = Comparator.comparingInt(e -> e.matchesPlayed);
                break;
            case MATCHES_WON:
                comparator = Comparator.comparingInt(e -> e.matchesWon);
                break;
            case TOTAL_RUNS:
                comparator = Comparator.comparingInt(e -> e.totalRuns);
                break;
            case RANK:
            default:
                comparator = Comparator.comparingInt(e -> e.rank);
                break;
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparingInt(e -> e.rank);
    }

    private void updateSortHeaderText() {
        setHeader(headerRank, "Rank", SortColumn.RANK);
        setHeader(headerName, "Name", SortColumn.NAME);
        setHeader(headerWinRate, "Win %", SortColumn.WIN_RATE);
        setHeader(headerPlayed, "Matches", SortColumn.MATCHES_PLAYED);
        setHeader(headerWon, "Won", SortColumn.MATCHES_WON);
        setHeader(headerRuns, "Runs", SortColumn.TOTAL_RUNS);
    }

    private void setHeader(TextView view, String label, SortColumn column) {
        view.setText(label);
        if (sortColumn == column) {
            view.setTextColor(0xFFFFFFFF);
            view.setAlpha(1f);
        } else {
            view.setTextColor(0xFFD2E7FF);
            view.setAlpha(0.82f);
        }
    }

    private void updatePeriodTabs() {
        bindTab(tabAllTime, PERIOD_ALL_TIME.equals(currentPeriod));
        bindTab(tabWeekly, PERIOD_WEEKLY.equals(currentPeriod));
        bindTab(tabMonthly, PERIOD_MONTHLY.equals(currentPeriod));
    }

    private void bindTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_leaderboard_tab_active : R.drawable.bg_leaderboard_tab_inactive);
        tab.setTextColor(selected ? Color.parseColor("#0A2E58") : Color.parseColor("#B8D5FF"));
        tab.setAlpha(selected ? 1f : 0.9f);
    }

    private void bindTopThree(List<LeaderboardEntry> rankedEntries) {
        LeaderboardEntry first = findByRank(rankedEntries, 1);
        LeaderboardEntry second = findByRank(rankedEntries, 2);
        LeaderboardEntry third = findByRank(rankedEntries, 3);

        bindPodiumSlot(first, podiumFirstSlot, podiumFirstAvatar, podiumFirstName, podiumFirstRate);
        bindPodiumSlot(second, podiumSecondSlot, podiumSecondAvatar, podiumSecondName, podiumSecondRate);
        bindPodiumSlot(third, podiumThirdSlot, podiumThirdAvatar, podiumThirdName, podiumThirdRate);
    }

    private LeaderboardEntry findByRank(List<LeaderboardEntry> entries, int rank) {
        for (LeaderboardEntry entry : entries) {
            if (entry.rank == rank) {
                return entry;
            }
        }
        return null;
    }

    private void bindPodiumSlot(LeaderboardEntry entry,
                                View slot,
                                TextView avatar,
                                TextView name,
                                TextView rate) {
        if (entry == null) {
            slot.setAlpha(0.55f);
            slot.setBackground(new ColorDrawable(Color.TRANSPARENT));
            avatar.setText("-");
            name.setText("--");
            rate.setText("--");
            return;
        }

        slot.setAlpha(1f);
        slot.setBackgroundResource(android.R.color.transparent);
        avatar.setText(extractInitial(entry));
        name.setText(displayName(entry));
        rate.setText(String.format(Locale.US, "%.1f%%", entry.winRate));
    }

    private String extractInitial(LeaderboardEntry entry) {
        String name = displayName(entry);
        if (name.isEmpty()) {
            return "-";
        }
        return name.substring(0, 1).toUpperCase(Locale.US);
    }

    private void updateYourRankForPeriod() {
        for (LeaderboardEntry entry : allItems) {
            if (entry.isYou) {
                txtYourRank.setText("Your Rank: #" + entry.rank);
                return;
            }
        }

        if (PERIOD_ALL_TIME.equals(currentPeriod)) {
            setRankFromCache();
        } else {
            txtYourRank.setText("Your Rank: --");
        }
    }

    private void setRankFromCache() {
        User me = AppState.get().getCachedUser();
        if (me != null && me.rank != null && me.rank > 0) {
            txtYourRank.setText("Your Rank: #" + me.rank);
        } else {
            txtYourRank.setText("Your Rank: --");
        }
    }

    private String displayName(LeaderboardEntry entry) {
        if (entry == null) {
            return "";
        }
        if (notBlank(entry.fullName)) {
            return entry.fullName.trim();
        }
        if (notBlank(entry.username)) {
            return entry.username.trim();
        }
        return "";
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
