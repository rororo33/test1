package com.example.todayrecipe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todayrecipe.R;
import com.example.todayrecipe.model.Ingredient;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<Ingredient> ingredients;

    public IngredientAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_display, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView amountText;

        IngredientViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.ingredientNameText);
            amountText = itemView.findViewById(R.id.ingredientAmountText);
        }

        void bind(Ingredient ingredient) {
            nameText.setText(ingredient.getName());
            String amount = ingredient.getAmount() + " " + ingredient.getUnit();
            amountText.setText(amount);

            if (ingredient.isOptional()) {
                nameText.setText(ingredient.getName() + " (선택)");
            }
        }
    }
}