package com.naemys.mychat.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.naemys.mychat.R;
import com.naemys.mychat.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item,
                parent,
                false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.userNameTextView.setText(message.getUserName());

        boolean isText = message.getImageUrl() == null;

        if (isText) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(message.getText());

            holder.photoImageView.setVisibility(View.GONE);
        } else {
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.photoImageView.getContext())
                    .load(Uri.parse(message.getImageUrl()))
                    .into(holder.photoImageView);

            holder.messageTextView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView photoImageView;
        private TextView messageTextView;
        private TextView userNameTextView;

        MessageViewHolder(View itemView) {
            super(itemView);

            photoImageView = itemView.findViewById(R.id.photoImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
        }
    }
}
