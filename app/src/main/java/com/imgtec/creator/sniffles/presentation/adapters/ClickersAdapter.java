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

package com.imgtec.creator.sniffles.presentation.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.Clicker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ClickersAdapter extends BaseAdapter<Clicker, ClickersAdapter.ViewHolder> {



  public interface ClickersAdapterListener {
    void onStartProvisioningClicked(Clicker clicker);
  }

  private Context context;
  private ClickersAdapterListener listener;

  public ClickersAdapter(Context context, ClickersAdapterListener listener) {
    this.context = context;
    this.listener = listener;
  }


  @Override
  public ClickersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clicker_item, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ClickersAdapter.ViewHolder holder, int position) {
    final Clicker clicker = getItem(position);
    holder.clickerName.setText(clicker.getClickerName());
    if (clicker.getInProvisionState()) {
      holder.startProvisioningButton.setVisibility(View.INVISIBLE);
      holder.progressBar.setVisibility(View.VISIBLE);
    } else if (clicker.getSelected()) {
      holder.startProvisioningButton.setVisibility(View.VISIBLE);
      holder.progressBar.setVisibility(View.INVISIBLE);
    } else {
      holder.startProvisioningButton.setVisibility(View.INVISIBLE);
      holder.progressBar.setVisibility(View.INVISIBLE);
    }
    holder.startProvisioningButton.setText("Start");
    holder.startProvisioningButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onStartProvisioningClicked(clicker);
      }
    });
    if (clicker.getSelected()) {
      holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.supporting_color_light_purple));
    } else {
      holder.container.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.clicker_item_container) ViewGroup container;
    @BindView(R.id.clicker_name_tv) TextView clickerName;
    @BindView(R.id.start_provisioning_button) Button startProvisioningButton;
    @BindView(R.id.provisioning_progress) ProgressBar progressBar;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
