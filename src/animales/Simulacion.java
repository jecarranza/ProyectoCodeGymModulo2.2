package animales;


import interfaces.Animal;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class Simulacion {
    public static final int GRID_SIZE = 20; // Tamaño del tablero
    private final List<Animal> animales = Collections.synchronizedList(new ArrayList<>());
    private final Planta[][] plantas = new Planta[GRID_SIZE][GRID_SIZE];
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Random random = new Random();
    private static Simulacion instancia;

    public static Simulacion getInstancia() {
        if (instancia == null) {
            instancia = new Simulacion();
        }
        return instancia;
    }

    private final Map<Class<? extends Animal>, Integer> cantidadPorEspecie = Map.of(
            Lobo.class, 30,
            Conejo.class, 150,
            Ciervo.class, 20,
            Raton.class, 500
    );

    public synchronized void agregarAnimal(Animal animal) {
        animales.add(animal);
    }

    public synchronized void eliminarAnimal(Animal animal) {
        animales.remove(animal);
    }

    public Simulacion() {
        inicializarPlantas();
        inicializarAnimales();
    }

    private void inicializarPlantas() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (random.nextDouble() < 0.3) { // 30% de probabilidad de planta
                    plantas[i][j] = new Planta();
                }
            }
        }
    }

    private void inicializarAnimales() {
        for (Map.Entry<Class<? extends Animal>, Integer> entry : cantidadPorEspecie.entrySet()) {
            Class<? extends Animal> especie = entry.getKey();
            int cantidad = entry.getValue();
            for (int i = 0; i < cantidad; i++) {
                try {
                    Animal animal = especie.getConstructor(int.class, int.class).newInstance(
                            random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)
                    );
                    animales.add(animal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void ejecutar(int ciclos) throws InterruptedException {
        // Conteo inicial de animales
        System.out.println("Iniciando simulación...");
        imprimirConteoAnimales();

        for (int ciclo = 1; ciclo <= ciclos; ciclo++) {
            System.out.println("\n--- Ciclo " + ciclo + " ---");
            Thread.sleep(700);
            procesarPlantas();
            procesarAnimales();
            imprimirTablero();
            Thread.sleep(700);
        }

        executor.shutdown();
        System.out.println("\nSimulación finalizada.");
        imprimirConteoAnimales();
    }

    private void procesarPlantas() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (plantas[i][j] != null) {
                    final int x = i;
                    final int y = j;
                    executor.execute(() -> {
                        if (!plantas[x][y].estaDisponible() && random.nextDouble() < 0.2) {
                            plantas[x][y].regenerar();
                        }
                    });
                }
            }
        }
    }

    private void procesarAnimales() {
        List<Animal> animalesParaEliminar = new ArrayList<>();
        List<Animal> animalesParaAgregar = new ArrayList<>();

        synchronized (animales) {
            for (Animal animal : animales) {
                executor.execute(() -> {
                    List<Object> comida = obtenerComidaEn(animal.getX(), animal.getY());
                    animal.comer(comida);
                    animal.mover(GRID_SIZE, GRID_SIZE);

                    // Verificar si el animal está muerto
                    if (animal.estaMuerto()) {
                        synchronized (animalesParaEliminar) {
                            animalesParaEliminar.add(animal); // Marcar para eliminar
                        }
                    } else {
                        // Intentar reproducirse
                        Animal nuevaCria = animal.Reproducir();
                        if (nuevaCria != null) {
                            synchronized (animalesParaAgregar) {
                                animalesParaAgregar.add(nuevaCria); // Marcar para agregar
                            }
                        }
                    }
                });
            }
        }

        // Esperar a que los hilos terminen
        try {
            executor.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Actualizar la lista de animales fuera de los hilos
        synchronized (animales) {
            animales.removeAll(animalesParaEliminar);
            animales.addAll(animalesParaAgregar);
        }
    }

    public synchronized List<Object> obtenerComidaEn(int x, int y) {
        List<Object> comida = new ArrayList<>();
        if (plantas[x][y] != null && plantas[x][y].estaDisponible()) {
            comida.add(plantas[x][y]);
        }
        synchronized (animales) {
            for (Animal animal : animales) {
                if (animal.getX() == x && animal.getY() == y) {
                    comida.add(animal);
                }
            }
        }
        return comida;
    }

    private void imprimirTablero() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                String icono = "⬜";
                if (plantas[i][j] != null && plantas[i][j].estaDisponible()) {
                    icono = "🌱";
                }
                synchronized (animales) {
                    for (Animal animal : animales) {
                        if (animal.getX() == i && animal.getY() == j) {
                            icono = animal.getIcon();
                            break;
                        }
                    }
                }
                System.out.print(icono + " ");
            }
            System.out.println();
        }
    }

    private void imprimirConteoAnimales() {
        Map<Class<?>, Long> conteoPorEspecie = animales.stream()
                .collect(Collectors.groupingBy(Animal::getClass, Collectors.counting()));

        System.out.println("📊 Conteo de animales:");
        for (Map.Entry<Class<?>, Long> entry : conteoPorEspecie.entrySet()) {
            System.out.println(entry.getKey().getSimpleName() + ": " + entry.getValue());
        }
        System.out.println("Total: " + animales.size() + " animales.");
    }


    public static void main(String[] args) throws InterruptedException {
        Simulacion simulacion = new Simulacion();
        simulacion.ejecutar(2);
    }
}

