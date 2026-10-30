package vista;

import modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VistaCafeteria extends JFrame implements ActionListener {
    private GestionCafeteria sistema;
    private JTabbedPane pestañas;
    
    // Panel Productos
    private JTextField txtNombreProd, txtPrecioProd, txtStockProd;
    private JButton btnGuardarProd, btnReponer, btnEditar;
    private JTable tablaProductos;
    private DefaultTableModel modeloTablaProd;

    // Panel Pedidos
    private JTextField txtClientePed;
    private JComboBox<Producto> cbxProductos;
    private JTextField txtCantidadPed;
    private JButton btnAgregarAlPedido, btnConfirmarPedido;
    private JTextArea areaResumenPedido;
    private Pedido pedidoActual;

    // Panel Reportes
    private JButton btnVentasDia, btnProductosMasVendidos;
    private JTextArea areaReportes;

    public VistaCafeteria() {
        sistema = new GestionCafeteria();
        
        setTitle("Sistema de Gestión de Pedidos V2.0 - El Buen Sabor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        pestañas = new JTabbedPane();
        pestañas.addTab("Productos", crearPanelProductos());
        pestañas.addTab("Nuevo Pedido", crearPanelPedidos());
        pestañas.addTab("Reportes", crearPanelReportes());
        
        add(pestañas);
        actualizarTablaProductos();
        actualizarComboProductos();
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlFormulario.setBorder(BorderFactory.createTitledBorder("Gestión de Productos"));
        
        pnlFormulario.add(new JLabel("Nombre del Producto:"));
        txtNombreProd = new JTextField();
        pnlFormulario.add(txtNombreProd);
        
        pnlFormulario.add(new JLabel("Precio Unitario ($):"));
        txtPrecioProd = new JTextField();
        pnlFormulario.add(txtPrecioProd);
        
        pnlFormulario.add(new JLabel("Stock Inicial / Cantidad:"));
        txtStockProd = new JTextField();
        pnlFormulario.add(txtStockProd);
        
        JPanel pnlBotones = new JPanel(new FlowLayout());
        btnGuardarProd = new JButton("Nuevo Producto");
        btnReponer = new JButton("Reponer Stock");
        btnEditar = new JButton("Editar Precio/Nombre");
        
        btnGuardarProd.addActionListener(this);
        btnReponer.addActionListener(this);
        btnEditar.addActionListener(this);
        
        pnlBotones.add(btnGuardarProd);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnReponer);
        pnlFormulario.add(pnlBotones);
        
        panel.add(pnlFormulario, BorderLayout.NORTH);
        
        modeloTablaProd = new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio", "Stock"}, 0);
        tablaProductos = new JTable(modeloTablaProd);
        panel.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel crearPanelPedidos() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlNorte = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Datos del Pedido"));
        
        pnlNorte.add(new JLabel("Cliente:"));
        txtClientePed = new JTextField();
        pnlNorte.add(txtClientePed);
        
        pnlNorte.add(new JLabel("Producto:"));
        cbxProductos = new JComboBox<>();
        pnlNorte.add(cbxProductos);
        
        pnlNorte.add(new JLabel("Cantidad:"));
        JPanel pnlAdd = new JPanel(new BorderLayout());
        txtCantidadPed = new JTextField();
        btnAgregarAlPedido = new JButton("Agregar al Pedido");
        btnAgregarAlPedido.addActionListener(this);
        pnlAdd.add(txtCantidadPed, BorderLayout.CENTER);
        pnlAdd.add(btnAgregarAlPedido, BorderLayout.EAST);
        pnlNorte.add(pnlAdd);
        
        panel.add(pnlNorte, BorderLayout.NORTH);
        
        areaResumenPedido = new JTextArea();
        areaResumenPedido.setEditable(false);
        panel.add(new JScrollPane(areaResumenPedido), BorderLayout.CENTER);
        
        btnConfirmarPedido = new JButton("Confirmar Pedido");
        btnConfirmarPedido.addActionListener(this);
        panel.add(btnConfirmarPedido, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pnlBotones = new JPanel(new FlowLayout());
        btnVentasDia = new JButton("Ventas de Hoy");
        btnProductosMasVendidos = new JButton("Productos Más Vendidos");
        
        btnVentasDia.addActionListener(this);
        btnProductosMasVendidos.addActionListener(this);
        
        pnlBotones.add(btnVentasDia);
        pnlBotones.add(btnProductosMasVendidos);
        
        panel.add(pnlBotones, BorderLayout.NORTH);
        
        areaReportes = new JTextArea();
        areaReportes.setEditable(false);
        areaReportes.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(areaReportes), BorderLayout.CENTER);
        
        return panel;
    }

    private void actualizarTablaProductos() {
        modeloTablaProd.setRowCount(0);
        for (Producto p : sistema.getProductos()) {
            modeloTablaProd.addRow(new Object[]{p.getIdProducto(), p.getNombre(), String.format(Locale.US, "%.2f", p.getPrecioUnitario()), p.getStock()});
        }
    }
    
    private void actualizarComboProductos() {
        cbxProductos.removeAllItems();
        for (Producto p : sistema.getProductos()) {
            cbxProductos.addItem(p);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGuardarProd) accionRegistrarProducto();
        if (e.getSource() == btnReponer) accionReponerStock();
        if (e.getSource() == btnEditar) accionEditarProducto();
        if (e.getSource() == btnAgregarAlPedido) accionAgregarLinea();
        if (e.getSource() == btnConfirmarPedido) accionConfirmarPedido();
        if (e.getSource() == btnVentasDia) accionReporteVentas();
        if (e.getSource() == btnProductosMasVendidos) accionReporteDemanda();
    }

    private void accionRegistrarProducto() {
        try {
            String nombre = txtNombreProd.getText();
            double precio = Double.parseDouble(txtPrecioProd.getText().replace(",", "."));
            int stock = Integer.parseInt(txtStockProd.getText());
            
            if (sistema.registrarProducto(sistema.generarSiguienteIdProducto(), nombre, precio, stock)) {
                JOptionPane.showMessageDialog(this, "Producto registrado con éxito.");
                txtNombreProd.setText(""); txtPrecioProd.setText(""); txtStockProd.setText("");
                actualizarTablaProductos();
                actualizarComboProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar: datos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique que precio y stock sean números.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void accionReponerStock() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = (int) modeloTablaProd.getValueAt(fila, 0);
            int cantidad = Integer.parseInt(txtStockProd.getText());
            if (sistema.reponerStock(id, cantidad)) {
                JOptionPane.showMessageDialog(this, "Stock actualizado.");
                txtStockProd.setText("");
                actualizarTablaProductos();
                actualizarComboProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Cantidad inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ingrese la cantidad en el campo Stock.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void accionEditarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = (int) modeloTablaProd.getValueAt(fila, 0);
            String nombre = txtNombreProd.getText();
            double precio = Double.parseDouble(txtPrecioProd.getText().replace(",", "."));
            
            if (sistema.editarProducto(id, nombre, precio)) {
                JOptionPane.showMessageDialog(this, "Producto modificado.");
                txtNombreProd.setText(""); txtPrecioProd.setText("");
                actualizarTablaProductos();
                actualizarComboProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Error en los datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Llene nombre y precio.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionAgregarLinea() {
        if (pedidoActual == null) {
            String cliente = txtClientePed.getText().trim();
            if (cliente.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el cliente primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            pedidoActual = new Pedido(sistema.generarSiguienteIdPedido(), fecha, cliente);
            areaResumenPedido.setText("Pedido para: " + cliente + "\n-------------------------\n");
        }
        try {
            Producto prod = (Producto) cbxProductos.getSelectedItem();
            int cant = Integer.parseInt(txtCantidadPed.getText());
            
            if (prod != null && sistema.agregarLineaAPedido(pedidoActual, prod.getIdProducto(), cant)) {
                areaResumenPedido.append(cant + "x " + prod.getNombre() + "\n");
                txtCantidadPed.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Stock insuficiente o cantidad inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ingrese cantidad numérica válida.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionConfirmarPedido() {
        if (pedidoActual == null || pedidoActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El pedido está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sistema.confirmarPedido(pedidoActual)) {
            String msj = String.format(Locale.US, "¡Venta Confirmada!\nSubtotal: $%.2f\nIVA: $%.2f\nTOTAL: $%.2f", 
                    pedidoActual.calcularSubtotalSinIva(), pedidoActual.calcularIva(), pedidoActual.getTotal());
            JOptionPane.showMessageDialog(this, msj);
            pedidoActual = null;
            areaResumenPedido.setText("");
            txtClientePed.setText("");
            actualizarTablaProductos();
            actualizarComboProductos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al confirmar (Stock modificado).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void accionReporteVentas() {
        String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        double total = sistema.calcularTotalVentasPorFecha(hoy);
        areaReportes.setText("VENTAS DE HOY (" + hoy + ")\n------------------------\nTotal recaudado: $" + total);
    }
    
    private void accionReporteDemanda() {
        areaReportes.setText("RANKING DE PRODUCTOS (MAYOR A MENOR DEMANDA)\n-----------------------------------------\n" + 
                             sistema.obtenerReporteDemandaProductos());
    }
}