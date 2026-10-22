import java.util.ArrayList;
import java.util.Locale;

public class Pedido {
    private int idPedido;
    private String fecha;
    private String cliente;
    private ArrayList<DetallePedido> detalles;
    private double total;

    public Pedido(int idPedido, String fecha, String cliente) {
        this.idPedido = idPedido;
        this.fecha = limpiarTexto(fecha);
        this.cliente = limpiarTexto(cliente);
        this.detalles = new ArrayList<DetallePedido>();
        this.total = 0.0;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public ArrayList<DetallePedido> getDetalles() {
        return detalles;
    }

    public double getTotal() {
        return total;
    }

    public void agregarDetalle(DetallePedido detalle) {
        if (detalle != null) {
            this.detalles.add(detalle);
            calcularTotal();
        }
    }

    public double calcularTotal() {
        double suma = 0.0;
        for (int i = 0; i < detalles.size(); i++) {
            suma += detalles.get(i).calcularSubtotal();
        }
        this.total = redondear(suma);
        return this.total;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().replace(";", ",");
    }

    public String aLineaArchivo() {
        return idPedido + ";" + fecha + ";" + cliente + ";" + String.format(Locale.US, "%.2f", total);
    }

    @Override
    public String toString() {
        StringBuilder constructor = new StringBuilder();
        constructor.append(String.format(Locale.US, "Pedido Nro: %-4d | Fecha: %-10s | Cliente: %-20s | Total: $%.2f\n",
                idPedido, fecha, cliente, total));
        for (int i = 0; i < detalles.size(); i++) {
            constructor.append(detalles.get(i).toString()).append("\n");
        }
        return constructor.toString();
    }
}