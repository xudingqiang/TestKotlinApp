package com.fde.baselib.view;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RecyclerScrollBinder {
    private OnScrollChangeListener listener;

    private final ExecutorService scrollExecutor = Executors.newSingleThreadExecutor();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Future<?> currentScrollTask = null;

    private  float mPendingProgress;
    private  boolean mPosted;

    private boolean mRecyclerScrollingByBar = false;

    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        this.listener = l;
    }

    public  void bind(RecyclerView rv, CustomScrollBarView bar) {

        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // 如果有正在等待执行的任务，直接取消它，只保留最新的计算
                if (currentScrollTask != null && !currentScrollTask.isDone()) {
                    currentScrollTask.cancel(true);
                }

                // 将计算工作扔进子线程
                currentScrollTask = scrollExecutor.submit(() -> {
                    // 确保安全：部分系统在极端非主流线程调用此方法可能偶发警告，但基本只读安全
                    int range = recyclerView.computeVerticalScrollRange();
                    int offset = recyclerView.computeVerticalScrollOffset();
                    int extent = recyclerView.computeVerticalScrollExtent();

                    int maxScrollRange = range - extent;
                    if (maxScrollRange <= 0 || range <= 0) return;

                    // 在子线程完成所有数学运算
                    float progress = offset * 1f / maxScrollRange;
                    float thumbHeight = extent * 1f / range * bar.getHeight();

                    if(mRecyclerScrollingByBar){
                        return;
                    }

                    // 切换回主线程只做纯粹的 UI 赋值
                    mainHandler.post(() -> {
                        bar.setThumbHeight(thumbHeight);
                        bar.setProgress(progress);
                    });
                });
            }
        });

        rv.addOnScrollListener(
                new RecyclerView.OnScrollListener(){

                    @Override
                    public void onScrollStateChanged(
                            RecyclerView rv,
                            int state){

                        if(state == RecyclerView.SCROLL_STATE_IDLE){

                            if(mRecyclerScrollingByBar){
                                mRecyclerScrollingByBar=false;
                            }
                        }
                    }
                });

        bar.setOnScrollChangeListener(progress -> {
            mPendingProgress = progress;

            if (mPosted) return;
            mPosted = true;

            // Execute heavy calculation on a background thread
            mExecutor.execute(() -> {
                // Compute metrics (Note: RV methods should ideally be read carefully or cached if possible)

                int position;

                int itemCount = rv.getAdapter() == null ?  0 : rv.getAdapter().getItemCount();
                if(itemCount == 0){
                    return;
                }
                mRecyclerScrollingByBar = true;
                if(itemCount <= 2000){
                    int range = rv.computeVerticalScrollRange();
                    int extent = rv.computeVerticalScrollExtent();
                    int currentOffset = rv.computeVerticalScrollOffset();

                    int targetOffset = (int) ((range - extent) * mPendingProgress);
                    position = targetOffset - currentOffset;
                } else {
                    position = (int)(itemCount * progress);
                }


                // Post ONLY the UI update back to the main thread
                rv.post(() -> {
                    mPosted = false;
                    if(itemCount > 2000){
                        lm.scrollToPositionWithOffset(
                                position,
                                0
                        );
                    }else {
                        rv.scrollBy(0, position);
                    }
                });
            });
        });

    }

    public void unbind(){
        if (scrollExecutor != null) {
            scrollExecutor.shutdownNow();
        }

        if(mExecutor !=null){
            mExecutor.shutdown();
        }
    }

    public interface OnScrollChangeListener {
        void onScroll(float progress);
    }
}
