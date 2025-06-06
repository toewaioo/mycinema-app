package com.two.my_libs.views.popup;

/*
 * Created by Toewaioo on 4/24/25
 * Description: [Add class description here]
 */
public enum ArrowDirection {
    LEFT(0),
    RIGHT(1),
    TOP(2),
    BOTTOM(3),
    // CENTER
    LEFT_CENTER(4),
    RIGHT_CENTER(5),
    TOP_CENTER(6),
    BOTTOM_CENTER(7),
    // HORIZONTAL > RIGHT
    TOP_RIGHT(8),
    BOTTOM_RIGHT(9);


    private int value;

    ArrowDirection(int value) {
        this.value = value;
    }

    public static ArrowDirection fromInt(int value) {
        for (ArrowDirection arrowDirection : ArrowDirection.values()) {
            if (value == arrowDirection.getValue()) {
                return arrowDirection;
            }
        }
        return LEFT;
    }

    public int getValue() {
        return value;
    }
}