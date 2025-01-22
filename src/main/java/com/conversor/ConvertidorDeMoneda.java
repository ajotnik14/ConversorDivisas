package com.conversor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConvertidorDeMoneda {

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/52ef6d89aa543a0466559b34/latest/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            mostrarMenu();
            int opcion = obtenerOpcion(scanner);

            if (opcion == 9) {
                System.out.println("Gracias por usar nuestro Servicio. ¡Hasta pronto!");
                break;
            }

            Map<String, String> monedas = obtenerMonedas(opcion);
            if (monedas == null) {
                System.out.println("La opción ingresada no es válida. Por favor, intenta de nuevo.");
                continue;
            }

            System.out.print("Ingresa el monto de la divisa a convertir: ");
            double cantidad = scanner.nextDouble();

            try {
                convertirMoneda(monedas.get("base"), monedas.get("destino"), cantidad);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error al intentar realizar la conversión: " + e.getMessage());
            }

            continuar = preguntarContinuar(scanner);
        }
    }

    private static void mostrarMenu() {
        System.out.println("=== MENÚ PRINCIPAL ===");
        System.out.println("Selecciona la divisa a convertir:");
        System.out.println("1. Convertir USD a EUR");
        System.out.println("2. Convertir EUR a USD");
        System.out.println("3. Convertir USD a COP");
        System.out.println("4. Convertir COP a USD");
        System.out.println("5. Convertir USD a CAD");
        System.out.println("6. Convertir CAD a USD");
        System.out.println("7. Convertir EUR a COP");
        System.out.println("8. Convertir COP a EUR");
        System.out.println("9. Salir");
        System.out.print("Selecciona una opción (1-9): ");
    }

    private static int obtenerOpcion(Scanner scanner) {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // Limpiar el buffer
            return -1;
        }
    }

    private static Map<String, String> obtenerMonedas(int opcion) {
        Map<Integer, String[]> opcionesMonedas = new HashMap<>();
        opcionesMonedas.put(1, new String[]{"USD", "EUR"});
        opcionesMonedas.put(2, new String[]{"EUR", "USD"});
        opcionesMonedas.put(3, new String[]{"USD", "COP"});
        opcionesMonedas.put(4, new String[]{"COP", "USD"});
        opcionesMonedas.put(5, new String[]{"USD", "CAD"});
        opcionesMonedas.put(6, new String[]{"CAD", "USD"});
        opcionesMonedas.put(7, new String[]{"EUR", "COP"});
        opcionesMonedas.put(8, new String[]{"COP", "EUR"});

        if (opcionesMonedas.containsKey(opcion)) {
            String[] monedas = opcionesMonedas.get(opcion);
            Map<String, String> mapa = new HashMap<>();
            mapa.put("base", monedas[0]);
            mapa.put("destino", monedas[1]);
            return mapa;
        }
        return null;
    }

    private static boolean preguntarContinuar(Scanner scanner) {
        System.out.print("¿Deseas realizar otra conversión? (s/n): ");
        String respuesta = scanner.next();
        return respuesta.equalsIgnoreCase("s");
    }

    public static void convertirMoneda(String monedaBase, String monedaDestino, double cantidad)
            throws IOException, InterruptedException {

        String url = API_URL + monedaBase;
        HttpClient cliente = HttpClient.newHttpClient();
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
            JsonObject rates = json.getAsJsonObject("conversion_rates");

            if (rates.has(monedaDestino)) {
                double tasa = rates.get(monedaDestino).getAsDouble();
                double resultado = cantidad * tasa;

                System.out.printf("Tasa de cambio: 1 %s = %.2f %s%n", monedaBase, tasa, monedaDestino);
                System.out.printf("Los %.2f %s equivalen a %.2f %s.%n", cantidad, monedaBase, resultado, monedaDestino);
            } else {
                System.out.println("La moneda de destino no es válida.");
            }
        } else {
            System.out.println("Error al conectar con la API. Código de estado: " + respuesta.statusCode());
        }
    }
}
