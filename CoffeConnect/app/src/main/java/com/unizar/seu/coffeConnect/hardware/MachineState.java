package com.unizar.seu.coffeConnect.hardware;

public enum MachineState {
    OFF ("Apagada"),
    TURNING_ON("Encendiendo"),
    READY ("Lista"),
    MAKING_ONE ("Preparando 1 café"),
    MAKING_TWO ("Preparando 2 cafés"),
    STEAM ("Produciendo vapor"),
    TURNING_OFF("Apagando"),
    NO_WATER ("Sin agua"),
    CONTAINER_FULL ("Depósito de café lleno"),
    FAILURE ("Avería"),
    DECALCIF ("Descalcificación");

    private final String name;

    private MachineState(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
