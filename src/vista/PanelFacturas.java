package vista;

import controlador.ClienteController;
import modelo.Cliente;
import modelo.Factura;
import controlador.FacturaController;
import modelo.ItemFactura;
import util.Capitalizador;
import util.GeneradorPDF;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PanelFacturas extends JPanel {
            private Cliente cliente;
            private Factura factura;
            private FacturaController facturaController;
            private Container panelAnterior;
            private JTextField txtNombreCliente;
            private JTextField txtTelefono;
            private JTable tablaItems;
            private DefaultTableModel modeloTabla;
            private JFormattedTextField txtAbono;
            private JLabel lblTotal;
            private JLabel lblAbono;
            private JLabel lblEstado;
            private JButton btnAgregarItem;
            private JButton btnGuardar;
            private JButton btnCancelar;
            private JLabel lblSaldo;
            private JTextField txtFecha;
            private JButton btnGenerarPDF;
            private ClienteController clienteController = new ClienteController();
            private JComboBox<String> comboEstadoTrabajo;


            public PanelFacturas(Cliente cliente, Factura factura) {
                this.cliente = cliente;
                this.factura = (factura != null && factura.getId() != 0) ? factura : new Factura();

                if (this.factura.getId() == 0) {
                    this.factura.setIdCliente(cliente.getId());
                    this.factura.setItems(new ArrayList<>());
                }

                initComponents();
                cargarDatosFactura();
                actualizarTabla();
                actualizarSaldos();
            }


            public PanelFacturas( Cliente cliente){
                this(cliente,null);
            }

            public PanelFacturas(Cliente cliente, Factura factura, Container panelAnterior) {
                this(cliente, factura); // llama al constructor existente
                // aquí podrías guardar panelAnterior como atributo si quieres usarlo
                this.panelAnterior = panelAnterior; // suponiendo que declaraste: private Container panelAnterior;
            }



            private void initComponents() {
                setLayout(new BorderLayout(10, 10));



                txtFecha = new JTextField();
                txtFecha.setEditable(false);
                txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(factura.getFecha()));
                add(new JLabel("Fecha:"));
                add(txtFecha);

                JPanel panelCliente = new JPanel(new GridLayout(2, 2, 5, 5));
                panelCliente.setBorder(BorderFactory.createTitledBorder("Cliente"));
                panelCliente.add(new JLabel("Nombre:"));
                txtNombreCliente = new JTextField(cliente.getNombre());
                txtNombreCliente.setEditable(false);
                panelCliente.add(txtNombreCliente);
                panelCliente.add(new JLabel("Teléfono:"));
                txtTelefono = new JTextField(cliente.getTelefono());
                txtTelefono.setEditable(false);
                panelCliente.add(txtTelefono);
                add(panelCliente, BorderLayout.NORTH);

                modeloTabla = new DefaultTableModel(new Object[]{"Tipo", "Descripción", "Cantidad", "Valor Unitario", "Subtotal"}, 0);
                tablaItems = new JTable(modeloTabla);
                add(new JScrollPane(tablaItems), BorderLayout.CENTER);

                JPanel panelInferior = new JPanel(new BorderLayout(10, 10));

                JPanel panelTotales = new JPanel(new GridBagLayout());
                panelTotales.setBorder(BorderFactory.createTitledBorder("Totales"));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 10, 5, 10);
                gbc.anchor = GridBagConstraints.WEST;

                int row = 0;
                gbc.gridx = 0; gbc.gridy = row;
                panelTotales.add(new JLabel("Total:"), gbc);
                lblTotal = new JLabel("$0.00");
                gbc.gridx = 1;
                panelTotales.add(lblTotal, gbc);

                gbc.gridx = 2;
                panelTotales.add(new JLabel("Abono:"), gbc);
                NumberFormat formatoMoneda = NumberFormat.getNumberInstance(new Locale("es", "CO"));
                txtAbono = new JFormattedTextField(formatoMoneda);
                txtAbono.setColumns(10);
                gbc.gridx = 3;
                panelTotales.add(txtAbono, gbc);

                txtAbono.setInputVerifier(new InputVerifier() {
                    @Override
                    public boolean verify(JComponent input) {
                        JFormattedTextField campo = (JFormattedTextField) input;
                        try {
                            campo.commitEdit();
                            return true;
                        } catch (ParseException e) {
                            JOptionPane.showMessageDialog(null, "Por favor, ingresa un número válido para el abono.");
                            return false;
                        }
                    }
                });
                txtAbono.getDocument().addDocumentListener(new DocumentListener() {
                    boolean actualizando = false;

                    private void formatearTexto() {
                        if (actualizando) return;
                        actualizando = true;

                        SwingUtilities.invokeLater(() -> {
                            String texto = txtAbono.getText().replaceAll("[^\\d]", "");
                            if (!texto.isEmpty()) {
                                try {
                                    long valor = Long.parseLong(texto);
                                    NumberFormat formatoCOP = NumberFormat.getNumberInstance(new Locale("es", "CO"));
                                    String formateado = formatoCOP.format(valor);
                                    txtAbono.setText(formateado);
                                    txtAbono.setCaretPosition(txtAbono.getText().length());
                                } catch (NumberFormatException e) {
                                    // Ignorar errores
                                }
                            } else {
                                txtAbono.setText("");
                            }
                            actualizando = false;
                        });
                    }

                    @Override public void insertUpdate(DocumentEvent e) { formatearTexto(); }
                    @Override public void removeUpdate(DocumentEvent e) { formatearTexto(); }
                    @Override public void changedUpdate(DocumentEvent e) { formatearTexto(); }
                });



                txtAbono.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        actualizarSaldos();
                    }
                });

                gbc.gridx = 4;
                panelTotales.add(new JLabel("Estado:"), gbc);
                lblEstado = new JLabel("PENDIENTE");
                gbc.gridx = 5;
                panelTotales.add(lblEstado, gbc);

                row++;
                gbc.gridx = 0; gbc.gridy = row;
                panelTotales.add(new JLabel("Saldo pendiente:"), gbc);
                lblSaldo = new JLabel("$0.00");
                gbc.gridx = 1;
                panelTotales.add(lblSaldo, gbc);

                gbc.gridx = 2;
                panelTotales.add(new JLabel("Abono ingresado:"), gbc);
                lblAbono = new JLabel("$0.00");
                gbc.gridx = 3;
                panelTotales.add(lblAbono, gbc);

                gbc.gridx = 4;
                panelTotales.add(new JLabel("Estado del trabajo:"), gbc);
                comboEstadoTrabajo = new JComboBox<>();
                comboEstadoTrabajo.addItem("Pendiente");
                comboEstadoTrabajo.addItem("En proceso");
                comboEstadoTrabajo.addItem("Terminado");
                comboEstadoTrabajo.addItem("Entregado");

                gbc.gridx = 5;
                panelTotales.add(comboEstadoTrabajo, gbc);


                JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                btnAgregarItem = new JButton("Agregar Ítem");
                btnGuardar = new JButton("Guardar");
                btnCancelar = new JButton("Cancelar");
                btnGenerarPDF = new JButton("Generar PDF");
                JButton btnEliminarItem = new JButton("Eliminar Ítem");

                panelBotones.add(btnAgregarItem);
                panelBotones.add(btnEliminarItem);
                panelBotones.add(btnGenerarPDF);
                panelBotones.add(btnGuardar);
                panelBotones.add(btnCancelar);


                panelInferior.add(panelTotales, BorderLayout.CENTER);
                panelInferior.add(panelBotones, BorderLayout.SOUTH);

                add(panelInferior, BorderLayout.SOUTH);

                btnAgregarItem.addActionListener(e -> agregarItem());

                btnEliminarItem.addActionListener(e -> {
                    int filaSeleccionada = tablaItems.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Deseas eliminar este ítem?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                        if (confirmacion == JOptionPane.YES_OPTION) {
                            modeloTabla.removeRow(filaSeleccionada);
                            factura.getItems().remove(filaSeleccionada);
                            actualizarSaldos();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Selecciona un ítem para eliminar.");
                    }
                });

                btnGuardar.addActionListener(new ActionListener() {
                    private final FacturaController facturaController = new FacturaController();

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            // 1. Obtener datos del formulario
                            String nombreCliente = txtNombreCliente.getText().trim();
                            String telefonoCliente = txtTelefono.getText().trim();

                            int idCliente = clienteController.obtenerOInsertarCliente(nombreCliente, telefonoCliente);

                            factura.setIdCliente(idCliente);
                            factura.setFecha(new java.sql.Date(System.currentTimeMillis()));

                            String abonoTexto = txtAbono.getText().trim()
                                    .replace("$", "")
                                    .replace(".", "")
                                    .replace(",", ".");

                            double abono = 0.0;
                            if (!abonoTexto.isEmpty()) {
                                abono = Double.parseDouble(abonoTexto);
                            }
                            factura.setAbono(abono);


                            String estadoSeleccionado = (String) comboEstadoTrabajo.getSelectedItem();
                            factura.setEstadoTrabajo(estadoSeleccionado);

                            // 2. Limpiar y agregar ítems de la tabla
                            factura.getItems().clear(); // Asegúrate de limpiar los ítems antes de agregar nuevos
                            for (int i = 0; i < tablaItems.getRowCount(); i++) {
                                String tipo = (String) tablaItems.getValueAt(i, 0);
                                String descripcion = (String) tablaItems.getValueAt(i, 1);
                                int cantidad = Integer.parseInt(tablaItems.getValueAt(i, 2).toString());
                                String valorTexto = tablaItems.getValueAt(i, 3).toString().trim()
                                        .replaceAll("[^\\d,\\.]", "")
                                        .replace(".", "")
                                        .replace(",", ".");

                                double valorUnitario = Double.parseDouble(valorTexto);
                                String empleado = (String) tablaItems.getValueAt(i, 4);

                                ItemFactura item = new ItemFactura(tipo, descripcion, cantidad, valorUnitario, nombreCliente);
                                factura.agregarItem(item);
                            }

                            // 3. Guardar o actualizar
                            facturaController.guardarOActualizarFacturaConItems(factura);

                            // 4. Limpiar formulario (opcional)
                            limpiarFormulario();

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Error al guardar la factura: " + ex.getMessage());
                            ex.printStackTrace();
                        }

                        cerrarVentana();
                    }
                });




                btnCancelar.addActionListener(e -> {
                    JFrame ventana = (JFrame) SwingUtilities.getWindowAncestor(this);
                    if (panelAnterior != null) {
                        ventana.setContentPane(panelAnterior);
                        ventana.revalidate();
                        ventana.repaint();
                    } else {
                        ventana.dispose(); // por si se abre como ventana independiente
                    }
                });
                btnGenerarPDF.addActionListener(e -> {
                    if (factura != null) {
                        GeneradorPDF.generarFacturaPDF(factura);
                    } else {
                        JOptionPane.showMessageDialog(this, "No hay factura para generar PDF.");
                    }
                });


                actualizarTabla();
                actualizarSaldos();

            }
            private void limpiarFormulario() {
                txtNombreCliente.setText("");
                txtTelefono.setText("");
                txtAbono.setText("");
                comboEstadoTrabajo.setSelectedIndex(0);
                modeloTabla.setRowCount(0); // limpia la tabla
            }

            private void cargarDatosFactura() {


                if (factura != null) {
                    txtNombreCliente.setText(cliente.getNombre());
                    txtTelefono.setText(cliente.getTelefono());

                    double abono = factura.getAbono();
                    txtAbono.setText(String.format("%,.0f", abono));
                    lblAbono.setText(String.format("$ %,.2f",abono));

                    double totalF = factura.getTotal();
                    Double saldoPendiente = totalF - abono;

                    lblSaldo.setText(String.format("$ %,.2f", saldoPendiente));
                    comboEstadoTrabajo.setSelectedItem(factura.getEstadoTrabajo());
                    double total = factura.getTotal();

                    // puedes cargar más datos si es necesario
                }
                actualizarSaldos();
            }

            private void agregarItem() {
                JRadioButton bordadoBtn = new JRadioButton("Bordado");
                JRadioButton programaBtn = new JRadioButton("Programa");
                ButtonGroup grupo = new ButtonGroup();
                grupo.add(bordadoBtn);
                grupo.add(programaBtn);

                JPanel panel = new JPanel(new GridLayout(2, 1));
                panel.add(bordadoBtn);
                panel.add(programaBtn);

                int result = JOptionPane.showConfirmDialog(this, panel, "Selecciona el tipo", JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) return;

                String tipo = bordadoBtn.isSelected() ? "bordado" : (programaBtn.isSelected() ? "programa" : null);
                if (tipo == null) {
                    JOptionPane.showMessageDialog(this, "Debes seleccionar un tipo.");
                    return;
                }

                String desc = JOptionPane.showInputDialog(this, "Descripción:");
                if (desc == null || desc.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "La descripción no puede estar vacía.");
                    return;
                }
                desc = Capitalizador.capitalizarPorPalabras(desc.trim());

                int cant;
                try {
                    cant = Integer.parseInt(JOptionPane.showInputDialog(this, "Cantidad:"));
                    if (cant <= 0) throw new NumberFormatException();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida.");
                    return;
                }


                double valor;
                try {

                    valor = Double.parseDouble(JOptionPane.showInputDialog(this, "Valor unitario:"));
                    if (valor <= 0) throw new NumberFormatException();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Valor unitario inválido.");
                    return;
                }




                ItemFactura item = new ItemFactura();
                item.setTipo(tipo);
                item.setDescripcion(desc);
                item.setCantidad(cant);
                item.setValorUnitario(valor);
                factura.getItems().add(item);

                actualizarTabla();
            }

            private void actualizarTabla() {
                modeloTabla.setRowCount(0);
                factura.calcularTotales();


                NumberFormat formatoPesos = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

                for (ItemFactura it : factura.getItems()) {
                    modeloTabla.addRow(new Object[]{
                            it.getTipo(),
                            it.getDescripcion(),
                            it.getCantidad(),
                            formatoPesos.format(it.getValorUnitario()),

                            formatoPesos.format(it.getSubtotal())
                    });
                }

                lblTotal.setText(formatoPesos.format(factura.getTotal()));
                actualizarSaldos();
            }



            private void actualizarSaldos() {
                if (factura == null) return;

                double total = factura.getTotal();
                double abono = 0.0;
                try {
                    Object valor = txtAbono.getValue();
                    if (valor instanceof Number) {
                        abono = ((Number) valor).doubleValue();
                    } else {
                        // En caso de que no se pueda leer como número, usamos el abono guardado
                        abono = factura.getAbono();
                    }
                } catch (Exception e) {
                    abono = factura.getAbono(); // Usar el abono guardado si falla
                }

                double saldo = total - abono;

                NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
                lblTotal.setText(formatoMoneda.format(total));
                lblAbono.setText(formatoMoneda.format(abono));
                lblSaldo.setText(formatoMoneda.format(saldo));

                if (saldo <= 0.01) {
                    lblEstado.setText("Cancelada");
                } else {
                    lblEstado.setText("Pendiente");
                }
            }







            private void cerrarVentana() {
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) {
                    w.dispose();
                }
            }
        }
