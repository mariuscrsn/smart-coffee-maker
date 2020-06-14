package com.unizar.seu.coffeConnect.model;

import com.unizar.seu.coffeConnect.R;

public class RecommendedCoffees {

    public Coffee[] recCoffees;

    public RecommendedCoffees() {
        recCoffees = new Coffee[]{ new Coffee("Personalizado", 100, 20, 2, R.drawable.personal_1),
                    new Coffee("Espresso", 40, 10, 1, R.drawable.espresso),
                    new Coffee("Largo", 200, 20, 1, R.drawable.largo),
                    new Coffee("Capuchino", 80, 5, 1, R.drawable.cappuchino)};
    }

    public RecommendedCoffees(Coffee[] recCoffees) {
        this.recCoffees = recCoffees;
    }

    public Coffee[] getRecCoffees() {
        return recCoffees;
    }
}
