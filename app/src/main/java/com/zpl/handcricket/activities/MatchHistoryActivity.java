package com.zpl.handcricket.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zpl.handcricket.R;
import com.zpl.handcricket.adapters.MatchHistoryAdapter;
import com.zpl.handcricket.api.ApiClient;
import com.zpl.handcricket.models.MatchSummary;
import com.zpl.handcricket.models.PageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchHistoryActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 15;

    private EditText edtSearch;
    private TextView chipAll, chipWon, chipLost, chipRanked, chipFriendly;
    private TextView txtCount, txtSortLabel;
    private RecyclerView recycler;
    private View emptyState;
    private SwipeRefreshLayout swipeRefresh;

    private final List<MatchSummary> items = new ArrayList<>();
    private MatchHistoryAdapter adapter;

    private int page = 0;
    private int totalPages = 1;
    private long totalItems = 0;
    private boolean loading = false;

    private String filter = "all";      // all | won | lost | ranked | friendly
    private String sort   = "latest";   // latest | oldest | highest
    private String query  = "";

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_match_history);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSort = findViewById(R.id.btnSort);
        edtSearch    = findViewById(R.id.edtSearch);
        chipAll      = findViewById(R.id.chipAll);
        chipWon      = findViewById(R.id.chipWon);
        chipLost     = findViewById(R.id.chipLost);
        chipRanked   = findViewById(R.id.chipRanked);
        chipFriendly = findViewById(R.id.chipFriendly);
        txtCount     = findViewById(R.id.txtCount);
        txtSortLabel = findViewById(R.id.txtSortLabel);
        recycler     = findViewById(R.id.recyclerMatches);
        emptyState   = findViewById(R.id.emptyState);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        adapter = new MatchHistoryAdapter(items, m -> {
            Intent i = new Intent(this, ResultActivity.class);
            i.putExtra(ResultActivity.EXTRA_MATCH_ID, m.id);
            startActivity(i);
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSort.setOnClickListener(v -> showSortDialog());

        chipAll     .setOnClickListener(v -> setFilter("all"));
        chipWon     .setOnClickListener(v -> setFilter("won"));
        chipLost    .setOnClickListener(v -> setFilter("lost"));
        chipRanked  .setOnClickListener(v -> setFilter("ranked"));
        chipFriendly.setOnClickListener(v -> setFilter("friendly"));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence c, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable e) {
                query = e.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> reload();
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });

        swipeRefresh.setColorSchemeColors(0xFF1976D2);
        swipeRefresh.setOnRefreshListener(this::reload);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || loading) return;
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int last = lm.findLastVisibleItemPosition();
                if (last >= adapter.getItemCount() - 3 && page + 1 < totalPages) {
                    page++;
                    loadPage(false);
                }
            }
        });

        updateSortLabel();
        reload();
    }

    private void setFilter(String f) {
        filter = f;
        chipAll     .setBackgroundResource("all"    .equals(f) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_idle);
        chipWon     .setBackgroundResource("won"    .equals(f) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_idle);
        chipLost    .setBackgroundResource("lost"   .equals(f) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_idle);
        chipRanked  .setBackgroundResource("ranked" .equals(f) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_idle);
        chipFriendly.setBackgroundResource("friendly".equals(f) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_idle);

        int selectedColor = 0xFF0D47A1;
        int idleColor     = 0xFFFFFFFF;
        chipAll     .setTextColor("all"    .equals(f) ? selectedColor : idleColor);
        chipWon     .setTextColor("won"    .equals(f) ? selectedColor : idleColor);
        chipLost    .setTextColor("lost"   .equals(f) ? selectedColor : idleColor);
        chipRanked  .setTextColor("ranked" .equals(f) ? selectedColor : idleColor);
        chipFriendly.setTextColor("friendly".equals(f) ? selectedColor : idleColor);

        reload();
    }

    private void showSortDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_sort_matches);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroupSort);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelSort);
        TextView btnApply = dialog.findViewById(R.id.btnApplySort);

        // Pre-select current sort
        if ("latest".equals(sort)) radioGroup.check(R.id.radioLatest);
        else if ("oldest".equals(sort)) radioGroup.check(R.id.radioOldest);
        else if ("highest".equals(sort)) radioGroup.check(R.id.radioHighest);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radioLatest) sort = "latest";
            else if (selectedId == R.id.radioOldest) sort = "oldest";
            else if (selectedId == R.id.radioHighest) sort = "highest";

            updateSortLabel();
            dialog.dismiss();
            reload();
        });

        dialog.show();
    }

    private void updateSortLabel() {
        String s = "Latest";
        if ("oldest" .equals(sort)) s = "Oldest";
        if ("highest".equals(sort)) s = "Highest";
        txtSortLabel.setText("Sort: " + s);
    }

    private void reload() {
        page = 0;
        items.clear();
        adapter.setLoading(true);
        loadPage(true);
    }

    private void loadPage(boolean isInitial) {
        if (loading) return;
        loading = true;

        ApiClient.get().matchHistory(page, PAGE_SIZE, query, filter, sort)
                .enqueue(new Callback<PageResponse<MatchSummary>>() {
                    @Override
                    public void onResponse(Call<PageResponse<MatchSummary>> c,
                                           Response<PageResponse<MatchSummary>> r) {
                        loading = false;
                        swipeRefresh.setRefreshing(false);

                        if (!r.isSuccessful() || r.body() == null) {
                            if (isInitial) adapter.setLoading(false);
                            renderEmpty();
                            return;
                        }
                        PageResponse<MatchSummary> body = r.body();
                        totalPages = Math.max(body.totalPages, 1);
                        totalItems = body.totalItems;
                        if (body.items != null) items.addAll(body.items);
                        
                        adapter.setItems(items);

                        txtCount.setText("Showing " + items.size() + " of " + totalItems + " matches");
                        boolean empty = items.isEmpty();
                        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<PageResponse<MatchSummary>> c, Throwable t) {
                        loading = false;
                        swipeRefresh.setRefreshing(false);
                        if (isInitial) adapter.setLoading(false);
                        renderEmpty();
                    }
                });
    }

    private void renderEmpty() {
        if (items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
            txtCount.setText("Showing 0 matches");
        }
    }
}
