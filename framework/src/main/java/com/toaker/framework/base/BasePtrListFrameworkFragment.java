/*******************************************************************************
 * Copyright 2013-2014 Toaker framework-master
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.toaker.framework.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.ListenerWrapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonDataRequest;
import com.android.volley.toolbox.RequestParameter;
import com.android.volley.toolbox.ResponseWrapper;
import com.android.volley.toolbox.VolleyErrorWrapper;
import com.toaker.framework.core.inter.LoadMoreHandler;
import com.toaker.framework.core.surface.fragment.BaseFragment;
import com.toaker.framework.core.view.AbsFrameworkListView;
import com.toaker.framework.utils.ReflectUtils;
import com.toaker.framework.utils.VolleyHelper;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Decorator for framework-master
 *
 * @author Toaker [Toaker](ToakerQin@gmail.com)
 *         [Toaker](http://www.toaker.com)
 * @Description:
 * @Time Create by 2015/4/9 11:48
 */
public abstract class BasePtrListFrameworkFragment<T extends ResponseWrapper> extends BaseFragment implements PtrHandler,LoadMoreHandler{

    public String   PARAMS_KEY_PAGE_NUM         = "page";

    public String   PARAMS_KEY_PAGE_size        = "page_size";

    public String   PARAMS_KEY_TOTAL_PAGE       = "total_page";

    public String   PARAMS_KEY_TOTAL_COUNT      = "page_count";

    protected int page_num                      = 0;

    protected int page_size                     = 0;

    protected int total_page                    = 0;

    protected int total_count                   = 0;

    protected RequestParameter mRequestParameter;

    protected RequestQueue     mRequestQueue;

    protected AbsFrameworkListView mListView;

    protected boolean              isLoadMore = false;

    protected PtrClassicFrameLayout mPtrLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = VolleyHelper.newRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPtrLayout = new PtrClassicFrameLayout(getActivity());
        mPtrLayout.addView(super.onCreateView(inflater, container, savedInstanceState));
        return mPtrLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPtrLayout.setPtrHandler(this);
    }

    ListenerWrapper<T> mListenerWrapper = new ListenerWrapper<T>() {
        @Override
        public void onSuccess(T response) {
            try {page_num = ReflectUtils.getFieldValue(response,ResponseWrapper.FIELD_NAME_PAGE_NUM);}catch (Exception e){}
            try {page_size = ReflectUtils.getFieldValue(response,ResponseWrapper.FIELD_NAME_PAGE_SIZE);}catch (Exception e){}
            try {total_count = ReflectUtils.getFieldValue(response,ResponseWrapper.FIELD_NAME_TOTAL_COUNT);}catch (Exception e){}
            try {total_page = ReflectUtils.getFieldValue(response,ResponseWrapper.FIELD_NAME_TOTAL_PAGE);}catch (Exception e){}
            if(isLoadMore){
                isLoadMore = false;
                BasePtrListFrameworkFragment.this.onLoadMoreSuccess(response);
            }else {
                BasePtrListFrameworkFragment.this.onSuccess(response);
            }
        }

        @Override
        public void onError(VolleyErrorWrapper error) {
            onError(error);
        }
    };

    protected void startNetWork(){
        startNetWork(Request.Method.GET);
    }

    protected void startNetWork(int method){
        startNetWork(method,null);
    }

    protected void startNetWork(int method,RequestParameter params){
        this.mRequestParameter = params;
        this.mRequestParameter.setMethod(method);
        mRequestQueue.add(new JsonDataRequest<T>(method,getRequestUrl(),params,mListenerWrapper,!isLoadMore));
    }

    public abstract void onSuccess(T response);

    public abstract void onLoadMoreSuccess(T response);

    /**
     *
     * @param error
     */
    protected void onError(VolleyErrorWrapper error){
        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
    }

    protected abstract String getRequestUrl();

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        if(mListView != null){
            return mListView.checkCanDoRefresh();
        }
        return false;
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame) {

    }

    @Override
    public void onLoadMore(AbsFrameworkListView view) {
        this.isLoadMore = true;
        int page = page_num + 1;
        if(page <= total_page){
            if(mRequestParameter != null){
               mRequestParameter.getStringParams().put(PARAMS_KEY_PAGE_NUM,String.valueOf(page));
               startNetWork(mRequestParameter.getMethod(),mRequestParameter);
            }else {
                noMoreData();
            }
        }else {
            noMoreData();
        }
    }

    protected void noMoreData(){
        if(mListView != null){
            mListView.completeLoadMore();
        }
    }
}
