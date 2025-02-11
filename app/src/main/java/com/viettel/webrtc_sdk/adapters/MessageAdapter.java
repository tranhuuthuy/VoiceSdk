package com.viettel.webrtc_sdk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.viettel.webrtc_sdk.R;
import com.viettel.webrtc_sdk.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Message.Type type = viewType == 0 ? Message.Type.USER : (viewType == 1 ? Message.Type.BOT : Message.Type.WAIT_ANSWER);

        if (type == Message.Type.USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_container_sent_message, parent, false);
            return new MessageAdapter.ViewHolder(view, type);
        } else if (type == Message.Type.BOT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_container_received_message, parent, false);
            return new MessageAdapter.ViewHolder(view, type);
        }
        if (type == Message.Type.WAIT_ANSWER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_container_wait, parent, false);
            return new MessageAdapter.ViewHolder(view, type);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.type == Message.Type.BOT || holder.type == Message.Type.USER) {
            holder.getTextView().setText(messages.get(position).getContent());
            SimpleDateFormat sdp = new SimpleDateFormat("HH:mm:ss");
            holder.getDateTimeView().setText(sdp.format(messages.get(position).getDate()));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages != null && !messages.isEmpty()) {
            Message message = messages.get(position);
            return message.getType() == Message.Type.USER ? 0 : (message.getType() == Message.Type.BOT ? 1 : 2);
        }
        return super.getItemViewType(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private TextView dateTimeView;

        private Message.Type type;

        public ViewHolder(View view, Message.Type type) {
            super(view);
            this.type = type;
            if (type == Message.Type.USER) {
                textView = view.findViewById(R.id.textSentMessage);
                dateTimeView = view.findViewById(R.id.textSentDateTime);
            } else if (type == Message.Type.BOT) {
                textView = view.findViewById(R.id.textReceivedMessage);
                dateTimeView = view.findViewById(R.id.textReceivedDateTime);
            }
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getDateTimeView() {
            return dateTimeView;
        }
    }


    private ArrayList<Message> messages;

    public MessageAdapter(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public MessageAdapter() {
        messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void removeLastWaitMessage() {
        if (messages != null && !messages.isEmpty()) {
            Message mes = messages.get(messages.size() - 1);
            if (mes.getType() == Message.Type.WAIT_ANSWER) {
                messages.remove(mes);
            }
        }
    }
}
