package com.example.todayrecipe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.R;
import com.example.todayrecipe.model.Comment;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameText;
        TextView contentText;
        TextView dateText;

        CommentViewHolder(View itemView) {
            super(itemView);
            nicknameText = itemView.findViewById(R.id.nicknameText);
            contentText = itemView.findViewById(R.id.contentText);
            dateText = itemView.findViewById(R.id.dateText);
        }

        void bind(Comment comment) {
            nicknameText.setText(comment.getUserNickname() != null ? comment.getUserNickname() : "익명");
            contentText.setText(comment.getContent());

            if (comment.getCommentDate() != null) {
                dateText.setText(dateFormat.format(comment.getCommentDate()));
            }
        }
    }
}