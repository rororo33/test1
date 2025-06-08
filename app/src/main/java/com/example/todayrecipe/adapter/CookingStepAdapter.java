package com.example.todayrecipe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.todayrecipe.R;
import com.example.todayrecipe.model.CookingStep;
import java.util.List;

public class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.StepViewHolder> {
    private List<CookingStep> steps;

    public CookingStepAdapter(List<CookingStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cooking_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        CookingStep step = steps.get(position);
        holder.bind(step);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumberText;
        TextView descriptionText;
        ImageView stepImage;

        StepViewHolder(View itemView) {
            super(itemView);
            stepNumberText = itemView.findViewById(R.id.stepNumberText);
            descriptionText = itemView.findViewById(R.id.stepDescriptionText);
            stepImage = itemView.findViewById(R.id.stepImage);
        }

        void bind(CookingStep step) {
            stepNumberText.setText("Step " + step.getStepNumber());
            descriptionText.setText(step.getDescription());

            if (step.getImageUrl() != null && !step.getImageUrl().isEmpty()) {
                stepImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(step.getImageUrl())
                        .into(stepImage);
            } else {
                stepImage.setVisibility(View.GONE);
            }
        }
    }
}