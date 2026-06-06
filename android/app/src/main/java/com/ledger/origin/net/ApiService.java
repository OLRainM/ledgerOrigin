package com.ledger.origin.net;

import com.ledger.origin.model.Account;
import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Budget;
import com.ledger.origin.model.Category;
import com.ledger.origin.model.Dtos;
import com.ledger.origin.model.Transaction;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/** Retrofit API 接口定义 */
public interface ApiService {

    @POST("api/register")
    Call<ApiResponse<Dtos.LoginData>> register(@Body Map<String, Object> body);

    @POST("api/login")
    Call<ApiResponse<Dtos.LoginData>> login(@Body Map<String, Object> body);

    // 交易记录
    @GET("api/transactions")
    Call<ApiResponse<Dtos.PageData<Transaction>>> getTransactions(@QueryMap Map<String, String> q);

    @POST("api/transactions")
    Call<ApiResponse<Object>> createTransaction(@Body Map<String, Object> body);

    @PUT("api/transactions/{id}")
    Call<ApiResponse<Object>> updateTransaction(@Path("id") long id, @Body Map<String, Object> body);

    @DELETE("api/transactions/{id}")
    Call<ApiResponse<Object>> deleteTransaction(@Path("id") long id);

    // 分类
    @GET("api/categories")
    Call<ApiResponse<List<Category>>> getCategories(@QueryMap Map<String, String> q);

    @POST("api/categories")
    Call<ApiResponse<Object>> createCategory(@Body Map<String, Object> body);

    @PUT("api/categories/{id}")
    Call<ApiResponse<Object>> updateCategory(@Path("id") long id, @Body Map<String, Object> body);

    @DELETE("api/categories/{id}")
    Call<ApiResponse<Object>> deleteCategory(@Path("id") long id);

    // 账户
    @GET("api/accounts")
    Call<ApiResponse<List<Account>>> getAccounts();

    @POST("api/accounts")
    Call<ApiResponse<Object>> createAccount(@Body Map<String, Object> body);

    @PUT("api/accounts/{id}")
    Call<ApiResponse<Object>> updateAccount(@Path("id") long id, @Body Map<String, Object> body);

    @DELETE("api/accounts/{id}")
    Call<ApiResponse<Object>> deleteAccount(@Path("id") long id);

    // 预算
    @GET("api/budgets")
    Call<ApiResponse<List<Budget>>> getBudgets(@QueryMap Map<String, String> q);

    @POST("api/budgets")
    Call<ApiResponse<Object>> createBudget(@Body Map<String, Object> body);

    @PUT("api/budgets/{id}")
    Call<ApiResponse<Object>> updateBudget(@Path("id") long id, @Body Map<String, Object> body);

    @DELETE("api/budgets/{id}")
    Call<ApiResponse<Object>> deleteBudget(@Path("id") long id);

    // 统计
    @GET("api/stats/overview")
    Call<ApiResponse<Dtos.Overview>> getOverview(@QueryMap Map<String, String> q);

    @GET("api/stats/category")
    Call<ApiResponse<List<Dtos.CategoryStat>>> getCategoryStat(@QueryMap Map<String, String> q);

    @GET("api/stats/daily")
    Call<ApiResponse<List<Dtos.DailyStat>>> getDailyStat(@QueryMap Map<String, String> q);
}
