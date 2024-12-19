package animales;

import interfaces.Animal;
import interfaces.Carnivoro;
import interfaces.Herbivoro;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Boa implements Animal, Carnivoro {
    private int x, y;
    private final int MAX_EDAD = 15;
    private int energia = 10;
    private int edad = 0;
    private final java.util.Map<Class<? extends Herbivoro>, Integer> probabilidadesCaza;

    public Boa(int x, int y) {
        this.x = x;
        this.y = y;
        this.energia = 10;
        probabilidadesCaza = Map.of(
                Conejo.class, 20,
                Caballo.class, 0,
                Ciervo.class, 0,
                Raton.class, 0,
                Cabra.class, 0,
                Oveja.class, 0,
                Jabali.class, 0,
                Bufalo.class, 0,
                Pato.class, 10,
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
                    System.out.println("Boa comió a un " + item.getClass().getSimpleName());
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
        return energia <= 0 || MAX_EDAD <= 0;
    }

    @Override
    public Animal Reproducir() {
        return new Lobo(x, y);
    }

    @Override
    public String getIcon() {
        return "🐍";
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
        Simulacion simulacion = Simulacion.getInstancia();
        List<Object> comida = simulacion.obtenerComidaEn(x, y); // Obtener comida disponible en la posición
        comer(comida);
        mover(Simulacion.GRID_SIZE, Simulacion.GRID_SIZE);
        edad++;
        energia--;

        if (estaMuerto()) {
            simulacion.eliminarAnimal(this);
        } else if (ThreadLocalRandom.current().nextDouble() < 0.2) { // 20% de probabilidad de reproducirse
            simulacion.agregarAnimal(Reproducir());
        }
    }
}
