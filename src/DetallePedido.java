import java.util.Locale;

public class DetallePedido {
    private int idPedido;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetallePedido(int idPedido, Producto producto, int cantidad, double precioUnitario) {
        this.idPedido = idPedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = redondear(precioUnitario);
        this.subtotal = calcularSubtotal();
    }

    public int getIdPedido() {
        return idPedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double calcularSubtotal() {
        this.subtotal = redondear(this.cantidad * this.precioUnitario);
        return this.subtotal;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public String aLineaArchivo() {
        return idPedido + ";" + producto.getIdProducto() + ";" + cantidad + ";"
                + String.format(Locale.US, "%.2f", precioUnitario) + ";"
                + String.format(Locale.US, "%.2f", subtotal);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "   -> Prod ID: %-3d | %-22s | Cant: %-3d | P.Unit: $%-6.2f | Subtotal: $%-7.2f",
                producto.getIdProducto(), producto.getNombre(), cantidad, precioUnitario, subtotal);
    }
}