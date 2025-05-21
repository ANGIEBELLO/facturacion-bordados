package controlador;

import modelo.Cliente;
import util.ConexionBD;
import util.Capitalizador;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;




public class ClienteController {
    /**
     * Agrega un nuevo cliente. Retorna true si la operación fue exitosa.
     * El nombre se capitaliza por cada palabra antes de guardar.
     */
    public boolean agregarCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, telefono) VALUES (?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Capitalizar nombre completo
            String nombreFormateado = Capitalizador.capitalizarPorPalabras(cliente.getNombre().trim());
            stmt.setString(1, nombreFormateado);
            stmt.setString(2, cliente.getTelefono());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza datos de un cliente existente por id.
     * El nombre se capitaliza por cada palabra antes de actualizar.
     */
    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, telefono = ? WHERE id_cliente = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String nombreFormateado = Capitalizador.capitalizarPorPalabras(cliente.getNombre().trim());
            stmt.setString(1, nombreFormateado);
            stmt.setString(2, cliente.getTelefono());
            stmt.setInt(3, cliente.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un cliente por su id.
     */
    public boolean eliminarCliente(int id) {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera todos los clientes.
     */
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, telefono FROM clientes";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("telefono")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca clientes por nombre o teléfono usando LIKE.
     */
    public List<Cliente> buscarPorNombreOTelefono(String texto) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, telefono FROM clientes WHERE nombre LIKE ? OR telefono LIKE ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String criterio = "%" + texto + "%";
            stmt.setString(1, criterio);
            stmt.setString(2, criterio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Cliente(
                            rs.getInt("id_cliente"),
                            rs.getString("nombre"),
                            rs.getString("telefono")
                    ));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar cliente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Verifica existencia por nombre únicamente.
     */
    public boolean clienteExistePorNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE nombre = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Capitalizador.capitalizarPorPalabras(nombre.trim()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica existencia por nombre y teléfono.
     */
    public boolean clienteExiste(String nombre, String telefono) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE nombre = ? AND telefono = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Capitalizador.capitalizarPorPalabras(nombre.trim()));
            stmt.setString(2, telefono.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public int obtenerOInsertarCliente(String nombre, String telefono) {
        int idCliente = -1;

        // Aplicar formato al nombre: Mayúscula inicial y minúsculas después
        nombre = formatearNombre(nombre);

        // Eliminar espacios y caracteres no numéricos del teléfono
        telefono = telefono.replaceAll("[^\\d]", "");

        String sqlBuscar = "SELECT id_cliente FROM clientes WHERE nombre = ? AND telefono = ?";
        String sqlInsertar = "INSERT INTO clientes (nombre, telefono) VALUES (?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            // Buscar cliente existente
            try (PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscar)) {
                stmtBuscar.setString(1, nombre);
                stmtBuscar.setString(2, telefono);
                ResultSet rs = stmtBuscar.executeQuery();

                if (rs.next()) {
                    idCliente = rs.getInt("id_cliente");
                } else {
                    // Insertar nuevo cliente
                    try (PreparedStatement stmtInsertar = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
                        stmtInsertar.setString(1, nombre);
                        stmtInsertar.setString(2, telefono);
                        stmtInsertar.executeUpdate();

                        ResultSet rsInsert = stmtInsertar.getGeneratedKeys();
                        if (rsInsert.next()) {
                            idCliente = rsInsert.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return idCliente;
    }
    private String formatearNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) return nombre;

        // Divide el nombre por espacios para aplicar formato a cada palabra
        String[] partes = nombre.toLowerCase().split("\\s+");
        StringBuilder nombreFormateado = new StringBuilder();

        for (String parte : partes) {
            if (!parte.isEmpty()) {
                nombreFormateado.append(Character.toUpperCase(parte.charAt(0)))
                        .append(parte.substring(1))
                        .append(" ");
            }
        }

        return nombreFormateado.toString().trim();
    }



}
