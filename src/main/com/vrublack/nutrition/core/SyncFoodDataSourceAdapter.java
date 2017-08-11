package com.vrublack.nutrition.core;


import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Adapter for asynchronous food data source
 */
public class SyncFoodDataSourceAdapter implements SyncFoodDataSource
{
    private AsyncFoodDataSource asyncFoodDataSource;

    public SyncFoodDataSourceAdapter(AsyncFoodDataSource asyncFoodDataSource)
    {
        this.asyncFoodDataSource = asyncFoodDataSource;
    }

    @Override
    public List<SearchResultItem> search(String searchStr, SearchHistory history)
    {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        // a wrapper has to be used because the variable needs to be final
        final ResultsWrapper<List<SearchResultItem>> result = new ResultsWrapper<>();
        asyncFoodDataSource.search(searchStr, new AsyncFoodDataSource.SearchCallback()
        {
            @Override
            public void onSuccess(List<SearchResultItem> results)
            {
                result.data = results;
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(int errorCode)
            {
                countDownLatch.countDown();
            }
        });

        return result.data;
    }

    @Override
    public FoodItem retrieve(String id, SearchHistory history)
    {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        // a wrapper has to be used because the variable needs to be final
        final ResultsWrapper<FoodItem> result = new ResultsWrapper<>();
        asyncFoodDataSource.retrieve(id, new AsyncFoodDataSource.RetrieveCallback()
        {
            @Override
            public void onSuccess(FoodItem foodItem)
            {
                result.data = foodItem;
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(int errorCode)
            {
                countDownLatch.countDown();
            }
        });

        return result.data;
    }

    @Override
    public FoodItem get(String id)
    {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        // a wrapper has to be used because the variable needs to be final
        final ResultsWrapper<FoodItem> result = new ResultsWrapper<>();
        asyncFoodDataSource.get(id, new AsyncFoodDataSource.RetrieveCallback()
        {
            @Override
            public void onSuccess(FoodItem foodItem)
            {
                result.data = foodItem;
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(int errorCode)
            {
                countDownLatch.countDown();
            }
        });

        return result.data;
    }

    private class ResultsWrapper<T>
    {
        public T data;
    }
}
