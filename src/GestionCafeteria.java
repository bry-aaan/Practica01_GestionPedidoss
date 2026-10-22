import java.util.ArrayList;
import java.util.Locale;

public class GestionCafeteria {
    private ArrayList<Producto> productos;
    private ArrayList<Pedido> pedidos;
    private PersistenciaArchivo persistencia;

    public GestionCafeteria() {
        this.persistencia = new PersistenciaArchivo("productos.txt", "pedidos.txt", "detalles_pedido.txt");
        this.productos = new ArrayList<Producto>();
        this.pedidos = new ArrayList<Pedido>();
        cargarDatosIniciales();
    }

    public void cargarDatosIniciales() {
        persistencia.inicializarArchivos();
        this.productos = persistencia.cargarProductos();
        this.pedidos = persistencia.cargarPedidos(this.productos);
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public ArrayList<Pedido> getPedidos() {
        return pedidos;
    }

    public Producto buscarProductoPorId(int idProducto) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getIdProducto() == idProducto) {
                return productos.get(i);
            }
        }
        return null;
    }

    public boolean registrarProducto(int idProducto, String nombre, double precioUnitario, int stockInicial) {
        if (idProducto <= 0 || nombre == null || nombre.trim().isEmpty() || precioUnitario <= 0 || stockInicial < 0) {
            return false;
        }
        if (buscarProductoPorId(idProducto) != null) {
            return false;
        }
        Producto nuevoProducto = new Producto(idProducto, nombre, precioUnitario, stockInicial);
        productos.add(nuevoProducto);
        return persistencia.guardarProductos(productos);
    }

    public boolean reponerStock(int idProducto, int cantidadAgregar) {
        Producto producto = buscarProductoPorId(idProducto);
        if (producto == null || cantidadAgregar <= 0) {
            return false;
        }
        boolean actualizado = producto.aumentarStock(cantidadAgregar);
        if (actualizado) {
            return persistencia.guardarProductos(productos);
        }
        return false;
    }

    public int generarSiguienteIdProducto() {
        int maxId = 0;
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getIdProducto() > maxId) {
                maxId = productos.get(i).getIdProducto();
            }
        }
        return maxId + 1;
    }

    public int generarSiguienteIdPedido() {
        int maxId = 0;
        for (int i = 0; i < pedidos.size(); i++) {
            if (pedidos.get(i).getIdPedido() > maxId) {
                maxId = pedidos.get(i).getIdPedido();
            }
        }
        return maxId + 1;
    }

    public int calcularCantidadEnPedido(Pedido pedido, int idProducto) {
        int acumulado = 0;
        if (pedido == null) {
            return 0;
        }
        ArrayList<DetallePedido> detalles = pedido.getDetalles();
        for (int i = 0; i < detalles.size(); i++) {
            if (detalles.get(i).getProducto().getIdProducto() == idProducto) {
                acumulado += detalles.get(i).getCantidad();
            }
        }
        return acumulado;
    }

    public boolean agregarLineaAPedido(Pedido pedido, int idProducto, int cantidad) {
        if (pedido == null || cantidad <= 0) {
            return false;
        }
        Producto producto = buscarProductoPorId(idProducto);
        if (producto == null) {
            return false;
        }
        int cantidadYaEnPedido = calcularCantidadEnPedido(pedido, idProducto);
        if (cantidad + cantidadYaEnPedido > producto.getStock()) {
            return false;
        }
        DetallePedido nuevoDetalle = new DetallePedido(pedido.getIdPedido(), producto, cantidad, producto.getPrecioUnitario());
        pedido.agregarDetalle(nuevoDetalle);
        return true;
    }

    public boolean confirmarPedido(Pedido nuevoPedido) {
        if (nuevoPedido == null || nuevoPedido.getDetalles().isEmpty()) {
            return false;
        }
        ArrayList<DetallePedido> detalles = nuevoPedido.getDetalles();
        for (int i = 0; i < detalles.size(); i++) {
            Producto prodCatalogo = buscarProductoPorId(detalles.get(i).getProducto().getIdProducto());
            if (prodCatalogo == null) {
                return false;
            }
            int totalRequerido = calcularCantidadEnPedido(nuevoPedido, prodCatalogo.getIdProducto());
            if (totalRequerido > prodCatalogo.getStock()) {
                return false;
            }
        }
        for (int i = 0; i < detalles.size(); i++) {
            DetallePedido detalle = detalles.get(i);
            Producto prodCatalogo = buscarProductoPorId(detalle.getProducto().getIdProducto());
            prodCatalogo.disminuirStock(detalle.getCantidad());
        }
        nuevoPedido.calcularTotal();
        pedidos.add(nuevoPedido);
        boolean productosGuardados = persistencia.guardarProductos(productos);
        boolean pedidosGuardados = persistencia.guardarPedidos(pedidos);
        return productosGuardados && pedidosGuardados;
    }

    public ArrayList<Pedido> obtenerPedidosPorFecha(String fecha) {
        ArrayList<Pedido> filtrados = new ArrayList<Pedido>();
        if (fecha == null) {
            return filtrados;
        }
        String fechaLimpia = fecha.trim();
        for (int i = 0; i < pedidos.size(); i++) {
            if (pedidos.get(i).getFecha().equals(fechaLimpia)) {
                filtrados.add(pedidos.get(i));
            }
        }
        return filtrados;
    }

    public double calcularTotalVentasPorFecha(String fecha) {
        ArrayList<Pedido> pedidosDia = obtenerPedidosPorFecha(fecha);
        double suma = 0.0;
        for (int i = 0; i < pedidosDia.size(); i++) {
            suma += pedidosDia.get(i).getTotal();
        }
        return Math.round(suma * 100.0) / 100.0;
    }

    public ArrayList<String> obtenerReporteDemandaProductos() {
        ArrayList<String> reporte = new ArrayList<String>();
        int n = productos.size();
        if (n == 0) {
            return reporte;
        }
        Producto[] copiaProductos = new Producto[n];
        int[] unidadesVendidas = new int[n];
        double[] montoGenerado = new double[n];

        for (int i = 0; i < n; i++) {
            copiaProductos[i] = productos.get(i);
            unidadesVendidas[i] = 0;
            montoGenerado[i] = 0.0;
        }

        for (int i = 0; i < pedidos.size(); i++) {
            ArrayList<DetallePedido> detalles = pedidos.get(i).getDetalles();
            for (int j = 0; j < detalles.size(); j++) {
                DetallePedido det = detalles.get(j);
                for (int k = 0; k < n; k++) {
                    if (copiaProductos[k].getIdProducto() == det.getProducto().getIdProducto()) {
                        unidadesVendidas[k] += det.getCantidad();
                        montoGenerado[k] = Math.round((montoGenerado[k] + det.getSubtotal()) * 100.0) / 100.0;
                    }
                }
            }
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (unidadesVendidas[j] < unidadesVendidas[j + 1]) {
                    int tempUnidades = unidadesVendidas[j];
                    unidadesVendidas[j] = unidadesVendidas[j + 1];
                    unidadesVendidas[j + 1] = tempUnidades;

                    double tempMonto = montoGenerado[j];
                    montoGenerado[j] = montoGenerado[j + 1];
                    montoGenerado[j + 1] = tempMonto;

                    Producto tempProd = copiaProductos[j];
                    copiaProductos[j] = copiaProductos[j + 1];
                    copiaProductos[j + 1] = tempProd;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            String fila = String.format(Locale.US,
                    "Pos #%d | ID: %-3d | %-22s | Unidades vendidas: %-4d | Ingresos: $%-8.2f | Stock actual: %d",
                    (i + 1), copiaProductos[i].getIdProducto(), copiaProductos[i].getNombre(),
                    unidadesVendidas[i], montoGenerado[i], copiaProductos[i].getStock());
            reporte.add(fila);
        }
        return reporte;
    }
}