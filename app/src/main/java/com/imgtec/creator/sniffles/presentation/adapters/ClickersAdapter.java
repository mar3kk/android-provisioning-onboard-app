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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.api.jsonrpc.pojo.Clicker;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ClickersAdapter extends BaseAdapter<ClickersAdapter.ClickerWrapper, ClickersAdapter.ViewHolder> {

  public interface ClickersAdapterListener {
    void onStartProvisioningClicked(Clicker clicker);
  }

  private Context context;
  private ClickersAdapterListener listener;

  public ClickersAdapter(Context context, ClickersAdapterListener listener) {
    this.context = context;
    this.listener = listener;
    setHasStableIds(true);
  }

  @Override
  public int getItemViewType(int position) {
    return getItem(position).getType().ordinal();
  }


  @Override
  public ClickersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = null;
    if (viewType == ClickerWrapper.Type.INFO.ordinal()) {
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clicker_item_label, parent, false);
      return new LabelViewHolder(v);
    }
    else if (viewType == ClickerWrapper.Type.ITEM.ordinal()) {
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clicker_item, parent, false);
      return new ItemViewHolder(v, listener);
    }
    else if (viewType == ClickerWrapper.Type.NONE.ordinal()){
      v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clicker_item_label, parent, false);
      return new EmptyViewHolder(v);
    }
    throw new IllegalArgumentException(String.format("Type: %s not supported!", viewType));
  }

  @Override
  public void onBindViewHolder(ClickersAdapter.ViewHolder holder, int position) {
    final ClickerWrapper wrapper = getItem(position);
    holder.handle(wrapper);
  }


  public static class ClickerWrapper {

    public enum Type {
      INFO, ITEM, NONE
    }

    String name;
    Clicker clicker;
    Type type;

    public ClickerWrapper(final Clicker clicker, Type type) {
      this.clicker = clicker;
      this.type = type;
    }

    public Clicker getClicker() {
      return clicker;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
    public void setType(Type type) {
      this.type = type;
    }

    public Type getType() {
      return type;
    }

  }


  abstract static class ViewHolder extends RecyclerView.ViewHolder{

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public abstract void handle(ClickerWrapper wrapper);
  }

  static class LabelViewHolder extends ViewHolder {

    @BindView(R.id.title) TextView title;
    @BindView(R.id.divider) View divider;

    public LabelViewHolder(View itemView) {
      super(itemView);
    }

    @Override
    public void handle(ClickerWrapper wrapper) {
      title.setText(wrapper.getName());
    }
  }

  static class ItemViewHolder extends ViewHolder {

    private final ClickersAdapterListener listener;

    @BindView(R.id.clicker_item_container) ViewGroup container;
    @BindView(R.id.clicker_name_tv) TextView clickerName;
    @BindView(R.id.start_provisioning_button) Button startProvisioningButton;
    @BindView(R.id.connected) ImageView connected;

    public ItemViewHolder(View itemView, ClickersAdapterListener listener) {
      super(itemView);
      this.listener = listener;
    }

    @Override
    public void handle(ClickerWrapper wrapper) {
      final Clicker clicker = wrapper.getClicker();
      clickerName.setText(clicker.getClickerName());

      if (clicker.getInProvisionState()) {
        startProvisioningButton.setVisibility(View.GONE);
      } else if (clicker.getSelected()) {
        startProvisioningButton.setVisibility(View.VISIBLE);

      } else {
        startProvisioningButton.setVisibility(View.GONE);
      }

      if (clicker.isProvisioned()) {
        connected.setVisibility(View.VISIBLE);
        startProvisioningButton.setVisibility(View.GONE);
      }
      else {
        connected.setVisibility(View.GONE);
        startProvisioningButton.setVisibility(View.VISIBLE);
      }

      startProvisioningButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          listener.onStartProvisioningClicked(clicker);
        }
      });
      if (!clicker.getSelected()) {
        startProvisioningButton.setVisibility(View.GONE);
      }
      container.setSelected(clicker.getSelected());
    }
  }

  static class EmptyViewHolder extends LabelViewHolder {

    public EmptyViewHolder(View itemView) {
      super(itemView);
    }

    @Override
    public void handle(ClickerWrapper wrapper) {
      title.setText("List is empty");
      divider.setVisibility(View.INVISIBLE);
    }
  }
}
