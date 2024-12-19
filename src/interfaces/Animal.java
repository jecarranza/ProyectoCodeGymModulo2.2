package interfaces;

import java.util.List;

public interface Animal extends Runnable{
    void mover(int MaxX, int MaxY);
    void comer(List<Object> comida);
    boolean estaMuerto();
    Animal Reproducir();
    String getIcon();
    int getX();
    int getY();
}
