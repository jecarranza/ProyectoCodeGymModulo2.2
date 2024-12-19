package animales;

import interfaces.Animal;
import interfaces.Carnivoro;
import interfaces.Herbivoro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Lobo implements Animal, Carnivoro {
    private int x, y;
    private final int MAX_EDAD = 15;
    private int energia;
    private int edad = 0;
    private final java.util.Map<Class<? extends Herbivoro>, Integer> probabilidadesCaza;

    public Lobo(int x, int y) {
        this.x = x;
        this.y = y;
        this.energia = 10;
        probabilidadesCaza = Map.of(
                Conejo.class, 60,
                Caballo.class, 10,
                Ciervo.class, 15,
                Raton.class, 80,
                Cabra.class, 60,
                Oveja.class, 70,
                Jabali.class, 15,
                Bufalo.class, 10,
                Pato.class, 40,
                Oruga.class, 0
        );
    }

    @Override
    public void mover(int maxX, int maxY) {
        x = Math.max(0, Math.min(maxX - 1, x + ThreadLocalRandom.current().nextInt(-1, 2)));
        y = Math.max(0, Math.min(maxY - 1, y + ThreadLocalRandom.current().nextInt(-1, 2)));
    }

    @Override
    public void comer(List<Object> comida) {
        for (Object item : comida) {
            if (item instanceof Herbivoro && probabilidadesCaza.containsKey(item.getClass())) {
                int probabilidad = probabilidadesCaza.get(item.getClass());
                if (ThreadLocalRandom.current().nextInt(100) < probabilidad) {
                    comida.remove(item);
                    energia += 15;
                    System.out.println("Lobo comió a un " + item.getClass().getSimpleName());
                    return;
                }
            }
        }
    }

    @Override
    public void cazar(List<Object> presas) {
        comer(presas);
    }

    @Override
    public boolean estaMuerto() {
        return energia <= 0 || edad >= MAX_EDAD; // Morir si la energía es cero o supera la edad máxima
    }

    @Override
    public Animal Reproducir() {
        if (energia >= 50) { // Solo reproducirse si tiene suficiente energía
            energia -= 20; // Reproducirse cuesta energía
            return new Lobo(x, y); // Nueva instancia de lobo en la misma posición
        }
        return null; // No reproducirse si no tiene suficiente energía
    }

    @Override
    public String getIcon() {
        return "🐺";
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
        // Acción del lobo en un ciclo de simulación
        Simulacion simulacion = Simulacion.getInstancia();
        List<Object> comida = simulacion.obtenerComidaEn(x, y); // Obtener comida disponible en la posición
        comer(comida);
        mover(Simulacion.GRID_SIZE, Simulacion.GRID_SIZE);
        edad++;
        energia--;

        // Verificar si está muerto
        if (estaMuerto()) {
            simulacion.eliminarAnimal(this);
        } else if (ThreadLocalRandom.current().nextDouble() < 0.2) { // 20% de probabilidad de reproducirse
            simulacion.agregarAnimal(Reproducir());
        }
    }
}





