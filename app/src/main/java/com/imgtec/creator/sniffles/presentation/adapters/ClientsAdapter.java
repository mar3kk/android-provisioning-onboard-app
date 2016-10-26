package com.imgtec.creator.sniffles.presentation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.api.pojo.Client;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ClientsAdapter extends BaseAdapter<Client, ClientsAdapter.ViewHolder> {

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list_item, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {
    Client item = data.get(position);
    holder.title.setText(item.getName());
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title) TextView title;
    @BindView(R.id.desc) TextView description;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
