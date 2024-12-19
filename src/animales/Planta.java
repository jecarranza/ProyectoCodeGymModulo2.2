package animales;

import interfaces.Recurso;

public class Planta implements Recurso {

    private boolean disponible = true;

    @Override
    public boolean estaDisponible() {
        return disponible;
    }

    @Override
    public void consumir() {
        disponible = false;
    }

    @Override
    public void regenerar() {
        disponible = true;
    }
}
