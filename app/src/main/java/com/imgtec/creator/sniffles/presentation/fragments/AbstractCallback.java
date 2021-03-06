/*
 * <b>Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 *  and/or licensors. </b>
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *      endorse or promote products derived from this software without specific prior written
 *      permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *  WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.imgtec.creator.sniffles.presentation.fragments;

import android.os.Handler;

import com.imgtec.creator.sniffles.data.api.ApiCallback;
import com.imgtec.creator.sniffles.utils.Condition;

import java.lang.ref.WeakReference;


public abstract class AbstractCallback<F extends BaseFragment, S,T> implements ApiCallback<S, T>{

  final Handler mainHandler;
  WeakReference<F> fragment;

  public AbstractCallback(F fragment, Handler mainHandler) throws IllegalArgumentException {
    super();
    Condition.checkArgument(fragment != null, "Fragment cannot be null");
    Condition.checkArgument(mainHandler != null, "Handler cannot be null");
    this.fragment = new WeakReference<>(fragment);
    this.mainHandler = mainHandler;
  }

  @Override
  public void onSuccess(final S service, final T result) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        F f = fragment.get();
        if (f != null && f.isAdded()) {
          onSuccess(f, service, result);
        }
      }
    });
  }

  @Override
  public void onFailure(final S service, final Throwable t) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        F f = fragment.get();
        if (f != null && f.isAdded()) {
          onFailure(f, service, t);
        }
      }
    });
  }

  protected abstract void onSuccess(F fragment, S service, T result);

  protected abstract void onFailure(F fragment, S service, Throwable t);
}
