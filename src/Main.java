import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    private static Scanner entrada = new Scanner(System.in);
    private static GestionCafeteria sistema = new GestionCafeteria();

    public static void main(String[] args) {
        int opcion = 0;
        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opcion (1-7): ");
            switch (opcion) {
                case 1:
                    opcionRegistrarProducto();
                    break;
                case 2:
                    opcionListarProductos();
                    break;
                case 3:
                    opcionReponerStock();
                    break;
                case 4:
                    opcionRegistrarPedido();
                    break;
                case 5:
                    opcionReporteVentasDelDia();
                    break;
                case 6:
                    opcionReporteProductosMayorDemanda();
                    break;
                case 7:
                    System.out.println("\nCerrando el sistema de la cafeteria El Buen Sabor. Datos conservados en archivos.");
                    break;
                default:
                    System.out.println("Opcion no valida. Por favor, elija un numero entre 1 y 7.");
                    break;
            }
        } while (opcion != 7);
        entrada.close();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n=======================================================");
        System.out.println("   CAFETERIA UNIVERSITARIA EL BUEN SABOR - SISTEMA     ");
        System.out.println("=======================================================");
        System.out.println("1. Registrar nuevo producto en el catalogo");
        System.out.println("2. Listar catalogo de productos");
        System.out.println("3. Reponer stock de un producto");
        System.out.println("4. Registrar nuevo pedido");
        System.out.println("5. Consultar reporte de ventas del dia");
        System.out.println("6. Consultar productos con mayor demanda");
        System.out.println("7. Salir del sistema");
        System.out.println("=======================================================");
    }

    private static void opcionRegistrarProducto() {
        System.out.println("\n--- REGISTRO DE NUEVO PRODUCTO ---");
        int idSugerido = sistema.generarSiguienteIdProducto();
        System.out.println("ID sugerido disponible: " + idSugerido);
        int id = leerEntero("Ingrese el ID del producto: ");
        if (id <= 0) {
            System.out.println("Error: El ID del producto debe ser un entero positivo mayor a cero.");
            return;
        }
        if (sistema.buscarProductoPorId(id) != null) {
            System.out.println("Error: Ya existe un producto registrado con el ID " + id + ".");
            return;
        }
        String nombre = leerTextoNoVacio("Ingrese el nombre del producto: ");
        double precio = leerDecimal("Ingrese el precio unitario ($): ");
        if (precio <= 0) {
            System.out.println("Error: El precio unitario debe ser mayor a cero.");
            return;
        }
        int stock = leerEntero("Ingrese el stock inicial: ");
        if (stock < 0) {
            System.out.println("Error: El stock inicial no puede ser negativo.");
            return;
        }
        boolean exito = sistema.registrarProducto(id, nombre, precio, stock);
        if (exito) {
            System.out.println("Producto registrado y guardado correctamente en productos.txt.");
        } else {
            System.out.println("No se pudo registrar el producto. Verifique los datos ingresados.");
        }
    }

    private static void opcionListarProductos() {
        System.out.println("\n--- CATALOGO DE PRODUCTOS DISPONIBLES ---");
        ArrayList<Producto> lista = sistema.getProductos();
        if (lista.isEmpty()) {
            System.out.println("No existen productos registrados en el catalogo actualmente.");
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            System.out.println(lista.get(i).toString());
        }
    }

    private static void opcionReponerStock() {
        System.out.println("\n--- REPOSICION DE STOCK DE PRODUCTO ---");
        if (sistema.getProductos().isEmpty()) {
            System.out.println("El catalogo esta vacio. Registre productos primero.");
            return;
        }
        opcionListarProductos();
        int id = leerEntero("Ingrese el ID del producto a reponer: ");
        Producto producto = sistema.buscarProductoPorId(id);
        if (producto == null) {
            System.out.println("Error: No se encontro ningun producto con el ID " + id + ".");
            return;
        }
        int cantidad = leerEntero("Ingrese la cantidad de unidades que desea agregar al stock: ");
        if (cantidad <= 0) {
            System.out.println("Error: La cantidad a reponer debe ser mayor a cero.");
            return;
        }
        boolean exito = sistema.reponerStock(id, cantidad);
        if (exito) {
            System.out.println("Stock actualizado con exito. Nuevo stock de '" + producto.getNombre() + "': " + producto.getStock());
        } else {
            System.out.println("Error al actualizar el stock del producto.");
        }
    }

    private static void opcionRegistrarPedido() {
        System.out.println("\n--- REGISTRO DE NUEVO PEDIDO ---");
        if (sistema.getProductos().isEmpty()) {
            System.out.println("No se pueden registrar pedidos porque el catalogo de productos esta vacio.");
            return;
        }
        int idPedido = sistema.generarSiguienteIdPedido();
        String fecha = leerFechaValida("Ingrese la fecha del pedido (formato AAAA-MM-DD, ej. 2026-10-21): ");
        String cliente = leerTextoNoVacio("Ingrese el nombre y tipo de cliente (ej. Juan Perez - Estudiante): ");

        Pedido nuevoPedido = new Pedido(idPedido, fecha, cliente);
        boolean agregandoProductos = true;

        while (agregandoProductos) {
            opcionListarProductos();
            int idProd = leerEntero("\nIngrese el ID del producto a agregar (o 0 para finalizar la seleccion): ");
            if (idProd == 0) {
                agregandoProductos = false;
            } else {
                Producto prod = sistema.buscarProductoPorId(idProd);
                if (prod == null) {
                    System.out.println("Error: No existe un producto con el ID " + idProd + ".");
                } else {
                    int enPedido = sistema.calcularCantidadEnPedido(nuevoPedido, idProd);
                    int disponibleReal = prod.getStock() - enPedido;
                    if (disponibleReal <= 0) {
                        System.out.println("Error: El producto '" + prod.getNombre() + "' no tiene unidades disponibles adicionales.");
                    } else {
                        System.out.println("Producto seleccionado: " + prod.getNombre() + " | Disponible para agregar: " + disponibleReal);
                        int cantidad = leerEntero("Ingrese la cantidad deseada: ");
                        if (cantidad <= 0) {
                            System.out.println("Error: La cantidad solicitada debe ser mayor a cero.");
                        } else if (cantidad > disponibleReal) {
                            System.out.println("Error: Stock insuficiente. Solicito " + cantidad + " pero solo quedan " + disponibleReal + " disponibles.");
                        } else {
                            boolean agregado = sistema.agregarLineaAPedido(nuevoPedido, idProd, cantidad);
                            if (agregado) {
                                System.out.printf(Locale.US, "Linea agregada con exito. Total acumulado del pedido: $%.2f\n", nuevoPedido.getTotal());
                            } else {
                                System.out.println("No se pudo agregar el producto al pedido.");
                            }
                        }
                    }
                }
            }
        }

        if (nuevoPedido.getDetalles().isEmpty()) {
            System.out.println("Operacion cancelada: El pedido no contiene ningun producto.");
            return;
        }

        System.out.println("\n--- RESUMEN PREVIO DEL PEDIDO ---");
        System.out.print(nuevoPedido.toString());
        String confirmacion = leerTextoNoVacio("Desea confirmar y guardar este pedido? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S") || confirmacion.equalsIgnoreCase("SI")) {
            boolean confirmado = sistema.confirmarPedido(nuevoPedido);
            if (confirmado) {
                System.out.println("Pedido Nro. " + nuevoPedido.getIdPedido() + " confirmado, stock actualizado y archivos guardados.");
            } else {
                System.out.println("Error al confirmar y guardar el pedido.");
            }
        } else {
            System.out.println("Pedido descartado por el usuario. El inventario no ha sido modificado.");
        }
    }

    private static void opcionReporteVentasDelDia() {
        System.out.println("\n--- REPORTE DE VENTAS DEL DIA ---");
        String fecha = leerFechaValida("Ingrese la fecha a consultar (formato AAAA-MM-DD): ");
        ArrayList<Pedido> pedidosDia = sistema.obtenerPedidosPorFecha(fecha);
        if (pedidosDia.isEmpty()) {
            System.out.println("No se registraron pedidos en la fecha " + fecha + ".");
            System.out.println("Total vendido en el dia: $0.00");
            return;
        }
        System.out.println("Pedidos registrados en la fecha " + fecha + ":");
        for (int i = 0; i < pedidosDia.size(); i++) {
            System.out.print(pedidosDia.get(i).toString());
            System.out.println("-------------------------------------------------------");
        }
        double totalDia = sistema.calcularTotalVentasPorFecha(fecha);
        System.out.printf(Locale.US, "CANTIDAD DE PEDIDOS EN EL DIA: %d\n", pedidosDia.size());
        System.out.printf(Locale.US, "MONTO TOTAL VENDIDO EN EL DIA (%s): $%.2f\n", fecha, totalDia);
    }

    private static void opcionReporteProductosMayorDemanda() {
        System.out.println("\n--- REPORTE DE PRODUCTOS CON MAYOR DEMANDA ---");
        ArrayList<String> reporte = sistema.obtenerReporteDemandaProductos();
        if (reporte.isEmpty()) {
            System.out.println("No hay productos registrados en el catalogo para evaluar su demanda.");
            return;
        }
        for (int i = 0; i < reporte.size(); i++) {
            System.out.println(reporte.get(i));
        }
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = entrada.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor, ingrese un numero entero valido.");
            }
        }
    }

    private static double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = entrada.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(linea);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor, ingrese un valor numerico valido (ej. 1.50).");
            }
        }
    }

    private static String leerTextoNoVacio(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("El campo no puede estar vacio. Intente nuevamente.");
        }
    }

    private static String leerFechaValida(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim();
            if (texto.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return texto;
            }
            System.out.println("Formato de fecha invalido. Utilice exactamente AAAA-MM-DD (ejemplo: 2026-10-21).");
        }
    }
}