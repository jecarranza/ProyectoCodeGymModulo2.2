package animales;

import interfaces.Animal;
import interfaces.Herbivoro;
import interfaces.Recurso;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Raton implements Animal, Herbivoro {
    private int x, y;
    private int energia = 5;
    private int edad = 0;
    private final int MAX_EDAD = 10;

    public Raton(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void mover(int maxX, int maxY) {
        x = Math.max(0, Math.min(maxX - 1, x + ThreadLocalRandom.current().nextInt(-1, 2)));
        y = Math.max(0, Math.min(maxY - 1, y + ThreadLocalRandom.current().nextInt(-1, 2)));
    }

    @Override
    public void comer(List<Object> comida) {
        for (Object item : comida) {
            if (item instanceof Recurso recurso && recurso.estaDisponible()) {
                recurso.consumir();
                energia += 3;
                System.out.println("Raton comió una planta.");
                return;
            }
        }
    }

    @Override
    public void comerPlanta(Recurso planta) {
        if (planta.estaDisponible()) {
            planta.consumir();
            energia += 3;
        }
    }

    @Override
    public boolean estaMuerto() {
        return energia <= 0 || edad >= MAX_EDAD;
    }

    @Override
    public Animal Reproducir() {
        return new Conejo(x, y);
    }

    @Override
    public String getIcon() {
        return "🐭";
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void run() {
        // Acción del conejo en un ciclo de simulación
        Simulacion simulacion = Simulacion.getInstancia();
        List<Object> comida = simulacion.obtenerComidaEn(x, y); // Obtener comida disponible
        comer(comida);
        mover(Simulacion.GRID_SIZE, Simulacion.GRID_SIZE);
        edad++;
        energia--;

        // Verificar si está muerto
        if (estaMuerto()) {
            simulacion.eliminarAnimal(this);
        } else if (ThreadLocalRandom.current().nextDouble() < 0.3) { // 30% de probabilidad de reproducirse
            simulacion.agregarAnimal(Reproducir());
        }
    }
}
