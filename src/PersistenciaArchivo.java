import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PersistenciaArchivo {
    private File archivoProductos;
    private File archivoPedidos;
    private File archivoDetalles;

    public PersistenciaArchivo(String rutaProductos, String rutaPedidos, String rutaDetalles) {
        this.archivoProductos = new File(rutaProductos);
        this.archivoPedidos = new File(rutaPedidos);
        this.archivoDetalles = new File(rutaDetalles);
    }

    public void inicializarArchivos() {
        try {
            if (!archivoProductos.exists()) {
                archivoProductos.createNewFile();
            }
            if (!archivoPedidos.exists()) {
                archivoPedidos.createNewFile();
            }
            if (!archivoDetalles.exists()) {
                archivoDetalles.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al inicializar los archivos de datos: " + e.getMessage());
        }
    }

    public ArrayList<Producto> cargarProductos() {
        ArrayList<Producto> listaProductos = new ArrayList<Producto>();
        if (!archivoProductos.exists()) {
            return listaProductos;
        }
        BufferedReader lector = null;
        try {
            lector = new BufferedReader(new FileReader(archivoProductos));
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split(";");
                    if (partes.length == 4) {
                        int id = Integer.parseInt(partes[0].trim());
                        String nombre = partes[1].trim();
                        double precio = Double.parseDouble(partes[2].trim().replace(",", "."));
                        int stock = Integer.parseInt(partes[3].trim());
                        if (id > 0 && precio > 0 && stock >= 0) {
                            listaProductos.add(new Producto(id, nombre, precio, stock));
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Advertencia al leer productos desde archivo: " + e.getMessage());
        } finally {
            if (lector != null) {
                try {
                    lector.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar lector de productos: " + e.getMessage());
                }
            }
        }
        return listaProductos;
    }

    public boolean guardarProductos(ArrayList<Producto> listaProductos) {
        BufferedWriter escritor = null;
        try {
            escritor = new BufferedWriter(new FileWriter(archivoProductos, false));
            for (int i = 0; i < listaProductos.size(); i++) {
                escritor.write(listaProductos.get(i).aLineaArchivo());
                escritor.newLine();
            }
            escritor.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar productos en archivo: " + e.getMessage());
            return false;
        } finally {
            if (escritor != null) {
                try {
                    escritor.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar escritor de productos: " + e.getMessage());
                }
            }
        }
    }

    public ArrayList<Pedido> cargarPedidos(ArrayList<Producto> catalogoProductos) {
        ArrayList<Pedido> listaPedidos = new ArrayList<Pedido>();
        if (!archivoPedidos.exists()) {
            return listaPedidos;
        }
        BufferedReader lector = null;
        try {
            lector = new BufferedReader(new FileReader(archivoPedidos));
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split(";");
                    if (partes.length == 4) {
                        int idPedido = Integer.parseInt(partes[0].trim());
                        String fecha = partes[1].trim();
                        String cliente = partes[2].trim();
                        if (idPedido > 0) {
                            Pedido pedido = new Pedido(idPedido, fecha, cliente);
                            listaPedidos.add(pedido);
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Advertencia al leer pedidos desde archivo: " + e.getMessage());
        } finally {
            if (lector != null) {
                try {
                    lector.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar lector de pedidos: " + e.getMessage());
                }
            }
        }
        cargarDetallesEnPedidos(listaPedidos, catalogoProductos);
        return listaPedidos;
    }

    private void cargarDetallesEnPedidos(ArrayList<Pedido> listaPedidos, ArrayList<Producto> catalogoProductos) {
        if (!archivoDetalles.exists()) {
            return;
        }
        BufferedReader lector = null;
        try {
            lector = new BufferedReader(new FileReader(archivoDetalles));
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split(";");
                    if (partes.length == 5) {
                        int idPedido = Integer.parseInt(partes[0].trim());
                        int idProducto = Integer.parseInt(partes[1].trim());
                        int cantidad = Integer.parseInt(partes[2].trim());
                        double precioUnitario = Double.parseDouble(partes[3].trim().replace(",", "."));
                        Pedido pedidoEncontrado = buscarPedidoEnLista(listaPedidos, idPedido);
                        Producto productoEncontrado = buscarProductoEnLista(catalogoProductos, idProducto);
                        if (pedidoEncontrado != null && productoEncontrado != null && cantidad > 0) {
                            DetallePedido detalle = new DetallePedido(idPedido, productoEncontrado, cantidad, precioUnitario);
                            pedidoEncontrado.agregarDetalle(detalle);
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Advertencia al leer detalles de pedido desde archivo: " + e.getMessage());
        } finally {
            if (lector != null) {
                try {
                    lector.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar lector de detalles: " + e.getMessage());
                }
            }
        }
    }

    public boolean guardarPedidos(ArrayList<Pedido> listaPedidos) {
        BufferedWriter escritorPedidos = null;
        BufferedWriter escritorDetalles = null;
        try {
            escritorPedidos = new BufferedWriter(new FileWriter(archivoPedidos, false));
            escritorDetalles = new BufferedWriter(new FileWriter(archivoDetalles, false));
            for (int i = 0; i < listaPedidos.size(); i++) {
                Pedido pedido = listaPedidos.get(i);
                escritorPedidos.write(pedido.aLineaArchivo());
                escritorPedidos.newLine();
                ArrayList<DetallePedido> detalles = pedido.getDetalles();
                for (int j = 0; j < detalles.size(); j++) {
                    escritorDetalles.write(detalles.get(j).aLineaArchivo());
                    escritorDetalles.newLine();
                }
            }
            escritorPedidos.flush();
            escritorDetalles.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Error al guardar pedidos y detalles en archivo: " + e.getMessage());
            return false;
        } finally {
            if (escritorPedidos != null) {
                try {
                    escritorPedidos.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar escritor de pedidos: " + e.getMessage());
                }
            }
            if (escritorDetalles != null) {
                try {
                    escritorDetalles.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar escritor de detalles: " + e.getMessage());
                }
            }
        }
    }

    private Producto buscarProductoEnLista(ArrayList<Producto> listaProductos, int idProducto) {
        for (int i = 0; i < listaProductos.size(); i++) {
            if (listaProductos.get(i).getIdProducto() == idProducto) {
                return listaProductos.get(i);
            }
        }
        return null;
    }

    private Pedido buscarPedidoEnLista(ArrayList<Pedido> listaPedidos, int idPedido) {
        for (int i = 0; i < listaPedidos.size(); i++) {
            if (listaPedidos.get(i).getIdPedido() == idPedido) {
                return listaPedidos.get(i);
            }
        }
        return null;
    }
}