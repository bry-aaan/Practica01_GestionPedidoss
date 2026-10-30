package modelo;

import java.util.ArrayList;
import java.util.Locale;

public class GestionCafeteria {
    private ArrayList<Producto> productos;
    private ArrayList<Pedido> pedidos;
    private PersistenciaArchivo persistencia;

    public GestionCafeteria() {
        this.persistencia = new PersistenciaArchivo("productos.txt", "pedidos.txt", "detalles_pedido.txt");
        UtilGenerico util = new UtilGenerico();
        this.productos = util.crearLista();
        this.pedidos = util.crearLista();
        cargarDatosIniciales();
    }

    public void cargarDatosIniciales() {
        persistencia.inicializarArchivos();
        this.productos = persistencia.cargarProductos();
        this.pedidos = persistencia.cargarPedidos(this.productos);
    }

    public ArrayList<Producto> getProductos() { return productos; }
    public ArrayList<Pedido> getPedidos() { return pedidos; }

    public Producto buscarProductoPorId(int idProducto) {
        for (Producto p : productos) {
            if (p.getIdProducto() == idProducto) return p;
        }
        return null;
    }

    public boolean registrarProducto(int idProducto, String nombre, double precioUnitario, int stockInicial) {
        if (idProducto <= 0 || nombre == null || nombre.trim().isEmpty() || precioUnitario <= 0 || stockInicial < 0) {
            return false;
        }
        if (buscarProductoPorId(idProducto) != null) return false;
        
        Producto nuevoProducto = new Producto(idProducto, nombre, precioUnitario, stockInicial);
        new UtilGenerico().agregarElementos(this.productos, nuevoProducto);
        return persistencia.guardarProductos(productos);
    }
    
    public boolean editarProducto(int idProducto, String nuevoNombre, double nuevoPrecio) {
        Producto p = buscarProductoPorId(idProducto);
        if (p == null || nuevoNombre == null || nuevoNombre.trim().isEmpty() || nuevoPrecio <= 0) return false;
        p.setNombre(nuevoNombre);
        p.setPrecioUnitario(nuevoPrecio);
        return persistencia.guardarProductos(productos);
    }

    public boolean reponerStock(int idProducto, int cantidadAgregar) {
        Producto producto = buscarProductoPorId(idProducto);
        if (producto == null || cantidadAgregar <= 0) return false;
        if (producto.aumentarStock(cantidadAgregar)) {
            return persistencia.guardarProductos(productos);
        }
        return false;
    }

    public int generarSiguienteIdProducto() {
        int maxId = 0;
        for (Producto p : productos) {
            if (p.getIdProducto() > maxId) maxId = p.getIdProducto();
        }
        return maxId + 1;
    }

    public int generarSiguienteIdPedido() {
        int maxId = 0;
        for (Pedido p : pedidos) {
            if (p.getIdPedido() > maxId) maxId = p.getIdPedido();
        }
        return maxId + 1;
    }

    public int calcularCantidadEnPedido(Pedido pedido, int idProducto) {
        if (pedido == null) return 0;
        int acumulado = 0;
        for (DetallePedido dp : pedido.getDetalles()) {
            if (dp.getProducto().getIdProducto() == idProducto) {
                acumulado += dp.getCantidad();
            }
        }
        return acumulado;
    }

    public boolean agregarLineaAPedido(Pedido pedido, int idProducto, int cantidad) {
        if (pedido == null || cantidad <= 0) return false;
        Producto producto = buscarProductoPorId(idProducto);
        if (producto == null) return false;
        
        int cantidadYaEnPedido = calcularCantidadEnPedido(pedido, idProducto);
        if (cantidad + cantidadYaEnPedido > producto.getStock()) return false;
        
        DetallePedido nuevoDetalle = new DetallePedido(pedido.getIdPedido(), producto, cantidad, producto.getPrecioUnitario());
        pedido.agregarDetalle(nuevoDetalle);
        return true;
    }

    public boolean confirmarPedido(Pedido nuevoPedido) {
        if (nuevoPedido == null || nuevoPedido.getDetalles().isEmpty()) return false;
        
        for (DetallePedido dp : nuevoPedido.getDetalles()) {
            Producto prodCatalogo = buscarProductoPorId(dp.getProducto().getIdProducto());
            if (prodCatalogo == null || calcularCantidadEnPedido(nuevoPedido, prodCatalogo.getIdProducto()) > prodCatalogo.getStock()) {
                return false;
            }
        }
        for (DetallePedido dp : nuevoPedido.getDetalles()) {
            Producto prodCatalogo = buscarProductoPorId(dp.getProducto().getIdProducto());
            prodCatalogo.disminuirStock(dp.getCantidad());
        }
        nuevoPedido.calcularTotal();
        pedidos.add(nuevoPedido);
        return persistencia.guardarProductos(productos) && persistencia.guardarPedidos(pedidos);
    }

    public ArrayList<Pedido> obtenerPedidosPorFecha(String fecha) {
        ArrayList<Pedido> filtrados = new ArrayList<>();
        if (fecha == null) return filtrados;
        String fechaLimpia = fecha.trim();
        for (Pedido p : pedidos) {
            if (p.getFecha().equals(fechaLimpia)) filtrados.add(p);
        }
        return filtrados;
    }

    public double calcularTotalVentasPorFecha(String fecha) {
        double suma = 0.0;
        for (Pedido p : obtenerPedidosPorFecha(fecha)) suma += p.getTotal();
        return Math.round(suma * 100.0) / 100.0;
    }

    public String obtenerReporteDemandaProductos() {
        int n = productos.size();
        if (n == 0) return "No hay productos en el catálogo.";
        
        Producto[] copiaProductos = new Producto[n];
        int[] unidadesVendidas = new int[n];
        
        for (int i = 0; i < n; i++) {
            copiaProductos[i] = productos.get(i);
            unidadesVendidas[i] = 0;
        }

        for (Pedido p : pedidos) {
            for (DetallePedido det : p.getDetalles()) {
                for (int k = 0; k < n; k++) {
                    if (copiaProductos[k].getIdProducto() == det.getProducto().getIdProducto()) {
                        unidadesVendidas[k] += det.getCantidad();
                    }
                }
            }
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (unidadesVendidas[j] < unidadesVendidas[j + 1]) {
                    int tempU = unidadesVendidas[j];
                    unidadesVendidas[j] = unidadesVendidas[j + 1];
                    unidadesVendidas[j + 1] = tempU;
                    
                    Producto tempP = copiaProductos[j];
                    copiaProductos[j] = copiaProductos[j + 1];
                    copiaProductos[j + 1] = tempP;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("Pos #").append(i + 1).append(" | ").append(copiaProductos[i].getNombre())
              .append(" - Vendidos: ").append(unidadesVendidas[i]).append(" u\n");
        }
        return sb.toString();
    }
}