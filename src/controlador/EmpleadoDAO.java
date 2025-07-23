package controlador;


import modelo.Empleado;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public boolean agregarEmpleado(Empleado empleado) {
        String sql = "INSERT INTO empleados (nombre, cedula, telefono, cargo) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empleado.getNombre());
            stmt.setString(2, empleado.getCedula());
            stmt.setString(3, empleado.getTelefono());
            stmt.setString(4, empleado.getCargo());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Empleado> listarEmpleados() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleados";

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Empleado emp = new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        rs.getString("telefono"),
                        rs.getString("cargo")
                );
                lista.add(emp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean eliminarEmpleado(int id) {
        String sql = "DELETE FROM empleados WHERE id_empleado = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEmpleado(Empleado empleado) {
        String sql = "UPDATE empleados SET nombre=?, cedula=?, telefono=?, cargo=? WHERE id_empleado=?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empleado.getNombre());
            stmt.setString(2, empleado.getCedula());
            stmt.setString(3, empleado.getTelefono());
            stmt.setString(4, empleado.getCargo());
            stmt.setInt(5, empleado.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Empleado> obtenerTodos() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT id_empleado, nombre FROM empleados";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setId(rs.getInt("id_empleado"));
                emp.setNombre(rs.getString("nombre"));
                empleados.add(emp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return empleados;
    }

    public Empleado obtenerEmpleadoPorId(int id) {
        Empleado emp = null;
        try (Connection con = ConexionBD.obtenerConexion()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM empleados WHERE id_empleado = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                emp = new Empleado();
                emp.setId(rs.getInt("id_empleado"));
                emp.setNombre(rs.getString("nombre"));
                emp.setCedula(rs.getString("cedula"));
                emp.setTelefono(rs.getString("telefono"));
                emp.setCargo(rs.getString("cargo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emp;
    }




}