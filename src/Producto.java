import java.util.Locale;

public class Producto {
    private int idProducto;
    private String nombre;
    private double precioUnitario;
    private int stock;

    public Producto(int idProducto, String nombre, double precioUnitario, int stock) {
        this.idProducto = idProducto;
        this.nombre = limpiarTexto(nombre);
        this.precioUnitario = redondear(precioUnitario);
        this.stock = stock;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = limpiarTexto(nombre);
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario > 0) {
            this.precioUnitario = redondear(precioUnitario);
        }
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock >= 0) {
            this.stock = stock;
        }
    }

    public boolean aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            return false;
        }
        this.stock += cantidad;
        return true;
    }

    public boolean disminuirStock(int cantidad) {
        if (cantidad <= 0 || cantidad > this.stock) {
            return false;
        }
        this.stock -= cantidad;
        return true;
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
        return idProducto + ";" + nombre + ";" + String.format(Locale.US, "%.2f", precioUnitario) + ";" + stock;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "ID: %-4d | Nombre: %-25s | Precio: $%-7.2f | Stock: %d",
                idProducto, nombre, precioUnitario, stock);
    }
}